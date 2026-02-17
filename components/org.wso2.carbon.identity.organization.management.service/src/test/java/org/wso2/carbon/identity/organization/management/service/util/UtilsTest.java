/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.DefaultRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.DEFAULT_DISCOVERY_DEFAULT_PARAM;

/**
 * Unit tests for Utils class.
 */
@Test
public class UtilsTest {

    private static final String CHANGED_PRIMARY_USER_STORE_DOMAIN_NAME = "WSO2.ORG";
    private static final String SECONDARY_DOMAIN_NAME = "DEFAULT";
    private static final String ORG_HANDLE = "orgHandle";
    private static final String NEW_ORG_VERSION = "v1.0.0";

    private RealmService realmService;
    private RealmConfiguration realmConfiguration;
    private MockedStatic<OrganizationManagementConfigUtil> organizationManagementConfigUtil;

    @BeforeClass
    public void testInit() {

        realmService = mock(DefaultRealmService.class);
        realmConfiguration = mock(RealmConfiguration.class);
        when(realmService.getBootstrapRealmConfiguration()).thenReturn(realmConfiguration);
        organizationManagementConfigUtil = mockStatic(OrganizationManagementConfigUtil.class);
        OrganizationManagementDataHolder.getInstance().setRealmService(realmService);
    }

    @AfterClass
    public void testEnd() {

        organizationManagementConfigUtil.close();
    }

    @Test(description = "This test verifies whether the `resolvePrimaryUserStoreDomainName` method returns the default"
            + " domain name when the primary user store domain name is not changed")
    public void testDefaultPrimaryUserStoreDomainName() {

        when(realmConfiguration.getUserStoreProperty(
                eq(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))).thenReturn(null);
        assertEquals(Utils.resolvePrimaryUserStoreDomainName(), UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                "The `resolvePrimaryUserStoreDomainName` method does not return the default primary domain name");
    }

    @Test(description = "This test verifies whether the `resolvePrimaryUserStoreDomainName` method returns the"
            + " configured domain name when the primary user store domain name is changed")
    public void testChangedPrimaryUserStoreDomainName() {

        when(realmConfiguration.getUserStoreProperty(
                eq(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))).thenReturn(
                CHANGED_PRIMARY_USER_STORE_DOMAIN_NAME);
        assertEquals(Utils.resolvePrimaryUserStoreDomainName(), CHANGED_PRIMARY_USER_STORE_DOMAIN_NAME,
                "The `resolvePrimaryUserStoreDomainName` method does not return the changed primary domain name");
    }

    @Test(description = "This test verifies whether the `getOrganizationUserInvitationPrimaryUserDomain` method"
            + " returns the default primary domain name when the invitation domain is set to `PRIMARY`")
    public void testDefaultPrimaryUserStoreDomainNameAsInvitationDomain() {

        when(realmConfiguration.getUserStoreProperty(
                eq(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))).thenReturn(null);
        organizationManagementConfigUtil.when(() -> OrganizationManagementConfigUtil.getProperty(eq(
                        OrganizationManagementConstants.ORGANIZATION_USER_INVITATION_PRIMARY_USER_DOMAIN)))
                .thenReturn(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        assertEquals(Utils.getOrganizationUserInvitationPrimaryUserDomain(),
                UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                "The `getOrganizationUserInvitationPrimaryUserDomain` method does not return the default primary"
                        + " domain name");
    }

    @Test(description = "This test verifies whether the `getOrganizationUserInvitationPrimaryUserDomain` method"
            + " returns the changed primary domain name when the invitation domain is set to `PRIMARY`")
    public void testChangedPrimaryUserStoreDomainNameAsInvitationDomain() {

        when(realmConfiguration.getUserStoreProperty(
                eq(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))).thenReturn(
                CHANGED_PRIMARY_USER_STORE_DOMAIN_NAME);
        organizationManagementConfigUtil.when(() -> OrganizationManagementConfigUtil.getProperty(eq(
                        OrganizationManagementConstants.ORGANIZATION_USER_INVITATION_PRIMARY_USER_DOMAIN)))
                .thenReturn(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        assertEquals(Utils.getOrganizationUserInvitationPrimaryUserDomain(),
                CHANGED_PRIMARY_USER_STORE_DOMAIN_NAME,
                "The `getOrganizationUserInvitationPrimaryUserDomain` method does not return the changed primary"
                        + " domain name");
    }

    @Test(description = "This test verifies whether the `getOrganizationUserInvitationPrimaryUserDomain` method"
            + " returns the configured domain name")
    public void testConfiguredUserStoreDomainNameAsInvitationDomain() {

        when(realmConfiguration.getUserStoreProperty(
                eq(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))).thenReturn(null);
        organizationManagementConfigUtil.when(() -> OrganizationManagementConfigUtil.getProperty(eq(
                        OrganizationManagementConstants.ORGANIZATION_USER_INVITATION_PRIMARY_USER_DOMAIN)))
                .thenReturn(SECONDARY_DOMAIN_NAME);
        assertEquals(Utils.getOrganizationUserInvitationPrimaryUserDomain(),
                SECONDARY_DOMAIN_NAME,
                "The `getOrganizationUserInvitationPrimaryUserDomain` method does not return the configured"
                        + " domain name");
    }

    @DataProvider(name = "discoveryDefaultParamProvider")
    public Object[][] discoveryDefaultParamProvider() {

        return new Object[][] {
                { ORG_HANDLE, ORG_HANDLE },
                { null, DEFAULT_DISCOVERY_DEFAULT_PARAM }
        };
    }

    @Test(dataProvider = "discoveryDefaultParamProvider",
            description = "This test verifies whether the `getOrganizationDiscoveryDefaultParam` method " +
                    "returns the configured value or else the default value.")
    public void testGetOrganizationDiscoveryDefaultParam(String configuredParam, String expectedParam) {

        organizationManagementConfigUtil.when(() -> OrganizationManagementConfigUtil.getProperty(
                eq(OrganizationManagementConstants.ORGANIZATION_DISCOVERY_DEFAULT_PARAM))).thenReturn(configuredParam);

        assertEquals(Utils.getOrganizationDiscoveryDefaultParam(), expectedParam);
    }

    @Test(description = "Test getNewOrganizationVersion returns the configured organization version")
    public void testGetNewOrganizationVersion() {

        organizationManagementConfigUtil.when(() -> OrganizationManagementConfigUtil.getProperty(
                        OrganizationManagementConstants.OrganizationVersion.NEW_ORGANIZATION_VERSION_PROPERTY))
                .thenReturn(NEW_ORG_VERSION);

        String result = Utils.getNewOrganizationVersion();
        assertEquals(result, NEW_ORG_VERSION,
                "getNewOrganizationVersion should return the configured organization version");
    }
 }
