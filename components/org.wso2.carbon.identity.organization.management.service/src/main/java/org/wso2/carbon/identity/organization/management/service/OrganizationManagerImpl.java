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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.organization.management.service.authz.OrganizationManagementAuthorizationManager;
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
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.ChildOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.ParentOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
import org.wso2.carbon.identity.organization.management.service.model.TenantTypeOrganization;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.stratos.common.exception.TenantManagementClientException;
import org.wso2.carbon.stratos.common.exception.TenantMgtException;
import org.wso2.carbon.tenant.mgt.services.TenantMgtService;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.AND;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CO;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATE_ORGANIZATION_ADMIN_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATE_ORGANIZATION_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.EW;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ACTIVE_CHILD_ORGANIZATIONS_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_KEY_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_VALUE_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_CREATE_REQUEST_PARENT_ORGANIZATION_IS_DISABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_DUPLICATE_ATTRIBUTE_KEYS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_AUTHORIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_TO_SUPER_AUTHORIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_MISSING_SUPER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_TIMESTAMP_FORMAT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_PARENT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_PATCH_OPERATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_HAS_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_ID_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NAME_EXIST_IN_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NAME_RESERVED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_TYPE_UNDEFINED;
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
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_SUPER_ORG_DELETE_OR_DISABLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_SUPER_ORG_RENAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_CREATE_CHILD_ORGANIZATION_IN_SUPER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNAUTHORIZED_ORG_ACCESS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_COMPLEX_QUERY_IN_FILTER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_OPERATION_FOR_ATTRIBUTE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ORGANIZATION_STATUS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION;
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
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SW;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.buildURIForBody;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getAuthenticatedUsername;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getOrganizationId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getTenantDomain;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getTenantId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getUserId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleClientException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.isSubOrganization;

/**
 * This class implements the {@link OrganizationManager} interface.
 */
public class OrganizationManagerImpl implements OrganizationManager {

    private final OrganizationManagementDAO organizationManagementDAO =
            new CacheBackedOrganizationManagementDAO(new OrganizationManagementDAOImpl());

    @Override
    public Organization addOrganization(Organization organization) throws OrganizationManagementException {

        validateAddOrganizationRequest(organization);
        setParentOrganization(organization);
        validateOrgNameUniqueness(organization.getParent().getId(), organization.getName());
        setCreatedAndLastModifiedTime(organization);
        getListener().preAddOrganization(organization);
        organizationManagementDAO.addOrganization(organization);
        String orgCreatorID =
                StringUtils.isNotBlank(organization.getCreatorId()) ? organization.getCreatorId() : getUserId();
        String orgCreatorName =
                StringUtils.isNotBlank(organization.getCreatorUsername()) ? organization.getCreatorUsername() :
                        getAuthenticatedUsername();
        String orgCreatorEmail =
                StringUtils.isNotBlank(organization.getCreatorEmail()) ? organization.getCreatorEmail() :
                        "dummyadmin@email.com";
        // Create a tenant for tenant type organization.
        if (organization instanceof TenantTypeOrganization) {
            String tenantDomainName = ((TenantTypeOrganization) organization).getDomainName();
            String organizationId = organization.getId();
            createTenant(tenantDomainName, organizationId, orgCreatorID, orgCreatorName, orgCreatorEmail);
        }
        getListener().postAddOrganization(organization);
        return organization;
    }

    @Override
    public boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementException {

        return organizationManagementDAO.isOrganizationExistByName(organizationName);
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

    @Override
    public Organization getOrganization(String organizationId, boolean showChildren, boolean includePermissions) throws
            OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        String requestInvokingOrganizationId = getOrganizationId();
        if (requestInvokingOrganizationId == null) {
            requestInvokingOrganizationId = SUPER_ORG_ID;
        }
        validateOrganizationAccess(requestInvokingOrganizationId, organizationId, true);
        getListener().preGetOrganization(organizationId.trim());

        Organization organization = organizationManagementDAO.getOrganization(organizationId.trim());
        if (organization == null) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }

