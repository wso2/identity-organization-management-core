/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.organization.management.authz.service.constant;

import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_ORGANIZATION_ID;
import static org.wso2.carbon.identity.organization.management.authz.service.constant.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_USER_ID;

/**
 * This class contains database queries related to organization management authorization.
 */
public class SQLConstants {

    public static final String PERMISSION_LIST_PLACEHOLDER = "_PERMISSION_LIST_";
    public static final String CHECK_USER_HAS_PERMISSION_TO_ORG_THROUGH_USER_ROLE_ASSIGNMENT =
            "SELECT COUNT(1) FROM UM_ORG_ROLE_PERMISSION WHERE UM_ORG_ROLE_PERMISSION.UM_ROLE_ID IN ( " +
            "SELECT UM_ORG_ROLE_USER.UM_ROLE_ID FROM UM_ORG_ROLE_USER LEFT JOIN UM_ORG_ROLE ON " +
            "UM_ORG_ROLE_USER.UM_ROLE_ID = UM_ORG_ROLE.UM_ROLE_ID " +
            "WHERE UM_USER_ID = :" + DB_SCHEMA_COLUMN_USER_ID + "; AND " +
            "UM_ORG_ID = :" + DB_SCHEMA_COLUMN_ORGANIZATION_ID + ";)";
    public static final String CHECK_USER_HAS_PERMISSION_TO_ORG_THROUGH_GROUPS_ASSIGNED_TO_ROLES =
            "SELECT COUNT(1) FROM UM_ORG_ROLE_PERMISSION WHERE UM_ORG_ROLE_PERMISSION.UM_ROLE_ID IN ( " +
            "SELECT UM_ORG_ROLE_GROUP.UM_ROLE_ID FROM UM_ORG_ROLE_GROUP LEFT JOIN UM_ORG_ROLE ON " +
            "UM_ORG_ROLE_GROUP.UM_ROLE_ID = UM_ORG_ROLE.UM_ROLE_ID " +
            "WHERE UM_GROUP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_GROUP_ID + "; AND " +
            "UM_ORG_ID = :" + DB_SCHEMA_COLUMN_ORGANIZATION_ID + ";) ";
    public static final String IS_USER_AUTHORIZED = "SELECT COUNT(1) FROM UM_ORG_PERMISSION WHERE " +
            "UM_ORG_PERMISSION.UM_ID IN ( " +
            "SELECT UM_PERMISSION_ID FROM UM_ORG_ROLE_PERMISSION WHERE UM_ORG_ROLE_PERMISSION.UM_ROLE_ID IN ( " +
            "SELECT UM_ORG_ROLE_USER.UM_ROLE_ID FROM UM_ORG_ROLE_USER LEFT JOIN UM_ORG_ROLE ON " +
            "UM_ORG_ROLE_USER.UM_ROLE_ID = UM_ORG_ROLE.UM_ROLE_ID " +
            "WHERE UM_USER_ID = :" + DB_SCHEMA_COLUMN_USER_ID + "; AND " +
            "UM_ORG_ID = :" + DB_SCHEMA_COLUMN_ORGANIZATION_ID + ";) " +
            ") AND UM_RESOURCE_ID IN (" + PERMISSION_LIST_PLACEHOLDER + ")";

    public static final String IS_GROUP_AUTHORIZED = "SELECT COUNT(1) FROM UM_ORG_PERMISSION WHERE " +
            "UM_ORG_PERMISSION.UM_ID IN ( " +
            "SELECT UM_PERMISSION_ID FROM UM_ORG_ROLE_PERMISSION WHERE UM_ORG_ROLE_PERMISSION.UM_ROLE_ID IN ( " +
            "SELECT UM_ORG_ROLE_GROUP.UM_ROLE_ID FROM UM_ORG_ROLE_GROUP LEFT JOIN UM_ORG_ROLE ON " +
            "UM_ORG_ROLE_GROUP.UM_ROLE_ID = UM_ORG_ROLE.UM_ROLE_ID " +
            "WHERE UM_GROUP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_GROUP_ID + "; AND " +
            "UM_ORG_ID = :" + DB_SCHEMA_COLUMN_ORGANIZATION_ID + ";) " +
            ") AND UM_RESOURCE_ID IN (" + PERMISSION_LIST_PLACEHOLDER + ")";

    public static final String GET_ORGANIZATION_ID_BY_NAME = "SELECT UM_ID FROM UM_ORG WHERE UM_ORG_NAME = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_ORGANIZATION_NAME + ";";

    public static final String VIEW_ID_COLUMN = "UM_ID";

    /**
     * SQL placeholders.
     */
    public static final class SQLPlaceholders {

        public static final String DB_SCHEMA_COLUMN_USER_ID = "ID";
        public static final String DB_SCHEMA_COLUMN_GROUP_ID = "ID";
        public static final String DB_SCHEMA_COLUMN_ORGANIZATION_ID = "NAME";
        public static final String DB_SCHEMA_COLUMN_ORGANIZATION_NAME = "NAME";
    }
}
