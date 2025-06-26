/*
 *  Copyright 2023-2024 the original author or authors.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of utility-type methods commonly used by the renderers.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public final class RenderUtils {
    /**
     * Common separators.
     */
    public static final char[] COMMON_SEPARATORS =
            {' ', '&', '(', ')', '-', '_', '=', '[', '{', ']', '}', '\\', '|', ';', ':', ',', '<', '.', '>', '/', '@'};
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
    private static final Logger LOGGER = Logger.getLogger(RenderUtils.class.getName());
    //Pre-computed lookup for separator characters - much faster than indexOf
    private static final boolean[] SEPARATOR_LOOKUP = new boolean[128];

    static {
        for (char c : COMMON_SEPARATORS) {
            SEPARATOR_LOOKUP[c] = true;
        }
    }

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
        var beats = (int) ((zdt.getSecond() + (zdt.getMinute() * 60) +
                (zdt.getHour() * 3600)) / 86.4);
        return String.format("@%03d", beats);
    }

    /**
     * Returns a {@code String} with the first letter of each word capitalized.
     *
     * @param src the source {@code String}
     * @return the capitalized {@code String}
     */
    public static String capitalizeWords(String src) {
        if (src == null) {
            return null;
        } else if (src.isEmpty()) {
            return "";
        }

        final var length = src.length();
        final var chars = new char[length];
        var capitalizeNext = true;

        for (int i = 0; i < length; i++) {
            final var c = src.charAt(i);
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                chars[i] = c;
            } else if (capitalizeNext) {
                chars[i] = Character.toUpperCase(c);
                capitalizeNext = false;
            } else {
                chars[i] = Character.toLowerCase(c);
            }
        }

        return new String(chars);
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
                return encodeJs(src);
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
        if (src == null || src.isEmpty()) {
            return src;
        }

        int len = src.length();
        var sb = new StringBuilder(len + (len >> 2)); // Pre-allocate ~25% extra capacity

        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            switch (c) {
                case '\'' -> sb.append("\\'");
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '/' -> sb.append("\\/");
                case '\b' -> sb.append("\\b");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\f' -> sb.append("\\f");
                case '\r' -> sb.append("\\r");
                default -> {
                    if (c < 0x20 || c == 0x7F) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
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
            var fetchUrl = new URL(url);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) fetchUrl.openConnection();
                connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
                var code = connection.getResponseCode();
                if (code >= 200 && code <= 399) {
                    try (var inputStream = connection.getInputStream()) {
                        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    }
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning("A " + code + " status code was returned by " + fetchUrl.getHost());
                    }
                }
            } catch (IOException ioe) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "An IO error occurred while connecting to " + fetchUrl.getHost(), ioe);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (MalformedURLException ignored) {
            // do nothing
        }

        return defaultContent;
    }

    /**
     * <p>Returns the last 4 digits a credit card number.</p>
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

        int len = src.length();
        var sb = new StringBuilder(len * 8); // Increased capacity estimate

        int codePoint;
        for (int i = 0; i < len; ) {
            codePoint = src.codePointAt(i);

            // Append the numeric character reference directly
            sb.append("&#").append(codePoint).append(';');

            // Advance by the number of char units consumed
            i += Character.charCount(codePoint);
        }

        return sb.toString();
    }

    private static boolean isCommonSeparator(char c) {
        return c < SEPARATOR_LOOKUP.length && SEPARATOR_LOOKUP[c];
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
            return "";
        }

        var normalized = Normalizer.normalize(src.trim(), Normalizer.Form.NFD).toCharArray();
        var sb = new StringBuilder(normalized.length);
        var lastWasSeparator = false;

        for (var c : normalized) {
            if (c > '\u007F') {
                continue; // Skip non-ASCII early
            }

            if (c >= '0' && c <= '9' || c >= 'a' && c <= 'z') {
                if (lastWasSeparator && !sb.isEmpty()) {
                    sb.append('-');
                }
                sb.append(c);
                lastWasSeparator = false;
            } else if (c >= 'A' && c <= 'Z') {
                if (lastWasSeparator && !sb.isEmpty()) {
                    sb.append('-');
                }
                sb.append((char) (c + 32)); // Convert to lowercase
                lastWasSeparator = false;
            } else if (isCommonSeparator(c)) {
                lastWasSeparator = true;
            }
        }

        return sb.toString();
    }

    /**
     * Returns a new {@code Properties} containing the properties specified in the given {@code String}.
     *
     * @param src the {@code} String containing the properties
     * @return the new {@code Properties}
     */
    public static Properties parsePropertiesString(String src) {
        var properties = new Properties();
        if (src != null && !src.isBlank()) {
            try {
                properties.load(new StringReader(src));
            } catch (IOException ignored) {
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
        return fetchUrl(
                String.format("https://api.qrserver.com/v1/create-qr-code/?format=svg&size=%s&data=%s",
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
        if (src == null) {
            return "";
        }

        if (src.isEmpty()) {
            return src;
        }

        var result = new StringBuilder(src.length());

        int i = 0;
        while (i < src.length()) {
            int codePoint = src.codePointAt(i);
            int charCount = Character.charCount(codePoint);

            // Only apply ROT13 to ASCII letters (A-Z, a-z)
            if ((codePoint >= 'A' && codePoint <= 'Z') || (codePoint >= 'a' && codePoint <= 'z')) {
                boolean isUpperCase = codePoint <= 'Z';

                // Convert to lowercase for calculation
                int lowerCodePoint = isUpperCase ? codePoint + 32 : codePoint;

                // Apply ROT13: shift by 13, wrap around
                int rotatedCodePoint = ((lowerCodePoint - 'a' + 13) % 26) + 'a';

                // Restore original case
                int finalCodePoint = isUpperCase ? rotatedCodePoint - 32 : rotatedCodePoint;

                result.appendCodePoint(finalCodePoint);
            } else {
                // Non-ASCII letters and other characters remain unchanged
                result.appendCodePoint(codePoint);
            }

            i += charCount;
        }

        return result.toString();
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
        if (src == null || src.isEmpty()) {
            return "";
        }

        var result = new StringBuilder(src.length());

        for (int i = 0; i < src.length(); /* incremented inside loop */) {
            int codePoint = src.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            int convertedCodePoint = codePoint;

            if (Character.isUpperCase(codePoint)) {
                convertedCodePoint = Character.toLowerCase(codePoint);
            } else if (Character.isLowerCase(codePoint)) {
                convertedCodePoint = Character.toUpperCase(codePoint);
            }

            result.appendCodePoint(convertedCodePoint);
            i += charCount;
        }
        return result.toString();
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
        long remainingUptime = uptime;

        long years = TimeUnit.MILLISECONDS.toDays(remainingUptime) / 365;
        remainingUptime -= TimeUnit.DAYS.toMillis(years * 365);

        if (years > 0) {
            sb.append(years).append(plural(years, properties.getProperty("year", " year "),
                    properties.getProperty("years", " years ")));
        }

        long months = TimeUnit.MILLISECONDS.toDays(remainingUptime) / 30;
        remainingUptime -= TimeUnit.DAYS.toMillis(months * 30);

        if (months > 0) {
            sb.append(months).append(plural(months, properties.getProperty("month", " month "),
                    properties.getProperty("months", " months ")));
        }

        long weeks = TimeUnit.MILLISECONDS.toDays(remainingUptime) / 7;
        remainingUptime -= TimeUnit.DAYS.toMillis(weeks * 7);

        if (weeks > 0) {
            sb.append(weeks).append(plural(weeks, properties.getProperty("week", " week "),
                    properties.getProperty("weeks", " weeks ")));
        }

        long days = TimeUnit.MILLISECONDS.toDays(remainingUptime);
        remainingUptime -= TimeUnit.DAYS.toMillis(days);

        if (days > 0) {
            sb.append(days).append(plural(days, properties.getProperty("day", " day "),
                    properties.getProperty("days", " days ")));
        }

        long hours = TimeUnit.MILLISECONDS.toHours(remainingUptime);
        remainingUptime -= TimeUnit.HOURS.toMillis(hours);

        if (hours > 0) {
            sb.append(hours).append(plural(hours, properties.getProperty("hour", " hour "),
                    properties.getProperty("hours", " hours ")));
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingUptime);
        if (minutes > 0 || sb.isEmpty()) { // Append minutes if there are any, or if no other units were appended
            sb.append(minutes).append(plural(minutes, properties.getProperty("minute", " minute"),
                    properties.getProperty("minutes", " minutes")));
        }

        return sb.toString().trim();
    }

    /**
     * Validates a credit card number using the Luhn algorithm.
     *
     * @param cc the credit card number
     * @return {@code true} if the credit card number is valid
     */
    public static boolean validateCreditCard(String cc) {
        if (cc == null) {
            return false;
        }

        int len = cc.length();
        if (len < 8 || len > 19) {
            return false;
        }

        int sum = 0;
        boolean second = false;

        // Process from right to left
        for (int i = len - 1; i >= 0; i--) {
            char c = cc.charAt(i);

            // Process only digits
            if (c >= '0' && c <= '9') {
                int digit = c - '0';

                if (second) {
                    digit <<= 1; // Multiply by 2 using bit shift
                    if (digit > 9) {
                        digit -= 9; // Equivalent to digit/10 + digit%10 when digit <= 18
                    }
                }

                sum += digit;
                second = !second;
            }
        }

        return sum % 10 == 0;
    }
}