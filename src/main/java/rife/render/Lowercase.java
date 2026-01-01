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
import rife.tools.Localization;

/**
 * <p>Converts a template value to lowercase.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Lowercase:valueId/--&gt;
 *   {{v render:rife.render.Lowercase:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Lowercase">rife.render.Lowercase</a>
 * @since 1.0
 */
public class Lowercase implements ValueRenderer {
    /**
     * Returns the template value converted to lowercase.
     *
     * @param template       the template that contains the value
     * @param valueId        the id of the value
     * @param differentiator the differentiator to use
     * @return the lowercase value
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var value = template.getValueOrAttribute(differentiator);
        if (value == null || value.isBlank()) {
            return value;
        }
        return template.getEncoder().encode(value.toLowerCase(Localization.getLocale()));
    }
}
