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

class TestFormat {
    @Test
    void testFormatCreditCard() {
        var t = TemplateFactory.TXT.get("formatCreditCard");
        t.setAttribute(TestCase.FOO, "4342 2565 6244 0179");
        assertThat(t.getContent()).as("US VISA").isEqualTo("0179");
        t.setAttribute(TestCase.FOO, "5130-3899-9169-8324");
        assertThat(t.getContent()).as("FR MASTERCARD").isEqualTo("8324");
        t.setAttribute(TestCase.FOO, "374380141731053");
        assertThat(t.getContent()).as("UK AMEX").isEqualTo("1053");
        t.setAttribute(TestCase.FOO, "000000000000001");
        assertThat(t.getContent()).isEmpty();
    }

    @Test
    void testShortenUrl() {
        var t = TemplateFactory.HTML.get("shortenUrl");
        var url = "https://example.com/";
        var shortUrl = "https://is.gd/AG3Hwv";
        t.setValue(TestCase.FOO, url);
        assertThat(t.getContent()).isEqualTo(String.format("<a href=\"%s\">%s</a>", shortUrl, url));
        t.setValue(TestCase.FOO, TestCase.FOO);
        assertThat(t.getContent()).isEqualTo("<a href=\"foo\">foo</a>");

    }
}
