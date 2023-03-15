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
import rife.template.ValueRenderer;

/**
 * <p>Translates a template value to/from ROT13.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.Rot13:valueId/--&gt;
 *   {{v render:rife.render.Rot13:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class Rot13 implements ValueRenderer {
    /**
     * Translates a String to/from ROT13.
     *
     * @param src The source String.
     * @return The translated String.
     */
    public static String rot13(String src) {
        if (src == null || src.isEmpty()) {
            return "";
        } else {
            var output = new StringBuilder(src.length());

            for (var i = 0; i < src.length(); i++) {
                var inChar = src.charAt(i);

                if ((inChar >= 'A') && (inChar <= 'Z')) {
                    inChar += (char) 13;

                    if (inChar > 'Z') {
                        inChar -= (char) 26;
                    }
                }

                if ((inChar >= 'a') && (inChar <= 'z')) {
                    inChar += (char) 13;

                    if (inChar > 'z') {
                        inChar -= (char) 26;
                    }
                }

                output.append(inChar);
            }

            return output.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        if (template.hasValueId(differentiator)) {
            return rot13(template.getValue(differentiator));
        } else {
            return "";
        }
    }
}
