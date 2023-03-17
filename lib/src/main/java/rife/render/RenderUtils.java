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
import rife.tools.Localization;
import rife.tools.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

/**
 * Collection of utility-type methods commonly used by the renderers.
 */
public final class RenderUtils {
    private RenderUtils() {
        // no-op
    }

    /**
     * Returns the Swatch Internet (.beat) Time for the give date-time.
     *
     * @param zonedDateTime the date and time.
     * @return the .beat time. (eg.: {@code @248})
     */
    public static String beatTime(ZonedDateTime zonedDateTime) {
        var zdt = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC+01:00"));
        var beats = (int) ((zdt.get(ChronoField.SECOND_OF_MINUTE) + (zdt.get(ChronoField.MINUTE_OF_HOUR) * 60) + (zdt.get(ChronoField.HOUR_OF_DAY) * 3600)) / 86.4);
        return String.format("@%03d", beats);
    }

    /**
     * Encodes a string to JavaScript/ECMAScript.
     *
     * @param src the source string
     * @return the enocded string
     */
    public static String encodeJS(String src) {
        if (src == null || src.isBlank()) {
            return src;
        }

        var len = src.length();
        var sb = new StringBuilder(len);

        char c;
        for (var i = 0; i < len; i++) {
            c = src.charAt(i);
            switch (c) {
                case '\'' -> sb.append("\\'");
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '/' -> sb.append("\\/");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Fetches the specified value from a template or template's attribute.
     *
     * @param template the template
     * @param valueId  the ID of the value to fetch
     * @return the fetched value.
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

    /**
     * Returns the last 4 digits a credit card number. The number must satisfy the Luhn algorithm.
     * Non-digits are stripped from the number.
     *
     * @param src the credit card number
     * @return the last 4 digits of the credit card number or empty
     */
    public static String formatCreditCard(String src) {
        if (src == null || src.isBlank()) {
            return src;
        }

        try {
            var cc = src.replaceAll("[^0-9]", "");

            var len = cc.length();
            if (len >= 4) {
                // Luhn algorithm
                var sum = 0;
                boolean isSecond = false;
                int digit;
                for (int i = len - 1; i >= 0; i--) {
                    digit = cc.charAt(i) - '0';
                    if (isSecond) {
                        digit = digit * 2;
                    }
                    sum += digit / 10;
                    sum += digit % 10;

                    isSecond = !isSecond;
                }
                if (sum % 10 == 0) {
                    return cc.substring(len - 4);
                }
            }
        } catch (NumberFormatException ignore) {
            // do nothing
        }

        return "";
    }

    /**
     * Translates a String to/from ROT13.
     *
     * @param src the source String
     * @return the translated String
     */
    public static String rot13(String src) {
        if (src == null || src.isBlank()) {
            return src;
        }

        var len = src.length();
        var output = new StringBuilder(len);

        for (var i = 0; i < len; i++) {
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

    /**
     * <p>Shortens a URL using <a href="https://is.gd/">is.gid</a>. The URL string must a valid http or https URL.</p>
     *
     * <p>Based on <a href="https://github.com/ethauvin/isgd-shorten">isgd-shorten</a></p>
     *
     * @param url the source URL
     * @return the short URL
     */
    public static String shortenUrl(String url) {
        if (url == null || url.isBlank() || !url.matches("^[Hh][Tt][Tt][Pp][Ss]?://\\w.*")) {
            return url;
        }

        var shorten = url;
        try {
            var connection = (HttpURLConnection) new URL(
                    String.format("https://is.gd/create.php?format=simple&url=%s", StringUtils.decodeUrl(url.trim())))
                    .openConnection();
            connection.setRequestProperty(
                    "User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/111.0"
            );
            var responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode <= 399) {
                try (var inputStream = connection.getInputStream()) {
                    shorten = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (IOException ignore) {
            // do nothing
        }
        return shorten;
    }

    /**
     * Swaps the case of a String.
     *
     * @param src the String to swap the case of
     * @return the modified String or null
     */
    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public static String swapCase(String src) {
        if (src == null || src.isBlank()) {
            return src;
        }

        int offset = 0;
        var len = src.length();
        var buff = new int[len];

        for (var i = 0; i < len; ) {
            int newCodePoint;
            var curCodePoint = src.codePointAt(i);
            if (Character.isUpperCase(curCodePoint) || Character.isTitleCase(curCodePoint)) {
                newCodePoint = Character.toLowerCase(curCodePoint);
            } else if (Character.isLowerCase(curCodePoint)) {
                newCodePoint = Character.toUpperCase(curCodePoint);
            } else {
                newCodePoint = curCodePoint;
            }
            buff[offset++] = newCodePoint;
            i += Character.charCount(newCodePoint);
        }
        return new String(buff, 0, offset);
    }

    /**
     * Converts a text string to HTML decimal entities.
     *
     * @param src the String to convert
     * @return the converted String
     */
    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public static String toHtmlEntities(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }

        var len = src.length();
        var sb = new StringBuilder(len * 6);

        // https://stackoverflow.com/a/6766497/8356718
        for (var i = 0; i < len; i++) {
            var codePoint = src.codePointAt(i);
            // Skip over the second char in a surrogate pair
            if (codePoint > 0xffff) {
                i++;
            }
            sb.append(String.format("&#%s;", codePoint));
        }
        return sb.toString();
    }

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

        var len = src.length();
        var buff = new StringBuilder(len);

        char c;
        String hex;
        for (var i = 0; i < len; i++) {
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
}
