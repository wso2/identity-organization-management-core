/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * Cache for tenant information against organization Id.
 */
public class TenantDomainCacheByOrgId extends BaseCache<OrganizationIdCacheKey, TenantDomainCacheEntry> {

    private static final String CACHE_NAME = "TenantDomainCacheByOrgId";

    private static volatile TenantDomainCacheByOrgId instance;

    private TenantDomainCacheByOrgId() {

        super(CACHE_NAME);
    }

    public static TenantDomainCacheByOrgId getInstance() {

        if (instance == null) {
            synchronized (TenantDomainCacheByOrgId.class) {
                if (instance == null) {
                    instance = new TenantDomainCacheByOrgId();
                }
            }
        }
        return instance;
    }
}
