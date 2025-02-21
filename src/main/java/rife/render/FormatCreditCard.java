/*
 *  Copyright 2023-2024 the original author or authors.
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
 * Formats a template credit card number value to the last 4 digits.
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.FormatCreditCard:valueId/--&gt;
 *   {{v render:rife.render.FormatCreditCard:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.FormatCreditCard">rife.render.FormatCreditCard</a>
 * @since 1.0
 */
public class FormatCreditCard implements ValueRenderer {
    /**
     * Returns the last 4 digits of the template credit number value.
     *
     * @param template       the {@link Template}
     * @param valueId        the value id
     * @param differentiator the differentiator
     * @return the formatted value
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return template.getEncoder().encode(RenderUtils.formatCreditCard(template.getValueOrAttribute(differentiator)));
    }
}
