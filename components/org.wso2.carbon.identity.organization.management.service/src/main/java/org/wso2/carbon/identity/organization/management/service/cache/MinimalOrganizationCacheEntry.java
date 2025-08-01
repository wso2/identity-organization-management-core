/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;

/**
 * Minimal organization cache entry.
 */
public class MinimalOrganizationCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 202310121234567890L;

    private String id;
    private String name;
    private String status;
    private String created;
    private String organizationHandle;
    private String parentOrganizationId;
    private int depth;

    public MinimalOrganizationCacheEntry(MinimalOrganization minimalOrganization) {

        this.id = minimalOrganization.getId();
        this.name = minimalOrganization.getName();
        this.status = minimalOrganization.getStatus();
        this.created = minimalOrganization.getCreated();
        this.organizationHandle = minimalOrganization.getOrganizationHandle();
        this.parentOrganizationId = minimalOrganization.getParentOrganizationId();
        this.depth = minimalOrganization.getDepth();
    }

    public MinimalOrganization getMinimalOrganization() {

        return new MinimalOrganization.Builder()
                .id(this.id)
                .name(this.name)
                .status(this.status)
                .created(this.created)
                .organizationHandle(this.organizationHandle)
                .parentOrganizationId(this.parentOrganizationId)
                .depth(this.depth)
                .build();
    }

    public void setMinimalOrganization(MinimalOrganization minimalOrganization) {

        this.id = minimalOrganization.getId();
        this.name = minimalOrganization.getName();
        this.status = minimalOrganization.getStatus();
        this.created = minimalOrganization.getCreated();
        this.organizationHandle = minimalOrganization.getOrganizationHandle();
        this.parentOrganizationId = minimalOrganization.getParentOrganizationId();
        this.depth = minimalOrganization.getDepth();
    }
}
