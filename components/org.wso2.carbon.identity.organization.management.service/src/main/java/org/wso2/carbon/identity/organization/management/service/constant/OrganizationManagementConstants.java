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

    public static final String ROOT = "ROOT";
    public static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
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
    public static final String ORGANIZATION_DESCRIPTION_FIELD = "description";
    public static final String ORGANIZATION_CREATED_TIME_FIELD = "created";
    public static final String ORGANIZATION_LAST_MODIFIED_FIELD = "lastModified";
    public static final String ORGANIZATION_STATUS_FIELD = "status";
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
    private static final String ORGANIZATION_MANAGEMENT_ERROR_CODE_PREFIX = "ORG-";
    private static final Map<String, String> attributeColumnMap = new HashMap<>();
    public static final Map<String, String> ATTRIBUTE_COLUMN_MAP = Collections.unmodifiableMap(attributeColumnMap);

    static {

        attributeColumnMap.put(ORGANIZATION_NAME_FIELD, VIEW_NAME_COLUMN);
        attributeColumnMap.put(ORGANIZATION_ID_FIELD, "UM_ORG." + VIEW_ID_COLUMN);
        attributeColumnMap.put(ORGANIZATION_DESCRIPTION_FIELD, VIEW_DESCRIPTION_COLUMN);
        attributeColumnMap.put(ORGANIZATION_CREATED_TIME_FIELD, VIEW_CREATED_TIME_COLUMN);
        attributeColumnMap.put(ORGANIZATION_LAST_MODIFIED_FIELD, VIEW_LAST_MODIFIED_COLUMN);
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
                "Unauthorized request to add an organization to parent organization with ID: %s."), // 403
        ERROR_CODE_INVALID_FILTER_FORMAT("60022", "Unable to retrieve organizations.", "Invalid " +
                "format used for filtering."),
        ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE("60023", "Unsupported filter attribute.",
                "The filter attribute '%s' is not supported."),
        ERROR_CODE_UNSUPPORTED_COMPLEX_QUERY_IN_FILTER("60024", "Unsupported filter.",
                "The complex query used for filtering is not supported."),
        ERROR_CODE_INVALID_PAGINATION_PARAMETER_NEGATIVE_LIMIT("60025", "Invalid pagination parameters.",
                "'limit' shouldn't be negative."),
        ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION("60026", "Unable to retrieve organizations.", "Invalid " +
                "cursor used for pagination."),
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
        ERROR_CODE_ORG_PARAMETER_NOT_FOUND("60036", "Organization parameter could not be found.", "The organization " +
                "parameter for shared application authentication is not found."),
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
        ERROR_CODE_PATCH_VALUE_NULL("60047", "Values cannot be null",
                "The patch values cannot be null for ADD and REPLACE operations."),
        ERROR_CODE_INVALID_ATTRIBUTE("60048", "Invalid attribute to assign for a role",
                "Invalid attribute to assign for a role."),
        ERROR_CODE_ROLE_LIST_INVALID_CURSOR("60049", "Cursor decoding failed.",
                "Malformed cursor %s cannot be processed."),
        ERROR_CODE_ROOT_ORG_DELETE_OR_DISABLE("60050", "ROOT organization can't be disabled or deleted.",
                "Organization %s can't be disabled or deleted."),
        ERROR_CODE_ROOT_ORG_RENAME("60051", "ROOT organization can't be renamed.",
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
        ERROR_CODE_UNABLE_TO_CREATE_CHILD_ORGANIZATION_IN_ROOT("60057", "Unable to create the organization.",
                "To create a child organization in root, the request should be invoked from the root " +
                        "organization."),

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
        ERROR_CODE_ERROR_BUILDING_URL_FOR_RESPONSE_BODY("65018", "Unable to build the URL.",
                "Server encountered an error while building URL for response body."),
        ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_AUTHORIZATION("65019", "Unable to create the organization.",
                "Server encountered an error while evaluating authorization of user to create the " +
                        "organization in parent organization with ID: %s."),
        ERROR_CODE_ERROR_BUILDING_PAGINATED_RESPONSE_URL("65020", "Unable to retrieve the organizations.",
                "Server encountered an error while building paginated response URL."),
        ERROR_CODE_ERROR_MISSING_ROOT("65021", "Unable to create the organization.",
                "Server encountered an error while retrieving the ROOT organization"),
        ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_TO_ROOT_AUTHORIZATION("65022", "Unable to create the " +
                "organization.", "Server encountered an error while evaluating authorization of user to create " +
                "a child organization in root."),
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
        ERROR_CODE_ERROR_VALIDATING_USER_ROOT_ASSOCIATION("65071", "Error while validating user " +
                "association with root organization.", "Server encountered when authorizing user against the root " +
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
        ERROR_CODE_ERROR_REMOVING_OAUTH_APP("65076", "Unable to share the application",
                "Server encountered an error when removing oauth consumer app: % for fragment application: %s in " +
                        "organization: %s.");

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
