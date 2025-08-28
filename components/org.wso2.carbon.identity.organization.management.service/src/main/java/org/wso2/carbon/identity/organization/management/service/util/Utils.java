/*
 * Copyright (c) 2022-2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.OrganizationManagerImpl;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.B2B_APPLICATION_ROLE_SUPPORT_ENABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.DEFAULT_DISCOVERY_DEFAULT_PARAM;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.DEFAULT_SUB_ORG_LEVEL;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_DB_METADATA;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CREATING_NEW_SYSTEM_ROLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.IS_CARBON_ROLE_VALIDATION_ENABLED_FOR_LEVEL_ONE_ORGS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.IS_ORG_QUALIFIED_URLS_SUPPORTED_FOR_LEVEL_ONE_ORGS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationVersion.ORG_VERSION_DELIMITER_REGEX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationVersion.ORG_VERSION_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATH_SEPARATOR;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUB_ORG_START_LEVEL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DB2;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.MICROSOFT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.MYSQL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.POSTGRESQL;
import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_DOMAIN;

/**
 * This class provides utility functions for the Organization Management.
 */
public class Utils {

    private static final Log LOG = LogFactory.getLog(Utils.class);
    private static DataSource dataSource;
    private static final OrganizationManager organizationManager = new OrganizationManagerImpl();
    private static final Pattern htmlContentPattern = Pattern.compile(".*<[^>]+(/>|>.*?</[^>]+>).*");

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

        if (dataSource == null) {
            dataSource = OrganizationManagementDataHolder.getInstance().getDataSource();
        }
        return new NamedJdbcTemplate(dataSource);
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
     * Check whether the string, "mysql", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    public static boolean isMySqlDB() throws OrganizationManagementServerException {

        return isDBTypeOf(MYSQL);
    }

    /**
     * Check whether the string, "db2", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    public static boolean isDB2DB() throws OrganizationManagementServerException {

        return isDBTypeOf(DB2);
    }

    /**
     * Check whether the string, "postgresql", contains in the driver name or db product name.
     *
     * @return true if the database type matches the driver type, false otherwise.
     * @throws OrganizationManagementServerException If error occurred while checking the DB metadata.
     */
    public static boolean isPostgreSqlDB() throws OrganizationManagementServerException {

        return isDBTypeOf(POSTGRESQL);
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

        String organizationId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId();
        if (StringUtils.isBlank(organizationId)) {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            try {
                organizationId = organizationManager.resolveOrganizationId(tenantDomain);
            } catch (OrganizationManagementException e) {
                throw new RuntimeException("Error occurred while retrieving organization ID for tenant domain: " +
                        tenantDomain + e.getMessage(), e);
            }
        }
        return organizationId;
    }

    /**
     * Get the username of the authenticated user.
     *
     * @return the username of the authenticated user.
     */
    public static String getAuthenticatedUsername() {

        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotEmpty(userName)) {
            return userName;
        }
        try {
            AbstractUserStoreManager userStoreManager = getUserStoreManager(PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getTenantId());
            User user = userStoreManager.getUser(getUserId(), null);
            if (user != null) {
                return user.getUsername();
            }
        } catch (UserStoreException e) {
            LOG.error("Error while resolving the username by user ID.");
        }
        return StringUtils.EMPTY;
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
        // consider iteration till the second last element since initial element is always an empty string.
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
     * @param end   index of final element to create subArray
     * @return Array.
     */
    private static <T> T[] subArray(T[] array, int end) {

        return Arrays.copyOfRange(array, 0, end);
    }

    /**
     * Is carbon role based validation enabled for first level organizations in the deployment.
     *
     * @return True if carbon role based validation enabled for first level organizations.
     */
    public static boolean isCarbonRoleValidationEnabledForLevelOneOrgs() {

        return Boolean.parseBoolean(
                OrganizationManagementConfigUtil.getProperty(IS_CARBON_ROLE_VALIDATION_ENABLED_FOR_LEVEL_ONE_ORGS));
    }

    /**
     * Get the start level of the sub-organizations in the organization tree.
     *
     * @return Start level of the sub-organizations.
     */
    public static int getSubOrgStartLevel() {

        String subOrgStartLevel = OrganizationManagementConfigUtil.getProperty(SUB_ORG_START_LEVEL);
        if (StringUtils.isNotEmpty(subOrgStartLevel)) {
            return Integer.parseInt(subOrgStartLevel);
        }
        return DEFAULT_SUB_ORG_LEVEL;
    }

