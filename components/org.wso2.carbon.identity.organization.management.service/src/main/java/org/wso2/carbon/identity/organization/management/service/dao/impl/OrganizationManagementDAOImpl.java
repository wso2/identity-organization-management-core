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

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.FilterQueryBuilder;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.service.util.Utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.GET_ORGANIZATION_ID_BY_NAME;
import static org.wso2.carbon.identity.organization.management.authz.service.util.OrganizationManagementAuthzUtil.getAllowedPermissions;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ALL_ORGANIZATION_PERMISSIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ATTRIBUTE_COLUMN_MAP;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.BASE_ORGANIZATION_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CO;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.EQ;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.EW;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ACTIVE_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_IF_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_IF_IMMEDIATE_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_ATTRIBUTE_KEY_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DELETING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DELETING_ORGANIZATION_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION_ADD_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION_DELETE_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_PATCHING_ORGANIZATION_UPDATE_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RESOLVING_ORGANIZATION_DOMAIN_FROM_TENANT_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RESOLVING_TENANT_DOMAIN_FROM_ORGANIZATION_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_ID_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_NAME_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_PERMISSIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_PARENT_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_UUID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_UPDATING_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_ANCESTORS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.FILTER_PLACEHOLDER_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.GE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.GT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.LE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.LT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus.ACTIVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus.DISABLED;
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
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_ORGANIZATION_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_PARENT_ID_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_STATUS_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_TENANT_UUID_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.VIEW_TYPE_COLUMN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_ORGANIZATIONS_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_CHILD_ORGANIZATIONS_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_IMMEDIATE_CHILD_OF_PARENT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_ORGANIZATION_ATTRIBUTE_KEY_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_ORGANIZATION_EXIST_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.CHECK_ORGANIZATION_EXIST_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DELETE_ORGANIZATION_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DELETE_ORGANIZATION_ATTRIBUTES_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.DELETE_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ANCESTORS_OF_GIVEN_ORG_INCLUDING_ITSELF;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_TAIL;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATIONS_TAIL_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_NAME_BY_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_PERMISSIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_ORGANIZATION_UUID_FROM_TENANT_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_PARENT_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_TENANT_DOMAIN_FROM_ORGANIZATION_UUID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.GET_TENANT_UUID_FROM_ORGANIZATION_UUID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_IMMEDIATE_ORGANIZATION_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_IMMEDIATE_ORGANIZATION_HIERARCHY_ORACLE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.INSERT_OTHER_ORGANIZATION_HIERARCHY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.PATCH_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.PATCH_ORGANIZATION_CONCLUDE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.PERMISSION_LIST_PLACEHOLDER;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SET_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_CREATED_TIME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_KEY;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_PARENT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_DOMAIN;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_VALUE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_LIMIT;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.UPDATE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.UPDATE_ORGANIZATION_ATTRIBUTE_VALUE;
import static org.wso2.carbon.identity.organization.management.service.constant.SQLConstants.UPDATE_ORGANIZATION_LAST_MODIFIED;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getUserId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;
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

    private void addOrganizationAttributes(Organization organization) throws TransactionException {

        String organizationId = organization.getId();
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
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
    }

    private void addOrganizationHierarchy(String query, Organization organization) throws TransactionException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        namedJdbcTemplate.withTransaction(template -> {
            template.executeInsert(query, namedPreparedStatement -> {
                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organization.getId());
                namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organization.getParent().getId());
            }, null, false);
            return null;
        });
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

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNodes, filterQueryBuilder);
        Map<String, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();

        FilterQueryBuilder parentIdFilterQueryBuilder = new FilterQueryBuilder();
        appendFilterQueryForParentId(parentIdFilterQueryBuilder, parentIdExpressionNodes);
        Map<String, String> parentIdFilterAttributeValueMap = parentIdFilterQueryBuilder.getFilterAttributeValue();
        String parentIdFilterQuery = parentIdFilterQueryBuilder.getFilterQuery();

        String sqlStmt;
        String getOrgSqlStmtTail = isOracleDB() ? GET_ORGANIZATIONS_TAIL_ORACLE : GET_ORGANIZATIONS_TAIL;

        if (StringUtils.isBlank(parentIdFilterQuery)) {
            sqlStmt = GET_ORGANIZATIONS + filterQueryBuilder.getFilterQuery() +
                    String.format(getOrgSqlStmtTail, SET_ID, recursive ? "> 0" : "= 1", sortOrder);
        } else {
            sqlStmt = GET_ORGANIZATIONS + filterQueryBuilder.getFilterQuery() +
                    String.format(getOrgSqlStmtTail, parentIdFilterQuery, recursive ? "> 0" : "= 1",
                            sortOrder);
        }

        String permissionPlaceholder = "PERMISSION_";
        List<String> permissions = getAllowedPermissions(VIEW_ORGANIZATION_PERMISSION);
        List<String> permissionPlaceholders = new ArrayList<>();
        // Constructing the placeholders required to hold the permission strings in the named prepared statement.
        for (int i = 1; i <= permissions.size(); i++) {
            permissionPlaceholders.add(":" + permissionPlaceholder + i + ";");
        }
        String placeholder = String.join(", ", permissionPlaceholders);
        sqlStmt = sqlStmt.replace(PERMISSION_LIST_PLACEHOLDER, placeholder);

        List<BasicOrganization> organizations;
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            organizations = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        BasicOrganization organization = new BasicOrganization();
                        organization.setId(resultSet.getString(1));
                        organization.setName(resultSet.getString(2));
                        organization.setCreated(resultSet.getTimestamp(3).toString());
                        return organization;
                    },
                    namedPreparedStatement -> {
                        namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_USER_ID, getUserId());
                        if (parentIdFilterAttributeValueMap.isEmpty()) {
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ID, organizationId);
                        }
                        for (Map.Entry<String, String> entry : parentIdFilterAttributeValueMap.entrySet()) {
                            namedPreparedStatement.setString(entry.getKey(), entry.getValue());
                        }
                        for (Map.Entry<String, String> entry : filterAttributeValue.entrySet()) {
                            namedPreparedStatement.setString(entry.getKey(), entry.getValue());
                        }
                        int index = 1;
                        for (String permission : permissions) {
                            namedPreparedStatement.setString(permissionPlaceholder + index, permission);
                            index++;
                        }
                        namedPreparedStatement.setInt(DB_SCHEMA_LIMIT, limit);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_ORGANIZATIONS, e);
        }
        return organizations;
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
    public List<String> getChildOrganizationIds(String organizationId) throws OrganizationManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        List<String> childOrganizationIds;
        try {
            childOrganizationIds = namedJdbcTemplate.executeQuery(GET_CHILD_ORGANIZATIONS,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    namedPreparedStatement ->
                            namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_PARENT_ID, organizationId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_ERROR_RETRIEVING_CHILD_ORGANIZATIONS, e, organizationId);
        }
        return childOrganizationIds;
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

    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder) {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeName = ATTRIBUTE_COLUMN_MAP.get(expressionNode.getAttributeValue());
                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
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
                                                 FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" >= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" <= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" > :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" < :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
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
}
