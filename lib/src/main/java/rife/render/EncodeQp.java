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
import rife.tools.Localization;

/**
 * <p>Converts a template value to a quoted-printable string.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.EncodeQp:valueId/--&gt;
 *   {{v render:rife.render.EncodeQp:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class EncodeQp implements ValueRenderer {
    /**
     * Converts the given String to a quoted-printable string.
     *
     * @param src the source String
     * @return the quoted-printable String
     */
    public static String toQuotedPrintable(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }

        char c;
        var buff = new StringBuilder(src.length());
        String hex;

        for (var i = 0; i < src.length(); i++) {
            c = src.charAt(i);

            if (((c > 47) && (c < 58)) || ((c > 64) && (c < 91)) || ((c > 96) && (c < 123))) {
                buff.append(c);
            } else {
                hex = Integer.toString(c, 16);

                buff.append('=');

                if (hex.length() == 1) {
                    buff.append('0');
                }

                buff.append(hex.toUpperCase(Localization.getLocale()));
            }
        }

        return buff.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return toQuotedPrintable(RenderUtils.fetchValue(template, differentiator));
    }
}
