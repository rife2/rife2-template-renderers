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
        assertThat(t.getContent()).isEqualTo("<a href=\"mailto:&#106;&#111;&#104;&#110;&#64;&#100;&#111;&#101;&#46;&#99;&#111;&#109;\">Email</a>");
    }

    @Test
    void testEncodeJson() {
        var t = TemplateFactory.JSON.get("encodeJson");
        t.setAttribute(TestCase.FOO, "fde\fde\rjk\tos\\u218Foi");
        assertThat(t.getContent()).isEqualTo("{\n    \"foo\": \"fde\\fde\\rjk\\tos\\\\u218Foi\"\n}");
    }

    @Test
    void testEncodeQp() {
        var t = TemplateFactory.TXT.get("encodeQp");
        t.setAttribute(TestCase.FOO, TestCase.SAMPLE_TEXT + "\nAnd one more test for =A0.");
        assertThat(t.getContent()).isEqualTo("This=20is=20a=20test=2E=0AAnd=20one=20more=20test=20for=20=3DA0=2E");
    }

    @Test
    void testEncodeRot13() {
        var t = TemplateFactory.TXT.get("rot13");
        var value = TestCase.SAMPLE_TEXT;
        var rot13 = "Guvf vf n grfg.";

        // Encode
        var bean = new ValueBean(value);
        t.setBean(bean);
        assertThat(t.getContent()).isEqualTo(bean.getValue() + ": " + rot13);

        // Decode
        t.setValue("value", rot13);
        assertThat(t.getContent()).isEqualTo(rot13 + ": " + value);
    }

    @Test
    void testEncodeUnicode() {
        var t = TemplateFactory.TXT.get("encodeUnicode");
        t.setAttribute(TestCase.FOO, TestCase.SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo("\\u0054\\u0068\\u0069\\u0073\\u0020\\u0069\\u0073\\u0020\\u0061\\u0020\\u0074\\u0065\\u0073\\u0074\\u002E");
    }

    @Test
    void testEncodeUrl() {
        var t = TemplateFactory.HTML.get("encodeUrl");
        t.setAttribute(TestCase.FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<a href=\"https://example.com/a%20test%20%26\">a test &amp;</a>");
    }

    @Test
    void testEncodeXml() {
        var t = TemplateFactory.XML.get("encodeXml");
        t.setAttribute(TestCase.FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<test>\n    <foo>a test &amp;</foo>\n</test>");
    }
}