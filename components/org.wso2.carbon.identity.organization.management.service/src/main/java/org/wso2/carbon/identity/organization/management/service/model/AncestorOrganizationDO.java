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
 * This class represents the ancestor organization of an organization.
 */
public class AncestorOrganizationDO {

    private String id;
    private String name;
    private int depth;

    /**
     * Get the ID of the ancestor organization.
     *
     * @return ID of the ancestor organization.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the ID of the ancestor organization.
     *
     * @param id ID of the ancestor organization.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Name of the ancestor organization.
     */
    public String getName() {

        return name;
    }

    /**
     * Name of the ancestor organization.
     *
     * @param name Name of the ancestor organization.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Depth of the ancestor organization in the organization hierarchy.
     */
    public int getDepth() {

        return depth;
    }

    /**
     * Set the depth of the ancestor organization in the organization hierarchy.
     *
     * @param depth Depth of the ancestor organization.
     */
    public void setDepth(int depth) {

        this.depth = depth;
    }
}
