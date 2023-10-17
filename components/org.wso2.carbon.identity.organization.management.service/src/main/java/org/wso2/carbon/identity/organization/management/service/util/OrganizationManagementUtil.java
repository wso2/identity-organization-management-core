/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.service.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class provides utility functions for the Organization Management.
 */
public class OrganizationManagementUtil {

    /**
     * Check whether the tenant is an organization.
     *
     * @param tenantDomain Tenant domain.
     * @return True if the tenant is an organization.
     * @throws OrganizationManagementException If an error occurs while checking whether the tenant is an organization.
     */
    public static boolean isOrganization(String tenantDomain) throws OrganizationManagementException {

        RealmService realmService = OrganizationManagementDataHolder.getInstance().getRealmService();
        int tenantId;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new OrganizationManagementException("Error while retrieving the tenant id for the tenant domain: " +
                    tenantDomain, e);
        }
        return isOrganization(tenantId);
    }

    /**
     * Check whether the tenant is an organization.
     *
     * @param tenantId Tenant id.
     * @return True if the tenant is an organization.
     * @throws OrganizationManagementException If an error occurs while checking whether the tenant is an organization.
     */
    public static boolean isOrganization(int tenantId) throws OrganizationManagementException {

        // If the tenant is super tenant, it is not an organization.
        if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
            return false;
        }
        RealmService realmService = OrganizationManagementDataHolder.getInstance().getRealmService();
        String organizationUUID;
        try {
            organizationUUID = realmService.getTenantManager().getTenant(tenantId).getAssociatedOrganizationUUID();
        } catch (UserStoreException e) {
            throw new OrganizationManagementException("Error while retrieving the associated organization UUID for " +
                    "the tenant with id: " + tenantId, e);
        }
        if (StringUtils.isBlank(organizationUUID)) {
            return false;
        }

        OrganizationManager organizationManager = OrganizationManagementDataHolder.getInstance()
                .getOrganizationManager();
        int organizationDepth = organizationManager.getOrganizationDepthInHierarchy(organizationUUID);
        return organizationDepth >= Utils.getSubOrgStartLevel();
    }
}
