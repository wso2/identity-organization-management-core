package org.wso2.carbon.identity.organization.management.service;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.organization.management.service.dao.OrganizationManagementDAO;
import org.wso2.carbon.identity.organization.management.service.dao.impl.OrganizationManagementDAOImpl;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RESOLVING_GROUPS_ROOT_ORG;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;
import static org.wso2.carbon.identity.organization.management.service.util.Utils.handleServerException;

/**
 * Service implementation to resolve group's resident organization.
 */
public class OrganizationGroupResidentResolverServiceImpl implements OrganizationGroupResidentResolverService {

    private final OrganizationManagementDAO organizationManagementDAO = new OrganizationManagementDAOImpl();

    @Override
    public Optional<String> resolveResidentOrganization(String groupId, String accessedOrganizationId)
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

    private String resolveTenantDomainForOrg(String organizationId) throws OrganizationManagementServerException {

        if (StringUtils.equals(SUPER_ORG_ID, organizationId)) {
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
