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

package org.wso2.carbon.identity.organization.management.service.dao;

import org.wso2.carbon.identity.organization.management.service.exception.NotImplementedException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.filter.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationNode;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
     * Retrieve the list of organizations under a given organization ID.
     *
     * @param recursive               Determines whether records should be retrieved in a recursive manner.
     * @param limit                   The maximum number of records to be returned.
     * @param organizationId          The organization ID.
     * @param sortOrder               The sort order, ascending or descending.
     * @param expressionNodes         The list of filters excluding filtering by parentId.
     * @param parentIdExpressionNodes The list of filters related to parentId.
     * @return the list of organizations.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organizations.
     */
    default List<Organization> getOrganizationsList(boolean recursive, Integer limit, String organizationId,
                                                    String sortOrder, List<ExpressionNode> expressionNodes,
                                                    List<ExpressionNode> parentIdExpressionNodes)
            throws OrganizationManagementServerException {

        throw new NotImplementedException();
    }

    /**
     * Retrieve the IDs of the organizations under a given organization ID for particular user
     * who has permissions over them.
     *
     * @param recursive               Determines whether records should be retrieved in a recursive manner.
     * @param limit                   The maximum number of records to be returned.
     * @param organizationId          The organization ID.
     * @param sortOrder               The sort order, ascending or descending.
     * @param expressionNodes         The list of filters excluding filtering by parentId.
     * @param parentIdExpressionNodes The list of filters related to parentId.
     * @param applicationAudience     The application audience.
     * @return the list of organization IDs.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organizations.
     */
    List<BasicOrganization> getUserAuthorizedOrganizations(boolean recursive, Integer limit, String organizationId,
                                                           String sortOrder, List<ExpressionNode> expressionNodes,
                                                           List<ExpressionNode> parentIdExpressionNodes,
                                                           String applicationAudience)
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
     * Check if sibling organization exist with the given name under the given parent organization.
     *
     * @param organizationName Name of the organization to be checked.
     * @param parentOrgId      ID of the parent organization.
     * @return true if the sibling organization exist with the given name under the given parent organization.
     */
    boolean isSiblingOrganizationExistWithName(String organizationName, String parentOrgId)
            throws OrganizationManagementServerException;

    /**
     * Check if child organization exist with the given name under the given parent organization.
     *
     * @param organizationName Name of the organization to be checked.
     * @param rootOrgId        ID of the root organization where the sub-org tree is rooted.
     * @return true if the child organization exist with the given name under the given root organization.
     */
    boolean isChildOrganizationExistWithName(String organizationName, String rootOrgId)
            throws OrganizationManagementServerException;

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
     * Retrieve the list of child organizations of a given organization.
     *
     * @param organizationId The organization ID.
     * @param recursive      Determines whether records should be retrieved in a recursive manner.
     * @return the list of the child organizations.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the child
     *                                               organizations.
     */
    List<BasicOrganization> getChildOrganizations(String organizationId, boolean recursive)
            throws OrganizationManagementServerException;

    /**
     * Retrieve the list of child organizations of a given organization in a tree structure.
     *
     * @param organizationId The organization ID.
     * @param recursive      Determines whether records should be retrieved in a recursive manner.
     * @return the list of the child organizations in a tree structure.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the child
     *                                               organizations.
     */
    default List<OrganizationNode> getChildOrganizationGraph(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        throw new NotImplementedException("getChildOrganizationGraph(organizationId, recursive) is not " +
                "implemented in " + this.getClass().getName());
    }

    /**
     * Retrieve the list of child organization IDs of a given organization.
     *
     * @param organizationId The organization ID.
     * @param recursive      Determines whether records should be retrieved in a recursive manner.
     * @return the ID list of the child organizations.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the child
     *                                               organizations.
     */
    default List<String> getChildOrganizationIds(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        throw new NotImplementedException("getChildOrganizationIds(organizationId, recursive) is not " +
                "implemented in " + this.getClass().getName());
    }

    /**
     * Retrieve the list of child organization IDs of a given organization.
     *
     * @param organizationId The organization ID.
     * @return the ID list of the child organizations.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the child
     *                                               organizations.
     */
    List<String> getChildOrganizationIds(String organizationId)
            throws OrganizationManagementServerException;

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
     * Derive the ID of an organization based on the given tenant ID.
     *
     * @param tenantId The tenant ID.
     * @return Organization ID.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organization ID of
     *                                               a given tenant ID.
     */
    Optional<String> resolveOrganizationIdFromTenantId(String tenantId) throws OrganizationManagementServerException;

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

    /**
     * Retrieve the depth of the given organization.
     *
     * @param organizationId Organization id.
     * @return The depth of the organization from super organization.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the depth.
     */
    int getOrganizationDepthInHierarchy(String organizationId) throws OrganizationManagementServerException;

    /**
     * Retrieve the relative depth between two organizations which exist on the same organization branch.
     *
     * @param firstOrgId  The first organization id.
     * @param secondOrgId The second organization id.
     * @return The relative depth between the given organizations.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the relative depth.
     */
    int getRelativeDepthBetweenOrganizationsInSameBranch(String firstOrgId, String secondOrgId)
            throws OrganizationManagementServerException;

    /**
     * Returns whether the parent org is an ancestor of the current org.
     *
     * @param currentOrgId Current organization id.
     * @param parentOrgId  Parent organization id.
     * @return true if the parent org is an ancestor of the current org.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the relative depth.
     */
    boolean isAncestorOrg(String currentOrgId, String parentOrgId)
            throws OrganizationManagementServerException;

    /**
     * Get the ancestor organization id of the given organization and the depth.
     *
     * @param organizationId ID of the organization to get the ancestor.
     * @param depth          Depth of the ancestor organization to get.
     * @return Ancestor organization id.
     * @throws OrganizationManagementServerException The server exception thrown when getting the ancestor organization.
     */
    String getAnAncestorOrganizationIdInGivenDepth(String organizationId, int depth)
            throws OrganizationManagementServerException;

    /**
     * Add a root organization.
     *
     * @param rootOrganization The root organization to be added.
     * @throws OrganizationManagementServerException The server exception thrown when adding a root organization.
     */
    void addRootOrganization(Organization rootOrganization) throws OrganizationManagementServerException;

    /**
     * Retrieve the list of organizations' meta attributes.
     *
     * @param recursive               Determines whether records should be retrieved in a recursive manner.
     * @param limit                   The maximum number of records to be returned.
     * @param organizationId          The super organization ID.
     * @param sortOrder               The sort order, ascending or descending.
     * @param expressionNodes         The list of filters.
     * @return the list of organizations' meta attributes.
     * @throws OrganizationManagementServerException The server exception thrown when retrieving the organizations'
     * meta attributes.
     */
    default List<String> getOrganizationsMetaAttributes(boolean recursive, Integer limit, String organizationId,
                                                        String sortOrder, List<ExpressionNode> expressionNodes)
            throws OrganizationManagementServerException {

        throw new OrganizationManagementServerException("getOrganizationsMetaAttributes is not implemented in "
                + this.getClass().getName());
    }

    /**
     * Retrieve a map of organization IDs to their corresponding {@link BasicOrganization} details.
     *
     * @param orgIds The list of organization IDs to fetch details for.
     * @return A map where the key is the organization ID and
     * the value is the corresponding {@link BasicOrganization} object.
     * @throws OrganizationManagementException If an error occurs while retrieving organization details.
     */
    default Map<String, BasicOrganization> getBasicOrganizationDetailsByOrgIDs(List<String> orgIds)
            throws OrganizationManagementException {

        throw new OrganizationManagementServerException("getBasicOrganizationDetailsByOrgIDs is not implemented in "
                + this.getClass().getName());
    }
}
