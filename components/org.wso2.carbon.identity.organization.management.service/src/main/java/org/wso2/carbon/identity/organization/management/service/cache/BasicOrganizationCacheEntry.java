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

import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;

/**
 * Basic organization cache entry.
 */
public class BasicOrganizationCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 202310121234567890L;

    private BasicOrganization basicOrganization;

    public BasicOrganizationCacheEntry(BasicOrganization basicOrganization) {

        this.basicOrganization = basicOrganization;
    }

    public BasicOrganization getBasicOrganization() {

        return basicOrganization;
    }

    public void setBasicOrganization(BasicOrganization basicOrganization) {

        this.basicOrganization = basicOrganization;
    }
}
