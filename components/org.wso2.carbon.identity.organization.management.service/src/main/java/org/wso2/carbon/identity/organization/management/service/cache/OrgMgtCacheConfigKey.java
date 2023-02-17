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
 * Cache for identity cache configuration key.
 */
public class OrgMgtCacheConfigKey {

    private String cacheManagerName;
    private String cacheName;

    public OrgMgtCacheConfigKey(String cacheManagerName, String cacheName) {

        this.cacheManagerName = cacheManagerName;
        this.cacheName = cacheName;
    }

    public boolean equals(Object o) {

        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            OrgMgtCacheConfigKey that = (OrgMgtCacheConfigKey) o;
            return !this.cacheManagerName.equals(that.cacheManagerName) ? false : this.cacheName.equals(that.cacheName);
        } else {
            return false;
        }
    }

    public int hashCode() {

        int result = this.cacheManagerName.hashCode();
        result = 31 * result + this.cacheName.hashCode();
        return result;
    }
}
