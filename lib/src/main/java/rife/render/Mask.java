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
import java.util.Properties;

/**
 * <p>Masks characters of a template value.
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
        if (template.hasDefaultValue(valueId)) {
            var properties = new Properties();
            try {
                properties.load(new StringReader(template.getDefaultValue(valueId)));
                mask = properties.getProperty("mask", mask);
                unmasked = Integer.parseInt(properties.getProperty("unmasked", "0"));
                fromStart = "true".equalsIgnoreCase(properties.getProperty("fromStart", "false"));
            } catch (IOException | NumberFormatException ignore) {
                // do nothing
            }
        }
        return RenderUtils.mask(template.getValueOrAttribute(differentiator), mask, unmasked, fromStart);
    }
}
