/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RESOLVING_GROUPS_ROOT_ORG;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;

/**
 * Service implementation to resolve group's resident organization.
 */
// coverage:ignore
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.organization.management.service." +
                        "OrganizationGroupResidentResolverService",
                "service.scope=singleton"
        }
)
public class OrganizationGroupResidentResolverServiceImpl implements OrganizationGroupResidentResolverService {

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    @Override
    public Optional<String> resolveResidentOrganization(String groupId, String accessedOrganizationId)
            throws OrganizationManagementException {

        String residentOrgId = null;
        try {
            List<String> ancestorOrganizationIds =
                    organizationManagementDAO.getAncestorOrganizationIds(accessedOrganizationId);
            int subOrgStartLevel = Utils.getSubOrgStartLevel();
            if (ancestorOrganizationIds != null) {
                if (subOrgStartLevel > 1) {
                    ancestorOrganizationIds.remove(ancestorOrganizationIds.size() - 1);
                }
                for (String organizationId : ancestorOrganizationIds) {
                    String associatedTenantDomainForOrg = OrganizationManagementDataHolder.getInstance()
                            .getOrganizationManager().resolveTenantDomain(organizationId);
                    if (StringUtils.isBlank(associatedTenantDomainForOrg)) {
                        continue;
                    }

                    AbstractUserStoreManager userStoreManager = getUserStoreManager(associatedTenantDomainForOrg);
                    if (userStoreManager.isGroupExist(groupId)) {
                        residentOrgId = organizationId;
                    }
                }
            }
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_ERROR_WHILE_RESOLVING_GROUPS_ROOT_ORG, e, groupId);
        }
        return Optional.ofNullable(residentOrgId);
    }

    private AbstractUserStoreManager getUserStoreManager(String tenantDomain) throws UserStoreException {

        int tenantId = OrganizationManagementDataHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);
        RealmService realmService = OrganizationManagementDataHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);
        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }
}
