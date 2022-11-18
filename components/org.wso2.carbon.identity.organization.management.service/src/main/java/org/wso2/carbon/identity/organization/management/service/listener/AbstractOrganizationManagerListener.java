package org.wso2.carbon.identity.organization.management.service.listener;

import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.PatchOperation;

import java.util.List;

/**
 * Abstract implementation for the {@link OrganizationManagerListener}.
 */
public abstract class AbstractOrganizationManagerListener implements OrganizationManagerListener {

    @Override
    public void preAddOrganization(Organization organization) throws OrganizationManagementException {

    }

    @Override
    public void postAddOrganization(Organization organization) throws OrganizationManagementException {

    }

    @Override
    public void preGetOrganization(String organizationId) throws OrganizationManagementException {

    }

    @Override
    public void postGetOrganization(String organizationId, Organization organization)
            throws OrganizationManagementException {

    }

    @Override
    public void preDeleteOrganization(String organizationId) throws OrganizationManagementException {

    }

    @Override
    public void postDeleteOrganization(String organizationId) throws OrganizationManagementException {

    }

    @Override
    public void prePatchOrganization(String organizationId, List<PatchOperation> patchOperations)
            throws OrganizationManagementException {

    }

    @Override
    public void postPatchOrganization(String organizationId, List<PatchOperation> patchOperations)
            throws OrganizationManagementException {

    }

    @Override
    public void preUpdateOrganization(String organizationId, Organization organization)
            throws OrganizationManagementException {

    }

    @Override
    public void postUpdateOrganization(String organizationId, Organization organization)
            throws OrganizationManagementException {

    }
}
