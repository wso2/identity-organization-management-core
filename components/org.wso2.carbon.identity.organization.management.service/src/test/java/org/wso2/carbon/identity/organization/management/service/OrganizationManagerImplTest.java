/*
 * Copyright (c) 2022-2025, WSO2 LLC. (https://www.wso2.com).
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.listener.OrganizationManagerListener;
import org.wso2.carbon.identity.organization.management.service.model.AncestorOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.service.model.TenantTypeOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.identity.organization.management.util.TestUtils;
import org.wso2.carbon.stratos.common.exception.TenantManagementClientException;
import org.wso2.carbon.stratos.common.exception.TenantMgtException;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_EXISTING_ORGANIZATION_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.STRUCTURAL;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_ADD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REMOVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REPLACE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_VERSION;
import static org.wso2.carbon.stratos.common.constants.TenantConstants.ErrorMessage.ERROR_CODE_EXISTING_DOMAIN;

public class OrganizationManagerImplTest {

    private static final String SUPER = "Super";
    private static final String ORG1_NAME = "ABC Builders";
    private static final String ORG2_NAME = "XYZ Builders";
    private static final String ORG3_NAME = "Greater";
    private static final String ORG4_NAME = "ABC Inc";
    private static final String ORG1_HANDLE = "abcbuilders";
    private static final String ORG2_HANDLE = "xyzbuilders";
    private static final String ORG3_HANDLE = "greater";
    private static final String ORG4_HANDLE = "abc.com";
    private static final String NON_EXISTING_ORG_NAME = "Dummy Builders";
    private static final String ORG_NAME_WITH_HTML_CONTENT = "<a href=\"evil.com\">Click me</a>";
    private static final String NEW_ORG1_NAME = "ABC Builders New";
    private static final String ORG_DESCRIPTION = "This is a construction company.";
    private static final String NEW_ORG_NAME = "New Org";
    private static final String NEW_ORG_DESCRIPTION = "new sample description.";
    private static final String ORG_ATTRIBUTE_KEY_COUNTRY = "country";
    private static final String ORG_ATTRIBUTE_VALUE_COUNTRY = "Sri Lanka";
    private static final String ORG_ATTRIBUTE_KEY_CITY = "city";
    private static final String ORG_ATTRIBUTE_VALUE_CITY = "Colombo";
    private static final String ORG_ATTRIBUTE_KEY_CAPITAL = "capital";
    private static final String SUPER_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String ORG_ID = "9f1c2e3a-45b6-4f21-9d3b-7a6f1e8a9c12";
    private static final String ROOT_ORG = "custom-root-org";
    private static final String ORG1_ID = "org_id_1";
    private static final String ORG2_ID = "org_id_2";
    private static final String ORG2_1 = "org_2_1";
    private static final String ORG3_1 = "org_3_1";
    private static final String ORG3_ID = "org_id_3";
    private static final String ORG4_ID = "org_id_4";
    private static final String INVALID_PARENT_ID = "invalid_parent_id";
    private static final String INVALID_ORG_ID = "invalid_org_id";
    private static final String ORG_CREATED = "createdTime";
    private static final String ORG_STATUS = "ACTIVE";
    private static final String NEW_SUPER_ORG_NAME = "New Super Org";
    private static final String V0 = "v0.0.0";
    private static final String V1 = "v1.0.0";

    private OrganizationManagerImpl organizationManager;

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    private RealmService realmService;

    private TenantManager tenantManager;

    private Tenant tenant;

    private TenantMgtService tenantMgtService;

    private MockedStatic<Utils> mockedUtilities;

    @Captor
    private ArgumentCaptor<org.wso2.carbon.user.core.tenant.Tenant> tenantArgumentCaptor;

    @BeforeClass
    public void init() {

        MockitoAnnotations.openMocks(this);

        realmService = mock(RealmService.class);
        tenantManager = mock(TenantManager.class);
        tenantMgtService = mock(TenantMgtService.class);
        tenant = mock(Tenant.class);
        mockUtils();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        organizationManager = new OrganizationManagerImpl();
        OrganizationManagementDataHolder.getInstance().setOrganizationManagerListener(mock(
                OrganizationManagerListener.class));
        OrganizationManagementDataHolder.getInstance().setRealmService(realmService);
        OrganizationManagementDataHolder.getInstance().setTenantMgtService(tenantMgtService);

        TestUtils.initiateH2Base();
        TestUtils.mockDataSource();
        Mockito.reset(tenantMgtService);

        // Super -> org1 -> org2
        //       -> org3
        Organization organization1 = getOrganization(ORG1_ID, ORG1_NAME, ORG_DESCRIPTION, SUPER_ORG_ID,
                STRUCTURAL.toString(), V0);
        Organization organization2 = getOrganization(ORG2_ID, ORG2_NAME, ORG_DESCRIPTION, ORG1_ID,
                STRUCTURAL.toString(), V0);
        Organization organization3 = getOrganization(ORG3_ID, ORG3_NAME, ORG_DESCRIPTION, SUPER_ORG_ID,
                TENANT.toString(), V0);

        setOrganizationAttributes(organization3, ORG_ATTRIBUTE_KEY_COUNTRY, ORG_ATTRIBUTE_VALUE_COUNTRY);
        setOrganizationAttributes(organization3, ORG_ATTRIBUTE_KEY_CITY, ORG_ATTRIBUTE_VALUE_CITY);
        setOrganizationAttributes(organization2, ORG_ATTRIBUTE_KEY_CAPITAL, ORG_ATTRIBUTE_VALUE_CITY);

        addOrganization(organization1, ORG1_HANDLE);
        addOrganization(organization2, ORG2_HANDLE);
        addOrganization(organization3, ORG3_HANDLE);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @AfterClass
    public void close() {

        mockedUtilities.close();
    }

    @Test
    public void testAddOrganizationUnderSuperTenant() throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, STRUCTURAL.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        Organization addedOrganization = organizationManager.addOrganization(sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
        assertFalse(addedOrganization.hasChildren());
    }

    @Test
    public void testAddRootOrganization() throws Exception {

        // Root_1
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ROOT_ORG, ORG_DESCRIPTION,
                null, TENANT.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenant(anyInt())).thenReturn(tenant);
        Organization addedOrganization = organizationManager.addRootOrganization(1, sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created root organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
        assertFalse(addedOrganization.hasChildren());
    }

    @Test
    public void testAddOrganizationFromImmediateParent() throws Exception {

        // Super -> org1 -> org2_1
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG2_1,
                ORG_DESCRIPTION, ORG1_ID, TENANT.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(anyString())).thenReturn(1);
        Organization addedOrganization = organizationManager.addOrganization(sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
        assertFalse(addedOrganization.hasChildren());
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationFromAncestorOrg() throws Exception {

        // Super -> org1 -> org2 -> org3_1
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG3_1,
                ORG_DESCRIPTION, ORG2_ID, TENANT.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddExistingSubOrganization() throws Exception {

        /*
            sub org level = 1
            Existing org hierarchy =  Super -> ORG1 -> ORG2
            Operation = Super -> ORG2
         */
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG2_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, TENANT.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test
    public void testAddPrimaryOrganizationWithSameNameOfSubOrg() throws Exception {

        /*
            sub org level = 2
            Existing org hierarchy =  Super -> ORG1 -> ORG2
            Root organizations before test = ORG1

            Operation = Super -> ORG2
            Root organizations after test = ORG1, ORG2
         */
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG2_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, TENANT.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        mockedUtilities.when(Utils::getSubOrgStartLevel).thenReturn(2);
        mockedUtilities.when(Utils::getNewOrganizationVersion).thenReturn(V0);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithExistingAncestorOrganizationName() throws Exception {

        /*
            sub org level = 1
            Existing org hierarchy =  Super -> ORG1 -> ORG2
            Operation = ORG2 -> ORG1
         */
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG1_NAME, ORG_DESCRIPTION,
                ORG2_ID, TENANT.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG2_ID);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithInvalidParentId() throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(),
                NEW_ORG_NAME, ORG_DESCRIPTION, INVALID_PARENT_ID, STRUCTURAL.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithReservedName() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), SUPER, ORG_DESCRIPTION, ORG1_NAME,
                TENANT.toString(), V0);
        organizationManager.addOrganization(organization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithNameIncludeHTMLContent() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), ORG_NAME_WITH_HTML_CONTENT,
                ORG_DESCRIPTION, ORG1_NAME, TENANT.toString(), V0);
        organizationManager.addOrganization(organization);
    }

    @DataProvider(name = "dataForAddOrganizationRequiredFieldsMissing")
    public Object[][] dataForAddOrganizationRequiredFieldsMissing() {

        return new Object[][]{

                {null, null},
                {StringUtils.EMPTY, StringUtils.EMPTY},
                {null, ORG1_ID},
                {StringUtils.EMPTY, ORG1_ID},
                {ORG1_NAME, null},
                {ORG1_NAME, StringUtils.EMPTY}
        };
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class,
            dataProvider = "dataForAddOrganizationRequiredFieldsMissing")
    public void testAddOrganizationRequiredFieldsMissing(String orgName, String parentId) throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(),
                orgName, ORG_DESCRIPTION, parentId, TENANT.toString(), V0);
        organizationManager.addOrganization(sampleOrganization);
    }

    @DataProvider(name = "dataForAddOrganizationInvalidOrganizationAttributes")
    public Object[][] dataForAddOrganizationInvalidOrganizationAttributes() {

        return new Object[][]{

                {null, null},
                {StringUtils.EMPTY, StringUtils.EMPTY},
                {null, StringUtils.EMPTY},
                {StringUtils.EMPTY, null},
                {ORG_ATTRIBUTE_KEY_COUNTRY, null},
                {null, ORG_ATTRIBUTE_VALUE_COUNTRY},
                {StringUtils.EMPTY, ORG_ATTRIBUTE_VALUE_COUNTRY}
        };
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class,
            dataProvider = "dataForAddOrganizationInvalidOrganizationAttributes")
    public void testAddOrganizationInvalidAttributes(String attributeKey, String attributeValue) throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        Organization organization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, STRUCTURAL.toString(), V0);
        List<OrganizationAttribute> organizationAttributeList = new ArrayList<>();
        OrganizationAttribute organizationAttribute = new OrganizationAttribute(attributeKey, attributeValue);
        organizationAttributeList.add(organizationAttribute);
        organization.setAttributes(organizationAttributeList);
        organizationManager.addOrganization(organization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationDuplicateAttributeKeys() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, STRUCTURAL.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        List<OrganizationAttribute> organizationAttributeList = new ArrayList<>();
        OrganizationAttribute organizationAttribute1 = new OrganizationAttribute(ORG_ATTRIBUTE_KEY_COUNTRY,
                ORG_ATTRIBUTE_VALUE_COUNTRY);
        OrganizationAttribute organizationAttribute2 = new OrganizationAttribute(ORG_ATTRIBUTE_KEY_COUNTRY,
                ORG_ATTRIBUTE_VALUE_COUNTRY);
        organizationAttributeList.add(organizationAttribute1);
        organizationAttributeList.add(organizationAttribute2);
        organization.setAttributes(organizationAttributeList);
        organizationManager.addOrganization(organization);
    }

    @Test
    public void testGetOrganization() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
        Organization organization = organizationManager.getOrganization(ORG1_ID, false, false);
        assertEquals(organization.getName(), ORG1_NAME);
        assertEquals(organization.getParent().getId(), SUPER_ORG_ID);
        assertTrue(organization.hasChildren());
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(null);
    }

    @Test
    public void testGetSelfOrganization() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
        Organization organization = organizationManager.getSelfOrganization();
        assertEquals(organization.getName(), SUPER);
        assertEquals(organization.getId(), SUPER_ORG_ID);
        assertTrue(organization.hasChildren());
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(null);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetParentOrganizationFromChildOrganization() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG2_ID);
        organizationManager.getOrganization(ORG1_ID, false, false);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationWithEmptyOrganizationId() throws Exception {

        organizationManager.getOrganization(StringUtils.EMPTY, false, false);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationNotExisting() throws Exception {

        organizationManager.getOrganization(INVALID_ORG_ID, false, false);
    }

    @Test
    public void testGetOrganizationWithChildren() throws Exception {

        Organization organization = organizationManager.getOrganization(ORG1_ID, true, false);
        assertEquals(organization.getName(), ORG1_NAME);
        assertEquals(organization.getParent().getId(), SUPER_ORG_ID);
        assertEquals(organization.getChildOrganizations().size(), 1);
        assertTrue(organization.hasChildren());
    }

    @DataProvider(name = "dataForFilterOrganizationsByMetaAttributes")
    public Object[][] dataForFilterOrganizationsByMetaAttributes() {

        return new Object[][]{
                {"attributes.country co S", false},
                {"attributes.country co S and name eq Greater", false},
                {"attributes.country sw S and attributes.city ew o", false},
                {"attributes.country co Z", true},
                {"attributes.invalid co S", true}
        };
    }

    @Test(dataProvider = "dataForFilterOrganizationsByMetaAttributes")
    public void testFilterOrganizationsByMetaAttributes(String filter, boolean isEmptyList) throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        List<Organization> organizations = organizationManager.getOrganizationsList(10, null, null,
                "ASC", filter, false);
        if (isEmptyList) {
            assertTrue(organizations.isEmpty());
        } else {
            assertEquals(organizations.size(), 1);
            assertEquals(organizations.get(0).getName(), ORG3_NAME);
            assertFalse(organizations.get(0).hasChildren());
            assertEquals(organizations.get(0).getAttributes().get(0).getKey(), ORG_ATTRIBUTE_KEY_COUNTRY);
            assertEquals(organizations.get(0).getAttributes().get(0).getValue(), ORG_ATTRIBUTE_VALUE_COUNTRY);
            assertEquals(organizations.get(0).getAttributes().get(1).getKey(), ORG_ATTRIBUTE_KEY_CITY);
            assertEquals(organizations.get(0).getAttributes().get(1).getValue(), ORG_ATTRIBUTE_VALUE_CITY);
        }
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationsWithUnsupportedFilterAttribute() throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.getOrganizationsList(10, null, null, "ASC",
                "invalid_attribute co xyz", false);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationsWithUnsupportedComplexQueryInFilter() throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.getOrganizationsList(10, null, null, "ASC",
                "name co xyz or name co abc", false);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationsWithInvalidPaginationAttribute() throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.getOrganizationsList(10, "MjAyNjkzMjg=", null, "ASC",
                "name co xyz", false);
    }

    @DataProvider(name = "dataForGetOrganizationsMetaAttributes")
    public Object[][] dataForGetOrganizationsMetaAttributes() {

        return new Object[][]{
                {"attributes eq country", false},
                {"attributes sw c and attributes ew try", false},
                {"attributes co cap", true},
        };
    }

    @Test(dataProvider = "dataForGetOrganizationsMetaAttributes")
    public void testGetOrganizationsMetaAttributes(String filter, boolean isRecursive) throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        List<String> metaAttributes = organizationManager.getOrganizationsMetaAttributes(10, null,
                null, "ASC", filter, isRecursive);
        assertEquals(metaAttributes.size(), 1);
        if (isRecursive) {
            assertEquals(metaAttributes.get(0), ORG_ATTRIBUTE_KEY_CAPITAL);
        } else {
            assertEquals(metaAttributes.get(0), ORG_ATTRIBUTE_KEY_COUNTRY);
        }
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testDeleteOrganizationWithEmptyOrganizationId() throws Exception {

        organizationManager.deleteOrganization(StringUtils.EMPTY);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testDeleteOrganizationWithChildOrganizations() throws Exception {

        organizationManager.deleteOrganization(ORG1_ID);
    }

    @Test
    public void testPatchOrganization() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_ADD, PATCH_PATH_ORG_DESCRIPTION,
                NEW_ORG_DESCRIPTION);
        patchOperations.add(patchOperation);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
        Organization patchedOrganization = organizationManager.patchOrganization(ORG1_ID, patchOperations);
        assertNotNull(patchedOrganization);
        assertEquals(patchedOrganization.getDescription(), NEW_ORG_DESCRIPTION);
        assertEquals(patchedOrganization.getName(), ORG1_NAME);
        assertTrue(patchedOrganization.hasChildren());
    }

    @Test
    public void testSelfPatchOrganization() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_NAME, NEW_SUPER_ORG_NAME);
        patchOperations.add(patchOperation);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
        Organization patchedOrganization = organizationManager.patchSelfOrganization(patchOperations);
        assertNotNull(patchedOrganization);
        assertEquals(patchedOrganization.getName(), NEW_SUPER_ORG_NAME);
        assertTrue(patchedOrganization.hasChildren());
    }

    @Test
    public void testPatchRootOrgVersion() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(false);
            List<PatchOperation> patchOperations = new ArrayList<>();
            PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_VERSION,
                    V1);
            patchOperations.add(patchOperation);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
            Organization patchedOrganization = organizationManager.patchOrganization(SUPER_ORG_ID, patchOperations);
            assertNotNull(patchedOrganization);
            assertEquals(patchedOrganization.getVersion(), V1);
        }
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchSubOrgVersion() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(true);
            List<PatchOperation> patchOperations = new ArrayList<>();
            PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_VERSION,
                    V1);
            patchOperations.add(patchOperation);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
            organizationManager.patchOrganization(ORG1_ID, patchOperations);
        }

    }

    @Test
    public void testGetSubOrgUnderV1Root() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(false);
            List<PatchOperation> patchOperations = new ArrayList<>();
            PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_VERSION,
                    V1);
            patchOperations.add(patchOperation);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
            organizationManager.patchOrganization(SUPER_ORG_ID, patchOperations);

            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(true);
            mockedUtilities.when(Utils::getSubOrgStartLevel).thenReturn(1);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
            Organization organization = organizationManager.getOrganization(ORG1_ID, false, false);
            assertEquals(organization.getName(), ORG1_NAME);
            assertEquals(organization.getVersion(), V1);
        }
    }

    @Test
    public void testGetSelfOrganizationVersionUnderV1Root() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(false);
            List<PatchOperation> patchOperations = new ArrayList<>();
            PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_VERSION,
                    V1);
            patchOperations.add(patchOperation);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
            organizationManager.patchOrganization(SUPER_ORG_ID, patchOperations);

            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(true);
            mockedUtilities.when(Utils::getSubOrgStartLevel).thenReturn(1);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
            Organization organization = organizationManager.getSelfOrganization();
            assertEquals(organization.getName(), ORG1_NAME);
            assertEquals(organization.getVersion(), V1);
        }
    }

    @Test
    public void testSelfPatchRootOrganizationVersion() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_VERSION, V1);
        patchOperations.add(patchOperation);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
        Organization patchedOrganization = organizationManager.patchSelfOrganization(patchOperations);
        assertNotNull(patchedOrganization);
        assertEquals(patchedOrganization.getVersion(), V1);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testSelfPatchSubOrganizationVersion() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(true);
            List<PatchOperation> patchOperations = new ArrayList<>();
            PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_VERSION, V1);
            patchOperations.add(patchOperation);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
            organizationManager.patchSelfOrganization(patchOperations);
        }
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationWithEmptyOrganizationId() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_ADD, PATCH_PATH_ORG_DESCRIPTION,
                NEW_ORG_DESCRIPTION);
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(StringUtils.EMPTY, patchOperations);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationWithInvalidOrganizationId() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_ADD, PATCH_PATH_ORG_DESCRIPTION,
                NEW_ORG_DESCRIPTION);
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(INVALID_ORG_ID, patchOperations);
    }

    @DataProvider(name = "invalidDataSet1ForPatchOrganization")
    public Object[][] invalidData1ForPatchOrganization() {

        return new Object[][]{

                {"invalid patch operation", PATCH_PATH_ORG_DESCRIPTION, "new value"},
                {StringUtils.EMPTY, PATCH_PATH_ORG_DESCRIPTION, "new value"},
                {null, PATCH_PATH_ORG_DESCRIPTION, "new value"},
                {null, null, null},
                {StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY},
                {PATCH_OP_ADD, "invalid patch path", "new value"},
        };
    }

    @Test(dataProvider = "invalidDataSet1ForPatchOrganization",
            expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationWithInvalidPatchRequest1(String op, String path, String value) throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(op, path, value);
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(ORG1_ID, patchOperations);
    }

    @DataProvider(name = "invalidDataSet2ForPatchOrganization")
    public Object[][] invalidData2ForPatchOrganization() {

        return new Object[][]{

                {PATCH_OP_ADD, StringUtils.EMPTY, "new value"},
                {PATCH_OP_ADD, null, "new value"},
                {PATCH_OP_ADD, PATCH_PATH_ORG_DESCRIPTION, null},
                {PATCH_OP_ADD, PATCH_PATH_ORG_ATTRIBUTES, "new value"}
        };
    }

    @Test(dataProvider = "invalidDataSet2ForPatchOrganization",
            expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationWithInvalidPatchRequest2(String op, String path, String value) throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(op, path, value);
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(ORG1_ID, patchOperations);
    }

    @DataProvider(name = "invalidDataSet3ForPatchOrganization")
    public Object[][] invalidData3ForPatchOrganization() {

        return new Object[][]{

                {PATCH_OP_REMOVE, PATCH_PATH_ORG_NAME, null},
        };
    }

    @Test(dataProvider = "invalidDataSet3ForPatchOrganization",
            expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationWithInvalidPatchRequest3(String op, String path, String value) throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(op, path, value);
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(ORG1_ID, patchOperations);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationRemoveNonExistingAttribute() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_REMOVE, PATCH_PATH_ORG_ATTRIBUTES + "country",
                null);
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(ORG1_ID, patchOperations);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testPatchOrganizationReplaceNonExistingAttribute() throws Exception {

        List<PatchOperation> patchOperations = new ArrayList<>();
        PatchOperation patchOperation = new PatchOperation(PATCH_OP_REPLACE, PATCH_PATH_ORG_ATTRIBUTES + "country",
                "India");
        patchOperations.add(patchOperation);
        organizationManager.patchOrganization(ORG1_ID, patchOperations);
    }

    @Test
    public void testUpdateOrganization() throws Exception {

        Organization sampleOrganization = getOrganization(ORG1_ID, NEW_ORG1_NAME, NEW_ORG_DESCRIPTION, SUPER_ORG_ID,
                STRUCTURAL.toString(), V0);
        Organization updatedOrganization = organizationManager.updateOrganization(ORG1_ID, ORG1_NAME,
                sampleOrganization);
        assertEquals(NEW_ORG_DESCRIPTION, updatedOrganization.getDescription());
        assertEquals(SUPER_ORG_ID, updatedOrganization.getParent().getId());

    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testUpdateOrganizationWithEmptyOrganizationId() throws Exception {

        organizationManager.updateOrganization(StringUtils.EMPTY, ORG1_NAME,
                getOrganization(ORG1_ID, ORG1_NAME, NEW_ORG_DESCRIPTION, SUPER_ORG_ID, STRUCTURAL.toString(), V0));
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testUpdateOrganizationWithInvalidOrganizationId() throws Exception {

        organizationManager.updateOrganization(INVALID_ORG_ID, ORG1_NAME, getOrganization(INVALID_ORG_ID, ORG1_NAME,
                NEW_ORG_DESCRIPTION, SUPER_ORG_ID, STRUCTURAL.toString(), V0));
    }

    @DataProvider(name = "dataForGetOrganizationDepth")
    public Object[][] dataForGetOrganizationDepth() {

        return new Object[][]{
                {ORG1_ID, 1},
                {INVALID_ORG_ID, -1}
        };
    }

    @Test(dataProvider = "dataForGetOrganizationDepth")
    public void testGetOrganizationDepthInHierarchy(String organizationId, int depth) throws Exception {

        int organizationDepthInHierarchy = organizationManager.getOrganizationDepthInHierarchy(organizationId);
        assertEquals(depth, organizationDepthInHierarchy);
    }

    @DataProvider(name = "dataForOrgNameUniquenessTest")
    public Object[][] dataForOrgNameUniquenessTest() {

        return new Object[][]{
                {ORG1_NAME, true},
                {NON_EXISTING_ORG_NAME, false}
        };
    }

    @Test(dataProvider = "dataForOrgNameUniquenessTest")
    public void testIsOrganizationExistByNameInGivenHierarchy(String organizationName, boolean expectedResult) {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        Assert.assertEquals(organizationManager.isOrganizationExistByNameInGivenHierarchy(organizationName),
                expectedResult);
    }

    @Test
    public void testGetRelativeDepthBetweenOrganizationsInSameBranch() throws OrganizationManagementException {

        Assert.assertEquals(organizationManager.getRelativeDepthBetweenOrganizationsInSameBranch(ORG1_ID, ORG2_ID), 1);
        Assert.assertEquals(organizationManager.getRelativeDepthBetweenOrganizationsInSameBranch(ORG1_ID, ORG3_ID), -1);
    }

    @Test
    public void testGetParentOrganizationId() throws OrganizationManagementException {

        Assert.assertEquals(organizationManager.getParentOrganizationId(ORG2_ID), ORG1_ID);
    }

    @Test(expectedExceptions = OrganizationManagementException.class,
            expectedExceptionsMessageRegExp = ".*Server encountered error while executing post listeners.*")
    public void testAddOrganizationFailWhileRoleBack() throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, STRUCTURAL.toString(), V0);
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        OrganizationManagerListener mockOrgMgtListener = OrganizationManagementDataHolder.getInstance()
                .getOrganizationManagerListener();
        Mockito.doThrow(new OrganizationManagementException("Server encountered error while executing post listeners."))
                .when(mockOrgMgtListener).postAddOrganization(sampleOrganization);
        Mockito.doThrow(new OrganizationManagementException("Server encountered error while deleting organization."))
                .when(mockOrgMgtListener).preDeleteOrganization(sampleOrganization.getId());
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testResolveTenantDomainWithEmptyOrganizationId() throws OrganizationManagementException {

        organizationManager.resolveTenantDomain("");
    }

    @Test
    public void testGetChildOrganizationIds() throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);

        // Non-recursive test case (only direct children)
        List<String> directChildIds = organizationManager.getChildOrganizationsIds(SUPER_ORG_ID, false);
        Assert.assertNotNull(directChildIds);
        Assert.assertEquals(directChildIds.size(), 2);
        Assert.assertTrue(directChildIds.contains(ORG1_ID));
        Assert.assertTrue(directChildIds.contains(ORG3_ID));
        Assert.assertFalse(directChildIds.contains(ORG2_ID));

        // Recursive test case (all levels of children)
        List<String> recursiveChildIds = organizationManager.getChildOrganizationsIds(SUPER_ORG_ID, true);
        Assert.assertNotNull(recursiveChildIds);
        Assert.assertEquals(recursiveChildIds.size(), 3);
        Assert.assertTrue(recursiveChildIds.contains(ORG1_ID));
        Assert.assertTrue(recursiveChildIds.contains(ORG2_ID));
        Assert.assertTrue(recursiveChildIds.contains(ORG3_ID));
    }

    @Test()
    public void testAddOrganizationWithExistingOrganizationHandle() throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);

        Organization sampleOrganization = getOrganization(
                ORG4_ID, ORG4_NAME, ORG_DESCRIPTION, SUPER_ORG_ID, TENANT.toString(), V0);
        sampleOrganization.setOrganizationHandle(ORG4_HANDLE);

        TenantManagementClientException tenantManagementClientException = new TenantManagementClientException(
                ERROR_CODE_EXISTING_DOMAIN.getCode(), ERROR_CODE_EXISTING_DOMAIN.getMessage());

        when(tenantMgtService.addTenant(any(org.wso2.carbon.user.core.tenant.Tenant.class)))
                .thenThrow(tenantManagementClientException);

        try {
            organizationManager.addOrganization(sampleOrganization);
        } catch (OrganizationManagementClientException e) {
            assertEquals(e.getDescription(),
                    String.format(ERROR_CODE_EXISTING_ORGANIZATION_HANDLE.getDescription(), ORG4_HANDLE));
        }
    }

    @DataProvider(name = "organizationHandleDataProvider")
    public Object[][] organizationHandleDataProvider() {

        return new Object[][]{
                {true}, {false}
        };
    }

    @Test(dataProvider = "organizationHandleDataProvider")
    public void testAddOrganizationWithOrganizationHandle(boolean isHandleProvided) throws Exception {

        TenantTypeOrganization tenantTypeOrganization = new TenantTypeOrganization(ORG_ID);
        tenantTypeOrganization.setId(ORG_ID);
        tenantTypeOrganization.setName(NEW_ORG_NAME);
        if (isHandleProvided) {
            tenantTypeOrganization.setOrganizationHandle(ORG2_HANDLE);
        }
        tenantTypeOrganization.setStatus(OrganizationStatus.ACTIVE.toString());
        tenantTypeOrganization.getParent().setId(SUPER_ORG_ID);
        tenantTypeOrganization.setType(TENANT.toString());
        tenantTypeOrganization.setCreated(Instant.now());
        tenantTypeOrganization.setLastModified(Instant.now());

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.addOrganization(tenantTypeOrganization);

        verify(tenantMgtService).addTenant(any(org.wso2.carbon.user.core.tenant.Tenant.class));
        verify(tenantMgtService).addTenant(tenantArgumentCaptor.capture());
        Tenant bean = tenantArgumentCaptor.getValue();
        if (isHandleProvided) {
            assertEquals(bean.getDomain(), ORG2_HANDLE);
        } else {
            assertEquals(bean.getDomain(), ORG_ID);
        }
    }

    @DataProvider(name = "dataForGetBasicOrganizationDetailsByOrgIDs")
    public Object[][] dataForGetBasicOrganizationDetailsByOrgIDs() {

        List<String> orgIds = Arrays.asList(ORG1_ID, ORG2_ID, ORG3_ID);

        List<String> expectedIds = Arrays.asList(ORG1_ID, ORG2_ID, ORG3_ID);
        List<String> expectedNames = Arrays.asList(ORG1_NAME, ORG2_NAME, ORG3_NAME);
        List<String> expectedOrganizationHandles = Arrays.asList(ORG1_HANDLE, ORG2_HANDLE, ORG3_HANDLE);
        List<Boolean> expectedHasChildren = Arrays.asList(true, false, false);

        return new Object[][] {
                { orgIds, expectedIds, expectedNames, expectedOrganizationHandles, expectedHasChildren }
        };
    }

    @Test(dataProvider = "dataForGetBasicOrganizationDetailsByOrgIDs")
    public void testGetBasicOrganizationDetailsByOrgIDs(List<String> orgIds, List<String> expectedIds,
                                                        List<String> expectedNames,
                                                        List<String> expectedOrganizationHandles,
                                                        List<Boolean> expectedHasChildren)
            throws OrganizationManagementException {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);

        Map<String, BasicOrganization> actualMap = organizationManager.getBasicOrganizationDetailsByOrgIDs(orgIds);
        Assert.assertNotNull(actualMap);

        for (String orgId : orgIds) {
            BasicOrganization org = actualMap.get(orgId);
            int index = orgIds.indexOf(orgId);

            Assert.assertEquals(org.getId(), expectedIds.get(index));
            Assert.assertEquals(org.getName(), expectedNames.get(index));
            Assert.assertEquals(org.getOrganizationHandle(), expectedOrganizationHandles.get(index));
            Assert.assertEquals(org.hasChildren(), expectedHasChildren.get(index).booleanValue());
        }
    }

    @DataProvider(name = "dataForGetBasicOrganizationDetailsByOrgIDsWithInvalidInput")
    public Object[][] dataForGetBasicOrganizationDetailsByOrgIDsWithInvalidInput() {

        List<String> orgIdList1 = Collections.emptyList();
        List<String> orgIdList2 = Collections.singletonList("Invalid_org_id");
        List<String> orgIdList3 = Arrays.asList("Invalid_org_id_1", ORG1_ID);
        List<String> orgIdList4 = Arrays.asList(ORG1_ID, "Invalid_org_id_1");

        return new Object[][] {
                { orgIdList1, 0, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY},
                { orgIdList2, 0, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY},
                { orgIdList3, 1, ORG1_ID, ORG1_NAME, ORG1_HANDLE},
                { orgIdList4, 1, ORG1_ID, ORG1_NAME, ORG1_HANDLE}
        };
    }

    @Test(dataProvider = "dataForGetBasicOrganizationDetailsByOrgIDsWithInvalidInput")
    public void testGetBasicOrganizationDetailsByOrgIDsWithInvalidInput(List<String> orgIds, int expectedMapSize,
                                                               String expectedId, String expectedName,
                                                               String expectedOrgHandle)
            throws OrganizationManagementException {

        Map<String, BasicOrganization> actualMap = organizationManager.getBasicOrganizationDetailsByOrgIDs(orgIds);
        Assert.assertEquals(actualMap.size(), expectedMapSize);
        if (!actualMap.isEmpty()) {
            BasicOrganization org = actualMap.get(ORG1_ID);
            Assert.assertEquals(org.getId(), expectedId);
            Assert.assertEquals(org.getName(), expectedName);
            Assert.assertEquals(org.getOrganizationHandle(), expectedOrgHandle);
        }
    }

    @Test()
    public void testCheckOrganizationExistByHandle() throws Exception {

        when(tenantMgtService.isDomainAvailable(ORG1_HANDLE)).thenReturn(false);
        assertTrue(organizationManager.isOrganizationExistByHandle(ORG1_HANDLE));
    }

    @Test(expectedExceptions = OrganizationManagementServerException.class)
    public void testCheckOrganizationExistByHandleException() throws Exception {

        when(tenantMgtService.isDomainAvailable(ORG1_HANDLE)).thenThrow(
                new TenantMgtException("Error checking domain availability."));
        organizationManager.isOrganizationExistByHandle(ORG1_HANDLE);
    }

    @Test
    public void testAncestorsNotIncludedWhenNotRequested() throws OrganizationManagementException {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(false);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(SUPER_ORG_ID);
            Organization organization = organizationManager.getOrganization(ORG2_ID, false, false, false);
            List<AncestorOrganizationDO> ancestors = organization.getAncestors();
            Assert.assertEquals(ancestors.size(), 0,
                    "The number of ancestors returned for  organization should be zero.");
        }
    }

    @DataProvider(name = "dataForTestGetAncestorsOfOrganization")
    public Object[][] dataForTestGetAncestorsOfOrganization() {

        List<AncestorOrganizationDO> ancestors = Arrays.asList(
                new AncestorOrganizationDO(SUPER_ORG_ID, SUPER, 0),
                new AncestorOrganizationDO(ORG1_ID, ORG1_NAME, 1)
        );

        return new Object[][]{
                {ORG1_ID, SUPER_ORG_ID, ancestors.subList(0, 1)},
                {ORG2_ID, SUPER_ORG_ID, ancestors},
                {ORG2_ID, ORG1_ID, ancestors.subList(1, 2)},
                {ORG2_ID, ORG2_ID, Collections.emptyList()},
        };
    }

    @Test(dataProvider = "dataForTestGetAncestorsOfOrganization")
    public void testGetAncestorsOfOrganization(String organizationId, String requestInitiatedOrgId,
                                           List<AncestorOrganizationDO> expectedAncestors)
            throws OrganizationManagementException {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(false);
           mockedUtilities.when(Utils::getSubOrgStartLevel).thenReturn(1);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(requestInitiatedOrgId);
            Organization organization = organizationManager.getOrganization(organizationId, false, false, true);
            List<AncestorOrganizationDO> ancestors = organization.getAncestors();
            Assert.assertEquals(ancestors.size(), expectedAncestors.size(),
                    "The number of ancestors returned does not match the expected count.");
            for (int i = 0; i < ancestors.size(); i++) {
                AncestorOrganizationDO expectedAncestor = expectedAncestors.get(i);
                AncestorOrganizationDO actualAncestor = ancestors.get(i);
                Assert.assertEquals(actualAncestor.getId(), expectedAncestor.getId(),
                        "The ancestor ID does not match the expected value.");
                Assert.assertEquals(actualAncestor.getName(), expectedAncestor.getName(),
                        "The ancestor name does not match the expected value.");
                Assert.assertEquals(actualAncestor.getDepth(), expectedAncestor.getDepth(),
                        "The ancestor depth does not match the expected value.");
            }
        }
    }

    @Test
    public void testAncestorRetrievalWithCustomSubOrgStartLevel() throws OrganizationManagementException {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class)) {
            organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                    .thenReturn(false);
            mockedUtilities.when(Utils::getSubOrgStartLevel).thenReturn(2);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
            Organization organization = organizationManager.getOrganization(ORG2_ID, false, false, true);
            List<AncestorOrganizationDO> ancestors = organization.getAncestors();
            Assert.assertEquals(ancestors.size(), 1,
                    "The number of ancestors returned should be one when sub-org start level is set to 2.");
            AncestorOrganizationDO ancestor = ancestors.get(0);
            Assert.assertEquals(ancestor.getId(), ORG1_ID,
                    "The ancestor ID should match the parent organization ID.");
            Assert.assertEquals(ancestor.getName(), ORG1_NAME,
                    "The ancestor name should match the parent organization name.");
            Assert.assertEquals(ancestor.getDepth(), 0, "The ancestor depth should be 0.");
        }
    }

    @Test
    public void testChildrenStatusOfOrganizationOnChildDeletion() throws OrganizationManagementException {

        // Assert ORG_1 has children.
        Organization organization1 = organizationManager.getOrganization(ORG1_ID, false, false, false);
        assertTrue(organization1.hasChildren());
        BasicOrganization basicOrganization = organizationManager.getBasicOrganizationDetailsByOrgIDs(
                Collections.singletonList(ORG1_ID)).get(ORG1_ID);
        assertTrue(basicOrganization.hasChildren());
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
        // Delete ORG_2.
        organizationManager.deleteOrganization(ORG2_ID);
        // Assert ORG_1 don't have children.
        organization1 = organizationManager.getOrganization(ORG1_ID, false, false, false);
        assertFalse(organization1.hasChildren());
        basicOrganization = organizationManager.getBasicOrganizationDetailsByOrgIDs(
                Collections.singletonList(ORG1_ID)).get(ORG1_ID);
        assertFalse(basicOrganization.hasChildren());
    }

    private void setOrganizationAttributes(Organization organization, String key, String value) {

        OrganizationAttribute organizationAttribute = new OrganizationAttribute(key, value);
        organization.setAttribute(organizationAttribute);
    }

    private void mockUtils() {

        mockedUtilities = Mockito.mockStatic(Utils.class, Mockito.withSettings()
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        mockedUtilities.when(() -> Utils.getTenantId("carbon.super")).thenReturn(-1234);
        mockedUtilities.when(() -> Utils.getTenantDomain(-1234)).thenReturn("carbon.super");
    }

    private TenantTypeOrganization getOrganization(String id, String name, String description, String parent,
                                                   String type, String version) {

        TenantTypeOrganization organization = new TenantTypeOrganization(name);
        organization.setId(id);
        organization.setName(name);
        organization.setDescription(description);
        organization.setStatus(OrganizationStatus.ACTIVE.toString());
        organization.getParent().setId(parent);
        organization.setType(type);
        organization.setVersion(version);
        organization.setCreated(Instant.now());
        organization.setLastModified(Instant.now());
        return organization;
    }

    private void addOrganization(Organization organization, String tenantDomain) throws Exception {

        organizationManagementDAO.addOrganization(organization);
        TestUtils.storeAssociatedTenant(tenantDomain, organization.getId());
    }
}
