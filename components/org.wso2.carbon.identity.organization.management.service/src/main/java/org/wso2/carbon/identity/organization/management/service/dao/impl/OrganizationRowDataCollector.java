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

package org.wso2.carbon.identity.organization.management.service.dao.impl;

import java.time.Instant;

/**
 * This class represents a row of data from the database to retrieve an organization.
 */
public class OrganizationRowDataCollector {

    private String id;
    private String name;
    private String description;
    private String parentId;
    private String type;
    private int tenantId;
    private String status;
    private Instant created;
    private Instant lastModified;
    private String attributeKey;
    private String attributeValue;

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

    String getParentId() {

        return parentId;
    }

    public void setParentId(String parentId) {

        this.parentId = parentId;
    }

    public Instant getCreated() {

        return created;
    }

    public void setCreated(Instant created) {

        this.created = created;
    }

    public Instant getLastModified() {

        return lastModified;
    }

    public void setLastModified(Instant lastModified) {

        this.lastModified = lastModified;
    }

    public String getAttributeKey() {

        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {

        this.attributeKey = attributeKey;
    }

    public String getAttributeValue() {

        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {

        this.attributeValue = attributeValue;
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

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }
}
