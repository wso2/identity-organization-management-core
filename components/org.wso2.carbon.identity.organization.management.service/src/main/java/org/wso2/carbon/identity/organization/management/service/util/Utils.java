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

package org.wso2.carbon.identity.organization.management.service.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverService;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverServiceImpl;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_DB_METADATA;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_UM_DATASOURCE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_CONTEXT_PATH_COMPONENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_PATH;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATH_SEPARATOR;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SERVER_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.V1_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.MICROSOFT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.ORACLE;

/**
 * This class provides utility functions for the Organization Management.
 */
public class Utils {

    private static final Log LOG = LogFactory.getLog(Utils.class);
    private static DataSource dataSource;
    private static final OrganizationUserResidentResolverService organizationUserResidentResolverService =
            new OrganizationUserResidentResolverServiceImpl();

    /**
     * Throw an OrganizationManagementClientException upon client side error in organization management.
     *
     * @param error The error enum.
     * @param data  The error message data.
     * @return OrganizationManagementClientException
     */
    public static OrganizationManagementClientException handleClientException(
            OrganizationManagementConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new OrganizationManagementClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Throw an OrganizationManagementServerException upon server side error in organization management.
     *
     * @param error The error enum.
     * @param e     The error.
     * @param data  The error message data.
     * @return OrganizationManagementServerException
     */
    public static OrganizationManagementServerException handleServerException(
            OrganizationManagementConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new OrganizationManagementServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static NamedJdbcTemplate getNewTemplate() throws OrganizationManagementServerException {

        try {
            if (dataSource == null) {
                if (CarbonContext.getThreadLocalCarbonContext().getUserRealm() == null) {
                    throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_UM_DATASOURCE, null);
                }
                dataSource = DatabaseUtil.getRealmDataSource(CarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm().getRealmConfiguration());
            }
        return new NamedJdbcTemplate(dataSource);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_UM_DATASOURCE, e);
        }
    }

    /**
     * Check whether the string, "oracle", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB() throws OrganizationManagementServerException {

        return isDBTypeOf(ORACLE);
    }

    /**
     * Check whether the string, "microsoft", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    public static boolean isMSSqlDB() throws OrganizationManagementServerException {

        return isDBTypeOf(MICROSOFT);
    }

    /**
     * Check whether the DB type string contains in the driver name or db product name.
     *
     * @param dbType database type string.
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    private static boolean isDBTypeOf(String dbType) throws OrganizationManagementServerException {

        try {
            NamedJdbcTemplate jdbcTemplate = getNewTemplate();
            return jdbcTemplate.getDriverName().toLowerCase().contains(dbType) ||
                    jdbcTemplate.getDatabaseProductName().toLowerCase().contains(dbType);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_DB_METADATA, e);
        }
    }

    /**
     * Get the tenant ID.
     *
     * @return the tenant ID.
     */
    public static int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Get the tenant domain.
     *
     * @return the tenant domain.
     */
    public static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Get the organization id from context.
     *
     * @return the organization id.
     */
    public static String getOrganizationId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId();
    }

    /**
     * Get the username of the authenticated user.
     *
     * @return the username of the authenticated user.
     */
    public static String getAuthenticatedUsername() {

        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (username == null) {
            try {
                username = organizationUserResidentResolverService.resolveUserFromResidentOrganization(null,
                        getUserId(), getOrganizationId()).map(User::getUsername).orElse(null);
            } catch (OrganizationManagementException e) {
                LOG.debug("Authenticated user's username could not be resolved.", e);
            }
        }
        return username;
    }

    /**
     * Get the user ID.
     *
     * @return the user ID.
     */
    public static String getUserId() {

        String userId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserId();
        /*
            The federated users with organization management permissions do not have user id set in the carbon
            context but the user id is set against the username. Therefor the user is resolved by the user id
            information saved in the username.
         */
        if (userId == null && PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() != null) {
            try {
                userId = organizationUserResidentResolverService.resolveUserFromResidentOrganization(null,
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername(),
                        getOrganizationId()).map(User::getUserID).orElse(null);
            } catch (OrganizationManagementException e) {
                LOG.debug("Authenticated user's id could not be resolved.", e);
            }
        }
        return userId;
    }

    /**
     * Build URI prepending the server API context with the proxy context path to the endpoint.
     *
     * @param organizationId The organization ID.
     * @return Relative URI.
     */
    public static String buildURIForBody(String organizationId) {

        String context = getContext(V1_API_PATH_COMPONENT + PATH_SEPARATOR + ORGANIZATION_PATH
                + PATH_SEPARATOR + organizationId);

        return context;
    }

    /**
     * Builds the API context.
     *
     * @param endpoint Relative endpoint path.
     * @return Context of the API.
     */
    public static String getContext(String endpoint) {

        String organizationId = getOrganizationId();
        if (StringUtils.isNotBlank(organizationId)) {
            return String.format(ORGANIZATION_CONTEXT_PATH_COMPONENT, organizationId) + SERVER_API_PATH_COMPONENT +
                    endpoint;
        } else {
            return SERVER_API_PATH_COMPONENT + endpoint;
        }
    }

    /**
     * Generate unique identifier for the organization.
     *
     * @return organization id.
     */
    public static String generateUniqueID() {

        return UUID.randomUUID().toString();
    }

    /**
     * Create a List containing allowed permissions.
     *
     * @param resourceId permission provided
     * @return List of allowed permissions.
     */
    public static List<String> getAllowedPermissions(String resourceId) {

        String[] permissionParts = resourceId.split(PATH_SEPARATOR);
        List<String> allowedPermissions = new ArrayList<>();
        for (int i = 0; i < permissionParts.length - 1; i++) {
            allowedPermissions.add(String.join(PATH_SEPARATOR,
                    subArray(permissionParts, permissionParts.length - i)));
        }
        return allowedPermissions;
    }

    /**
     * Create a subArray by slicing array from start to specified end.
     *
     * @param array original array with element to be sliced
     * @param end index of final element to create subArray
     * @return Array.
     */
    private static <T> T[] subArray(T[] array, int end) {

        return Arrays.copyOfRange(array, 0, end);
    }
}
