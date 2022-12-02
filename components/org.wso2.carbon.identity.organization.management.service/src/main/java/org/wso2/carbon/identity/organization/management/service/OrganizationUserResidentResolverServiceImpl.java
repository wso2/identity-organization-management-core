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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.organization.management.service.authz.OrganizationManagementAuthorizationManager;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RESOLVING_ROOT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RESOLVING_USER_FROM_RESIDENT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_NO_USERNAME_OR_ID_TO_RESOLVE_USER_FROM_RESIDENT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleClientException;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;
import static org.wso2.carbon.user.core.UserCoreConstants.DOMAIN_SEPARATOR;
import static org.wso2.carbon.user.core.UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME;

/**
 * Service implementation to resolve user's resident organization.
 */
public class OrganizationUserResidentResolverServiceImpl implements OrganizationUserResidentResolverService {

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    @Override
    public Optional<User> resolveUserFromResidentOrganization(String userName, String userId,
                                                              String accessedOrganizationId)
            throws OrganizationManagementException {

        User resolvedUser = null;
        String domain = null;

        try {
            if (userName == null && userId == null) {
                throw handleClientException(ERROR_CODE_NO_USERNAME_OR_ID_TO_RESOLVE_USER_FROM_RESIDENT_ORG);
            }
            if (userName != null) {
                domain = UserCoreUtil.extractDomainFromName(userName);
            }
            List<String> ancestorOrganizationIds =
                    organizationManagementDAO.getAncestorOrganizationIds(accessedOrganizationId);
            if (ancestorOrganizationIds != null) {
                for (String organizationId : ancestorOrganizationIds) {
                    String associatedTenantDomainForOrg = resolveTenantDomainForOrg(organizationId);
                    if (associatedTenantDomainForOrg != null) {
                        AbstractUserStoreManager userStoreManager = getUserStoreManager(associatedTenantDomainForOrg);
                        User user = null;
                        boolean isValidDomain = false;
                        if (domain != null && userStoreManager.getSecondaryUserStoreManager(domain) != null) {
                            isValidDomain = true;
                        }
                        if (userName != null && isValidDomain && userStoreManager.isExistingUser(userName)) {
                            user = userStoreManager.getUser(null, userName);
                        } else if (userId != null && userStoreManager.isExistingUserWithID(userId)) {
                            user = userStoreManager.getUser(userId, null);
                        } else if (userName != null && UserCoreUtil.removeDomainFromName(userName).equals(userName)) {
                            /**
                             * Try to find the user from the secondary user stores when the username is not domain
                             * qualified.
                             */
                            boolean userFound = false;
                            UserStoreManager secondaryUserStoreManager =
                                    userStoreManager.getSecondaryUserStoreManager();
                            while (secondaryUserStoreManager != null) {
                                domain = secondaryUserStoreManager.getRealmConfiguration().getUserStoreProperties()
                                        .get(PROPERTY_DOMAIN_NAME);
                                if (userStoreManager.isExistingUser(domain + DOMAIN_SEPARATOR + userName)) {
                                    user = userStoreManager.getUser(null, domain + DOMAIN_SEPARATOR + userName);
                                    userFound = true;
                                    break;
                                }
                                secondaryUserStoreManager = secondaryUserStoreManager.getSecondaryUserStoreManager();
                            }
                            if (!userFound) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                        /*
                            When user found from an organization where carbon roles are applied, the organization
                            permission check has to be skipped
                         */
                        if (!Utils.useOrganizationRolesForValidation(organizationId)) {
                            resolvedUser = user;
                            break;
                        }
                        // Check whether user has any association against the org the user is trying to access.
                        boolean userHasAccessPermissions =
                                OrganizationManagementAuthorizationManager.getInstance()
                                        .hasUserOrgAssociation(user.getUserID(), accessedOrganizationId);
                        if (userHasAccessPermissions) {
                            resolvedUser = user;
                            /*
                                User resident organization logic should be improved based on the user store
                                configurations in the deployment. So commenting the flow break as a temporary fix.
                             */
                            //break;
                        }
                    }
                }
            }
        } catch (UserStoreException | OrganizationManagementServerException e) {
            throw handleServerException(ERROR_CODE_ERROR_WHILE_RESOLVING_USER_FROM_RESIDENT_ORG, e, userName,
                    accessedOrganizationId);
        }
        return Optional.ofNullable(resolvedUser);
    }

    @Override
    public Optional<String> resolveResidentOrganization(String userId, String accessedOrganizationId)
            throws OrganizationManagementException {

        String residentOrgId = null;
        try {
            List<String> ancestorOrganizationIds =
                    organizationManagementDAO.getAncestorOrganizationIds(accessedOrganizationId);
            if (ancestorOrganizationIds != null) {
                for (String organizationId : ancestorOrganizationIds) {
                    String associatedTenantDomainForOrg = resolveTenantDomainForOrg(organizationId);
                    if (StringUtils.isBlank(associatedTenantDomainForOrg)) {
                        continue;
                    }
                    AbstractUserStoreManager userStoreManager = getUserStoreManager(associatedTenantDomainForOrg);
                    if (userStoreManager.isExistingUserWithID(userId)) {
                        residentOrgId = organizationId;
                        /*
                            User resident organization logic should be improved based on the user store configurations
                            in the deployment. So commenting the flow break as a temporary fix.
                         */
                        //break;
                    }
                }
            }
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_WHILE_RESOLVING_ROOT_ORG, e, userId);
        }
        return Optional.ofNullable(residentOrgId);
    }

