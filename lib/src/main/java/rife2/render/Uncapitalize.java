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

package rife2.render;

import rife.template.Template;
import rife.template.ValueRenderer;

import java.util.Locale;

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
 * @since 1.0
 */
public class Uncapitalize implements ValueRenderer {
    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        if (template.hasValueId(differentiator)) {
            var value = template.getValue(differentiator);
            return value.substring(0, 1).toLowerCase(Locale.getDefault()) + value.substring(1);
        } else {
            return "";
        }
    }
}
