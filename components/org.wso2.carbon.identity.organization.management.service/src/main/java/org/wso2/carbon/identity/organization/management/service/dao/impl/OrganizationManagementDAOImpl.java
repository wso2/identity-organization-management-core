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

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.filter.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.FilterQueryBuilder;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationNode;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.identity.organization.management.service.authz.constant.SQLConstants.GET_ORGANIZATION_ID_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ALL_ORGANIZATION_PERMISSIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ATTRIBUTE_COLUMN_MAP;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.BASE_ORGANIZATION_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CO;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.EQ;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.EW;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_ORGANIZATION_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_ORGANIZATION_HIERARCHY_DATA;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ACTIVE_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_CHILD_ORGANIZATION_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_IF_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_IF_IMMEDIATE_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_IS_ANCESTOR_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_ATTRIBUTE_KEY_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_SIBLING_ORGANIZATION_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DELETING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DELETING_ORGANIZATION_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION_ADD_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION_DELETE_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION_UPDATE_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_DOMAIN_FROM_TENANT_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_ID_FROM_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RESOLVING_TENANT_DOMAIN_FROM_ORGANIZATION_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_META_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DEPTH;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DETAILS_BY_ORGANIZATION_IDS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_ID_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_NAME_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_PERMISSIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_PARENT_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_RELATIVE_ORGANIZATION_DEPTH_IN_BRANCH;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_UUID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_UPDATING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_ANCESTORS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_GET_ANCESTOR_IN_DEPTH;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.FILTER_PLACEHOLDER_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.GE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.GT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.LE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.LT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ATTRIBUTES_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ATTRIBUTES_FIELD_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_CREATED_TIME_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus.ACTIVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus.DISABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PAGINATION_AFTER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PAGINATION_BEFORE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PARENT_ID_FILTER_PLACEHOLDER_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_ADD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REMOVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REPLACE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SW;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_ATTR_KEY_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_ATTR_VALUE_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_CREATED_TIME_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_DESCRIPTION_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_ID_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_LAST_MODIFIED_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_NAME_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_PARENT_ID_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_STATUS_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_TENANT_DOMAIN_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_TENANT_UUID_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_TYPE_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_ORGANIZATIONS_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_ORGANIZATIONS_EXIST_WITH_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_ORGANIZATIONS_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_IMMEDIATE_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_ORGANIZATION_ATTRIBUTE_KEY_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_ORGANIZATION_EXIST_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_ORGANIZATION_EXIST_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_SIBLING_ORGANIZATIONS_EXIST_WITH_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DELETE_ORGANIZATION_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DELETE_ORGANIZATION_ATTRIBUTES_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DELETE_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ALL_UM_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ALL_UM_ORG_ATTRIBUTES_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ANCESTORS_OF_GIVEN_ORG_INCLUDING_ITSELF;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ANCESTOR_ORGANIZATION_ID_WITH_DEPTH;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_BASIC_ORG_DETAILS_BY_ORG_IDS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_CHILD_ORGANIZATIONS_INCLUDING_ORG_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_CHILD_ORGANIZATION_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_CHILD_ORGANIZATION_IDS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_DEPTH_TO_ANCESTOR_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_IMMEDIATE_OR_ALL_CHILD_ORG_IDS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_INCLUDING_ORG_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_META_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_META_ATTRIBUTES_TAIL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_META_ATTRIBUTES_TAIL_MSSQL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_META_ATTRIBUTES_TAIL_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_TAIL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_TAIL_MSSQL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_TAIL_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_INCLUDING_ORG_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_TAIL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_TAIL_MSSQL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_TAIL_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_INCLUDING_ORG_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_TAIL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_TAIL_MSSQL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_TAIL_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_DEPTH_IN_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_DEPTH_IN_HIERARCHY_MSSQL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_DEPTH_IN_HIERARCHY_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_NAME_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_PERMISSIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_UUID_FROM_TENANT_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_UUID_FROM_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_PARENT_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_RELATIVE_ORG_DEPTH_BETWEEN_ORGANIZATIONS_IN_SAME_BRANCH;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_TENANT_DOMAIN_FROM_ORGANIZATION_UUID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_TENANT_UUID_FROM_ORGANIZATION_UUID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INNER_JOIN_UM_ORG_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_IMMEDIATE_ORGANIZATION_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_IMMEDIATE_ORGANIZATION_HIERARCHY_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_OTHER_ORGANIZATION_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_ROOT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_ROOT_ORGANIZATION_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.PATCH_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.PATCH_ORGANIZATION_CONCLUDE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.PERMISSION_LIST_PLACEHOLDER;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SET_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_AUDIENCE_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_CREATED_TIME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_DEPTH;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_KEY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_PARENT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_VALUE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_LIMIT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.UPDATE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.UPDATE_ORGANIZATION_ATTRIBUTE_VALUE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.UPDATE_ORGANIZATION_LAST_MODIFIED;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.USER_NAME_LIST_PLACEHOLDER;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getAllowedPermissions;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getAuthenticatedUsername;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getOrganizationUserInvitationPrimaryUserDomain;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getUserId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.isMSSqlDB;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.isOracleDB;

/**
 * Organization management dao implementation.
 */
public class OrganizationManagementDAOImpl implements OrganizationManagementDAO {

