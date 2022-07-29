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

package org.wso2.carbon.identity.organization.management.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.authz.service.OrganizationManagementAuthorizationManager;
import org.wso2.carbon.identity.organization.management.authz.service.exception.OrganizationManagementAuthzServiceServerException;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.model.ChildOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.OrganizationAttribute;
import org.wso2.carbon.identity.organization.management.service.model.ParentOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;
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
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATE_ORGANIZATION_ADMIN_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.CREATE_ORGANIZATION_PERMISSION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ACTIVE_CHILD_ORGANIZATIONS_EXIST;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_KEY_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_VALUE_MISSING;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_CREATE_REQUEST_PARENT_ORGANIZATION_IS_DISABLED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_DUPLICATE_ATTRIBUTE_KEYS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_AUTHORIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_TO_ROOT_AUTHORIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_MISSING_ROOT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_FORMAT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_ID;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_ORGANIZATION_TYPE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_PARENT_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_PATCH_OPERATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_HAS_CHILD_ORGANIZATIONS;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_ID_UNDEFINED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NAME_RESERVED;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT;
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
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ROOT_ORG_DELETE_OR_DISABLE;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ROOT_ORG_RENAME;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_CREATE_CHILD_ORGANIZATION_IN_ROOT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_COMPLEX_QUERY_IN_FILTER;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE;
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
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ROOT;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ROOT_ORG_ID;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.buildURIForBody;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getAuthenticatedUsername;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getTenantDomain;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getTenantId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.getUserId;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleClientException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;

/**
 * This class implements the {@link OrganizationManager} interface.
 */
public class OrganizationManagerImpl implements OrganizationManager {

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    @Override
    public Organization addOrganization(Organization organization) throws OrganizationManagementException {

        validateAddOrganizationRequest(organization);
        setParentOrganization(organization);
        setCreatedAndLastModifiedTime(organization);
        organizationManagementDAO.addOrganization(organization);
        String orgCreatorID = getUserId();
        if (StringUtils.equals(TENANT.toString(), organization.getType())) {
            createTenant(organization.getId(), orgCreatorID);
        }
        return organization;
    }

    @Override
    public boolean isOrganizationExistByName(String organizationName) throws OrganizationManagementException {

        return organizationManagementDAO.isOrganizationExistByName(organizationName);
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
        Organization organization = organizationManagementDAO.getOrganization(organizationId.trim());

        if (organization == null) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }

        if (!ROOT.equals(organization.getName())) {
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
        return organization;
    }

