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

package org.wso2.carbon.identity.organization.management.service.model;

/**
 * This class represents the model of an organization.
 * This model is used for pagination purposes in get organizations API call.
 */
public class BasicOrganization {

    private String id;
    private String name;
    private String status;
    private String version;
    private String created;
    private String organizationHandle;
    private boolean hasChildren;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getCreated() {

        return created;
    }

    public void setCreated(String created) {

        this.created = created;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
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

    /**
     * Returns whether the organization has child organizations.
     *
     * @return True if the organization has child organizations, false otherwise.
     */
    public boolean hasChildren() {

        return hasChildren;
    }

    /**
     * Sets whether the organization has child organizations.
     *
     * @param hasChildren True if the organization has child organizations, false otherwise.
     */
    public void setHasChildren(boolean hasChildren) {

        this.hasChildren = hasChildren;
    }
}
