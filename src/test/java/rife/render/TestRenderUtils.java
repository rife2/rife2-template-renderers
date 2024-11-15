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
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class TestRenderUtils {
    static final String SAMPLE_GERMAN = "Möchten Sie ein paar Äpfel?";

    @Test
    void testAbbreviate() {
        assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 9, "")).as("max=9")
                .isEqualTo("This is a");

        assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 0, "")).as("max=0").isEmpty();

        assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, -1, "")).as("max=-1")
                .isEqualTo(TestCase.SAMPLE_TEXT);

        assertThat(RenderUtils.abbreviate("", 10, "")).as("").isEmpty();
    }

    @Test
    void testCapitalizeWords() {
        assertThat(RenderUtils.capitalizeWords("hello world")).isEqualTo("Hello World");
        assertThat(RenderUtils.capitalizeWords("java programming")).isEqualTo("Java Programming");
        assertThat(RenderUtils.capitalizeWords("TEST")).isEqualTo("Test");
        assertThat(RenderUtils.capitalizeWords("multiple   spaces")).isEqualTo("Multiple   Spaces");
        assertThat(RenderUtils.capitalizeWords("white\t\fspaces")).isEqualTo("White\t\fSpaces");
        assertThat(RenderUtils.capitalizeWords("")).isEmpty();
        assertThat(RenderUtils.capitalizeWords(null)).isNull();
    }

    @Test
    void testEncode() {
        var p = new Properties();
        p.put(RenderUtils.ENCODING_PROPERTY, "blah");
        assertThat(RenderUtils.encode(TestCase.SAMPLE_TEXT, p)).as("invalid encoding").isEqualTo(TestCase.SAMPLE_TEXT);
        p.put(RenderUtils.ENCODING_PROPERTY, "json");
        assertThat(RenderUtils.encode("This is a \"•test\"", p)).as("json").isEqualTo("This is a \\\"\\u2022test\\\"");
        p.put(RenderUtils.ENCODING_PROPERTY, "html");
        assertThat(RenderUtils.encode("<a test &>", p)).as("html").isEqualTo("&lt;a test &amp;&gt;");
        p.put(RenderUtils.ENCODING_PROPERTY, "js");
        assertThat(RenderUtils.encode("\"test'", p)).as("js").isEqualTo("\\\"test\\'");
        p.put(RenderUtils.ENCODING_PROPERTY, "unicode");
        assertThat(RenderUtils.encode("test", p)).as("unicode").isEqualTo("\\u0074\\u0065\\u0073\\u0074");
        p.put(RenderUtils.ENCODING_PROPERTY, "url");
        assertThat(RenderUtils.encode("a = test", p)).as("url").isEqualTo("a%20%3D%20test");
        p.put(RenderUtils.ENCODING_PROPERTY, "xml");
        assertThat(RenderUtils.encode("Joe's Café & Bar", p)).as("xml").isEqualTo("Joe&apos;s Café &amp; Bar");
    }

    @Test
    void testEncodeJs() {
        assertThat(RenderUtils.encodeJs("")).isEmpty();
    }

    @Test
    void testFetchUrl() {
        var s = "default";
        assertThat(RenderUtils.fetchUrl("blah", s)).isEqualTo(s);
        assertThat(RenderUtils.fetchUrl("https://www.google.com/404", s)).isEqualTo(s);
        assertThat(RenderUtils.fetchUrl("https://www.notreallythere.com/", s)).isEqualTo(s);
    }

    @Test
    void testHtmlEntities() {
        assertThat(RenderUtils.htmlEntities("")).isEmpty();
        assertThat(RenderUtils.htmlEntities(SAMPLE_GERMAN))
                .isEqualTo("&#77;&#246;&#99;&#104;&#116;&#101;&#110;&#32;&#83;&#105;&#101;&#32;&#101;&#105;&#110;&#32;&#112;&#97;&#97;&#114;&#32;&#196;&#112;&#102;&#101;&#108;&#63;");
    }

    @Test
    void testMask() {
        var foo = "4342256562440179";

        assertThat(RenderUtils.mask("", " ", 2, false)).isEmpty();

        assertThat(RenderUtils.mask(foo, "?", 4, false)).as("mask=?")
                .isEqualTo("????????????0179");

        assertThat(RenderUtils.mask(foo, "-", 22, true)).as("unmasked=22")
                .isEqualTo("----------------");

        assertThat(RenderUtils.mask(foo, "&bull;", -1, false)).as("mask=&bull;")
                .isEqualTo("&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;");
    }

    @Test
    void testNormalize() {
        assertThat(RenderUtils.normalize("")).as("empty").isEmpty();
        assertThat(RenderUtils.normalize(" &()-_=[{]}\\|;:,<.>/")).as("blank").isEmpty();
        assertThat(RenderUtils.normalize(SAMPLE_GERMAN)).as("greman").isEqualTo("mochten-sie-ein-paar-apfel");
        assertThat(RenderUtils.normalize("foo  bar, <foo-bar>,foo:bar,foo;(bar), {foo} & bar=foo.bar[foo|bar]"))
                .as("foo-bar")
                .isEqualTo("foo-bar-foo-bar-foo-bar-foo-bar-foo-bar-foo-bar-foo-bar");
        assertThat(RenderUtils.normalize("News for January 6, 2023 (Paris)")).as("docs example")
                .isEqualTo("news-for-january-6-2023-paris");
    }

    @Test
    void testQrCode() {
        assertThat(RenderUtils.qrCode("", "12")).isEmpty();
    }

    @Test
    void testRot13() {
        var encoded = "Zöpugra Fvr rva cnne Äcsry?";
        assertThat(RenderUtils.rot13("")).isEmpty();
        assertThat(RenderUtils.rot13(SAMPLE_GERMAN)).as("encode").isEqualTo(encoded);
        assertThat(RenderUtils.rot13(encoded)).as("decode").isEqualTo(SAMPLE_GERMAN);
    }

    @Test
    void testSwapCase() {
        assertThat(RenderUtils.swapCase("")).isEmpty();
        assertThat(RenderUtils.swapCase(SAMPLE_GERMAN)).isEqualTo("mÖCHTEN sIE EIN PAAR äPFEL?");
    }

    @Test
    void testValidateCreditCard() {
        try (var softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(RenderUtils.validateCreditCard("4505 4672 3366 6430")).as("visa").isTrue();
            softly.assertThat(RenderUtils.validateCreditCard("5189-5923-3915-0425")).as("mastercard").isTrue();
            softly.assertThat(RenderUtils.validateCreditCard("3433634926643302")).as("amex").isTrue();
            softly.assertThat(RenderUtils.validateCreditCard("6011 1076-8252 0629")).as("discover").isTrue();
            softly.assertThat(RenderUtils.validateCreditCard("0123456789012345")).as("invalid").isFalse();
        }
    }
}
