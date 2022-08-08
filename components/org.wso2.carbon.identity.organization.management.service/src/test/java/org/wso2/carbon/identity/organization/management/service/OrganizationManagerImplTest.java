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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.organization.management.authz.service.OrganizationManagementAuthorizationManager;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.util.TestUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.STRUCTURAL;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_ADD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REMOVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REPLACE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@WithAxisConfiguration
public class OrganizationManagerImplTest {

    private static final String ROOT = "ROOT";
    private static final String ORG1_NAME = "ABC Builders";
    private static final String ORG2_NAME = "XYZ Builders";
    private static final String ORG_DESCRIPTION = "This is a construction company.";
    private static final String NEW_ORG_NAME = "New Org";
    private static final String NEW_ORG_DESCRIPTION = "new sample description.";
    private static final String ORG_ATTRIBUTE_KEY = "country";
    private static final String ORG_ATTRIBUTE_VALUE = "Sri Lanka";
    private static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String ORG1_ID = "org_id_1";
    private static final String ORG2_ID = "org_id_2";
    private static final String INVALID_PARENT_ID = "invalid_parent_id";
    private static final String INVALID_ORG_ID = "invalid_org_id";
    private static final String ERROR_MESSAGE = "message";
    private static final String ERROR_DESCRIPTION = "description";
    private static final String ERROR_CODE = "code";

    private OrganizationManagerImpl organizationManager;

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    private RealmService realmService;

    private UserRealm userRealm;

    private AuthorizationManager authorizationManager;

