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
    public static final String FOO = "foo";

    @Test
    void testEncodeBase64() {
        var t = TemplateFactory.TXT.get("base64");
        t.setValue(FOO, "This is a test");
        assertThat(t.getContent()).isEqualTo(t.getValue(FOO) + ": VGhpcyBpcyBhIHRlc3Q=");
    }

    @Test
    void testEncodeHtml() {
        var t = TemplateFactory.HTML.get("encodeHtml");
        t.setAttribute(FOO, "<a test &>");
        assertThat(t.getContent()).isEqualTo("&lt;a test &amp;&gt;");
    }

    @Test
    void testEncodeRot13() {
        var t = TemplateFactory.TXT.get("rot13");
        var value = "This is a test";
        var rot13 = "Guvf vf n grfg";

        // Encode
        var bean = new ValueBean(value);
        t.setBean(bean);
        assertThat(t.getContent()).isEqualTo(bean.getValue() + ": " + rot13);

        // Decode
        t.setValue("value", rot13);
        assertThat(t.getContent()).isEqualTo(rot13 + ": " + value);
    }

    @Test
    void testEncodeUrl() {
        var t = TemplateFactory.HTML.get("encodeUrl");
        t.setAttribute(FOO, "a test &");
        assertThat(t.getContent()).isEqualTo("<a href=\"https://example.com/a%20test%20%26\">a test &amp;</a>");
    }
}