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
import rife.tools.StringUtils;

/**
 * <p>Un-capitalizes a template value.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Uncapitalize:valueId/--&gt;
 *   {{v render:rife.render.Uncapitalize:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Uncapitalize">rife.render.Uncapitalize</a>
 * @since 1.0
 */
public class Uncapitalize implements ValueRenderer {
    /**
     * Returns the un-capitalized template value.
     *
     * @param template       the template to render
     * @param valueId        the id of the value to render
     * @param differentiator the differentiator to use for the value lookup
     * @return the un-capitalized value
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return template.getEncoder().encode(StringUtils.uncapitalize(template.getValueOrAttribute(differentiator)));
    }
}
