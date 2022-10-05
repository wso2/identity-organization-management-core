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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.ORGANIZATION_MGT_CONFIG_FILE;

/**
 * Config builder class for organization management related configs in organization-mgt.xml file.
 */
public class OrganizationManagementConfigBuilder {

    private static final Log LOG = LogFactory.getLog(OrganizationManagementConfigBuilder.class);
    private static final Map<String, Object> orgMgtConfigurations = new HashMap<>();
    private static final OrganizationManagementConfigBuilder organizationManagementConfigBuilder =
            new OrganizationManagementConfigBuilder();

    public static OrganizationManagementConfigBuilder getInstance() {

        return organizationManagementConfigBuilder;
    }

    private OrganizationManagementConfigBuilder() {

        loadConfigurations();
    }

    /**
     * Get organization management related configs.
     *
     * @return Map of org mgt configs.
     */
    public Map<String, Object> getOrgMgtConfigurations() {

        return orgMgtConfigurations;
    }

    /**
     * Read the organization-mgt.xml file and build the configuration map.
     */
    @SuppressWarnings(value = "PATH_TRAVERSAL_IN", justification = "Don't use any user input file.")
    private void loadConfigurations() {

        String configDirPath = CarbonUtils.getCarbonConfigDirPath();
        File configFile = new File(configDirPath, FilenameUtils.getName(ORGANIZATION_MGT_CONFIG_FILE));
        if (!configFile.exists()) {
            return;
        }
        try (InputStream stream = Files.newInputStream(configFile.toPath())) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            // Prevents using external resources when parsing xml.
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            // Prevents using external document type definition when parsing xml.
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader parser = factory.createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<>();
            readChildElements(documentElement, nameStack);
        } catch (IOException e) {
            LOG.warn("Error while loading organization management configs.", e);
        } catch (XMLStreamException e) {
            LOG.warn("Error while streaming organization management configs.", e);
        }
    }

    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element) &&  orgMgtConfigurations != null) {
                String key = getKey(nameStack);
                Object currentObject = orgMgtConfigurations.get(key);
                String value = replaceSystemProperty(element.getText());

                if (currentObject == null) {
                    orgMgtConfigurations.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                        orgMgtConfigurations.put(key, list);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList arrayList = new ArrayList(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        orgMgtConfigurations.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
    }

    private String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        /*
        The following condition deals with properties.
        Properties are specified as ${system.property},and are assumed to be System properties
         */
        StringBuilder textBuilder = new StringBuilder(text);
        while (indexOfStartingChars < textBuilder.indexOf("${") &&
                (indexOfStartingChars = textBuilder.indexOf("${")) != -1 &&
                (indexOfClosingBrace = textBuilder.indexOf("}")) != -1) {
            String sysProp = textBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue +
                        textBuilder.substring(indexOfClosingBrace + 1));
            }
            if (sysProp.equals(ServerConstants.CARBON_HOME) &&
                    System.getProperty(ServerConstants.CARBON_HOME).equals(".")) {
                textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator);
            }
        }
        return textBuilder.toString();
    }
}
