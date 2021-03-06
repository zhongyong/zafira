/*******************************************************************************
 * Copyright 2013-2019 Qaprosoft (http://www.qaprosoft.com).
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
package com.qaprosoft.zafira.models.dto.widget;

import com.qaprosoft.zafira.models.db.WidgetTemplate;
import com.qaprosoft.zafira.models.dto.AbstractType;

public class WidgetTemplateType extends AbstractType {

    private static final long serialVersionUID = 7998270816228259812L;

    private String name;
    private String description;
    private WidgetTemplate.Type type;
    private String chartConfig;
    private String paramsConfig;
    private String legendConfig;
    private Boolean hidden;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WidgetTemplate.Type getType() {
        return type;
    }

    public void setType(WidgetTemplate.Type type) {
        this.type = type;
    }

    public String getChartConfig() {
        return chartConfig;
    }

    public void setChartConfig(String chartConfig) {
        this.chartConfig = chartConfig;
    }

    public String getParamsConfig() {
        return paramsConfig;
    }

    public void setParamsConfig(String paramsConfig) {
        this.paramsConfig = paramsConfig;
    }

    public String getLegendConfig() {
        return legendConfig;
    }

    public void setLegendConfig(String legendConfig) {
        this.legendConfig = legendConfig;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
