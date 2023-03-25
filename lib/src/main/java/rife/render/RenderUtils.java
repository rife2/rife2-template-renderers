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

import rife.tools.Localization;
import rife.tools.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    /**
     * The encoding property.
     */
    public static final String ENCODING_PROPERTY = "encoding";

    /**
     * ISO 8601 date formatter.
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     */
    public static final DateTimeFormatter ISO_8601_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Localization.getLocale());
    /**
     * ISO 8601 date and time formatter.
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     */
    public static final DateTimeFormatter ISO_8601_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXXXX").withLocale(Localization.getLocale());
    /**
     * ISO 8601 time formatter.
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     */
    public static final DateTimeFormatter ISO_8601_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Localization.getLocale());
    /**
     * ISO 8601 Year formatter.
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     */
    static public final DateTimeFormatter ISO_8601_YEAR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy").withLocale(Localization.getLocale());
    /**
     * RFC 2822 date and time formatter.
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc2822">RFC 2822</a>
     */
    public static final DateTimeFormatter RFC_2822_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz").withLocale(Localization.getLocale());
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/111.0";

    private RenderUtils() {
        // no-op
    }

    /**
     * Abbreviates a {@code String} to the given length using a replacement marker.
     *
     * @param src    the source {@code String}
     * @param max    the maximum length of the resulting {@code String}
     * @param marker the {@code String} used as a replacement marker
     * @return the abbreviated {@code String}
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
     * @param zonedDateTime the date and time
     * @return the .beat time. (eg.: {@code @248})
     */
    public static String beatTime(ZonedDateTime zonedDateTime) {
        var zdt = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC+01:00"));
        var beats = (int) ((zdt.get(ChronoField.SECOND_OF_MINUTE) + (zdt.get(ChronoField.MINUTE_OF_HOUR) * 60) +
                (zdt.get(ChronoField.HOUR_OF_DAY) * 3600)) / 86.4);
        return String.format("@%03d", beats);
    }

    /**
     * <p>Encodes the source {@code String} to the specified encoding.</p>
     *
     * <p>The supported encodings are:</p>
     *
     * <ul>
     *     <li>{@code html}</li>
     *     <li>{@code js}</li>
     *     <li>{@code json}</li>
     *     <li>{@code unicode}</li>
     *     <li>{@code url}</li>
     *     <li>{@code xml}</li>
     * </ul>
     *
     * @param src        the source {@code String} to encode
     * @param properties the properties containing the {@link #ENCODING_PROPERTY encoding property}.
     * @return the encoded {@code String}
     */
    public static String encode(String src, Properties properties) {
        if (src == null || src.isBlank() || properties.isEmpty()) {
            return src;
        }

        var encoding = properties.getProperty(ENCODING_PROPERTY, "");
        switch (encoding) {
            case "html" -> {
                return StringUtils.encodeHtml(src);
            }
            case "js" -> {
                return RenderUtils.encodeJs(src);
            }
            case "json" -> {
                return StringUtils.encodeJson(src);
            }
            case "unicode" -> {
                return StringUtils.encodeUnicode(src);
            }
            case "url" -> {
                return StringUtils.encodeUrl(src);
            }
            case "xml" -> {
                return StringUtils.encodeXml(src);
            }
            default -> {
                return src;
            }
        }
    }

    /**
     * Encodes a {@code String} to JavaScript/ECMAScript.
     *
     * @param src the source {@code String}
     * @return the encoded {@code String}
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
     * @param url            the URL {@code String}
     * @param defaultContent the default content to return if none fetched
     * @return the url content, or empty
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
     * <p></p>Returns the last 4 digits a credit card number.</p>
     *
     * <ul>
     *     <li>The number must satisfy the Luhn algorithm</li>
     *     <li>Non-digits are stripped from the number</li>
     * </ul>
     *
     * @param src the credit card number
     * @return the last 4 digits of the credit card number or empty
     */
    public static String formatCreditCard(String src) {
        if (src == null || src.isBlank()) {
            return src;
        }

        var cc = src.replaceAll("[^0-9]", "");

        if (validateCreditCard(cc)) {
            return cc.substring(cc.length() - 4);
        } else {
            return "";
        }
    }

    /**
     * Converts a text {@code String} to HTML decimal entities.
     *
     * @param src the {@code String} to convert
     * @return the converted {@code String}
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
     * @param src       the source {@code String}
     * @param mask      the {@code String} to mask characters with
     * @param unmasked  the number of characters to leave unmasked
     * @param fromStart to unmask characters from the start of the {@code String}
     * @return the masked {@code String}
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
     * Normalizes a {@code String} for inclusion in a URL path.
     *
     * @param src the source {@code String}
     * @return the normalized {@code String}
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
     * Returns a new {@code Properties} containing the properties specified in the given {$String}.
     *
     * @param src the {@code} String containing the properties
     * @return the new {@code Properties}
     */
    public static Properties parsePropertiesString(String src) {
        var properties = new Properties();
        if (src != null && !src.isBlank()) {
            try {
                properties.load(new StringReader(src));
            } catch (IOException ignore) {
                // ignore
            }
        }
        return properties;
    }

    /**
     * Returns the plural form of a word, if count &gt; 1.
     *
     * @param count  the count
     * @param word   the singular word
     * @param plural the plural word
     * @return the singular or plural {@code String}
     */
    public static String plural(final long count, final String word, final String plural) {
        if (count > 1) {
            return plural;
        } else {
            return word;
        }
    }

    /**
     * Generates an SVG QR Code from the given {@code String} using <a href="https://goqr.me/">goQR.me</a>.
     *
     * @param src  the data {@code String}
     * @param size the QR Code size. (e.g. {@code 150x150})
     * @return the QR code
     */
    public static String qrCode(String src, String size) {
        if (src == null || src.isBlank()) {
            return src;
        }
        return fetchUrl(String.format("https://api.qrserver.com/v1/create-qr-code/?format=svg&size=%s&data=%s",
                        StringUtils.encodeUrl(size),
                        StringUtils.encodeUrl(src.trim())),
                src);
    }

    /**
     * Translates a {@code String} to/from ROT13.
     *
     * @param src the source {@code String}
     * @return the translated {@code String}
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
     * <p>Shortens a URL using <a href="https://is.gd/">is.gid</a>.</p>
     *
     * <p>The URL {@code String} must be a valid http or https URL.</p>
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
     * @param src the {@code String} to swap the case of
     * @return the modified {@code String} or null
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
     * <p>Returns the formatted server uptime.</p>
     *
     * <p>The default Properties are:</p>
     *
     * <pre>
     * year=\ year\u29F5u0020
     * years=\ years\u29F5u0020
     * month=\ month\u29F5u0020
     * months=\ months\u29F5u0020
     * week=\ week\u29F5u0020
     * weeks=\ weeks\u29F5u0020
     * day=\ day\u29F5u0020
     * days=\ days\u29F5u0020
     * hour=\ hour\u29F5u0020
     * hours=\ hours\u29F5u0020
     * minute=\ minute
     * minutes=\ minutes
     * </pre>
     *
     * @param uptime     the uptime in milliseconds
     * @param properties the format properties
     * @return the formatted uptime
     */
    @SuppressWarnings("UnnecessaryUnicodeEscape")
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

    /**
     * Validates a credit card number using the Luhn algorithm.
     *
     * @param cc the credit card number
     * @return {@code true} if the credit card number is valid
     */
    public static boolean validateCreditCard(String cc) {
        try {
            var len = cc.length();
            if (len >= 8 && len <= 19) {
                // Luhn algorithm
                var sum = 0;
                boolean second = false;
                int digit;
                char c;
                for (int i = len - 1; i >= 0; i--) {
                    c = cc.charAt(i);
                    if (c >= '0' && c <= '9') {
                        digit = cc.charAt(i) - '0';
                        if (second) {
                            digit = digit * 2;
                        }
                        sum += digit / 10;
                        sum += digit % 10;

                        second = !second;
                    }
                }
                if (sum % 10 == 0) {
                    return true;
                }
            }
        } catch (NumberFormatException ignore) {
            // do nothing
        }
        return false;
    }

}