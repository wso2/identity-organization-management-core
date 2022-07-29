/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.organization.management.authz.service.dao;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.organization.management.authz.service.exception.OrganizationManagementAuthzServiceServerException;
import org.wso2.carbon.identity.organization.management.authz.service.internal.OrganizationManagementAuthzServiceHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.organization.management.authz.service.constant.AuthorizationConstants.ROOT;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.CHECK_USER_HAS_PERMISSION_TO_ORG_THROUGH_GROUPS_ASSIGNED_TO_ROLES;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.CHECK_USER_HAS_PERMISSION_TO_ORG_THROUGH_USER_ROLE_ASSIGNMENT;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.GET_ORGANIZATION_ID_BY_NAME;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.IS_GROUP_AUTHORIZED;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.IS_USER_AUTHORIZED;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.PERMISSION_LIST_PLACEHOLDER;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_ORGANIZATION_ID;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_ORGANIZATION_NAME;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_USER_ID;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.VIEW_ID_COLUMN;
import static org.wso2.carbon.identity.organization.management.authz.service.util.OrganizationManagementAuthzUtil.getAllowedPermissions;
import static org.wso2.carbon.identity.organization.management.authz.service.util.OrganizationManagementAuthzUtil.getNewTemplate;

/**
 * Implementation of {@link OrganizationManagementAuthzDAO}.
 */
public class OrganizationManagementAuthzDAOImpl implements OrganizationManagementAuthzDAO {

    @Override
    public boolean isUserAuthorized(String userId, String resourceId, String orgId)
            throws OrganizationManagementAuthzServiceServerException {

        String permissionPlaceholder = "PERMISSION_";
        List<String> permissions = getAllowedPermissions(resourceId);
        List<String> permissionPlaceholders = new ArrayList<>();
        // Constructing the placeholders required to hold the permission strings in the named prepared statement.
        for (int i = 1; i <= permissions.size(); i++) {
            permissionPlaceholders.add(":" + permissionPlaceholder + i + ";");
        }
        String placeholder = String.join(", ", permissionPlaceholders);
        String sqlStmt = IS_USER_AUTHORIZED.replace(PERMISSION_LIST_PLACEHOLDER, placeholder);

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        boolean isAuthorized;
        try {
            isAuthorized = namedJdbcTemplate.fetchSingleRecord(sqlStmt,
                    (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_USER_ID, userId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_ORGANIZATION_ID, orgId);
                        int index = 1;
                        for (String permission : permissions) {
                            namedPreparedStatement.setString(permissionPlaceholder + index, permission);
                            index++;
                        }
                    });

            if (isAuthorized) {
                return true;
            }
        } catch (DataAccessException e) {
            throw new OrganizationManagementAuthzServiceServerException(e);
        }

        try {
            AbstractUserStoreManager userStoreManager =
                    getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            /*
            Currently, userstore groups in the same organization can be assigned to a role.
            A user can be belonged only to the groups in the userstore where user resides.
            So group authorization is not required if the user is not inside the same org.
             */
            boolean isUserExists = userStoreManager.isExistingUserWithID(userId);
            if (!isUserExists) {
                return false;
            }
            List<Group> groupListOfUser = userStoreManager.getGroupListOfUser(userId, null, null);

            String groupSqlStmt = IS_GROUP_AUTHORIZED.replace(PERMISSION_LIST_PLACEHOLDER, placeholder);

            for (Group group: groupListOfUser) {
                NamedJdbcTemplate groupNamedJdbcTemplate = getNewTemplate();
                    isAuthorized = groupNamedJdbcTemplate.fetchSingleRecord(groupSqlStmt,
                            (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                            namedPreparedStatement -> {
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_USER_ID, group.getGroupID());
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_ORGANIZATION_ID, orgId);
                                int index = 1;
                                for (String permission : permissions) {
                                    namedPreparedStatement.setString(permissionPlaceholder + index, permission);
                                    index++;
                                }
                            });

                if (isAuthorized) {
                    return true;
                }
            }
        } catch (UserStoreException | DataAccessException e) {
            throw new OrganizationManagementAuthzServiceServerException(e);
        }

        return false;
    }

    @Override
    public boolean hasUserOrgAssociation(String userId, String orgId)
            throws OrganizationManagementAuthzServiceServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        boolean hasOrgAssociation;
        try {
            hasOrgAssociation =
                    namedJdbcTemplate.fetchSingleRecord(CHECK_USER_HAS_PERMISSION_TO_ORG_THROUGH_USER_ROLE_ASSIGNMENT,
                            (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                            namedPreparedStatement -> {
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_USER_ID, userId);
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_ORGANIZATION_ID, orgId);
                            });
            if (hasOrgAssociation) {
                return true;
            }
        } catch (DataAccessException e) {
            throw new OrganizationManagementAuthzServiceServerException(e);
        }

        try {
            AbstractUserStoreManager userStoreManager =
                    getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            /*
            Currently, userstore groups in the same organization can be assigned to a role.
            A user can be belonged only to the groups in the userstore where user resides.
            So group authorization is not required if the user is not inside the same org.
             */
            boolean isUserExists = userStoreManager.isExistingUserWithID(userId);
            if (!isUserExists) {
                return false;
            }
            List<Group> groupListOfUser = userStoreManager.getGroupListOfUser(userId, null, null);
            for (Group group : groupListOfUser) {
                NamedJdbcTemplate groupNamedJdbcTemplate = getNewTemplate();
                hasOrgAssociation = groupNamedJdbcTemplate.fetchSingleRecord(
                        CHECK_USER_HAS_PERMISSION_TO_ORG_THROUGH_GROUPS_ASSIGNED_TO_ROLES,
                        (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                        namedPreparedStatement -> {
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_USER_ID, group.getGroupID());
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_ORGANIZATION_ID, orgId);
                        });
                if (hasOrgAssociation) {
                    return true;
                }
            }
        } catch (UserStoreException | DataAccessException e) {
            throw new OrganizationManagementAuthzServiceServerException(e);
        }
        return false;
    }

    @Override
    public String getRootOrganizationId() throws OrganizationManagementAuthzServiceServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_ID_BY_NAME,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_ID_COLUMN), namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_ORGANIZATION_NAME, ROOT));
        } catch (DataAccessException e) {
            throw new OrganizationManagementAuthzServiceServerException(e);
        }
    }

    private AbstractUserStoreManager getUserStoreManager(int tenantId) throws UserStoreException {

        RealmService realmService = OrganizationManagementAuthzServiceHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);

        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }
}
