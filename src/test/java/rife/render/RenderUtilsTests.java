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

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.Headers;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import rife.bld.extension.testing.RandomRange;
import rife.bld.extension.testing.RandomRangeResolver;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TestClassWithoutTestCases"})
class RenderUtilsTests {
    @Nested
    @DisplayName("Abbreviate Tests")
    class AbbreviateTests {
        @Test
        @NotWindowsJdk17
        void abbreviateWithEllipsis() {
            assertThat(RenderUtils.abbreviate(CaseTests.SAMPLE_TEXT, 10, "…"))
                    .isEqualTo("This is a…");
        }

        @Test
        void abbreviateWithEmpty() {
            assertThat(RenderUtils.abbreviate("", 10, "")).as("").isEmpty();
        }

        @Test
        void abbreviateWithMarker() {
            assertThat(RenderUtils.abbreviate(CaseTests.SAMPLE_TEXT, 12, "..."))
                    .isEqualTo("This is a...");
        }

        @Test
        void abbreviateWithMax() {
            assertThat(RenderUtils.abbreviate(CaseTests.SAMPLE_TEXT, 9, "")).isEqualTo("This is a");
        }

        @Test
        void abbreviateWithMaxNegative() {
            assertThat(RenderUtils.abbreviate(CaseTests.SAMPLE_TEXT, -1, "")).isEqualTo(CaseTests.SAMPLE_TEXT);
        }