    private static final Log LOG = LogFactory.getLog(OrganizationManagementDAOImpl.class);
    private static final Calendar CALENDAR = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    @Override
    public void addOrganization(Organization organization) throws
            OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_ORGANIZATION, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organization.getId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, organization.getName());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_DESCRIPTION, organization.getDescription());
                    namedPreparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                            Timestamp.from(organization.getCreated()), CALENDAR);
                    namedPreparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                            Timestamp.from(organization.getLastModified()), CALENDAR);
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_STATUS, organization.getStatus());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organization.getParent().getId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_TYPE, organization.getType());
                }, organization, false);
                if (CollectionUtils.isNotEmpty(organization.getAttributes())) {
                    addOrganizationAttributes(organization);
                }
                if (isOracleDB()) {
                    addOrganizationHierarchy(INSERT_IMMEDIATE_ORGANIZATION_HIERARCHY_ORACLE, organization);
                } else {
                    addOrganizationHierarchy(INSERT_IMMEDIATE_ORGANIZATION_HIERARCHY, organization);
                }
                addOrganizationHierarchy(INSERT_OTHER_ORGANIZATION_HIERARCHY, organization);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_ADDING_ORGANIZATION, e);
        }
    }

    private void addOrganizationAttributes(Organization organization) throws OrganizationManagementServerException {

        String organizationId = organization.getId();
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(INSERT_ATTRIBUTE, (namedPreparedStatement -> {
                    for (OrganizationAttribute attribute : organization.getAttributes()) {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_KEY, attribute.getKey());
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_VALUE, attribute.getValue());
                        namedPreparedStatement.addBatch();
                    }
                }), organizationId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_ADDING_ORGANIZATION_ATTRIBUTE, e);
        }
    }

    private void addOrganizationHierarchy(String query, Organization organization)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(query, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organization.getId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organization.getParent().getId());
                }, null, false);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_ADDING_ORGANIZATION_HIERARCHY_DATA, e);
        }
    }

    @Override
    public boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementServerException {

        return isOrganizationExist(organizationName, CHECK_ORGANIZATION_EXIST_BY_NAME,
                DB_SCHEMA_COLUMN_NAME_NAME, ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_NAME);
    }

    @Override
    public boolean isOrganizationExistById(String organizationId) throws OrganizationManagementServerException {

        return isOrganizationExist(organizationId, CHECK_ORGANIZATION_EXIST_BY_ID,
                DB_SCHEMA_COLUMN_NAME_ID, ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_ID);
    }

    private boolean isOrganizationExist(String organization, String checkOrganizationExistQuery,
                                        String dbSchemaColumnNameId,
                                        OrganizationManagementConstants.ErrorMessages errorMessage)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            int orgCount = namedJdbcTemplate.fetchSingleRecord(checkOrganizationExistQuery,
                    (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement ->
                            namedPreparedStatement.setString(dbSchemaColumnNameId, organization));
            return orgCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(errorMessage, e, organization);
        }
    }

    @Override
    public String getOrganizationIdByName(String organizationName) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_ID_BY_NAME,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_ID_COLUMN), namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, organizationName));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_ID_BY_NAME, e, organizationName);
        }
    }

    @Override
    public Optional<String> getOrganizationNameById(String organizationId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        String organizationName;
        try {
            organizationName = namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_NAME_BY_ID,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_NAME_COLUMN), namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId));
            return Optional.ofNullable(organizationName);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_NAME_BY_ID, e, organizationId);
        }
    }

    @Override
    public Organization getOrganization(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        List<OrganizationRowDataCollector> organizationRowDataCollectors;
        try {
            organizationRowDataCollectors = namedJdbcTemplate
                    .executeQuery(GET_ORGANIZATION_BY_ID,
                            (resultSet, rowNumber) -> {
                                OrganizationRowDataCollector collector = new OrganizationRowDataCollector();
                                collector.setId(organizationId);
                                collector.setName(resultSet.getString(VIEW_NAME_COLUMN));
                                collector.setDescription(resultSet.getString(VIEW_DESCRIPTION_COLUMN));
                                collector.setType(resultSet.getString(VIEW_TYPE_COLUMN));
                                collector.setParentId(resultSet.getString(VIEW_PARENT_ID_COLUMN));
                                collector.setLastModified(resultSet.getTimestamp(VIEW_LAST_MODIFIED_COLUMN, CALENDAR)
                                        .toInstant());
                                collector.setCreated(resultSet.getTimestamp(VIEW_CREATED_TIME_COLUMN, CALENDAR)
                                        .toInstant());
                                collector.setStatus(resultSet.getString(VIEW_STATUS_COLUMN));
                                collector.setAttributeKey(resultSet.getString(VIEW_ATTR_KEY_COLUMN));
                                collector.setAttributeValue(resultSet.getString(VIEW_ATTR_VALUE_COLUMN));
                                return collector;
                            },
                            namedPreparedStatement ->
                                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId));
            return (organizationRowDataCollectors == null || organizationRowDataCollectors.size() == 0) ?
                    null : buildOrganizationFromRawData(organizationRowDataCollectors);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_BY_ID, e, organizationId);
        }
    }

    @Override
    public List<BasicOrganization> getOrganizations(boolean recursive, Integer limit, String organizationId,
                                                    String sortOrder, List<ExpressionNode> expressionNodes,
                                                    List<ExpressionNode> parentIdExpressionNodes)
            throws OrganizationManagementServerException {

        return getOrganizationsBasicInfo(false, recursive, limit, organizationId, sortOrder,
                                    expressionNodes, parentIdExpressionNodes, null);
    }

    @Override
    public List<Organization> getOrganizationsList(boolean recursive, Integer limit, String organizationId,
                                                   String sortOrder, List<ExpressionNode> expressionNodes,
                                                   List<ExpressionNode> parentIdExpressionNodes)
            throws OrganizationManagementServerException {

        return getOrganizationsList(false, recursive, limit, organizationId, sortOrder,
                                expressionNodes, parentIdExpressionNodes, null);
    }

    @Override
    public List<BasicOrganization> getUserAuthorizedOrganizations(boolean recursive, Integer limit,
                                                                  String organizationId, String sortOrder,
                                                                  List<ExpressionNode> expressionNodes,
                                                                  List<ExpressionNode> parentIdExpressionNodes,
                                                                  String applicationAudience)
            throws OrganizationManagementServerException {

        return getOrganizationsBasicInfo(true, recursive, limit, organizationId, sortOrder,
                                    expressionNodes, parentIdExpressionNodes, applicationAudience);
    }

    @Override
    public void deleteOrganization(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            // Delete organization from UM_ORG table and cascade the deletion to the other table.
            namedJdbcTemplate.executeUpdate(DELETE_ORGANIZATION_BY_ID, namedPreparedStatement ->
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_DELETING_ORGANIZATION, e, organizationId);
        }
    }

    @Override
    public boolean hasChildOrganizations(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            List<Integer> childOrganizationIds = namedJdbcTemplate.executeQuery(CHECK_CHILD_ORGANIZATIONS_EXIST,
                    (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId));
            return childOrganizationIds.get(0) > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS, e, organizationId);
        }
    }

    @Override
    public boolean isSiblingOrganizationExistWithName(String organizationName, String parentOrgId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            List<Integer> childOrganizationIds =
                    namedJdbcTemplate.executeQuery(CHECK_SIBLING_ORGANIZATIONS_EXIST_WITH_NAME,
                            (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement -> {
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, organizationName);
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, parentOrgId);
                            });
            return childOrganizationIds.get(0) > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_SIBLING_ORGANIZATION_BY_NAME, e, parentOrgId);
        }
    }

    @Override
    public boolean isChildOrganizationExistWithName(String organizationName, String rootOrgId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            List<Integer> childOrganizationIds =
                    namedJdbcTemplate.executeQuery(CHECK_CHILD_ORGANIZATIONS_EXIST_WITH_NAME,
                            (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement -> {
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, organizationName);
                                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, rootOrgId);
                            });
            return childOrganizationIds.get(0) > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_CHILD_ORGANIZATION_BY_NAME, e, rootOrgId);
        }
    }

    @Override
    public void patchOrganization(String organizationId, Instant lastModifiedInstant,
                                  List<PatchOperation> patchOperations) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                for (PatchOperation patchOperation : patchOperations) {
                    if (patchOperation.getPath().startsWith(PATCH_PATH_ORG_ATTRIBUTES)) {
                        patchOrganizationAttribute(organizationId, patchOperation);
                    } else {
                        patchOrganizationField(organizationId, patchOperation);
                    }
                }
                updateLastModifiedTime(organizationId, lastModifiedInstant);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_PATCHING_ORGANIZATION, e, organizationId);
        }
    }

    @Override
    public void updateOrganization(String organizationId, Organization organization) throws
            OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeUpdate(UPDATE_ORGANIZATION, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, organization.getName());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_DESCRIPTION, organization.getDescription());
                    namedPreparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                            Timestamp.from(organization.getLastModified()), CALENDAR);
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_STATUS, organization.getStatus());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                });
                deleteOrganizationAttributes(organizationId);
                if (CollectionUtils.isNotEmpty(organization.getAttributes())) {
                    addOrganizationAttributes(organization);
                }
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_UPDATING_ORGANIZATION, e, organizationId);
        }
    }

    @Override
    public boolean isAttributeExistByKey(String organizationId, String attributeKey)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            int attrCount = namedJdbcTemplate.fetchSingleRecord(CHECK_ORGANIZATION_ATTRIBUTE_KEY_EXIST,
                    (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_KEY, attributeKey);
                    });
            return attrCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_ORGANIZATION_ATTRIBUTE_KEY_EXIST, e, attributeKey,
                    organizationId);
        }
    }

    @Override
    public List<BasicOrganization> getChildOrganizations(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        String sqlStmt = String.format(GET_CHILD_ORGANIZATIONS_INCLUDING_ORG_HANDLE, recursive ? "> 0" : "= 1");
        try {
            return namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        BasicOrganization organization = new BasicOrganization();
                        organization.setId(resultSet.getString(1));
                        organization.setName(resultSet.getString(2));
                        organization.setCreated(resultSet.getTimestamp(3).toString());
                        organization.setOrganizationHandle(resultSet.getString(4));
                        return organization;
                    },
                    namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS, e, organizationId);
        }
    }

    @Override
    public List<OrganizationNode> getChildOrganizationGraph(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        String sqlStmt = String.format(GET_CHILD_ORGANIZATION_HIERARCHY, recursive ? "> 0" : "= 1");
        List<OrganizationNode> rawResults;
        try {
            rawResults = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        String id = resultSet.getString(1);
                        String name = resultSet.getString(2);
                        String parentId = resultSet.getString(3);
                        String created = resultSet.getTimestamp(4).toString();
                        String handle = resultSet.getString(5);
                        return new OrganizationNode(id, name, created, handle, parentId);
                    },
                    namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId));

        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS, e, organizationId);
        }

        if (CollectionUtils.isEmpty(rawResults)) {
            return new ArrayList<>(); // No children found.
        }

        // --- Build the graph ---
        Map<String, OrganizationNode> nodeMap = new HashMap<>();

        // First pass: Create all node objects.
        for (OrganizationNode node : rawResults) {
            nodeMap.put(node.getId(), node);
        }
        // Second pass: Link children to parents.
        List<OrganizationNode> topLevelNodes = new ArrayList<>(); // Nodes that are direct children of 'organizationId'.
        for (OrganizationNode data : rawResults) {
            OrganizationNode currentNode = nodeMap.get(data.getId()); // Should always exist from the first pass.

            // Check if the parent from the DB result (data.parentId) exists in our map.
            OrganizationNode parentNode = nodeMap.get(data.getParentId());

            if (parentNode != null) {
                // If the parent node exists within the fetched results, link it.
                parentNode.addChild(currentNode);
            } else if (data.getParentId().equals(organizationId)) {
                // If the parent is the initial organizationId we queried for,
                // this is a top-level node for our result list.
                topLevelNodes.add(currentNode);
            }
            /* If parentNode is null and data.parentId is not the root organizationId,
             it means the parent is outside the scope of the current query result
             (e.g., querying a sub-subtree non-recursively). This is expected.*/
        }

        /* If recursive was false, topLevelNodes is already correct.
         If recursive was true, the logic above correctly identifies the direct children
         of 'organizationId' based on the parentId field fetched from the DB. */

        return topLevelNodes;
    }

    @Override
    public List<String> getChildOrganizationIds(String organizationId, boolean recursive)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        String sqlStmt = String.format(GET_IMMEDIATE_OR_ALL_CHILD_ORG_IDS, recursive ? "> 0" : "= 1");
        try {
            return namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS, e, organizationId);
        }
    }

    @Override
    public List<String> getChildOrganizationIds(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(GET_CHILD_ORGANIZATION_IDS,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS, e, organizationId);
        }
    }

    @Override
    public boolean hasActiveChildOrganizations(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            List<Integer> activeChildOrganizations = namedJdbcTemplate.executeQuery(CHECK_CHILD_ORGANIZATIONS_STATUS,
                    (resultSet, rowNumber) -> resultSet.getInt(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_STATUS, ACTIVE.toString());
                    });
            return activeChildOrganizations.get(0) > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_ACTIVE_CHILD_ORGANIZATIONS, e, organizationId);
        }
    }

    @Override
    public boolean isParentOrganizationDisabled(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            String status = namedJdbcTemplate.fetchSingleRecord(GET_PARENT_ORGANIZATION_STATUS,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_STATUS_COLUMN), namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId));
            return StringUtils.equals(status, DISABLED.toString());
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_PARENT_ORGANIZATION_STATUS, e, organizationId);
        }
    }

    @Override
    public String getOrganizationStatus(String organizationId) throws
            OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_STATUS,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_STATUS_COLUMN), namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_STATUS, e, organizationId);
        }
    }

    @Override
    public String getOrganizationType(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_TYPE,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_TYPE_COLUMN),
                    namedPreparedStatement -> namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID,
                            organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_TYPE, e, organizationId);
        }
    }

    @Override
    public List<String> getOrganizationPermissions(String organizationId, String userId)
            throws OrganizationManagementServerException {

        String permissionPlaceholder = "PERMISSION_";
        List<String> permissionPlaceholders = new ArrayList<>();

        List<String> allowedPermissions = getAllowedPermissions(BASE_ORGANIZATION_PERMISSION);
        allowedPermissions.addAll(ALL_ORGANIZATION_PERMISSIONS);

        // Constructing the placeholders required to hold the permission strings in the named prepared statement.
        for (int i = 1; i <= allowedPermissions.size(); i++) {
            permissionPlaceholders.add(":" + permissionPlaceholder + i + ";");
        }
        String placeholder = String.join(", ", permissionPlaceholders);
        String sqlStmt = GET_ORGANIZATION_PERMISSIONS.replace(PERMISSION_LIST_PLACEHOLDER, placeholder);

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        List<String> resourceIds;
        try {
            resourceIds = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userId);
                        int index = 1;
                        for (String allowedPermission : allowedPermissions) {
                            namedPreparedStatement.setString(permissionPlaceholder + index, allowedPermission);
                            index++;
                        }
                    });

            List<String> assignedPermissions = new ArrayList<>();

            for (String resourceId : resourceIds) {
                if (ALL_ORGANIZATION_PERMISSIONS.contains(resourceId)) {
                    assignedPermissions.add(resourceId);
                } else {
                    return ALL_ORGANIZATION_PERMISSIONS;
                }
            }
            return assignedPermissions;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_PERMISSIONS,
                    e, organizationId, userId);
        }
    }

    @Override
    public String getAssociatedTenantUUIDForOrganization(String organizationId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_TENANT_UUID_FROM_ORGANIZATION_UUID,
                    (resultSet, rowNumber) -> resultSet.getString(VIEW_TENANT_UUID_COLUMN),
                    namedPreparedStatement -> namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID,
                            organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_TENANT_UUID, e, organizationId);
        }
    }

    @Override
    public String resolveTenantDomain(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_TENANT_DOMAIN_FROM_ORGANIZATION_UUID,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID,
                            organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RESOLVING_TENANT_DOMAIN_FROM_ORGANIZATION_DOMAIN, e,
                    organizationId);
        }
    }

    private List<Organization> getOrganizationsList(boolean authorizedSubOrgsOnly, boolean recursive,
                                                    Integer limit, String organizationId, String sortOrder,
                                                    List<ExpressionNode> expressionNodes,
                                                    List<ExpressionNode> parentIdExpressionNodes,
                                                    String applicationAudience)
            throws OrganizationManagementServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes, ORGANIZATION_CREATED_TIME_FIELD);
        FilterQueryBuilder parentIdFilterQueryBuilder = buildParentIdFilterQuery(parentIdExpressionNodes);

        String userID =  getUserId();
        String sqlStmt = prepareGetOrganizationQuery(authorizedSubOrgsOnly, recursive, sortOrder, applicationAudience,
                        filterQueryBuilder, parentIdFilterQueryBuilder, userID);
        boolean isFilteringMetaAttributes = filterQueryBuilder.getMetaAttributeCount() > 0;

        if (isFilteringMetaAttributes) {
            if (isOracleDB()) {
                sqlStmt = String.format(GET_ALL_UM_ORG_ATTRIBUTES_ORACLE, sqlStmt);
            } else {
                sqlStmt = String.format(GET_ALL_UM_ORG_ATTRIBUTES, sqlStmt);
            }
        }
        List<Organization> organizations;
        Map<String, Organization> organizationMap = new HashMap<>();
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            organizations = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        if (isFilteringMetaAttributes) {
                            String orgId = resultSet.getString(1);
                            Organization organization = organizationMap.get(orgId);
                            if (organization == null) {
                                organization = buildOrganization(resultSet);
                                organizationMap.put(orgId, organization);
                            }
                            OrganizationAttribute organizationAttribute = new OrganizationAttribute();
                            organizationAttribute.setKey(resultSet.getString(5));
                            organizationAttribute.setValue(resultSet.getString(6));
                            organization.setAttribute(organizationAttribute);
                            return organization;
                        }
                        return buildOrganization(resultSet);
                    },
                    namedPreparedStatement -> setPreparedStatementParams(namedPreparedStatement, organizationId,
                            applicationAudience, limit, filterQueryBuilder, parentIdFilterQueryBuilder, userID));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS, e);
        }
        if (isFilteringMetaAttributes) {
            return new ArrayList<>(organizationMap.values());
        }
        return organizations;
    }

    private Organization buildOrganization(ResultSet resultSet) throws SQLException {

        Organization organization = new Organization();
        organization.setId(resultSet.getString(1));
        organization.setName(resultSet.getString(2));
        organization.setCreated(resultSet.getTimestamp(3).toInstant());
        organization.setStatus(resultSet.getString(4));
        organization.setOrganizationHandle(resultSet.getString(5));
        return organization;
    }

    private List<BasicOrganization> getOrganizationsBasicInfo(boolean authorizedSubOrgsOnly, boolean recursive,
                                                              Integer limit, String organizationId, String sortOrder,
                                                              List<ExpressionNode> expressionNodes,
                                                              List<ExpressionNode> parentIdExpressionNodes,
                                                              String applicationAudience)
            throws OrganizationManagementServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes, ORGANIZATION_CREATED_TIME_FIELD);
        FilterQueryBuilder parentIdFilterQueryBuilder = buildParentIdFilterQuery(parentIdExpressionNodes);

        String userID =  getUserId();
        String sqlStmt = prepareGetOrganizationQuery(authorizedSubOrgsOnly, recursive, sortOrder,
                applicationAudience, filterQueryBuilder, parentIdFilterQueryBuilder, userID);

        List<BasicOrganization> organizations;
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            organizations = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        BasicOrganization organization = new BasicOrganization();
                        organization.setId(resultSet.getString(1));
                        organization.setName(resultSet.getString(2));
                        organization.setCreated(resultSet.getTimestamp(3).toString());
                        organization.setStatus(resultSet.getString(4));
                        organization.setOrganizationHandle(resultSet.getString(5));
                        return organization;
                    },
                    namedPreparedStatement -> setPreparedStatementParams(namedPreparedStatement, organizationId,
                            applicationAudience, limit, filterQueryBuilder, parentIdFilterQueryBuilder, userID));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS, e);
        }
        return organizations;
    }

    private void deleteOrganizationAttributes(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeUpdate(DELETE_ORGANIZATION_ATTRIBUTES_BY_ID, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_DELETING_ORGANIZATION_ATTRIBUTES, e, organizationId);
        }
    }

    private String buildQueryOrganization(String path) {

        // Updating a primary field
        StringBuilder sb = new StringBuilder();
        sb.append(PATCH_ORGANIZATION);
        if (path.equals(PATCH_PATH_ORG_NAME)) {
            sb.append(VIEW_NAME_COLUMN);
        } else if (path.equals(PATCH_PATH_ORG_DESCRIPTION)) {
            sb.append(VIEW_DESCRIPTION_COLUMN);
        } else if (path.equals(PATCH_PATH_ORG_STATUS)) {
            sb.append(VIEW_STATUS_COLUMN);
        }
        sb.append(PATCH_ORGANIZATION_CONCLUDE);
        String query = sb.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Organization patch query : " + query);
        }
        return query;
    }

    private void patchOrganizationField(String organizationId, PatchOperation patchOperation)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeUpdate(buildQueryOrganization(patchOperation.getPath()), namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_VALUE,
                            patchOperation.getOp().equals(PATCH_OP_REMOVE) ? null : patchOperation.getValue());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_PATCHING_ORGANIZATION, e, organizationId);
        }
    }

    private void patchOrganizationAttribute(String organizationId, PatchOperation patchOperation)
            throws OrganizationManagementServerException {

        String attributeKey = patchOperation.getPath().replace(PATCH_PATH_ORG_ATTRIBUTES, "").trim();
        patchOperation.setPath(attributeKey);
        if (patchOperation.getOp().equals(PATCH_OP_ADD)) {
            insertOrganizationAttribute(organizationId, patchOperation);
        } else if (patchOperation.getOp().equals(PATCH_OP_REPLACE)) {
            updateOrganizationAttribute(organizationId, patchOperation);
        } else {
            deleteOrganizationAttribute(organizationId, patchOperation);
        }
    }

    private void insertOrganizationAttribute(String organizationId, PatchOperation patchOperation)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_ATTRIBUTE, (namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_KEY, patchOperation.getPath());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_VALUE, patchOperation.getValue());
                }), null, false);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_PATCHING_ORGANIZATION_ADD_ATTRIBUTE, e);
        }
    }

    private void updateOrganizationAttribute(String organizationId, PatchOperation patchOperation)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeUpdate(UPDATE_ORGANIZATION_ATTRIBUTE_VALUE, preparedStatement -> {
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_VALUE, patchOperation.getValue());
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_KEY, patchOperation.getPath());
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_PATCHING_ORGANIZATION_UPDATE_ATTRIBUTE, e, organizationId);
        }
    }

    private void deleteOrganizationAttribute(String organizationId, PatchOperation patchOperation)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.executeUpdate(DELETE_ORGANIZATION_ATTRIBUTE, namedPreparedStatement -> {
                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_KEY, patchOperation.getPath());
            });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_PATCHING_ORGANIZATION_DELETE_ATTRIBUTE, e,
                    patchOperation.getPath(), organizationId);
        }
    }

    private void updateLastModifiedTime(String organizationId, Instant lastModifiedInstant) throws
            OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeUpdate(UPDATE_ORGANIZATION_LAST_MODIFIED, preparedStatement -> {
                    preparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                            Timestamp.from(lastModifiedInstant), CALENDAR);
                    preparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_PATCHING_ORGANIZATION, e, organizationId);
        }
    }

    private Organization buildOrganizationFromRawData(List<OrganizationRowDataCollector>
                                                              organizationRowDataCollectors) {

        Organization organization = new Organization();
        organizationRowDataCollectors.forEach(collector -> {
            if (organization.getId() == null) {
                organization.setId(collector.getId());
                organization.setName(collector.getName());
                organization.setDescription(collector.getDescription());
                organization.setType(collector.getType());
                organization.getParent().setId(collector.getParentId());
                organization.setCreated(collector.getCreated());
                organization.setLastModified(collector.getLastModified());
                organization.setStatus(collector.getStatus());
            }
            List<OrganizationAttribute> attributes = organization.getAttributes();
            List<String> attributeKeys = new ArrayList<>();
            for (OrganizationAttribute attribute : attributes) {
                attributeKeys.add(attribute.getKey());
            }
            if (collector.getAttributeKey() != null && !attributeKeys.contains(collector.getAttributeKey())) {
                organization.getAttributes().add(new OrganizationAttribute(collector.getAttributeKey(),
                        collector.getAttributeValue()));
            }
        });
        return organization;
    }

    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder,
                                   String attributeUsedForCursor)
            throws OrganizationManagementServerException {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeValue = expressionNode.getAttributeValue();
                if (attributeValue.equalsIgnoreCase(PAGINATION_AFTER) ||
                        attributeValue.equalsIgnoreCase(PAGINATION_BEFORE)) {
                    attributeValue = attributeUsedForCursor;
                }
                String attributeName = ATTRIBUTE_COLUMN_MAP.get(attributeValue);

                if (attributeValue.startsWith(ORGANIZATION_ATTRIBUTES_FIELD_PREFIX)) {
                    attributeName = handleViewAttrKeyColumn(expressionNode, filterQueryBuilder);
                }
                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    if (VIEW_CREATED_TIME_COLUMN.equals(attributeName) ||
                            VIEW_LAST_MODIFIED_COLUMN.equals(attributeName)) {
                        filterQueryBuilder.addTimestampFilterAttributes(FILTER_PLACEHOLDER_PREFIX);
                    }
                    switch (operation) {
                        case EQ: {
                            equalFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case SW: {
                            startWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case EW: {
                            endWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case CO: {
                            containsFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case GE: {
                            greaterThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case LE: {
                            lessThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case GT: {
                            greaterThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case LT: {
                            lessThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
            } else {
                filterQueryBuilder.setFilterQuery(filter.toString());
            }
        }
    }

    private void appendFilterQueryForParentId(FilterQueryBuilder filterQueryBuilder,
                                              List<ExpressionNode> expressionNodes) {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        for (ExpressionNode expressionNode : expressionNodes) {
            String operation = expressionNode.getOperation();
            String value = expressionNode.getValue();
            if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(operation)) {
                switch (operation) {
                    case EQ: {
                        equalFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case SW: {
                        startWithFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case EW: {
                        endWithFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case CO: {
                        containsFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case GE: {
                        greaterThanOrEqualFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case LE: {
                        lessThanOrEqualFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case GT: {
                        greaterThanFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    case LT: {
                        lessThanFilterBuilderForParentId(count, value, filter, filterQueryBuilder);
                        count++;
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
        if (StringUtils.isBlank(filter.toString())) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            filterQueryBuilder.setFilterQuery(filter.toString());
        }
    }

    @Override
    public boolean isChildOfParent(String organizationId, String parentId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();

        try {
            int attrCount = namedJdbcTemplate.fetchSingleRecord(CHECK_CHILD_OF_PARENT,
                    (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, parentId);
                    });
            return attrCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_IF_CHILD_OF_PARENT, e, organizationId, parentId);
        }
    }

    @Override
    public boolean isImmediateChildOfParent(String organizationId, String parentId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();

        try {
            int attrCount = namedJdbcTemplate.fetchSingleRecord(CHECK_IMMEDIATE_CHILD_OF_PARENT,
                    (resultSet, rowNumber) -> resultSet.getInt(1), namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, parentId);
                    });
            return attrCount > 0;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_IF_IMMEDIATE_CHILD_OF_PARENT, e, organizationId,
                    parentId);
        }
    }

    @Override
    public Optional<String> resolveOrganizationId(String tenantDomain) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            String organizationId = namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_UUID_FROM_TENANT_DOMAIN,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_TENANT_DOMAIN,
                            tenantDomain));
            return Optional.ofNullable(organizationId);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_DOMAIN_FROM_TENANT_DOMAIN, e,
                    tenantDomain);
        }
    }

    @Override
    public Optional<String> resolveOrganizationIdFromTenantId(String tenantId)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            String organizationId = namedJdbcTemplate.fetchSingleRecord(GET_ORGANIZATION_UUID_FROM_TENANT_ID,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_TENANT_ID,
                            tenantId));
            return Optional.ofNullable(organizationId);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_ID_FROM_TENANT_ID, e,
                    tenantId);
        }
    }

    @Override
    public List<String> getAncestorOrganizationIds(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            return namedJdbcTemplate.executeQuery(GET_ANCESTORS_OF_GIVEN_ORG_INCLUDING_ITSELF,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_WHILE_RETRIEVING_ANCESTORS, e, organizationId);
        }
    }

    @Override
    public List<Organization> getOrganizationsByName(String organizationName)
            throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        List<Organization> organizations;
        try {
            organizations = namedJdbcTemplate.executeQuery(GET_ORGANIZATIONS_BY_NAME,
                    (resultSet, rowNumber) -> {
                        Organization organization = new Organization();
                        organization.setId(resultSet.getString(VIEW_ID_COLUMN));
                        organization.setName(resultSet.getString(VIEW_NAME_COLUMN));
                        organization.setDescription(resultSet.getString(VIEW_DESCRIPTION_COLUMN));
                        return organization;
                    },
                    namedPreparedStatement -> namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME,
                            organizationName));
            return organizations;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_BY_NAME, e, organizationName);
        }
    }

    @Override
    public int getOrganizationDepthInHierarchy(String organizationId) throws OrganizationManagementServerException {

        String getOrgDepthQuery = GET_ORGANIZATION_DEPTH_IN_HIERARCHY;
        if (isOracleDB()) {
            getOrgDepthQuery = GET_ORGANIZATION_DEPTH_IN_HIERARCHY_ORACLE;
        } else if (isMSSqlDB()) {
            getOrgDepthQuery = GET_ORGANIZATION_DEPTH_IN_HIERARCHY_MSSQL;
        }

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            String depth = namedJdbcTemplate.fetchSingleRecord(getOrgDepthQuery,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                    });
            if (depth == null) {
                return -1;
            }
            return Integer.parseInt(depth);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DEPTH, e, organizationId);
        }
    }

    @Override
    public int getRelativeDepthBetweenOrganizationsInSameBranch(String firstOrgId, String secondOrgId)
            throws OrganizationManagementServerException {

        if (StringUtils.equals(firstOrgId, secondOrgId)) {
            return 0;
        }
        try {
            String depth = Utils.getNewTemplate().fetchSingleRecord(
                    GET_RELATIVE_ORG_DEPTH_BETWEEN_ORGANIZATIONS_IN_SAME_BRANCH,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, firstOrgId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, secondOrgId);
                    });
            if (depth == null) {
                return -1;
            }
            return Integer.parseInt(depth);
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_RELATIVE_ORGANIZATION_DEPTH_IN_BRANCH, e,
                    firstOrgId, secondOrgId);
        }
    }

    @Override
    public boolean isAncestorOrg(String currentOrgId, String parentOrgId)
            throws OrganizationManagementServerException {

        if (StringUtils.equals(currentOrgId, parentOrgId)) {
            return false;
        }
        try {
            String depth = Utils.getNewTemplate().fetchSingleRecord(
                    GET_DEPTH_TO_ANCESTOR_ORGANIZATION,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, parentOrgId);
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, currentOrgId);
                    });
            return depth != null;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_IS_ANCESTOR_ORGANIZATION, e,
                    parentOrgId, currentOrgId);
        }
    }

    @Override
    public String getAnAncestorOrganizationIdInGivenDepth(String organizationId, int depth)
            throws OrganizationManagementServerException {

        try {
            return Utils.getNewTemplate().fetchSingleRecord(
                    GET_ANCESTOR_ORGANIZATION_ID_WITH_DEPTH,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        namedPreparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_DEPTH, depth);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_GET_ANCESTOR_IN_DEPTH, e, String.valueOf(depth), organizationId);
        }
    }

    @Override
    public void addRootOrganization(Organization rootOrganization) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_ROOT_ORGANIZATION, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, rootOrganization.getId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_NAME, rootOrganization.getName());
                    namedPreparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_CREATED_TIME,
                            Timestamp.from(rootOrganization.getCreated()), CALENDAR);
                    namedPreparedStatement.setTimeStamp(DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED,
                            Timestamp.from(rootOrganization.getLastModified()), CALENDAR);
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_STATUS, rootOrganization.getStatus());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_TYPE, rootOrganization.getType());
                }, rootOrganization, false);
                if (CollectionUtils.isNotEmpty(rootOrganization.getAttributes())) {
                    addOrganizationAttributes(rootOrganization);
                }
                createRootOrganizationHierarchy(rootOrganization.getId());
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_ADDING_ORGANIZATION, e);
        }
    }

    private void createRootOrganizationHierarchy(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_ROOT_ORGANIZATION_HIERARCHY, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                }, null, false);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_ERROR_ADDING_ORGANIZATION_HIERARCHY_DATA, e);
        }
    }

    @Override
    public List<String> getOrganizationsMetaAttributes(boolean recursive, Integer limit, String organizationId,
                                                       String sortOrder, List<ExpressionNode> expressionNodes)
            throws OrganizationManagementServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes, ORGANIZATION_ATTRIBUTES_FIELD);
        String sqlStmt = getOrgMetaAttributesSqlStmt(recursive, sortOrder, filterQueryBuilder);

        List<String> organizationMetaAttributes;
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            organizationMetaAttributes = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement -> setPreparedStatementParams(namedPreparedStatement, organizationId,
                            null, limit, filterQueryBuilder, new FilterQueryBuilder(), null));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_META_ATTRIBUTES, e);
        }
        return organizationMetaAttributes;
    }

    @Override
    public Map<String, BasicOrganization> getBasicOrganizationDetailsByOrgIDs(List<String> orgIds)
            throws OrganizationManagementException {

        String placeholders = orgIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format(GET_BASIC_ORG_DETAILS_BY_ORG_IDS, placeholders);
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();

        try {
            Map<String, BasicOrganization> basicOrganizationDetailsMap = new HashMap<>();
            List<String> invalidOrgIds = new ArrayList<>();

            namedJdbcTemplate.executeQuery(sql, (resultSet, rowNumber) -> {
                    String orgId = resultSet.getString(VIEW_ID_COLUMN);
                    String orgName = resultSet.getString(VIEW_NAME_COLUMN);

                if (StringUtils.isNotBlank(orgName)) {
                    BasicOrganization basicOrganization = new BasicOrganization();
                    basicOrganization.setId(orgId);
                    basicOrganization.setName(orgName);
                    basicOrganization.setStatus(resultSet.getString(VIEW_STATUS_COLUMN));
                    basicOrganization.setCreated(resultSet.getString(VIEW_CREATED_TIME_COLUMN));
                    basicOrganization.setOrganizationHandle(resultSet.getString(VIEW_TENANT_DOMAIN_COLUMN));
                    basicOrganizationDetailsMap.put(orgId, basicOrganization);
                } else {
                    invalidOrgIds.add(orgId);
                }
                return null;
                },
                namedPreparedStatement -> {
                    int index = 1;
                    for (String orgId : orgIds) {
                        namedPreparedStatement.setString(index++, orgId);
                    }
                });
            if (LOG.isDebugEnabled() && !invalidOrgIds.isEmpty()) {
                String invalidIdsString = String.join(", ", invalidOrgIds);
                LOG.debug("Invalid org ids found while getOrganizationNamesByIds: " + invalidIdsString);
            }
            return basicOrganizationDetailsMap;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_DETAILS_BY_ORGANIZATION_IDS, e);
        }
    }

    private static String getOrgMetaAttributesSqlStmt(boolean recursive, String sortOrder,
                                                      FilterQueryBuilder filterQueryBuilder)
            throws OrganizationManagementServerException {

        String orgSqlStmtTail;
        if (isOracleDB()) {
            orgSqlStmtTail = GET_ORGANIZATIONS_META_ATTRIBUTES_TAIL_ORACLE;
        } else if (isMSSqlDB()) {
            orgSqlStmtTail = GET_ORGANIZATIONS_META_ATTRIBUTES_TAIL_MSSQL;
        } else {
            orgSqlStmtTail = GET_ORGANIZATIONS_META_ATTRIBUTES_TAIL;
        }
        return GET_ORGANIZATIONS_META_ATTRIBUTES + filterQueryBuilder.getFilterQuery() +
                String.format(orgSqlStmtTail, SET_ID, recursive ? "> 0" : "= 1", sortOrder);
    }

    private void equalFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" = :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void startWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                        FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value + "%");
    }

    private void endWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                      FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, "%" + value);
    }

    private void containsFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, "%" + value + "%");
    }

    private void greaterThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder)
            throws OrganizationManagementServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " >= CAST(:%s%s; AS DATETIME) AND "
                : " >= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder)
            throws OrganizationManagementServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " <= CAST(:%s%s; AS DATETIME) AND "
                : " <= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder)
            throws OrganizationManagementServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " > CAST(:%s%s; AS DATETIME) AND "
                : " > :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder)
            throws OrganizationManagementServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " < CAST(:%s%s; AS DATETIME) AND "
                : " < :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void equalFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                               FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" = :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void startWithFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                   FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, value + "%");
    }

    private void endWithFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, "%" + value);
    }

    private void containsFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                  FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, "%" + value + "%");
    }

    private void greaterThanOrEqualFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                            FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" >= :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanOrEqualFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                         FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" <= :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                  FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" < :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void greaterThanFilterBuilderForParentId(int count, String value, StringBuilder filter,
                                                     FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" > :%s%s; ", PARENT_ID_FILTER_PLACEHOLDER_PREFIX, count);
        appendFilterForParentId(filterString, filter);
        filterQueryBuilder.setFilterAttributeValue(PARENT_ID_FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void appendFilterForParentId(String filterString, StringBuilder filter) {

        if (StringUtils.isBlank(filter.toString())) {
            filter.append(OrganizationManagementConstants.VIEW_ID_COLUMN).append(filterString);
        } else {
            filter.append(" AND ").append(OrganizationManagementConstants.VIEW_ID_COLUMN).append(filterString);
        }
    }

    private boolean isDateTimeAndMSSql(String attributeName) throws OrganizationManagementServerException {

        return (VIEW_CREATED_TIME_COLUMN.equals(attributeName) || VIEW_LAST_MODIFIED_COLUMN.equals(attributeName))
                && isMSSqlDB();
    }

    private FilterQueryBuilder buildFilterQuery(List<ExpressionNode> expressionNodes, String attributeUsedForCursor)
            throws OrganizationManagementServerException {

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNodes, filterQueryBuilder, attributeUsedForCursor);
        return filterQueryBuilder;
    }

    private FilterQueryBuilder buildParentIdFilterQuery(List<ExpressionNode> parentIdExpressionNodes) {

        FilterQueryBuilder parentIdFilterQueryBuilder = new FilterQueryBuilder();
        appendFilterQueryForParentId(parentIdFilterQueryBuilder, parentIdExpressionNodes);
        return parentIdFilterQueryBuilder;
    }

    private String prepareGetOrganizationQuery(boolean authorizedSubOrgsOnly, boolean recursive, String sortOrder,
                                               String applicationAudience, FilterQueryBuilder filterQueryBuilder,
                                               FilterQueryBuilder parentIdFilterQueryBuilder, String userID)
            throws OrganizationManagementServerException {

        String sqlStmt = getOrgSqlStatement(authorizedSubOrgsOnly, applicationAudience);
        String getOrgSqlStmtTail = getOrgSqlStmtTail(authorizedSubOrgsOnly, applicationAudience);

        if (filterQueryBuilder.getMetaAttributeCount() > 0) {
            for (String placeholder : filterQueryBuilder.getMetaAttributePlaceholders()) {
                sqlStmt = sqlStmt.replace("WHERE",
                        String.format(INNER_JOIN_UM_ORG_ATTRIBUTE, placeholder, placeholder));
            }
        }
        sqlStmt = appendFilterQueries(sqlStmt, filterQueryBuilder, parentIdFilterQueryBuilder, getOrgSqlStmtTail,
                recursive, sortOrder);

        /* The shared user parent user might be created with user ID if there is business user with same name
        in the child organization. */
        return sqlStmt.replace(USER_NAME_LIST_PLACEHOLDER, Stream.of(DB_SCHEMA_COLUMN_NAME_USER_NAME,
                        DB_SCHEMA_COLUMN_NAME_USER_ID).map(name -> ":" + name + ";").collect(Collectors.joining(",")));
    }

    private String getOrgSqlStatement(boolean authorizedSubOrgsOnly, String applicationAudience) {

        if (authorizedSubOrgsOnly) {
            if (StringUtils.isNotBlank(applicationAudience)) {
                return GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_INCLUDING_ORG_HANDLE;
            }
            return GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_INCLUDING_ORG_HANDLE;
        }
        return GET_ORGANIZATIONS_INCLUDING_ORG_HANDLE;
    }

    private String getOrgSqlStmtTail(boolean authorizedSubOrgsOnly, String applicationAudience)
            throws OrganizationManagementServerException {

        String getOrgSqlStmtTail = authorizedSubOrgsOnly
                ? StringUtils.isNotBlank(applicationAudience)
                ? GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_TAIL
                : GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_TAIL
                : GET_ORGANIZATIONS_TAIL;

        if (isOracleDB()) {
            getOrgSqlStmtTail = authorizedSubOrgsOnly
                    ? StringUtils.isNotBlank(applicationAudience)
                    ? GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_TAIL_ORACLE
                    : GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_TAIL_ORACLE
                    : GET_ORGANIZATIONS_TAIL_ORACLE;
        } else if (isMSSqlDB()) {
            getOrgSqlStmtTail = authorizedSubOrgsOnly
                    ? StringUtils.isNotBlank(applicationAudience)
                    ? GET_ORGANIZATIONS_WITH_USER_ROLE_ASSOCIATIONS_TAIL_MSSQL
                    : GET_ORGANIZATIONS_WITH_USER_ASSOCIATIONS_TAIL_MSSQL
                    : GET_ORGANIZATIONS_TAIL_MSSQL;
        }
        return getOrgSqlStmtTail;
    }

    private String appendFilterQueries(String sqlStmt, FilterQueryBuilder filterQueryBuilder,
                                       FilterQueryBuilder parentIdFilterQueryBuilder, String getOrgSqlStmtTail,
                                       boolean recursive, String sortOrder) {

        String parentIdFilterQuery = parentIdFilterQueryBuilder.getFilterQuery();
        if (StringUtils.isBlank(parentIdFilterQuery)) {
            return sqlStmt + filterQueryBuilder.getFilterQuery() +
                    String.format(getOrgSqlStmtTail, SET_ID, recursive ? "> 0" : "= 1", sortOrder);
        }
        return sqlStmt + filterQueryBuilder.getFilterQuery() +
                String.format(getOrgSqlStmtTail, parentIdFilterQuery, recursive ? "> 0" : "= 1", sortOrder);
    }

    private void setPreparedStatementParams(NamedPreparedStatement namedPreparedStatement, String organizationId,
                                            String applicationAudience, Integer limit,
                                            FilterQueryBuilder filterQueryBuilder,
                                            FilterQueryBuilder parentIdFilterQueryBuilder, String userID)
            throws SQLException {

        String username = getAuthenticatedUsername();
        if (StringUtils.isNotEmpty(username)) {
            username = UserCoreUtil.removeDomainFromName(username);
        }
        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_NAME, username);
        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, userID);
        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_DOMAIN,
                                    getOrganizationUserInvitationPrimaryUserDomain());
        if (parentIdFilterQueryBuilder.getFilterAttributeValue().isEmpty()) {
            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
        }
        if (StringUtils.isNotBlank(applicationAudience)) {
            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_AUDIENCE_ID, applicationAudience);
        }
        setFilterAttributes(namedPreparedStatement, parentIdFilterQueryBuilder.getFilterAttributeValue(),
                filterQueryBuilder.getFilterAttributeValue(), filterQueryBuilder.getTimestampFilterAttributes());
        namedPreparedStatement.setInt(DB_SCHEMA_LIMIT, limit);
    }

    private void setFilterAttributes(NamedPreparedStatement namedPreparedStatement,
                                     Map<String, String> parentIdFilterAttributeValueMap,
                                     Map<String, String> filterAttributeValue, List<String> timestampTypeAttributes)
            throws SQLException {

        for (Map.Entry<String, String> entry : parentIdFilterAttributeValueMap.entrySet()) {
            namedPreparedStatement.setString(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : filterAttributeValue.entrySet()) {
            if (timestampTypeAttributes.contains(entry.getKey())) {
                namedPreparedStatement.setTimeStamp(entry.getKey(), Timestamp.valueOf(entry.getValue()), null);
            } else {
                namedPreparedStatement.setString(entry.getKey(), entry.getValue());
            }
        }
    }

    private String handleViewAttrKeyColumn(ExpressionNode expressionNode, FilterQueryBuilder filterQueryBuilder) {

        String placeholder = filterQueryBuilder.generateMetaAttributePlaceholder();
        String attributeValue = expressionNode.getAttributeValue();
        String subAttributeName = StringUtils.substringAfter(attributeValue, ORGANIZATION_ATTRIBUTES_FIELD + ".");

        return placeholder + "." + VIEW_ATTR_KEY_COLUMN + " = '" + subAttributeName + "' AND " + placeholder + "."
                + VIEW_ATTR_VALUE_COLUMN;
    }
}
