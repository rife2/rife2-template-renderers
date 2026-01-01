/*
 *  Copyright 2023-2026 the original author or authors.
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

import java.lang.management.ManagementFactory;

/**
 * Renders the server uptime.
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Uptime/--&gt;
 *   {{v render:rife.render.Uptime/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Uptime">rife.render.Uptime</a>
 * @since 1.0
 */
public class Uptime implements ValueRenderer {
    /**
     * Renders the server uptime.
     *
     * @param template       the template that is currently being rendered
     * @param valueId        the id of the value to render
     * @param differentiator a differentiator that may be used to differentiate the rendering of this value renderer
     * @return the server uptime
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var properties = RenderUtils.parsePropertiesString(template.getDefaultValue(valueId));
        String uptime;
        if (template.hasAttribute(Uptime.class.getName())) {
            uptime = RenderUtils.uptime((long) template.getAttribute(Uptime.class.getName()), properties);
        } else {
            uptime = RenderUtils.uptime(ManagementFactory.getRuntimeMXBean().getUptime(), properties);
        }

        return template.getEncoder().encode(uptime);
    }
}
