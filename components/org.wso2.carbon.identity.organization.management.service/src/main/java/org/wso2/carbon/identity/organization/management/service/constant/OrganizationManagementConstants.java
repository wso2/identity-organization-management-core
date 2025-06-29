/*
 * Copyright (c) 2022-2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.service.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains constants related to organization management.
 */
public class OrganizationManagementConstants {

    public static final String SUPER = "Super";
    public static final String SUPER_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    public static final int DEFAULT_SUB_ORG_LEVEL = 1;
    public static final String PATH_SEPARATOR = "/";
    public static final String V1_API_PATH_COMPONENT = "v1";
    public static final String ORGANIZATION_PATH = "organizations";
    public static final String ORGANIZATION_CONTEXT_PATH_COMPONENT = "/o/%s";
    public static final String SERVER_API_PATH_COMPONENT = "/api/server/";
    public static final String VIEW_ID_COLUMN = "UM_ID";
    public static final String VIEW_NAME_COLUMN = "UM_ORG_NAME";
    public static final String VIEW_DESCRIPTION_COLUMN = "UM_ORG_DESCRIPTION";
    public static final String VIEW_CREATED_TIME_COLUMN = "UM_CREATED_TIME";
    public static final String VIEW_LAST_MODIFIED_COLUMN = "UM_LAST_MODIFIED";
    public static final String VIEW_STATUS_COLUMN = "UM_STATUS";
    public static final String VIEW_PARENT_ID_COLUMN = "UM_PARENT_ID";
    public static final String VIEW_ATTR_KEY_COLUMN = "UM_ATTRIBUTE_KEY";
    public static final String VIEW_ATTR_VALUE_COLUMN = "UM_ATTRIBUTE_VALUE";
    public static final String VIEW_TYPE_COLUMN = "UM_ORG_TYPE";
    public static final String VIEW_TENANT_UUID_COLUMN = "UM_TENANT_UUID";
    public static final String VIEW_TENANT_DOMAIN_COLUMN = "UM_DOMAIN_NAME";
    public static final String VIEW_ORGANIZATION_ATTRIBUTES_TABLE = "UM_ORG_ATTRIBUTE";
    public static final String PATCH_OP_ADD = "ADD";
    public static final String PATCH_OP_REMOVE = "REMOVE";
    public static final String PATCH_OP_REPLACE = "REPLACE";
    public static final String PATCH_PATH_ORG_NAME = "/name";
    public static final String PATCH_PATH_ORG_DESCRIPTION = "/description";
    public static final String PATCH_PATH_ORG_STATUS = "/status";
    public static final String PATCH_PATH_ORG_ATTRIBUTES = "/attributes/";
    public static final String PARENT_ID_FIELD = "parentId";
    public static final String ORGANIZATION_NAME_FIELD = "name";
    public static final String ORGANIZATION_ID_FIELD = "id";
    public static final String ORGANIZATION_ID_PROPERTY = "ORGANIZATION_ID";
    public static final String ORGANIZATION_DESCRIPTION_FIELD = "description";
    public static final String ORGANIZATION_CREATED_TIME_FIELD = "created";
    public static final String ORGANIZATION_LAST_MODIFIED_FIELD = "lastModified";
    public static final String ORGANIZATION_STATUS_FIELD = "status";
    public static final String ORGANIZATION_ATTRIBUTES_FIELD = "attributes";
    public static final String ORGANIZATION_ATTRIBUTES_FIELD_PREFIX = "attributes.";
    public static final String PAGINATION_AFTER = "after";
    public static final String PAGINATION_BEFORE = "before";
    public static final String CREATE_ORGANIZATION_ADMIN_PERMISSION = "/permission/admin/";
    public static final String BASE_ORGANIZATION_PERMISSION = "/permission/admin/manage/identity/organizationmgt";
    public static final String CREATE_ORGANIZATION_PERMISSION = "/permission/admin/manage/identity/organizationmgt/" +
            "create";
    public static final String VIEW_ORGANIZATION_PERMISSION = "/permission/admin/manage/identity/organizationmgt/" +
            "view";
    public static final String UPDATE_ORGANIZATION_PERMISSION = "/permission/admin/manage/identity/organizationmgt/" +
            "update";
    public static final String DELETE_ORGANIZATION_PERMISSION = "/permission/admin/manage/identity/organizationmgt/" +
            "delete";
    public static final List<String> ALL_ORGANIZATION_PERMISSIONS = Collections.unmodifiableList(Arrays
            .asList(CREATE_ORGANIZATION_PERMISSION, VIEW_ORGANIZATION_PERMISSION, UPDATE_ORGANIZATION_PERMISSION,
                    DELETE_ORGANIZATION_PERMISSION));
    public static final String ROOT_TENANT_DOMAIN = "RootTenantDomain";
    public static final String DESC_SORT_ORDER = "DESC";
    public static final String ASC_SORT_ORDER = "ASC";

    public static final String EQ = "eq";
    public static final String CO = "co";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String GE = "ge";
    public static final String LE = "le";
    public static final String GT = "gt";
    public static final String LT = "lt";
    public static final String AND = "and";
    public static final String FILTER_PLACEHOLDER_PREFIX = "FILTER_ID_";
    public static final String PARENT_ID_FILTER_PLACEHOLDER_PREFIX = "FILTER_PARENT_ID_";
    public static final String META_ATTRIBUTE_PLACEHOLDER_PREFIX = "UM_ORG_ATTRIBUTE_";
    private static final String ORGANIZATION_MANAGEMENT_ERROR_CODE_PREFIX = "ORG-";
    private static final Map<String, String> attributeColumnMap = new HashMap<>();
    public static final Map<String, String> ATTRIBUTE_COLUMN_MAP = Collections.unmodifiableMap(attributeColumnMap);
    public static final String ORGANIZATION_MGT_CONFIG_FILE = "organization-mgt.xml";
    public static final String IS_CARBON_ROLE_VALIDATION_ENABLED_FOR_LEVEL_ONE_ORGS =
            "LevelOneOrganizationConfigs.EnableCarbonRoleBasedValidation";
    public static final String IS_ORG_QUALIFIED_URLS_SUPPORTED_FOR_LEVEL_ONE_ORGS =
            "LevelOneOrganizationConfigs.SupportOrganizationQualifiedURLs";
    public static final String SUB_ORG_START_LEVEL = "SubOrganizationStartLevel";
    public static final String B2B_APPLICATION_ROLE_SUPPORT_ENABLED = "B2BApplicationRoleSupportEnabled";
    public static final String IS_ORGANIZATION_MANAGEMENT_FEATURE_ENABLED = "Enable";

    // Organization management cache constants.
    public static final String CACHE_CONFIG = "CacheConfig";
    public static final String CACHE_MANAGER = "CacheManager";
    public static final String CACHE_MANAGER_NAME = "name";
    public static final String CACHE = "Cache";
    public static final String CACHE_NAME = "name";
    public static final String CACHE_ENABLE = "enable";
    public static final String CACHE_TIMEOUT = "timeout";
    public static final String CACHE_CAPACITY = "capacity";
    public static final String IS_DISTRIBUTED_CACHE = "isDistributed";
    public static final String IS_TEMPORARY = "isTemporary";
    public static final int DEFAULT_ORGANIZATION_DEPTH_IN_HIERARCHY = -1;

