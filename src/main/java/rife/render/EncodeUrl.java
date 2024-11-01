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
 * <p>URL-encodes a template value.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.EncodeUrl:valueId/--&gt;
 *   {{v render:rife.render.EncodeUrl:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeUrl">rife.render.EncodeUrl</a>
 * @see StringUtils#encodeUrl(String)
 * @since 1.0
 */
public class EncodeUrl implements ValueRenderer {
    /**
     * Returns the template value encoded to URL.
     *
     * @param template       the template that contains the value
     * @param valueId        the id of the value
     * @param differentiator the differentiator to use
     * @return the URL-encoded value
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var properties = RenderUtils.parsePropertiesString(template.getDefaultValue(valueId));
        return RenderUtils.encode(StringUtils.encodeUrl(template.getValueOrAttribute(differentiator)), properties);
    }
}
