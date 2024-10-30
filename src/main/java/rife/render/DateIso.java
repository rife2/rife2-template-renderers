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

import java.time.ZonedDateTime;

/**
 * <p>Renders the current date in ISO 8601 format.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.DateIso/--&gt;
 *   {{v render:rife.render.DateIso/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.DateIso">rife.render.DateIso</a>
 * @since 1.0
 */
public class DateIso implements ValueRenderer {
    /**
     * Returns the current date in ISO 8601 format, encoded according to the template's encoding rules.
     *
     * @param template       the template that is currently being rendered
     * @param valueId        the value id that triggers the rendering of this value renderer
     * @param differentiator a differentiator that may be used to differentiate the rendering of this value renderer
     * @return the current date in ISO 8601 format, encoded according to the template's encoding rules
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return template.getEncoder().encode(ZonedDateTime.now().format(RenderUtils.ISO_8601_DATE_FORMATTER));
    }
}
