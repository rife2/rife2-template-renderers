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

import java.time.ZonedDateTime;

/**
 * <p>Renders the current time in ISO 8601 format.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.TimeIso/--&gt;
 *   {{v render:rife.render.TimeIso/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.TimeIso">rife.render.TimeIso</a>
 * @since 1.0
 */
public class TimeIso implements ValueRenderer {
    /**
     * Returns the current time in ISO 8601 format.
     *
     * @param template       the template that is currently being rendered
     * @param valueId        the id of the value to be rendered
     * @param differentiator a differentiator that may be used to differentiate the rendering of this value renderer
     * @return the current time in ISO 8601 format
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return template.getEncoder().encode(ZonedDateTime.now().format(RenderUtils.ISO_8601_TIME_FORMATTER));
    }
}
