/*
 *  Copyright 2023-2026 the original author or authors.
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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import rife.tools.Localization;
import rife.tools.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Collection of utility-type methods commonly used by the renderers.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@NullMarked
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
    public static final DateTimeFormatter ISO_8601_YEAR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy").withLocale(Localization.getLocale());
    /**
     * RFC 2822 date and time formatter.
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc2822">RFC 2822</a>
     */
    public static final DateTimeFormatter RFC_2822_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz").withLocale(Localization.getLocale());
    /**
     * Common separators.
     */
    static final char[] COMMON_SEPARATORS =
            {' ', '&', '(', ')', '-', '_', '=', '[', '{', ']', '}', '\\', '|', ';', ':', ',', '<', '.', '>', '/', '@'};
    private static final String
            DEFAULT_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64; rv:140.0) Gecko/20100101 Firefox/140.0";
    /**
     * Shared HTTP client. Thread-safe; reused across all requests.
     * Uses a fixed 10-second connect timeout and 30-second request timeout.
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Logger LOGGER = Logger.getLogger(RenderUtils.class.getName());
    private static final long MILLIS_PER_MINUTE = 60L * 1000;
    private static final long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
    private static final long MILLIS_PER_WEEK = 7L * MILLIS_PER_DAY;
    private static final long MILLIS_PER_MONTH = 30L * MILLIS_PER_DAY;
    // Year is intentionally fixed at 365 days (leap years not accounted for)
    private static final long MILLIS_PER_YEAR = 365L * MILLIS_PER_DAY;
    private static final UptimeUnit[] UPTIME_UNITS = {
            new UptimeUnit(MILLIS_PER_YEAR, "year", "years", " year ", " years "),
            new UptimeUnit(MILLIS_PER_MONTH, "month", "months", " month ", " months "),
            new UptimeUnit(MILLIS_PER_WEEK, "week", "weeks", " week ", " weeks "),
            new UptimeUnit(MILLIS_PER_DAY, "day", "days", " day ", " days "),
            new UptimeUnit(MILLIS_PER_HOUR, "hour", "hours", " hour ", " hours "),
            new UptimeUnit(MILLIS_PER_MINUTE, "minute", "minutes", " minute", " minutes")
    };
    // Pre-computed lookup for separator characters — much faster than indexOf.
    private static final boolean[] SEPARATOR_LOOKUP = new boolean[128];
    private static final Pattern URL_MATCH = Pattern.compile("https?://\\w.*", Pattern.CASE_INSENSITIVE);

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
     * @throws NullPointerException if the {@code src} or {@code marker} is {@code null}
     */
    public static String abbreviate(String src, int max, String marker) {
        Objects.requireNonNull(src, "The abbreviate source string cannot be null");
        Objects.requireNonNull(marker, "The abbreviate marker string cannot be null");
        if (src.isBlank()) {
            return src;
        } else if (src.length() <= max || max < 0) {
            return src;
        } else if (marker.length() >= max) {
            return src.substring(0, max);
        }

        return src.substring(0, max - marker.length()) + marker;
    }

    /**
     * Appends a {@code \\uXXXX} Unicode escape sequence for the given char to the builder.
     * Avoids {@code String.format} overhead in hot encoding paths.
     */
    private static void appendUnicodeEscape(StringBuilder sb, char c) {
        sb.append("\\u")
                .append(Character.forDigit((c >> 12) & 0xF, 16))
                .append(Character.forDigit((c >> 8) & 0xF, 16))
                .append(Character.forDigit((c >> 4) & 0xF, 16))
                .append(Character.forDigit(c & 0xF, 16));
    }

    /**
     * Returns the Swatch Internet (.beat) Time for the give date-time.
     *
     * @param zonedDateTime the date and time
     * @return the .beat time. (eg.: {@code @248})
     */
    public static String beatTime(ZonedDateTime zonedDateTime) {
        var zdt = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC+01:00"));
        var beats = (int) ((zdt.getSecond() + (zdt.getMinute() * 60) + (zdt.getHour() * 3600)) / 86.4);
        return String.format("@%03d", beats);
    }

    /**
     * Returns a {@code String} with the first letter of each word capitalized and
     * the remaining letters lowercased (i.e., title-cased).
     *
     * <p>Note: this method lowercases all non-initial characters within each word.
     * For example, {@code "myHTML"} becomes {@code "Myhtml"}.</p>
     *
     * @param src the source {@code String}
     * @return the title-cased {@code String}
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String capitalizeWords(String src) {
        Objects.requireNonNull(src, "The capitalizeWords source string cannot be null");
        if (src.isBlank()) {
            return src;
        }

        final var sb = new StringBuilder(src.length());
        var capitalizeNext = true;

        final var codePoints = src.codePoints().toArray();

        for (int codePoint : codePoints) {
            if (Character.isWhitespace(codePoint)) {
                capitalizeNext = true;
                sb.appendCodePoint(codePoint);
            } else if (capitalizeNext) {
                sb.appendCodePoint(Character.toUpperCase(codePoint));
                capitalizeNext = false;
            } else {
                sb.appendCodePoint(Character.toLowerCase(codePoint));
            }
        }

        return sb.toString();
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
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String encode(String src, Properties properties) {
        Objects.requireNonNull(src, "The encode source string cannot be null");
        if (src.isBlank() || properties.isEmpty()) {
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
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String encodeJs(String src) {
        Objects.requireNonNull(src, "The encodeJs source string cannot be null");
        if (src.isEmpty()) {
            return src;
        }

        var encoded = new StringBuilder(src.length() * 2);

        int i = 0;
        while (i < src.length()) {
            char c = src.charAt(i);

            switch (c) {
                case '\\' -> encoded.append("\\\\");
                case '"' -> encoded.append("\\\"");
                case '/' -> encoded.append("\\/");
                case '\'' -> encoded.append("\\'");
                case '\r' -> encoded.append("\\r");
                case '\n' -> encoded.append("\\n");
                case '\t' -> encoded.append("\\t");
                case '\f' -> encoded.append("\\f");
                case '\b' -> encoded.append("\\b");
                case '\u2028' -> encoded.append("\\u2028"); // Line separator
                case '\u2029' -> encoded.append("\\u2029"); // Paragraph separator
                default -> {
                    if (c <= 0x1F || c == 0x7F || (c >= 0x80 && c <= 0x9F)) {
                        // Control characters
                        appendUnicodeEscape(encoded, c);
                    } else if (c > 0x7F) {
                        // Non-ASCII Unicode characters
                        if (Character.isHighSurrogate(c) && i + 1 < src.length()) {
                            // Handle surrogate pairs for characters outside BMP
                            char lowSurrogate = src.charAt(i + 1);
                            if (Character.isLowSurrogate(lowSurrogate)) {
                                appendUnicodeEscape(encoded, c);
                                appendUnicodeEscape(encoded, lowSurrogate);
                                i++; // Skip the low surrogate
                            } else {
                                appendUnicodeEscape(encoded, c);
                            }
                        } else {
                            appendUnicodeEscape(encoded, c);
                        }
                    } else {
                        // Regular character, no escaping needed
                        encoded.append(c);
                    }
                }
            }
            i++;
        }

        return encoded.toString();
    }

    /**
     * Fetches the content (body) of a URL.
     *
     * @param url            the URL {@code String}
     * @param defaultContent the default content to return if none fetched
     * @return the URL content, or empty
     * @throws NullPointerException if the URL or default content is {@code null}
     */
    public static String fetchUrl(String url, String defaultContent) {
        Objects.requireNonNull(url, "The fetch URL cannot be null");
        Objects.requireNonNull(defaultContent, "The fetch default content cannot be null");
        try {
            var uri = URI.create(url);
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("User-Agent", DEFAULT_USER_AGENT)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            var statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode <= 399) {
                return response.body();
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("A " + statusCode + " status code was returned by " + uri.getHost());
                }
            }
        } catch (IllegalArgumentException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Invalid URL: " + url, e);
            }
        } catch (InterruptedException e) {
            // Intentionally not re-interrupting — fetchUrl is called from container-managed
            // threads (e.g. servlet workers) where resetting the interrupt flag could
            // interfere with the container's thread lifecycle.
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Interrupted while fetching URL: " + url, e);
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Error occurred while fetching URL: " + url, e);
            }
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
     * @return the last 4 digits of the credit card number, or {@code empty} if blank or invalid
     * @throws NullPointerException if {@code src} is {@code null}
     */
    public static String formatCreditCard(String src) {
        Objects.requireNonNull(src, "The credit card number cannot be null");
        if (!src.isBlank()) {
            var cc = src.replaceAll("[^0-9]", "");

            if (validateCreditCard(cc)) {
                return cc.substring(cc.length() - 4);
            }
        }

        return "";
    }

    /**
     * Converts a text {@code String} to HTML decimal entities.
     *
     * @param src the {@code String} to convert
     * @return the converted {@code String}
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String htmlEntities(String src) {
        Objects.requireNonNull(src, "The htmlEntities source string cannot be null");
        if (src.isEmpty()) {
            return src;
        }

        int len = src.length();
        var sb = new StringBuilder(len * 6);

        int codePoint;
        int i = 0;
        while (i < len) {
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
     * <p>If {@code unmasked} is negative or zero, the entire string is masked.
     * If {@code unmasked} is greater than or equal to the total character count,
     * the entire string is returned unmasked.</p>
     *
     * @param src       the source {@code String}
     * @param mask      the {@code String} to mask characters with
     * @param unmasked  the number of characters to leave unmasked (negative treated as 0)
     * @param fromStart to unmask characters from the start of the {@code String}
     * @return the masked {@code String}
     * @throws NullPointerException if {@code src} or {@code mask} are null
     */
    public static String mask(String src, String mask, int unmasked, boolean fromStart) {
        Objects.requireNonNull(src, "The mask source string cannot be null");
        Objects.requireNonNull(mask, "The mask string cannot be null");
        if (src.isEmpty()) {
            return src;
        }

        // Use codePointCount for proper Unicode support (counts actual characters, not UTF-16 units)
        int codePointCount = src.codePointCount(0, src.length());

        // Early return for full masking
        if (unmasked <= 0 || unmasked >= codePointCount) {
            return mask.repeat(codePointCount);
        }

        var buff = new StringBuilder();

        if (fromStart) {
            // Show first N characters, mask the rest
            int unmaskedEndIndex = src.offsetByCodePoints(0, unmasked);
            buff.append(src, 0, unmaskedEndIndex)
                    .append(mask.repeat(codePointCount - unmasked));
        } else {
            // Mask first part, show last N characters
            int maskedEndIndex = src.offsetByCodePoints(0, codePointCount - unmasked);
            buff.append(mask.repeat(codePointCount - unmasked))
                    .append(src, maskedEndIndex, src.length());
        }

        return buff.toString();
    }

    /**
     * Normalizes a {@code String} for inclusion in a URL path.
     *
     * @param src the source {@code String}
     * @return the normalized {@code String}
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String normalize(String src) {
        Objects.requireNonNull(src, "The normalize source string cannot be null");
        if (src.isBlank()) {
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
    public static Properties parsePropertiesString(@Nullable String src) {
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
     * Returns the plural form of a word based on count.
     *
     * <p>Uses {@code count != 1} as the pluralization condition, so both {@code 0} and
     * values {@code > 1} return the plural form (e.g., "0 minutes", "2 minutes").</p>
     *
     * @param count  the count
     * @param word   the singular word
     * @param plural the plural word
     * @return the singular or plural {@code String}
     */
    public static String plural(final long count, final String word, final String plural) {
        Objects.requireNonNull(word, "The plural word cannot be null");
        Objects.requireNonNull(plural, "The plural cannot be null");
        if (count != 1) { // 0 minutes, not 0 minute
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
     * @throws NullPointerException if the {@code src} or {@code size} is {@code null}
     */
    public static String qrCode(String src, String size) {
        Objects.requireNonNull(src, "The qrCode source string cannot be null");
        Objects.requireNonNull(size, "The qrCode size string cannot be null");
        if (src.isBlank()) {
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
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String rot13(String src) {
        Objects.requireNonNull(src, "The rot13 source string cannot be null");

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
     * @throws NullPointerException if the {@code url} is {@code null}
     */
    public static String shortenUrl(String url) {
        Objects.requireNonNull(url, "The shorten URL string cannot be null");
        if (url.isBlank() || !URL_MATCH.matcher(url).matches()) {
            return url;
        }
        return fetchUrl(String.format("https://is.gd/create.php?format=simple&url=%s",
                StringUtils.encodeUrl(url.trim())), url);
    }

    /**
     * Swaps the case of a String.
     *
     * @param src the {@code String} to swap the case of
     * @return the modified {@code String}
     * @throws NullPointerException if the {@code src} is {@code null}
     */
    public static String swapCase(String src) {
        Objects.requireNonNull(src, "The swapCase source string cannot be null");
        if (src.isEmpty()) {
            return src;
        }

        var result = new StringBuilder(src.length());

        int i = 0;
        while (i < src.length()) {
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
        long remaining = uptime;

        for (UptimeUnit unit : UPTIME_UNITS) {
            long value = remaining / unit.divisor;

            if (value > 0) {
                remaining %= unit.divisor;
                sb.append(value).append(plural(value,
                        properties.getProperty(unit.singularKey, unit.defaultSingular),
                        properties.getProperty(unit.pluralKey, unit.defaultPlural)));
            }
        }

        // If no units were added, add 0 minutes
        if (sb.isEmpty()) {
            sb.append('0').append(properties.getProperty("minutes", " minutes"));
        }

        return sb.toString().trim();
    }

    /**
     * Validates a credit card number using the Luhn algorithm.
     *
     * @param cc the credit card number
     * @return {@code true} if the credit card number is valid
     * @throws NullPointerException if the {@code cc} is {@code null}
     */
    public static boolean validateCreditCard(String cc) {
        Objects.requireNonNull(cc, "The credit card number cannot be null");

        int sum = 0;
        int digitCount = 0;
        boolean second = false;

        for (int i = cc.length() - 1; i >= 0; i--) {
            char c = cc.charAt(i);

            if (c >= '0' && c <= '9') {
                int digit = c - '0';

                if (second) {
                    digit <<= 1;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }

                sum += digit;
                digitCount++;
                second = !second;
            }
        }

        if (digitCount < 8 || digitCount > 19) {
            return false;
        }

        return sum % 10 == 0;
    }

    private record UptimeUnit(long divisor, String singularKey, String pluralKey, String defaultSingular,
                              String defaultPlural) {

    }
}