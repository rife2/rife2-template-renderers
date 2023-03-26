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
     * {@inheritDoc}
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
            } catch (NumberFormatException ignore) {
                // do nothing
            }
            fromStart = "true".equalsIgnoreCase(properties.getProperty("fromStart", "false"));
        }
        return template.getEncoder().encode(
                RenderUtils.mask(template.getValueOrAttribute(differentiator), mask, unmasked, fromStart));
    }
}
