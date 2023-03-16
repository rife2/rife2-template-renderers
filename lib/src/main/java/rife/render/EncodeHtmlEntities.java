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
 * <p>Encodes a template value to HTML decimal entities</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.EncodeHtmlEntities:valueId/--&gt;
 *   {{v render:rife.render.EncodeHtmlEntities:valueId/}}
 * </pre>
 *
 * <p>For example {@code john@doe.com} would be encoded to:</p>
 *
 * <pre>&amp;#106;&amp;#111;&amp;#104;&amp;#110;&amp;#64;&amp;#100;&amp;#111;&amp;#101;&amp;#46;&amp;#99;&amp;#111;&amp;#109;</pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class EncodeHtmlEntities implements ValueRenderer {
    /**
     * Converts a text string to HTML decimal entities.
     *
     * @param text the String to convert.
     * @return the converted string.
     */
    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public static String toHtmlEntities(String text) {
        // https://stackoverflow.com/a/6766497/8356718
        var sb = new StringBuilder(text.length() * 6);
        for (var i = 0; i < text.length(); i++) {
            var codePoint = text.codePointAt(i);
            // Skip over the second char in a surrogate pair
            if (codePoint > 0xffff) {
                i++;
            }
            sb.append(String.format("&#%s;", codePoint));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return toHtmlEntities(RenderUtils.fetchValue(template, differentiator));
    }
}
