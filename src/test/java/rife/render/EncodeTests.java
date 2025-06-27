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

import org.junit.jupiter.api.Test;
import rife.template.TemplateFactory;

import static org.assertj.core.api.Assertions.assertThat;

class EncodeTests {
    @Test
    void decodeRot13() {
        var t = TemplateFactory.TXT.get("rot13");
        var rot13 = "Guvf vf n grfg.";

        t.setValue("value", rot13);
        assertThat(t.getContent()).isEqualTo(rot13 + ": " + CaseTests.SAMPLE_TEXT);
    }

    @Test
    void encodeBase64() {
        var t = TemplateFactory.TXT.get("encodeBase64");
        t.setValue(CaseTests.FOO, CaseTests.SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo(t.getValue(CaseTests.FOO) + ": VGhpcyBpcyBhIHRlc3Qu");
    }

    @Test
    void encodeBase64WithUrlEncoding() {
        var t = TemplateFactory.HTML.get("encodeBase64");
        t.setValue(CaseTests.FOO, CaseTests.SAMPLE_TEXT + " URL Encoded.");
        assertThat(t.getContent()).as("with URL encoding")
                .contains("VGhpcyBpcyBhIHRlc3QuIFVSTCBFbmNvZGVkLg%3D%3D");
    }

    @Test
    void encodeHtml() {
        var t = TemplateFactory.HTML.get("encodeHtml");
        t.setAttribute(CaseTests.FOO, "<a test &>");
        assertThat(t.getContent()).isEqualTo("&lt;a test &amp;&gt;");
    }

    @Test
    void encodeHtmlEntities() {
        var t = TemplateFactory.HTML.get("encodeHtmlEntities");
        t.setAttribute(CaseTests.FOO, "john@doe.com");
        assertThat(t.getContent()).isEqualTo(
                "<a href=\"mailto:&#106;&#111;&#104;&#110;&#64;&#100;&#111;&#101;&#46;&#99;&#111;&#109;\">Email</a>");
    }

    @Test
    void encodeJs() {
        var t = TemplateFactory.TXT.get("encodeJs");
        t.setAttribute(CaseTests.FOO, "'\"\\/");
        assertThat(t.getContent()).isEqualTo("\\'\\\"\\\\\\/");
    }

    @Test
    void encodeJsWithSpecialCharacters() {
        var t = TemplateFactory.TXT.get("encodeJs");
        t.setAttribute(CaseTests.FOO, "This is\f\b a\r\n\ttest");
        assertThat(t.getContent()).isEqualTo("This is\\f\\b a\\r\\n\\ttest");
    }

    @Test
    void encodeJsWithUnicode() {
        var t = TemplateFactory.HTML.get("encodeJs");
        t.setAttribute(CaseTests.FOO, '"' + CaseTests.SAMPLE_TEXT + '"');
        assertThat(t.getContent()).isEqualTo(
                "\\u005C\\u0022\\u0054\\u0068\\u0069\\u0073\\u0020\\u0069\\u0073\\u0020\\u0061\\u0020\\u0074\\u0065\\u0073\\u0074\\u002E\\u005C\\u0022");
    }

    @Test
    void encodeJson() {
        var t = TemplateFactory.JSON.get("encodeJson");
        t.setAttribute(CaseTests.FOO, "This is a \"•test\"");
        assertThat(t.getContent()).isEqualTo("{\n    \"foo\": \"This is a \\\"\\u2022test\\\"\"\n}");
    }

    @Test
    void encodeJsonWithHtml() {
        var t = TemplateFactory.HTML.get("encodeJson");
        t.setAttribute(CaseTests.FOO, "\"<test>\"");
        assertThat(t.getContent()).isEqualTo("\\&quot;&lt;test&gt;\\&quot;");
    }

    @Test
    void encodeRot13() {
        var t = TemplateFactory.TXT.get("rot13");
        var rot13 = "Guvf vf n grfg.";

        var bean = new ValueBean(CaseTests.SAMPLE_TEXT);
        t.setBean(bean);
        assertThat(t.getContent()).isEqualTo(bean.getValue() + ": " + rot13);
    }

    @Test
    void encodeUnicode() {
        var t = TemplateFactory.TXT.get("encodeUnicode");
        t.setAttribute(CaseTests.FOO, CaseTests.SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo(
                "\\u0054\\u0068\\u0069\\u0073\\u0020\\u0069\\u0073\\u0020\\u0061\\u0020\\u0074\\u0065\\u0073\\u0074\\u002E");
    }

    @Test
    void encodeUnicodeWithJs() {
        var t = TemplateFactory.HTML.get("encodeUnicode");
        t.setAttribute(CaseTests.FOO, '"' + CaseTests.SAMPLE_TEXT + '"');
        assertThat(t.getContent()).contains(
                "'\\\\u0022\\\\u0054\\\\u0068\\\\u0069\\\\u0073\\\\u0020\\\\u0069\\\\u0073\\\\u0020\\\\u0061\\\\u0020\\\\u0074\\\\u0065\\\\u0073\\\\u0074\\\\u002E\\\\u0022'");
    }

    @Test
    void encodeUrl() {
        var t = TemplateFactory.HTML.get("encodeUrl");
        t.setAttribute(CaseTests.FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<a href=\"https://example.com/a%20test%20%26\">a test &amp;</a>");
    }

    @Test
    void encodeUrlWithUnicode() {
        var t = TemplateFactory.HTML.get("encodeUrlwithUnicode");
        t.setAttribute(CaseTests.FOO, "a=test");
        assertThat(t.getContent()).contains("https://foo.com/\\u0061\\u0025\\u0033\\u0044\\u0074\\u0065\\u0073\\u0074");
    }

    @Test
    void encodeXml() {
        var t = TemplateFactory.XML.get("encodeXml");
        t.setAttribute(CaseTests.FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<test>\n    <foo>a test &amp;</foo>\n</test>");
    }
}