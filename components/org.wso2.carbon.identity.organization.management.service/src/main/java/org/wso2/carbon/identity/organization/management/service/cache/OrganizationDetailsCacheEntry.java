/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.organization.management.service.cache;

import java.util.List;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.DEFAULT_ORGANIZATION_DEPTH_IN_HIERARCHY;

/**
 * Organization details cache entry.
 */
public class OrganizationDetailsCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 6281482632454325986L;

    private String orgName;
    private String status;
    private String type;
    private List<String> ancestorOrganizationIds;
    private int organizationDepthInHierarchy;

    public OrganizationDetailsCacheEntry(Builder builder) {

        this.orgName = builder.orgName;
        this.status = builder.status;
        this.type = builder.type;
        this.ancestorOrganizationIds = builder.ancestorOrganizationIds;
        this.organizationDepthInHierarchy = builder.organizationDepthInHierarchy;
    }

    public String getOrgName() {

        return orgName;
    }

    public void setOrgName(String orgName) {

        this.orgName = orgName;
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

    public List<String> getAncestorOrganizationIds() {

        return ancestorOrganizationIds;
    }

    public void setAncestorOrganizationIds(List<String> ancestorOrganizationIds) {

        this.ancestorOrganizationIds = ancestorOrganizationIds;
    }

    public int getOrganizationDepthInHierarchy() {

        return organizationDepthInHierarchy;
    }

    public void setOrganizationDepthInHierarchy(int organizationDepthInHierarchy) {

        this.organizationDepthInHierarchy = organizationDepthInHierarchy;
    }

    /**
     * Builder class for OrganizationDetailsCacheEntry object.
     */
    public static class Builder {

        private String orgName;
        private String status;
        private String type;
        private List<String> ancestorOrganizationIds;
        private int organizationDepthInHierarchy = DEFAULT_ORGANIZATION_DEPTH_IN_HIERARCHY;

        public Builder setOrgName(String orgName) {

            this.orgName = orgName;
            return this;
        }

        public Builder setStatus(String status) {

            this.status = status;
            return this;
        }

        public Builder setType(String type) {

            this.type = type;
            return this;
        }

        public Builder setAncestorOrganizationIds(List<String> ancestorOrganizationIds) {

            this.ancestorOrganizationIds = ancestorOrganizationIds;
            return this;
        }

        public Builder setOrganizationDepthInHierarchy(int organizationDepthInHierarchy) {

            this.organizationDepthInHierarchy = organizationDepthInHierarchy;
            return this;
        }

        public OrganizationDetailsCacheEntry build() {

            return new OrganizationDetailsCacheEntry(this);
        }
    }
}
