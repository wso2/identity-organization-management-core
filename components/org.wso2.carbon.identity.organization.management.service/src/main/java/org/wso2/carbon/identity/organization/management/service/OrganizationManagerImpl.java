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

package org.wso2.carbon.identity.organization.management.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.CacheBackedOrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.filter.ExpressionNode;
import org.wso2.carbon.identity.organization.management.service.filter.FilterTreeBuilder;
import org.wso2.carbon.identity.organization.management.service.filter.Node;
import org.wso2.carbon.identity.organization.management.service.filter.OperationNode;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.listener.OrganizationManagerListener;
import org.wso2.carbon.identity.organization.management.service.model.AncestorOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.ChildOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationNode;
import org.wso2.carbon.identity.organization.management.service.model.ParentOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.service.model.TenantTypeOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.stratos.common.exception.TenantManagementClientException;
import org.wso2.carbon.stratos.common.exception.TenantMgtException;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.AND;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ASC_SORT_ORDER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CO;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATOR_EMAIL;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATOR_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATOR_USERNAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.DESC_SORT_ORDER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.EW;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ACTIVE_CHILD_ORGANIZATIONS_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_KEY_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_VALUE_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_CREATE_REQUEST_PARENT_ORGANIZATION_IS_DISABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_DUPLICATE_ATTRIBUTE_KEYS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_CREATING_ROOT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DEACTIVATING_ROOT_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_VALIDATING_ORGANIZATION_OWNER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_EXISTING_ORGANIZATION_HANDLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_TIMESTAMP_FORMAT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_NEW_ORGANIZATION_VERSION_CONFIGURED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_PATCH_OPERATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_NO_PARENT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_HAS_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_ID_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NAME_CONTAINS_HTML_CONTENT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NAME_EXIST_IN_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NAME_RESERVED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_OWNER_NOT_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_TYPE_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_VERSION_UPDATE_NOT_ALLOWED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PARENT_ORGANIZATION_IS_DISABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_OPERATION_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_ATTRIBUTE_KEY_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_INVALID_PATH;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_MANDATORY_FIELD_INVALID_OPERATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_PATH_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_REMOVE_NON_EXISTING_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_REPLACE_NON_EXISTING_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_PATCH_REQUEST_VALUE_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_REQUIRED_FIELDS_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_RETRIEVING_ORGANIZATIONS_BY_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_SAME_ORG_NAME_ON_IMMEDIATE_SUB_ORGANIZATIONS_OF_PARENT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_SUPER_ORGANIZATION_RENAME_CONFLICT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_SUPER_ORG_DELETE_OR_DISABLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_SUPER_ORG_RENAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNAUTHORIZED_ORG_ACCESS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_COMPLEX_QUERY_IN_FILTER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_OPERATION_FOR_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ORGANIZATION_VERSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ATTRIBUTES_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ATTRIBUTES_FIELD_PREFIX;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_CREATED_TIME_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_DESCRIPTION_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_ID_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_LAST_MODIFIED_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_NAME_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_STATUS_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus.ACTIVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationStatus.DISABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.STRUCTURAL;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.OrganizationTypes.TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PAGINATION_AFTER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PAGINATION_BEFORE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PARENT_ID_FIELD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_ADD;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REMOVE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_OP_REPLACE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_ATTRIBUTES;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_DESCRIPTION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_NAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.PATCH_PATH_ORG_VERSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SW;
import static org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil.getSuperRootOrgName;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getAuthenticatedUsername;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getOrganizationId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getTenantDomain;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getUserId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleClientException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.hasHtmlContent;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.isSubOrganization;
import static org.wso2.carbon.stratos.common.constants.TenantConstants.ErrorMessage.ERROR_CODE_EXISTING_DOMAIN;

/**
 * This class implements the {@link OrganizationManager} interface.
 */
public class OrganizationManagerImpl implements OrganizationManager {

    private static final Log LOG = LogFactory.getLog(OrganizationManagerImpl.class);

    private final OrganizationManagementDAO organizationManagementDAO =
            new CacheBackedOrganizationManagementDAO(new OrganizationManagementDAOImpl());

    @Override
    public Organization addOrganization(Organization organization) throws OrganizationManagementException {

        validateAddOrganizationRequest(organization);
        validateParentOrganization(organization);
        validateOrgNameUniqueness(organization.getParent().getId(), organization.getName());
        setCreatedAndLastModifiedTime(organization);
        getListener().preAddOrganization(organization);
        setOrganizationOwnerInformation(organization);
        setOrganizationVersion(organization);
        organizationManagementDAO.addOrganization(organization);

        // Create a tenant for tenant type organization.
        if (organization instanceof TenantTypeOrganization) {
            String organizationHandle = organization.getOrganizationHandle();
            if (StringUtils.isBlank(organizationHandle)) {
                organizationHandle = ((TenantTypeOrganization) organization).getDomainName();
                if (StringUtils.isBlank(organizationHandle)) {
                    organizationHandle = organization.getId();
                }
                organization.setOrganizationHandle(organizationHandle);
            }
            createTenant(organizationHandle, organization);
        }
        try {
            getListener().postAddOrganization(organization);
        } catch (OrganizationManagementException e) {
            // Rollback created organization.
            try {
                deleteOrganization(organization.getId());
            } catch (OrganizationManagementException exception) {
                LOG.error("The server encountered an error while deleting the organization due to a rollback " +
                        "after a failed organization creation.", exception);
            }
            throw e;
        }

        resolveInheritedOrganizationVersion(organization);
        return organization;
    }

    @Override
    public boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementException {

        return organizationManagementDAO.isOrganizationExistByName(organizationName);
    }

