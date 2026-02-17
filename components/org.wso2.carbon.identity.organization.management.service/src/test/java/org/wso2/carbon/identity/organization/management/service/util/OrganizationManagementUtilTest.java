/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;

/**
 * Unit tests for Organization Management Util class.
 */
public class OrganizationManagementUtilTest {

    private static final String rootTenantDomain = "rootTenantDomain";
    private static final String tenantDomain = "sampleTenantDomain";
    private static final String organizationId = "sampleOrganizationId";
    private static final String rootOrganizationId = "rootOrganizationId";

    @Mock
    private OrganizationManager organizationManager;

    @BeforeMethod
    public void testInit() {

        MockitoAnnotations.openMocks(this);
        OrganizationManagementDataHolder.getInstance().setOrganizationManager(organizationManager);
    }

    @Test
    public void testGetRootOrgTenantDomainBySubOrgTenantDomain() throws OrganizationManagementException {

        when(organizationManager.resolveOrganizationId(tenantDomain)).thenReturn(organizationId);
        when(organizationManager.getPrimaryOrganizationId(organizationId)).thenReturn(rootOrganizationId);
        when(organizationManager.resolveTenantDomain(rootOrganizationId)).thenReturn(rootTenantDomain);

        assertEquals(OrganizationManagementUtil.getRootOrgTenantDomainBySubOrgTenantDomain(tenantDomain),
                rootTenantDomain);
        verify(organizationManager).resolveOrganizationId(tenantDomain);
        verify(organizationManager).getPrimaryOrganizationId(organizationId);
        verify(organizationManager).resolveTenantDomain(rootOrganizationId);
    }

    @Test
    public void testGetSuperRootOrgName() throws OrganizationManagementException {

        when(organizationManager.getOrganizationNameById(SUPER_ORG_ID)).thenReturn(SUPER);

        assertEquals(OrganizationManagementUtil.getSuperRootOrgName(), SUPER);
        verify(organizationManager).getOrganizationNameById(SUPER_ORG_ID);
    }
}
