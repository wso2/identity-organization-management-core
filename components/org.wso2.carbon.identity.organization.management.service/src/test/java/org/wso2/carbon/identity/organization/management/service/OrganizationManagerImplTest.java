/*
 * Copyright (c) 2022-2024, WSO2 LLC. (http://www.wso2.com).
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.listener.OrganizationManagerListener;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.identity.organization.management.util.TestUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.STRUCTURAL;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_ADD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REMOVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REPLACE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_NAME;

@WithAxisConfiguration
public class OrganizationManagerImplTest {

    private static final String SUPER = "Super";
    private static final String ORG1_NAME = "ABC Builders";
    private static final String ORG2_NAME = "XYZ Builders";
    private static final String ORG3_NAME = "Greater";
    private static final String NON_EXISTING_ORG_NAME = "Dummy Builders";
    private static final String NEW_ORG1_NAME = "ABC Builders New";
    private static final String ORG_DESCRIPTION = "This is a construction company.";
    private static final String NEW_ORG_NAME = "New Org";
    private static final String NEW_ORG_DESCRIPTION = "new sample description.";
    private static final String ORG_ATTRIBUTE_KEY_COUNTRY = "country";
    private static final String ORG_ATTRIBUTE_VALUE_COUNTRY = "Sri Lanka";
    private static final String ORG_ATTRIBUTE_KEY_CITY = "city";
    private static final String ORG_ATTRIBUTE_VALUE_CITY = "Colombo";
    private static final String SUPER_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String ROOT_ORG = "custom-root-org";
    private static final String ORG1_ID = "org_id_1";
    private static final String ORG2_ID = "org_id_2";
    private static final String ORG2_1 = "org_2_1";
    private static final String ORG3_1 = "org_3_1";
    private static final String ORG3_ID = "org_id_3";
    private static final String INVALID_PARENT_ID = "invalid_parent_id";
    private static final String INVALID_ORG_ID = "invalid_org_id";

    private OrganizationManagerImpl organizationManager;

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    private RealmService realmService;

    private UserRealm userRealm;

    private AuthorizationManager authorizationManager;

    private TenantManager tenantManager;

    private Tenant tenant;

    private MockedStatic<Utils> mockedUtilities;

    @BeforeClass
    public void init() {

        realmService = mock(RealmService.class);
        tenantManager = mock(TenantManager.class);
        tenant = mock(Tenant.class);
        mockUtils();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        organizationManager = new OrganizationManagerImpl();
        OrganizationManagementDataHolder.getInstance().setOrganizationManagerListener(mock(
                OrganizationManagerListener.class));
        OrganizationManagementDataHolder.getInstance().setRealmService(realmService);

        TestUtils.initiateH2Base();
        TestUtils.mockDataSource();

        // Super -> org1 -> org2
        //       -> org3
        Organization organization1 = getOrganization(ORG1_ID, ORG1_NAME, ORG_DESCRIPTION, SUPER_ORG_ID,
                STRUCTURAL.toString());
        Organization organization2 = getOrganization(ORG2_ID, ORG2_NAME, ORG_DESCRIPTION, ORG1_ID,
                STRUCTURAL.toString());
        Organization organization3 = getOrganization(ORG3_ID, ORG3_NAME, ORG_DESCRIPTION, SUPER_ORG_ID,
                TENANT.toString());

        OrganizationAttribute organizationAttribute = new OrganizationAttribute(ORG_ATTRIBUTE_KEY_COUNTRY,
                                                                            ORG_ATTRIBUTE_VALUE_COUNTRY);
        organization3.setAttribute(organizationAttribute);
        organizationAttribute = new OrganizationAttribute(ORG_ATTRIBUTE_KEY_CITY, ORG_ATTRIBUTE_VALUE_CITY);
        organization3.setAttribute(organizationAttribute);

        organizationManagementDAO.addOrganization(organization1);
        organizationManagementDAO.addOrganization(organization2);
        organizationManagementDAO.addOrganization(organization3);
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
                SUPER_ORG_ID, STRUCTURAL.toString());
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        Organization addedOrganization = organizationManager.addOrganization(sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
    }

    @Test
    public void testAddRootOrganization() throws Exception {

        // Root_1
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ROOT_ORG, ORG_DESCRIPTION,
                null, TENANT.toString());
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenant(anyInt())).thenReturn(tenant);
        Organization addedOrganization = organizationManager.addRootOrganization(1, sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created root organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
    }

    @Test
    public void testAddOrganizationFromImmediateParent() throws Exception {

        // Super -> org1 -> org2_1
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG2_1,
                ORG_DESCRIPTION, ORG1_ID, TENANT.toString());
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG1_ID);
        Organization addedOrganization = organizationManager.addOrganization(sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationFromAncestorOrg() throws Exception {

        // Super -> org1 -> org2 -> org3_1
        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), ORG3_1,
                ORG_DESCRIPTION, ORG2_ID, TENANT.toString());
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
                SUPER_ORG_ID, TENANT.toString());
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
                SUPER_ORG_ID, TENANT.toString());
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        mockedUtilities.when(Utils::getSubOrgStartLevel).thenReturn(2);
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
                ORG2_ID, TENANT.toString());
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORG2_ID);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithInvalidParentId() throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(),
                NEW_ORG_NAME, ORG_DESCRIPTION, INVALID_PARENT_ID, STRUCTURAL.toString());
        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithReservedName() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), SUPER, ORG_DESCRIPTION, ORG1_NAME,
                TENANT.toString());
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
                orgName, ORG_DESCRIPTION, parentId, TENANT.toString());
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
                SUPER_ORG_ID, STRUCTURAL.toString());
        List<OrganizationAttribute> organizationAttributeList = new ArrayList<>();
        OrganizationAttribute organizationAttribute = new OrganizationAttribute(attributeKey, attributeValue);
        organizationAttributeList.add(organizationAttribute);
        organization.setAttributes(organizationAttributeList);
        organizationManager.addOrganization(organization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationDuplicateAttributeKeys() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                SUPER_ORG_ID, STRUCTURAL.toString());
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
    }

    @DataProvider(name = "dataForFilterOrganizationsByMetaAttributes")
    public Object[][] dataForFilterOrganizationsByMetaAttributes() {

        return new Object[][]{
                {"attributes.country co S", false},
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

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testDeleteOrganization() throws Exception {

        organizationManager.deleteOrganization(ORG2_ID);
        assertNull(organizationManager.getOrganization(ORG2_ID, false, false));
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
                STRUCTURAL.toString());
        Organization updatedOrganization = organizationManager.updateOrganization(ORG1_ID, ORG1_NAME,
                sampleOrganization);
        assertEquals(NEW_ORG_DESCRIPTION, updatedOrganization.getDescription());
        assertEquals(SUPER_ORG_ID, updatedOrganization.getParent().getId());

    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testUpdateOrganizationWithEmptyOrganizationId() throws Exception {

        organizationManager.updateOrganization(StringUtils.EMPTY, ORG1_NAME,
                getOrganization(ORG1_ID, ORG1_NAME, NEW_ORG_DESCRIPTION, SUPER_ORG_ID, STRUCTURAL.toString()));
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testUpdateOrganizationWithInvalidOrganizationId() throws Exception {

        organizationManager.updateOrganization(INVALID_ORG_ID, ORG1_NAME, getOrganization(INVALID_ORG_ID, ORG1_NAME,
                NEW_ORG_DESCRIPTION, SUPER_ORG_ID, STRUCTURAL.toString()));
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

    private void mockUtils() {

        mockedUtilities = Mockito.mockStatic(Utils.class, Mockito.withSettings()
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        mockedUtilities.when(() -> Utils.getTenantId("carbon.super")).thenReturn(-1234);
        mockedUtilities.when(() -> Utils.getTenantDomain(-1234)).thenReturn("carbon.super");
    }

    private Organization getOrganization(String id, String name, String description, String parent, String type) {

        Organization organization = new Organization();
        organization.setId(id);
        organization.setName(name);
        organization.setDescription(description);
        organization.setStatus(OrganizationStatus.ACTIVE.toString());
        organization.getParent().setId(parent);
        organization.setType(type);
        organization.setCreated(Instant.now());
        organization.setLastModified(Instant.now());
        return organization;
    }
}
