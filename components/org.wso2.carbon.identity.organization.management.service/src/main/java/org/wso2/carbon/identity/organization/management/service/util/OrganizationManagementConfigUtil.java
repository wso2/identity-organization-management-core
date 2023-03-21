/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.organization.management.service.util;

import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.identity.organization.management.service.cache.OrgMgtCacheConfig;
import org.wso2.carbon.identity.organization.management.service.cache.OrgMgtCacheConfigKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class to find Organization management configs.
 */
public class OrganizationManagementConfigUtil {

    private static Map<String, Object> orgMgtConfigurations = new HashMap<>();
    private static Map<OrgMgtCacheConfigKey, OrgMgtCacheConfig> orgMgtCacheConfigurations = new HashMap();

    public static void loadOrgMgtConfigurations() {

        orgMgtConfigurations = OrganizationManagementConfigBuilder.getInstance().getOrgMgtConfigurations();
        orgMgtCacheConfigurations = OrganizationManagementConfigBuilder.getInstance()
                .getOrgMgtCacheConfigurations();
    }

    /**
     * Return the config value for a given config key.
     *
     * @param key config key. It should be in the format of concatenating XML tags by a period.
     * @return Config value in the file.
     */
    public static String getProperty(String key) {

        Object value = orgMgtConfigurations.get(key);
        String strValue;

        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            value = ((List<?>) value).get(0);
        }
        if (value instanceof String) {
            strValue = (String) value;
        } else {
            strValue = String.valueOf(value);
        }
        return strValue;
    }

    /**
     * This reads the CacheConfig configuration in organization-mgt.xml.
     * Since the name of the cache is different between the distributed mode and local mode, that is specially handled.
     */
    public static OrgMgtCacheConfig getOrgMgtCacheConfig(String cacheManagerName, String cacheName) {

        OrgMgtCacheConfigKey configKey = new OrgMgtCacheConfigKey(cacheManagerName, cacheName);
        OrgMgtCacheConfig orgMgtCacheConfig = (OrgMgtCacheConfig) orgMgtCacheConfigurations.get(configKey);
        if (orgMgtCacheConfig == null && cacheName.startsWith(CachingConstants.LOCAL_CACHE_PREFIX)) {
            configKey = new OrgMgtCacheConfigKey(cacheManagerName,
                    cacheName.replace(CachingConstants.LOCAL_CACHE_PREFIX, ""));
            orgMgtCacheConfig = (OrgMgtCacheConfig) orgMgtCacheConfigurations.get(configKey);
        }
        return orgMgtCacheConfig;
    }

    /**
     * Read configuration elements defined as lists.
     *
     * @param key Element Name as specified from the parent elements in the XML structure.
     *            To read the element value of b in {@code <a><b>t1</b><b>t2</b></a>},
     *            the property name should be passed as "a.b" to get a list of b
     * @return String list from the config element passed in as key.
     */
    public static List<String> getPropertyAsList(String key) {

        List<String> propertyList = new ArrayList<>();
        Object value = orgMgtConfigurations.get(key);

        if (value == null) {
            return propertyList;
        }
        if (value instanceof List) {
            List rawProps = (List) value;
            for (Object rawProp: rawProps) {
                if (rawProp instanceof String) {
                    propertyList.add((String) rawProp);
                } else {
                    propertyList.add(String.valueOf(rawProp));
                }
            }
        } else if (value instanceof String) {
            propertyList.add((String) value);
        } else {
            propertyList.add(String.valueOf(value));
        }
        return propertyList;
    }
}
