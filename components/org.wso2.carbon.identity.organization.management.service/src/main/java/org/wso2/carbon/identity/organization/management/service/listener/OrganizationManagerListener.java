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

        // This default method is added to avoid breaking changes. This method will be removed in the next major release.
    }

    void prePatchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException;

    void postPatchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException;

    void preUpdateOrganization(String organizationId, Organization organization) throws
            OrganizationManagementException;

    void postUpdateOrganization(String organizationId, Organization organization) throws
            OrganizationManagementException;
}
