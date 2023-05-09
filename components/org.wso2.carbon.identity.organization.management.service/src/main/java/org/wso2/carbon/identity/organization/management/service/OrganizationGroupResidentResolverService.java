/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;

import java.util.Optional;

/**
 * Service to resolve group's resident organization.
 */
public interface OrganizationGroupResidentResolverService {

    /**
     * Retrieve group's resident organization by traversing through the ancestor organizations from a given child
     * organization.
     *
     * @param groupId         The group ID.
     * @param organizationId The given child organization in the hierarchy.
     * @return group's resident organization.
     * @throws OrganizationManagementException Error occurred while resolving resident org of the group.
     */
    Optional<String> resolveResidentOrganization(String groupId, String organizationId)
            throws OrganizationManagementException;
}
