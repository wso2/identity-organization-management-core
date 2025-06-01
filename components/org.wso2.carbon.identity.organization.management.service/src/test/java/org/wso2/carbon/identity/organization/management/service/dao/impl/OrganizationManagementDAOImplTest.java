/*
 * Copyright (c) 2022-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.filter.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationNode;
import org.wso2.carbon.identity.organization.management.service.model.ParentOrganizationDO;
import org.wso2.carbon.identity.organization.management.util.TestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ASC_SORT_ORDER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ATTRIBUTES_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ATTRIBUTES_FIELD_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.STRUCTURAL;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.generateUniqueID;
import static org.wso2.carbon.identity.organization.management.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.organization.management.util.TestUtils.getConnection;
import static org.wso2.carbon.identity.organization.management.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.organization.management.util.TestUtils.mockDataSource;

public class OrganizationManagementDAOImplTest {

    private OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();
    private static final Calendar CALENDAR = Calendar.getInstance(TimeZone.getTimeZone(UTC));
    private static final String ATTRIBUTE_KEY = "country";
    private static final String ATTRIBUTE_KEY_REGION = "region";
    private static final String ATTRIBUTE_VALUE = "Sri Lanka";
    private static final String ATTRIBUTE_KEY_CITY = "city";
    private static final String ATTRIBUTE_VALUE_CITY = "Colombo";
    private static final String ORG_NAME = "XYZ builders";
    private static final String ORG_HANDLE = "xyzbuilders";
    private static final String CHILD_ORG_NAME = "ABC builders";
    private static final String CHILD_ORG_HANDLE = "abcbuilders";
    private static final String ORG_DESCRIPTION = "This is a construction company.";
    private static final String INVALID_DATA = "invalid data";
    private static final String SUPER_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private String orgId;
    private String childOrgId;

    // For testing organization graph.
    private String grandChildOrgId1;
    private String grandChildOrgId2;
    private String greatGrandChildOrgId;
    private static final String GRANDCHILD_ORG_NAME_1 = "DEF builders";
    private static final String GRANDCHILD_ORG_HANDLE_1 = "defbuilders";
    private static final String GRANDCHILD_ORG_NAME_2 = "GHI builders";
    private static final String GRANDCHILD_ORG_HANDLE_2 = "ghibuilders";
    private static final String GREAT_GRANDCHILD_ORG_NAME = "JKL builders";
    private static final String GREAT_GRANDCHILD_ORG_HANDLE = "jklbuilders";

    private String orgWithNoChildrenId;
    private static final String ORG_WITH_NO_CHILDREN_NAME = "Lonely Org";
    private static final String ORG_WITH_NO_CHILDREN_HANDLE = "lonelyorg";

    @BeforeClass
    public void setUp() throws Exception {

        initiateH2Base();
        mockDataSource();

        orgId = generateUniqueID();
        childOrgId = generateUniqueID();
        grandChildOrgId1 = generateUniqueID();
        grandChildOrgId2 = generateUniqueID();
        greatGrandChildOrgId = generateUniqueID();
        orgWithNoChildrenId = generateUniqueID();

        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_KEY, ATTRIBUTE_VALUE);
        storeChildOrganization(orgId, ORG_NAME, ORG_HANDLE, ORG_DESCRIPTION, SUPER_ORG_ID, attributes);
        storeOrganizationAttributes(orgId, ATTRIBUTE_KEY_CITY, ATTRIBUTE_VALUE_CITY);
        storeChildOrganization(childOrgId, CHILD_ORG_NAME, CHILD_ORG_HANDLE, ORG_DESCRIPTION, orgId);
        storeOrganizationAttributes(childOrgId, ATTRIBUTE_KEY_REGION, ATTRIBUTE_VALUE);

        // Create grandchild and great-grandchild organizations for graph testing.
        storeChildOrganization(grandChildOrgId1, GRANDCHILD_ORG_NAME_1, GRANDCHILD_ORG_HANDLE_1,
                ORG_DESCRIPTION, childOrgId);
        storeChildOrganization(grandChildOrgId2, GRANDCHILD_ORG_NAME_2, GRANDCHILD_ORG_HANDLE_2,
                ORG_DESCRIPTION, childOrgId);
        storeChildOrganization(greatGrandChildOrgId, GREAT_GRANDCHILD_ORG_NAME, GREAT_GRANDCHILD_ORG_HANDLE,
                ORG_DESCRIPTION, grandChildOrgId1);

        // Create organization with no children.
        storeChildOrganization(orgWithNoChildrenId, ORG_WITH_NO_CHILDREN_NAME, ORG_WITH_NO_CHILDREN_HANDLE,
                "No child organization.", SUPER_ORG_ID);
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Base();
    }

    @Test
    public void testAddOrganization() throws Exception {

        String orgId = generateUniqueID();

        Organization organization = new Organization();
        organization.setId(orgId);
        organization.setName("org1");
        organization.setDescription("org1 description.");
        organization.setCreated(Instant.now());
        organization.setLastModified(Instant.now());
        organization.setStatus(OrganizationManagementConstants.OrganizationStatus.ACTIVE.toString());
        organization.setType(STRUCTURAL.toString());

        ParentOrganizationDO parentOrganizationDO = new ParentOrganizationDO();
        parentOrganizationDO.setId(SUPER_ORG_ID);
        organization.setParent(parentOrganizationDO);

        List<OrganizationAttribute> attributes = new ArrayList<>();
        attributes.add(new OrganizationAttribute(ATTRIBUTE_KEY, "USA"));
        organization.setAttributes(attributes);

        organizationManagementDAO.addOrganization(organization);
        Assert.assertNotNull(organizationManagementDAO.getOrganization(orgId));
    }

    @DataProvider(name = "dataForIsOrganizationExistByName")
    public Object[][] dataForIsOrganizationExistByName() {

        return new Object[][]{
                {ORG_NAME},
                {INVALID_DATA},
        };
    }

    @Test(dataProvider = "dataForIsOrganizationExistByName")
    public void testIsOrganizationExistByName(String name) throws Exception {

        boolean organizationExistByName = organizationManagementDAO.isOrganizationExistByName(name);
        if (StringUtils.equals(name, ORG_NAME)) {
            Assert.assertTrue(organizationExistByName);
        } else if (StringUtils.equals(name, INVALID_DATA)) {
            Assert.assertFalse(organizationExistByName);
        }
    }

    @DataProvider(name = "dataForIsOrganizationExistById")
    public Object[][] dataForIsOrganizationExistById() {

        return new Object[][]{
                {orgId},
                {INVALID_DATA},
        };
    }

    @Test(dataProvider = "dataForIsOrganizationExistById")
    public void testIsOrganizationExistById(String id) throws Exception {

        boolean organizationExistByName = organizationManagementDAO.isOrganizationExistById(id);
        if (StringUtils.equals(id, orgId)) {
            Assert.assertTrue(organizationExistByName);
        } else if (StringUtils.equals(id, INVALID_DATA)) {
            Assert.assertFalse(organizationExistByName);
        }
    }

    @Test
    public void testGetOrganizationIdByName() throws Exception {

        String organizationId = organizationManagementDAO.getOrganizationIdByName(ORG_NAME);
        Assert.assertEquals(organizationId, orgId);
    }

    @Test
    public void testGetOrganization() throws Exception {

        Organization organization = organizationManagementDAO.getOrganization(orgId);
        Assert.assertEquals(organization.getName(), ORG_NAME);
        Assert.assertEquals(organization.getParent().getId(), SUPER_ORG_ID);
    }

    @Test
    public void testGetChildOrganizations() throws Exception {

        List<BasicOrganization> organizations = organizationManagementDAO.getChildOrganizations(orgId, false);
        Assert.assertEquals(organizations.get(0).getOrganizationHandle(), CHILD_ORG_HANDLE);
    }

    @Test
    public void testGetBasicOrganizationList() throws Exception {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        List<BasicOrganization> organizations = organizationManagementDAO.getOrganizations(false, 10,
                SUPER_ORG_ID, ASC_SORT_ORDER, Collections.emptyList(), Collections.emptyList());
        Assert.assertEquals(organizations.get(0).getName(), ORG_NAME);
    }

    @DataProvider(name = "dataForFilterOrganizationsByPrimaryAttributes")
    public Object[][] dataForFilterOrganizationsByPrimaryAttributes() {

        return new Object[][]{
                {"name", "co", "XYZ"},
                {"id", "eq", orgId},
                {"description", "sw", "This"}
        };
    }

    @Test(dataProvider = "dataForFilterOrganizationsByPrimaryAttributes")
    public void testFilterOrganizationsByPrimaryAttributes(String attributeValue, String operation, String value)
            throws OrganizationManagementServerException {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        ExpressionNode expressionNode = getExpressionNode(attributeValue, operation, value);
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(expressionNode);
        List<Organization> organizations = organizationManagementDAO.getOrganizationsList(false, 10,
                SUPER_ORG_ID, "DESC", expressionNodes, new ArrayList<>());

        Assert.assertEquals(organizations.get(0).getName(), ORG_NAME);
        Assert.assertTrue(organizations.get(0).getAttributes().isEmpty());
    }

    @DataProvider(name = "dataForFilterOrganizationsByMetaAttributes")
    public Object[][] dataForFilterOrganizationsByMetaAttributes() {

        return new Object[][]{
                {ORGANIZATION_ATTRIBUTES_FIELD_PREFIX + ATTRIBUTE_KEY, "eq", ATTRIBUTE_VALUE},
                {ORGANIZATION_ATTRIBUTES_FIELD_PREFIX + ATTRIBUTE_KEY, "co", "L"}
        };
    }

    @Test(dataProvider = "dataForFilterOrganizationsByMetaAttributes")
    public void testFilterOrganizationsByMetaAttributes(String attributeValue, String operation, String value)
            throws OrganizationManagementServerException {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        ExpressionNode expressionNode = getExpressionNode(attributeValue, operation, value);
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(expressionNode);
        List<Organization> organizations = organizationManagementDAO.getOrganizationsList(false, 10,
                SUPER_ORG_ID, "DESC", expressionNodes, new ArrayList<>());

        Assert.assertEquals(organizations.get(0).getName(), ORG_NAME);
    }

    @DataProvider(name = "dataForFilterOrganizationsByMultipleAttributes")
    public Object[][] dataForFilterOrganizationsByMultipleAttributes() {

        return new Object[][]{
                {"attributes.country co S and name co XYZ"},
                {"attributes.country sw S and attributes.country ew a and attributes.city eq Colombo"},
        };
    }

    @Test(dataProvider = "dataForFilterOrganizationsByMultipleAttributes")
    public void testFilterOrganizationsByMultipleAttributes(String filter)
            throws OrganizationManagementServerException {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        String[] filters = filter.split(" and ");

        for (String singleFilter : filters) {
            String[] parts = singleFilter.split(" ");
            String attributeValue = parts[0];
            String operation = parts[1];
            String value = parts[2];

            ExpressionNode expressionNode = getExpressionNode(attributeValue, operation, value);
            expressionNodes.add(expressionNode);
        }
        List<Organization> organizations = organizationManagementDAO.getOrganizationsList(false, 10,
                SUPER_ORG_ID, "DESC", expressionNodes, new ArrayList<>());

        Assert.assertEquals(organizations.get(0).getName(), ORG_NAME);
        Assert.assertEquals(organizations.get(0).getAttributes().get(0).getKey(), ATTRIBUTE_KEY);
        Assert.assertEquals(organizations.get(0).getAttributes().get(0).getValue(), ATTRIBUTE_VALUE);
    }

    @DataProvider(name = "dataForGetOrganizationsMetaAttributes")
    public Object[][] dataForGetOrganizationsMetaAttributes() {

        return new Object[][]{
                {ORGANIZATION_ATTRIBUTES_FIELD, "eq", ATTRIBUTE_KEY, false},
                {ORGANIZATION_ATTRIBUTES_FIELD, "eq", ATTRIBUTE_KEY_REGION, true},
        };
    }

    @Test(dataProvider = "dataForGetOrganizationsMetaAttributes")
    public void testGetOrganizationsMetaAttributes(String attributeValue, String operation, String value,
                                                   boolean isRecursive) throws OrganizationManagementServerException {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        ExpressionNode expressionNode = getExpressionNode(attributeValue, operation, value);
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(expressionNode);
        List<String> metaAttributes = organizationManagementDAO.getOrganizationsMetaAttributes(isRecursive, 10,
                SUPER_ORG_ID, "DESC", expressionNodes);

        Assert.assertEquals(metaAttributes.size(), 1);
        if (isRecursive) {
            Assert.assertEquals(metaAttributes.get(0), ATTRIBUTE_KEY_REGION);
        } else {
            Assert.assertEquals(metaAttributes.get(0), ATTRIBUTE_KEY);
        }
    }

    @DataProvider(name = "dataForHasChildOrganizations")
    public Object[][] dataForHasChildOrganizations() {

        return new Object[][]{
                {SUPER_ORG_ID},
                {orgId},
        };
    }

    @Test(dataProvider = "dataForHasChildOrganizations")
    public void testHasChildOrganizations(String id) throws Exception {

        boolean hasChildOrganizations = organizationManagementDAO.hasChildOrganizations(id);
        if (StringUtils.equals(id, childOrgId)) {
            Assert.assertFalse(hasChildOrganizations);
        } else if (StringUtils.equals(id, SUPER_ORG_ID)) {
            Assert.assertTrue(hasChildOrganizations);
        }
    }

    @DataProvider(name = "dataForIsAttributeExistByKey")
    public Object[][] dataForIsAttributeExistByKey() {

        return new Object[][]{
                {ATTRIBUTE_KEY},
                {INVALID_DATA},
        };
    }

    @Test(dataProvider = "dataForIsAttributeExistByKey")
    public void testIsAttributeExistByKey(String key) throws Exception {

        boolean attributeExistByKey = organizationManagementDAO.isAttributeExistByKey(orgId, key);
        if (StringUtils.equals(key, ATTRIBUTE_KEY)) {
            Assert.assertTrue(attributeExistByKey);
        } else if (StringUtils.equals(key, INVALID_DATA)) {
            Assert.assertFalse(attributeExistByKey);
        }
    }

    @Test
    public void testDeleteOrganization() throws Exception {

        String id = generateUniqueID();
        storeOrganization(id, "Dummy organization", "dummyorg",
                "This is a sample organization to test the delete functionality.", SUPER_ORG_ID);
        organizationManagementDAO.deleteOrganization(id);
        Assert.assertNull(organizationManagementDAO.getOrganization(id));
    }

    @Test
    public void testGetChildOrganizationGraph() throws Exception {

        // Test non-recursive first (should only return immediate children).
        List<OrganizationNode> nonRecursiveGraph = organizationManagementDAO.getChildOrganizationGraph(orgId, false);

        // Should have only 1 node (the immediate child).
        Assert.assertEquals(nonRecursiveGraph.size(), 1);

        // Verify the immediate child.
        OrganizationNode immediateChildNode = nonRecursiveGraph.get(0);
        Assert.assertEquals(immediateChildNode.getId(), childOrgId);
        Assert.assertEquals(immediateChildNode.getName(), CHILD_ORG_NAME);

        // Should have no children since we're not recursive.
        Assert.assertTrue(immediateChildNode.getChildren().isEmpty());

        // Now test recursive (should return the full hierarchy).
        List<OrganizationNode> recursiveGraph = organizationManagementDAO.getChildOrganizationGraph(orgId, true);

        // Should still have 1 top-level node.
        Assert.assertEquals(recursiveGraph.size(), 1);

        // Get the root node.
        OrganizationNode rootNode = recursiveGraph.get(0);
        Assert.assertEquals(rootNode.getId(), childOrgId);
        Assert.assertEquals(rootNode.getName(), CHILD_ORG_NAME);

        // Root node should have 2 children.
        Assert.assertEquals(rootNode.getChildren().size(), 2);

        // Find grandchild1 node, which should contain great-grandchild.
        OrganizationNode grandChild1 = null;
        OrganizationNode grandChild2 = null;

        for (OrganizationNode child : rootNode.getChildren()) {
            if (child.getId().equals(grandChildOrgId1)) {
                grandChild1 = child;
            } else if (child.getId().equals(grandChildOrgId2)) {
                grandChild2 = child;
            }
        }

        Assert.assertNotNull(grandChild1, "Grandchild 1 node should be present");
        Assert.assertNotNull(grandChild2, "Grandchild 2 node should be present");

        Assert.assertEquals(grandChild1.getName(), GRANDCHILD_ORG_NAME_1);

        Assert.assertEquals(grandChild2.getName(), GRANDCHILD_ORG_NAME_2);

        // grandChild1 should have 1 child (the great-grandchild).
        Assert.assertEquals(grandChild1.getChildren().size(), 1);

        // grandChild2 should have 0 children.
        Assert.assertEquals(grandChild2.getChildren().size(), 0);

        // Verify great-grandchild.
        OrganizationNode greatGrandChild = grandChild1.getChildren().get(0);
        Assert.assertEquals(greatGrandChild.getId(), greatGrandChildOrgId);
        Assert.assertEquals(greatGrandChild.getName(), GREAT_GRANDCHILD_ORG_NAME);
        Assert.assertEquals(greatGrandChild.getChildren().size(), 0);

        // Now test for org with no children (recursive).
        List<OrganizationNode> noChildrenRecursiveGraph = organizationManagementDAO.getChildOrganizationGraph(
                orgWithNoChildrenId, true);
        Assert.assertNotNull(noChildrenRecursiveGraph, "Graph should not be null even if no children");
        Assert.assertTrue(noChildrenRecursiveGraph.isEmpty(), "Graph should be empty for org with no children" +
                " (recursive)");
    }

    @Test
    public void testGetChildOrganizationGraph_NoChildren() throws Exception {

        // Should return an empty list for org with no children.
        List<OrganizationNode> result = organizationManagementDAO.getChildOrganizationGraph(orgWithNoChildrenId, false);
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertTrue(result.isEmpty(), "Result should be empty for org with no children");
    }

    private void storeChildOrganization(String id, String name, String handle, String description, String parentId,
                                        Map<String, String> attributes) throws Exception {

        storeOrganization(id, name, handle, description, parentId);
        storeOrganizationHierarchy(id, parentId);
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                storeOrganizationAttributes(id, entry.getKey(), entry.getValue());
            }
        }
    }

    private void storeChildOrganization(String id, String name, String handle, String description, String parentId)
            throws Exception {

        storeChildOrganization(id, name, handle, description, parentId, null);
    }

    private void storeOrganization(String id, String name, String handle, String description, String parentId)
            throws Exception {

        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO UM_ORG (UM_ID, UM_ORG_NAME, UM_ORG_DESCRIPTION, UM_CREATED_TIME, " +
                    "UM_LAST_MODIFIED, UM_PARENT_ID, UM_ORG_TYPE) VALUES ( ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setTimestamp(4, Timestamp.from(Instant.now()), CALENDAR);
            statement.setTimestamp(5, Timestamp.from(Instant.now()), CALENDAR);
            statement.setString(6, parentId);
            statement.setString(7, STRUCTURAL.toString());
            statement.execute();
        }
        TestUtils.storeAssociatedTenant(handle, id);
    }

    private void storeOrganizationHierarchy(String id, String parentId) throws Exception {

        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO UM_ORG_HIERARCHY (UM_PARENT_ID, UM_ID, DEPTH) VALUES ( ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, parentId);
            statement.setString(2, id);
            statement.setString(3, "1");
            statement.execute();
        }
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO UM_ORG_HIERARCHY (UM_PARENT_ID, UM_ID, DEPTH) SELECT UM_PARENT_ID, ?, " +
                    "DEPTH + 1 FROM UM_ORG_HIERARCHY WHERE UM_ORG_HIERARCHY.UM_ID = ? AND UM_PARENT_ID <> UM_ID";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, parentId);
            statement.execute();
        }
    }

    private void storeOrganizationAttributes(String id, String attributeKey, String attributeValue) throws Exception {

        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO UM_ORG_ATTRIBUTE (UM_ORG_ID, UM_ATTRIBUTE_KEY, UM_ATTRIBUTE_VALUE) VALUES " +
                    "( ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, attributeKey);
            statement.setString(3, attributeValue);
            statement.execute();
        }
    }

    private ExpressionNode getExpressionNode(String attributeValue, String operation, String value) {

        ExpressionNode expressionNode = new ExpressionNode();
        expressionNode.setAttributeValue(attributeValue);
        expressionNode.setOperation(operation);
        expressionNode.setValue(value);
        return expressionNode;
    }
}
