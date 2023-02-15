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

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Organization Id cache key.
 */
public class OrganizationIdCacheKey extends CacheKey {

    private String orgId;

    public OrganizationIdCacheKey(String orgId) {
        this.orgId = orgId;
    }

    public String getOrganizationId() {
        return orgId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        OrganizationIdCacheKey that = (OrganizationIdCacheKey) o;

        return orgId.equals(that.orgId);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + orgId.hashCode();
        return result;
    }
}
