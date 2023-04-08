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

import org.junit.jupiter.api.Test;
import rife.template.TemplateFactory;

import static org.assertj.core.api.Assertions.assertThat;

class TestEncode {
    @Test
    void testEncodeBase64() {
        var t = TemplateFactory.TXT.get("encodeBase64");
        t.setValue(TestCase.FOO, TestCase.SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo(t.getValue(TestCase.FOO) + ": VGhpcyBpcyBhIHRlc3Qu");

        t = TemplateFactory.HTML.get("encodeBase64");
        t.setValue(TestCase.FOO, TestCase.SAMPLE_TEXT + " URL Encoded.");
        assertThat(t.getContent()).as("with URL encoding").contains("VGhpcyBpcyBhIHRlc3QuIFVSTCBFbmNvZGVkLg%3D%3D");
    }

    @Test
    void testEncodeHtml() {
        var t = TemplateFactory.HTML.get("encodeHtml");
        t.setAttribute(TestCase.FOO, "<a test &>");
        assertThat(t.getContent()).isEqualTo("&lt;a test &amp;&gt;");
    }

    @Test
    void testEncodeHtmlEntities() {
        var t = TemplateFactory.HTML.get("encodeHtmlEntities");
        t.setAttribute(TestCase.FOO, "john@doe.com");
        assertThat(t.getContent()).isEqualTo(
                "<a href=\"mailto:&#106;&#111;&#104;&#110;&#64;&#100;&#111;&#101;&#46;&#99;&#111;&#109;\">Email</a>");
    }

    @Test
    void testEncodeJs() {
        var t = TemplateFactory.TXT.get("encodeJs");
        t.setAttribute(TestCase.FOO, "'\"\\/");
        assertThat(t.getContent()).isEqualTo("\\'\\\"\\\\\\/");

        t = TemplateFactory.HTML.get("encodeJs");
        t.setAttribute(TestCase.FOO, '"' + TestCase.SAMPLE_TEXT + '"');
        assertThat(t.getContent()).as("with unicode")
                .isEqualTo("\\u005C\\u0022\\u0054\\u0068\\u0069\\u0073\\u0020\\u0069\\u0073\\u0020\\u0061\\u0020\\u0074\\u0065\\u0073\\u0074\\u002E\\u005C\\u0022");
    }

    @Test
    void testEncodeJson() {
        var t = TemplateFactory.JSON.get("encodeJson");
        t.setAttribute(TestCase.FOO, "This is a \"•test\"");
        assertThat(t.getContent()).isEqualTo("{\n    \"foo\": \"This is a \\\"\\u2022test\\\"\"\n}");

        t = TemplateFactory.HTML.get("encodeJson");
        t.setAttribute(TestCase.FOO, "\"<test>\"");
        assertThat(t.getContent()).as("with html").isEqualTo("\\&quot;&lt;test&gt;\\&quot;");
    }

    @Test
    void testEncodeRot13() {
        var t = TemplateFactory.TXT.get("rot13");
        var rot13 = "Guvf vf n grfg.";

        // Encode
        var bean = new ValueBean(TestCase.SAMPLE_TEXT);
        t.setBean(bean);
        assertThat(t.getContent()).as("encode").isEqualTo(bean.getValue() + ": " + rot13);

        // Decode
        t.setValue("value", rot13);
        assertThat(t.getContent()).as("decode").isEqualTo(rot13 + ": " + TestCase.SAMPLE_TEXT);
    }

    @Test
    void testEncodeUnicode() {
        var t = TemplateFactory.TXT.get("encodeUnicode");
        t.setAttribute(TestCase.FOO, TestCase.SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo(
                "\\u0054\\u0068\\u0069\\u0073\\u0020\\u0069\\u0073\\u0020\\u0061\\u0020\\u0074\\u0065\\u0073\\u0074\\u002E");

        t = TemplateFactory.HTML.get("encodeUnicode");
        t.setAttribute(TestCase.FOO, '"' + TestCase.SAMPLE_TEXT + '"');
        assertThat(t.getContent()).as("with js")
                .contains("'\\\\u0022\\\\u0054\\\\u0068\\\\u0069\\\\u0073\\\\u0020\\\\u0069\\\\u0073\\\\u0020\\\\u0061\\\\u0020\\\\u0074\\\\u0065\\\\u0073\\\\u0074\\\\u002E\\\\u0022'");
    }

    @Test
    void testEncodeUrl() {
        var t = TemplateFactory.HTML.get("encodeUrl");
        t.setAttribute(TestCase.FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<a href=\"https://example.com/a%20test%20%26\">a test &amp;</a>");

        t = TemplateFactory.HTML.get("encodeUrlwithUnicode");
        t.setAttribute(TestCase.FOO, "a=test");
        assertThat(t.getContent()).as("with unicode")
                .contains("https://foo.com/\\u0061\\u0025\\u0033\\u0044\\u0074\\u0065\\u0073\\u0074");
    }

    @Test
    void testEncodeXml() {
        var t = TemplateFactory.XML.get("encodeXml");
        t.setAttribute(TestCase.FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<test>\n    <foo>a test &amp;</foo>\n</test>");
    }
}