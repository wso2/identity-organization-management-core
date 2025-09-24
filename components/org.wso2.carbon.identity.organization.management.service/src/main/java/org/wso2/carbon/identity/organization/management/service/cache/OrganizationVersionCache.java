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

/**
 * Cache for organization version against organization ID.
 */
public class OrganizationVersionCache extends BaseCache<OrganizationIdCacheKey, OrganizationVersionCacheEntry> {

    private static final String CACHE_NAME = "OrganizationVersionCache";
    private static final OrganizationVersionCache INSTANCE = new OrganizationVersionCache();

    private OrganizationVersionCache() {

        super(CACHE_NAME);
    }

    /**
     * Get organization version cache instance.
     *
     * @return Organization version cache instance.
     */
    public static OrganizationVersionCache getInstance() {

        return INSTANCE;
    }
}
