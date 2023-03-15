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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>Return the current date and time in RFC 2822 format.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.DateTimeRfc2822/--&gt;
 *   {{v render:rife.render.DateTimeRfc2822/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class DateTimeRfc2822 implements ValueRenderer {
    /**
     * RFC 2822 date and time formatter.
     */
    static public final DateTimeFormatter rfc2822Formatter =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz").withLocale(Localization.getLocale());

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        return ZonedDateTime.now().format(rfc2822Formatter);
    }
}
