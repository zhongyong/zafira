/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.services.services.application.integration.impl;

import static com.qaprosoft.zafira.models.db.Setting.SettingType.JIRA_CLOSED_STATUS;
import static com.qaprosoft.zafira.models.db.Setting.Tool.JIRA;

import java.util.List;

import com.qaprosoft.zafira.services.services.application.integration.AbstractIntegration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.zafira.models.db.Setting;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.application.SettingsService;
import com.qaprosoft.zafira.services.services.application.integration.context.JiraContext;

import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import org.springframework.stereotype.Component;

@Component
public class JiraService extends AbstractIntegration<JiraContext> {

    private static final Logger LOGGER = Logger.getLogger(JiraService.class);

    private final SettingsService settingsService;
    private final CryptoService cryptoService;

    public JiraService(SettingsService settingsService, CryptoService cryptoService) {
        super(JIRA);
        this.settingsService = settingsService;
        this.cryptoService = cryptoService;
    }

    @Override
    public void init() {

        String url = null;
        String username = null;
        String password = null;
        boolean enabled = false;

        try {
            List<Setting> jiraSettings = settingsService.getSettingsByTool(JIRA);
            for (Setting setting : jiraSettings) {
                if (setting.isEncrypted()) {
                    setting.setValue(cryptoService.decrypt(setting.getValue()));
                }
                switch (Setting.SettingType.valueOf(setting.getName())) {
                case JIRA_URL:
                    url = setting.getValue();
                    break;
                case JIRA_USER:
                    username = setting.getValue();
                    break;
                case JIRA_PASSWORD:
                    password = setting.getValue();
                    break;
                case JIRA_ENABLED:
                    enabled = Boolean.valueOf(setting.getValue());
                    break;
                default:
                    break;
                }
            }
            init(url, username, password, enabled);
        } catch (Exception e) {
            LOGGER.error("Setting does not exist", e);
        }
    }

    public void init(String url, String username, String password, boolean enabled) {
        try {
            if (!StringUtils.isEmpty(url) && !StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
                putContext(new JiraContext(url, username, password, enabled));
            }
        } catch (Exception e) {
            LOGGER.error("Unable to initialize Jira integration: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        boolean connected = false;
        try {
            connected = getJiraClient() != null && getJiraClient().getProjects() != null;
        } catch (Exception e) {
            LOGGER.error("Unable to connect to JIRA", e);
        }
        return connected;
    }

    public Issue getIssue(String ticket) {
        Issue issue = null;
        try {
            issue = getJiraClient().getIssue(ticket);
        } catch (Exception e) {
            LOGGER.error("Unable to find Jira issue: " + ticket, e);
        }
        return issue;
    }

    public boolean isIssueClosed(String ticket) throws ServiceException {
        Issue issue = getIssue(ticket);
        return isIssueClosed(issue);
    }

    public boolean isIssueClosed(Issue issue) throws ServiceException {
        boolean isIssueClosed = false;
        String[] closeStatuses = settingsService.getSettingValue(JIRA_CLOSED_STATUS).split(";");
        for (String closeStatus : closeStatuses) {
            if (issue.getStatus().getName().equalsIgnoreCase(closeStatus)) {
                isIssueClosed = true;
            }
        }
        return isIssueClosed;
    }

    public JiraClient getJiraClient() {
        return getContext() != null ? getContext().getJiraClient() : null;
    }
}