    @Override
    public List<BasicOrganization> getHierarchyUptoResidentOrganization
            (String userId, String accessedOrganizationId) throws OrganizationManagementException {

        String residentOrgId = null;
        List<BasicOrganization> basicOrganizationList = new ArrayList<>();
        try {
            List<String> ancestorOrganizationIds =
                    organizationManagementDAO.getAncestorOrganizationIds(accessedOrganizationId);
            if (ancestorOrganizationIds != null) {
                for (String organizationId : ancestorOrganizationIds) {
                    String associatedTenantDomainForOrg = resolveTenantDomainForOrg(organizationId);
                    if (StringUtils.isBlank(associatedTenantDomainForOrg)) {
                        continue;
                    }
                    Optional<String> organizationName = organizationManagementDAO
                            .getOrganizationNameById(organizationId);
                    if (organizationName.isPresent()) {
                        BasicOrganization basicOrganization = new BasicOrganization();
                        basicOrganization.setId(organizationId);
                        basicOrganization.setName(organizationName.get());
                        basicOrganizationList.add(basicOrganization);
                    }
                    AbstractUserStoreManager userStoreManager = getUserStoreManager(associatedTenantDomainForOrg);
                    if (userStoreManager.isExistingUserWithID(userId)) {
                        residentOrgId = organizationId;
                        /*
                            User resident organization logic should be improved based on the user store configurations
                            in the deployment. So commenting the flow break as a temporary fix.
                         */
                        //break;
                    }
                }
            }
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_WHILE_RESOLVING_ROOT_ORG, e, userId);
        }
        /*
        Organizations will be sorted starting from resident organization (higher level) and ended up with
        the accessed organization (lower level)
         */
        if (residentOrgId == null) {
            return new ArrayList<>();
        }
        int residentOrgIndex = basicOrganizationList.stream().map(BasicOrganization::getId)
                .collect(Collectors.toList()).indexOf(residentOrgId);
        basicOrganizationList = basicOrganizationList.subList(0, residentOrgIndex + 1);
        Collections.reverse(basicOrganizationList);
        return basicOrganizationList;
    }

    private String resolveTenantDomainForOrg(String organizationId) throws OrganizationManagementServerException {

        if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
            // super tenant domain will be returned.
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        } else {
            return organizationManagementDAO.resolveTenantDomain(organizationId);
        }
    }

    private AbstractUserStoreManager getUserStoreManager(String tenantDomain) throws UserStoreException {

        int tenantId = OrganizationManagementDataHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);
        RealmService realmService = OrganizationManagementDataHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);
        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }
}
