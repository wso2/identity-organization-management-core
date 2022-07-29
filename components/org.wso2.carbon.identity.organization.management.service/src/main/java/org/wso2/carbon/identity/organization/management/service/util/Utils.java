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

package org.wso2.carbon.identity.organization.management.service.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.persistence.UmPersistenceManager;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;

import java.util.UUID;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_BUILDING_URL_FOR_RESPONSE_BODY;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_DB_METADATA;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_CONTEXT_PATH_COMPONENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_PATH;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATH_SEPARATOR;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SERVER_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.V1_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.ORACLE;

/**
 * This class provides utility functions for the Organization Management.
 */
public class Utils {

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
    public static NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(UmPersistenceManager.getInstance().getDataSource());
    }

    /**
     * Check whether the string, "oracle", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    public static boolean isOracleDB() throws OrganizationManagementServerException {
        try {
            NamedJdbcTemplate jdbcTemplate = getNewTemplate();
            return jdbcTemplate.getDriverName().toLowerCase().contains(ORACLE) ||
                    jdbcTemplate.getDatabaseProductName().toLowerCase().contains(ORACLE);
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

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * Get the user ID.
     *
     * @return the user ID.
     */
    public static String getUserId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserId();
    }

    /**
     * Build URI prepending the server API context with the proxy context path to the endpoint.
     *
     * @param organizationId The organization ID.
     * @return Relative URI.
     */
    public static String buildURIForBody(String organizationId) throws OrganizationManagementServerException {

        String context = getContext(V1_API_PATH_COMPONENT + PATH_SEPARATOR + ORGANIZATION_PATH
                + PATH_SEPARATOR + organizationId);

        try {
            return ServiceURLBuilder.create().addPath(context).build().getRelativePublicURL();
        } catch (URLBuilderException e) {
            throw handleServerException(ERROR_CODE_ERROR_BUILDING_URL_FOR_RESPONSE_BODY, e);
        }
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
}
