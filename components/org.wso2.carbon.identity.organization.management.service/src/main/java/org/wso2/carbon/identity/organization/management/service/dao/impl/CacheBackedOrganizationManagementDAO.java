/*
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.organization.management.service.cache.MinimalOrganizationCacheByOrgId;
import org.wso2.carbon.identity.organization.management.service.cache.MinimalOrganizationCacheEntry;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationDetailsCacheByOrgId;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationDetailsCacheEntry;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationIdCacheByTenantDomain;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationIdCacheEntry;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationIdCacheKey;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationVersionCache;
import org.wso2.carbon.identity.organization.management.service.cache.OrganizationVersionCacheEntry;
import org.wso2.carbon.identity.organization.management.service.cache.TenantDomainCacheByOrgId;
import org.wso2.carbon.identity.organization.management.service.cache.TenantDomainCacheEntry;
import org.wso2.carbon.identity.organization.management.service.cache.TenantDomainCacheKey;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.filter.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.model.AncestorOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationNode;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.DEFAULT_ORGANIZATION_DEPTH_IN_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Cached DAO of organization management. All the DAO access should happen through this layer to ensure single point
 * of caching.
 */
public class CacheBackedOrganizationManagementDAO implements OrganizationManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedOrganizationManagementDAO.class);
    private final OrganizationManagementDAO organizationMgtDAO;

    public CacheBackedOrganizationManagementDAO(OrganizationManagementDAO organizationMgtDAO) {

        this.organizationMgtDAO = organizationMgtDAO;
    }

    @Override
    public void addOrganization(Organization organization) throws OrganizationManagementServerException {

        organizationMgtDAO.addOrganization(organization);
    }

    @Override
    public boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementServerException {

        return organizationMgtDAO.isOrganizationExistByName(organizationName);
    }

    @Override
    public boolean isOrganizationExistById(String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        if (tenantDomain == null) {
            return organizationMgtDAO.isOrganizationExistById(organizationId);
        }

        OrganizationDetailsCacheEntry cachedOrgDetails = getOrganizationDetailsFromCache(organizationId, tenantDomain);
        if (cachedOrgDetails != null) {
            return true;
        }
        return organizationMgtDAO.isOrganizationExistById(organizationId);
    }

    @Override
    public String getOrganizationIdByName(String organizationName) throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganizationIdByName(organizationName);
    }

    @Override
    public Optional<String> getOrganizationNameById(String organizationId)
            throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        if (tenantDomain == null) {
            return organizationMgtDAO.getOrganizationNameById(organizationId);
        }

        OrganizationDetailsCacheEntry cachedOrgDetails = getOrganizationDetailsFromCache(organizationId, tenantDomain);
        if (cachedOrgDetails != null && cachedOrgDetails.getOrgName() != null) {
            return Optional.ofNullable(cachedOrgDetails.getOrgName());
        }

        Optional<String> orgName = organizationMgtDAO.getOrganizationNameById(organizationId);
        if (cachedOrgDetails != null && orgName.isPresent()) {
            cachedOrgDetails.setOrgName(orgName.get());
        } else {
            addOrganizationNameToCache(organizationId, orgName.isPresent() ? orgName.get() : null, tenantDomain);
        }
        return orgName;
    }

    @Override
    public Organization getOrganization(String organizationId) throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganization(organizationId);
    }

    @Override
    public List<BasicOrganization> getOrganizations(boolean recursive, Integer limit, String organizationId,
                                                    String sortOrder, List<ExpressionNode> expressionNodes,
                                                    List<ExpressionNode> parentIdExpressionNodes)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganizations(recursive, limit, organizationId, sortOrder, expressionNodes,
                parentIdExpressionNodes);
    }

    @Override
    public List<Organization> getOrganizationsList(boolean recursive, Integer limit, String organizationId,
                                                   String sortOrder, List<ExpressionNode> expressionNodes,
                                                   List<ExpressionNode> parentIdExpressionNodes)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganizationsList(recursive, limit, organizationId, sortOrder, expressionNodes,
                parentIdExpressionNodes);
    }

    @Override
    public List<BasicOrganization> getUserAuthorizedOrganizations(boolean recursive, Integer limit,
                                                                  String organizationId, String sortOrder,
                                                                  List<ExpressionNode> expressionNodes,
                                                                  List<ExpressionNode> parentIdExpressionNodes,
                                                                  String applicationAudience)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getUserAuthorizedOrganizations(recursive, limit, organizationId, sortOrder,
                expressionNodes, parentIdExpressionNodes, applicationAudience);
    }

    @Override
    public void deleteOrganization(String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        organizationMgtDAO.deleteOrganization(organizationId);
        clearOrganizationCache(organizationId, tenantDomain);
        clearTenantDomainCache(organizationId);
    }

    @Override
    public boolean hasChildOrganizations(String organizationId) throws OrganizationManagementServerException {

        return organizationMgtDAO.hasChildOrganizations(organizationId);
    }

    @Override
    public boolean isSiblingOrganizationExistWithName(String organizationName, String parentOrgId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.isSiblingOrganizationExistWithName(organizationName, parentOrgId);
    }

    @Override
    public boolean isChildOrganizationExistWithName(String organizationName, String rootOrgId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.isChildOrganizationExistWithName(organizationName, rootOrgId);
    }

    @Override
    public boolean isOrganizationExistWithName(String organizationName) throws OrganizationManagementServerException {

        return organizationMgtDAO.isOrganizationExistWithName(organizationName);
    }

    @Override
    public void patchOrganization(String organizationId, Instant lastModifiedInstant,
                                  List<PatchOperation> patchOperations) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        organizationMgtDAO.patchOrganization(organizationId, lastModifiedInstant, patchOperations);
        clearOrganizationCache(organizationId, tenantDomain);
    }

    @Override
    public void updateOrganization(String organizationId, Organization organization)
            throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        organizationMgtDAO.updateOrganization(organizationId, organization);
        clearOrganizationCache(organizationId, tenantDomain);
    }

    @Override
    public boolean isAttributeExistByKey(String organizationId, String attributeKey)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.isAttributeExistByKey(organizationId, attributeKey);
    }

    @Override
    public List<BasicOrganization> getChildOrganizations(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getChildOrganizations(organizationId, recursive);
    }

    @Override
    public List<OrganizationNode> getChildOrganizationGraph(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getChildOrganizationGraph(organizationId, recursive);
    }

    @Override
    public List<String> getChildOrganizationIds(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getChildOrganizationIds(organizationId, recursive);
    }

    @Override
    public List<String> getChildOrganizationIds(String organizationId) throws OrganizationManagementServerException {

        return organizationMgtDAO.getChildOrganizationIds(organizationId);
    }

    @Override
    public boolean hasActiveChildOrganizations(String organizationId) throws OrganizationManagementServerException {

        return organizationMgtDAO.hasActiveChildOrganizations(organizationId);
    }

    @Override
    public boolean isParentOrganizationDisabled(String organizationId) throws OrganizationManagementServerException {

        return organizationMgtDAO.isParentOrganizationDisabled(organizationId);
    }

    @Override
    public String getOrganizationStatus(String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        if (tenantDomain == null) {
            return organizationMgtDAO.getOrganizationStatus(organizationId);
        }

        OrganizationDetailsCacheEntry cachedOrgDetails = getOrganizationDetailsFromCache(organizationId, tenantDomain);
        if (cachedOrgDetails != null && cachedOrgDetails.getStatus() != null) {
            return cachedOrgDetails.getStatus();
        }

        String status = organizationMgtDAO.getOrganizationStatus(organizationId);
        if (cachedOrgDetails != null) {
            cachedOrgDetails.setStatus(status);
        } else {
            addOrganizationStatusToCache(organizationId, status, tenantDomain);
        }
        return status;
    }

    @Override
    public String getOrganizationType(String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        if (tenantDomain == null) {
            return organizationMgtDAO.getOrganizationType(organizationId);
        }

        OrganizationDetailsCacheEntry cachedOrgDetails = getOrganizationDetailsFromCache(organizationId, tenantDomain);
        if (cachedOrgDetails != null && cachedOrgDetails.getType() != null) {
            return cachedOrgDetails.getType();
        }

        String type = organizationMgtDAO.getOrganizationType(organizationId);
        if (cachedOrgDetails != null) {
            cachedOrgDetails.setStatus(type);
        } else {
            addOrganizationTypeToCache(organizationId, type, tenantDomain);
        }
        return type;
    }

    @Override
    public List<String> getOrganizationPermissions(String organizationId, String userId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganizationPermissions(organizationId, userId);
    }

    @Override
    public String getAssociatedTenantUUIDForOrganization(String organizationId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getAssociatedTenantUUIDForOrganization(organizationId);
    }

    @Override
    public String resolveTenantDomain(String organizationId) throws OrganizationManagementServerException {

        if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
            // Super tenant domain will be returned.
            return SUPER_TENANT_DOMAIN_NAME;
        }
        TenantDomainCacheEntry cachedTenantDomain = getTenantDomainFromCache(organizationId);
        if (cachedTenantDomain != null) {
            return cachedTenantDomain.getTenantDomain();
        }
        String tenantDomain = organizationMgtDAO.resolveTenantDomain(organizationId);
        addTenantDomainToCache(organizationId, tenantDomain);
        return tenantDomain;
    }

    @Override
    public boolean isChildOfParent(String organizationId, String parentId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.isChildOfParent(organizationId, parentId);
    }

    @Override
    public boolean isImmediateChildOfParent(String organizationId, String parentId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.isImmediateChildOfParent(organizationId, parentId);
    }

    @Override
    public Optional<String> resolveOrganizationId(String tenantDomain) throws OrganizationManagementServerException {

        if (StringUtils.equals(SUPER_TENANT_DOMAIN_NAME, tenantDomain)) {
            return Optional.of(SUPER_ORG_ID);
        }
        OrganizationIdCacheEntry cachedOrganizationId = getOrganizationIdFromCache(tenantDomain);
        if (cachedOrganizationId != null) {
            return Optional.of(cachedOrganizationId.getOrganizationId());
        }
        Optional<String> organizationId = organizationMgtDAO.resolveOrganizationId(tenantDomain);
        organizationId.ifPresent(s -> addOrganizationIdToCache(tenantDomain, s));
        return organizationId;
    }

    @Override
    public Optional<String> resolveOrganizationIdFromTenantId(String tenantId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.resolveOrganizationIdFromTenantId(tenantId);
    }

    @Override
    public List<String> getAncestorOrganizationIds(String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        if (tenantDomain == null) {
            return organizationMgtDAO.getAncestorOrganizationIds(organizationId);
        }

        OrganizationDetailsCacheEntry cachedOrgDetails = getOrganizationDetailsFromCache(organizationId, tenantDomain);
        if (cachedOrgDetails != null && cachedOrgDetails.getAncestorOrganizationIds() != null) {
            return cachedOrgDetails.getAncestorOrganizationIds();
        }

        List<String> ancestorOrganizationIds = organizationMgtDAO.getAncestorOrganizationIds(organizationId);
        if (cachedOrgDetails != null) {
            cachedOrgDetails.setAncestorOrganizationIds(ancestorOrganizationIds);
        } else {
            addAncestorOrganizationIdsToCache(organizationId, ancestorOrganizationIds, tenantDomain);
        }
        return ancestorOrganizationIds;
    }

    @Override
    public List<Organization> getOrganizationsByName(String organizationName)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganizationsByName(organizationName);
    }

    @Override
    public int getOrganizationDepthInHierarchy(String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = resolveTenantDomain(organizationId);
        if (tenantDomain == null) {
            return organizationMgtDAO.getOrganizationDepthInHierarchy(organizationId);
        }

        OrganizationDetailsCacheEntry cachedOrgDetails = getOrganizationDetailsFromCache(organizationId, tenantDomain);
        if (cachedOrgDetails != null &&
                cachedOrgDetails.getOrganizationDepthInHierarchy() != DEFAULT_ORGANIZATION_DEPTH_IN_HIERARCHY) {
            return cachedOrgDetails.getOrganizationDepthInHierarchy();
        }

        int organizationDepthInHierarchy = organizationMgtDAO.getOrganizationDepthInHierarchy(organizationId);
        if (cachedOrgDetails != null) {
            cachedOrgDetails.setOrganizationDepthInHierarchy(organizationDepthInHierarchy);
        } else {
            addOrganizationDepthInHierarchyToCache(organizationId, organizationDepthInHierarchy, tenantDomain);
        }
        return organizationDepthInHierarchy;
    }

    @Override
    public int getRelativeDepthBetweenOrganizationsInSameBranch(String firstOrgId, String secondOrgId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getRelativeDepthBetweenOrganizationsInSameBranch(firstOrgId, secondOrgId);
    }

    @Override
    public String getAnAncestorOrganizationIdInGivenDepth(String organizationId, int depth)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getAnAncestorOrganizationIdInGivenDepth(organizationId, depth);
    }

    @Override
    public void addRootOrganization(Organization organization) throws OrganizationManagementServerException {

        organizationMgtDAO.addRootOrganization(organization);
    }

    @Override
    public List<String> getOrganizationsMetaAttributes(boolean recursive, Integer limit, String organizationId,
                                                       String sortOrder, List<ExpressionNode> expressionNodes)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getOrganizationsMetaAttributes(recursive, limit, organizationId, sortOrder,
                expressionNodes);
    }

    @Override
    public Map<String, BasicOrganization> getBasicOrganizationDetailsByOrgIDs(List<String> orgIds)
            throws OrganizationManagementException {

        return organizationMgtDAO.getBasicOrganizationDetailsByOrgIDs(orgIds);
    }

    @Override
    public List<AncestorOrganizationDO> getAncestorOrganizations(String organizationId)
            throws OrganizationManagementServerException {

        return organizationMgtDAO.getAncestorOrganizations(organizationId);
    }

    @Override
    public MinimalOrganization getMinimalOrganization(String organizationId, String associatedTenantDomain)
            throws OrganizationManagementException {

        String tenantDomain = associatedTenantDomain;
        if (tenantDomain == null) {
            tenantDomain = resolveTenantDomain(organizationId);
        }

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        MinimalOrganizationCacheEntry entry = MinimalOrganizationCacheByOrgId.getInstance()
                .getValueFromCache(cacheKey, tenantDomain);

        if (entry != null) {
            LOG.debug("Minimal Organization Cache entry found for organization id: " + organizationId);
            return entry.getMinimalOrganization();
        }
        LOG.debug("Minimal Organization Cache entry not found for organization id: " + organizationId +
                ". Fetching entry from DB.");

        MinimalOrganization minimalOrganization = organizationMgtDAO.getMinimalOrganization(organizationId,
                tenantDomain);

        if (minimalOrganization != null) {
            LOG.debug("Minimal Organization entry fetched from DB for organization id: " + organizationId +
                    ". Updating cache.");
            // Resolving tenantDomain from the fetched object to prevent organization getting cached against
            // wrong tenant domain passed to the method.
            tenantDomain = minimalOrganization.getOrganizationHandle();
            MinimalOrganizationCacheByOrgId.getInstance()
                    .addToCache(cacheKey, new MinimalOrganizationCacheEntry(minimalOrganization), tenantDomain);
        } else {
            LOG.debug("Minimal Organization Entry for organization id: " + organizationId +
                    " not found in cache or DB.");
        }

        return minimalOrganization;
    }

    @Override
    public Optional<String> getOrganizationVersion(String organizationId, String tenantDomain)
            throws OrganizationManagementException {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        OrganizationVersionCacheEntry cacheEntry = OrganizationVersionCache.getInstance()
                .getValueFromCache(cacheKey, tenantDomain);
        if (cacheEntry != null) {
            return Optional.of(cacheEntry.getVersion());
        }

        Optional<String> version = organizationMgtDAO.getOrganizationVersion(organizationId, tenantDomain);
        if (!version.isPresent()) {
            // Can be null if the organization does not exist. Error is handled in the service layer.
            return version;
        }

        cacheEntry = new OrganizationVersionCacheEntry(version.get());
        OrganizationVersionCache.getInstance().addToCache(cacheKey, cacheEntry, tenantDomain);
        return version;
    }

    private TenantDomainCacheEntry getTenantDomainFromCache(String organizationId) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        TenantDomainCacheByOrgId cache = TenantDomainCacheByOrgId.getInstance();
        return cache.getValueFromCache(cacheKey, SUPER_TENANT_DOMAIN_NAME);
    }

    private OrganizationIdCacheEntry getOrganizationIdFromCache(String tenantDomain) {

        TenantDomainCacheKey cacheKey = new TenantDomainCacheKey(tenantDomain);
        OrganizationIdCacheByTenantDomain cache = OrganizationIdCacheByTenantDomain.getInstance();
        return cache.getValueFromCache(cacheKey, SUPER_TENANT_DOMAIN_NAME);
    }

    private void addTenantDomainToCache(String organizationId, String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain) || StringUtils.isBlank(organizationId)) {
            return;
        }
        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        TenantDomainCacheEntry cacheEntry = new TenantDomainCacheEntry(tenantDomain);
        TenantDomainCacheByOrgId.getInstance()
                .addToCache(cacheKey, cacheEntry, SUPER_TENANT_DOMAIN_NAME);
    }

    private void addOrganizationIdToCache(String tenantDomain, String organizationId) {

        if (StringUtils.isBlank(tenantDomain) || StringUtils.isBlank(organizationId)) {
            return;
        }
        TenantDomainCacheKey cacheKey = new TenantDomainCacheKey(tenantDomain);
        OrganizationIdCacheEntry cacheEntry = new OrganizationIdCacheEntry(organizationId);
        OrganizationIdCacheByTenantDomain.getInstance()
                .addToCache(cacheKey, cacheEntry, SUPER_TENANT_DOMAIN_NAME);
    }

    private OrganizationDetailsCacheEntry getOrganizationDetailsFromCache(String organizationId, String tenantDomain) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        OrganizationDetailsCacheByOrgId cache = OrganizationDetailsCacheByOrgId.getInstance();
        return cache.getValueFromCache(cacheKey, tenantDomain);
    }

    private void addOrganizationNameToCache(String organizationId, String organizationName, String tenantDomain) {

        OrganizationDetailsCacheEntry cacheEntry = new OrganizationDetailsCacheEntry.Builder()
                .setOrgName(organizationName).build();
        addOrganizationDetailsToCache(organizationId, cacheEntry, tenantDomain);
    }

    private void addOrganizationStatusToCache(String organizationId, String status, String tenantDomain) {

        OrganizationDetailsCacheEntry cacheEntry = new OrganizationDetailsCacheEntry.Builder()
                .setStatus(status).build();
        addOrganizationDetailsToCache(organizationId, cacheEntry, tenantDomain);
    }

    private void addOrganizationTypeToCache(String organizationId, String type, String tenantDomain) {

        OrganizationDetailsCacheEntry cacheEntry = new OrganizationDetailsCacheEntry.Builder()
                .setType(type).build();
        addOrganizationDetailsToCache(organizationId, cacheEntry, tenantDomain);
    }

    private void addOrganizationDepthInHierarchyToCache(String organizationId, int organizationDepthInHierarchy,
                                                        String tenantDomain) {

        OrganizationDetailsCacheEntry cacheEntry = new OrganizationDetailsCacheEntry.Builder()
                .setOrganizationDepthInHierarchy(organizationDepthInHierarchy).build();
        addOrganizationDetailsToCache(organizationId, cacheEntry, tenantDomain);
    }

    private void addAncestorOrganizationIdsToCache(String organizationId, List<String> ancestorOrganizationIds,
                                                   String tenantDomain) {

        OrganizationDetailsCacheEntry cacheEntry = new OrganizationDetailsCacheEntry.Builder()
                .setAncestorOrganizationIds(ancestorOrganizationIds).build();
        addOrganizationDetailsToCache(organizationId, cacheEntry, tenantDomain);
    }

    private void addOrganizationDetailsToCache(String organizationId,
                                               OrganizationDetailsCacheEntry organizationDetailsCacheEntry,
                                               String tenantDomain) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        OrganizationDetailsCacheByOrgId.getInstance().addToCache(cacheKey, organizationDetailsCacheEntry, tenantDomain);
    }

    private void clearTenantDomainCache(String organizationId) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        TenantDomainCacheByOrgId.getInstance().clearCacheEntry(cacheKey, SUPER_TENANT_DOMAIN_NAME);
    }

    private void clearOrganizationCache(String organizationId, String tenantDomain) {

        if (tenantDomain == null) {
            return;
        }

        clearOrganizationDetailsCache(organizationId, tenantDomain);
        clearMinimalOrganizationCache(organizationId, tenantDomain);
        clearOrganizationVersionCache(organizationId, tenantDomain);
    }

    private void clearOrganizationDetailsCache(String organizationId, String tenantDomain) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        OrganizationDetailsCacheByOrgId.getInstance().clearCacheEntry(cacheKey, tenantDomain);
    }

    private void clearMinimalOrganizationCache(String organizationId, String tenantDomain) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        MinimalOrganizationCacheByOrgId.getInstance().clearCacheEntry(cacheKey, tenantDomain);
    }

    private void clearOrganizationVersionCache(String organizationId, String tenantDomain) {

        OrganizationIdCacheKey cacheKey = new OrganizationIdCacheKey(organizationId);
        OrganizationVersionCache.getInstance().clearCacheEntry(cacheKey, tenantDomain);
    }
}