    // Self-service constants.
    public static final String USER_DOMAIN_SEPARATOR = "/";
    public static final String SELF_SERVICE_SYSTEM_USER_NAME = "SelfService.SystemUserName";
    public static final String USER_STORE_NAME_FOR_SYSTEM_USER = "SelfService.UserStoreNameForSystemUser";
    public static final String SELF_SERVICE_INTERNAL_ROLE_NAME = "SelfService.InternalRoleName";
    public static final String SELF_SERVICE_INTERNAL_ROLE_PERMISSIONS =
            "SelfService.InternalRolePermissions.Permission";

    // Application sharing related constants
    public static final String SHARE_WITH_ALL_CHILDREN = "shareWithAllChildren";
    public static final String IS_APP_SHARED = "isAppShared";

    public static final String CREATOR_ID = "creator.id";
    public static final String CREATOR_USERNAME = "creator.username";
    public static final String CREATOR_EMAIL = "creator.email";
    public static final String ORGANIZATION_USER_INVITATION_PRIMARY_USER_DOMAIN =
            "OrganizationUserInvitation.PrimaryUserDomain";

    /**
     * Contains constants related to filter operations.
     */
    public static class Filter {

        public static final String AND = "and";
        public static final String OR = "or";
        public static final String NOT = "not";
    }

    static {

        attributeColumnMap.put(ORGANIZATION_NAME_FIELD, VIEW_NAME_COLUMN);
        attributeColumnMap.put(ORGANIZATION_ID_FIELD, "UM_ORG." + VIEW_ID_COLUMN);
        attributeColumnMap.put(ORGANIZATION_DESCRIPTION_FIELD, VIEW_DESCRIPTION_COLUMN);
        attributeColumnMap.put(ORGANIZATION_CREATED_TIME_FIELD, VIEW_CREATED_TIME_COLUMN);
        attributeColumnMap.put(ORGANIZATION_LAST_MODIFIED_FIELD, VIEW_LAST_MODIFIED_COLUMN);
        attributeColumnMap.put(ORGANIZATION_STATUS_FIELD, VIEW_STATUS_COLUMN);
        attributeColumnMap.put(ORGANIZATION_ATTRIBUTES_FIELD, VIEW_ATTR_KEY_COLUMN);
        attributeColumnMap.put(PAGINATION_AFTER, VIEW_CREATED_TIME_COLUMN);
        attributeColumnMap.put(PAGINATION_BEFORE, VIEW_CREATED_TIME_COLUMN);
    }

    /**
     * Enum for organization types.
     */
    public enum OrganizationTypes {

        STRUCTURAL,
        TENANT
    }

    /**
     * Enum for organization status.
     */
    public enum OrganizationStatus {

        ACTIVE,
        DISABLED
    }

