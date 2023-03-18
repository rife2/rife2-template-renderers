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

import java.io.IOException;
import java.io.StringReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * <p>Return the current date and time in ISO 8601 format.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.DateTimeIso/--&gt;
 *   {{v render:rife.render.DateTimeIso/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class DateTimeIso implements ValueRenderer {
    /**
     * ISO 8601 date and time formatter.
     */
    static public final DateTimeFormatter iso8601Formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXXXX").withLocale(Localization.getLocale());

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        if (template.hasDefaultValue(valueId)) {
            var properties = new Properties();
            try {
                var tz = "tz";
                properties.load(new StringReader(template.getDefaultValue(valueId)));
                if (properties.containsKey(tz)) {
                    return ZonedDateTime.now().format(
                            DateTimeIso.iso8601Formatter.withZone(ZoneId.of(properties.getProperty(tz))));
                }
            } catch (IOException ignore) {
                // do nothing
            }

        }
        return ZonedDateTime.now().format(iso8601Formatter);
    }
}
