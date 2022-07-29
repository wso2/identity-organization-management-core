/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.OrganizationManagerImpl;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverService;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverServiceImpl;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi service component for organization management core bundle.
 */
@Component(name = "identity.organization.management.component",
        immediate = true)
public class OrganizationManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(OrganizationManagementServiceComponent.class);

    /**
     * Register Organization Manager service in the OSGi context.
     *
     * @param componentContext OSGi service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(OrganizationManager.class.getName(), new OrganizationManagerImpl(), null);
            bundleContext.registerService(OrganizationUserResidentResolverService.class.getName(),
                    new OrganizationUserResidentResolverServiceImpl(), null);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Organization Management component activated successfully.");
            }
        } catch (Exception e) {
            LOG.error("Error while activating Organization Management module.", e);
        }
    }

    /**
     * Set realm service implementation.
     *
     * @param realmService RealmService
     */
    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the Realm Service.");
        }
        OrganizationManagementDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unset realm service implementation.
     *
     * @param realmService RealmService
     */
    protected void unsetRealmService(RealmService realmService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the Realm Service.");
        }
        OrganizationManagementDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "org.wso2.carbon.tenant.mgt",
            service = org.wso2.carbon.tenant.mgt.services.TenantMgtService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantMgtService")
    protected void setTenantMgtService(TenantMgtService tenantMgtService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the Tenant Management Service.");
        }
        OrganizationManagementDataHolder.getInstance().setTenantMgtService(tenantMgtService);
    }

    protected void unsetTenantMgtService(TenantMgtService tenantMgtService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the Tenant Management Service.");
        }
        OrganizationManagementDataHolder.getInstance().setTenantMgtService(null);
    }
}
