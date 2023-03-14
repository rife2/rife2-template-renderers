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

/**
 * <p>Swap case of a template value.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.SwapCase:valueId/--&gt;
 *   {{v render:rife.render.SwapCase:valueId}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class SwapCase implements ValueRenderer {
    /**
     * Swaps the case of a String.
     * @param s the String to swap the case of
     * @return the modified String or null
     */
    public static String swapCase(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        var buffer = s.toCharArray();
        var whitespace = true;

        for (var i = 0; i < buffer.length; i++) {
            var ch = buffer[i];
            if (Character.isUpperCase(ch) || Character.isTitleCase(ch)) {
                buffer[i] = Character.toLowerCase(ch);
                whitespace = false;
            } else if (Character.isLowerCase(ch)) {
                if (whitespace) {
                    buffer[i] = Character.toTitleCase(ch);
                    whitespace = false;
                } else {
                    buffer[i] = Character.toUpperCase(ch);
                }
            } else {
                whitespace = Character.isWhitespace(ch);
            }
        }
        return new String(buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        if (differentiator != null && !differentiator.isBlank() && template.hasValueId(differentiator)) {
            return swapCase(template.getValue(differentiator));
        } else {
            return "";
        }
    }
}
