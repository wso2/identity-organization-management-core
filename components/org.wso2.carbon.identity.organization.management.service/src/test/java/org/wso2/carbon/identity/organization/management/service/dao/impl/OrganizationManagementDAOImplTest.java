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

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.ParentOrganizationDO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
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
    private static final String ATTRIBUTE_VALUE = "Sri Lanka";
    private static final String ORG_NAME = "XYZ builders";
    private static final String INVALID_DATA = "invalid data";
    private static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private String orgId;

    @BeforeClass
    public void setUp() throws Exception {

        initiateH2Base();
        mockDataSource();
        storeChildOrganization(ROOT_ORG_ID);
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
        parentOrganizationDO.setId(ROOT_ORG_ID);
        organization.setParent(parentOrganizationDO);

        List<OrganizationAttribute> attributes = new ArrayList<>();
        attributes.add(new OrganizationAttribute(ATTRIBUTE_KEY, ATTRIBUTE_VALUE));
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

        String organizationId = organizationManagementDAO.getOrganizationIdByName(
                ORG_NAME);
        Assert.assertEquals(organizationId, orgId);
    }

    @Test
    public void testGetOrganization() throws Exception {

        Organization organization = organizationManagementDAO.getOrganization(orgId);
        Assert.assertEquals(organization.getName(), ORG_NAME);
        Assert.assertEquals(organization.getParent().getId(), ROOT_ORG_ID);
    }

    @DataProvider(name = "dataForHasChildOrganizations")
    public Object[][] dataForHasChildOrganizations() {

        return new Object[][]{

                {ROOT_ORG_ID},
                {orgId},
        };
    }

    @Test(dataProvider = "dataForHasChildOrganizations")
    public void testHasChildOrganizations(String id) throws Exception {

        boolean hasChildOrganizations = organizationManagementDAO.hasChildOrganizations(id);
        if (StringUtils.equals(id, orgId)) {
            Assert.assertFalse(hasChildOrganizations);
        } else if (StringUtils.equals(id, ROOT_ORG_ID)) {
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
        storeOrganization(id, "Dummy organization",
                "This is a sample organization to test the delete functionality.", ROOT_ORG_ID);
        organizationManagementDAO.deleteOrganization(id);
        Assert.assertNull(organizationManagementDAO.getOrganization(id));
    }

    private void storeChildOrganization(String parentId) throws Exception {

        orgId = generateUniqueID();
        storeOrganization(orgId, "XYZ builders", "This is a construction company.", parentId);
        storeOrganizationAttributes(orgId);
    }

    private void storeOrganization(String id, String name, String description, String parentId)
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
    }

    private void storeOrganizationAttributes(String id) throws Exception {

        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO UM_ORG_ATTRIBUTE (UM_ORG_ID, UM_ATTRIBUTE_KEY, UM_ATTRIBUTE_VALUE) VALUES " +
                    "( ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, ATTRIBUTE_KEY);
            statement.setString(3, ATTRIBUTE_VALUE);
            statement.execute();
        }
    }
}
