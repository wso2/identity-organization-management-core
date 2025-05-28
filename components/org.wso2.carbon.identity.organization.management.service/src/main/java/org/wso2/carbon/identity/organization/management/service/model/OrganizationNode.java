/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an organization node in the organization tree.
 */
public class OrganizationNode {

    private String id;
    private String name;
    private String created;
    private String organizationHandle;
    private String parentId;
    private List<OrganizationNode> children;

    public OrganizationNode(String id, String name, String created, String organizationHandle, String parentId) {

        this.id = id;
        this.name = name;
        this.created = created;
        this.organizationHandle = organizationHandle;
        this.parentId = parentId;
        this.children = new ArrayList<>();
    }

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

    public String getCreated() {

        return created;
    }

    public void setCreated(String created) {

        this.created = created;
    }

    public String getOrganizationHandle() {

        return organizationHandle;
    }

    public void setOrganizationHandle(String organizationHandle) {

        this.organizationHandle = organizationHandle;
    }

    public String getParentId() {

        return parentId;
    }

    public void setParentId(String parentId) {

        this.parentId = parentId;
    }

    public List<OrganizationNode> getChildren() {

        return children;
    }

    public void setChildren(List<OrganizationNode> children) {

        this.children = children;
    }

    public void addChild(OrganizationNode child) {

        this.children.add(child);
    }

    @Override
    public String toString() {

        return "OrganizationNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", parentId='" + parentId + '\'' +
                ", childrenCount=" + children.size() +
                '}';
    }
}
