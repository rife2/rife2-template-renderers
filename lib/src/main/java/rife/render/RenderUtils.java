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

import rife.tools.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Collection of utility-type methods commonly used by the renderers.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class RenderUtils {
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/111.0";

    private RenderUtils() {
        // no-op
    }

    /**
     * Abbreviates a String to the given length using a replacement marker.
     *
     * @param src    the source String
     * @param max    the maximum length of the resulting String
     * @param marker the String used as a replacement marker
     * @return the abbreviated String
     */
    public static String abbreviate(String src, int max, String marker) {
        if (src == null || src.isBlank() || marker == null) {
            return src;
        }

        var len = src.length();

        if (len <= max || max < 0) {
            return src;
        }

        return src.substring(0, max - marker.length()) + marker;
    }

    /**
     * Returns the Swatch Internet (.beat) Time for the give date-time.
     *
     * @param zonedDateTime the date and time.
     * @return the .beat time. (eg.: {@code @248})
     */
    public static String beatTime(ZonedDateTime zonedDateTime) {
        var zdt = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC+01:00"));
        var beats = (int) ((zdt.get(ChronoField.SECOND_OF_MINUTE) + (zdt.get(ChronoField.MINUTE_OF_HOUR) * 60) +
                (zdt.get(ChronoField.HOUR_OF_DAY) * 3600)) / 86.4);
        return String.format("@%03d", beats);
    }

    /**
     * Encodes a String to JavaScript/ECMAScript.
     *
     * @param src the source String
     * @return the encoded String
     */
    public static String encodeJs(String src) {
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
     * Fetches the content (body) of a URL.
     *
     * @param url            the URL string.
     * @param defaultContent the default content to return if none fetched.
     * @return the url content, or empty.
     */
    public static String fetchUrl(String url, String defaultContent) {
        try {
            var connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            var code = connection.getResponseCode();
            if (code >= 200 && code <= 399) {
                try (var inputStream = connection.getInputStream()) {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (IOException ignore) {
            // do nothing
        }
        return defaultContent;
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
     * Converts a text string to HTML decimal entities.
     *
     * @param src the String to convert
     * @return the converted String
     */
    @SuppressWarnings("PMD.AvoidReassigningLoopVariables")
    public static String htmlEntities(String src) {
        if (src == null || src.isEmpty()) {
            return src;
        }

        var len = src.length();
        var sb = new StringBuilder(len * 6);

        // https://stackoverflow.com/a/6766497/8356718
        int codePoint;
        for (var i = 0; i < len; i++) {
            codePoint = src.codePointAt(i);
            // Skip over the second char in a surrogate pair
            if (codePoint > 0xffff) {
                i++;
            }
            sb.append(String.format("&#%s;", codePoint));
        }
        return sb.toString();
    }

    /**
     * Masks characters in a String.
     *
     * @param src       the source String.
     * @param mask      the String to mask characters with
     * @param unmasked  the number of characters to leave unmasked
     * @param fromStart to unmask characters from the start of the String
     * @return the masked String
     */
    public static String mask(String src, String mask, int unmasked, boolean fromStart) {
        if (src == null || src.isEmpty()) {
            return src;
        }

        var len = src.length();
        var buff = new StringBuilder(len);
        if (unmasked > 0 && unmasked < len) {
            if (fromStart) {
                buff.append(src, 0, unmasked);
            }
            buff.append(mask.repeat(len - unmasked));
            if (!fromStart) {
                buff.append(src.substring(len - unmasked));
            }
        } else {
            buff.append(mask.repeat(len));
        }
        return buff.toString();
    }

    /**
     * Normalizes a String for inclusion in a URL path.
     *
     * @param src The source String
     * @return The normalized String
     */
    public static String normalize(String src) {
        if (src == null || src.isBlank()) {
            return src;
        }

        var normalized = Normalizer.normalize(src.trim(), Normalizer.Form.NFD);
        var sb = new StringBuilder(normalized.length());
        boolean space = false;
        for (var c : normalized.toCharArray()) {
            if (c <= '\u007F') { // ascii only
                if (!space && c == ' ') {
                    space = true;
                    sb.append('-');
                } else {
                    space = false;
                    if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')) {
                        sb.append(c);
                    } else if (c >= 'A' && c <= 'Z') {
                        sb.append((char) (c + 32)); // lowercase
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns the plural form of a word, if count &gt; 1.
     *
     * @param count  the count
     * @param word   the singular word
     * @param plural the plural word
     * @return the singular or plural String
     */
    public static String plural(final long count, final String word, final String plural) {
        if (count > 1) {
            return plural;
        } else {
            return word;
        }
    }

    /**
     * Generates an SVG QR Code from the given String using <a href="https://goqr.me/">goQR.me</a>.
     *
     * @param src  the data String
     * @param size the QR Code size. (e.g. {@code 150x150})
     * @return the QR code
     */
    public static String qrCode(String src, String size) {
        if (src == null || src.isBlank()) {
            return src;
        }
        return fetchUrl(String.format("https://api.qrserver.com/v1/create-qr-code/?format=svg&size=%s&data=%s", size,
                StringUtils.encodeUrl(src.trim())), src);
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
        return fetchUrl(String.format("https://is.gd/create.php?format=simple&url=%s",
                StringUtils.encodeUrl(url.trim())), url);
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
     * Returns the formatted server uptime.
     *
     * @param uptime     the uptime in milliseconds
     * @param properties the format properties
     * @return The formatted uptime.
     */
    public static String uptime(long uptime, Properties properties) {
        var sb = new StringBuilder();

        var days = TimeUnit.MILLISECONDS.toDays(uptime);
        var years = days / 365;
        days %= 365;
        var months = days / 30;
        days %= 30;
        var weeks = days / 7;
        days %= 7;
        var hours = TimeUnit.MILLISECONDS.toHours(uptime) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(uptime));
        var minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(uptime));

        if (years > 0) {
            sb.append(years).append(plural(years, properties.getProperty("year", " year "),
                    properties.getProperty("years", " years ")));
        }

        if (months > 0) {
            sb.append(months).append(plural(months, properties.getProperty("month", " month "),
                    properties.getProperty("months", " months ")));
        }

        if (weeks > 0) {
            sb.append(weeks).append(plural(weeks, properties.getProperty("week", " week "),
                    properties.getProperty("weeks", " weeks ")));
        }

        if (days > 0) {
            sb.append(days).append(plural(days, properties.getProperty("day", " day "),
                    properties.getProperty("days", " days ")));
        }

        if (hours > 0) {
            sb.append(hours).append(plural(hours, properties.getProperty("hour", " hour "),
                    properties.getProperty("hours", " hours ")));
        }

        sb.append(minutes).append(plural(minutes, properties.getProperty("minute", " minute"),
                properties.getProperty("minutes", " minutes")));

        return sb.toString();
    }
}