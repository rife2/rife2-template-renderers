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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class TestRenderUtils {
    static final String SAMPLE_GERMAN = "Möchten Sie ein paar Äpfel?";

    @Test
    void htmlEntities() {
        assertThat(RenderUtils.htmlEntities(SAMPLE_GERMAN)).isEqualTo(
                "&#77;&#246;&#99;&#104;&#116;&#101;&#110;&#32;&#83;&#105;&#101;&#32;&#101;&#105;&#110;&#32;&#112;&#97;&#97;&#114;&#32;&#196;&#112;&#102;&#101;&#108;&#63;");
    }

    @Test
    void htmlEntitiesWithEmpty() {
        assertThat(RenderUtils.htmlEntities("")).isEmpty();
    }

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
    @DisplayName("Capitalize Tests")
    class CapitalizeTests {
        @Test
        void capitalizeWords() {
            assertThat(RenderUtils.capitalizeWords("hello world")).isEqualTo("Hello World");
        }

        @Test
        void capitalizeWordsWithEmpty() {
            assertThat(RenderUtils.capitalizeWords("")).isEmpty();
        }

        @Test
        void capitalizeWordsWithMultipleSpaces() {
            assertThat(RenderUtils.capitalizeWords("multiple   spaces")).isEqualTo("Multiple   Spaces");
        }

        @Test
        void capitalizeWordsWithNull() {
            assertThat(RenderUtils.capitalizeWords(null)).isNull();
        }

        @Test
        void capitalizeWordsWithSpecialCharacters() {
            assertThat(RenderUtils.capitalizeWords("white\t\fspaces")).isEqualTo("White\t\fSpaces");
        }

        @Test
        void capitalizeWordsWithUppercase() {
            assertThat(RenderUtils.capitalizeWords("HELLO World")).isEqualTo("Hello World");
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
            private static Stream<Arguments> javascriptEscapeTestCases() {
                return Stream.of(
                        Arguments.of("test's", "test\\'s"),
                        Arguments.of("test\"s", "test\\\"s"),
                        Arguments.of("test\\s", "test\\\\s"),
                        Arguments.of("test/s", "test\\/s"),
                        Arguments.of("test\bs", "test\\bs"),
                        Arguments.of("test\ns", "test\\ns"),
                        Arguments.of("test\ts", "test\\ts"),
                        Arguments.of("test\fs", "test\\fs"),
                        Arguments.of("test\rs", "test\\rs"),
                        Arguments.of("a'b\"c\\d/e\bf\ng\th\fi\rj", "a\\'b\\\"c\\\\d\\/e\\bf\\ng\\th\\fi\\rj")
                );
            }

            @Test
            void encodeJsWithAllSpecialChars() {
                var input = "'\"\\/\b\n\t\f\r";
                var expected = "\\'\\\"\\\\\\/\\b\\n\\t\\f\\r";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            void encodeJsWithBackslash() {
                assertThat(RenderUtils.encodeJs("This is a test \\string\\"))
                        .isEqualTo("This is a test \\\\string\\\\");
            }

            @Test
            void encodeJsWithBackspace() {
                assertThat(RenderUtils.encodeJs("abc\bdef")).isEqualTo("abc\\bdef");
            }

            @Test
            void encodeJsWithBlankInput() {
                assertThat(RenderUtils.encodeJs("   ")).isEqualTo("   ");
            }

            @Test
            void encodeJsWithCarriageReturn() {
                assertThat(RenderUtils.encodeJs("abc\rdef")).isEqualTo("abc\\rdef");
            }

            @Test
            void encodeJsWithConsecutiveSpecialChars() {
                assertThat(RenderUtils.encodeJs("''\"\"\\\\")).isEqualTo("\\'\\'\\\"\\\"\\\\\\\\");
            }

            @Test
            void encodeJsWithDoubleQuote() {
                assertThat(RenderUtils.encodeJs("This is a test \"string\""))
                        .isEqualTo("This is a test \\\"string\\\"");
            }

            @Test
            void encodeJsWithEmptyInput() {
                assertThat(RenderUtils.encodeJs("")).isEqualTo("");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "  ", "\t", "\n"})
            void encodeJsWithEmptyOrBlankInputs(String input) {
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(input);
            }

            @Test
            void encodeJsWithFormFeed() {
                assertThat(RenderUtils.encodeJs("abc\fdef")).isEqualTo("abc\\fdef");
            }

            @Test
            void encodeJsWithForwardSlash() {
                assertThat(RenderUtils.encodeJs("This is a test /string/"))
                        .isEqualTo("This is a test \\/string\\/");
            }

            @Test
            void encodeJsWithMixedChars() {
                var input = "Hello 'World' and \"JavaScript\" with \\slashes/ and \nnewlines.";
                var expected = "Hello \\'World\\' and \\\"JavaScript\\\" with \\\\slashes\\/ and \\nnewlines.";
                assertThat(RenderUtils.encodeJs(input)).isEqualTo(expected);
            }

            @Test
            void encodeJsWithNewline() {
                assertThat(RenderUtils.encodeJs("abc\ndef")).isEqualTo("abc\\ndef");
            }

            @Test
            void encodeJsWithNoSpecialChars() {
                var input = "Hello World 123!";
                assertThat(input).isEqualTo(RenderUtils.encodeJs(input));
            }

            @Test
            void encodeJsWithNullInput() {
                assertThat(RenderUtils.encodeJs(null)).isNull();
            }

            @Test
            void encodeJsWithSingleQuote() {
                assertThat(RenderUtils.encodeJs("This is a test 'string'"))
                        .isEqualTo("This is a test \\'string\\'");
            }

            @Test
            void encodeJsWithSpecialCharsAtStartAndEnd() {
                assertThat(RenderUtils.encodeJs("'test'")).isEqualTo("\\'test\\'");
                assertThat(RenderUtils.encodeJs("\"test\"")).isEqualTo("\\\"test\\\"");
                assertThat(RenderUtils.encodeJs("\\test\\")).isEqualTo("\\\\test\\\\");
            }

            @Test
            void encodeJsWithTab() {
                assertThat(RenderUtils.encodeJs("abc\tdef")).isEqualTo("abc\\tdef");
            }

            @ParameterizedTest
            @MethodSource("javascriptEscapeTestCases")
            void encodeJsWithVariousSpecialCharsParameterized(String input, String expected) {
                assertThat(RenderUtils.encodeJs(input)).as("encodeJs(%s,%s)", input, expected).isEqualTo(expected);
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
                    .contains("\"foo\": \"bar\"");
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
    @DisplayName("ROT13 Test")
    class Rot13Test {
        private static final String ENCODED = "Zöpugra Fvr rva cnne Äcsry?";

        @Test
        void rot13Decode() {
            assertThat(RenderUtils.rot13(ENCODED)).as("decode").isEqualTo(SAMPLE_GERMAN);
        }

        @Test
        void rot13Encode() {
            assertThat(RenderUtils.rot13(SAMPLE_GERMAN)).as("encode").isEqualTo(ENCODED);
        }

        @Test
        void rot13WithEmpty() {
            assertThat(RenderUtils.rot13("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validate Credit Card")
    class ValidateCreditCard {
        @Test
        void amexCreditCard() {
            assertThat(RenderUtils.validateCreditCard("3433634926643302")).as("amex").isTrue();
        }

        @Test
        void discoverCreditCard() {
            assertThat(RenderUtils.validateCreditCard("6011 1076-8252 0629")).as("discover").isTrue();
        }

        @Test
        void invalidCreditCard() {
            assertThat(RenderUtils.validateCreditCard("0123456789012345")).as("invalid").isFalse();
        }

        @Test
        void mastercardCreditCard() {
            assertThat(RenderUtils.validateCreditCard("5189-5923-3915-0425")).as("mastercard").isTrue();
        }

        @Test
        void visaCreditCard() {
            assertThat(RenderUtils.validateCreditCard("4505 4672 3366 6430")).as("visa").isTrue();
        }
    }
}
