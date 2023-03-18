/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package rife.render;

import rife.template.Template;
import rife.template.ValueRenderer;

import java.io.IOException;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.util.Properties;

/**
 * Renders the server uptime.
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Uptime/--&gt;
 *   {{v render:rife.render.Uptime:valueId/}}
 * </pre>
 *
 * <p>You can also specify custom formatting using the following properties:</p>
 *
 * <pre>
 *   {{v: render:rife.renader.Uptime}}
 *   year=Y-
 *   years=Y-
 *   month=M-
 *   months=M-
 *   week=W-
 *   weeks=W-
 *   day=D-
 *   days=D-
 *   hour=H-
 *   hours=H-
 *   minute=M-
 *   minutes=M-
 *   {{/v}}
 * </pre>
 *
 * <p>which would render something like</p>
 *
 * <pre>
 *   17Y-2M-2W-1D-9H-33M
 * </pre>
 *
 * <p>You can also specify the uptime via a template attribute</p>
 *
 * <pre>
 *   template.setAttribute(Uptime.class.getName(), 120000L);
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class Uptime implements ValueRenderer {
    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var properties = new Properties();

        if (template.hasDefaultValue(valueId)) {
            try {
                properties.load(new StringReader(template.getDefaultValue(valueId)));
            } catch (IOException ignore) {
                // ignore
            }
        }

        if (template.hasAttribute(Uptime.class.getName())) {
            return RenderUtils.uptime((long) template.getAttribute(Uptime.class.getName()), properties);
        } else {
            return RenderUtils.uptime(ManagementFactory.getRuntimeMXBean().getUptime(), properties);
        }
    }
}
