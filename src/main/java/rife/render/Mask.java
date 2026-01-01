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
 * <p>Masks characters of a template value.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Mask:valueId/--&gt;
 *   {{v render:rife.render.Mask:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Mask">rife.render.Mask</a>
 * @since 1.0
 */
public class Mask implements ValueRenderer {

    /**
     * <p>Renders a template value with characters of the value masked using the specified mask.</p>
     *
     * <p>The mask is specified as a template default value with the following syntax:</p>
     *
     * <pre>
     *   mask=&lt;mask&gt;[,unmasked=&lt;unmasked&gt;][,fromStart=&lt;fromStart&gt;]
     * </pre>
     *
     * <p>Where:</p>
     *
     * <ul>
     *   <li><var>mask</var> is the character to use for masking, defaulting to <code>*</code></li>
     *   <li><var>unmasked</var> is the number of characters at the beginning of the value that should be left unmasked,
     *   defaulting to <code>0</code></li>
     *   <li><var>fromStart</var> is a boolean indicating whether the <var>unmasked</var> value should be counted from
     *   the start of the value, defaulting to <code>false</code></li>
     * </ul>
     *
     * @param template       the template to render the value in
     * @param valueId        the ID of the value to render
     * @param differentiator the differentiator of the value to render
     * @return the rendered value
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var mask = "*";
        var unmasked = 0;
        var fromStart = false;
        var defaultValue = template.getDefaultValue(valueId);
        if (defaultValue != null && !defaultValue.isBlank()) {
            var properties = RenderUtils.parsePropertiesString(defaultValue);
            mask = properties.getProperty("mask", mask);
            try {
                unmasked = Integer.parseInt(properties.getProperty("unmasked", "0"));
            } catch (NumberFormatException ignored) {
                // do nothing
            }
            fromStart = "true".equalsIgnoreCase(properties.getProperty("fromStart", "false"));
        }
        return template.getEncoder().encode(
                RenderUtils.mask(template.getValueOrAttribute(differentiator), mask, unmasked, fromStart));
    }
}