    /**
     * Is B2B application role support enabled.
     *
     * @return True if B2B application role support is enabled.
     */
    public static boolean isB2BApplicationRoleSupportEnabled() {

        return Boolean.parseBoolean(
                OrganizationManagementConfigUtil.getProperty(B2B_APPLICATION_ROLE_SUPPORT_ENABLED));
    }

    /**
     * Get the organization user invitation primary user domain.
     *
     * @return User invitation primary user domain configuration.
     */
    public static String getOrganizationUserInvitationPrimaryUserDomain() {

        String configuredUserDomain = OrganizationManagementConfigUtil.getProperty(
                OrganizationManagementConstants.ORGANIZATION_USER_INVITATION_PRIMARY_USER_DOMAIN);
        if (StringUtils.equals(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, configuredUserDomain)) {
            String configuredPrimaryDomainName = resolvePrimaryUserStoreDomainName();
            if (!StringUtils.equals(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, configuredPrimaryDomainName)) {
                configuredUserDomain = configuredPrimaryDomainName;
            }
        }
        return configuredUserDomain;
    }

    /**
     * This method resolves the primary user store domain name when it is changed
     * from `PRIMARY` to a different name; otherwise, it returns `PRIMARY`.
     *
     * @return Primary user store domain name.
     */
    public static String resolvePrimaryUserStoreDomainName() {

        RealmConfiguration realmConfiguration =
                OrganizationManagementDataHolder.getInstance().getRealmService().getBootstrapRealmConfiguration();
        if (realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME) != null) {
            return realmConfiguration.getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME).toUpperCase();
        }
        return UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
    }

    /**
     * Return whether organization role based validation is used.
     *
     * @param organizationId Organization id.
     * @return False if the organization is a first level organization in the deployment and
     * IS_CARBON_ROLE_VALIDATION_ENABLED_FOR_LEVEL_ONE_ORGS config is enabled. Otherwise, true.
     */
    public static boolean useOrganizationRolesForValidation(String organizationId) {

        if (!Utils.isCarbonRoleValidationEnabledForLevelOneOrgs()) {
            return true;
        }
        try {
            // Return false if the organization is in depth 1.
            return organizationManager.getOrganizationDepthInHierarchy(organizationId) != 1;
        } catch (OrganizationManagementServerException e) {
            LOG.error("Error while checking the depth of the given organization.");
        }
        return true;
    }

    /**
     * Return whether the given organization is a sub-organization.
     *
     * @param organizationDepth The depth of the organization in the organization tree.
     * @return True if the organization is a sub-organization.
     */
    public static boolean isSubOrganization(int organizationDepth) {

        return organizationDepth >= Utils.getSubOrgStartLevel();
    }

    /**
     * Return whether organization qualified URLs are supported for first level organizations in the deployment.
     *
     * @return True if organization qualified URLs are supported for first level organizations.
     */
    public static boolean isOrgQualifiedURLsSupportedForLevelOneOrganizations() {

        return Boolean.parseBoolean(
                OrganizationManagementConfigUtil.getProperty(IS_ORG_QUALIFIED_URLS_SUPPORTED_FOR_LEVEL_ONE_ORGS));
    }

    /**
     * Return whether the given organization supports both o/ and t/ supported endpoints with organization qualified
     * URLs. True if those endpoints are supported with o/ paths. False if those endpoints are supported with t/ paths.
     *
     * @param organizationId Organization id.
     * @return False if the organization is a first level organization in the deployment and
     * IS_ORG_QUALIFIED_URLS_SUPPORTED_FOR_LEVEL_ONE_ORGS config is disabled. Otherwise, true.
     */
    public static boolean isOrganizationQualifiedURLsSupported(String organizationId) {

        if (Utils.isOrgQualifiedURLsSupportedForLevelOneOrganizations()) {
            return true;
        }
        try {
            // Return false if the organization is in depth 1.
            return organizationManager.getOrganizationDepthInHierarchy(organizationId) != 1;
        } catch (OrganizationManagementServerException e) {
            LOG.error("Error while checking the depth of the given organization.");
        }
        return true;
    }

    /**
     * Retrieve tenant ID for a given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return the tenant ID.
     * @throws RuntimeException If error occurred when retrieving tenant ID or when given tenant domain is invalid.
     */
    public static int getTenantId(String tenantDomain) throws RuntimeException {

        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            if (OrganizationManagementDataHolder.getInstance().getRealmService() != null) {
                tenantId = OrganizationManagementDataHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            }
        } catch (UserStoreException e) {
            throw new RuntimeException("Error occurred while retrieving tenantId for tenantDomain: " + tenantDomain +
                    e.getMessage(), e);
        }
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new RuntimeException("Invalid tenant domain " + tenantDomain);
        }
        return tenantId;
    }

    /**
     * Retrieve tenant domain for a given tenant ID.
     *
     * @param tenantId Tenant ID.
     * @return the tenant domain.
     * @throws RuntimeException If error occurred when retrieving tenant domain or when given tenant ID is invalid.
     */
    public static String getTenantDomain(int tenantId) throws RuntimeException {

        String tenantDomain = null;
        try {
            tenantDomain = OrganizationManagementDataHolder.getInstance().getRealmService().getTenantManager()
                    .getDomain(tenantId);
        } catch (UserStoreException e) {
            throw new RuntimeException("Error occurred while retrieving tenantDomain for tenantId: " + tenantId +
                    e.getMessage(), e);
        }
        if (tenantDomain == null) {
            throw new RuntimeException("Can not find the tenant domain for the tenant id " + tenantId);
        }
        return tenantDomain;
    }

    /**
     * Create the system user for self-service.
     *
     * @param tenantDomain tenant domain.
     * @return userid of the system user.
     */
    public static String getB2BSelfServiceSystemUser(String tenantDomain) {

        // If Legacy authorization is not enabled, API subscription will be used and there's no need to crete this user.
        if (!isLegacyAuthzRuntime()) {
            return null;
        }
        // Read self service configurations.
        String userName = OrganizationManagementConfigUtil.getProperty(
                OrganizationManagementConstants.SELF_SERVICE_SYSTEM_USER_NAME);
        String userStore = OrganizationManagementConfigUtil
                .getProperty(OrganizationManagementConstants.USER_STORE_NAME_FOR_SYSTEM_USER);
        String roleName = OrganizationManagementConfigUtil.getProperty(
                OrganizationManagementConstants.SELF_SERVICE_INTERNAL_ROLE_NAME);
        List<String> permissionsList = OrganizationManagementConfigUtil.getPropertyAsList(
                OrganizationManagementConstants.SELF_SERVICE_INTERNAL_ROLE_PERMISSIONS);

        if (StringUtils.isBlank(userName) || StringUtils.isBlank(roleName)
                || CollectionUtils.isEmpty(permissionsList)) {
            String msg = "Error while creating self-service role for tenant " + tenantDomain;
            String errorMsg = "Self service is not configured properly";
            LOG.debug(msg, new OrganizationManagementServerException(errorMsg));
            return null;
        }

        // Add internal domain to role name.
        roleName = INTERNAL_DOMAIN + OrganizationManagementConstants.USER_DOMAIN_SEPARATOR + roleName;

        // Add user store to username if configured.
        if (StringUtils.isNotBlank(userStore)) {
            userName = userStore + OrganizationManagementConstants.USER_DOMAIN_SEPARATOR + userName;
        }

        AbstractUserStoreManager userStoreManager;
        try {
            userStoreManager = getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            UserCoreUtil.setSkipPasswordPatternValidationThreadLocal(true);
            UserCoreUtil.setSkipUsernamePatternValidationThreadLocal(true);
            // Create system user if not already exist with the tenant.
            if (!userStoreManager.isExistingUser(userName)) {
                userStoreManager.addUser(userName, generatePassword().toCharArray(), null, null,
                        null, false);
            }
            // Create a role for the application and assign the user to that role.
            if (!isRoleAlreadyApplied(userName, roleName, userStoreManager)) {
                createRoleForUser(tenantDomain, userName, permissionsList, roleName, userStoreManager);
            }
            return userStoreManager.getUserIDFromUserName(userName);
        } catch (UserStoreException | OrganizationManagementServerException e) {
            LOG.debug("Exception while creating self service user for tenant " + tenantDomain, e);
            return null;
        } finally {
            UserCoreUtil.removeSkipPasswordPatternValidationThreadLocal();
            UserCoreUtil.removeSkipUsernamePatternValidationThreadLocal();
        }
    }

    /**
     * Get the organization discovery default parameter.
     *
     * @return Organization discovery default parameter.
     */
    public static String getOrganizationDiscoveryDefaultParam() {

        String discoveryDefaultParam = OrganizationManagementConfigUtil.getProperty(
                OrganizationManagementConstants.ORGANIZATION_DISCOVERY_DEFAULT_PARAM);
        if (StringUtils.isNotEmpty(discoveryDefaultParam)) {
            return discoveryDefaultParam;
        }
        return DEFAULT_DISCOVERY_DEFAULT_PARAM;
    }

    /**
     * Get the version that will be used to create new organizations.
     *
     * @return New organization version.
     */
    public static String getNewOrganizationVersion() {

        return OrganizationManagementConfigUtil.getProperty(
                OrganizationManagementConstants.OrganizationVersion.NEW_ORGANIZATION_VERSION_PROPERTY);
    }

    /**
     * Check whether login and registration config inheritance is enabled for the organization identified by the
     * given tenant domain.
     * <p>
     * Login & registration configuration inheritance is considered to be enabled if the organization version is
     * greater than or equal to v1.0.0
     *
     * @param tenantDomain Tenant domain.
     * @return True if login and registration config inheritance is enabled, false otherwise.
     */
    public static boolean isLoginAndRegistrationConfigInheritanceEnabled(String tenantDomain) {

        try {
            return isOrgVersionApplicable(tenantDomain,
                    OrganizationManagementConstants.OrganizationVersion.ORG_VERSION_V1);
        } catch (OrganizationManagementException e) {
            LOG.error("Error while resolving organization ID for tenant domain: " + tenantDomain, e);
        }
        return false;
    }

    /**
     * Check whether claim and OIDC claim inheritance is enabled for the organization identified by the given tenant
     * domain.
     * <p>
     * Claim and OIDC claim inheritance is considered to be enabled if the organization version is
     * equal to or greater than v1.0.0
     *
     * @param tenantDomain Tenant domain of the organization.
     * @return true if claim and OIDC scope inheritance is enabled, false otherwise.
     * @throws OrganizationManagementException If an error occurs while resolving organization id for the given
     * tenant domain
     */
    public static boolean isClaimAndOIDCScopeInheritanceEnabled(String tenantDomain)
            throws OrganizationManagementException {

        return isOrgVersionApplicable(tenantDomain,
                OrganizationManagementConstants.OrganizationVersion.ORG_VERSION_V1);
    }

    /**
     * Checks whether the current organization version is greater than or equal to the minimum
     *
     * @param tenantDomain             Tenant domain of the organization.
     * @param minimumApplicableVersion The minimum applicable version.
     * @return true if the org version is greater than or equal to the minimum applicable version.
     * @throws OrganizationManagementException If an error occurs while resolving organization id for the given
     * tenant domain
     */
    private static boolean isOrgVersionApplicable(String tenantDomain, String minimumApplicableVersion)
            throws OrganizationManagementException {

        String orgVersion;
        try {
            orgVersion = organizationManager.getOrganizationVersion(
                    organizationManager.resolveOrganizationId(tenantDomain));
        } catch (OrganizationManagementException e) {
            if (ERROR_CODE_INVALID_ORGANIZATION.getCode().equals(e.getErrorCode()) ||
                    ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT.getCode().equals(e.getErrorCode())) {
                /*
                 * If the organization is not found, it means the organization related to the given tenant is deleted.
                 * In this case, it is not possible to inherit resources since the organization hierarchy cannot be
                 * resolved. Hence, return false.
                 */
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The organization for tenant domain: " + tenantDomain + " is not found. Hence, " +
                            "returning false for org version applicability check for minimum applicable version: " +
                            minimumApplicableVersion);
                }
                return false;
            }
            throw e;
        }

        if (StringUtils.isBlank(orgVersion) || StringUtils.isBlank(minimumApplicableVersion)) {
            return false;
        }
        // Semantic version comparison
        String[] orgVersionComponents = orgVersion.replace(ORG_VERSION_PREFIX, StringUtils.EMPTY)
                .split(ORG_VERSION_DELIMITER_REGEX);
        String[] minVersionComponents = minimumApplicableVersion.replace(ORG_VERSION_PREFIX, StringUtils.EMPTY)
                .split(ORG_VERSION_DELIMITER_REGEX);
        for (int i = 0; i < orgVersionComponents.length; i++) {
            int orgVersionComponent = Integer.parseInt(orgVersionComponents[i]);
            int minVersionComponent = Integer.parseInt(minVersionComponents[i]);
            if (orgVersionComponent > minVersionComponent) {
                return true;
            }
            if (orgVersionComponent < minVersionComponent) {
                return false;
            }
        }
        // If the comparison didn't return anything within the "for" block,
        // it means both versions are equal, therefore, we return true.
        return true;
    }

    /**
     * Create the system role for the application and assign the user to that role.
     *
     * @param tenantDomain     Tenant Domain.
     * @param username         Username of user to be assigned the role.
     * @param permissionsList  List of permissions to be assigned ot the role.
     * @param roleName         Name of the role to be created.
     * @param userStoreManager User store manager.
     * @throws OrganizationManagementServerException
     */
    private static void createRoleForUser(String tenantDomain, String username, List<String> permissionsList,
                                          String roleName, UserStoreManager userStoreManager)
            throws OrganizationManagementServerException {

        List<Permission> permissionList = new ArrayList<>();
        permissionsList.stream().forEach(permission -> {
            permissionList.add(new Permission(permission, UserMgtConstants.EXECUTE_ACTION));
        });

        String[] usernames = {username};
        Permission[] permissions = permissionList.toArray(new Permission[permissionList.size()]);
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating internal role : " + roleName + " and assign the user : "
                        + Arrays.toString(usernames) + " to that role for tenant " + tenantDomain);
            }
            userStoreManager.addRole(roleName, usernames, permissions);
        } catch (UserStoreException e) {
            assignRoleToUser(username, roleName, userStoreManager, e);
        }
    }

    /**
     * If system role already exists issue, then assign the role to user.
     *
     * @param username         User name
     * @param roleName         Role name
     * @param userStoreManager User store manager
     * @param e                User store exception threw.
     * @throws OrganizationManagementServerException
     */
    private static void assignRoleToUser(String username, String roleName, UserStoreManager userStoreManager,
                                         UserStoreException e) throws OrganizationManagementServerException {

        String errMsg = e.getMessage();
        if (errMsg.contains(ERROR_CODE_ERROR_CREATING_NEW_SYSTEM_ROLE.getCode())
                || errMsg.contains(ERROR_CODE_ERROR_CREATING_NEW_SYSTEM_ROLE.getMessage())) {
            String[] newRoles = {roleName};
            if (LOG.isDebugEnabled()) {
                LOG.debug("Internal role is already created. Skip creating: " + roleName + " and assigning" +
                        " the user: " + username);
            }
            try {
                userStoreManager.updateRoleListOfUser(username, null, newRoles);
            } catch (UserStoreException e1) {
                String msg = "Error while updating internal role: " + roleName + " with user " + username;

                // If concurrent requests were made, the role could already be assigned to the user. When that
                // validation is done upon a user store exception(rather than checking it prior updating the role
                // list of the user), even the extreme case where the concurrent request assigns the role just before
                // db query is executed, is handled.
                try {
                    if (isRoleAlreadyApplied(username, roleName, userStoreManager)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("The role: " + roleName + ", is already assigned to the user: " + username
                                    + ". Skip assigning");
                        }
                        return;
                    }
                } catch (UserStoreException ex) {
                    msg = "Error while getting existing internal roles of the user " + username;
                    throw new OrganizationManagementServerException(msg, ex.getMessage(), ex.getCause());
                }

                // Throw the error, unless the error caused from role being already assigned.
                throw new OrganizationManagementServerException(msg, e1.getMessage(), e1.getCause());
            }
        } else {
            throw new OrganizationManagementServerException("Error while creating internal role: " + roleName +
                    " with user " + username, e.getMessage(), e.getCause());
        }
    }

    private static boolean isRoleAlreadyApplied(String username, String roleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        boolean isRoleAlreadyApplied = false;
        String[] roleListOfUser = userStoreManager.getRoleListOfUser(username);
        if (roleListOfUser != null) {
            isRoleAlreadyApplied = Arrays.asList(roleListOfUser).contains(roleName);
        }
        return isRoleAlreadyApplied;
    }

    public static AbstractUserStoreManager getUserStoreManager(int tenantId) throws UserStoreException {

        RealmService realmService = OrganizationManagementDataHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);
        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }

    private static String generatePassword() {

        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 12);
    }

    /**
     * Check whether the legacy authorization runtime is enabled or not.
     *
     * @return True if the legacy authorization runtime is enabled.
     */
    public static boolean isLegacyAuthzRuntime() {

        return CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME;
    }

    public static boolean hasHtmlContent(String orgName) {

        return htmlContentPattern.matcher(orgName).find();
    }

}
