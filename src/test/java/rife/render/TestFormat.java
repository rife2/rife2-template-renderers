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

class TestFormat {
    @Test
    void testAbbreviate() {
        var t = TemplateFactory.HTML.get("abbreviate");
        t.setAttribute(TestCase.FOO, TestCase.SAMPLE_TEXT);
        assertThat(t.getContent()).as("activate.html").endsWith("&hellip;").hasSize(19);

        t = TemplateFactory.TXT.get("abbreviate");
        t.setAttribute(TestCase.FOO, TestCase.SAMPLE_TEXT);
        assertThat(t.getContent()).as("activate.txt").endsWith("...").hasSize(8);
    }

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
        assertThat(t.getContent()).as("000000000000001").isEmpty();
        t.setAttribute(TestCase.FOO, "");
        assertThat(t.getContent()).as("").isEmpty();
    }

    @Test
    void testMask() {
        var t = TemplateFactory.HTML.get("mask");
        var foo = "374380141731053";
        t.setAttribute(TestCase.FOO, foo);
        assertThat(t.getContent()).as("mask.html")
                .isEqualTo("3743&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;");

        t = TemplateFactory.TXT.get("mask");
        t.setAttribute(TestCase.FOO, foo);
        assertThat(t.getContent()).as("mask.txt").isEqualTo("***************");
    }

    @Test
    void testNormalize() {
        var t = TemplateFactory.HTML.get("normalize");
        var foo = "News for January 6, 2023 (Paris)";
        t.setValue(TestCase.FOO, foo);
        assertThat(t.getContent()).isEqualTo("<a href=\"news/20230106/news-for-january-6-2023-paris\">"
                + foo + "</a>");
    }

    @Test
    @DisabledOnCi
    void testQrCode() {
        var t = TemplateFactory.SVG.get("qrCode");
        var foo = "https://example.com/";
        t.setAttribute(TestCase.FOO, foo);
        assertThat(t.getContent()).startsWith("<?xml").contains("<desc>" + foo + "</desc").contains("width=\"200\"");
    }

    @Test
    @DisabledOnCi
    void testShortenUrl() {
        var t = TemplateFactory.HTML.get("shortenUrl");
        var url = "https://example.com/";
        var shortUrl = "https://is.gd/AG3Hwv";
        t.setValue(TestCase.FOO, url);
        assertThat(t.getContent()).isEqualTo(String.format("<a href=\"%s\">%s</a>", shortUrl, url));
        t.setValue(TestCase.FOO, TestCase.FOO);
        assertThat(t.getContent()).isEqualTo("<a href=\"foo\">foo</a>");
    }

    @Test
    void testUptime() {
        var t = TemplateFactory.TXT.get("uptime");
        assertThat(t.getContent()).as("uptime.txt").isEqualTo("0 minute\n0 minuto\n0 minute");

        t = TemplateFactory.HTML.get("uptime");
        t.setAttribute(Uptime.class.getName(), 547800300076L);
        assertThat(t.getContent()).as("uptime.html")
                .isEqualTo("17 ann&eacute;es, 4 mois, 2 semaines, 1 jour, 6 heures, 45 minutes");
        t.setAttribute(Uptime.class.getName(), 120000L);
        assertThat(t.getContent()).as("uptime.html: 2 min").isEqualTo("2 minutes");

        t = TemplateFactory.JSON.get("uptime");
        t.setAttribute(Uptime.class.getName(), 5999964460000L);
        assertThat(t.getContent()).as("uptime.json")
                .isEqualTo("190 years 3 months 4 days 47 minutes");
        t.setAttribute(Uptime.class.getName(), 34822860000L);
        assertThat(t.getContent()).as("uptime.json: 1 year...")
                .isEqualTo("1 year 1 month 1 week 1 day 1 hour 1 minute");

        t = TemplateFactory.TXT.get("uptime2");
        t.setAttribute(Uptime.class.getName(), 547800388076L);
        assertThat(t.getContent()).as("uptime2.txt").isEqualTo("17YRS-4MOS-2WKS-1D-6H-46M");
    }
}
