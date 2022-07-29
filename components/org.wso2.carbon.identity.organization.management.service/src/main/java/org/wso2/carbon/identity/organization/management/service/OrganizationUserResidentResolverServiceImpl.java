/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.organization.management.service;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.organization.management.authz.service.OrganizationManagementAuthorizationManager;
import org.wso2.carbon.identity.organization.management.authz.service.exception.OrganizationManagementAuthzServiceServerException;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RESOLVING_USER_FROM_RESIDENT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_NO_USERNAME_OR_ID_TO_RESOLVE_USER_FROM_RESIDENT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ROOT_ORG_ID;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleClientException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;

/**
 * Service implementation to resolve user's resident organization.
 */
public class OrganizationUserResidentResolverServiceImpl implements OrganizationUserResidentResolverService {

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    @Override
    public Optional<User> resolveUserFromResidentOrganization(String userName, String userId,
                                                              String accessedOrganizationId)
            throws OrganizationManagementException {

        User resolvedUser = null;
        try {
            if (userName == null && userId == null) {
                throw handleClientException(ERROR_CODE_NO_USERNAME_OR_ID_TO_RESOLVE_USER_FROM_RESIDENT_ORG);
            }
            List<String> ancestorOrganizationIds =
                    organizationManagementDAO.getAncestorOrganizationIds(accessedOrganizationId);
            if (ancestorOrganizationIds != null) {
                for (String organizationId : ancestorOrganizationIds) {
                    String associatedTenantDomainForOrg = resolveTenantDomainForOrg(organizationId);
                    if (associatedTenantDomainForOrg != null) {
                        AbstractUserStoreManager userStoreManager =
                                getUserStoreManager(associatedTenantDomainForOrg);
                        User user;
                        if (userName != null && userStoreManager.isExistingUser(userName)) {
                            user = userStoreManager.getUser(null, userName);
                        } else if (userId != null && userStoreManager.isExistingUserWithID(userId)) {
                            user = userStoreManager.getUser(userId, null);
                        } else {
                            continue;
                        }
                        // Check whether user has any association against the org the user is trying to access.
                        boolean userHasAccessPermissions =
                                OrganizationManagementAuthorizationManager.getInstance()
                                        .hasUserOrgAssociation(user.getUserID(), accessedOrganizationId);
                        if (userHasAccessPermissions) {
                            resolvedUser = user;
                            break;
                        }
                    }
                }
            }
        } catch (UserStoreException | OrganizationManagementAuthzServiceServerException e) {
            throw handleServerException(ERROR_CODE_ERROR_WHILE_RESOLVING_USER_FROM_RESIDENT_ORG, e, userName,
                    accessedOrganizationId);
        }
        return Optional.ofNullable(resolvedUser);
    }

    private String resolveTenantDomainForOrg(String organizationId) throws OrganizationManagementServerException {

        if (StringUtils.equals(ROOT_ORG_ID, organizationId)) {
            // super tenant domain will be returned.
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        } else {
            return organizationManagementDAO.resolveTenantDomain(organizationId);
        }
    }

    private AbstractUserStoreManager getUserStoreManager(String tenantDomain) throws UserStoreException {

        int tenantId = OrganizationManagementDataHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);
        RealmService realmService = OrganizationManagementDataHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);
        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }
}
