/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * This class represents a minimal model of an organization.
 * Attributes in this class should not depend on changes on ancestor or child organizations.
 */
public class MinimalOrganization {

    private final String id;
    private final String name;
    private final String status;
    private final String created;
    private final String organizationHandle;
    private final String parentOrganizationId;
    private final int depth;

    private MinimalOrganization(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.status = builder.status;
        this.created = builder.created;
        this.organizationHandle = builder.organizationHandle;
        this.parentOrganizationId = builder.parentOrganizationId;
        this.depth = builder.depth;
    }

    /**
     * Returns the unique identifier of the organization.
     *
     * @return The unique identifier of the organization.
     */
    public String getId() {

        return id;
    }

    /**
     * Returns the creation time of the organization.
     *
     * @return The creation time of the organization.
     */
    public String getCreated() {

        return created;
    }

    /**
     * Returns the name of the organization.
     *
     * @return The name of the organization.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the status of the organization.
     *
     * @return The status of the organization.
     */
    public String getStatus() {

        return status;
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
     * Returns the ID of the parent organization.
     *
     * @return The ID of the parent organization.
     */
    public String getParentOrganizationId() {

        return parentOrganizationId;
    }

    /**
     * Returns the depth of the organization in the hierarchy.
     *
     * @return The depth of the organization.
     */
    public int getDepth() {

        return depth;
    }

    /**
     * Builder class for creating instances of MinimalOrganization.
     */
    public static class Builder {

        private String id;
        private String name;
        private String status;
        private String created;
        private String organizationHandle;
        private String parentOrganizationId;
        private int depth;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder status(String status) {

            this.status = status;
            return this;
        }

        public Builder created(String created) {

            this.created = created;
            return this;
        }

        public Builder organizationHandle(String organizationHandle) {

            this.organizationHandle = organizationHandle;
            return this;
        }

        public Builder parentOrganizationId(String parentOrganizationId) {

            this.parentOrganizationId = parentOrganizationId;
            return this;
        }

        public Builder depth(int depth) {

            this.depth = depth;
            return this;
        }

        public MinimalOrganization build() {

            return new MinimalOrganization(this);
        }
    }
}
