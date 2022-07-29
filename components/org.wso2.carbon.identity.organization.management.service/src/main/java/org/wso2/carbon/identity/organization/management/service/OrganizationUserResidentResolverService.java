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

package org.wso2.carbon.identity.organization.management.service;

import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.user.core.common.User;

import java.util.Optional;

/**
 * Service to resolve user's resident organization.
 */
public interface OrganizationUserResidentResolverService {

    /**
     * Resolve the user from resident organization.
     *
     * @param userName               Username.
     * @param userId                 User ID.
     * @param accessedOrganizationId Organization that user is trying to access.
     * @return User object based on user resident.
     * @throws OrganizationManagementException Error occurred while resolving resident org of the user.
     */
    Optional<User> resolveUserFromResidentOrganization(String userName, String userId, String accessedOrganizationId)
            throws OrganizationManagementException;
}
