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

import org.wso2.carbon.identity.organization.management.service.exception.NotImplementedException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;

import java.util.List;

/**
 * Organization manager service interface.
 */
public interface OrganizationManager {

    /**
     * Create new {@link Organization} in the database.
     *
     * @param organization The organization to be created.
     * @return The newly created organization.
     * @throws OrganizationManagementException The exception thrown when creating an organization.
     */
    Organization addOrganization(Organization organization) throws OrganizationManagementException;

    /**
     * Check if the {@link Organization} exists by name.
     *
     * @param organizationName The organization name.
     * @return true if the organization exists.
     * @throws OrganizationManagementException The exception thrown when checking the organization existence.
     */
    boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementException;

    /**
     * Check if the {@link Organization} exists by name in hierarchy.
     *
     * @param organizationName The organization name.
     * @return true if the organization exists.
     */
    boolean isOrganizationExistByNameInGivenHierarchy(String organizationName);

    /**
     * Check if the {@link Organization} exists by ID.
     *
     * @param organizationId The organization ID.
     * @return true if the organization exists.
     * @throws OrganizationManagementException The exception thrown when checking the organization existence.
     */
    boolean isOrganizationExistById(String organizationId) throws OrganizationManagementException;

    /**
     * Retrieve organization ID if the given organization name.
     *
     * @param organizationName The organization name.
     * @return the organization ID.
     * @throws OrganizationManagementException The exception thrown when retrieving the ID an organization.
     */
    String getOrganizationIdByName(String organizationName) throws OrganizationManagementException;

    /**
     * Retrieve the organization name for the given organization id.
     *
     * @param organizationId The organization id.
     * @return the organization Name.
     * @throws OrganizationManagementException The exception thrown when retrieving the name of an organization.
     */
    String getOrganizationNameById(String organizationId) throws OrganizationManagementException;

    /**
     * Retrieve {@link Organization} by ID.
     *
     * @param organizationId     The organization ID.
     * @param showChildren       Whether the child organizations should be retrieved.
     * @param includePermissions Whether the permissions attached for organization should be retrieved.
     * @return the organization object.
     * @throws OrganizationManagementException The exception thrown when retrieving an organization.
     */
    Organization getOrganization(String organizationId, boolean showChildren, boolean includePermissions)
            throws OrganizationManagementException;

    /**
     * Returns the list of child organizations for a given organization.
     *
     * @param organizationId The organization ID.
     * @param recursive      Determines whether records should be retrieved in a recursive manner.
     * @return the list of Child organizations ({@link BasicOrganization}).
     * @throws OrganizationManagementException exception is thrown when listing organizations.
     */
    List<BasicOrganization> getChildOrganizations(String organizationId, boolean recursive)
            throws OrganizationManagementException;

    /**
     * Returns the unique identifiers of the child organizations for a given organization.
     *
     * @param organizationId The organization ID.
     * @return the list of Child organization IDs.
     * @throws OrganizationManagementException exception is thrown when listing organizations.
     */
    List<String> getChildOrganizationsIds(String organizationId)
            throws OrganizationManagementException;

    /**
     * List or search organizations.
     *
     * @param limit     The maximum number of records to be returned.
     * @param after     The pointer to next page.
     * @param before    The pointer to previous page.
     * @param sortOrder The sort order, ascending or descending.
     * @param filter    The filter string.
     * @param recursive Determines whether records should be retrieved in a recursive manner.
     * @return the list of {@link BasicOrganization}s.
     * @throws OrganizationManagementException The exception thrown when listing organizations.
     * @deprecated Use {@link #getOrganizationsList(Integer, String, String, String, String, boolean)} instead.
     */
    @Deprecated
    List<BasicOrganization> getOrganizations(Integer limit, String after, String before, String sortOrder,
                                             String filter, boolean recursive) throws OrganizationManagementException;

    /**
     * List or search organizations.
     *
     * @param limit     The maximum number of records to be returned.
     * @param after     The pointer to next page.
     * @param before    The pointer to previous page.
     * @param sortOrder The sort order, ascending or descending.
     * @param filter    The filter string.
     * @param recursive Determines whether records should be retrieved in a recursive manner.
     * @return the list of {@link Organization}s.
     * @throws OrganizationManagementException The exception thrown when listing organizations.
     */
    default List<Organization> getOrganizationsList(Integer limit, String after, String before, String sortOrder,
                                             String filter, boolean recursive) throws OrganizationManagementException {

        throw new NotImplementedException();
    }

    /**
     * List or search organizations authorized for the user.
     *
     * @param limit     The maximum number of records to be returned.
     * @param after     The pointer to next page.
     * @param before    The pointer to previous page.
     * @param sortOrder The sort order, ascending or descending.
     * @param filter    The filter string.
     * @param recursive Determines whether records should be retrieved in a recursive manner.
     * @return the list of {@link BasicOrganization}s.
     * @throws OrganizationManagementException The exception thrown when listing organizations.
     */
    List<BasicOrganization> getUserAuthorizedOrganizations(Integer limit, String after, String before, String sortOrder,
                                                           String filter, boolean recursive)
            throws OrganizationManagementException;

