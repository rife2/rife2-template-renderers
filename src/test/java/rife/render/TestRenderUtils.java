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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class TestRenderUtils {
    static final String SAMPLE_GERMAN = "Möchten Sie ein paar Äpfel?";

    @Test
    void qrCode() {
        assertThat(RenderUtils.qrCode("erik", "24")).as("svg")
                .startsWith("<?xml").contains("<svg").contains("<desc>erik");
    }

    @Test
    void qrCodeWithEmpty() {
        assertThat(RenderUtils.qrCode("", "12")).as("empty").isEmpty();
    }

    @Test
    void swapCase() {
        assertThat(RenderUtils.swapCase(SAMPLE_GERMAN)).isEqualTo("mÖCHTEN sIE EIN PAAR äPFEL?");
    }

    @Test
    void swapCaseWithEmpty() {
        assertThat(RenderUtils.swapCase("")).isEmpty();
    }

    @Nested
    @DisplayName("Abbreviate Tests")
    class AbbreviateTests {
        @Test
        void abbreviateWithEllipsis() {
            assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 10, "…"))
                    .isEqualTo("This is a…");
        }

        @Test
        void abbreviateWithEmpty() {
            assertThat(RenderUtils.abbreviate("", 10, "")).as("").isEmpty();
        }

        @Test
        void abbreviateWithMarker() {
            assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 12, "..."))
                    .isEqualTo("This is a...");
        }

        @Test
        void abbreviateWithMax() {
            assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 9, "")).isEqualTo("This is a");
        }

        @Test
        void abbreviateWithMaxNegative() {
            assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, -1, ""))
                    .isEqualTo(TestCase.SAMPLE_TEXT);
        }

        @Test
        void abbreviateWithMaxZero() {
            assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 0, "")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Capitalize Words Tests")
    class CapitalizeWordsTests {
        @Test
        void capitalizeWordsBlankInput() {
            assertEquals("   ", RenderUtils.capitalizeWords("   "));
        }

        @Test
        void capitalizeWordsMultipleWords() {
            assertEquals("The Quick Brown Fox", RenderUtils.capitalizeWords("the quick brown fox"));
        }

        @Test
        void capitalizeWordsNullInput() {
            assertNull(RenderUtils.capitalizeWords(null));
        }

        @Test
        void capitalizeWordsSingleWord() {
            assertEquals("Hello", RenderUtils.capitalizeWords("hello"));
        }

        @Test
        void capitalizeWordsWordWithLeadingWhitespace() {
            assertEquals(" Hello World", RenderUtils.capitalizeWords(" hello world"));
        }

        @Test
        void capitalizeWordsWordWithMultipleWhitespaces() {
            assertEquals("  The\tQuick   Brown\fFox  ",
                    RenderUtils.capitalizeWords("  the\tquick   brown\ffox  "));
        }

        @Test
        void capitalizeWordsWordWithTrailingWhitespace() {
            assertEquals("Hello World ", RenderUtils.capitalizeWords("hello world "));
        }

        @Test
        void capitalizeWordsWordWithUnicode() {
            assertEquals("Über Äpfel Éclair", RenderUtils.capitalizeWords("über äpfel éclair"));
        }
    }

    @Nested
    @DisplayName("Credit Card Validation Tests")
    class CreditCardValidationTests {
        @Nested
        @DisplayName("Comprehensive Edge Cases")
        class ComprehensiveEdgeCases {
            static Stream<Arguments> cardLengthTestCases() {
                return Stream.of(
                        Arguments.of("1234567", false, "7 digits - too short"),
                        Arguments.of("123456789", false, "9 digits - invalid Luhn"),
                        Arguments.of("4532015112830366", true, "16 digits - standard Visa"),
                        Arguments.of("378282246310005", true, "15 digits - AmEx"),
                        Arguments.of("12345678901234567890", false, "20 digits - too long")
                );
            }

            @ParameterizedTest
            @DisplayName("Should handle various card lengths with mixed results")
            @MethodSource("cardLengthTestCases")
            void shouldHandleVariousCardLengths(String creditCard, boolean expected, String description) {
                assertThat(RenderUtils.validateCreditCard(creditCard))
                        .as("Testing %s (%d digits): %s", description, creditCard.length(), creditCard)
                        .isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("Invalid Credit Cards")
        class InvalidCreditCards {
            @ParameterizedTest
            @DisplayName("Should reject cards that fail Luhn algorithm")
            @ValueSource(strings = {
                    "4532015112830367",  // Last digit wrong
                    "4532015112830365",  // Last digit wrong
                    "1234567890123456",  // Sequential numbers
                    "1111111111111111",  // All ones
                    "4111111111111112",  // Valid format but wrong checksum
                    "5431111111111112",  // Valid format but wrong checksum
                    "6011601160116612",  // Valid format but wrong checksum
                    "12345678",          // Too simple, fails Luhn
                    "87654321098765432", // Random numbers, fails Luhn
                    "4532015112830368",  // Close to valid but fails Luhn
                    "40000015",          // 8 digits, fails Luhn
                    "4000001234567890127" // 19 digits, fails Luhn
            })
            void shouldRejectLuhnFailures(String creditCard) {
                assertThat(RenderUtils.validateCreditCard(creditCard)).isFalse();
            }

            @ParameterizedTest
            @DisplayName("Should reject null and empty strings")
            @NullAndEmptySource
            void shouldRejectNullAndEmpty(String creditCard) {
                assertThat(RenderUtils.validateCreditCard(creditCard)).isFalse();
            }

            @ParameterizedTest
            @DisplayName("Should reject cards that are too long")
            @ValueSource(strings = {
                    "12345678901234567890",   // 20 digits
                    "123456789012345678901",  // 21 digits
                    "1234567890123456789012", // 22 digits
                    "12345678901234567890123456789" // 29 digits
            })
            void shouldRejectTooLongCards(String creditCard) {
                assertThat(RenderUtils.validateCreditCard(creditCard)).isFalse();
            }

            @ParameterizedTest
            @DisplayName("Should reject cards that are too short")
            @ValueSource(strings = {
                    "1234567",  // 7 digits
                    "123456",   // 6 digits
                    "12345",    // 5 digits
                    "1234",     // 4 digits
                    "1"         // 1 digit
            })
            void shouldRejectTooShortCards(String creditCard) {
                assertThat(RenderUtils.validateCreditCard(creditCard)).isFalse();
            }
        }

        @Nested
        @DisplayName("Valid Credit Cards")
        class ValidCreditCards {
            @ParameterizedTest
            @DisplayName("Should validate brand-specific patterns")
            @CsvSource({
                    "378734493671000, American Express Corporate",
                    "371449635398431, American Express",
                    "378282246310005, American Express",
                    "3711 1111 1111 114, American Express",
                    "5610591081018250, Australian BankCard",
                    "5019717010103742, Dankort (PBS)",
                    "5019346126415137, Dankort (PBS)",
                    "30569309025904, Diners Club",
                    "38520000023237, Diners Club",
                    "3600 0000 0000 08, Diners",
                    "6011000990139424, Discover",
                    "6011111111111117, Discover",
                    "3530111333300000, JCB",
                    "3566002020360505, JCB",
                    "2222 4000 1000 0008, Mastercard",
                    "2222 4000 3000 0004, Mastercard",
                    "2222 4000 5000 0009, Mastercard",
                    "2222 4107 0000 0002, Mastercard",
                    "2223 0000 4841 0010, Mastercard",
                    "5100 0600 0000 0002, Mastercard",
                    "5105105105105100, MasterCard",
                    "5431 1111 1111 1111, MasterCard",
                    "5431111188111101, MasterCard",
                    "5453010000095323, MasterCard",
                    "5555 5555 5555 4444, Mastercard",
                    "5555555555554444, MasterCard",
                    "6331101999990016, Switch/Solo (Paymentech)",
                    "4000 1600 0000 0004, Visa",
                    "4000 1800 0000 0002, Visa",
                    "4012888888881881, Visa",
                    "4111 1111 1111 1111, Visa",
                    "4111111111111111, Visa",
                    "4222222222222, Visa",
                    "4988 0800 0000 0000, Visa",
                    "4999999999999202, Visa"
            })
            void shouldValidateBrandSpecificPatterns(String creditCard, String brand) {
                assertThat(RenderUtils.validateCreditCard(creditCard))
                        .as("Validating %s card: %s", brand, creditCard)
                        .isTrue();
            }

            @ParameterizedTest
            @DisplayName("Should validate common valid credit card numbers")
            @ValueSource(strings = {
                    "4532015112830366",  // Visa
                    "4000056655665556",  // Visa
                    "5555555555554444",  // Mastercard
                    "5105105105105100",  // Mastercard
                    "378282246310005",   // American Express
                    "371449635398431",   // American Express
                    "6011111111111117",  // Discover
                    "6011000990139424",  // Discover
                    "4111111111111111",  // Generic Visa
                    "5431111111111111",  // Generic Mastercard
                    "6011601160116611",  // Generic Discover
                    "30569309025904",    // Diners Club
                    "38520000023237"     // Diners Club
            })
            void shouldValidateCommonValidCreditCards(String creditCard) {
                assertThat(RenderUtils.validateCreditCard(creditCard)).isTrue();
            }

            @ParameterizedTest
            @DisplayName("Should validate valid credit card numbers with non-digits")
            @ValueSource(strings = {
                    "4505 4672 3366 6430",  // Visa
                    "5189-5923-3915-0425",  // Mastercard
                    "6011 1076-8252 0629",  // Discover
                    "A123456789007",
                    "B23456789\n0004",
                    "345678901009***"
            })
            void shouldValidateCommonValidCreditCardsWithNonDigits(String creditCard) {
                assertThat(RenderUtils.validateCreditCard(creditCard)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Encoding Tests")
    class EncodingTests {
        private Properties createProperties(String encodingType) {
            var props = new Properties();
            props.setProperty(RenderUtils.ENCODING_PROPERTY, encodingType);
            return props;
        }

        @Test
        void encodeHtml() {
            var p = createProperties("html");
            assertThat(RenderUtils.encode("<a test &>", p)).isEqualTo("&lt;a test &amp;&gt;");
        }

        @Test
        void encodeJs() {
            var p = createProperties("js");
            assertThat(RenderUtils.encode("\"test'", p)).isEqualTo("\\\"test\\'");
        }

        @Test
        void encodeJson() {
            var p = createProperties("json");
            assertThat(RenderUtils.encode("This is a \"•test\"", p))
                    .isEqualTo("This is a \\\"\\u2022test\\\"");
        }

        @Test
        void encodeUnicode() {
            var p = createProperties("unicode");
            assertThat(RenderUtils.encode("test", p)).isEqualTo("\\u0074\\u0065\\u0073\\u0074");
        }

        @Test
        void encodeUrl() {
            var p = createProperties("url");
            assertThat(RenderUtils.encode("a = test", p)).isEqualTo("a%20%3D%20test");
        }

        @Test
        void encodeWhenEncodingPropertyIsEmptyString() {
            var src = "testString";
            var p = createProperties(""); // Encoding property is ""
            assertThat(RenderUtils.encode(src, p)).isEqualTo(src);
        }

        @Test
        void encodeWhenEncodingPropertyIsMissing() {
            var src = "testString";
            var p = new Properties();
            p.setProperty("some.other.property", "value"); // Not empty, but no ENCODING_PROPERTY
            assertThat(RenderUtils.encode(src, p)).isEqualTo(src);
        }

        @Test
        void encodeWhenEncodingPropertyIsUnknown() {
            var src = "testString";
            var p = createProperties("unknown_encoding_type");
            assertThat(RenderUtils.encode(src, p)).isEqualTo(src);
        }

        @Test
        void encodeWhenPropertiesIsEmpty() {
            var src = "testString";
            var p = new Properties();
            assertThat(RenderUtils.encode(src, p)).isEqualTo(src);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void encodeWhenSrcIsBlank(String blankSrc) {
            var p = createProperties("html"); // Properties are not empty
            assertThat(RenderUtils.encode(blankSrc, p)).as("encode(%s)", blankSrc)
                    .isEqualTo(blankSrc);
        }

        @Test
        void encodeWhenSrcIsNull() {
            var p = createProperties("html"); // Properties are not empty
            assertThat(RenderUtils.encode(null, p)).isNull();
        }

        @Test
        void encodeWithInvalidFormat() {
            var p = createProperties("blah");
            assertThat(RenderUtils.encode(TestCase.SAMPLE_TEXT, p)).isEqualTo(TestCase.SAMPLE_TEXT);
        }

        @Test
        void encodeXml() {
            var p = createProperties("xml");
            assertThat(RenderUtils.encode("Joe's Café & Bar", p)).isEqualTo("Joe&apos;s Café &amp; Bar");
        }


        @Nested
        @DisplayName("Encode JavaScript Tests")
        class EncodeJavaScriptTests {
            @Test
            @DisplayName("Should be consistent with multiple calls")
            void shouldBeConsistentWithMultipleCalls() {
                var input = "Test'String\"With\\Special/Characters\n\t";
                var result1 = RenderUtils.encodeJs(input);
                var result2 = RenderUtils.encodeJs(input);

                assertThat(result1).isEqualTo(result2);
            }

            @Test
            @DisplayName("Should encode backslash")
            void shouldEncodeBackslash() {
                assertThat(RenderUtils.encodeJs("\\")).isEqualTo("\\\\");
                assertThat(RenderUtils.encodeJs("Hello\\World")).isEqualTo("Hello\\\\World");
            }

            @Test
            @DisplayName("Should encode backspace")
            void shouldEncodeBackspace() {
                assertThat(RenderUtils.encodeJs("\b")).isEqualTo("\\b");
                assertThat(RenderUtils.encodeJs("Hello\bWorld")).isEqualTo("Hello\\bWorld");
            }

            @Test
            @DisplayName("Should encode carriage return")
            void shouldEncodeCarriageReturn() {
                assertThat(RenderUtils.encodeJs("\r")).isEqualTo("\\r");
                assertThat(RenderUtils.encodeJs("Hello\rWorld")).isEqualTo("Hello\\rWorld");
            }

            @Test
            @DisplayName("Should encode control characters as unicode escapes")
            void shouldEncodeControlCharacters() {
                // Test some control characters
                try (var softly = new AutoCloseableSoftAssertions()) {
                    softly.assertThat(RenderUtils.encodeJs("\u0000")).isEqualTo("\\u0000"); // null character
                    softly.assertThat(RenderUtils.encodeJs("\u0001")).isEqualTo("\\u0001"); // start of heading
                    softly.assertThat(RenderUtils.encodeJs("\u001f")).isEqualTo("\\u001F"); // unit separator
                    softly.assertThat(RenderUtils.encodeJs("\u007f")).isEqualTo("\\u007F"); // delete character
                }
            }

            @Test
            @DisplayName("Should encode double quote")
            void shouldEncodeDoubleQuote() {
                assertThat(RenderUtils.encodeJs("\"")).isEqualTo("\\\"");
                assertThat(RenderUtils.encodeJs("Hello\"World")).isEqualTo("Hello\\\"World");
            }

            @Test
            @DisplayName("Should encode form feed")
            void shouldEncodeFormFeed() {
                assertThat(RenderUtils.encodeJs("\f")).isEqualTo("\\f");
                assertThat(RenderUtils.encodeJs("Hello\fWorld")).isEqualTo("Hello\\fWorld");
            }

            @Test
            @DisplayName("Should encode forward slash")
            void shouldEncodeForwardSlash() {
                assertThat(RenderUtils.encodeJs("/")).isEqualTo("\\/");
                assertThat(RenderUtils.encodeJs("Hello/World")).isEqualTo("Hello\\/World");
            }

            @Test
            @DisplayName("Should encode multiple special characters")
            void shouldEncodeMultipleSpecialCharacters() {
                var input = "Hello\n\t\"World\"\\/'Test'";
                var expected = "Hello\\n\\t\\\"World\\\"\\\\\\/\\'Test\\'";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            @DisplayName("Should encode newline")
            void shouldEncodeNewline() {
                assertThat(RenderUtils.encodeJs("\n")).isEqualTo("\\n");
                assertThat(RenderUtils.encodeJs("Hello\nWorld")).isEqualTo("Hello\\nWorld");
            }

            @Test
            @DisplayName("Should encode single quote")
            void shouldEncodeSingleQuote() {
                assertThat(RenderUtils.encodeJs("'")).isEqualTo("\\'");
                assertThat(RenderUtils.encodeJs("Hello'World")).isEqualTo("Hello\\'World");
            }

            @Test
            @DisplayName("Should encode tab")
            void shouldEncodeTab() {
                assertThat(RenderUtils.encodeJs("\t")).isEqualTo("\\t");
                assertThat(RenderUtils.encodeJs("Hello\tWorld")).isEqualTo("Hello\\tWorld");
            }

            @Test
            @DisplayName("Should handle JavaScript code snippet")
            void shouldHandleJavaScriptCodeSnippet() {
                var input = "alert('Hello\\nWorld');";
                var expected = "alert(\\'Hello\\\\nWorld\\');";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            @DisplayName("Should handle JSON-like string")
            void shouldHandleJsonLikeString() {
                var input = "{\"name\": \"John\", \"age\": 30}";
                var expected = "{\\\"name\\\": \\\"John\\\", \\\"age\\\": 30}";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            @DisplayName("Should handle long string efficiently")
            void shouldHandleLongString() {
                var input = "test'string\"with\\special/chars\n".repeat(1000);
                var result = RenderUtils.encodeJs(input);

                assertThat(result)
                        .isNotNull()
                        .contains("\\'")
                        .contains("\\\"")
                        .contains("\\\\")
                        .contains("\\/")
                        .contains("\\n");
            }

            @Test
            @DisplayName("Should handle mixed content with control characters")
            void shouldHandleMixedContentWithControlCharacters() {
                var input = "Hello\u0001World\u007f";
                var expected = "Hello\\u0001World\\u007F";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {"", " ", "   "})
            @DisplayName("Should handle null and empty strings")
            void shouldHandleNullAndEmptyStrings(String input) {
                var result = RenderUtils.encodeJs(input);
                assertThat(result).isEqualTo(input);
            }

            @Test
            @DisplayName("Should handle edge case with only special characters")
            void shouldHandleOnlySpecialCharacters() {
                var input = "'\"\\/\b\n\t\f\r";
                var expected = "\\'\\\"\\\\\\/\\b\\n\\t\\f\\r";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            @DisplayName("Should handle regular text without encoding")
            void shouldHandleRegularText() {
                var input = "Hello World 123 ABC xyz";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(input);
            }

            @Test
            @DisplayName("Should handle unicode characters above control range")
            void shouldHandleUnicodeCharacters() {
                var input = "Hello 世界 🌍";
                var expected = "Hello \\u4E16\\u754C \\uD83C\\uDF0D";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            @DisplayName("Should return null for null input")
            void shouldReturnNullForNullInput() {
                assertThat(RenderUtils.encodeJs(null)).isNull();
            }
        }
    }

    @Nested
    @DisplayName("FetchUrl Tests")
    class FetchUrlTests {
        private static final String DEFAULT = "default";

        @Test
        void fetchUrl() {
            assertThat(RenderUtils.fetchUrl("https://postman-echo.com/get?foo=bar", DEFAULT))
                    .contains("\"foo\":\"bar\"");
        }

        @Test
        void fetchUrlWith404() {
            assertThat(RenderUtils.fetchUrl("https://www.google.com/404", DEFAULT)).isEqualTo(DEFAULT);
        }

        @Test
        void fetchUrlWithInvalidHostname() {
            assertThat(RenderUtils.fetchUrl("https://www.notreallythere.com/", DEFAULT)).isEqualTo(DEFAULT);
        }

        @Test
        void fetchUrlWithInvalidUrl() {
            assertThat(RenderUtils.fetchUrl("blah", DEFAULT)).isEqualTo(DEFAULT);
        }
    }

    @Nested
    @DisplayName("HTML Entities Tests")
    class HtmlEntitiesTests {
        @Test
        @DisplayName("Should be consistent across multiple calls")
        void shouldBeConsistentAcrossMultipleCalls() {
            var input = "Hello 世界! <test>";
            var result1 = RenderUtils.htmlEntities(input);
            var result2 = RenderUtils.htmlEntities(input);

            assertThat(result1).isEqualTo(result2);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "a", "A", "1", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")",
                "_", "+", "=", "[", "]", "{", "}", "|", "\\", ":", ";", "\"", "'",
                "<", ">", ",", ".", "?", "/", "~", "`"
        })
        @DisplayName("Should encode all printable ASCII characters")
        void shouldEncodeAllPrintableAsciiCharacters(String character) {
            var result = RenderUtils.htmlEntities(character);

            assertThat(result)
                    .isNotNull()
                    .startsWith("&#")
                    .endsWith(";")
                    .matches("&#\\d+;");
        }

        @ParameterizedTest
        @CsvSource({
                "A, &#65;",
                "a, &#97;",
                "0, &#48;",
                "!, &#33;",
                "@, &#64;"
        })
        @DisplayName("Should encode various ASCII characters correctly")
        void shouldEncodeAsciiCharacters(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should encode control characters")
        void shouldEncodeControlCharacters() {
            assertThat(RenderUtils.htmlEntities("\u0000")).isEqualTo("&#0;");
            assertThat(RenderUtils.htmlEntities("\u0001")).isEqualTo("&#1;");
            assertThat(RenderUtils.htmlEntities("\u001F")).isEqualTo("&#31;");
            assertThat(RenderUtils.htmlEntities("\u007F")).isEqualTo("&#127;");
        }

        @ParameterizedTest
        @CsvSource({
                "😀, &#128512;",
                "🌍, &#127757;",
                "❤️, &#10084;&#65039;"
        })
        @DisplayName("Should encode emoji characters")
        void shouldEncodeEmojiCharacters(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should encode HTML tag completely")
        void shouldEncodeHtmlTag() {
            var input = "<div>";
            var expected = "&#60;&#100;&#105;&#118;&#62;";
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "á, &#225;",
                "é, &#233;",
                "ñ, &#241;",
                "ü, &#252;"
        })
        @DisplayName("Should encode Latin extended characters")
        void shouldEncodeLatinExtendedCharacters(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should encode mixed content")
        void shouldEncodeMixedContent() {
            var input = "Hello 世界! <test>";
            var expected = "&#72;&#101;&#108;&#108;&#111;&#32;&#19990;&#30028;&#33;&#32;&#60;&#116;&#101;&#115;&#116;&#62;";
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should encode multiple ASCII characters")
        void shouldEncodeMultipleAsciiCharacters() {
            assertThat(RenderUtils.htmlEntities("Hello"))
                    .isEqualTo("&#72;&#101;&#108;&#108;&#111;");
        }

        @ParameterizedTest
        @CsvSource({
                "123, &#49;&#50;&#51;",
                "0, &#48;",
                "9, &#57;"
        })
        @DisplayName("Should encode numbers correctly")
        void shouldEncodeNumbers(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "A, &#65;",
                "a, &#97;",
                "0, &#48;"
        })
        @DisplayName("Should encode single ASCII character")
        void shouldEncodeSingleAsciiCharacter(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "<, &#60;",
                ">, &#62;",
                "&, &#38;",
                "\", &#34;",
                "\"'\", &#34;&#39;&#34;"
        })
        @DisplayName("Should encode special HTML characters")
        void shouldEncodeSpecialHtmlCharacters(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "世, &#19990;",
                "界, &#30028;",
                "한, &#54620;",
                "국, &#44397;"
        })
        @DisplayName("Should encode Unicode characters")
        void shouldEncodeUnicodeCharacters(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "' ', &#32;",
                "'\t', &#9;",
                "'\n', &#10;",
                "'\r', &#13;",
        })
        @DisplayName("Should encode whitespace characters")
        void shouldEncodeWhitespaceCharacters(String input, String expected) {
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle complete HTML document snippet")
        void shouldHandleHtmlDocumentSnippet() {
            var input = "<html><body>Hello & 世界</body></html>";
            var expected = "&#60;&#104;&#116;&#109;&#108;&#62;&#60;&#98;&#111;&#100;&#121;&#62;" +
                    "&#72;&#101;&#108;&#108;&#111;&#32;&#38;&#32;&#19990;&#30028;" +
                    "&#60;&#47;&#98;&#111;&#100;&#121;&#62;&#60;&#47;&#104;&#116;&#109;&#108;&#62;";
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle JavaScript code snippet")
        void shouldHandleJavaScriptSnippet() {
            var input = "alert('Hello');";
            var expected = "&#97;&#108;&#101;&#114;&#116;&#40;&#39;&#72;&#101;&#108;&#108;&#111;&#39;&#41;&#59;";
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle long string efficiently")
        void shouldHandleLongString() {
            var input = "Test<>&\"'".repeat(1000);
            var result = RenderUtils.htmlEntities(input);

            assertThat(result)
                    .isNotNull()
                    .isNotEmpty()
                    .startsWith("&#84;&#101;&#115;&#116;") // "Test"
                    .contains("&#60;") // <
                    .contains("&#62;") // >
                    .contains("&#38;") // &
                    .contains("&#34;") // "
                    .contains("&#39;"); // '
        }

        @Test
        @DisplayName("Should handle string with mixed emoji and text")
        void shouldHandleMixedEmojiAndText() {
            var input = "Hello 😀 World 🌍!";
            assertThat(RenderUtils.htmlEntities(input))
                    .contains("&#72;&#101;&#108;&#108;&#111;") // Hello
                    .contains("&#128512;") // 😀
                    .contains("&#87;&#111;&#114;&#108;&#100;") // World
                    .contains("&#127757;") // 🌍
                    .contains("&#33;"); // !
        }

        @Test
        @DisplayName("Should handle edge case with only special characters")
        void shouldHandleOnlySpecialCharacters() {
            var input = "<>&\"'";
            var expected = "&#60;&#62;&#38;&#34;&#39;";
            assertThat(RenderUtils.htmlEntities(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle surrogate pairs correctly")
        void shouldHandleSurrogatePairs() {
            // Musical symbol (requires surrogate pair)
            var musicalNote = "𝄞"; // U+1D11E
            assertThat(RenderUtils.htmlEntities(musicalNote)).isEqualTo("&#119070;");

            // Another high Unicode character
            var mathSymbol = "𝒳"; // U+1D4B3
            assertThat(RenderUtils.htmlEntities(mathSymbol)).isEqualTo("&#119987;");
        }

        @Test
        @DisplayName("Should preserve character order in output")
        void shouldPreserveCharacterOrder() {
            var input = "ABC";
            var result = RenderUtils.htmlEntities(input);

            assertThat(result)
                    .startsWith("&#65;") // A
                    .contains("&#66;")   // B (should be in middle)
                    .endsWith("&#67;");  // C
        }

        @Test
        @DisplayName("Should return empty string for empty input")
        void shouldReturnEmptyStringForEmptyInput() {
            assertThat(RenderUtils.htmlEntities("")).isEmpty();
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(RenderUtils.htmlEntities(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Mask Tests")
    class MaskTests {
        public static final String FOO = "4342256562440179";

        @Test
        void maskWithDash() {
            assertThat(RenderUtils.mask(FOO, "–", 22, true))
                    .isEqualTo("––––––––––––––––");
        }

        @Test
        void maskWithEmpty() {
            assertThat(RenderUtils.mask("", " ", 2, false)).isEmpty();
        }

        @Test
        void maskWithHtmlBuller() {
            assertThat(RenderUtils.mask(FOO, "&bull;", -1, false)).isEqualTo(
                    "&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;");
        }

        @Test
        void maskWithQuestionMark() {
            assertThat(RenderUtils.mask(FOO, "?", 4, false)).as("mask=?")
                    .isEqualTo("????????????0179");
        }
    }

    @Nested
    @DisplayName("Normalize Tests")
    class NormalizeTests {
        @Test
        void normalize() {
            assertThat(RenderUtils.normalize("News for January 6, 2023 (Paris)")).as("docs example")
                    .isEqualTo("news-for-january-6-2023-paris");
        }

        @Test
        void normalizeWithGerman() {
            assertThat(RenderUtils.normalize(SAMPLE_GERMAN)).as("greman")
                    .isEqualTo("mochten-sie-ein-paar-apfel");
        }

        @Test
        void normalizeWithMixedPunctuation() {
            assertThat(RenderUtils.normalize(" &()-_=[{]}\\|;:,<.>/")).as("blank").isEmpty();
        }

        @Test
        void normalizeWithMixedSeparators() {
            assertThat(RenderUtils.normalize("foo  bar, <foo-bar>,foo:bar,foo;(bar), {foo} & bar=foo.bar[foo|bar]"))
                    .as("foo-bar")
                    .isEqualTo("foo-bar-foo-bar-foo-bar-foo-bar-foo-bar-foo-bar-foo-bar");
        }
    }

    @Nested
    @DisplayName("ROT13 Tests")
    class Rot13Tests {
        @ParameterizedTest
        @CsvSource({
                "a, n",
                "n, a",
                "z, m",
                "m, z",
                "A, N",
                "N, A",
                "Z, M",
                "M, Z",
                "hello, uryyb",
                "world, jbeyq",
                "HELLO, URYYB",
                "WORLD, JBEYQ"
        })
        @DisplayName("Should apply ROT13 transformation to basic ASCII letters")
        void shouldApplyRot13ToBasicStrings(String input, String expected) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("ROT13 should be its own inverse (applying twice returns original)")
        void shouldBeItsOwnInverse() {
            String[] testCases = {
                    "Hello World",
                    "The quick brown fox",
                    "UPPERCASE",
                    "lowercase",
                    "MiXeD cAsE",
                    "Numbers 123 and symbols !@#",
                    "Unicode café naïve",
                    "Emoji 😀 test 🚀"
            };

            for (String original : testCases) {
                String encoded = RenderUtils.rot13(original);
                String decoded = RenderUtils.rot13(encoded);

                assertThat(decoded)
                        .as("ROT13 should be its own inverse for: %s", original)
                        .isEqualTo(original);
            }
        }

        @ParameterizedTest
        @CsvSource({
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ, NOPQRSTUVWXYZABCDEFGHIJKLM",
                "abcdefghijklmnopqrstuvwxyz, nopqrstuvwxyzabcdefghijklm",
                "abcdefghijklm, nopqrstuvwxyz",
                "nopqrstuvwxyz, abcdefghijklm",
                "ABCDEFGHIJKLM, NOPQRSTUVWXYZ",
                "NOPQRSTUVWXYZ, ABCDEFGHIJKLM"
        })
        @DisplayName("Should correctly map the full alphabet (A-M ↔ N-Z)")
        void shouldCorrectlyMapAlphabet(String input, String expected) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'Hello, World!', 'Uryyb, Jbeyq!'",
                "Test123, Grfg123",
                "'ROT13 cipher', 'EBG13 pvcure'",
                "a1b2c3, n1o2p3",
                "'Mix3d C4s3!', 'Zvk3q P4f3!'",
                "'The quick brown fox jumps over the lazy dog.', 'Gur dhvpx oebja sbk whzcf bire gur ynml qbt.'",
                "'Pack my box with five dozen liquor jugs.', 'Cnpx zl obk jvgu svir qbmra yvdhbe whtf.'"
        })
        @DisplayName("Should handle mixed content with letters, numbers, and symbols")
        void shouldHandleMixedContent(String input, String expected) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty input gracefully")
        void shouldHandleNullAndEmpty(String input) {
            String result = RenderUtils.rot13(input);

            if (input == null) {
                assertThat(result).isEmpty();
            } else {
                assertThat(result).isEqualTo(input);
            }
        }

        @Test
        @DisplayName("Should only transform ASCII letters (A-Z, a-z)")
        void shouldHandleOnlyAsciiLetters() {
            // Verify that only ASCII A-Z and a-z are transformed
            char[] asciiLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
            char[] expectedRot13 = "NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm".toCharArray();

            for (int i = 0; i < asciiLetters.length; i++) {
                String input = String.valueOf(asciiLetters[i]);
                String result = RenderUtils.rot13(input);
                String expected = String.valueOf(expectedRot13[i]);

                assertThat(result)
                        .as("ASCII letter %c should transform to %c", asciiLetters[i], expectedRot13[i])
                        .isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("Should not transform non-ASCII letters (accented, Greek, Cyrillic, etc.)")
        void shouldNotTransformNonAsciiLetters() {
            String[] nonAsciiLetters = {
                    "À", "Á", "Â", "Ã", "Ä", "Å", // Accented A
                    "à", "á", "â", "ã", "ä", "å", // Accented a
                    "Ç", "ç", "È", "É", "Ê", "Ë", // Other European
                    "α", "β", "γ", "δ", "ε", "ζ", // Greek
                    "А", "Б", "В", "Г", "Д", "Е"  // Cyrillic
            };

            for (String letter : nonAsciiLetters) {
                String result = RenderUtils.rot13(letter);
                assertThat(result)
                        .as("Non-ASCII letter %s should remain unchanged", letter)
                        .isEqualTo(letter);
            }
        }

        @ParameterizedTest
        @CsvSource({
                "Hello, Uryyb",
                "WoRlD, JbEyQ",
                "TeSt, GrFg",
                "ROT13, EBG13",
                "CamelCase, PnzryPnfr",
                "MiXeD, ZvKrQ"
        })
        @DisplayName("Should preserve original case when transforming mixed-case strings")
        void shouldPreserveCaseInMixedStrings(String input, String expected) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "😀, 😀",
                "'Hello 😀', 'Uryyb 😀'",
                "'Test 🌟 case', 'Grfg 🌟 pnfr'",
                "'🚀 Launch', '🚀 Ynhapu'",
                "Mix🎉ed, Zvk🎉rq"
        })
        @DisplayName("Should preserve emojis while transforming ASCII letters")
        void shouldPreserveEmojis(String input, String expected) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123",
                "!@#$%^&*()",
                "   ",
                "\t\n\r",
                ".,;:?!",
                "[]{}()",
                "+-*/=",
                "<>|&"
        })
        @DisplayName("Should leave non-alphabetic characters unchanged")
        void shouldPreserveNonAlphabeticCharacters(String input) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("Should preserve the original string length")
        void shouldPreserveStringLength() {
            String[] testCases = {
                    "Hello World",
                    "123ABC456def",
                    "!@#TeStInG$%^",
                    "Unicode café naïve",
                    "Emoji 😀 test 🚀",
                    "",
                    "a",
                    "ABC123!@#αβγ😀"
            };

            for (String testCase : testCases) {
                String result = RenderUtils.rot13(testCase);
                assertThat(result)
                        .as("Length should be preserved for: %s", testCase)
                        .hasSameSizeAs(testCase);
            }
        }

        @ParameterizedTest
        @CsvSource({
                "café, pnsé",
                "naïve, anïir",
                "résumé, eéfhzé",
                "Ñoño, Ñbñb",
                "αβγ, αβγ",
                "中文, 中文",
                "العربية, العربية",
                "русский, русский",
                "'Hello café', 'Uryyb pnsé'",
                "'Test αβγ', 'Grfg αβγ'",
                "Mix中ed, Zvk中rq"
        })
        @DisplayName("Should preserve Unicode characters while transforming ASCII letters")
        void shouldPreserveUnicodeCharacters(String input, String expected) {
            String result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Swap Case Tests")
    class SwapCaseTests {
        @ParameterizedTest
        @ValueSource(strings = {
                "hello",
                "world",
                "testing",
                "lowercase"
        })
        @DisplayName("Should convert lowercase to uppercase")
        void shouldConvertLowercaseToUppercase(String input) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEqualTo(input.toUpperCase());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "HELLO",
                "WORLD",
                "TESTING",
                "UPPERCASE"
        })
        @DisplayName("Should convert uppercase to lowercase")
        void shouldConvertUppercaseToLowercase(String input) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEqualTo(input.toLowerCase());
        }

        @ParameterizedTest
        @CsvSource({
                "' ', ' '",
                "A, a",
                "z, Z",
                "AaAaAa, aAaAaA",
                "123456, 123456",
                "!@#$%^, !@#$%^",
                "aA1bB2cC3, Aa1Bb2Cc3"
        })
        @DisplayName("Should handle edge cases")
        void shouldHandleEdgeCases(String input, String expected) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "a, A",
                "Z, z",
                "1, 1",
                "!, !"
        })
        @DisplayName("Should handle single character input")
        void shouldHandleSingleCharacter(String input, String expected) {
            assertThat(RenderUtils.swapCase(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'hello, WORLD!', 'HELLO, world!'",
                "Test@123, tEST@123",
                "a1B2c3D4, A1b2C3d4",
                "!@#$%^&*(), !@#$%^&*()",
                "Mix3d C4s3!, mIX3D c4S3!"
        })
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters(String input, String expected) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "café, CAFÉ",
                "NAÏVE, naïve",
                "résumé, RÉSUMÉ",
                "Ñoño, ñOÑO",
                "αβγ, ΑΒΓ",
                "ΑΒΓ, αβγ"
        })
        @DisplayName("Should handle Unicode characters")
        void shouldHandleUnicodeCharacters(String input, String expected) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "' ', ' '",
                "'\t', '\t'",
                "'\n', '\n'",
                "'\r', '\r'",
                "a b, A B",
                "'hello\tWorld\n', 'HELLO\twORLD\n'"
        })
        @DisplayName("Should handle whitespace characters")
        void shouldHandleWhitespaceCharacters(String input, String expected) {
            assertThat(RenderUtils.swapCase(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123",
                "!@#$%",
                "   ",
                "123abc",
                "ABC123",
                "hello123WORLD"
        })
        @DisplayName("Should preserve numbers and special characters")
        void shouldPreserveNonAlphabeticCharacters(String input) {
            var result = RenderUtils.swapCase(input);

            if (input.chars().noneMatch(Character::isLetter)) {
                assertThat(result).isEqualTo(input);
            } else {
                assertThat(result).hasSize(input.length());
                for (int i = 0; i < input.length(); i++) {
                    char inputChar = input.charAt(i);
                    char resultChar = result.charAt(i);
                    if (!Character.isLetter(inputChar)) {
                        assertThat(resultChar).isEqualTo(inputChar);
                    }
                }
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Hello World",
                "123ABC456def",
                "!@#TeStInG$%^",
                "αβγδεζ",
                ""
        })
        @DisplayName("Should preserve string length")
        void shouldPreserveStringLength(String testCase) {
            var result = RenderUtils.swapCase(testCase);
            assertThat(result).hasSize(testCase.length());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should return empty string for null or empty input")
        void shouldReturnEmptyStringForNullOrEmpty(String input) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
                "Hello, hELLO",
                "WoRlD, wOrLd",
                "TeSt, tEsT",
                "jAvA, JaVa",
                "hELLo WoRLD, HellO wOrld",
                "ThE QuIcK bRoWn FoX, tHe qUiCk BrOwN fOx"
        })
        @DisplayName("Should swap case for mixed case input")
        void shouldSwapMixedCase(String input, String expected) {
            var result = RenderUtils.swapCase(input);

            assertThat(result).isEqualTo(expected);
        }
    }
}
