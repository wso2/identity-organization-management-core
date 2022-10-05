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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class to find Organization management configs.
 */
public class OrganizationManagementConfigUtil {

    private static Map<String, Object> orgMgtConfigurations = new HashMap<>();

    public static void loadOrgMgtConfigurations() {

        orgMgtConfigurations = OrganizationManagementConfigBuilder.getInstance().getOrgMgtConfigurations();
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
}
