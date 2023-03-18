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
 *   {{v render:rife.render.Uptime/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Uptime">rife.render.Uptime</a>
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
