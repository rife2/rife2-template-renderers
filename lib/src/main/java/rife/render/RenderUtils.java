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
import rife.tools.Convert;

/**
 * Collection of utility-type methods commonly used by the renderers.
 */
public final class RenderUtils {
    private RenderUtils() {
        // no-op
    }

    /**
     * Fetches the specified value from a template or template's attribute.
     *
     * @param template the template
     * @param valueId  the ID of the value to fetch
     * @return The fetched value.
     */
    public static String fetchValue(Template template, String valueId) {
        Object value = null;
        if (template.hasValueId(valueId)) {
            value = template.getValue(valueId);
        }
        if (value == null && template.hasAttribute(valueId)) {
            value = template.getAttribute(valueId);
        }
        return Convert.toString(value);
    }
}