    @Override
    public boolean isOrganizationExistByHandle(String organizationHandle) throws OrganizationManagementServerException {

        try {
            return !getTenantMgtService().isDomainAvailable(organizationHandle);
        } catch (TenantMgtException e) {
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_ORGANIZATION_EXIST_BY_HANDLE, e);
        }
    }

    @Override
    public boolean isOrganizationExistByNameInGivenHierarchy(String organizationName) {

        try {
            String orgId = resolveOrganizationId(getTenantDomain());
            validateOrgNameUniqueness(orgId, organizationName);
        } catch (OrganizationManagementException e) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOrganizationExistById(String organizationId) throws OrganizationManagementException {

        return organizationManagementDAO.isOrganizationExistById(organizationId);
    }

    @Override
    public String getOrganizationIdByName(String organizationName) throws OrganizationManagementException {

        return organizationManagementDAO.getOrganizationIdByName(organizationName);
    }

    @Override
    public String getOrganizationNameById(String organizationId) throws OrganizationManagementException {

        return organizationManagementDAO.getOrganizationNameById(organizationId).orElseThrow(
                () -> handleClientException(ERROR_CODE_INVALID_ORGANIZATION_ID, organizationId));
    }

    /**
     * @deprecated Use {@link #getOrganization(String, boolean, boolean, boolean)} instead.
     */
    @Deprecated
    @Override
    public Organization getOrganization(String organizationId, boolean showChildren, boolean includePermissions) throws
            OrganizationManagementException {

        return getOrganization(organizationId, showChildren, includePermissions, false);
    }

    @Override
    public Organization getOrganization(String organizationId, boolean showChildren, boolean includePermissions,
                                         boolean showAncestorOrganizations) throws OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        String requestInvokingOrganizationId = getOrganizationId();
        validateOrganizationAccess(requestInvokingOrganizationId, organizationId, true);
        getListener().preGetOrganization(organizationId.trim());

        Organization organization = organizationManagementDAO.getOrganization(organizationId.trim());
        if (organization == null) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }

        if (showChildren) {
            List<String> childOrganizationIds = organizationManagementDAO.getChildOrganizationIds(organizationId);
            if (CollectionUtils.isNotEmpty(childOrganizationIds)) {
                List<ChildOrganizationDO> childOrganizations = new ArrayList<>();
                for (String childOrganizationId : childOrganizationIds) {
                    ChildOrganizationDO childOrganization = new ChildOrganizationDO();
                    childOrganization.setId(childOrganizationId);
                    childOrganizations.add(childOrganization);
                }
                organization.setChildOrganizations(childOrganizations);
            }
        } else {
            organization.setChildOrganizations(null);
        }

        if (includePermissions) {
            List<String> permissions = organizationManagementDAO.getOrganizationPermissions(organizationId,
                    getUserId());
            if (CollectionUtils.isNotEmpty(permissions)) {
                organization.setPermissions(permissions);
            }
        }

        if (showAncestorOrganizations) {
            List<AncestorOrganizationDO> ancestorOrganizations =
                    getAncestorOrganizations(organizationId, requestInvokingOrganizationId);
            organization.setAncestors(ancestorOrganizations);
        }

        organization.setOrganizationHandle(resolveTenantDomain(organization.getId()));
        resolveInheritedOrganizationVersion(organization);
        getListener().postGetOrganization(organizationId.trim(), organization);
        return organization;
    }

    @Override
    public List<BasicOrganization> getChildOrganizations(String organizationId, boolean recursive)
            throws OrganizationManagementException {

        return organizationManagementDAO.getChildOrganizations(organizationId, recursive);
    }

    @Override
    public List<OrganizationNode> getChildOrganizationGraph(String organizationId, boolean recursive)
            throws OrganizationManagementException {

        return organizationManagementDAO.getChildOrganizationGraph(organizationId, recursive);
    }

    @Override
    public List<String> getChildOrganizationsIds(String organizationId, boolean recursive)
            throws OrganizationManagementException {

        return organizationManagementDAO.getChildOrganizationIds(organizationId, recursive);
    }

    @Override
    public List<String> getChildOrganizationsIds(String organizationId)
            throws OrganizationManagementException {

        return organizationManagementDAO.getChildOrganizationIds(organizationId);
    }

    /**
     * @deprecated Use {@link #getOrganizationsList(Integer, String, String, String, String, boolean)} instead.
     */
    @Deprecated
    @Override
    public List<BasicOrganization> getOrganizations(Integer limit, String after, String before, String sortOrder,
                                                    String filter, boolean recursive)
            throws OrganizationManagementException {

        return getOrganizationsBasicInfo(false, limit, after, before, sortOrder, filter, recursive,
                null);
    }

    @Override
    public List<Organization> getOrganizationsList(Integer limit, String after, String before, String sortOrder,
                                                    String filter, boolean recursive)
            throws OrganizationManagementException {

        return getOrganizationList(limit, after, before, sortOrder, filter, recursive);
    }

    @Override
    public List<BasicOrganization> getUserAuthorizedOrganizations(Integer limit, String after, String before,
                                                                  String sortOrder, String filter, boolean recursive)
            throws OrganizationManagementException {

        return getOrganizationsBasicInfo(true, limit, after, before, sortOrder, filter, recursive,
                null);
    }

    @Override
    public List<BasicOrganization> getUserAuthorizedOrganizations(Integer limit, String after, String before,
                                                                  String sortOrder, String filter, boolean recursive,
                                                                  String applicationAudience)
            throws OrganizationManagementException {

        return getOrganizationsBasicInfo(true, limit, after, before, sortOrder, filter, recursive,
                applicationAudience);
    }

    private List<BasicOrganization> getOrganizationsBasicInfo(boolean authorizedSubOrgsOnly, Integer limit,
                                                              String after, String before, String sortOrder,
                                                              String filter, boolean recursive,
                                                              String applicationAudience)
            throws OrganizationManagementException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, DESC_SORT_ORDER);
        List<ExpressionNode> filteringByParentIdExpressionNodes = getParentIdExpressionNodes(expressionNodes);
        String orgId = resolveOrganizationId(getTenantDomain());
        expressionNodes.removeAll(filteringByParentIdExpressionNodes);

        return authorizedSubOrgsOnly ?
                resolveInheritedBasicOrganizationVersions(
                        organizationManagementDAO.getUserAuthorizedOrganizations(recursive, limit, orgId, sortOrder,
                                    expressionNodes, filteringByParentIdExpressionNodes, applicationAudience)) :
                resolveInheritedBasicOrganizationVersions(
                        organizationManagementDAO.getOrganizations(recursive, limit, orgId, sortOrder, expressionNodes,
                                    filteringByParentIdExpressionNodes));
    }

    private List<Organization> getOrganizationList(Integer limit, String after, String before, String sortOrder,
                                                   String filter, boolean recursive)
            throws OrganizationManagementException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, DESC_SORT_ORDER);
        List<ExpressionNode> filteringByParentIdExpressionNodes = getParentIdExpressionNodes(expressionNodes);
        String orgId = resolveOrganizationId(getTenantDomain());
        expressionNodes.removeAll(filteringByParentIdExpressionNodes);

        return  resolveInheritedOrganizationVersions(organizationManagementDAO.getOrganizationsList(
                recursive, limit, orgId, sortOrder, expressionNodes, filteringByParentIdExpressionNodes));
    }

    @Override
    public String getOrganizationVersion(String organizationId) throws OrganizationManagementException {

        if (StringUtils.isEmpty(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }

        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        resolveInheritedOrganizationVersion(organization);
        return organization.getVersion();
    }

    private List<BasicOrganization> resolveInheritedBasicOrganizationVersions(
            List<BasicOrganization> basicOrganizationList) throws OrganizationManagementException {

        for (BasicOrganization basicOrganization : basicOrganizationList) {
            resolveInheritedBasicOrganizationVersion(basicOrganization);
        }

        return basicOrganizationList;
    }

    private void resolveInheritedBasicOrganizationVersion(BasicOrganization basicOrganization)
            throws OrganizationManagementException {

        if (OrganizationManagementUtil.isOrganization(resolveTenantDomain(basicOrganization.getId()))) {
            String primaryOrgId = getPrimaryOrganizationId(basicOrganization.getId());
            String primaryOrgVersion = organizationManagementDAO.getOrganization(primaryOrgId).getVersion();
            basicOrganization.setVersion(primaryOrgVersion);
        }
    }

    private List<Organization> resolveInheritedOrganizationVersions(List<Organization> organizationList)
            throws OrganizationManagementException {

        if (CollectionUtils.isEmpty(organizationList)) {
            return organizationList;
        }

        for (Organization organization : organizationList) {
            resolveInheritedOrganizationVersion(organization);
        }

        return organizationList;
    }

    private void resolveInheritedOrganizationVersion(Organization organization)
            throws OrganizationManagementException {

        if (OrganizationManagementUtil.isOrganization(resolveTenantDomain(organization.getId()))) {
            String primaryOrgId = getPrimaryOrganizationId(organization.getId());
            String primaryOrgVersion = organizationManagementDAO.getOrganization(primaryOrgId).getVersion();
            organization.setVersion(primaryOrgVersion);
        }
    }

    private List<ExpressionNode> getParentIdExpressionNodes(List<ExpressionNode> expressionNodes)
            throws OrganizationManagementException {

        List<ExpressionNode> filteringByParentIdExpressionNodes = new ArrayList<>();

        for (ExpressionNode expressionNode : expressionNodes) {
            String attributeValue = expressionNode.getAttributeValue();
            String operation = expressionNode.getOperation();
            if (StringUtils.equals(attributeValue, PARENT_ID_FIELD)) {
                filteringByParentIdExpressionNodes.add(expressionNode);
            } else if (StringUtils.equals(attributeValue, ORGANIZATION_CREATED_TIME_FIELD) ||
                    StringUtils.equals(attributeValue, ORGANIZATION_LAST_MODIFIED_FIELD)) {
                if (SW.equals(operation) || EW.equals(operation) || CO.equals(operation)) {
                    throw handleClientException(ERROR_CODE_UNSUPPORTED_FILTER_OPERATION_FOR_ATTRIBUTE, operation,
                            attributeValue);
                }
                try {
                    Timestamp.valueOf(expressionNode.getValue());
                } catch (IllegalArgumentException e) {
                    throw handleClientException(ERROR_CODE_INVALID_FILTER_TIMESTAMP_FORMAT);
                }
            }
        }
        return filteringByParentIdExpressionNodes;
    }

    @Override
    public void deleteOrganization(String organizationId) throws OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        String requestInvokingOrganizationId = getOrganizationId();
        validateOrganizationAccess(requestInvokingOrganizationId, organizationId, false);
        validateOrganizationDelete(organizationId);
        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        if (organization == null) {
            return;
        }
        getListener().preDeleteOrganization(organizationId);
        if (StringUtils.equals(TENANT.toString(), organization.getType())) {
            String tenantID = organizationManagementDAO.getAssociatedTenantUUIDForOrganization(organizationId);
            if (StringUtils.isNotBlank(tenantID)) {
                try {
                    getTenantMgtService().deactivateTenant(tenantID);
                } catch (TenantMgtException e) {
                    throw handleServerException(ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT, e, organizationId);
                }
            }
        }
        int organizationDepthInHierarchy = organizationManagementDAO.getOrganizationDepthInHierarchy(organizationId);
        organizationManagementDAO.deleteOrganization(organizationId);
        getListener().postDeleteOrganization(organizationId, organizationDepthInHierarchy);
    }

    @Override
    public Organization patchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        organizationId = organizationId.trim();
        String requestInvokingOrganizationId = getOrganizationId();
        validateOrganizationAccess(requestInvokingOrganizationId, organizationId, false);
        if (!isOrganizationExistById(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }
        validateOrganizationPatchOperations(patchOperations, organizationId, false);

        getListener().prePatchOrganization(organizationId, patchOperations);
        organizationManagementDAO.patchOrganization(organizationId, Instant.now(), patchOperations);
        patchTenantStatus(patchOperations, organizationId);

        getListener().postPatchOrganization(organizationId, patchOperations);

        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        organization.setOrganizationHandle(resolveTenantDomain(organization.getId()));
        resolveInheritedOrganizationVersion(organization);
        return organization;
    }

    @Override
    public Organization updateOrganization(String organizationId, String currentOrganizationName,
                                           Organization organization) throws OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        organizationId = organizationId.trim();
        String requestInvokingOrganizationId = getOrganizationId();
        validateOrganizationAccess(requestInvokingOrganizationId, organizationId, false);
        if (!isOrganizationExistById(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }

        validateUpdateOrganizationRequest(currentOrganizationName, organization);
        if (!currentOrganizationName.equals(organization.getName().trim())) {
            // If the PUT request has a different organization name, need to check the availability.
            validateOrgNameUniqueness(organization.getParent().getId(), organization.getName());
        }
        updateLastModifiedTime(organization);

        getListener().preUpdateOrganization(organizationId, organization);
        organizationManagementDAO.updateOrganization(organizationId, organization);

        Organization updatedOrganization = organizationManagementDAO.getOrganization(organizationId);
        updatedOrganization.setOrganizationHandle(resolveTenantDomain(organization.getId()));
        resolveInheritedOrganizationVersion(updatedOrganization);

        if (StringUtils.equals(TENANT.toString(), organization.getType())) {
            updateTenantStatus(organization.getStatus(), organizationId);
        }
        getListener().postUpdateOrganization(organizationId, organization);
        return updatedOrganization;
    }

    @Override
    public String resolveTenantDomain(String organizationId) throws OrganizationManagementException {

        if (StringUtils.isEmpty(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }
        if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
            // super tenant domain will be returned.
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return organizationManagementDAO.resolveTenantDomain(organizationId);
    }

    @Override
    public String resolveTenantId(String organizationId) throws OrganizationManagementException {

        return organizationManagementDAO.getAssociatedTenantUUIDForOrganization(organizationId);
    }

    @Override
    public String resolveOrganizationId(String tenantDomain) throws OrganizationManagementException {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenantDomain)) {
            return OrganizationManagementConstants.SUPER_ORG_ID;
        } else {
            return organizationManagementDAO.resolveOrganizationId(tenantDomain).orElseThrow(
                    () -> handleClientException(ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT, tenantDomain));
        }
    }

    @Override
    public String resolveOrganizationIdFromTenantId(String tenantId) throws OrganizationManagementException {

        return organizationManagementDAO.resolveOrganizationIdFromTenantId(tenantId).orElseThrow(
                () -> handleClientException(ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT_ID, tenantId));
    }

    @Override
    public List<String> getAncestorOrganizationIds(String organizationId) throws OrganizationManagementServerException {

        return organizationManagementDAO.getAncestorOrganizationIds(organizationId);
    }

    @Override
    public List<Organization> getOrganizationsByName(String organizationName)
            throws OrganizationManagementException {

        List<Organization> organizations = organizationManagementDAO.getOrganizationsByName(organizationName);
        if (CollectionUtils.isNotEmpty(organizations)) {
            return organizations;
        }
        throw handleClientException(ERROR_CODE_RETRIEVING_ORGANIZATIONS_BY_NAME, organizationName);
    }

    @Override
    public int getOrganizationDepthInHierarchy(String organizationId) throws OrganizationManagementServerException {

        return organizationManagementDAO.getOrganizationDepthInHierarchy(organizationId);
    }

    @Override
    public int getRelativeDepthBetweenOrganizationsInSameBranch(String firstOrgId, String secondOrgId)
            throws OrganizationManagementServerException {

        return organizationManagementDAO.getRelativeDepthBetweenOrganizationsInSameBranch(firstOrgId, secondOrgId);
    }

    @Override
    public String getParentOrganizationId(String organizationId) throws OrganizationManagementException {

        if (SUPER_ORG_ID.equals(organizationId)) {
            throw handleClientException(ERROR_CODE_NO_PARENT_ORG, organizationId);
        }
        String parentOrgId =
                organizationManagementDAO.getAnAncestorOrganizationIdInGivenDepth(organizationId, 1);
        if (StringUtils.isBlank(parentOrgId)) {
            throw handleClientException(ERROR_CODE_NO_PARENT_ORG, organizationId);
        }
        return parentOrgId;
    }

    @Override
    public boolean isPrimaryOrganization(String organizationId) throws OrganizationManagementServerException {

        return Utils.getSubOrgStartLevel() - 1 == getOrganizationDepthInHierarchy(organizationId);
    }

    @Override
    public String getPrimaryOrganizationId(String organizationId) throws OrganizationManagementServerException {

        List<String> ancestorOrgIds = getAncestorOrganizationIds(organizationId);
        // Primary organization is the parent level organization of the sub organization start level.
        int primaryOrgDepth = Utils.getSubOrgStartLevel() - 1;
        if (ancestorOrgIds != null && ancestorOrgIds.size() > primaryOrgDepth) {
            // Ancestor organization list is in reverse order. Hence, the primary organization index has to be derived.
            int primaryOrgIndex = ancestorOrgIds.size() - primaryOrgDepth - 1;
            return ancestorOrgIds.get(primaryOrgIndex);
        }
        return null;
    }

    @Override
    public Organization addRootOrganization(int tenantId, Organization organization)
            throws OrganizationManagementException {

        setCreatedAndLastModifiedTime(organization);
        organization.setType(TENANT.name());
        setOrganizationVersion(organization);
        try {
            organizationManagementDAO.addRootOrganization(organization);
            org.wso2.carbon.user.api.Tenant tenant = getRealmService().getTenantManager().getTenant(tenantId);
            tenant.setAssociatedOrganizationUUID(organization.getId());
            getRealmService().getTenantManager().updateTenant(tenant);
        }  catch (OrganizationManagementServerException | UserStoreException e) {
            deactivateTenant(tenantId);
            throw handleServerException(ERROR_CODE_ERROR_CREATING_ROOT_ORGANIZATION, e, String.valueOf(tenantId));
        }
        return organization;
    }

    @Override
    public List<String> getOrganizationsMetaAttributes(Integer limit, String after, String before, String sortOrder,
                                                       String filter, boolean recursive)
            throws OrganizationManagementException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before, ASC_SORT_ORDER);
        String orgId = resolveOrganizationId(getTenantDomain());
        return organizationManagementDAO.getOrganizationsMetaAttributes(recursive, limit, orgId, sortOrder,
                                                                    expressionNodes);
    }

    @Override
    public Map<String, BasicOrganization> getBasicOrganizationDetailsByOrgIDs(List<String> orgIdList)
            throws OrganizationManagementException {

        if (CollectionUtils.isEmpty(orgIdList)) {
            return Collections.emptyMap();
        }
        return organizationManagementDAO.getBasicOrganizationDetailsByOrgIDs(orgIdList);
    }

    @Override
    public Organization getSelfOrganization() throws OrganizationManagementException {

        String requestInvokingOrganizationId = getOrganizationId();
        if (StringUtils.isBlank(requestInvokingOrganizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        String organizationId = requestInvokingOrganizationId.trim();
        getListener().preGetOrganization(organizationId);

        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        if (organization == null) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }
        organization.setOrganizationHandle(resolveTenantDomain(organization.getId()));

        getListener().postGetOrganization(organizationId, organization);
        resolveInheritedOrganizationVersion(organization);
        return organization;
    }

    /**
     * Get the ancestors of the given organization up to the request initiated organization.
     *
     * @param organizationId        The organization id to get the ancestors.
     * @param requestInitiatedOrgId The organization id of the request initiator.
     * @return List of ancestor organizations up to the request initiated organization.
     * @throws OrganizationManagementServerException If an error occurs while retrieving the ancestor organizations.
     */
    private List<AncestorOrganizationDO> getAncestorOrganizations(String organizationId, String requestInitiatedOrgId)
            throws OrganizationManagementServerException {

        if (StringUtils.equals(organizationId, requestInitiatedOrgId)) {
            // If the organization id is same as the request initiated organization id, return an empty list.
            return Collections.emptyList();
        }
        List<AncestorOrganizationDO> ancestorOrganizations =
                organizationManagementDAO.getAncestorOrganizations(organizationId);
        /*
        Remove the organization which has depth less that Utils.getSubOrgStartLevel() - 1 to start the list from
        and adjust the depth of existing ones to start from 0.
         */
        if (CollectionUtils.isNotEmpty(ancestorOrganizations)) {
            int rootOrgLevel = Utils.getSubOrgStartLevel() - 1;
            ancestorOrganizations = ancestorOrganizations.stream()
                    .filter(ancestor -> ancestor.getDepth() >= rootOrgLevel)
                    .peek(ancestor -> ancestor.setDepth(ancestor.getDepth() - rootOrgLevel))
                    .collect(Collectors.toList());
        }
        // Remove if any org with less depth compared to requestInitiatedOrg id.
        if (StringUtils.isNotBlank(requestInitiatedOrgId)) {
            int index = ancestorOrganizations.stream()
                    .map(AncestorOrganizationDO::getId)
                    .collect(Collectors.toList())
                    .indexOf(requestInitiatedOrgId);
            if (index != -1) {
                ancestorOrganizations = ancestorOrganizations.subList(index, ancestorOrganizations.size());
            }
        }
        return ancestorOrganizations;
    }

    @Override
    public Organization patchSelfOrganization(List<PatchOperation> patchOperations)
            throws OrganizationManagementException {

        String requestInvokingOrganizationId = getOrganizationId();
        if (StringUtils.isBlank(requestInvokingOrganizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        String organizationId = requestInvokingOrganizationId.trim();
        if (!isOrganizationExistById(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }
        validateOrganizationPatchOperations(patchOperations, organizationId, true);

        getListener().prePatchOrganization(organizationId, patchOperations);
        organizationManagementDAO.patchOrganization(organizationId, Instant.now(), patchOperations);
        patchTenantStatus(patchOperations, organizationId);

        getListener().postPatchOrganization(organizationId, patchOperations);

        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        organization.setOrganizationHandle(resolveTenantDomain(organization.getId()));
        return organization;
    }

    @Override
    public MinimalOrganization getMinimalOrganization(String organizationId, String associatedTenantDomain)
            throws OrganizationManagementException {

        if (SUPER_ORG_ID.equals(organizationId)) {
            return new MinimalOrganization.Builder()
                    .id(SUPER_ORG_ID)
                    .name(SUPER)
                    .organizationHandle(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                    .depth(0)
                    .build();
        }
        return organizationManagementDAO.getMinimalOrganization(organizationId, associatedTenantDomain);
    }

    private void updateTenantStatus(String status, String organizationId) throws OrganizationManagementServerException {

        String tenantDomain = organizationManagementDAO.resolveTenantDomain(organizationId);
        if (StringUtils.equals(ACTIVE.toString(), status)) {
            try {
                getTenantMgtService().activateTenant(getRealmService().getTenantManager().getTenantId(tenantDomain));
            } catch (TenantMgtException | UserStoreException e) {
                throw handleServerException(ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT, e, organizationId);
            }
        } else {
            try {
                getTenantMgtService().deactivateTenant(getRealmService().getTenantManager().getTenantId(tenantDomain));
            } catch (TenantMgtException | UserStoreException e) {
                throw handleServerException(ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT, e, organizationId);
            }
        }
    }

    private void updateLastModifiedTime(Organization organization) {

        Instant now = Instant.now();
        organization.setLastModified(now);
    }

    private void validateOrganizationDelete(String organizationId) throws OrganizationManagementException {

        if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
            throw handleClientException(ERROR_CODE_SUPER_ORG_DELETE_OR_DISABLE, organizationId);
        }
        if (organizationManagementDAO.hasChildOrganizations(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_HAS_CHILD_ORGANIZATIONS, organizationId);
        }
    }

    private void setCreatedAndLastModifiedTime(Organization organization) {

        Instant now = Instant.now();
        organization.setCreated(now);
        organization.setLastModified(now);
    }

    private void validateAddOrganizationRequest(Organization organization) throws OrganizationManagementException {

        validateAddOrganizationRequiredFields(organization);
        validateOrganizationNameField(organization.getName(), organization.getId());
        validateOrganizationAttributes(organization.getAttributes());
        validateAddOrganizationType(organization);
    }

    private void validateOrgNameUniqueness(String parentOrgId, String organizationName)
            throws OrganizationManagementException {

        int depthOfChildOrg = organizationManagementDAO.getOrganizationDepthInHierarchy(parentOrgId) + 1;
        if (!isSubOrganization(depthOfChildOrg)) {
            if (organizationManagementDAO.isSiblingOrganizationExistWithName(organizationName, parentOrgId)) {
                throw handleClientException(ERROR_CODE_SAME_ORG_NAME_ON_IMMEDIATE_SUB_ORGANIZATIONS_OF_PARENT_ORG,
                        organizationName, parentOrgId);
            }
            return;
        }

        List<String> ancestorOrgIds = organizationManagementDAO.getAncestorOrganizationIds(parentOrgId);
        // Root org is the parent level org of the sub organization start level.
        int depthOfRootOrg = Utils.getSubOrgStartLevel() - 1;
        if (ancestorOrgIds != null && ancestorOrgIds.size() > depthOfRootOrg) {
            // Ancestor organization list is in reverse order. Hence the root org index has to be derived.
            int rootOrgIndex = ancestorOrgIds.size() - depthOfRootOrg - 1;
            String rootOrgId = ancestorOrgIds.get(rootOrgIndex);
            if (organizationManagementDAO.isChildOrganizationExistWithName(organizationName, rootOrgId)) {
                throw handleClientException(ERROR_CODE_ORGANIZATION_NAME_EXIST_IN_CHILD_ORGANIZATIONS, rootOrgId);
            }
        }
    }

    private void validateAddOrganizationType(Organization organization) throws OrganizationManagementClientException {

        String organizationType = organization.getType();
        if (StringUtils.isBlank(organizationType)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_TYPE_UNDEFINED);
        }

        if (!StringUtils.equals(STRUCTURAL.toString(), organizationType) &&
                !StringUtils.equals(TENANT.toString(), organizationType)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION_TYPE);
        }
    }

    private void validateAddOrganizationParentStatus(String parentId) throws OrganizationManagementException {

        String parentStatus = organizationManagementDAO.getOrganizationStatus(parentId);
        if (!StringUtils.equals(ACTIVE.toString(), parentStatus)) {
            throw handleClientException(ERROR_CODE_CREATE_REQUEST_PARENT_ORGANIZATION_IS_DISABLED, parentId);
        }
    }

    private void validateAddOrganizationRequiredFields(Organization organization) throws
            OrganizationManagementClientException {

        validateOrganizationRequiredFieldName(organization.getName());
    }

    private void validateUpdateOrganizationRequiredFields(Organization organization) throws
            OrganizationManagementClientException {

        validateOrganizationRequiredFieldName(organization.getName());
        validateOrganizationRequiredFieldStatus(organization.getStatus());
    }

    private void validateOrganizationRequiredFieldName(String organizationName) throws
            OrganizationManagementClientException {

        if (StringUtils.isBlank(organizationName)) {
            throw handleClientException(ERROR_CODE_REQUIRED_FIELDS_MISSING, ORGANIZATION_NAME_FIELD);
        }
    }

    private void validateOrganizationRequiredFieldStatus(String organizationStatus) throws
            OrganizationManagementClientException {

        if (StringUtils.isBlank(organizationStatus)) {
            throw handleClientException(ERROR_CODE_REQUIRED_FIELDS_MISSING, ORGANIZATION_STATUS_FIELD);
        }
    }

    private void validateOrganizationAttributes(List<OrganizationAttribute> organizationAttributes) throws
            OrganizationManagementClientException {

        if (organizationAttributes != null) {
            for (OrganizationAttribute attribute : organizationAttributes) {
                String attributeKey = attribute.getKey();
                String attributeValue = attribute.getValue();

                if (StringUtils.isBlank(attributeKey)) {
                    throw handleClientException(ERROR_CODE_ATTRIBUTE_KEY_MISSING);
                }
                if (StringUtils.isBlank(attributeValue)) {
                    throw handleClientException(ERROR_CODE_ATTRIBUTE_VALUE_MISSING);
                }
                attribute.setKey(attributeKey.trim());
                attribute.setValue(attributeValue.trim());
            }

            // Check if attribute keys are duplicated.
            Set<String> tempSet = organizationAttributes.stream().map(OrganizationAttribute::getKey)
                    .collect(Collectors.toSet());
            if (organizationAttributes.size() > tempSet.size()) {
                throw handleClientException(ERROR_CODE_DUPLICATE_ATTRIBUTE_KEYS);
            }
        }
    }

    private void validateOrganizationNameField(String organizationName, String organizationId)
            throws OrganizationManagementException {

        if (hasHtmlContent(organizationName)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_NAME_CONTAINS_HTML_CONTENT);
        }
        if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
            if (organizationManagementDAO.isOrganizationExistWithName(organizationName)) {
                throw handleClientException(ERROR_CODE_SUPER_ORGANIZATION_RENAME_CONFLICT, organizationId);
            }
            return;
        }
        String superRootOrgName = getSuperRootOrgName();
        if (StringUtils.equalsIgnoreCase(superRootOrgName, organizationName) ||
                StringUtils.equalsIgnoreCase(SUPER, organizationName)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_NAME_RESERVED, organizationName);
        }
    }

    private void validateParentOrganization(Organization organization) throws OrganizationManagementException {

        // Child organizations are only allowed to be created from the immediate parent organization.
        String authorizedOrganization = getOrganizationId();
        if (StringUtils.isEmpty(authorizedOrganization)) {
            authorizedOrganization = resolveOrganizationId(getTenantDomain());
        }

        ParentOrganizationDO parentOrganization = organization.getParent();
        String parentId = parentOrganization.getId();
        if (StringUtils.isBlank(parentId)) {
            parentId = authorizedOrganization;
        } else {
            parentId = parentId.trim();
        }

        /*
        For parentId an alias as 'Super' is supported. This indicates that the organization should be created as an
        immediate child of the super organization.
         */
        if (SUPER.equals(parentId)) {
            parentId = SUPER_ORG_ID;
        }

        if (!StringUtils.equals(authorizedOrganization, parentId)) {
            throw handleClientException(ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION, parentId);
        }

        validateAddOrganizationParentStatus(parentId);
        parentOrganization.setId(parentId);
    }

    private void validateUpdateOrganizationRequest(String currentOrganizationName, Organization organization)
            throws OrganizationManagementException {

        validateUpdateOrganizationRequiredFields(organization);
        validateOrganizationStatusUpdate(organization.getStatus(), organization.getId());
        validateOrganizationVersionPut(organization);
        String newOrganizationName = organization.getName().trim();
        if (StringUtils.equals(SUPER, currentOrganizationName)) {
            throw handleClientException(ERROR_CODE_SUPER_ORG_RENAME, organization.getId());
        }
        // Check if the organization name is reserved.
        if (!StringUtils.equals(currentOrganizationName, newOrganizationName)) {
            validateOrganizationNameField(newOrganizationName, organization.getId());
        }
        organization.setName(newOrganizationName);

        validateOrganizationAttributes(organization.getAttributes());
    }

    private void validateOrganizationPatchOperations(List<PatchOperation> patchOperations, String organizationId,
                                                     boolean isSelfOrganizationUpdate)
            throws OrganizationManagementException {

        for (PatchOperation patchOperation : patchOperations) {

            if (StringUtils.isBlank(patchOperation.getOp())) {
                throw handleClientException(ERROR_CODE_PATCH_OPERATION_UNDEFINED, organizationId);
            }
            String op = patchOperation.getOp().trim();
            if (!(PATCH_OP_ADD.equals(op) || PATCH_OP_REMOVE.equals(op) || PATCH_OP_REPLACE.equals(op))) {
                throw handleClientException(ERROR_CODE_INVALID_PATCH_OPERATION, op);
            }

            if (StringUtils.isBlank(patchOperation.getPath())) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_PATH_UNDEFINED);
            }
            String path = patchOperation.getPath().trim();

            String value;
            if (StringUtils.isBlank(patchOperation.getValue()) && !PATCH_OP_REMOVE.equals(op)) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_VALUE_UNDEFINED);
            } else {
                value = patchOperation.getValue() != null ? patchOperation.getValue().trim() : "";
            }

            if (!op.equals(PATCH_OP_REPLACE) && !(path.equals(PATCH_PATH_ORG_DESCRIPTION) ||
                    path.startsWith(PATCH_PATH_ORG_ATTRIBUTES))) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_MANDATORY_FIELD_INVALID_OPERATION, op, path);
            }

            if (isSelfOrganizationUpdate) {
                validatePatchForSelfOrgUpdate(path, value, organizationId);
            } else {
                validatePatchForOrgUpdate(path, op, value, organizationId);
            }

            if (path.equals(PATCH_PATH_ORG_NAME)) {
                validateOrganizationNameField(value, organizationId);
                Organization organization = organizationManagementDAO.getOrganization(organizationId);
                if (!organization.getName().equals(value)) {
                    validateOrgNameUniqueness(organization.getParent().getId(), value);
                }
            }

            patchOperation.setOp(op);
            patchOperation.setPath(path);
            patchOperation.setValue(value);
        }
    }

    private void validatePatchForOrgUpdate(String path, String op, String value, String organizationId)
            throws OrganizationManagementException {

        if (!(PATCH_PATH_ORG_NAME.equals(path) ||
                PATCH_PATH_ORG_DESCRIPTION.equals(path) ||
                PATCH_PATH_ORG_STATUS.equals(path) ||
                PATCH_PATH_ORG_VERSION.equals(path) ||
                path.startsWith(PATCH_PATH_ORG_ATTRIBUTES))) {
            throw handleClientException(ERROR_CODE_PATCH_REQUEST_INVALID_PATH, path);
        }

        if (path.startsWith(PATCH_PATH_ORG_ATTRIBUTES)) {
            String attributeKey = path.replace(PATCH_PATH_ORG_ATTRIBUTES, "").trim();
            if (StringUtils.isBlank(attributeKey)) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_ATTRIBUTE_KEY_UNDEFINED);
            }
            boolean attributeExist = organizationManagementDAO.isAttributeExistByKey(organizationId, attributeKey);

            if (op.equals(PATCH_OP_ADD) && attributeExist) {
                op = PATCH_OP_REPLACE;
            }
            if (op.equals(PATCH_OP_REMOVE) && !attributeExist) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_REMOVE_NON_EXISTING_ATTRIBUTE, attributeKey);
            }
            if (op.equals(PATCH_OP_REPLACE) && !attributeExist) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_REPLACE_NON_EXISTING_ATTRIBUTE, attributeKey);
            }
        }

        if (StringUtils.equals(PATCH_PATH_ORG_STATUS, path)) {
            validateOrganizationStatusUpdate(value, organizationId);
        }

        if (StringUtils.equals(PATCH_PATH_ORG_VERSION, path)) {
            validateOrganizationVersionPatch(value, organizationId);
        }
    }

    private void validatePatchForSelfOrgUpdate(String path, String value, String organizationId)
            throws OrganizationManagementException {

        if (!(PATCH_PATH_ORG_NAME.equals(path) || PATCH_PATH_ORG_VERSION.equals(path))) {
            throw handleClientException(ERROR_CODE_PATCH_REQUEST_INVALID_PATH, path);
        }

        if (StringUtils.equals(PATCH_PATH_ORG_VERSION, path)) {
            validateOrganizationVersionPatch(value, organizationId);
        }
    }

    private void patchTenantStatus(List<PatchOperation> patchOperations, String organizationId)
            throws OrganizationManagementException {

        for (PatchOperation patchOperation : patchOperations) {
            if (StringUtils.equals(PATCH_PATH_ORG_STATUS, patchOperation.getPath().trim())) {
                String type = organizationManagementDAO.getOrganizationType(organizationId);
                if (StringUtils.equals(TENANT.toString(), type)) {
                    updateTenantStatus(patchOperation.getValue(), organizationId);
                }
            }
        }
    }

    private void validateOrganizationStatusUpdate(String value, String organizationId)
            throws OrganizationManagementException {

        if (!(StringUtils.equals(ACTIVE.toString(), value) || StringUtils.equals(DISABLED.toString(), value))) {
            throw handleClientException(ERROR_CODE_UNSUPPORTED_ORGANIZATION_STATUS, value);
        }
        if (StringUtils.equals(DISABLED.toString(), value) &&
                StringUtils.equals(SUPER_ORG_ID, organizationId)) {
            throw handleClientException(ERROR_CODE_SUPER_ORG_DELETE_OR_DISABLE, organizationId);
        } else if (StringUtils.equals(DISABLED.toString(), value) &&
                organizationManagementDAO.hasActiveChildOrganizations(organizationId)) {
            throw handleClientException(ERROR_CODE_ACTIVE_CHILD_ORGANIZATIONS_EXIST, organizationId);
        } else if (StringUtils.equals(ACTIVE.toString(), value) &&
                organizationManagementDAO.isParentOrganizationDisabled(organizationId)) {
            throw handleClientException(ERROR_CODE_PARENT_ORGANIZATION_IS_DISABLED);
        }
    }

    private void validateOrganizationVersionPatch(String value, String organizationId)
            throws OrganizationManagementException {

        if (OrganizationManagementUtil.isOrganization(resolveTenantDomain(organizationId))) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_VERSION_UPDATE_NOT_ALLOWED, organizationId);
        }

        if (Stream.of(OrganizationManagementConstants.OrganizationVersion.OrganizationVersions.values()).noneMatch(
                version -> StringUtils.equals(version.getVersion(), value))) {
            throw handleClientException(ERROR_CODE_UNSUPPORTED_ORGANIZATION_VERSION, value);
        }
    }

    private void validateOrganizationVersionPut(Organization organization)
            throws OrganizationManagementException {

        /* Ignoring the version provided in the PUT request for sub-organizations since we do not allow version
         * updates for sub-organizations.
         */
        if (OrganizationManagementUtil.isOrganization(resolveTenantDomain(organization.getId()))) {
            organization.setVersion(OrganizationManagementConstants.OrganizationVersion.BASE_ORG_VERSION);
        } else if (Stream.of(OrganizationManagementConstants.OrganizationVersion.OrganizationVersions.values()).
                noneMatch(version -> StringUtils.equals(version.getVersion(), organization.getVersion()))) {
            throw handleClientException(ERROR_CODE_UNSUPPORTED_ORGANIZATION_VERSION, organization.getVersion());
        }
    }

    private List<ExpressionNode> getExpressionNodes(String filter, String after, String before,
                                                    String paginationSortOrder)
            throws OrganizationManagementClientException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        if (StringUtils.isBlank(filter)) {
            filter = StringUtils.EMPTY;
        }
        // paginationSortOrder specifies the sorting order for the pagination cursor.
        // E.g., descending for creation time (most recent first) or ascending for metadata name (alphabetical).
        String paginatedFilter = paginationSortOrder.equals(ASC_SORT_ORDER) ?
                getPaginatedFilterForAscendingOrder(filter, after, before) :
                getPaginatedFilterForDescendingOrder(filter, after, before);
        try {
            if (StringUtils.isNotBlank(paginatedFilter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(paginatedFilter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
        } catch (IOException e) {
            throw handleClientException(ERROR_CODE_INVALID_FILTER_FORMAT);
        }
        return expressionNodes;
    }

    private String getPaginatedFilterForAscendingOrder(String paginatedFilter, String after, String before)
            throws OrganizationManagementClientException {

        try {
            if (StringUtils.isNotBlank(before)) {
                String decodedString = new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and before lt "
                        + decodedString : "before lt " + decodedString;
            } else if (StringUtils.isNotBlank(after)) {
                String decodedString = new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and after gt "
                        + decodedString : "after gt " + decodedString;
            }
        } catch (IllegalArgumentException e) {
            throw handleClientException(ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION);
        }
        return paginatedFilter;
    }

    private String getPaginatedFilterForDescendingOrder(String paginatedFilter, String after, String before)
            throws OrganizationManagementClientException {

        try {
            if (StringUtils.isNotBlank(before)) {
                String decodedString = new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8);
                Timestamp.valueOf(decodedString);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and before gt " + decodedString :
                        "before gt " + decodedString;
            } else if (StringUtils.isNotBlank(after)) {
                String decodedString = new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8);
                Timestamp.valueOf(decodedString);
                paginatedFilter += StringUtils.isNotBlank(paginatedFilter) ? " and after lt " + decodedString :
                        "after lt " + decodedString;
            }
        } catch (IllegalArgumentException e) {
            throw handleClientException(ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION);
        }
        return paginatedFilter;
    }

    /**
     * Sets the expression nodes required for the retrieval of organizations from the database.
     *
     * @param node       The node.
     * @param expression The list of expression nodes.
     */
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression)
            throws OrganizationManagementClientException {

        if (node instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) node;
            String attributeValue = expressionNode.getAttributeValue();
            if (StringUtils.isNotBlank(attributeValue)) {
                if (attributeValue.startsWith(ORGANIZATION_ATTRIBUTES_FIELD_PREFIX)) {
                    attributeValue = ORGANIZATION_ATTRIBUTES_FIELD;
                }
                if (isFilteringAttributeNotSupported(attributeValue)) {
                    throw handleClientException(ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE, attributeValue);
                }
                expression.add(expressionNode);
            }
        } else if (node instanceof OperationNode) {
            String operation = ((OperationNode) node).getOperation();
            if (!StringUtils.equalsIgnoreCase(AND, operation)) {
                throw handleClientException(ERROR_CODE_UNSUPPORTED_COMPLEX_QUERY_IN_FILTER);
            }
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }

    private boolean isFilteringAttributeNotSupported(String attributeValue) {

        return !attributeValue.equalsIgnoreCase(ORGANIZATION_ID_FIELD) &&
                !attributeValue.equalsIgnoreCase(ORGANIZATION_NAME_FIELD) &&
                !attributeValue.equalsIgnoreCase(ORGANIZATION_DESCRIPTION_FIELD) &&
                !attributeValue.equalsIgnoreCase(ORGANIZATION_CREATED_TIME_FIELD) &&
                !attributeValue.equalsIgnoreCase(ORGANIZATION_LAST_MODIFIED_FIELD) &&
                !attributeValue.equalsIgnoreCase(ORGANIZATION_STATUS_FIELD) &&
                !attributeValue.equalsIgnoreCase(ORGANIZATION_ATTRIBUTES_FIELD) &&
                !attributeValue.equalsIgnoreCase(PARENT_ID_FIELD) &&
                !attributeValue.equalsIgnoreCase(PAGINATION_AFTER) &&
                !attributeValue.equalsIgnoreCase(PAGINATION_BEFORE);
    }

    private void createTenant(String domain, Organization organization) throws OrganizationManagementException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(organization.getCreatorUsername());
            getTenantMgtService().addTenant(createTenantInfoBean(domain, organization));
        } catch (TenantMgtException e) {
            // Rollback created organization.
            deleteOrganization(organization.getId());
            if (e instanceof TenantManagementClientException) {
                if (StringUtils.equals(ERROR_CODE_EXISTING_DOMAIN.getCode(), e.getErrorCode())) {
                    throw handleClientException(ERROR_CODE_EXISTING_ORGANIZATION_HANDLE, domain);
                }
                throw handleClientException(ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION);
            } else {
                throw handleServerException(ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION, e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private Tenant createTenantInfoBean(String domain, Organization organization) {

        Tenant tenant = new Tenant();
        tenant.setActive(true);
        tenant.setDomain(domain);
        tenant.setAdminName(organization.getCreatorUsername());
        tenant.setAdminUserId(organization.getCreatorId());
        tenant.setEmail(organization.getCreatorEmail());
        tenant.setAssociatedOrganizationUUID(organization.getId());
        tenant.setProvisioningMethod(StringUtils.EMPTY);
        return tenant;
    }

    private RealmService getRealmService() {

        return OrganizationManagementDataHolder.getInstance().getRealmService();
    }

    private TenantMgtService getTenantMgtService() {

        return OrganizationManagementDataHolder.getInstance().getTenantMgtService();
    }

    /**
     * Allow management access of sub organization from an ancestor organization unless the self-manage is allowed
     * where the requesting organization can manage itself.
     *
     * @param requestInvokingOrganizationId The organization qualifier id where the request is authorized to access.
     * @param accessedOrganizationId        The id of the organization trying to access.
     * @param isSelfOrgManageAllowed        Whether the request invoked organization can manage itself.
     * @throws OrganizationManagementException The exception is thrown when the request invoked organization doesn't
     *                                         have manage access to the organization which is going to access.
     */
    private void validateOrganizationAccess(String requestInvokingOrganizationId, String accessedOrganizationId,
                                            boolean isSelfOrgManageAllowed) throws OrganizationManagementException {

        if (!(isSelfOrgManageAllowed && StringUtils.equals(requestInvokingOrganizationId, accessedOrganizationId)) &&
                !organizationManagementDAO.isChildOfParent(accessedOrganizationId, requestInvokingOrganizationId)) {
            throw handleClientException(ERROR_CODE_UNAUTHORIZED_ORG_ACCESS, accessedOrganizationId,
                    requestInvokingOrganizationId);
        }
    }

    private OrganizationManagerListener getListener() {

        return OrganizationManagementDataHolder.getInstance().getOrganizationManagerListener();
    }

    private void deactivateTenant(int tenantId) throws OrganizationManagementServerException {

        try {
            org.wso2.carbon.user.api.Tenant tenant = getRealmService().getTenantManager().getTenant(tenantId);
            tenant.setActive(false);
            getRealmService().getTenantManager().updateTenant(tenant);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_DEACTIVATING_ROOT_ORGANIZATION_TENANT, e,
                    String.valueOf(tenantId));
        }
    }

    private void setOrganizationOwnerInformation(Organization organization) throws OrganizationManagementException {

        for (OrganizationAttribute attribute : organization.getAttributes()) {
            switch (attribute.getKey()) {
                case CREATOR_ID:
                    organization.setCreatorId(attribute.getValue());
                    break;
                case CREATOR_USERNAME:
                    organization.setCreatorUsername(attribute.getValue());
                    break;
                case CREATOR_EMAIL:
                    organization.setCreatorEmail(attribute.getValue());
                    break;
                default:
                    break;
            }
        }

        String orgOwnerId = organization.getCreatorId();
        String orgOwnerName = organization.getCreatorUsername();
        String orgOwnerEmail = organization.getCreatorEmail();
        if (StringUtils.isNotEmpty(orgOwnerId)) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            try {
                AbstractUserStoreManager userStoreManager = Utils.getUserStoreManager(tenantId);
                if (!userStoreManager.isExistingUserWithID(orgOwnerId)) {
                    throw handleClientException(ERROR_CODE_ORGANIZATION_OWNER_NOT_EXIST, String.valueOf(tenantId));
                }
                if (StringUtils.isEmpty(orgOwnerName)) {
                    orgOwnerName = userStoreManager.getUser(orgOwnerId, null).getUsername();
                    organization.setCreatorUsername(orgOwnerName);
                }
            } catch (UserStoreException e) {
                throw handleServerException(ERROR_CODE_ERROR_VALIDATING_ORGANIZATION_OWNER, e, organization.getId());
            }
        } else {
            organization.setCreatorId(getUserId());
        }

        if (StringUtils.isEmpty(orgOwnerName)) {
            organization.setCreatorUsername(getAuthenticatedUsername());
        }

        if (StringUtils.isEmpty(orgOwnerEmail)) {
            String email = "dummyadmin@email.com";
            organization.setCreatorEmail(email);
        }
    }

    private void setOrganizationVersion(Organization organization)
            throws OrganizationManagementServerException {

        String parentId = null;
        if (organization.getParent() != null) {
            parentId = organization.getParent().getId();
        }

        if (StringUtils.isBlank(parentId) ||
                getOrganizationDepthInHierarchy(parentId) + 1 < Utils.getSubOrgStartLevel()) {
            String configuredNewOrgVersion;
            if (StringUtils.isNotBlank(Utils.getNewOrganizationVersion())) {
                configuredNewOrgVersion = Utils.getNewOrganizationVersion();
            } else {
                configuredNewOrgVersion = OrganizationManagementConstants.OrganizationVersion
                        .BASE_ORG_VERSION;
            }

            if (Stream.of(OrganizationManagementConstants.OrganizationVersion.OrganizationVersions.values()).noneMatch(
                    version -> StringUtils.equals(version.getVersion(), configuredNewOrgVersion))) {
                throw handleServerException(ERROR_CODE_INVALID_NEW_ORGANIZATION_VERSION_CONFIGURED, null);
            }

            organization.setVersion(Utils.getNewOrganizationVersion());
        } else {
            // In the current implementation, sub-organization version will always be v0.0.0
            organization.setVersion(OrganizationManagementConstants.OrganizationVersion.BASE_ORG_VERSION);
        }
    }
}