        if (!SUPER.equals(organization.getName())) {
            organization.getParent().setRef(buildURIForBody(organization.getParent().getId()));
        }

        if (showChildren) {
            List<String> childOrganizationIds = organizationManagementDAO.getChildOrganizationIds(organizationId);
            if (CollectionUtils.isNotEmpty(childOrganizationIds)) {
                List<ChildOrganizationDO> childOrganizations = new ArrayList<>();
                for (String childOrganizationId : childOrganizationIds) {
                    ChildOrganizationDO childOrganization = new ChildOrganizationDO();
                    childOrganization.setId(childOrganizationId);
                    childOrganization.setRef(buildURIForBody(childOrganizationId));
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

        getListener().postGetOrganization(organizationId.trim(), organization);
        return organization;
    }

    @Override
    public List<BasicOrganization> getChildOrganizations(String organizationId, boolean recursive)
            throws OrganizationManagementException {

        return organizationManagementDAO.getChildOrganizations(organizationId, recursive);
    }

    @Override
    public List<String> getChildOrganizationsIds(String organizationId)
            throws OrganizationManagementException {

        return organizationManagementDAO.getChildOrganizationIds(organizationId);
    }

    @Override
    public List<BasicOrganization> getOrganizations(Integer limit, String after, String before, String sortOrder,
                                                    String filter, boolean recursive)
            throws OrganizationManagementException {

        return getOrganizationList(false, limit, after, before, sortOrder, filter, recursive);
    }

    @Override
    public List<BasicOrganization> getUserAuthorizedOrganizations(Integer limit, String after, String before,
                                                                  String sortOrder, String filter, boolean recursive)
            throws OrganizationManagementException {

        return getOrganizationList(true, limit, after, before, sortOrder, filter, recursive);
    }

    private List<BasicOrganization> getOrganizationList(boolean authorizedSubOrgsOnly, Integer limit, String after,
                                                        String before, String sortOrder,
                                                        String filter, boolean recursive)
            throws OrganizationManagementException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before);

        String orgId = resolveOrganizationId(getTenantDomain());

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
        expressionNodes.removeAll(filteringByParentIdExpressionNodes);

        return authorizedSubOrgsOnly ? organizationManagementDAO.getUserAuthorizedOrganizations(
                recursive, limit, orgId, sortOrder, expressionNodes, filteringByParentIdExpressionNodes)
                : organizationManagementDAO.getOrganizations(
                recursive, limit, orgId, sortOrder, expressionNodes, filteringByParentIdExpressionNodes);
    }

    @Override
    public void deleteOrganization(String organizationId) throws OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        String requestInvokingOrganizationId = getOrganizationId();
        if (requestInvokingOrganizationId == null) {
            requestInvokingOrganizationId = SUPER_ORG_ID;
        }
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
        if (requestInvokingOrganizationId == null) {
            requestInvokingOrganizationId = SUPER_ORG_ID;
        }
        validateOrganizationAccess(requestInvokingOrganizationId, organizationId, false);
        if (!isOrganizationExistById(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }
        validateOrganizationPatchOperations(patchOperations, organizationId);

        getListener().prePatchOrganization(organizationId, patchOperations);
        organizationManagementDAO.patchOrganization(organizationId, Instant.now(), patchOperations);
        patchTenantStatus(patchOperations, organizationId);

        getListener().postPatchOrganization(organizationId, patchOperations);

        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        if (!SUPER.equals(organization.getName())) {
            organization.getParent().setRef(buildURIForBody(organization.getParent().getId()));
        }

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
        if (requestInvokingOrganizationId == null) {
            requestInvokingOrganizationId = SUPER_ORG_ID;
        }
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
        if (!SUPER.equals(updatedOrganization.getName())) {
            updatedOrganization.getParent().setRef(buildURIForBody(updatedOrganization.getParent().getId()));
        }

        if (StringUtils.equals(TENANT.toString(), organization.getType())) {
            updateTenantStatus(organization.getStatus(), organizationId);
        }
        getListener().postUpdateOrganization(organizationId, organization);
        return updatedOrganization;
    }

