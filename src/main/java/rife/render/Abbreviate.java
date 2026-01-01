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

/**
 * <p>Abbreviates a template value with ellipses.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Abbreviate:valueId/--&gt;
 *   {{v render:rife.render.Abbreviate:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Abbreviate">rife.render.Abbreviate</a>
 * @since 1.0
 */
public class Abbreviate implements ValueRenderer {
    /**
     * <p>Returns the template value abbreviated with ellipses.</p>
     *
     * <p>Two parameters can be specified:</p>
     * <ul>
     *   <li><code>mark</code>: the string that will be used to abbreviate the value. Default is <code>...</code></li>
     *   <li><code>max</code>: the maximum number of characters to render. Default is <code>-1</code> (no abbreviation).</li>
     * </ul>
     *
     * @param template       the template that contains the value
     * @param valueId        the id of the value to render
     * @param differentiator a generic string that can be used to differentiate the rendering
     * @return the abbreviated value, or the original value if no abbreviation is necessary
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var mark = "...";
        var max = -1;
        var defaultValue = template.getDefaultValue(valueId);
        if (defaultValue != null) {
            var properties = RenderUtils.parsePropertiesString(defaultValue);
            mark = properties.getProperty("mark", mark);
            max = Integer.parseInt(properties.getProperty("max", String.valueOf(max)));
        }

        return template.getEncoder().encode(
                RenderUtils.abbreviate(template.getValueOrAttribute(differentiator), max, mark));
    }
}