    /**
     * List or search organizations authorized for the user with a role for given application audience.
     *
     * @param limit               The maximum number of records to be returned.
     * @param after               The pointer to next page.
     * @param before              The pointer to previous page.
     * @param sortOrder           The sort order, ascending or descending.
     * @param filter              The filter string.
     * @param recursive           Determines whether records should be retrieved in a recursive manner.
     * @param applicationAudience The application audience.
     * @return the list of {@link BasicOrganization}s.
     * @throws OrganizationManagementException The exception thrown when listing organizations.
     */
    List<BasicOrganization> getUserAuthorizedOrganizations(Integer limit, String after, String before, String sortOrder,
                                                           String filter, boolean recursive, String applicationAudience)
            throws OrganizationManagementException;

    /**
     * Delete the organization identified by the provided ID.
     *
     * @param organizationId The organization ID.
     * @throws OrganizationManagementException The exception thrown when deleting an organization.
     */
    void deleteOrganization(String organizationId) throws OrganizationManagementException;

    /**
     * Patch organization and its attributes.
     *
     * @param organizationId  The organization ID.
     * @param patchOperations The list of patch operations.
     * @return the patched organization.
     * @throws OrganizationManagementException The exception thrown when patching an organization.
     */
    Organization patchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException;

    /**
     * Update organization and its attributes.
     *
     * @param organizationId          The organization ID.
     * @param currentOrganizationName The organization name.
     * @param organization            The organization with values to be updated.
     * @return the updated organization.
     * @throws OrganizationManagementException The exception thrown when updating an organization.
     */
    Organization updateOrganization(String organizationId, String currentOrganizationName, Organization organization)
            throws OrganizationManagementException;

    /**
     * Derive the tenant domain of an organization based on the given organization id.
     *
     * @param organizationId The organization ID.
     * @return tenant domain.
     * @throws OrganizationManagementException The exception thrown when retrieving the tenant domain of an
     *                                         organization.
     */
    String resolveTenantDomain(String organizationId) throws OrganizationManagementException;

    /**
     * Derive the tenant id of an organization based on the given organization id.
     *
     * @param organizationId The organization ID.
     * @return Tenant id.
     * @throws OrganizationManagementException The exception thrown when retrieving the tenant id of an
     *                                         organization.
     */
    String resolveTenantId(String organizationId) throws OrganizationManagementException;

    /**
     * Derive the organization id of the given tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return organization id.
     * @throws OrganizationManagementException The exception thrown when retrieving the organization id of a tenant.
     */
    String resolveOrganizationId(String tenantDomain) throws OrganizationManagementException;

    /**
     * Derive the organization id of the given tenant id.
     *
     * @param tenantId The tenant id.
     * @return Organization id.
     * @throws OrganizationManagementException The exception thrown when retrieving the organization id of a tenant id.
     */
    String resolveOrganizationIdFromTenantId(String tenantId) throws OrganizationManagementException;

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
     * @throws OrganizationManagementException The server exception thrown when retrieving the list of
     *                                         organizations by name.
     */
    List<Organization> getOrganizationsByName(String organizationName) throws OrganizationManagementException;

    /**
     * Return the depth of the given organization in organization hierarchy.
     * Return -1 if the organization is not found in the tree.
     *
     * @param organizationId Checking organization id.
     * @return Depth from super organization.
     * @throws OrganizationManagementServerException Error occurred while checking depth of the organization.
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
     * Get the parent organization id of the given organization.
     *
     * @param organizationId ID of the organization which needs the parent organization id.
     * @return Organization id of the parent organization.
     * @throws OrganizationManagementException Error occurred while retrieving the parent organization id.
     */
    String getParentOrganizationId(String organizationId) throws OrganizationManagementException;

    /**
     * Check if the provided organization ID is a primary organization.
     *
     * @param organizationId The organization ID.
     * @return True if the organization is the primary organization; otherwise, false.
     */
    boolean isPrimaryOrganization(String organizationId) throws OrganizationManagementServerException;

    /**
     * Fetch the primary organization of the given organization.
     *
     * @param organizationId The ID of the organization whose primary organization is to be fetched.
     * @return The ID of the primary organization.
     * @throws OrganizationManagementServerException The server exception thrown when fetching the primary organization.
     */
    String getPrimaryOrganizationId(String organizationId) throws OrganizationManagementServerException;

    /**
     * Add Root organization which can initiate an organization tree.
     *
     * @param tenantId     The tenant ID corresponds to the root organization.
     * @param organization The organization to be created.
     * @return The newly created organization.
     * @throws OrganizationManagementException The exception thrown when creating an organization.
     */
    default Organization addRootOrganization(int tenantId, Organization organization)
            throws OrganizationManagementException {

        throw new NotImplementedException();
    }

    /**
     * List of organizations' meta attributes.
     *
     * @param limit     The maximum number of records to be returned.
     * @param after     The pointer to next page.
     * @param before    The pointer to previous page.
     * @param sortOrder The sort order, ascending or descending.
     * @param filter    The filter string.
     * @param recursive Determines whether records should be retrieved in a recursive manner.
     * @return The list of organizations' meta attributes.
     * @throws OrganizationManagementException The exception thrown when listing organizations.
     */
    default List<String> getOrganizationsMetaAttributes(Integer limit, String after, String before, String sortOrder,
                                                        String filter, boolean recursive)
            throws OrganizationManagementException {

        throw new NotImplementedException();
    }
}
