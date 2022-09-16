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

package org.wso2.carbon.identity.organization.management.service.internal;

import org.wso2.carbon.identity.organization.management.service.listener.OrganizationManagerListener;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Organization management data holder.
 */
public class OrganizationManagementDataHolder {

    private static final OrganizationManagementDataHolder instance = new OrganizationManagementDataHolder();
    private RealmService realmService;
    private TenantMgtService tenantMgtService;
    private OrganizationManagerListener organizationManagerListener;

    public static OrganizationManagementDataHolder getInstance() {

        return instance;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public TenantMgtService getTenantMgtService() {

        return tenantMgtService;
    }

    public void setTenantMgtService(TenantMgtService tenantMgtService) {

        this.tenantMgtService = tenantMgtService;
    }


    /**
     * Get {@link OrganizationManagerListener}.
     *
     * @return IdentityEventService.
     */
    public OrganizationManagerListener getOrganizationManagerListener() {

        return organizationManagerListener;
    }

    /**
     * Set {@link OrganizationManagerListener}.
     *
     * @param organizationManagerListener Instance of {@link OrganizationManagerListener}.
     */
    public void setOrganizationManagerListener(OrganizationManagerListener organizationManagerListener) {

        this.organizationManagerListener = organizationManagerListener;
    }
}