    @BeforeClass
    public void init() {
        realmService = mock(RealmService.class);
        userRealm = mock(UserRealm.class);
        authorizationManager = mock(AuthorizationManager.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        organizationManager = new OrganizationManagerImpl();

        TestUtils.initiateH2Base();
        TestUtils.mockDataSource();

        Organization organization1 = getOrganization(ORG1_ID, ORG1_NAME, ORG_DESCRIPTION, ROOT_ORG_ID,
                    STRUCTURAL.toString());
        Organization organization2 = getOrganization(ORG2_ID, ORG2_NAME, ORG_DESCRIPTION, ORG1_ID,
                STRUCTURAL.toString());
        organizationManagementDAO.addOrganization(organization1);
        organizationManagementDAO.addOrganization(organization2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @Test
    public void testAddOrganization() throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                ROOT_ORG_ID, STRUCTURAL.toString());
        mockCarbonContext();
        mockAuthorizationManager();
        when(authorizationManager.isUserAuthorized(anyString(), anyString(), anyString())).thenReturn(true);

        OrganizationManagementAuthorizationManager authorizationManager =
                mock(OrganizationManagementAuthorizationManager.class);
        setFinalStatic(OrganizationManagementAuthorizationManager.class.getDeclaredField("INSTANCE"),
                authorizationManager);

        when(OrganizationManagementAuthorizationManager.getInstance().isUserAuthorized(anyString(), anyString(),
                anyString())).thenReturn(true);

        Organization addedOrganization = organizationManager.addOrganization(sampleOrganization);
        assertNotNull(addedOrganization.getId(), "Created organization id cannot be null");
        assertEquals(addedOrganization.getName(), sampleOrganization.getName());
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithInvalidParentId() throws Exception {

        Organization sampleOrganization = getOrganization(UUID.randomUUID().toString(),
                NEW_ORG_NAME, ORG_DESCRIPTION, INVALID_PARENT_ID, STRUCTURAL.toString());
        mockCarbonContext();
        organizationManager.addOrganization(sampleOrganization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationWithReservedName() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), ROOT, ORG_DESCRIPTION, ORG1_NAME,
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
                {ORG_ATTRIBUTE_KEY, null},
                {null, ORG_ATTRIBUTE_VALUE},
                {StringUtils.EMPTY, ORG_ATTRIBUTE_VALUE}
        };
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class,
            dataProvider = "dataForAddOrganizationInvalidOrganizationAttributes")
    public void testAddOrganizationInvalidAttributes(String attributeKey, String attributeValue) throws Exception {

        mockCarbonContext();
        Organization organization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                ROOT_ORG_ID, STRUCTURAL.toString());
        List<OrganizationAttribute> organizationAttributeList = new ArrayList<>();
        OrganizationAttribute organizationAttribute = new OrganizationAttribute(attributeKey, attributeValue);
        organizationAttributeList.add(organizationAttribute);
        organization.setAttributes(organizationAttributeList);
        organizationManager.addOrganization(organization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationDuplicateAttributeKeys() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                ROOT_ORG_ID, STRUCTURAL.toString());
        mockCarbonContext();
        List<OrganizationAttribute> organizationAttributeList = new ArrayList<>();
        OrganizationAttribute organizationAttribute1 = new OrganizationAttribute(ORG_ATTRIBUTE_KEY,
                ORG_ATTRIBUTE_VALUE);
        OrganizationAttribute organizationAttribute2 = new OrganizationAttribute(ORG_ATTRIBUTE_KEY,
                ORG_ATTRIBUTE_VALUE);
        organizationAttributeList.add(organizationAttribute1);
        organizationAttributeList.add(organizationAttribute2);
        organization.setAttributes(organizationAttributeList);
        organizationManager.addOrganization(organization);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testAddOrganizationUserNotAuthorized() throws Exception {

        Organization organization = getOrganization(UUID.randomUUID().toString(), NEW_ORG_NAME, ORG_DESCRIPTION,
                ROOT_ORG_ID, STRUCTURAL.toString());
        mockCarbonContext();
        mockAuthorizationManager();
        when(authorizationManager.isUserAuthorized(anyString(), anyString(), anyString())).thenReturn(false);
        OrganizationManagementAuthorizationManager orgAuthorizationManager =
                mock(OrganizationManagementAuthorizationManager.class);
        setFinalStatic(OrganizationManagementAuthorizationManager.class.getDeclaredField("INSTANCE"),
                orgAuthorizationManager);
        when(orgAuthorizationManager.isUserAuthorized(anyString(), anyString(), anyString())).thenReturn(false);

        organizationManager.addOrganization(organization);
    }

    @Test
    public void testGetOrganization() throws Exception {

        Organization organization = organizationManager.getOrganization(ORG1_ID, false, false);
        assertEquals(organization.getName(), ORG1_NAME);
        assertEquals(organization.getParent().getId(), ROOT_ORG_ID);
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
        assertEquals(organization.getParent().getId(), ROOT_ORG_ID);
        assertEquals(organization.getChildOrganizations().size(), 1);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationsWithUnsupportedFilterAttribute() throws Exception {

        mockCarbonContext();
        organizationManager.getOrganizations(10, null, null, "ASC",
                "invalid_attribute co xyz", false);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationsWithUnsupportedComplexQueryInFilter() throws Exception {

        mockCarbonContext();
        organizationManager.getOrganizations(10, null, null, "ASC",
                "name co xyz or name co abc", false);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testGetOrganizationsWithInvalidPaginationAttribute() throws Exception {

        mockCarbonContext();
        organizationManager.getOrganizations(10, "MjAyNjkzMjg=", null, "ASC", "name co xyz",
                false);
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

        Organization sampleOrganization = getOrganization(ORG1_ID, ORG1_NAME, NEW_ORG_DESCRIPTION, ROOT_ORG_ID,
                STRUCTURAL.toString());
        Organization updatedOrganization = organizationManager.updateOrganization(ORG1_ID, ORG1_NAME,
                sampleOrganization);
        assertEquals(updatedOrganization.getDescription(), NEW_ORG_DESCRIPTION);
        assertEquals(updatedOrganization.getParent().getId(), ROOT_ORG_ID);
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testUpdateOrganizationWithEmptyOrganizationId() throws Exception {

        organizationManager.updateOrganization(StringUtils.EMPTY, ORG1_NAME,
                getOrganization(ORG1_ID, ORG1_NAME, NEW_ORG_DESCRIPTION, ROOT_ORG_ID, STRUCTURAL.toString()));
    }

    @Test(expectedExceptions = OrganizationManagementClientException.class)
    public void testUpdateOrganizationWithInvalidOrganizationId() throws Exception {

        organizationManager.updateOrganization(INVALID_ORG_ID, ORG1_NAME, getOrganization(INVALID_ORG_ID, ORG1_NAME,
                NEW_ORG_DESCRIPTION, ROOT_ORG_ID, STRUCTURAL.toString()));
    }

    private void mockCarbonContext() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome,
                "repository/conf").toString());

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
    }

    private void mockAuthorizationManager() throws UserStoreException {

        OrganizationManagementDataHolder.getInstance().setRealmService(realmService);
        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
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

    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