    @Override
    public String resolveTenantDomain(String organizationId) throws OrganizationManagementException {

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

    private void updateTenantStatus(String status, String organizationId) throws OrganizationManagementServerException {

        if (StringUtils.equals(ACTIVE.toString(), status)) {
            try {
                getTenantMgtService().activateTenant(getRealmService().getTenantManager().getTenantId(organizationId));
            } catch (TenantMgtException | UserStoreException e) {
                throw handleServerException(ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT, e, organizationId);
            }
        } else {
            try {
                getTenantMgtService().deactivateTenant(getRealmService().getTenantManager()
                        .getTenantId(organizationId));
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
        validateOrganizationNameField(organization.getName());
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
        validateOrganizationRequiredFieldParentId(organization.getParent().getId());
    }

    private void validateUpdateOrganizationRequiredFields(Organization organization) throws
            OrganizationManagementClientException {

        validateOrganizationRequiredFieldName(organization.getName());
        validateOrganizationRequiredFieldStatus(organization.getStatus());
    }

    private void validateOrganizationRequiredFieldParentId(String parentId) throws
            OrganizationManagementClientException {

        if (StringUtils.isBlank(parentId)) {
            throw handleClientException(ERROR_CODE_REQUIRED_FIELDS_MISSING, PARENT_ID_FIELD);
        }
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

    private void validateOrganizationNameField(String organizationName) throws OrganizationManagementException {

        if (StringUtils.equalsIgnoreCase(SUPER, organizationName)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_NAME_RESERVED, SUPER);
        }
    }

    private void setParentOrganization(Organization organization) throws OrganizationManagementException {

        ParentOrganizationDO parentOrganization = organization.getParent();
        String parentId = parentOrganization.getId().trim();
        /*
        For parentId an alias as 'Super' is supported. This indicates that the organization should be created as an
        immediate child of the super organization.
         */
        if (StringUtils.equals(SUPER, parentId)) {
            String superOrganizationId = SUPER_ORG_ID;
            if (StringUtils.isBlank(superOrganizationId)) {
                throw handleServerException(ERROR_CODE_ERROR_MISSING_SUPER, null);
            }
            parentId = superOrganizationId;
        } else {
            Organization parent = organizationManagementDAO.getOrganization(parentId);
            if (parent == null) {
                throw handleClientException(ERROR_CODE_INVALID_PARENT_ORGANIZATION, parentId);
            }
        }
        /*
        To create an organization as an immediate child of super organization, the request should be invoked from the
        super organization (super tenant) space.
         */
        if (StringUtils.equals(SUPER_ORG_ID, parentId) && getTenantId() != MultitenantConstants.SUPER_TENANT_ID) {
            throw handleClientException(ERROR_CODE_UNABLE_TO_CREATE_CHILD_ORGANIZATION_IN_SUPER);
        }

        validateAddOrganizationParentStatus(parentId);
        /*
        Having '/permission/admin/' assigned to the user would be sufficient to create an organization as an
        immediate child organization of the super organization.
        */
        if (StringUtils.equals(SUPER_ORG_ID, parentId)) {
            if (!isUserAuthorizedToCreateChildOrganizationInSuper() &&
                    !isUserAuthorizedToCreateOrganization(parentId)) {
                throw handleClientException(ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION, parentId);
            }
        /*
         For level-1 & below, having '/permission/admin/manage/identity/organizationmgt/create' would be sufficient
         to create an organization as a child organization.
        */
        } else if (!isUserAuthorizedToCreateOrganization(parentId)) {
            throw handleClientException(ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION, parentId);
        }
        parentOrganization.setId(parentId);
        parentOrganization.setRef(buildURIForBody(parentId));
    }

    private boolean isUserAuthorizedToCreateChildOrganizationInSuper() throws OrganizationManagementException {

        String username = getAuthenticatedUsername();
        try {
            UserRealm tenantUserRealm = getRealmService().getTenantUserRealm(getTenantId());
            AuthorizationManager authorizationManager = tenantUserRealm.getAuthorizationManager();
            return authorizationManager.isUserAuthorized(username, CREATE_ORGANIZATION_ADMIN_PERMISSION,
                    CarbonConstants.UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_TO_SUPER_AUTHORIZATION, e);
        }
    }

    private boolean isUserAuthorizedToCreateOrganization(String parentId) throws OrganizationManagementServerException {

        try {
            if (!Utils.useOrganizationRolesForValidation(parentId)) {
                String username = getAuthenticatedUsername();
                UserRealm tenantUserRealm = getRealmService().getTenantUserRealm(getTenantId());
                AuthorizationManager authorizationManager = tenantUserRealm.getAuthorizationManager();
                return authorizationManager.isUserAuthorized(username, CREATE_ORGANIZATION_PERMISSION,
                        CarbonConstants.UI_PERMISSION_ACTION);
            }
            return OrganizationManagementAuthorizationManager.getInstance().isUserAuthorized(getUserId(),
                    CREATE_ORGANIZATION_PERMISSION, parentId);
        } catch (OrganizationManagementServerException | UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_AUTHORIZATION, e, parentId);
        }
    }

    private void validateUpdateOrganizationRequest(String currentOrganizationName, Organization organization)
            throws OrganizationManagementException {

        validateUpdateOrganizationRequiredFields(organization);
        validateOrganizationStatusUpdate(organization.getStatus(), organization.getId());

        String newOrganizationName = organization.getName().trim();
        if (StringUtils.equals(SUPER, currentOrganizationName)) {
            throw handleClientException(ERROR_CODE_SUPER_ORG_RENAME, organization.getId());
        }
        // Check if the organization name is reserved.
        if (!StringUtils.equals(currentOrganizationName, newOrganizationName)) {
            validateOrganizationNameField(newOrganizationName);
        }
        organization.setName(newOrganizationName);

        validateOrganizationAttributes(organization.getAttributes());
    }

    private void validateOrganizationPatchOperations(List<PatchOperation> patchOperations, String organizationId)
            throws OrganizationManagementException {

        for (PatchOperation patchOperation : patchOperations) {
            // Validate requested patch operation.
            if (StringUtils.isBlank(patchOperation.getOp())) {
                throw handleClientException(ERROR_CODE_PATCH_OPERATION_UNDEFINED, organizationId);
            }
            String op = patchOperation.getOp().trim();
            if (!(PATCH_OP_ADD.equals(op) || PATCH_OP_REMOVE.equals(op) || PATCH_OP_REPLACE.equals(op))) {
                throw handleClientException(ERROR_CODE_INVALID_PATCH_OPERATION, op);
            }

            // Validate path.
            if (StringUtils.isBlank(patchOperation.getPath())) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_PATH_UNDEFINED);
            }
            String path = patchOperation.getPath().trim();

            /*
            Check if it is a supported path for patching.
            Fields such as the parentId can't be modified with the current implementation.
             */
            if (!(path.equals(PATCH_PATH_ORG_NAME) || path.equals(PATCH_PATH_ORG_DESCRIPTION) ||
                    path.equals(PATCH_PATH_ORG_STATUS) || path.startsWith(PATCH_PATH_ORG_ATTRIBUTES))) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_INVALID_PATH, path);
            }

