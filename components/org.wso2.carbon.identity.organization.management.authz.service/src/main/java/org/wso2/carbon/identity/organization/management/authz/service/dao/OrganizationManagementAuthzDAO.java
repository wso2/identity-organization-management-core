/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.authz.service.dao;

import org.wso2.carbon.identity.organization.management.authz.service.exception.OrganizationManagementAuthzServiceServerException;

/**
 * DAO to validate authorization capabilities of organization management.
 */
public interface OrganizationManagementAuthzDAO {

    boolean isUserAuthorized(String userId, String resourceId, String orgId)
            throws OrganizationManagementAuthzServiceServerException;

    /**
     * Check whether user has any permission/association for the given organization.
     *
     * @param userId User id.
     * @param orgId  Organization id.
     * @return True if user has at least single permission against organization.
     * @throws OrganizationManagementAuthzServiceServerException Error occurred while retrieving user
     *                                                           association to org.
     */
    boolean hasUserOrgAssociation(String userId, String orgId) throws OrganizationManagementAuthzServiceServerException;

    /**
     * Resolve root organization id.
     *
     * @return Root organization id.
     * @throws OrganizationManagementAuthzServiceServerException if error occurred when retrieving root org id.
     */
    String getRootOrganizationId() throws OrganizationManagementAuthzServiceServerException;
}
