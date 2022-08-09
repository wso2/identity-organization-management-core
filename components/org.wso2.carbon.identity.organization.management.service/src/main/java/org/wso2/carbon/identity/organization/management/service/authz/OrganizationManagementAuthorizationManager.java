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

package org.wso2.carbon.identity.organization.management.service.authz;

import org.wso2.carbon.identity.organization.management.service.authz.dao.OrganizationManagementAuthzDAO;
import org.wso2.carbon.identity.organization.management.service.authz.dao.OrganizationManagementAuthzDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;

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
     * @throws OrganizationManagementServerException The server exception thrown when evaluating user's authorization.
     */
    public boolean isUserAuthorized(String userId, String resourceId, String orgId)
            throws OrganizationManagementServerException {

        OrganizationManagementAuthzDAO organizationMgtAuthzDAO = new OrganizationManagementAuthzDAOImpl();
        return organizationMgtAuthzDAO.isUserAuthorized(userId, resourceId, orgId);
    }

    /**
     * Check whether user has any permission/association for the given organization.
     *
     * @param userId User id.
     * @param orgId  Organization id.
     * @return True if user has at least single permission against organization.
     * @throws OrganizationManagementServerException Error occurred while retrieving user association to organization.
     */
    public boolean hasUserOrgAssociation(String userId, String orgId)
            throws OrganizationManagementServerException {

        OrganizationManagementAuthzDAO organizationMgtAuthzDAO = new OrganizationManagementAuthzDAOImpl();
        return organizationMgtAuthzDAO.hasUserOrgAssociation(userId, orgId);
    }

    /**
     * Resolve super organization id.
     *
     * @return Super organization id.
     * @throws OrganizationManagementServerException if error occurred when retrieving super org id.
     */
    public String getSuperOrganizationId() throws OrganizationManagementServerException {

        OrganizationManagementAuthzDAO organizationMgtAuthzDAO = new OrganizationManagementAuthzDAOImpl();
        return organizationMgtAuthzDAO.getSuperOrganizationId();
    }
}
