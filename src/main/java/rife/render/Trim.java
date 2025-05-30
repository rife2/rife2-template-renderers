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
 * <p>Removes leading and trailing whitespace from a template value.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Trim:valueId/--&gt;
 *   {{v render:rife.render.Trim:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Trim">rife.render.Trim</a>
 * @since 1.0
 */
public class Trim implements ValueRenderer {
    /**
     * Renders the template value by removing leading and trailing whitespace.
     *
     * @param template       the template instance
     * @param valueId        the id of the value to render
     * @param differentiator an optional differentiator to use for cache invalidation
     * @return the trimmed value, or the original value if it is {@code null} or empty
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var value = template.getValueOrAttribute(differentiator);
        if (value == null || value.isEmpty()) {
            return value;
        }
        return template.getEncoder().encode(value.trim());
    }
}