    @Override
    public List<BasicOrganization> getOrganizations(Integer limit, String after, String before, String sortOrder,
                                                    String filter, boolean recursive)
            throws OrganizationManagementException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter, after, before);

        String orgId = resolveOrganizationId(getTenantDomain());

        List<ExpressionNode> filteringByParentIdExpressionNodes = new ArrayList<>();
        for (ExpressionNode expressionNode : expressionNodes) {
            if (StringUtils.equals(expressionNode.getAttributeValue(), PARENT_ID_FIELD)) {
                filteringByParentIdExpressionNodes.add(expressionNode);
            }
        }
        expressionNodes.removeAll(filteringByParentIdExpressionNodes);

        return organizationManagementDAO.getOrganizations(recursive, limit, orgId, sortOrder, expressionNodes,
                filteringByParentIdExpressionNodes);
    }

    @Override
    public void deleteOrganization(String organizationId) throws OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        validateOrganizationDelete(organizationId);
        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        if (organization == null) {
            return;
        }
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
        organizationManagementDAO.deleteOrganization(organizationId);
    }

    @Override
    public Organization patchOrganization(String organizationId, List<PatchOperation> patchOperations) throws
            OrganizationManagementException {

        if (StringUtils.isBlank(organizationId)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_ID_UNDEFINED);
        }
        organizationId = organizationId.trim();
        if (!isOrganizationExistById(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }
        validateOrganizationPatchOperations(patchOperations, organizationId);

        organizationManagementDAO.patchOrganization(organizationId, Instant.now(), patchOperations);
        patchTenantStatus(patchOperations, organizationId);

        Organization organization = organizationManagementDAO.getOrganization(organizationId);
        if (!ROOT.equals(organization.getName())) {
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
        if (!isOrganizationExistById(organizationId)) {
            throw handleClientException(ERROR_CODE_INVALID_ORGANIZATION, organizationId);
        }

        validateUpdateOrganizationRequest(currentOrganizationName, organization);
        updateLastModifiedTime(organization);
        organizationManagementDAO.updateOrganization(organizationId, organization);

        Organization updatedOrganization = organizationManagementDAO.getOrganization(organizationId);
        if (!ROOT.equals(updatedOrganization.getName())) {
            updatedOrganization.getParent().setRef(buildURIForBody(updatedOrganization.getParent().getId()));
        }

        if (StringUtils.equals(TENANT.toString(), organization.getType())) {
            updateTenantStatus(organization.getStatus(), organizationId);
        }
        return updatedOrganization;
    }

    @Override
    public String resolveTenantDomain(String organizationId) throws OrganizationManagementException {

        if (StringUtils.equals(ROOT_ORG_ID, organizationId)) {
            // super tenant domain will be returned.
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return organizationManagementDAO.resolveTenantDomain(organizationId);
    }

    @Override
    public String resolveOrganizationId(String tenantDomain) throws OrganizationManagementException {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenantDomain)) {
            return OrganizationManagementConstants.ROOT_ORG_ID;
        } else {
            return organizationManagementDAO.resolveOrganizationId(tenantDomain).orElseThrow(
                    () -> handleClientException(ERROR_CODE_ORGANIZATION_NOT_FOUND_FOR_TENANT, tenantDomain));
        }
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
        } else {
            throw handleClientException(ERROR_CODE_RETRIEVING_ORGANIZATIONS_BY_NAME, organizationName);
        }
    }

    private void updateTenantStatus(String status, String organizationId) throws OrganizationManagementServerException {

        if (StringUtils.equals(ACTIVE.toString(), status)) {
            try {
                getTenantMgtService().activateTenant(IdentityTenantUtil.getTenantId(organizationId));
            } catch (TenantMgtException e) {
                throw handleServerException(ERROR_CODE_ERROR_ACTIVATING_ORGANIZATION_TENANT, e, organizationId);
            }
        } else {
            try {
                getTenantMgtService().deactivateTenant(IdentityTenantUtil.getTenantId(organizationId));
            } catch (TenantMgtException e) {
                throw handleServerException(ERROR_CODE_ERROR_DEACTIVATING_ORGANIZATION_TENANT, e, organizationId);
            }
        }
    }

    private void updateLastModifiedTime(Organization organization) {

        Instant now = Instant.now();
        organization.setLastModified(now);
    }

    private void validateOrganizationDelete(String organizationId) throws OrganizationManagementException {

        if (StringUtils.equals(ROOT_ORG_ID, organizationId)) {
            throw handleClientException(ERROR_CODE_ROOT_ORG_DELETE_OR_DISABLE, organizationId);
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

    private void validateOrganizationNameField(String organizationName) throws OrganizationManagementException {

        if (StringUtils.equalsIgnoreCase(ROOT, organizationName)) {
            throw handleClientException(ERROR_CODE_ORGANIZATION_NAME_RESERVED, ROOT);
        }
    }

    private void setParentOrganization(Organization organization) throws OrganizationManagementException {

        ParentOrganizationDO parentOrganization = organization.getParent();
        String parentId = parentOrganization.getId().trim();
        /*
        For parentId an alias as 'ROOT' is supported. This indicates that the organization should be created as an
        immediate child of the ROOT organization.
         */
        if (StringUtils.equals(ROOT, parentId)) {
            String rootOrganizationId = ROOT_ORG_ID;
            if (StringUtils.isBlank(rootOrganizationId)) {
                throw handleServerException(ERROR_CODE_ERROR_MISSING_ROOT, null);
            }
            parentId = rootOrganizationId;
        } else {
            Organization parent = organizationManagementDAO.getOrganization(parentId);
            if (parent == null) {
                throw handleClientException(ERROR_CODE_INVALID_PARENT_ORGANIZATION, parentId);
            }
        }
        /*
        To create an organization as an immediate child of ROOT organization, the request should be invoked from the
        ROOT organization (super tenant) space.
         */
        if (StringUtils.equals(ROOT_ORG_ID, parentId) && getTenantId() != MultitenantConstants.SUPER_TENANT_ID) {
            throw handleClientException(ERROR_CODE_UNABLE_TO_CREATE_CHILD_ORGANIZATION_IN_ROOT);
        }

        validateAddOrganizationParentStatus(parentId);
        /*
        Having '/permission/admin/' assigned to the user would be sufficient to create an organization as an
        immediate child organization of the ROOT organization.
        */
        if (StringUtils.equals(ROOT_ORG_ID, parentId)) {
            if (!isUserAuthorizedToCreateChildOrganizationInRoot() && !isUserAuthorizedToCreateOrganization(parentId)) {
                throw handleClientException(ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION, parentId);
            }
        } else if (!isUserAuthorizedToCreateOrganization(parentId)) {
            throw handleClientException(ERROR_CODE_USER_NOT_AUTHORIZED_TO_CREATE_ORGANIZATION, parentId);
        }
        parentOrganization.setId(parentId);
        parentOrganization.setRef(buildURIForBody(parentId));
    }

    private boolean isUserAuthorizedToCreateChildOrganizationInRoot() throws OrganizationManagementServerException {

        String username = getAuthenticatedUsername();
        try {
            UserRealm tenantUserRealm = getRealmService().getTenantUserRealm(getTenantId());
            AuthorizationManager authorizationManager = tenantUserRealm.getAuthorizationManager();
            return authorizationManager.isUserAuthorized(username, CREATE_ORGANIZATION_ADMIN_PERMISSION,
                    CarbonConstants.UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_TO_ROOT_AUTHORIZATION, e);
        }
    }

    private boolean isUserAuthorizedToCreateOrganization(String parentId) throws OrganizationManagementServerException {

        try {
            return OrganizationManagementAuthorizationManager.getInstance().isUserAuthorized(getUserId(),
                    CREATE_ORGANIZATION_PERMISSION, parentId);
        } catch (OrganizationManagementAuthzServiceServerException e) {
            throw handleServerException(ERROR_CODE_ERROR_EVALUATING_ADD_ORGANIZATION_AUTHORIZATION, e, parentId);
        }
    }

    private void validateUpdateOrganizationRequest(String currentOrganizationName, Organization organization)
            throws OrganizationManagementException {

        validateUpdateOrganizationRequiredFields(organization);
        validateOrganizationStatusUpdate(organization.getStatus(), organization.getId());

        String newOrganizationName = organization.getName().trim();
        if (StringUtils.equals(ROOT, currentOrganizationName)) {
            throw handleClientException(ERROR_CODE_ROOT_ORG_RENAME, organization.getId());
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
                if (StringUtils.equals(ROOT_ORG_ID, organizationId)) {
                    throw handleClientException(ERROR_CODE_ROOT_ORG_RENAME, organizationId);
                }
                validateOrganizationNameField(value);
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
                StringUtils.equals(ROOT_ORG_ID, organizationId)) {
            throw handleClientException(ERROR_CODE_ROOT_ORG_DELETE_OR_DISABLE, organizationId);
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
        } catch (IOException | IdentityException e) {
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
                !attributeValue.equalsIgnoreCase(PARENT_ID_FIELD) &&
                !attributeValue.equalsIgnoreCase(PAGINATION_AFTER) &&
                !attributeValue.equalsIgnoreCase(PAGINATION_BEFORE);
    }

    private void createTenant(String domain, String orgCreatorID) throws OrganizationManagementException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                    .SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            getTenantMgtService().addTenant(createTenantInfoBean(domain, orgCreatorID));
        } catch (TenantMgtException e) {
            // Rollback created organization.
            deleteOrganization(domain);
            if (e instanceof TenantManagementClientException) {
                throw handleClientException(ERROR_CODE_INVALID_TENANT_TYPE_ORGANIZATION);
            } else {
                throw handleServerException(ERROR_CODE_ERROR_ADDING_TENANT_TYPE_ORGANIZATION, e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private Tenant createTenantInfoBean(String domain, String orgCreatorID) {

        Tenant tenant = new Tenant();
        tenant.setActive(true);
        tenant.setDomain(domain);
        /*
        Set the creator's UUID as the tenant admin name because tenant data model doesn't store the admin uuid,
        and it is required when creating the org-user association.
         */
        tenant.setAdminName(orgCreatorID);
        tenant.setAdminUserId(orgCreatorID);
        tenant.setEmail("dummyadmin@email.com");
        // set the password as domain for now to avoid findbugs detecting it as a hardcoded value.
        tenant.setAdminPassword(domain);
        tenant.setAssociatedOrganizationUUID(domain);
        tenant.setProvisioningMethod(StringUtils.EMPTY);
        return tenant;
    }

    private RealmService getRealmService() {

        return OrganizationManagementDataHolder.getInstance().getRealmService();
    }

    private TenantMgtService getTenantMgtService() {

        return OrganizationManagementDataHolder.getInstance().getTenantMgtService();
    }
}