            // Validate value.
            String value;
            // Value is mandatory for Add and Replace operations.
            if (StringUtils.isBlank(patchOperation.getValue()) && !PATCH_OP_REMOVE.equals(op)) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_VALUE_UNDEFINED);
            } else {
                // Avoid NPEs down the road.
                value = patchOperation.getValue() != null ? patchOperation.getValue().trim() : "";
            }

            // Mandatory fields can only be 'Replaced'.
            if (!op.equals(PATCH_OP_REPLACE) && !(path.equals(PATCH_PATH_ORG_DESCRIPTION) ||
                    path.startsWith(PATCH_PATH_ORG_ATTRIBUTES))) {
                throw handleClientException(ERROR_CODE_PATCH_REQUEST_MANDATORY_FIELD_INVALID_OPERATION, op, path);
            }

            // Check whether the new organization name is reserved.
            if (path.equals(PATCH_PATH_ORG_NAME)) {
                if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
                    throw handleClientException(ERROR_CODE_SUPER_ORG_RENAME, organizationId);
                }
                validateOrganizationNameField(value);
                Organization organization = organizationManagementDAO.getOrganization(organizationId);
                if (!organization.getName().equals(value)) {
                    // If trying to patch a new name, need to check the availability.
                    validateOrgNameUniqueness(organization.getParent().getId(), value);
                }
            }

            if (StringUtils.equals(PATCH_PATH_ORG_STATUS, path)) {
                validateOrganizationStatusUpdate(value, organizationId);
            }

            if (path.startsWith(PATCH_PATH_ORG_ATTRIBUTES)) {
                String attributeKey = path.replace(PATCH_PATH_ORG_ATTRIBUTES, "").trim();
                // Attribute key can not be empty.
                if (StringUtils.isBlank(attributeKey)) {
                    throw handleClientException(ERROR_CODE_PATCH_REQUEST_ATTRIBUTE_KEY_UNDEFINED);
                }
                boolean attributeExist = organizationManagementDAO.isAttributeExistByKey(organizationId, attributeKey);
                // If attribute key to be added already exists, update its value.
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

            patchOperation.setOp(op);
            patchOperation.setPath(path);
            patchOperation.setValue(value);
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

    private List<ExpressionNode> getExpressionNodes(String filter, String after, String before)
            throws OrganizationManagementClientException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        if (StringUtils.isBlank(filter)) {
            filter = StringUtils.EMPTY;
        }
        String paginatedFilter = getPaginatedFilter(filter, after, before);
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

    private String getPaginatedFilter(String paginatedFilter, String after, String before) throws
            OrganizationManagementClientException {

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
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression) throws
            OrganizationManagementClientException {

        if (node instanceof ExpressionNode) {
            ExpressionNode expressionNode = (ExpressionNode) node;
            String attributeValue = expressionNode.getAttributeValue();
            if (StringUtils.isNotBlank(attributeValue)) {
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
                !attributeValue.equalsIgnoreCase(PARENT_ID_FIELD) &&
                !attributeValue.equalsIgnoreCase(PAGINATION_AFTER) &&
                !attributeValue.equalsIgnoreCase(PAGINATION_BEFORE);
    }

    private void createTenant(String domain, String organizationId, String orgCreatorID, String orgCreatorName,
                              String orgCreatorEmail) throws OrganizationManagementException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(orgCreatorName);
            getTenantMgtService().addTenant(
                    createTenantInfoBean(domain, organizationId, orgCreatorID, orgCreatorName, orgCreatorEmail));
        } catch (TenantMgtException e) {
            // Rollback created organization.
            deleteOrganization(organizationId);
            if (e instanceof TenantManagementClientException) {
                throw handleClientException(ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION);
            } else {
                throw handleServerException(ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION, e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private Tenant createTenantInfoBean(String domain, String organizationId, String orgCreatorID,
                                        String orgCreatorName, String orgCreatorEmail) {

        Tenant tenant = new Tenant();
        tenant.setActive(true);
        tenant.setDomain(domain);
        tenant.setAdminName(orgCreatorName);
        tenant.setAdminUserId(orgCreatorID);
        tenant.setEmail(orgCreatorEmail);
        tenant.setAssociatedOrganizationUUID(organizationId);
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
}
