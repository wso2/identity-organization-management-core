/*
 * Copyright (c) 2022-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.service.listener;

import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;

import java.util.List;

/**
 * Listener interface for organization management operations.
 */
public interface OrganizationManagerListener {

    void preAddOrganization(Organization organization) throws OrganizationManagementException;

    void postAddOrganization(Organization organization) throws OrganizationManagementException;

    void preGetOrganization(String organizationId) throws OrganizationManagementException;

    void postGetOrganization(String organizationId, Organization organization)
            throws OrganizationManagementException;

    void preDeleteOrganization(String organizationId) throws OrganizationManagementException;

    @Deprecated
    void postDeleteOrganization(String organizationId) throws OrganizationManagementException;

    default void postDeleteOrganization(String organizationId, int organizationDepthInHierarchy)
            throws OrganizationManagementException {

        // This method is not implemented.
    }

    /**
     * Invoked after an organization is deleted, carrying the details of the deleted organization, since they can no
     * longer be retrieved once the deletion is complete.
     *
     * @param organizationId                ID of the deleted organization.
     * @param organization                  Details of the deleted organization, as they were before the deletion.
     * @param organizationDepthInHierarchy  Depth of the deleted organization in the hierarchy.
     * @throws OrganizationManagementException If an error occurs while handling the event.
     */
    default void postDeleteOrganization(String organizationId, Organization organization,
                                        int organizationDepthInHierarchy) throws OrganizationManagementException {

        postDeleteOrganization(organizationId, organizationDepthInHierarchy);
    }

    void prePatchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException;

    void postPatchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException;

    void preUpdateOrganization(String organizationId, Organization organization) throws
            OrganizationManagementException;

    void postUpdateOrganization(String organizationId, Organization organization) throws
            OrganizationManagementException;

    /**
     * Invoked after an organization is updated, carrying the organization as it was before the update, so that the
     * values changed by the update can be resolved.
     *
     * @param organizationId        ID of the updated organization.
     * @param organization          Organization details carried by the update request.
     * @param previousOrganization  Details of the organization as they were before the update.
     * @throws OrganizationManagementException If an error occurs while handling the event.
     */
    default void postUpdateOrganization(String organizationId, Organization organization,
                                        Organization previousOrganization) throws OrganizationManagementException {

        postUpdateOrganization(organizationId, organization);
    }
}
