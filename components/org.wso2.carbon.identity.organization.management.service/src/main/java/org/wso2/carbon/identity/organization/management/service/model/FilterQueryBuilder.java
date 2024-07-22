/*
 * Copyright (c) 2022-2024, WSO2 LLC. (http://www.wso2.com).
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

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.META_ATTRIBUTE_PLACEHOLDER_PREFIX;

/**
 * Create filter query builder.
 */
public class FilterQueryBuilder {

    private Map<String, String> stringParameters = new HashMap<>();
    private List<String> timestampParameters = new ArrayList<>();
    private int count = 1;
    private String filter;
    private List<String> metaAttributePlaceholders = new ArrayList<>();
    private int metaAttributeCount;

    /**
     * Get filter query builder attributes.
     *
     * @return Map of filter query builder attributes.
     */
    public Map<String, String> getFilterAttributeValue() {

        return stringParameters;
    }

    /**
     * Add a filter query builder attribute.
     *
     * @param placeholder filter attribute placeholder.
     * @param value attribute value.
     */
    public void setFilterAttributeValue(String placeholder, String value) {

        stringParameters.put(placeholder + count, value);
        count++;
    }

    /**
     * Get filter query builder attributes.
     *
     * @return List filter query builder timestamp attributes.
     */
    public List<String> getTimestampFilterAttributes() {

        return timestampParameters;
    }

    /**
     * Add a filter query builder timestamp attribute.
     *
     * @param placeholder filter timestamp attribute placeholder.
     */
    public void addTimestampFilterAttributes(String placeholder) {

        timestampParameters.add(placeholder + count);
    }

    /**
     * Set filter query.
     *
     * @param filter filter query.
     */
    public void setFilterQuery(String filter) {

        this.filter = filter;
    }

    /**
     * Get filter query.
     *
     * @return Filter query string.
     */
    public String getFilterQuery() {

        return filter;
    }

    /**
     * Get filter query builder meta attribute placeholders.
     *
     * @return List of filter query builder meta attribute placeholders.
     */
    public List<String> getMetaAttributePlaceholders() {

        return metaAttributePlaceholders;
    }

    /**
     * Generate and add a filter query builder meta attribute placeholder.
     *
     * @return Next meta attribute placeholder.
     */
    public String generateMetaAttributePlaceholder() {

        metaAttributeCount++;
        String placeholder = META_ATTRIBUTE_PLACEHOLDER_PREFIX + metaAttributeCount;
        metaAttributePlaceholders.add(placeholder);
        return placeholder;
    }

    /**
     * Get the current count of meta attribute placeholders.
     *
     * @return Meta attribute count.
     */
    public int getMetaAttributeCount() {

        return metaAttributeCount;
    }
}
