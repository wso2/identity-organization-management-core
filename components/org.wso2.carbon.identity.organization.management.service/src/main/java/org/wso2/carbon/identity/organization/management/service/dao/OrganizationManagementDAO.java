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

package org.wso2.carbon.identity.organization.management.service.dao;

import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * This interface performs CRUD operations for {@link Organization}
 */
public interface OrganizationManagementDAO {

    /**
     * Create new {@link Organization} in the database.
     *
     * @param organization The organization to be created.
     * @throws OrganizationManagementServerException The server exception thrown when creating an organization.
     */
    void addOrganization(Organization organization) throws OrganizationManagementServerException;

    /**
     * Check if the {@link Organization} exists by name.
     *
     * @param organizationName The organization name.
     * @return true if the organization exists.
     * @throws OrganizationManagementServerException The server exception thrown when checking for the organization
     *                                               existence.
     */
    boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementServerException;

    /**
     * Check if the {@link Organization} exists by organization ID.
     *
     * @param organizationId The organization ID.
     * @return true if the organization exists.
     * @throws OrganizationManagementServerException The server exception thrown when checking for the organization
     *                                               existence.
     */
    boolean isOrganizationExistById(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve organization ID if the given organization name exists.
     *
     * @param organizationName The organization name.
     * @return the organization ID.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organization ID.
     */
    String getOrganizationIdByName(String organizationName) throws OrganizationManagementServerException;

    /**
     * Retrieve organization name for the given organization id if organization exists.
     *
     * @param organizationId The organization id.
     * @return the organization Name.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organization name.
     */
    Optional<String> getOrganizationNameById(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve {@link Organization} by ID.
     *
     * @param organizationId The organization ID.
     * @return the organization object.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organization.
     */
    Organization getOrganization(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve the IDs of the organizations under a given organization ID.
     *
     * @param recursive               Determines whether records should be retrieved in a recursive manner.
     * @param limit                   The maximum number of records to be returned.
     * @param organizationId          The organization ID.
     * @param sortOrder               The sort order, ascending or descending.
     * @param expressionNodes         The list of filters excluding filtering by parentId.
     * @param parentIdExpressionNodes The list of filters related to parentId.
     * @return the list of organization IDs.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organizations.
     */
    List<BasicOrganization> getOrganizations(boolean recursive, Integer limit, String organizationId, String sortOrder,
                                             List<ExpressionNode> expressionNodes,
                                             List<ExpressionNode> parentIdExpressionNodes)
            throws OrganizationManagementServerException;

    /**
     * Delete {@link Organization} by ID.
     *
     * @param organizationId The organization ID.
     * @throws OrganizationManagementServerException The server exception thrown when deleting the organization.
     */
    void deleteOrganization(String organizationId) throws OrganizationManagementServerException;

    /**
     * Check if an organization has child organizations.
     *
     * @param organizationId The organization ID.
     * @return true if the organization has child organizations.
     * @throws OrganizationManagementServerException The server exception thrown when checking if an organization has
     *                                               child organizations.
     */
    boolean hasChildOrganizations(String organizationId) throws OrganizationManagementServerException;

    /**
     * Add, remove or replace organization fields and attributes.
     *
     * @param organizationId      The organization ID.
     * @param lastModifiedInstant The last modified time.
     * @param patchOperations     The list of patch operations.
     * @throws OrganizationManagementServerException The server exception thrown when patching an organization.
     */
    void patchOrganization(String organizationId, Instant lastModifiedInstant,
                           List<PatchOperation> patchOperations) throws OrganizationManagementServerException;

    /**
     * Update {@link Organization} by ID.
     *
     * @param organizationId The organization ID.
     * @param organization   The organization object.
     * @throws OrganizationManagementServerException The server exception thrown when updating an organization.
     */
    void updateOrganization(String organizationId, Organization organization) throws
            OrganizationManagementServerException;

    /**
     * Check if the organization has the given attribute.
     *
     * @param organizationId The organization ID.
     * @param attributeKey   The attribute key of the organization.
     * @return true if the organization attribute exists.
     * @throws OrganizationManagementServerException The server exception thrown when checking if an organization
     *                                               attribute exists.
     */
    boolean isAttributeExistByKey(String organizationId, String attributeKey)
            throws OrganizationManagementServerException;

    /**
     * Retrieve the list of child organization IDs of a given organization.
     *
     * @param organizationId The organization ID.
     * @return the ID list of the child organizations.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the child
     *                                               organizations.
     */
    List<String> getChildOrganizationIds(String organizationId) throws OrganizationManagementServerException;

    /**
     * Check if the organization has any child organizations with the status as 'ACTIVE'.
     *
     * @param organizationId The organization ID.
     * @return true if 'ACTIVE' child organizations exist.
     * @throws OrganizationManagementServerException The server exception thrown when checking if the organization has
     *                                               any child organizations with the status as 'ACTIVE'.
     */
    boolean hasActiveChildOrganizations(String organizationId) throws OrganizationManagementServerException;

    /**
     * Check if the parent organization of an organization is having the status as 'DISABLED'.
     *
     * @param organizationId The organization ID.
     * @return true if the parent organization status is 'DISABLED'.
     * @throws OrganizationManagementServerException The server exception thrown when checking if the parent
     *                                               organization status is 'DISABLED'.
     */
    boolean isParentOrganizationDisabled(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve the status of the organization.
     *
     * @param organizationId The organization ID.
     * @return the status of the organization.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the status of the
     *                                               organization.
     */
    String getOrganizationStatus(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve the type of the organization.
     *
     * @param organizationId The organization ID.
     * @return the organization type.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organization type.
     */
    String getOrganizationType(String organizationId) throws OrganizationManagementServerException;

    /**
     * Get list of permissions assigned to user for the organization.
     *
     * @param organizationId The organization ID.
     * @param userId         Unique identifier of the user.
     * @return list of resource ids of permissions
     * @throws OrganizationManagementServerException The server exception thrown when retrieving user's permissions
     *                                               for an organization.
     */
    List<String> getOrganizationPermissions(String organizationId, String userId)
            throws OrganizationManagementServerException;

    /**
     * Retrieve the tenant UUID if the organization is a tenant type organization.
     *
     * @param organizationId Organization ID.
     * @return Associated tenant UUID.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the associated tenant.
     */
    String getAssociatedTenantUUIDForOrganization(String organizationId) throws OrganizationManagementServerException;

    /**
     * Derive the tenant domain of an organization based on the given organization id.
     *
     * @param organizationId The organization ID.
     * @return associated tenant domain.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the tenant domain of
     *                                               an organization.
     */
    String resolveTenantDomain(String organizationId) throws OrganizationManagementServerException;

    /**
     * Check whether an organization is a child organization of the given parent.
     *
     * @param organizationId The organization ID.
     * @param parentId       The parent organization ID.
     * @return whether an organization is a child organization of the given parent.
     * @throws OrganizationManagementServerException The server exception thrown when checking if an organization is
     *                                               a child organization of the given parent.
     */
    boolean isChildOfParent(String organizationId, String parentId) throws
            OrganizationManagementServerException;

    /**
     * Check whether an organization is an immediate child organization of the given parent.
     *
     * @param organizationId The organization ID.
     * @param parentId       The parent organization ID.
     * @return whether an organization is an immediate child organization of the given parent.
     * @throws OrganizationManagementServerException The server exception thrown when checking if an organization is
     *                                               an immediate child organization of the given parent.
     */
    boolean isImmediateChildOfParent(String organizationId, String parentId) throws
            OrganizationManagementServerException;

    /**
     * Derive the ID of an organization based on the given tenant domain.
     *
     * @param tenantDomain The tenant domain.
     * @return organization ID.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the tenant domain of
     *                                               an organization.
     */
    Optional<String> resolveOrganizationId(String tenantDomain) throws OrganizationManagementServerException;

    /**
     * Get ancestor organization ids (including itself) of a given organization.
     *
     * @param organizationId Organization id.
     * @return List of ancestor organization ids including itself.
     */
    List<String> getAncestorOrganizationIds(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve list of organizations by the organization name.
     *
     * @param organizationName The name of the organizations.
     * @return List of {@link Organization}
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the list of
     *                                               organization by name.
     */
    List<Organization> getOrganizationsByName(String organizationName) throws OrganizationManagementServerException;
}
