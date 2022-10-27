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

package org.wso2.carbon.identity.organization.management.service.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create filter query builder.
 */
public class FilterQueryBuilder {

    private Map<String, String> stringParameters = new HashMap<>();
    private List<String> timestampParameters = new ArrayList<>();
    private int count = 1;
    private String filter;

    public Map<String, String> getFilterAttributeValue() {

        return stringParameters;
    }

    public void setFilterAttributeValue(String placeholder, String value) {

        stringParameters.put(placeholder + count, value);
        count++;
    }

    public List<String> getTimestampFilterAttributes() {

        return timestampParameters;
    }

    public void addTimestampFilterAttributes(String placeholder) {

        timestampParameters.add(placeholder + count);
    }

    public void setFilterQuery(String filter) {

        this.filter = filter;
    }

    public String getFilterQuery() {

        return filter;
    }
}
