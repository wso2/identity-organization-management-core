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

package org.wso2.carbon.identity.organization.management.service;

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
     * List or search organizations.
     *
     * @param limit     The maximum number of records to be returned.
     * @param after     The pointer to next page.
     * @param before    The pointer to previous page.
     * @param sortOrder The sort order, ascending or descending.
     * @param filter    The filter string.
     * @param recursive Determines whether records should be retrieved in a recursive manner.
     * @return the list of organization IDs.
     * @throws OrganizationManagementException The exception thrown when listing organizations.
     */
    List<BasicOrganization> getOrganizations(Integer limit, String after, String before, String sortOrder,
                                             String filter, boolean recursive) throws OrganizationManagementException;

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
     * Derive the organization id of the given tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return organization id.
     * @throws OrganizationManagementException The exception thrown when retrieving the organization id of a tenant.
     */
    String resolveOrganizationId(String tenantDomain) throws OrganizationManagementException;

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

}
