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

package org.wso2.carbon.identity.organization.management.service.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the model of an Organization.
 */
public class Organization {

    private String id;
    private String name;
    private String version;
    private String description;
    private String status;
    private String type;
    private String creatorId;
    private String creatorUsername;
    private String creatorEmail;
    private ParentOrganizationDO parent = new ParentOrganizationDO();
    private Instant lastModified;
    private Instant created;
    private List<OrganizationAttribute> attributes = new ArrayList<>();
    private List<ChildOrganizationDO> childOrganizations = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private String organizationHandle;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public ParentOrganizationDO getParent() {

        return parent;
    }

    public void setParent(ParentOrganizationDO parent) {

        this.parent = parent;
    }

    public Instant getLastModified() {

        return lastModified;
    }

    public void setLastModified(Instant lastModified) {

        this.lastModified = lastModified;
    }

    public Instant getCreated() {

        return created;
    }

    public void setCreated(Instant created) {

        this.created = created;
    }

    public String getCreatorId() {

        return creatorId;
    }

    public void setCreatorId(String creatorId) {

        this.creatorId = creatorId;
    }

    public String getCreatorUsername() {

        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {

        this.creatorUsername = creatorUsername;
    }

    public String getCreatorEmail() {

        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {

        this.creatorEmail = creatorEmail;
    }

    public List<OrganizationAttribute> getAttributes() {

        return attributes;
    }

    public void setAttributes(List<OrganizationAttribute> attributes) {

        this.attributes = attributes;
    }

    public void setAttribute(OrganizationAttribute attribute) {

        if (attribute != null) {
            this.attributes.add(attribute);
        }
    }

    public List<ChildOrganizationDO> getChildOrganizations() {

        return childOrganizations;
    }

    public void setChildOrganizations(List<ChildOrganizationDO> childOrganizations) {

        this.childOrganizations = childOrganizations;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public List<String> getPermissions() {

        return permissions;
    }

    public void setPermissions(List<String> permissions) {

        this.permissions = permissions;
    }

    /**
     * Returns the version of the organization.
     *
     * @return The version of the organization.
     */
    public String getVersion() {

        return version;
    }

    /**
     * Sets the version of the organization.
     *
     * @param version The version to set.
     */
    public void setVersion(String version) {

        this.version = version;
    }

    /**
     * Returns the organization handle.
     *
     * @return The organization handle.
     */
    public String getOrganizationHandle() {

        return organizationHandle;
    }

    /**
     * Sets the organization handle.
     *
     * @param organizationHandle The organization handle to set.
     */
    public void setOrganizationHandle(String organizationHandle) {

        this.organizationHandle = organizationHandle;
    }
}