    /**
     * Enum for error messages related to organization management.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_CODE_INVALID_REQUEST_BODY("60001", "Invalid request.", "Provided request body content " +
                "is not in the expected format."),
        ERROR_CODE_REQUIRED_FIELDS_MISSING("60002", "Invalid request body.", "Missing required field : %s"),
        ERROR_CODE_ATTRIBUTE_KEY_MISSING("60003", "Invalid request body.",
                "Attribute keys cannot be empty."),
        ERROR_CODE_DUPLICATE_ATTRIBUTE_KEYS("60004", "Invalid request body.",
                "Attribute keys cannot be duplicated."),
        ERROR_CODE_INVALID_PARENT_ORGANIZATION("60005", "Invalid parent organization.",
                "Defined parent organization: %s doesn't exist."),
        ERROR_CODE_ORGANIZATION_HAS_CHILD_ORGANIZATIONS("60007", "Unable to delete the organization.",
                "Organization with ID: %s has one or more child organizations."),
        ERROR_CODE_PATCH_OPERATION_UNDEFINED("60008", "Unable to patch the organization.",
                "Missing patch operation in the patch request sent for organization with ID: %s."),
        ERROR_CODE_INVALID_PATCH_OPERATION("60009", "Unable to patch the organization.",
                "Invalid patch operation: %s. Patch operation must be one of ['add', 'replace', 'remove']."),
        ERROR_CODE_PATCH_REQUEST_PATH_UNDEFINED("60010", "Unable to patch the organization.",
                "Patch path is not defined."),
        ERROR_CODE_PATCH_REQUEST_INVALID_PATH("60011", "Unable to patch the organization.",
                "Provided path :%s is invalid."),
        ERROR_CODE_PATCH_REQUEST_VALUE_UNDEFINED("60012", "Missing required value.",
                "Value is mandatory for 'add' and 'replace' operations."),
        ERROR_CODE_PATCH_REQUEST_MANDATORY_FIELD_INVALID_OPERATION("60013", "Unable to patch the organization.",
                "Mandatory fields can only be replaced. Provided op : %s, path : %s"),
        ERROR_CODE_ORGANIZATION_ID_UNDEFINED("60014", "Invalid request.",
                "The organization ID can't be empty."),
        ERROR_CODE_INVALID_ORGANIZATION("60015", "Invalid organization.",
                "Organization with ID: %s doesn't exist."), // 404
        ERROR_CODE_ATTRIBUTE_VALUE_MISSING("60016", "Invalid request body.",
                "Attribute value is required for all attributes."),
        ERROR_CODE_ORGANIZATION_NAME_RESERVED("60017", "Organization name unavailable.",
                "Creating an organization with name: %s is restricted. Use a different organization name."),
        ERROR_CODE_PATCH_REQUEST_ATTRIBUTE_KEY_UNDEFINED("60018", "Unable to patch the organization.",
                "Missing attribute key."),
        ERROR_CODE_PATCH_REQUEST_REMOVE_NON_EXISTING_ATTRIBUTE("60019", "Unable to patch the organization.",
                "Cannot remove non existing attribute key: %s"),
        ERROR_CODE_PATCH_REQUEST_REPLACE_NON_EXISTING_ATTRIBUTE("60020", "Unable to patch the organization.",
                "Cannot replace non existing attribute key: %s"),
        ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION("60021", "Unable to create the organization.",
                "Unauthorized request to add an organization under the parent organization with ID: %s."), // 403
        ERROR_CODE_INVALID_FILTER_FORMAT("60022", "Unable to retrieve organizations.", "Invalid " +
                "format used for filtering."),
        ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE("60023", "Unsupported filter attribute.",
                "The filter attribute '%s' is not supported."),
        ERROR_CODE_UNSUPPORTED_COMPLEX_QUERY_IN_FILTER("60024", "Unsupported filter.",
                "The complex query used for filtering is not supported."),
        ERROR_CODE_INVALID_PAGINATION_PARAMETER_NEGATIVE_LIMIT("60025", "Invalid pagination parameters.",
                "'limit' shouldn't be negative."),
        ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION("60026", "Unable to retrieve paginated result.",
                "Invalid cursor used for pagination."),
        ERROR_CODE_UNSUPPORTED_ORGANIZATION_STATUS("60028", "Unsupported status provided.",
                "Organization status must be 'ACTIVE' or 'DISABLED'."),
        ERROR_CODE_ACTIVE_CHILD_ORGANIZATIONS_EXIST("60029", "Active child organizations exist.",
                "Organization with ID: %s can't be disabled as there are active child organizations."),
        ERROR_CODE_PARENT_ORGANIZATION_IS_DISABLED("60030", "Parent organization is disabled.",
                "To set the child organization status as active, parent organization should be in active status."),
        ERROR_CODE_CREATE_REQUEST_PARENT_ORGANIZATION_IS_DISABLED("60031", "Parent organization is disabled.",
                "To create a child organization in organization with ID: %s, it should be in active status."),
        ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION("60032", "Unable to create the organization.",
                "Invalid request body for tenant type organization."),
        ERROR_CODE_ORGANIZATION_TYPE_UNDEFINED("60033", "Unable to create the organization.",
                "Organization type should be defined."),
        ERROR_CODE_INVALID_ORGANIZATION_TYPE("60034", "Invalid organization type.", "The organization " +
                "type should be 'TENANT' or 'STRUCTURAL'."),
        ERROR_CODE_INVALID_APPLICATION("60035", "Invalid application", "The requested application %s is invalid."),
        ERROR_CODE_APPLICATION_NOT_SHARED("60037", "Application not shared with organization.", "The " +
                "application %s is not shared with organization with ID %s."),
        ERROR_CODE_REMOVING_REQUIRED_ATTRIBUTE("60038", "Unable to remove a required attribute.",
                "Cannot remove the required attribute %s with operation %s."),
        ERROR_CODE_INVALID_ATTRIBUTE_PATCHING("60039", "Invalid attribute.",
                "Invalid attribute %s for operation %s."),
        ERROR_CODE_ROLE_DISPLAY_NAME_MULTIPLE_VALUES("60040", "The display name cannot have multiple values.",
                "The display name should have single value."),
        ERROR_CODE_ROLE_DISPLAY_NAME_NULL("60041", "Role name cannot be null",
                "Role name cannot be null."),
        ERROR_CODE_ROLE_DISPLAY_NAME_ALREADY_EXISTS("60042", "Role name already exists.",
                "Role name %s exists in organization %s"),
        ERROR_CODE_INVALID_ROLE("60043", "Invalid role.", "Role with ID: %s doesn't exist."),
        ERROR_CODE_REMOVE_OP_VALUES("60044", "Remove patch operation values are passed with the path.",
                "Remove patch operation values are passed along with the path."),
        ERROR_CODE_INVALID_USER_ID("60045", "Invalid user.", "User with ID: %s doesn't exist."),
        ERROR_CODE_INVALID_GROUP_ID("60046", "Invalid group.", "Invalid group %s."),
        ERROR_CODE_PATCH_VALUE_EMPTY("60047", "Value cannot be empty",
                "The patch value cannot be empty for ADD operations."),
        ERROR_CODE_INVALID_ATTRIBUTE("60048", "Invalid attribute to assign for a role",
                "Invalid attribute to assign for a role."),
        ERROR_CODE_ROLE_LIST_INVALID_CURSOR("60049", "Cursor decoding failed.",
                "Malformed cursor %s cannot be processed."),
        ERROR_CODE_SUPER_ORG_DELETE_OR_DISABLE("60050", "Super organization can't be disabled or deleted.",
                "Organization %s can't be disabled or deleted."),
        ERROR_CODE_SUPER_ORG_RENAME("60051", "Super organization can't be renamed.",
                "Organization %s can't be renamed."),
        ERROR_CODE_ROLE_IS_UNMODIFIABLE("60052", "Role can't be modified.",
                "Role %s cannot be updated or deleted."),
        ERROR_CODE_INVALID_ORGANIZATION_NAME("60053", "Organization not found",
                "Organization with name %s not found"),
        ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT("60054", "Organization not found for the tenant",
                "Organization for the tenant domain %s not found."),
        ERROR_CODE_NO_USERNAME_OR_ID_TO_RESOLVE_USER_FROM_RESIDENT_ORG("60055",
                "Both userId and UserName cannot be null.",
                "Either userId or UserName is required to resolve user from resident organization."),
        ERROR_CODE_RETRIEVING_ORGANIZATIONS_BY_NAME("60055", "No organization found",
                "Organizations not found by name: %s"),
        ERROR_CODE_INVALID_ORGANIZATION_ID("65056", "Unable to retrieve the organization name",
                "Organization not found with organization with ID: %s."),
        ERROR_CODE_UNABLE_TO_CREATE_CHILD_ORGANIZATION_IN_SUPER("60057", "Unable to create the organization.",
                "To create a child organization in super, the request should be invoked from the super " +
                        "organization."),
        ERROR_CODE_USER_ROOT_ORGANIZATION_NOT_FOUND("60058", "Unable to retrieve the root organization.",
                "A root organization is not found for the authenticated user with ID: %s."),
        ERROR_CODE_UNSUPPORTED_FILTER_OPERATION("60059", "Given filter operator is not supported.",
                "Given filter operator is not supported. Filter attribute: %s"),
        ERROR_CODE_EMPTY_FILTER_VALUE("60060", "Provided filter value is empty.",
                "Provided filter value is empty. attributeValue: %s  operation: %s"),
        ERROR_CODE_INVALID_FILTER_ARGUMENT("60061", "Invalid filter argument",
                "Invalid argument: Identity Provider filter name value is empty or invalid symbol: %s"),
        ERROR_CODE_SUPER_ORG_ROLE_CREATE("60062", "Organization roles can't be created in Super organization.",
                "Organization %s can't manage organization roles."),
        ERROR_CODE_UNAUTHORIZED_ORG_ROLE_ACCESS("60063", "Organization roles can't be managed from a another " +
                "organization.", "Organization roles of organization %s can't manage from organization %s."),
        ERROR_CODE_UNAUTHORIZED_ORG_ACCESS("60064", "Organization can only be managed from an ancestor " +
                "organization.", "Organization %s can't manage from organization %s."),
        ERROR_CODE_UNAUTHORIZED_APPLICATION_SHARE("60065", "Applications can be shared only from the " +
                "organization that application resides.", "Application %s can't be shared from organization %s."),
        ERROR_CODE_UNAUTHORIZED_FRAGMENT_APP_ACCESS("60066", "Fragment applications can be managed only " +
                "from the organization that fragment application resides.", "Application %s can't be managed " +
                "from organization %s."),
        ERROR_CODE_INVALID_SHARE_APPLICATION_EMPTY_REQUEST_BODY("60067", "Invalid request.",
                "At least one of the attributes from shareWithAllChildren and sharedOrganizations should be present."),
        ERROR_CODE_INVALID_SHARE_APPLICATION_REQUEST_BODY("60068", "Invalid request.", "Cannot share " +
                "the application with a set of child organizations when shareWithAllChildren is set to true."),
        ERROR_CODE_INVALID_DELETE_SHARE_REQUEST("60069", "Invalid request.", "Cannot unshare " +
                "the application with ID %s from the organization with ID %s if the application is shared with all " +
                "children organizations."),
        ERROR_CODE_SAME_ORG_NAME_ON_IMMEDIATE_SUB_ORGANIZATIONS_OF_PARENT_ORG("60070",
                "Given organization name is taken from a sibling organization.", "Given organization " +
                "name: %s is taken from a sibling organization of parent organization id %s"),
        ERROR_CODE_INVALID_FILTER_TIMESTAMP_FORMAT("60071", "Unable to retrieve organizations.", "Invalid " +
                "timestamp format used for filtering."),
        ERROR_CODE_UNSUPPORTED_FILTER_OPERATION_FOR_ATTRIBUTE("60072", "Unsupported filter operation on attribute.",
                "The filter operation '%s' on attribute '%s' is not supported."),
        ERROR_CODE_ORG_ROLE_PATCH_REMOVE_OPERATION_INVALID_FILTER_FORMAT("60073", "Unable to update organization " +
                "role.", "Invalid filter format '%s' provided in the remove operation's path of the patch request to " +
                "organization role: %s ."),
        ERROR_CODE_RETRIEVING_ORG_ROLES_INVALID_FILTER_FORMAT("60074", "Unable to get organization roles", "Invalid " +
                "filter format provided when listing organization roles : %s ."),
        ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT_ID("60075", "Organization not found for the tenant id",
                "Organization for the tenant id %s not found."),
        ERROR_CODE_ORGANIZATION_NAME_EXIST_IN_CHILD_ORGANIZATIONS("60076", "Given organization name is taken " +
                "from a child organization of the root organization.",
                "Given organization name is taken from a child organization of the root organization id: %s."),
        ERROR_CODE_BLOCK_SHARING_SHARED_APP("60077", "Shared applications are not allowed to be shared.",
                "Application %s can't be shared with any organization."),
        ERROR_CODE_SUB_ORG_CANNOT_CREATE_APP("60078", "Applications cannot be created for sub-organizations.",
                "Application cannot be created for the sub-organization with id: %s."),
        ERROR_CODE_NO_PARENT_ORG("60079", "No parent organization.",
                "No parent organization is available for the give organization id: %s."),
        ERROR_CODE_DISCOVERY_CONFIG_DISABLED("60080", "Unable to execute the requested organization discovery " +
                "management task.", "The organization discovery configuration is disabled in the root organization " +
                "with ID: %s."),
        ERROR_CODE_UNAUTHORIZED_ORG_FOR_DISCOVERY_ATTRIBUTE_MANAGEMENT("60081", "Unable to execute the " +
                "requested organization discovery management task.", "Only the root organization is allowed to " +
                "manage the discovery attributes of the organization with ID: %s."),
        ERROR_CODE_DUPLICATE_DISCOVERY_ATTRIBUTE_TYPES("60082", "Invalid request body.", "The discovery " +
                "attribute type: %s is duplicated."),
        ERROR_CODE_DISCOVERY_ATTRIBUTE_TAKEN("60083", "Discovery attribute is already mapped in the " +
                "organization hierarchy.", "The provided discovery attribute value is already associated with a " +
                "different organization's discovery attribute of type %s."),
        ERROR_CODE_UNSUPPORTED_DISCOVERY_ATTRIBUTE("60084", "Unsupported discovery attribute.",
                "The discovery attribute type: %s is unsupported."),
        ERROR_CODE_DISCOVERY_ATTRIBUTE_ALREADY_ADDED_FOR_ORGANIZATION("60085", "Discovery attribute(s) are " +
                "already added for the organization.", "Discovery attribute(s) are already added for the " +
                "organization with ID: %s."),
        ERROR_CODE_PAGINATION_NOT_IMPLEMENTED("60086", "Pagination not supported.", "Pagination " +
                "capabilities are not currently supported."),
        ERROR_CODE_FILTERING_NOT_IMPLEMENTED("60087", "Filtering not supported.", "Filtering " +
                "capabilities are not currently supported."),
        ERROR_CODE_EMPTY_DISCOVERY_ATTRIBUTES("60088", "Invalid request.", "Discovery attributes " +
                "cannot be empty."),
        ERROR_CODE_MANAGED_ORGANIZATION_CLAIM_UPDATE_NOT_ALLOWED("60089", "The managed organization " +
                "is not allowed to modify.", "The managed organization is a read only property."),
        ERROR_CODE_EMAIL_DOMAIN_ASSOCIATED_WITH_DIFFERENT_ORGANIZATION("60090", "The user is not allowed " +
                "to register with this organization.", "The email domain provided has been associated with a " +
                "different organization."),
        ERROR_CODE_EMAIL_DOMAIN_NOT_MAPPED_TO_ORGANIZATION("60091", "The user is not allowed to register with " +
                "this organization.", "The email domain provided is not associated with this organization."),
        ERROR_CODE_INVALID_DISCOVERY_ATTRIBUTE_VALUE("60092", "Invalid discovery attribute value.", "The provided " +
                "discovery attribute(s) do not adhere to the expected format for the discovery type: %s."),
        ERROR_CODE_INVALID_OFFSET("60093", "Invalid request.", "The provided offset value is invalid."),
        ERROR_CODE_INVALID_LIMIT("60094", "Invalid request.", "The provided limit value is invalid."),
        ERROR_CODE_SHARED_USER_CLAIM_UPDATE_NOT_ALLOWED("60095", "The claims cannot be modified for the shared users",
                "The shared user profile attributes are read only."),
        ERROR_CODE_ORGANIZATION_OWNER_NOT_EXIST("60096", "The assigned organization owner does not exist ",
                "The assigned organization owner is not found in the tenant with ID: %s"),
        ERROR_CODE_ORGANIZATION_NAME_CONTAINS_HTML_CONTENT("60097", "Invalid organization name.",
                "HTML content is not allowed in organization name."),
        ERROR_CODE_INVALID_EMAIL_DOMAIN("60098", "Invalid email domain.",
                "Email domain resolved from the authenticated federated IDP is not mapped to the organization: %s"),
        ERROR_CODE_NO_EMAIL_ATTRIBUTE_FOUND("60099", "No email attribute found",
                "No email attribute returned by the authenticated federated IDP"),
        ERROR_CODE_EXISTING_ORGANIZATION_HANDLE("60100", "Unable to create the organization.",
                "The given organization handle %s already exists. Please use a different handle."),

        // Server errors.
        ERROR_CODE_UNEXPECTED("65001", "Unexpected processing error",
                "Server encountered an error while serving the request."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS("65002", "Unable to retrieve the organizations.",
                "Server encountered an error while retrieving the organizations."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_BY_ID("65003", "Unable to retrieve the organization.",
                "Server encountered an error while retrieving the organization with ID: %s"),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_ID_BY_NAME("65004", "Unable to retrieve the organization.",
                "Server encountered an error while retrieving organization with name: %s."),
        ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS("65005", "Unable to retrieve child organizations.",
                "Server encountered an error while retrieving the child organizations of organization " +
                        "with ID: %s."),
        ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_NAME("65006", "Unable to check if the organization" +
                " name is available.", "Server encountered an error while checking if an organization with " +
                "name: %s exists"),
        ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_ID("65007",
                "Error while checking if the organization exists.",
                "Server encountered an error while checking if the organization with ID: %s exists."),
        ERROR_CODE_ERROR_PATCHING_ORGANIZATION("65008", "Unable to patch the organization.",
                "Server encountered an error while patching the organization with ID: %s."),
        ERROR_CODE_ERROR_UPDATING_ORGANIZATION("65009", "Unable to update the organization.",
                "Server encountered an error while updating the organization with ID: %s."),
        ERROR_CODE_ERROR_DELETING_ORGANIZATION("65010", "Unable to delete the organization.",
                "Server encountered an error while deleting the organization with ID: %s."),
        ERROR_CODE_ERROR_DELETING_ORGANIZATION_ATTRIBUTES("65011", "Unable to delete organization " +
                "attributes.", "Server encountered an error while deleting the attributes of " +
                "organization : %s."),
        ERROR_CODE_ERROR_CHECKING_ORGANIZATION_ATTRIBUTE_KEY_EXIST("65012", "Error while checking if the " +
                "attribute exists.", "Server encountered an error while checking if the attribute : %s exists" +
                " for organization with ID: %s."),
        ERROR_CODE_ERROR_PATCHING_ORGANIZATION_ADD_ATTRIBUTE("65013", "Unable to patch the organization.",
                "Server encountered an error while adding the attribute: %s to organization with ID: %s."),
        ERROR_CODE_ERROR_PATCHING_ORGANIZATION_DELETE_ATTRIBUTE("65014", "Unable to patch the organization.",
                "Server encountered an error while deleting the attribute: %s of organization with ID: %s."),
        ERROR_CODE_ERROR_PATCHING_ORGANIZATION_UPDATE_ATTRIBUTE("65015", "Unable to patch the organization.",
                "Server encountered an error while updating attribute: %s of organization with ID: %s."),
        ERROR_CODE_ERROR_ADDING_ORGANIZATION("65016", "Unable to create the organization.",
                "Server encountered an error while creating the organization."),
        ERROR_CODE_ERROR_BUILDING_RESPONSE_HEADER_URL("65017", "Unable to build created organization URL.",
                "Server encountered an error while building URL for response header."),
        ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_AUTHORIZATION("65019", "Unable to create the organization.",
                "Server encountered an error while evaluating authorization of user to create the " +
                        "organization in parent organization with ID: %s."),
        ERROR_CODE_ERROR_BUILDING_PAGINATED_RESPONSE_URL("65020", "Unable to retrieve the paginated results.",
                "Server encountered an error while building paginated response URL."),
        ERROR_CODE_ERROR_MISSING_SUPER("65021", "Unable to create the organization.",
                "Server encountered an error while retrieving the super organization."),
        ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_TO_SUPER_AUTHORIZATION("65022", "Unable to create the " +
                "organization.", "Server encountered an error while evaluating authorization of user to create " +
                "a child organization in super organization."),
        ERROR_CODE_ERROR_CHECKING_ACTIVE_CHILD_ORGANIZATIONS("65023", "Unable to retrieve active child " +
                "organizations.", "Server encountered an error while retrieving the active child organizations " +
                "of organization with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_PARENT_ORGANIZATION_STATUS("65024", "Unable to retrieve the status of " +
                "the parent organization.", "Server encountered an error while checking the status of the parent " +
                "organization of organization with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_STATUS("65025", "Unable to retrieve the status of the organization.",
                "Server encountered an error while checking the status of the organization with ID: %s."),
        ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION("65026", "Unable to create the organization.",
                "Server encountered an error while creating the tenant."),
        ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT("65027", "Unable to deactivate the tenant.",
                "Server encountered an error while deactivating the tenant of the tenant type organization with " +
                        "ID: %s."),
        ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT("65028", "Unable to activate the tenant.",
                "Server encountered an error while activating the tenant of the tenant type organization with " +
                        "ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_TYPE("65029", "Unable to retrieve the organization type.",
                "Server encountered an error while retrieving the type of the organization with ID: %s."),
        ERROR_CODE_ADDING_ROLE_TO_ORGANIZATION("65030", "Error adding role to the organization.",
                "Server encountered an error while adding a role to an organization %s."),
        ERROR_CODE_ADDING_GROUP_TO_ROLE("65031", "Error adding group(s) to role.",
                "Server encountered an error while adding a group to the role %s."),
        ERROR_CODE_ADDING_USER_TO_ROLE("65032", "Error adding user(s) to role.",
                "Server encountered an error while adding a user to the role."),
        ERROR_CODE_ADDING_PERMISSION_TO_ROLE("65033", "Error adding permission(s) to role.",
                "Server encountered an error while adding a permission to the role %s"),
        ERROR_CODE_GETTING_ROLE_FROM_ID("65034", "Error getting role.",
                "Server encountered an error while retrieving role from role id %s."),
        ERROR_CODE_ERROR_REQUEST_ORGANIZATION_REDIRECT("65035", "Unable to redirect to request organization.",
                "Server encountered an error when redirecting to requested organization via Organization Login."),
        ERROR_CODE_GETTING_USERS_USING_ROLE_ID("65036", "Error getting users.",
                "Server encountered an error while retrieving user(s) from role id %s."),
        ERROR_CODE_GETTING_GROUPS_USING_ROLE_ID("65037", "Error getting users of role Id.",
                "Server encountered an error while retrieving user(s) from role id %s."),
        ERROR_CODE_GETTING_PERMISSIONS_USING_ROLE_ID("65038", "Error getting permissions for the role.",
                "Server encountered an error while retrieving permission(s) from role id %s."),
        ERROR_CODE_GETTING_ROLES_FROM_ORGANIZATION("65039", "Error getting roles from organization.",
                "Server encountered an error while retrieving role(s) from organization %s."),
        ERROR_CODE_PATCHING_ROLE("65040", "Error patching a role from organization.",
                "Server encountered an error while patching a role in the organization %s."),
        ERROR_CODE_REMOVING_USERS_FROM_ROLE("65041", "Error removing a user from role.",
                "Server encountered an error while removing users from the role %s."),
        ERROR_CODE_REMOVING_GROUPS_FROM_ROLE("65042", "Error removing a group from role.",
                "Server encountered an error while removing groups from the role %s."),
        ERROR_CODE_REMOVING_PERMISSIONS_FROM_ROLE("65043", "Error removing a permission from role.",
                "Server encountered an error while removing permissions from the role %s."),
        ERROR_CODE_REMOVING_ROLE_FROM_ORGANIZATION("65044", "Error removing a role from an organization.",
                "Server encountered an error while removing a role %s from organization %s."),
        ERROR_CODE_REPLACING_DISPLAY_NAME_OF_ROLE("65045", "Error replacing display name of role.",
                "Server encountered an error while replacing the display name %s of role %s."),
        ERROR_CODE_GETTING_PERMISSION_IDS_USING_PERMISSION_STRING("65046",
                "Error getting permission ids using resource id.",
                "Server encountered an error while retrieving the permission ids."),
        ERROR_CODE_GETTING_ROLE_FROM_ORGANIZATION_ID_ROLE_NAME("65047",
                "Error getting role from role name and organization id.",
                "Server encountered an error while retrieving a role %s from organization %s."),
        ERROR_CODE_GETTING_ROLE_FROM_ORGANIZATION_ID_ROLE_ID("65048",
                "Error getting role from role id and organization id.",
                "Sever encountered an error while retrieving a role %s from organization %s."),
        ERROR_CODE_GETTING_USER_VALIDITY("65049", "Error getting user from user id.",
                "Server encountered an error while retrieving a user %s."),
        ERROR_CODE_GETTING_GROUP_VALIDITY("65050", "Error getting group from group id.",
                "Server encountered an error while retrieving a group %s."),
        ERROR_CODE_ERROR_BUILDING_ROLE_URI("65051", "Unable to build create role URI.",
                "Server encountered an error while building URL for role with roleId %s."),
        ERROR_CODE_ERROR_BUILDING_GROUP_URI("65052", "Unable to build create group URI.",
                "Server encountered an error while building URL for group with groupId %s."),
        ERROR_CODE_ERROR_BUILDING_USER_URI("65053", "Unable to build create user URI.",
                "Server encountered an error while building URL for user with userId %s."),
        ERROR_CODE_ERROR_RETRIEVING_APPLICATION("65054", "Unable to retrieve the application.",
                "Server encountered an error while retrieving the application with ID: %s."),
        ERROR_CODE_ERROR_RESOLVING_SHARED_APPLICATION("65055", "Unable to resolve the shared application", "Server " +
                "encountered an error while resolving the shared application for application: %s in organization: %s."),
        ERROR_CODE_ERROR_SHARING_APPLICATION("65056", "Unable to share the application",
                "Server encountered an error when sharing application: %s to organization: %s."),
        ERROR_CODE_ERROR_LINK_APPLICATIONS("65057", "Unable to link the shared application.",
                "Server encountered an error when linking the application: %s to shared application: %s."),
        ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_LOGIN("65058", "Unable to resolve the shared app for organization " +
                "login.", "Server encountered an error when resolving organization login for application: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_PERMISSIONS("65059", "Unable to retrieve organizations permissions.",
                "Server encountered an error while retrieving the organizations permissions of organization " +
                        "with ID: %s for user with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_TENANT_UUID("65060",
                "Unable to retrieve the associated tenant UUID for the organization.",
                "Server encountered an error while retrieving the associated tenant UUID for the organization ID: %s."),
        ERROR_CODE_ERROR_RESOLVING_TENANT_DOMAIN_FROM_ORGANIZATION_DOMAIN("65061", "Unable to retrieve the " +
                "associated tenant domain for the organization.", "Server encountered an error while retrieving the " +
                "associated tenant domain for the organization with ID: %s."),
        ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_DOMAIN_FROM_TENANT_DOMAIN("65062", "Unable to retrieve the " +
                "organization domain for the tenant.", "Server encountered an error while retrieving the " +
                "organization domain for the tenant: %s."),
        ERROR_CODE_ERROR_CHECKING_IF_CHILD_OF_PARENT("65063", "Error while checking if an organization" +
                " is a child of another organization.", "Server encountered an error while checking if organization " +
                "with ID: %s is a child organization of organization with ID: %s."),
        ERROR_CODE_ERROR_CHECKING_IF_IMMEDIATE_CHILD_OF_PARENT("65064", "Error while checking if an organization " +
                "is an immediate child of another organization.", "Server encountered an error while " +
                "checking if organization with ID: %s is an immediate child organization of organization with ID: %s."),
        ERROR_CODE_ERROR_WHILE_RETRIEVING_ANCESTORS("65065", "Error while retrieving ancestors of an organization.",
                "Error while retrieving ancestors of organization with ID: %s."),
        ERROR_CODE_ERROR_WHILE_RESOLVING_USER_FROM_RESIDENT_ORG("65066",
                "Error while resolving user from resident organization.",
                "Error while resolving user: %s from resident organization, to access organization with ID: %s."),
        ERROR_CODE_ERROR_CHECKING_DB_METADATA("65067", "Error while checking the database metadata.",
                "Server encountered an error while checking database type."),
        ERROR_CODE_ERROR_RETRIEVING_USER_ORGANIZATION_ROLES("65068", "Error while retrieving organization roles of " +
                "the user.", "Server encountered an error while retrieving the organizations roles of organization " +
                "with ID: %s for user with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_AUTHENTICATED_USER("65069", "Error while retrieving authenticated user.",
                "Server encountered while retrieving the authenticated user from user store."),
        ERROR_CODE_ERROR_VALIDATING_USER_ASSOCIATION("65070", "Error while validating user association " +
                "for organization.", "Server encountered when authorizing user against the associated " +
                "organization."),
        ERROR_CODE_ERROR_VALIDATING_USER_SUPER_ASSOCIATION("65071", "Error while validating user " +
                "association with super organization.", "Server encountered when authorizing user against the super " +
                "organization."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_NAME_BY_ID("65072", "Unable to retrieve the organization.",
                "Server encountered an error while retrieving organization with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_BY_NAME("65073", "Unable to retrieve organizations.",
                "Server encountered an error while retrieving organizations with name: %s."),
        ERROR_CODE_ERROR_ADMIN_USER_NOT_FOUND_FOR_ORGANIZATION("65074", "Unable to retrieve " +
                "admin user for the organization.", "Server encountered an error while retrieving " +
                "organization user with id: %s."),
        ERROR_CODE_ERROR_CREATING_OAUTH_APP("65075", "Unable create oauth consumer app for fragment application",
                "Server encountered an error when creating oauth consumer app for fragment application: %s in " +
                        "organization: %s."),
        ERROR_CODE_ERROR_REMOVING_OAUTH_APP("65076", "Unable to remove the oauth consumer app for fragment application",
                "Server encountered an error when removing oauth consumer app: %s for fragment application: %s in " +
                        "organization: %s."),
        ERROR_CODE_ORG_PARAMETERS_NOT_RESOLVED("65077", "Organization name or organization id is not " +
                "resolved.", "The organization information has not resolved before the authentication."),
        ERROR_CODE_ERROR_WHILE_RESOLVING_USERS_ROOT_ORG("65078", "Unable to resolve user's root organization.",
                "Error while resolving root organization of user with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_IDP_LIST("65079", "Unable to retrieve organization IDP list.",
                "Error while retrieving the IDP list of the organization: %s"),
        ERROR_CODE_ERROR_REMOVING_FRAGMENT_APP("65080", "Unable to remove the fragment application",
                "Server encountered an error when removing fragment app: %s in organization: %s."),
        ERROR_CODE_ERROR_CREATING_ORG_LOGIN_IDP("65081", "Unable to create organization IDP.",
                "Error while creating the Organization Login IDP in organization: %s"),
        ERROR_CODE_ERROR_UPDATING_APPLICATION("65082", "Unable to update the application authentication steps.",
                "Error while updating the authentication details of the application: %s"),
        ERROR_CODE_ERROR_RETRIEVING_UM_DATASOURCE("65083", "Error while retrieving user management data source.",
                "Server encountered an error while retrieving user management data source."),
        ERROR_CODE_ERROR_ADDING_ORGANIZATION_ATTRIBUTE("65084", "Unable to add organization attribute.",
                "Server encountered an error while adding organization attribute."),
        ERROR_CODE_ERROR_ADDING_ORGANIZATION_HIERARCHY_DATA("65085", "Unable to add organization hierarchy data.",
                "Server encountered an error while adding organization hierarchy data."),
        ERROR_CODE_ERROR_CHECKING_IF_USER_AUTHORIZED("65086", "Error while checking whether user is authorized.",
                "Error while checking whether user with ID: %s is authorized in organization with ID: %s"),
        ERROR_CODE_ERROR_CHECKING_USER_ASSOCIATION_WITH_ORGANIZATION("65087", "Error while checking whether user " +
                "association with organization.", "Error while checking whether user with ID: %s is associated with " +
                "organization with ID: %s"),
        ERROR_CODE_ERROR_CHECKING_APPLICATION_HAS_FRAGMENTS("65088",
                "Unable to check whether the application has fragments.",
                "Server encountered an error when checking whether the application: %s already has fragments."),
        ERROR_CODE_ERROR_RESOLVING_MAIN_APPLICATION("65089", "Unable to resolve the main application",
                "Server encountered an error while resolving the main application for " +
                        "shared application: %s in shared organization: %s."),
        ERROR_CODE_ERROR_FIRING_EVENTS("65090", "Error while firing the events",
                "Server encountered an error while firing the events"),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DEPTH("65091", "Unable to retrieve the organization depth.",
                "Server encountered an error while retrieving depth of organization with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_APPLICATIONS("65092", "Error while retrieving applications.",
                "Encountered an error while retrieving applications in organization id %s."),
        ERROR_CODE_ERROR_CHECKING_APPLICATION_IS_A_FRAGMENT("65093", "Error while checking whether app is " +
                "a fragment.", "Encountered an error while checking whether application %s is a fragment application."),
        ERROR_CODE_ERROR_UPDATING_APPLICATION_ATTRIBUTE("65094", "Error when updating application.",
                "Encountered an error when updating application of id: %s with property share with all children."),
        ERROR_CODE_ERROR_WHILE_RESOLVING_GROUPS_ROOT_ORG("65095", "Unable to resolve group's root organization.",
                "Error while resolving root organization of group with ID: %s."),
        ERROR_CODE_ERROR_CREATING_NEW_SYSTEM_ROLE("31701", "Please pick another role name",
                "Role name already exists in the system. Please pick another role name."),
        ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_ID_FROM_TENANT_ID("65095", "Unable to retrieve the " +
                "organization id for the tenant id.", "Server encountered an error while retrieving the " +
                "organization id for the tenant id: %s."),
        ERROR_CODE_ERROR_CREATING_SHARED_APP_ROLES("65096", "Unable to create shared application roles.",
                "Server encountered an error when creating shared application roles for shared application: %s " +
                        " in sub-organization: %s."),
        ERROR_CODE_ERROR_DELETING_SHARED_APP_ROLES("65097", "Unable to delete shared application roles.",
                "Server encountered an error when deleting shared application roles related to" +
                        " parent application: %s in organization: %s."),
        ERROR_CODE_ERROR_DELETING_USER_ROLE_ASSIGNMENTS("65098", "Unable to remove the user from the roles.",
                "Server encountered an error while removing the user id: %s from the roles."),
        ERROR_CODE_ERROR_GENERATING_AUTH_TOKEN_FOR_TENANT_DELETION_SERVICE("65099",
                "Error while generating authentication token for tenant deletion service provider.",
                "Error while generating authentication token for tenant deletion service provider using " +
                        "client credentials. Server responded with with response code : %d"),
        ERROR_CODE_ERROR_DELETING_SUB_ORGANIZATION_TENANT("65100", "Unable to delete the underlying " +
                "tenant of the sub organization.", "Server encountered an error while deleting the underlying " +
                "tenant for sub organization id : %s."),
        ERROR_CODE_ERROR_WHILE_DELETING_TENANT_META_DATA("65101", "Unable to delete tenant meta data.",
                "Server encountered an error while attempting to delete tenant meta data for tenant id: %s."),
        ERROR_CODE_ERROR_RETRIEVING_TENANT_DELETION_SERVICE_HOST_URL("650102", "Tenant deletion " +
                "service host url not found.", "Configuration could not be found for tenant deletion " +
                "service host url."),
        ERROR_CODE_ERROR_RETRIEVING_IDENTITY_SERVER_HOST_URL("65103", "Error while retrieving identity " +
                "server host url.", "Internal server error while reading identity server host url."),
        ERROR_CODE_ERROR_WHILE_RESOLVING_USER_IN_RESIDENT_ORG("65104",
                "Error while resolving user in resident organization.",
                "Error while resolving user: %s in resident organization with ID: %s."),
        ERROR_CODE_ERROR_SENDING_TENANT_DELETION_REQUEST("65105", "Error while sending tenant deletion " +
                "request.", "Server encountered an error while sending delete request for tenant id: %s."),
        ERROR_CODE_ERROR_SENDING_TENANT_DELETION_AUTHENTICATION_REQUEST("65106", "Error while sending" +
                "authentication request for tenant deletion service.", "Server encountered an error while" +
                "sending authentication request for tenant deletion service"),
        ERROR_CODE_ERROR_CHECKING_SIBLING_ORGANIZATION_BY_NAME("65107", "Failed to check sibling organization " +
                "by name.", "Error while checking child organization of the parent organization id: %s by name."),
        ERROR_CODE_ERROR_CHECKING_CHILD_ORGANIZATION_BY_NAME("65108", "Failed to check child organization " +
                "by name.", "Error while checking child organization of the root organization id: %s by name."),
        ERROR_CODE_ERROR_RESOLVING_ROOT_ORGANIZATION_OF_ORGANIZATION("65109", "Unable to retrieve the " +
                "root organization of the child organization", "Server encountered an error while retrieving the " +
                "root organization of the organization id: %s."),
        ERROR_CODE_ERROR_RETRIEVING_RELATIVE_ORGANIZATION_DEPTH_IN_BRANCH("65110", "Unable to retrieve the " +
                "relative organization depth in branch.", "Server encountered an error while retrieving relative " +
                "depth of organizations with IDs: %s & %s"),
        ERROR_CODE_ERROR_WHILE_RESOLVING_UNDERLYING_TENANT_OF_SUB_ORGANIZATION("65111", "Unable to retrieve " +
                "the underlying tenant of the sub organization.", "Server encountered an error while retrieving " +
                "the underlying tenant of the sub organization id: %s."),
        ERROR_CODE_ERROR_WHILE_DELETING_TENANT_RESOURCES("65112", "Unable to delete tenant resources.",
                "Server responded with response code: %d while deleting tenant resources for tenant id: %s."),
        ERROR_CODE_ERROR_WHILE_INITIALIZING_SUB_ORGANIZATION_TENANT_DELETION_HANDLER("65113", "Error while " +
                "initializing sub organization tenant deletion handler.", "Server encountered an error while " +
                "initializing SubOrganizationTenantDeletionHandler. Value not found for : %s."),
        ERROR_CODE_GET_ANCESTOR_IN_DEPTH("65114", "Unable to retrieve the ancestor organization in depth.",
                "Server encountered an error while retrieving the ancestor organization in depth %s for organization " +
                        "ID: %s."),
        ERROR_CODE_ERROR_ADDING_ORGANIZATION_DISCOVERY_ATTRIBUTE("65115", "Unable to add organization " +
                "discovery attribute.", "Server encountered an error while adding organization discovery " +
                "attribute for organization with ID: %s."),
        ERROR_CODE_ERROR_CHECKING_ORGANIZATION_DISCOVERY_ATTRIBUTE_EXIST_IN_HIERARCHY("65116", "Error " +
                "while checking if the organization discovery attribute exists.", "Server encountered an error" +
                " while checking if the organization discovery attribute exists within the hierarchy."),
        ERROR_CODE_ERROR_CHECKING_DISCOVERY_ATTRIBUTE_ADDED_IN_ORGANIZATION("65117", "Error while checking " +
                "if the organization already has discovery attributes", "Server encountered an error while " +
                "checking if the organization with ID: %s already has discovery attribute(s) added."),
        ERROR_CODE_ERROR_RETRIEVING_DISCOVERY_CONFIGURATION("65118", "Error while retrieving discovery " +
                "configuration.", "Server encountered an error while retrieving discovery configuration for " +
                "the root organization with ID: %s."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DISCOVERY_ATTRIBUTE("65119", "Error while retrieving " +
                "organization discovery attribute.", "Server encountered an error while retrieving organization " +
                "discovery attribute for organization with ID: %s."),
        ERROR_CODE_ERROR_DELETING_ORGANIZATION_DISCOVERY_ATTRIBUTE("65120", "Error while deleting " +
                "organization discovery attribute.", "Server encountered an error while deleting organization " +
                "discovery attribute for organization with ID: %s."),
        ERROR_CODE_ERROR_UPDATING_ORGANIZATION_DISCOVERY_ATTRIBUTE("65121", "Unable to update organization " +
                "discovery attribute.", "Server encountered an error while updating organization discovery " +
                "attribute for organization with ID: %s."),
        ERROR_CODE_ERROR_LISTING_ORGANIZATIONS_DISCOVERY_ATTRIBUTES("65122", "Error while listing " +
                "organizations discovery attributes.", "Server encountered an error while fetching discovery " +
                "attributes for organizations."),
        ERROR_CODE_ERROR_CREATE_ORGANIZATION_USER_ASSOCIATION("65123", "Unable to create organization user " +
                "association.", "Server encountered an error while creating organization user association " +
                "for user with ID: %s."),
        ERROR_CODE_ERROR_DELETE_ORGANIZATION_USER_ASSOCIATION_FOR_SHARED_USER("65124", "Unable to delete" +
                " organization user association.", "Server encountered an error while deleting organization " +
                "user association of the shared user with ID: %s."),
        ERROR_CODE_ERROR_DELETE_ORGANIZATION_USER_ASSOCIATIONS("65125", "Unable to delete" +
                " organization user associations of the user.", "Server encountered an error while deleting " +
                "organization user associations of user."),
        ERROR_CODE_ERROR_GET_ORGANIZATION_USER_ASSOCIATIONS("65126", "Unable to get the organization user " +
                "associations.", "Server encountered an error while fetching organization user " +
                "associations for the user."),
        ERROR_CODE_ERROR_GET_ORGANIZATION_USER_ASSOCIATION_FOR_USER_AT_SHARED_ORG("65127", "Unable to get the " +
                "organization user association.", "Server encountered an error while fetching the organization " +
                "user association for the user in shared organization %s."),
        ERROR_CODE_ERROR_DELETE_SHARED_USER("65128", "Unable to remove the shared user.",
                "Server encountered an error while deleting the shared user with ID: %s from the shared " +
                        "organization %s"),
        ERROR_CODE_ERROR_FETCH_USER_MANAGED_ORGANIZATION_CLAIM("65129", "Unable to fetch the user managed " +
                "organization.", "Server encountered an error while fetching the user managed " +
                "organization of the shared user with ID: %s"),
        ERROR_CODE_ERROR_CREATE_SHARED_USER("65130", "Unable to share the user.",
                "Server encountered an error while sharing user with organization %s"),
        ERROR_CODE_ERROR_GET_ORGANIZATION_USER_ASSOCIATION_OF_SHARED_USER("65131", "Unable to get the " +
                "organization user association for shared user.", "Server encountered an error while " +
                "fetching the organization user association for the shared user with ID: %s at shared " +
                "organization %s."),
        ERROR_CODE_ERROR_GETTING_ORGANIZATION_ID_BY_DISCOVERY_ATTRIBUTE("65132", "Unable to retrieve the " +
                "organization ID associated with the provided discovery attribute.", "Server encountered an error " +
                "when attempting to retrieve the organization ID linked to the given discovery attribute, which has " +
                "the type: %s and value: %s, within the hierarchy under the root organization with ID: %s"),
        ERROR_CODE_ERROR_VALIDATING_ORGANIZATION_DISCOVERY_ATTRIBUTE("65133", "Unable to validate the " +
                "organization discovery attribute.", "Server encountered an error while validating the " +
                "organization discovery attribute."),
        ERROR_CODE_ERROR_GETTING_ORGANIZATION_DISCOVERY_CONFIG("65134", "Unable to retrieve the " +
                "organization discovery configuration.", "Server encountered an error while retrieving the " +
                "organization discovery configuration."),
        ERROR_CODE_ERROR_CREATING_ROOT_ORGANIZATION("65135", "Unable to create the root organization.",
                "Server encountered an error while creating the root organization for the tenant with ID: %s"),
        ERROR_CODE_ERROR_DEACTIVATING_ROOT_ORGANIZATION_TENANT("65136", "Failed to deactivate the tenant " +
                "when root organization failed to create.", "Server encountered an error while deactivating" +
                " the root organization tenant with ID: %s"),
        ERROR_CODE_ERROR_VALIDATING_ORGANIZATION_OWNER("65137", "Error while validating organization " +
                "owner.", "Server encountered while validating the organization owner for the " +
                "organization with ID: %s."),
        ERROR_CODE_ERROR_REVOKING_SHARED_APP_TOKENS("65138", "Error while revoking tokens issued for " +
                "shared application.", "Server encountered an error while revoking tokens issued for application: " +
                "%s in organization with ID: %s"),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_META_ATTRIBUTES("65139", "Unable to retrieve " +
                "the organizations' meta attributes.", "Server encountered an error while retrieving " +
                "the organizations' meta attributes."),
        ERROR_CODE_ERROR_VALIDATING_ORGANIZATION_LOGIN_HINT_ATTRIBUTE("65140", "Unable to validate the " +
                "login hint attribute.", "Server encountered an error while validating the " +
                "login hint attribute."),
        ERROR_CODE_ERROR_DELETING_SHARED_APPLICATION_LINK("65141", "Unable to delete the shared " +
                "application link", "Server encountered an error while deleting the shared application " +
                "links for organization: %s."),
        ERROR_WHILE_RETRIEVING_ORG_DISCOVERY_ATTRIBUTES("65142",
                "Error while retrieving organization discovery attributes",
                "Error while retrieving organization discovery attributes for tenantDomain: %s"),
        ERROR_CODE_ERROR_UPDATE_ORGANIZATION_USER_ASSOCIATIONS("65143",
                "Unable to update organization user associations.",
                "Server encountered an error while updating organization user associations for the user."),
        ERROR_CODE_ERROR_CHECK_ORGANIZATION_USER_ASSOCIATIONS("65144",
                "Unable to check if organization user associations exist.",
                "Server encountered an error while checking organization user associations for the user."),
        ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_HANDLE("65145",
                "Error while checking if the organization exists.",
                "Server encountered an error while checking if the organization with handle: %s exists."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DETAILS_BY_ORGANIZATION_IDS("65146",
                "Error in retrieving organization details by organization ids.",
                "Server encountered an error while retrieving organization details for given organization ids"),
        ERROR_CODE_ERROR_RETRIEVING_APPLICATION_SHARED_ACCESS_STATUS("65147",
                "Error in retrieving application shared access status.",
                "Server encountered an error while retrieving application shared access status."),
        ERROR_CODE_ERROR_SHARING_APPLICATION_NAME_CONFLICT("65148",
                "Organization %s has a non shared application with name %s.",
                "Server encountered an error while sharing application to organization %s due to a non shared " +
                        "application with name %s."),
        ERROR_CODE_ERROR_SHARING_APPLICATION_ROLE_CONFLICT("65149", "Organization %s has a non shared role with " +
                "name %s.", "Server encountered an error while sharing application to organization %s " +
                "due to a non shared role with name %s."),
        ERROR_CODE_ERROR_CHECKING_IS_ANCESTOR_ORGANIZATION("65150", "Unable to check if organization " +
                "is an ancestor organization.", "Server encountered an error while checking if " +
                "organizations %s is an ancestor organization of organizations %s.");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return ORGANIZATION_MANAGEMENT_ERROR_CODE_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }
    }
}
