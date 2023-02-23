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

/**
 * Cache for identity cache configuration.
 */
public class OrgMgtCacheConfig {

    private OrgMgtCacheConfigKey orgMgtCacheConfigKey;
    private boolean isEnabled;
    private int timeout;
    private int capacity;
    private boolean isDistributed = true;
    private boolean isTemporary = false;

    public OrgMgtCacheConfig(OrgMgtCacheConfigKey orgMgtCacheConfigKey) {

        this.orgMgtCacheConfigKey = orgMgtCacheConfigKey;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean isEnabled) {

        this.isEnabled = isEnabled;
    }

    public int getTimeout() {

        return this.timeout;
    }

    public void setTimeout(int timeout) {

        this.timeout = timeout;
    }

    public int getCapacity() {

        return this.capacity;
    }

    public void setCapacity(int capacity) {

        this.capacity = capacity;
    }

    public OrgMgtCacheConfigKey getIdentityCacheConfigKey() {

        return this.orgMgtCacheConfigKey;
    }

    public boolean isDistributed() {

        return this.isDistributed;
    }

    public boolean isTemporary() {

        return this.isTemporary;
    }

    public void setTemporary(boolean temporary) {

        this.isTemporary = temporary;
    }

    public void setDistributed(boolean isDistributed) {

        this.isDistributed = isDistributed;
    }
}