        @Test
        void abbreviateWithMaxZero() {
            assertThat(RenderUtils.abbreviate(CaseTests.SAMPLE_TEXT, 0, "")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Capitalize Words Tests")
    class CapitalizeWordsTests {
        @ParameterizedTest
        @DisplayName("Should handle accented characters and diacritics")
        @NotWindowsJdk17
        @CsvSource({
                "'café naïve', 'Café Naïve'",
                "'schön günstig', 'Schön Günstig'",
                "'résumé français', 'Résumé Français'",
                "'ñoño niño', 'Ñoño Niño'",
                "'istanbul türkiye', 'Istanbul Türkiye'"
        })
        void shouldCapitalizeAccentedCharacters(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should capitalize basic ASCII words correctly")
        @CsvSource({
                "'hello world', 'Hello World'",
                "'HELLO WORLD', 'Hello World'",
                "'hELLo WoRLd', 'Hello World'",
                "'hello', 'Hello'",
                "'a', 'A'",
                "'java programming', 'Java Programming'",
                "'the quick brown fox', 'The Quick Brown Fox'"
        })
        void shouldCapitalizeBasicWords(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle complex surrogate pair scenarios")
        @NotWindowsJdk17
        void shouldHandleComplexSurrogatePairScenarios() {
            // Test string with multiple surrogate pairs and regular characters
            var input = "𝓱𝓮𝓵𝓵𝓸 world 🌟 test 𝕞𝕦𝕞𝕓𝕖𝔯";
            var expected = "𝓱𝓮𝓵𝓵𝓸 World 🌟 Test 𝕞𝕦𝕞𝕓𝕖𝔯";

            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle complex Unicode casing rules (e.g., German ß)")
        @NotWindowsJdk17
        void shouldHandleComplexUnicodeCase() {
            // German ß (sharp s) has special uppercasing rules
            var input = "straße café";
            var expected = "Straße Café";

            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should handle emojis and special Unicode characters")
        @NotWindowsJdk17
        @CsvSource({
                "'hello 🌟 world', 'Hello 🌟 World'",
                "'test 🚀 rocket', 'Test 🚀 Rocket'",
                "'café ☕ time', 'Café ☕ Time'",
                "'👋 hello world 👋', '👋 Hello World 👋'"
        })
        void shouldHandleEmojisAndSpecialUnicodeCharacters(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should handle mathematical Unicode characters")
        @NotWindowsJdk17
        @CsvSource({
                "'𝕙𝕖𝕝𝕝𝕠 𝕨𝕠𝕣𝕝𝕕', '𝕙𝕖𝕝𝕝𝕠 𝕨𝕠𝕣𝕝𝕕'", // Mathematical script letters
                "'𝒽𝑒𝓁𝓁𝑜 𝓌𝑜𝓇𝓁𝒹', '𝒽𝑒𝓁𝓁𝑜 𝓌𝑜𝓇𝓁𝒹'"  // Mathematical script letters
        })
        void shouldHandleMathematicalUnicodeCharacters(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should handle non-Latin scripts appropriately")
        @NotWindowsJdk17
        @CsvSource({
                "'こんにちは 世界', 'こんにちは 世界'",
                "'привет мир', 'Привет Мир'",
                "'مرحبا بالعالم', 'مرحبا بالعالم'",
                "'שלום עולם', 'שלום עולם'"
        })
        void shouldHandleNonLatinScripts(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r\n"})
        @DisplayName("Should return unchanged for null, empty, or whitespace-only strings")
        void shouldHandleNullEmptyAndWhitespaceOnlyStrings(String input) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(input);
        }

        @ParameterizedTest
        @NotWindowsJdk17
        @CsvSource({
                "é, É",
                "è, È",
        })
        @DisplayName("Should handle single accented character inputs correctly")
        void shouldHandleSingleAccentedCharacter(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "a, A",
                "Z, Z",
                "1, 1",
                "' ', ' '"
        })
        @DisplayName("Should handle single character inputs correctly")
        void shouldHandleSingleCharacter(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should handle punctuation, numbers, and special characters")
        @CsvSource({
                "'hello-world', 'Hello-world'",
                "'test_case', 'Test_case'",
                "'user@domain.com', 'User@domain.com'",
                "'file.txt extension', 'File.txt Extension'",
                "'123 numbers here', '123 Numbers Here'",
                "'mix3d c4s3', 'Mix3d C4s3'"
        })
        void shouldHandleSpecialCharactersAndNumbers(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should handle surrogate pairs correctly")
        @NotWindowsJdk17
        @CsvSource({
                "'𝐡𝐞𝐥𝐥𝐨 𝐰𝐨𝐫𝐥𝐝', '𝐡𝐞𝐥𝐥𝐨 𝐰𝐨𝐫𝐥𝐝'", // Mathematical bold letters (surrogate pairs)
                "'hello 𝕨𝕠𝓇𝓁𝒹 test', 'Hello 𝕨𝕠𝓇𝓁𝒹 Test'", // Mixed ASCII and surrogate pairs
                "'𝔞𝔟𝔠 𝔡𝔢𝔣', '𝔞𝔟𝔠 𝔡𝔢𝔣'", // Mathematical fraktur letters
                "'🎯 hello 🚀 world', '🎯 Hello 🚀 World'", // Emojis as surrogate pairs
                "'💻 programming 📱 mobile', '💻 Programming 📱 Mobile'"
        })
        void shouldHandleSurrogatePairs(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Should preserve various types of whitespace")
        @CsvSource({
                "'hello  world', 'Hello  World'",
                "'  hello world  ', '  Hello World  '",
                "'hello\tworld', 'Hello\tWorld'",
                "'hello\nworld', 'Hello\nWorld'",
                "'hello\r\nworld', 'Hello\r\nWorld'",
                "'a   b   c', 'A   B   C'"
        })
        void shouldHandleVariousWhitespaceCharacters(String input, String expected) {
            assertThat(RenderUtils.capitalizeWords(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should perform well with very long strings")
        void shouldHandleVeryLongStrings() {
            String longInput = "word ".repeat(1000).trim();
            String expectedOutput = "Word ".repeat(1000).trim();

            assertThat(RenderUtils.capitalizeWords(longInput)).isEqualTo(expectedOutput);
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
            @NullAndEmptySource
            @DisplayName("Should reject null and empty strings")
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
        @Test
        void encodeHtml() {
            var p = createProperties("html");
            assertThat(RenderUtils.encode("<a test &>", p)).isEqualTo("&lt;a test &amp;&gt;");
        }

        private Properties createProperties(String encodingType) {
            var props = new Properties();
            props.setProperty(RenderUtils.ENCODING_PROPERTY, encodingType);
            return props;
        }

        @Test
        void encodeJs() {
            var p = createProperties("js");
            assertThat(RenderUtils.encode("\"test'", p)).isEqualTo("\\\"test\\'");
        }

        @Test
        void encodeJson() {
            var p = createProperties("json");
            assertThat(RenderUtils.encode("This is a \"test\"", p))
                    .isEqualTo("This is a \\\"test\\\"");
        }

        @Test
        @NotWindowsJdk17
        void encodeJsonWithUnicode() {
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
            assertThat(RenderUtils.encode(blankSrc, p)).as("encode(%s)", blankSrc).isEqualTo(blankSrc);
        }

        @Test
        void encodeWhenSrcIsNull() {
            var p = createProperties("html"); // Properties are not empty
            assertThat(RenderUtils.encode(null, p)).isNull();
        }

        @Test
        void encodeWithInvalidFormat() {
            var p = createProperties("blah");
            assertThat(RenderUtils.encode(CaseTests.SAMPLE_TEXT, p)).isEqualTo(CaseTests.SAMPLE_TEXT);
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

            @ParameterizedTest
            @DisplayName("Should escape Unicode")
            @NotWindowsJdk17
            @CsvSource({
                    "'世', '\\u4E16'",
                    "'界', '\\u754C'",
                    "'é', '\\u00E9'",
                    "'ñ', '\\u00F1'",
                    "'ü', '\\u00FC'"
            })
            void shouldEscapeUnicode(String input, String expected) {
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
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

            @ParameterizedTest
            @DisplayName("Should handle mixed ASCII and Unicode content")
            @NotWindowsJdk17
            @CsvSource({
                    "'Hello 世界', 'Hello \\u4E16\\u754C'",
                    "'café-shop', 'caf\\u00E9-shop'",
                    "'Price: €100', 'Price: \\u20AC100'"
            })
            void shouldHandleMixedContent(String input, String expected) {
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
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
            @DisplayName("Should handle emoji and surrogate pairs correctly")
            @NotWindowsJdk17
            void shouldHandleSurrogatePairs() {
                String emoji = "😀"; // U+1F600, requires surrogate pair
                var result = RenderUtils.encodeJs(emoji);

                assertThat(result)
                        .isEqualTo("\\uD83D\\uDE00")
                        .hasSize(12);
            }

            @Test
            @DisplayName("Should handle unicode characters above control range")
            @NotWindowsJdk17
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
        private static final MockWebServer MOCK_WEB_SERVER = new MockWebServer();

        @BeforeEach
        void beforeEach() throws IOException {
            MOCK_WEB_SERVER.start();
        }

        @Test
        void fetchUrl() {
            var foobar = "foobar";
            MOCK_WEB_SERVER.enqueue(
                    new MockResponse(200, Headers.EMPTY, foobar)
            );
            assertThat(RenderUtils.fetchUrl(MOCK_WEB_SERVER.url("/").toString(), DEFAULT))
                    .isEqualTo(foobar);
        }

        @Test
        void fetchUrlWith404() {
            MOCK_WEB_SERVER.enqueue(
                    new MockResponse.Builder().code(404).build()
            );
            assertThat(RenderUtils.fetchUrl(MOCK_WEB_SERVER.url("/").toString(), DEFAULT)).isEqualTo(DEFAULT);
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
        @NotWindowsJdk17
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
        @NotWindowsJdk17
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
        @NotWindowsJdk17
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
        @NotWindowsJdk17
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
        @NotWindowsJdk17
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
        @NotWindowsJdk17
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
        @NotWindowsJdk17
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
                    .contains("&#66;")   // B (should be in the middle)
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
        @Test
        @DisplayName("Method should handle all combinations consistently")
        void allCombinationsConsistent() {
            var input = "testing";

            // Test all combinations of fromStart boolean and various unmasked values
            for (boolean fromStart : new boolean[]{true, false}) {
                for (int unmasked = -1; unmasked <= input.length() + 1; unmasked++) {
                    var result = RenderUtils.mask(input, "*", unmasked, fromStart);

                    // Result should never be null for non-null input
                    assertThat(result).isNotNull();

                    // Result should always have the same length as input
                    assertThat(result).hasSize(input.length());

                    // Result should contain only original characters or mask characters
                    assertThat(result.chars().allMatch(c ->
                            input.indexOf(c) >= 0 || c == '*'
                    )).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("Different mask characters")
        class DifferentMaskCharacters {
            @Test
            @DisplayName("Multi-character mask string")
            void multiCharacterMask() {
                assertThat(RenderUtils.mask("hello", "**", 2, true))
                        .isEqualTo("he******");
                assertThat(RenderUtils.mask("test", "ab", 1, false))
                        .isEqualTo("abababt");
            }

            @ParameterizedTest
            @DisplayName("Various mask characters")
            @CsvSource(delimiter = '|', value = {
                    "'hello'|'*'|2|true|'he***'",
                    "'hello'|'#'|2|true|'he###'",
                    "'hello'|'X'|2|true|'heXXX'",
                    "'hello'|'-'|2|true|'he---'",
                    "'hello'|'•'|2|true|'he•••'",
                    "'hello'|'_'|2|false|'___lo'"
            })
            void variousMaskCharacters(String input,
                                       String maskChar,
                                       int unmasked,
                                       boolean fromStart,
                                       String expected) {
                assertThat(RenderUtils.mask(input, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("Edge cases and special scenarios")
        @ExtendWith(RandomRangeResolver.class)
        class EdgeCasesAndSpecialScenarios {
            @ParameterizedTest
            @DisplayName("Single character strings")
            @CsvSource({
                    "'a', '*', 1, true, '*'",
                    "'a', '*', 1, false, '*'",
                    "'a', '*', 0, true, '*'",
                    "'a', '#', 2, true, '#'"
            })
            void singleCharacterStrings(String input,
                                        String maskChar,
                                        int unmasked,
                                        boolean fromStart,
                                        String expected) {
                assertThat(RenderUtils.mask(input, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }

            @Test
            @DisplayName("Special characters in input")
            void specialCharactersInInput() {
                assertThat(RenderUtils.mask("user@domain.com", "*", 4, true))
                        .isEqualTo("user***********");

                assertThat(RenderUtils.mask("123-45-6789", "#", 3, false))
                        .isEqualTo("########789");

                assertThat(RenderUtils.mask("!@#$%^&*()", "X", 2, true))
                        .isEqualTo("!@XXXXXXXX");
            }

            @Test
            @DisplayName("Unicode characters")
            @NotWindowsJdk17
            void unicodeCharacters() {
                assertThat(RenderUtils.mask("héllo", "*", 2, true))
                        .isEqualTo("hé***");

                assertThat(RenderUtils.mask("🙂🙃😊😂", "*", 2, false))
                        .isEqualTo("**😊😂");
            }

            @RepeatedTest(3)
            @DisplayName("Very long strings")
            @RandomRange(min = 500, max = 5000)
            void veryLongStrings(int length) {
                var input = "a".repeat(length);
                var result = RenderUtils.mask(input, "*", 5, true);

                assertThat(result).hasSize(length);
                assertThat(result).startsWith("aaaaa");
                assertThat(result.substring(5)).isEqualTo("*".repeat(length - 5));
            }
        }

        @Nested
        @DisplayName("Full masking scenarios")
        class FullMasking {
            @ParameterizedTest
            @DisplayName("Unmasked >= length should fully mask")
            @CsvSource({
                    "'hi', '*', 2, true, '**'",
                    "'hi', '*', 3, true, '**'",
                    "'hello', '#', 5, false, '#####'",
                    "'test', 'X', 10, false, 'XXXX'"
            })
            void unmaskedGreaterThanLengthFullyMasks(String input,
                                                     String maskChar,
                                                     int unmasked,
                                                     boolean fromStart,
                                                     String expected) {
                assertThat(RenderUtils.mask(input, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }

            @ParameterizedTest
            @DisplayName("Zero or negative unmasked should fully mask")
            @CsvSource({
                    "'hello', '*', 0, true, '*****'",
                    "'hello', '*', -1, true, '*****'",
                    "'hello', '*', -5, false, '*****'",
                    "'test', '#', 0, false, '####'"
            })
            void zeroOrNegativeUnmaskedFullyMasks(String input,
                                                  String maskChar,
                                                  int unmasked,
                                                  boolean fromStart,
                                                  String expected) {
                assertThat(RenderUtils.mask(input, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("Null and empty input handling")
        class NullAndEmptyInput {
            @Test
            @DisplayName("Empty input should return empty string")
            void emptyInputReturnsEmpty() {
                assertThat(RenderUtils.mask("", "*", 2, true)).isEmpty();
                assertThat(RenderUtils.mask("", "*", 2, false)).isEmpty();
            }

            @Test
            @DisplayName("Null input should return null")
            void nullInputReturnsNull() {
                assertThat(RenderUtils.mask(null, "*", 2, true)).isNull();
                assertThat(RenderUtils.mask(null, "*", 2, false)).isNull();
            }
        }

        @Nested
        @DisplayName("Partial masking from end")
        class PartialMaskingFromEnd {
            @ParameterizedTest
            @DisplayName("Mask first part, show last N characters")
            @CsvSource({
                    "'hello', '*', 1, false, '****o'",
                    "'hello', '*', 2, false, '***lo'",
                    "'hello', '*', 3, false, '**llo'",
                    "'hello', '*', 4, false, '*ello'",
                    "'password123', '#', 3, false, '########123'",
                    "'ab', 'X', 1, false, 'Xb'",
                    "'test@email.com', '*', 3, false, '***********com'"
            })
            void maskFirstShowLastCharacters(String input,
                                             String maskChar,
                                             int unmasked,
                                             boolean fromStart,
                                             String expected) {
                assertThat(RenderUtils.mask(input, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("Partial masking from start")
        class PartialMaskingFromStart {
            @ParameterizedTest
            @DisplayName("Show first N characters, mask the rest")
            @CsvSource({
                    "'hello', '*', 1, true, 'h****'",
                    "'hello', '*', 2, true, 'he***'",
                    "'hello', '*', 3, true, 'hel**'",
                    "'hello', '*', 4, true, 'hell*'",
                    "'password123', '#', 4, true, 'pass#######'",
                    "'ab', 'X', 1, true, 'aX'",
                    "'test@email.com', '*', 4, true, 'test**********'"
            })
            void showFirstCharactersMaskRest(String input,
                                             String maskChar,
                                             int unmasked,
                                             boolean fromStart,
                                             String expected) {
                assertThat(RenderUtils.mask(input, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("Real-world use cases")
        class RealWorldUseCases {
            @ParameterizedTest
            @DisplayName("Credit card masking")
            @CsvSource({
                    "'1234567890123456', '*', 4, false, '************3456'",
                    "'4111-1111-1111-1111', '*', 4, false, '***************1111'",
                    "'5555 5555 5555 4444', 'X', 4, false, 'XXXXXXXXXXXXXXX4444'"
            })
            void creditCardMasking(String cardNumber,
                                   String maskChar,
                                   int unmasked,
                                   boolean fromStart,
                                   String expected) {
                assertThat(RenderUtils.mask(cardNumber, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }

            @ParameterizedTest
            @DisplayName("Email masking")
            @CsvSource({
                    "'john.doe@example.com', '*', 4, true, 'john****************'",
                    "'user@domain.co.uk', '#', 2, true, 'us###############'",
                    "'test.email@company.org', '*', 3, false, '*******************org'"
            })
            void emailMasking(String email, String maskChar, int unmasked, boolean fromStart, String expected) {
                assertThat(RenderUtils.mask(email, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }

            @ParameterizedTest
            @DisplayName("Phone number masking")
            @CsvSource({
                    "'+1-555-123-4567', '*', 4, false, '***********4567'",
                    "'(555) 123-4567', '#', 3, true, '(55###########'",
                    "'555.123.4567', 'X', 4, false, 'XXXXXXXX4567'"
            })
            void phoneNumberMasking(String phone, String maskChar, int unmasked, boolean fromStart, String expected) {
                assertThat(RenderUtils.mask(phone, maskChar, unmasked, fromStart)).isEqualTo(expected);
            }
        }
    }

    @Nested
    @DisplayName("Normalize Tests")
    class NormalizeTests {
        @ParameterizedTest
        @CsvSource({
                "'hello', 'hello'",
                "'HELLO', 'hello'",
                "'Hello', 'hello'",
                "'HeLLo', 'hello'",
                "'ABC123', 'abc123'",
                "'Test123', 'test123'"
        })
        @DisplayName("Should convert uppercase to lowercase")
        void shouldConvertUppercaseToLowercase(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle common separators")
        void shouldHandleCommonSeparators() {
            try (var softly = new AutoCloseableSoftAssertions()) {
                for (var sep : RenderUtils.COMMON_SEPARATORS) {
                    softly.assertThat(RenderUtils.normalize("hello" + sep + "world"))
                            .isEqualTo("hello-world");
                }
            }
        }

        @ParameterizedTest
        @CsvSource({
                "'Hello World!', 'hello-world'",
                "'The Quick Brown Fox', 'the-quick-brown-fox'",
                "'Java & Spring Boot', 'java-spring-boot'",
                "'API_V2.0', 'api-v2-0'",
                "'user@example.com', 'user-example-com'",
                "'file-name.txt', 'file-name-txt'",
                "'My-Awesome_Project', 'my-awesome-project'"
        })
        @DisplayName("Should handle complex real-world examples")
        void shouldHandleComplexRealWorldExamples(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle empty result after normalization")
        @NotWindowsJdk17
        void shouldHandleEmptyResultAfterNormalization() {
            assertThat(RenderUtils.normalize("世界")).isEqualTo("");
            assertThat(RenderUtils.normalize("🙂🙃")).isEqualTo("");
            assertThat(RenderUtils.normalize("αβγ")).isEqualTo("");
        }

        @Test
        @DisplayName("Should handle long strings efficiently")
        void shouldHandleLongStringsEfficiently() {
            String longInput = "a".repeat(1000) + " " + "b".repeat(1000);
            String expected = "a".repeat(1000) + "-" + "b".repeat(1000);

            assertThat(RenderUtils.normalize(longInput)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "a, a",
                "A, a",
                "1, 1",
                "' ', ''",
                "&, ''"
        })
        @DisplayName("Should handle single character inputs")
        void shouldHandleSingleCharacterInputs(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'héllo wörld', 'hello-world'",
                "'café', 'cafe'",
                "'naïve', 'naive'",
                "'résumé', 'resume'",
                "'Björk', 'bjork'",
                "'François', 'francois'"
        })
        @DisplayName("Should normalize accented characters")
        @NotWindowsJdk17
        void shouldNormalizeAccentedCharacters(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'hello123world', 'hello123world'",
                "'test-123-abc', 'test-123-abc'",
                "'abc123def456', 'abc123def456'",
                "'123hello456', '123hello456'",
                "'123', '123'",
                "'abc', 'abc'"
        })
        @DisplayName("Should preserve alphanumeric characters")
        void shouldPreserveAlphanumericCharacters(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'hello世界world', 'helloworld'",
                "'test中文test', 'testtest'",
                "'café世界', 'cafe'",
                "'🙂hello🙂', 'hello'",
                "'αβγ hello δεζ', 'hello'"
        })
        @DisplayName("Should remove non-ASCII characters")
        @NotWindowsJdk17
        void shouldRemoveNonAsciiCharacters(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'hello   world', 'hello-world'",
                "'hello&&&world', 'hello-world'",
                "'hello()()world', 'hello-world'",
                "'hello---world', 'hello-world'",
                "'hello___world', 'hello-world'",
                "'hello &- world', 'hello-world'",
                "'hello.,;world', 'hello-world'",
                "'hello   &&&   world', 'hello-world'"
        })
        @DisplayName("Should replace multiple consecutive separators with single hyphen")
        void shouldReplaceMultipleSeparatorsWithSingleHyphen(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'hello world', 'hello-world'",
                "'hello&world', 'hello-world'",
                "'hello()world', 'hello-world'",
                "'hello-world', 'hello-world'",
                "'hello_world', 'hello-world'",
                "'hello=[world', 'hello-world'",
                "'hello{world}', 'hello-world'",
                "'hello\\world', 'hello-world'",
                "'hello|world', 'hello-world'",
                "'hello;world', 'hello-world'",
                "'hello:world', 'hello-world'",
                "'hello,world', 'hello-world'",
                "'hello<world>', 'hello-world'",
                "'hello.world', 'hello-world'",
                "'hello/world', 'hello-world'"
        })
        @DisplayName("Should replace single separators with hyphen")
        void shouldReplaceSingleSeparatorsWithHyphen(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "'   ', ''",
                "'&&&', ''",
                "'()()', ''",
                "'---', ''",
                "'___', ''",
                "'.,;', ''",
                "'   &&&   ', ''"
        })
        @DisplayName("Should return empty string when input contains only separators")
        void shouldReturnEmptyStringWhenOnlySeparators(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n", " \t \n "})
        @DisplayName("Should return empty when blank")
        void shouldReturnEmptyWhenBlank(String input) {
            assertThat(RenderUtils.normalize(input)).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
                "' hello world ', 'hello-world'",
                "'  hello world  ', 'hello-world'",
                "'&hello world&', 'hello-world'",
                "'()hello world()', 'hello-world'",
                "'---hello world---', 'hello-world'",
                "'___hello world___', 'hello-world'",
                "'.,;hello world.,;', 'hello-world'"
        })
        @DisplayName("Should strip leading and trailing separators")
        void shouldStripLeadingAndTrailingSeparators(String input, String expected) {
            assertThat(RenderUtils.normalize(input)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Parse Properties String Tests")
    class ParsePropertiesStringTests {
        @Test
        @DisplayName("Should handle duplicate keys")
        void handleDuplicateKeys() {
            String input = "key1=value1\nkey1=value2";
            Properties result = RenderUtils.parsePropertiesString(input);

            assertThat(result).hasSize(1);
            assertThat(result).containsEntry("key1", "value2");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        @DisplayName("Should handle empty or null string input")
        void handleEmptyOrNullStringInput(String input) {
            Properties result = RenderUtils.parsePropertiesString(input);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle string with invalid format")
        void handleInvalidFormat() {
            var input = "keyWithoutValue\n=onlyValueNoKey\nkey=value\nkey2==value2";
            var result = RenderUtils.parsePropertiesString(input);

            assertThat(result).containsEntry("key", "value");
            assertThat(result).containsEntry("key2", "=value2");
            assertThat(result).containsEntry("keyWithoutValue", "");
            assertThat(result).containsEntry("", "onlyValueNoKey");
        }

        @Test
        @DisplayName("Should handle string with spaces")
        void handleStringWithSpaces() {
            var input = " key1 = value1 \n key2=value2";
            var result = RenderUtils.parsePropertiesString(input);

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("key1", "value1 ");
            assertThat(result).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("Should parse valid properties string")
        void parseValidPropertiesString() {
            var input = "key1=value1\nkey2=value2\nkey3=value3";
            var result = RenderUtils.parsePropertiesString(input);

            assertThat(result).hasSize(3);
            assertThat(result).containsEntry("key1", "value1");
            assertThat(result).containsEntry("key2", "value2");
            assertThat(result).containsEntry("key3", "value3");
        }
    }

    @Nested
    @DisplayName("QR Code Tests")
    class QrCodeTests {
        @Test
        void qrCode() {
            assertThat(RenderUtils.qrCode("erik", "24")).as("svg")
                    .startsWith("<?xml").contains("<svg").contains("<desc>erik");
        }

        @Test
        void qrCodeWithEmpty() {
            assertThat(RenderUtils.qrCode("", "12")).as("empty").isEmpty();
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
            var result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }


        @ParameterizedTest
        @ValueSource(strings = {
                "Hello World",
                "The quick brown fox",
                "UPPERCASE",
                "lowercase",
                "MiXeD cAsE",
                "Numbers 123 and symbols !@#",
                "Unicode café naïve",
                "Emoji 😀 test 🚀"
        })
        @DisplayName("ROT13 should be its own inverse (applying twice returns original)")
        void shouldBeItsOwnInverse(String original) {
            var encoded = RenderUtils.rot13(original);
            var decoded = RenderUtils.rot13(encoded);

            assertThat(decoded)
                    .as("ROT13 should be its own inverse for: %s", original)
                    .isEqualTo(original);
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
            var result = RenderUtils.rot13(input);

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
        @NotWindowsJdk17
        void shouldHandleMixedContent(String input, String expected) {
            var result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty input gracefully")
        void shouldHandleNullAndEmpty(String input) {
            var result = RenderUtils.rot13(input);

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

            try (var softly = new AutoCloseableSoftAssertions()) {
                for (int i = 0; i < asciiLetters.length; i++) {
                    var input = String.valueOf(asciiLetters[i]);
                    var result = RenderUtils.rot13(input);
                    var expected = String.valueOf(expectedRot13[i]);

                    softly.assertThat(result)
                            .as("ASCII letter %c should transform to %c", asciiLetters[i], expectedRot13[i])
                            .isEqualTo(expected);
                }
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "À", "Á", "Â", "Ã", "Ä", "Å", // Accented A
                "à", "á", "â", "ã", "ä", "å", // Accented a
                "Ç", "ç", "È", "É", "Ê", "Ë", // Other European
                "α", "β", "γ", "δ", "ε", "ζ", // Greek
                "А", "Б", "В", "Г", "Д", "Е", // Cyrillic
        })
        @DisplayName("Should not transform non-ASCII letters (accented, Greek, Cyrillic, etc.)")
        void shouldNotTransformNonAsciiLetters(String letter) {
            var result = RenderUtils.rot13(letter);
            assertThat(result)
                    .as("Non-ASCII letter %s should remain unchanged", letter)
                    .isEqualTo(letter);
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
            var result = RenderUtils.rot13(input);

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
            var result = RenderUtils.rot13(input);

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
            var result = RenderUtils.rot13(input);

            assertThat(result).isEqualTo(input);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Testing123",
                "abcdefg",
                "ABCDEFG",
                "HeLlO WoRlD",
                "Special !@# Chars",
                "Numbers 12345",
                "\t\nWhitespace",
                "😀 Emoji Test"
        })
        @DisplayName("Should preserve string length")
        void shouldPreserveStringLength(String input) {
            var result = RenderUtils.rot13(input);
            assertThat(result).hasSameSizeAs(input);
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
            var result = RenderUtils.rot13(input);

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
        @NotWindowsJdk17
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

    @Nested
    @DisplayName("Uptime Tests")
    class UptimeTests {
        private static final Properties PROPERTIES = new Properties();

        static {
            PROPERTIES.setProperty("year", " year ");
            PROPERTIES.setProperty("years", " years ");
            PROPERTIES.setProperty("month", " month ");
            PROPERTIES.setProperty("months", " months ");
            PROPERTIES.setProperty("week", " week ");
            PROPERTIES.setProperty("weeks", " weeks ");
            PROPERTIES.setProperty("day", " day ");
            PROPERTIES.setProperty("days", " days ");
            PROPERTIES.setProperty("hour", " hour ");
            PROPERTIES.setProperty("hours", " hours ");
            PROPERTIES.setProperty("minute", " minute");
            PROPERTIES.setProperty("minutes", " minutes");
        }

        @ParameterizedTest
        @DisplayName("Parametrized edge cases")
        @CsvSource({
                "0, '0 minutes'",
                "1000, '0 minutes'",
                "59000, '0 minutes'",
                "60000, '1 minute'",
                "120000, '2 minutes'",
                "3600000, '1 hour'",
                "3660000, '1 hour 1 minute'",
                "86400000, '1 day'",
                "90000000, '1 day 1 hour'"
        })
        void edgeCases(long uptimeMs, String expected) {
            assertThat(RenderUtils.uptime(uptimeMs, PROPERTIES))
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("Result should be trimmed")
        void resultShouldBeTrimmed() {
            // This test ensures the trim() at the end works correctly
            var propsWithSpaces = new Properties();
            propsWithSpaces.setProperty("minute", "  minute  ");

            var result = RenderUtils.uptime(0, propsWithSpaces);
            assertThat(result).isEqualTo("0 minutes");
            assertThat(result).doesNotStartWith(" ");
            assertThat(result).doesNotEndWith(" ");
        }

        @Nested
        @DisplayName("Basic time unit conversions")
        class BasicTimeUnits {
            @Test
            @DisplayName("Exactly one hour")
            void exactlyOneHour() {
                long oneHour = TimeUnit.HOURS.toMillis(1);
                assertThat(RenderUtils.uptime(oneHour, PROPERTIES)).isEqualTo("1 hour");
            }

            @Test
            @DisplayName("Exactly one minute")
            void exactlyOneMinute() {
                long oneMinute = TimeUnit.MINUTES.toMillis(1);
                assertThat(RenderUtils.uptime(oneMinute, PROPERTIES)).isEqualTo("1 minute");
            }

            @Test
            @DisplayName("Hours and minutes")
            void hoursAndMinutes() {
                long twoHours30Minutes = TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(30);
                assertThat(RenderUtils.uptime(twoHours30Minutes, PROPERTIES)).isEqualTo("2 hours 30 minutes");
            }

            @Test
            @DisplayName("Less than one minute should return '0 minutes'")
            void lessThanOneMinute() {
                long thirtySeconds = TimeUnit.SECONDS.toMillis(30);
                assertThat(RenderUtils.uptime(thirtySeconds, PROPERTIES)).isEqualTo("0 minutes");
            }

            @Test
            @DisplayName("Multiple hours")
            void multipleHours() {
                long threeHours = TimeUnit.HOURS.toMillis(3);
                assertThat(RenderUtils.uptime(threeHours, PROPERTIES)).isEqualTo("3 hours");
            }

            @Test
            @DisplayName("Multiple minutes")
            void multipleMinutes() {
                long fiveMinutes = TimeUnit.MINUTES.toMillis(5);
                assertThat(RenderUtils.uptime(fiveMinutes, PROPERTIES)).isEqualTo("5 minutes");
            }

            @Test
            @DisplayName("Zero uptime should return '0 minutes'")
            void zeroUptime() {
                assertThat(RenderUtils.uptime(0, PROPERTIES)).isEqualTo("0 minutes");
            }
        }

        @Nested
        @DisplayName("Boundary Tests")
        class BoundaryConditions {
            @Test
            @DisplayName("Days boundary")
            void daysBoundary() {
                assertThat(RenderUtils.uptime(86399999, PROPERTIES)).isEqualTo("23 hours 59 minutes");
                assertThat(RenderUtils.uptime(86400000, PROPERTIES)).isEqualTo("1 day");
            }

            @Test
            @DisplayName("Hours boundary")
            void hoursBoundary() {
                assertThat(RenderUtils.uptime(3599999, PROPERTIES)).isEqualTo("59 minutes");
                assertThat(RenderUtils.uptime(3600000, PROPERTIES)).isEqualTo("1 hour");
            }

            @Test
            @DisplayName("Milliseconds boundary")
            void millisecondsBoundary() {
                assertThat(RenderUtils.uptime(59999, PROPERTIES)).isEqualTo("0 minutes");
                assertThat(RenderUtils.uptime(60000, PROPERTIES)).isEqualTo("1 minute");
            }
        }

        @Nested
        @DisplayName("Custom properties")
        class CustomProperties {
            @Test
            @DisplayName("Custom singular and plural forms")
            void customPluralForms() {
                Properties customProps = new Properties();
                customProps.setProperty("hour", "h");
                customProps.setProperty("hours", "h");
                customProps.setProperty("minute", "m");
                customProps.setProperty("minutes", "m");

                long uptime = TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(30);
                assertThat(RenderUtils.uptime(uptime, customProps)).isEqualTo("2h30m");
            }

            @Test
            @DisplayName("Localized properties")
            void localizedProperties() {
                var spanishProps = new Properties();
                spanishProps.setProperty("year", " año ");
                spanishProps.setProperty("years", " años ");
                spanishProps.setProperty("month", " mes ");
                spanishProps.setProperty("months", " meses ");
                spanishProps.setProperty("week", " semana ");
                spanishProps.setProperty("weeks", " semanas ");
                spanishProps.setProperty("day", " día ");
                spanishProps.setProperty("days", " días ");
                spanishProps.setProperty("hour", " hora ");
                spanishProps.setProperty("hours", " horas ");
                spanishProps.setProperty("minute", " minuto");
                spanishProps.setProperty("minutes", " minutos");

                long uptime = TimeUnit.DAYS.toMillis(700) + TimeUnit.DAYS.toMillis(8)
                        + TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(1);
                assertThat(RenderUtils.uptime(uptime, spanishProps))
                        .isEqualTo("1 año 11 meses 1 semana 6 días 3 horas 1 minuto");
            }

            @Test
            @DisplayName("Missing properties should use defaults")
            void missingPropertiesUseDefaults() {
                Properties emptyProps = new Properties();
                long oneHour = TimeUnit.HOURS.toMillis(1);
                assertThat(RenderUtils.uptime(oneHour, emptyProps)).isEqualTo("1 hour");
            }
        }

        @Nested
        @DisplayName("Day-based calculations")
        class DayBasedCalculations {
            @Test
            @DisplayName("Weeks, days, hours, and minutes")
            void complexDayBasedUptime() {
                long uptime = TimeUnit.DAYS.toMillis(10) + // 1 week, 3 days
                        TimeUnit.HOURS.toMillis(5) +
                        TimeUnit.MINUTES.toMillis(30);
                assertThat(RenderUtils.uptime(uptime, PROPERTIES))
                        .isEqualTo("1 week 3 days 5 hours 30 minutes");
            }

            @Test
            @DisplayName("Exactly one day")
            void exactlyOneDay() {
                long oneDay = TimeUnit.DAYS.toMillis(1);
                assertThat(RenderUtils.uptime(oneDay, PROPERTIES)).isEqualTo("1 day");
            }

            @Test
            @DisplayName("One month (30 days)")
            void exactlyOneMonth() {
                long oneMonth = TimeUnit.DAYS.toMillis(30);
                assertThat(RenderUtils.uptime(oneMonth, PROPERTIES)).isEqualTo("1 month");
            }

            @Test
            @DisplayName("One week exactly")
            void exactlyOneWeek() {
                long oneWeek = TimeUnit.DAYS.toMillis(7);
                assertThat(RenderUtils.uptime(oneWeek, PROPERTIES)).isEqualTo("1 week");
            }

            @Test
            @DisplayName("Multiple days")
            void multipleDays() {
                long fiveDays = TimeUnit.DAYS.toMillis(5);
                assertThat(RenderUtils.uptime(fiveDays, PROPERTIES)).isEqualTo("5 days");
            }

            @Test
            @DisplayName("Multiple months")
            void multipleMonths() {
                long twoMonths = TimeUnit.DAYS.toMillis(60);
                assertThat(RenderUtils.uptime(twoMonths, PROPERTIES)).isEqualTo("2 months");
            }

            @Test
            @DisplayName("No minutes")
            void noMinutes() {
                long uptime = TimeUnit.SECONDS.toMillis(30);
                assertThat(RenderUtils.uptime(uptime, PROPERTIES)).isEqualTo("0 minutes");
            }
        }

        @Nested
        @DisplayName("Shorten URL Tests")
        class ShortenUrlTests {
            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Should handle null and empty URLs gracefully")
            void shouldHandleNullAndEmptyUrls(String url) {
                var result = RenderUtils.shortenUrl(url);

                assertThat(result).isEqualTo(url);
            }

            @Test
            @DisplayName("Should return original URL for invalid input")
            void shouldReturnOriginalForInvalidUrls() {
                var invalidUrl = "not-a-valid-url";
                var result = RenderUtils.shortenUrl(invalidUrl);

                assertThat(result).isEqualTo(invalidUrl);
            }

            @Test
            @DisplayName("Should shorten valid URL")
            void shouldShortenValidUrl() {
                var result = RenderUtils.shortenUrl("https://example.com");

                assertThat(result).isEqualTo("https://is.gd/jGamH3");
            }
        }

        @Nested
        @DisplayName("Year-based calculations")
        class YearBasedCalculations {
            @Test
            @DisplayName("Complex uptime with all units")
            void complexUptimeAllUnits() {
                long uptime = TimeUnit.DAYS.toMillis(400) + TimeUnit.MINUTES.toMillis(45);
                assertThat(RenderUtils.uptime(uptime, PROPERTIES))
                        .isEqualTo("1 year 1 month 5 days 45 minutes");
            }

            @Test
            @DisplayName("Exactly one year")
            void exactlyOneYear() {
                long oneYear = TimeUnit.DAYS.toMillis(365);
                assertThat(RenderUtils.uptime(oneYear, PROPERTIES)).isEqualTo("1 year");
            }

            @Test
            @DisplayName("Multiple years")
            void multipleYears() {
                long twoYears = TimeUnit.DAYS.toMillis(730);
                assertThat(RenderUtils.uptime(twoYears, PROPERTIES)).isEqualTo("2 years");
            }

            @Test
            @DisplayName("Very large uptime")
            void veryLargeUptime() {
                long uptime = TimeUnit.DAYS.toMillis(10839) + TimeUnit.HOURS.toMillis(1)
                        + TimeUnit.MINUTES.toMillis(5);
                assertThat(RenderUtils.uptime(uptime, PROPERTIES))
                        .isEqualTo("29 years 8 months 2 weeks 1 hour 5 minutes");
            }
        }
    }
}
