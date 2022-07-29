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

package org.wso2.carbon.identity.organization.management.authz.service;

import org.wso2.carbon.identity.organization.management.authz.service.dao.OrganizationManagementAuthzDAO;
import org.wso2.carbon.identity.organization.management.authz.service.dao.OrganizationManagementAuthzDAOImpl;
import org.wso2.carbon.identity.organization.management.authz.service.exception.OrganizationManagementAuthzServiceServerException;

/**
 * Manager for organization management related authorization.
 */
public class OrganizationManagementAuthorizationManager {

    private static final OrganizationManagementAuthorizationManager INSTANCE =
            new OrganizationManagementAuthorizationManager();

    public static OrganizationManagementAuthorizationManager getInstance() {

        return INSTANCE;
    }

    /**
     * Check whether the user is authorized for the particular organization.
     *
     * @param userId     Unique identifier of the user.
     * @param resourceId Required permission.
     * @param orgId      Organization id.
     * @return Whether the user is authorized or not.
     * @throws OrganizationManagementAuthzServiceServerException The server exception thrown when evaluating user's
     *                                                           authorization.
     */
    public boolean isUserAuthorized(String userId, String resourceId, String orgId)
            throws OrganizationManagementAuthzServiceServerException {

        OrganizationManagementAuthzDAO organizationMgtAuthzDAO = new OrganizationManagementAuthzDAOImpl();
        return organizationMgtAuthzDAO.isUserAuthorized(userId, resourceId, orgId);
    }

    /**
     * Check whether user has any permission/association for the given organization.
     *
     * @param userId User id.
     * @param orgId  Organization id.
     * @return True if user has at least single permission against organization.
     * @throws OrganizationManagementAuthzServiceServerException Error occurred while retrieving user
     *                                                           association to organization.
     */
    public boolean hasUserOrgAssociation(String userId, String orgId)
            throws OrganizationManagementAuthzServiceServerException {

        OrganizationManagementAuthzDAO organizationMgtAuthzDAO = new OrganizationManagementAuthzDAOImpl();
        return organizationMgtAuthzDAO.hasUserOrgAssociation(userId, orgId);
    }

    /**
     * Resolve root organization id.
     *
     * @return Root organization id.
     * @throws OrganizationManagementAuthzServiceServerException if error occurred when retrieving root org id.
     */
    public String getRootOrganizationId() throws OrganizationManagementAuthzServiceServerException {

        OrganizationManagementAuthzDAO organizationMgtAuthzDAO = new OrganizationManagementAuthzDAOImpl();
        return organizationMgtAuthzDAO.getRootOrganizationId();
    }
}
