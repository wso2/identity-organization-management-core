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

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.management.service.cache.MinimalOrganizationCacheByOrgId;
import org.wso2.carbon.identity.organization.management.service.cache.MinimalOrganizationCacheEntry;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationIdCacheKey;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.identity.organization.management.util.TestUtils;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;

public class CacheBackedOrganizationManagementDAOTest {

    private static final String TEST_ORG_TENANT_DOMAIN = "sub.com";
    private static final String TEST_ORG_ID = "37b035d3-ca7b-4bcf-90c9-6adda2a08664";
    private static final String TEST_ORG_NAME = "SUB";
    private static final String TEST_ORG_STATUS = "ACTIVE";
    private static final String TEST_ORG_CREATED_TIME = Instant.now().toString();;
    private static final int TEST_ORG_DEPTH = 1;

    private static final String TEST_PARENT_ORG_ID = "2bb16581-c7ec-4a3b-a3c5-1f48b8028eec";

    OrganizationManagementDAO organizationManagementDAO;
    CacheBackedOrganizationManagementDAO cacheBackedOrganizationManagementDAO;
    MinimalOrganizationCacheByOrgId minimalOrganizationCache;
    MinimalOrganization minimalOrganization;
    MockedStatic<Utils> utils;

    @BeforeClass
    public void setup() {

        TestUtils.mockCarbonContext(SUPER_ORG_ID);
        organizationManagementDAO = mock(OrganizationManagementDAO.class);
        cacheBackedOrganizationManagementDAO = new CacheBackedOrganizationManagementDAO(organizationManagementDAO);
        minimalOrganizationCache = MinimalOrganizationCacheByOrgId.getInstance();
        utils = mockStatic(Utils.class);
        utils.when(() -> Utils.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
        utils.when(() -> Utils.getTenantId(TEST_ORG_TENANT_DOMAIN)).thenReturn(2);

        minimalOrganization = new MinimalOrganization.Builder()
                .id(TEST_ORG_ID)
                .name(TEST_ORG_NAME)
                .status(TEST_ORG_STATUS)
                .created(TEST_ORG_CREATED_TIME)
                .organizationHandle(TEST_ORG_TENANT_DOMAIN)
                .parentOrganizationId(TEST_PARENT_ORG_ID)
                .depth(TEST_ORG_DEPTH)
                .build();
    }

    @AfterClass
    public void tearDown() {

        utils.close();
    }

    @BeforeMethod
    public void init() throws Exception {

        minimalOrganizationCache.clear(TEST_ORG_TENANT_DOMAIN);
        doReturn(TEST_ORG_TENANT_DOMAIN).when(organizationManagementDAO).resolveTenantDomain(TEST_ORG_ID);
    }

    @Test
    public void testGetMinimalOrganizationFromDB() throws OrganizationManagementException {

        doReturn(minimalOrganization).when(organizationManagementDAO).getMinimalOrganization(TEST_ORG_ID,
                TEST_ORG_TENANT_DOMAIN);

        MinimalOrganization result = cacheBackedOrganizationManagementDAO.getMinimalOrganization(TEST_ORG_ID,
                TEST_ORG_TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getId(), TEST_ORG_ID);
        assertEquals(result.getName(), TEST_ORG_NAME);
        assertEquals(result.getStatus(), TEST_ORG_STATUS);
        assertEquals(result.getCreated(), TEST_ORG_CREATED_TIME);
        assertEquals(result.getOrganizationHandle(), TEST_ORG_TENANT_DOMAIN);
        assertEquals(result.getParentOrganizationId(), TEST_PARENT_ORG_ID);
        assertEquals(result.getDepth(), TEST_ORG_DEPTH);

        // Check cache
        MinimalOrganizationCacheEntry cachedOrgEntry = minimalOrganizationCache.getValueFromCache(
                new OrganizationIdCacheKey(TEST_ORG_ID), TEST_ORG_TENANT_DOMAIN);
        assertNotNull(cachedOrgEntry);
        MinimalOrganization minimalOrganizationFromCache = cachedOrgEntry.getMinimalOrganization();
        assertEquals(minimalOrganizationFromCache.getId(), result.getId());
        assertEquals(minimalOrganizationFromCache.getName(), result.getName());
        assertEquals(minimalOrganizationFromCache.getStatus(), result.getStatus());
        assertEquals(minimalOrganizationFromCache.getCreated(), result.getCreated());
        assertEquals(minimalOrganizationFromCache.getOrganizationHandle(), result.getOrganizationHandle());
        assertEquals(minimalOrganizationFromCache.getParentOrganizationId(), result.getParentOrganizationId());
        assertEquals(minimalOrganizationFromCache.getDepth(), result.getDepth());
    }

    @Test
    public void testGetMinimalOrganizationFromCache() throws OrganizationManagementException {

        minimalOrganizationCache.addToCache(new OrganizationIdCacheKey(TEST_ORG_ID),
                new MinimalOrganizationCacheEntry(minimalOrganization), TEST_ORG_TENANT_DOMAIN);
        MinimalOrganization result = cacheBackedOrganizationManagementDAO.getMinimalOrganization(
                TEST_ORG_ID, TEST_ORG_TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getId(), TEST_ORG_ID);
        assertEquals(result.getName(), TEST_ORG_NAME);
        assertEquals(result.getStatus(), TEST_ORG_STATUS);
        assertEquals(result.getCreated(), TEST_ORG_CREATED_TIME);
        assertEquals(result.getOrganizationHandle(), TEST_ORG_TENANT_DOMAIN);
        assertEquals(result.getParentOrganizationId(), TEST_PARENT_ORG_ID);
        assertEquals(result.getDepth(), TEST_ORG_DEPTH);
        verify(organizationManagementDAO, never()).getBasicOrganizationDetailsByOrgIDs(any());
    }

    @Test
    public void testMinimalOrganizationCacheInvalidationWithUpdateOrg() throws OrganizationManagementException {

        minimalOrganizationCache.addToCache(new OrganizationIdCacheKey(TEST_ORG_ID),
                new MinimalOrganizationCacheEntry(minimalOrganization), TEST_ORG_TENANT_DOMAIN);
        doNothing().when(organizationManagementDAO).updateOrganization(any(), any());

        cacheBackedOrganizationManagementDAO.updateOrganization(TEST_ORG_ID, mock(Organization.class));

        // Check cache
        MinimalOrganizationCacheEntry cachedOrgEntry = minimalOrganizationCache.getValueFromCache(
                new OrganizationIdCacheKey(TEST_ORG_ID), TEST_ORG_TENANT_DOMAIN);
        assertNull(cachedOrgEntry);
    }

    @Test
    public void testMinimalOrganizationCacheInvalidationWithPatchOrg() throws OrganizationManagementException {

        minimalOrganizationCache.addToCache(new OrganizationIdCacheKey(TEST_ORG_ID),
                new MinimalOrganizationCacheEntry(minimalOrganization), TEST_ORG_TENANT_DOMAIN);
        doNothing().when(organizationManagementDAO).patchOrganization(any(), any(), any());

        cacheBackedOrganizationManagementDAO.patchOrganization(TEST_ORG_ID, mock(Instant.class),
                Collections.emptyList());

        // Check cache
        MinimalOrganizationCacheEntry cachedOrgEntry = minimalOrganizationCache.getValueFromCache(
                new OrganizationIdCacheKey(TEST_ORG_ID), TEST_ORG_TENANT_DOMAIN);
        assertNull(cachedOrgEntry);
    }

    @Test
    public void testMinimalOrganizationCacheInvalidationWithDeleteOrg() throws OrganizationManagementException {

        minimalOrganizationCache.addToCache(new OrganizationIdCacheKey(TEST_ORG_ID),
                new MinimalOrganizationCacheEntry(minimalOrganization), TEST_ORG_TENANT_DOMAIN);
        doNothing().when(organizationManagementDAO).deleteOrganization(any());

        cacheBackedOrganizationManagementDAO.deleteOrganization(TEST_ORG_ID);

        // Check cache
        MinimalOrganizationCacheEntry cachedOrgEntry = minimalOrganizationCache.getValueFromCache(
                new OrganizationIdCacheKey(TEST_ORG_ID), TEST_ORG_TENANT_DOMAIN);
        assertNull(cachedOrgEntry);
    }
 }
