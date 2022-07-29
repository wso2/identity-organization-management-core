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

package org.wso2.carbon.identity.organization.management.authz.service.util;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.persistence.UmPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.authz.service.internal.OrganizationManagementAuthzServiceHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wso2.carbon.identity.organization.management.authz.service.constant.AuthorizationConstants.PERMISSION_SPLITTER;

/**
 * This class provides utility functions for the organization management authorization.
 */
public class OrganizationManagementAuthzUtil {

    /**
     * Get the userstore manager by user.
     *
     * @param user The user object.
     * @return The userstore manager.
     * @throws UserStoreException Error while getting the userstore manager.
     */
    public static UserStoreManager getUserStoreManager(User user) throws UserStoreException {

        RealmService realmService = OrganizationManagementAuthzServiceHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(user
                .getTenantDomain()));
        String userStoreDomain = user.getUserStoreDomain();
        if (IdentityUtil.getPrimaryDomainName().equals(userStoreDomain) || userStoreDomain == null) {
            return (UserStoreManager) tenantUserRealm.getUserStoreManager();
        }
        return ((UserStoreManager) tenantUserRealm.getUserStoreManager()).getSecondaryUserStoreManager(userStoreDomain);
    }

    /**
     * Get a new Jdbc template.
     *
     * @return a new Jdbc template.
     */
    public static NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(UmPersistenceManager.getInstance().getDataSource());
    }

    public static List<String> getAllowedPermissions(String resourceId) {

        String[] permissionParts = resourceId.split(PERMISSION_SPLITTER);
        List<String> allowedPermissions = new ArrayList<>();
        for (int i = 0; i < permissionParts.length - 1; i++) {
            allowedPermissions.add(String.join(PERMISSION_SPLITTER,
                    subArray(permissionParts, permissionParts.length - i)));
        }
        return allowedPermissions;
    }

    private static <T> T[] subArray(T[] array, int end) {

        return Arrays.copyOfRange(array, 0, end);
    }
}
