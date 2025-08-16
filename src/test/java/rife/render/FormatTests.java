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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import rife.bld.extension.testing.DisabledOnCi;
import rife.template.TemplateFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class FormatTests {
    @Test
    void abbreviate() {
        var t = TemplateFactory.TXT.get("abbreviate");
        t.setAttribute(CaseTests.FOO, CaseTests.SAMPLE_TEXT);
        assertThat(t.getContent()).endsWith("...").hasSize(8);
    }

    @Test
    void abbreviateHtml() {
        var t = TemplateFactory.HTML.get("abbreviate");
        t.setAttribute(CaseTests.FOO, CaseTests.SAMPLE_TEXT);
        assertThat(t.getContent()).hasSize(19);
    }

    @Test
    void mask() {
        var t = TemplateFactory.TXT.get("mask");
        var foo = "374380141731053";
        t.setAttribute(CaseTests.FOO, foo);
        assertThat(t.getContent()).isEqualTo("***************");
    }

    @Test
    void maskHtml() {
        var t = TemplateFactory.HTML.get("mask");
        var foo = "374380141731053";
        t.setAttribute(CaseTests.FOO, foo);
        assertThat(t.getContent())
                .isEqualTo("3743&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;");
    }

    @Test
    void normalize() {
        var t = TemplateFactory.HTML.get("normalize");
        var foo = "News for January 6, 2023 (Paris)";
        t.setValue(CaseTests.FOO, foo);
        assertThat(t.getContent())
                .isEqualTo("<a href=\"news/20230106/news-for-january-6-2023-paris\">" + foo + "</a>");
    }

    @Test
    @DisabledOnCi
    @Tag("no-ci")
    void qrCode() {
        var t = TemplateFactory.SVG.get("qrCode");
        var foo = "https://example.com/";
        t.setAttribute(CaseTests.FOO, foo);
        assertThat(t.getContent()).startsWith("<?xml").contains("<desc>" + foo + "</desc").contains("width=\"200\"");
    }

    @Test
    @DisabledOnCi
    @Tag("no-ci")
    void shortenUrl() {
        var t = TemplateFactory.HTML.get("shortenUrl");
        var url = "https://example.com/";
        var shortUrl = "https://is.gd/AG3Hwv";
        t.setValue(CaseTests.FOO, url);
        assertThat(t.getContent()).isEqualTo(String.format("<a href=\"%s\">%s</a>", shortUrl, url));
        t.setValue(CaseTests.FOO, CaseTests.FOO);
        assertThat(t.getContent()).isEqualTo("<a href=\"foo\">foo</a>");
    }

    @Nested
    @DisplayName("Credit Card Format Tests")
    class CreditCardFormatTests {
        @Test
        void amexCreditCard() {
            var t = TemplateFactory.TXT.get("formatCreditCard");
            t.setAttribute(CaseTests.FOO, "374380141731053");
            assertThat(t.getContent()).isEqualTo("1053");
        }

        @Test
        void creditCardWithEmpty() {
            var t = TemplateFactory.TXT.get("formatCreditCard");
            t.setAttribute(CaseTests.FOO, "");
            assertThat(t.getContent()).isEmpty();
        }

        @Test
        void discoverCreditCard() {
            var t = TemplateFactory.TXT.get("formatCreditCard");
            t.setAttribute(CaseTests.FOO, "6011 1076-8252 0629");
            assertThat(t.getContent()).isEqualTo("0629");
        }

        @Test
        void invalidCreditCard() {
            var t = TemplateFactory.TXT.get("formatCreditCard");
            t.setAttribute(CaseTests.FOO, "000000000000001");
            assertThat(t.getContent()).isEmpty();
        }

        @Test
        void mastercardCreditCard() {
            var t = TemplateFactory.TXT.get("formatCreditCard");
            t.setAttribute(CaseTests.FOO, "5130-3899-9169-8324");
            assertThat(t.getContent()).isEqualTo("8324");
        }

        @Test
        void visaCreditCard() {
            var t = TemplateFactory.TXT.get("formatCreditCard");
            t.setAttribute(CaseTests.FOO, "4342 2565 6244 0179");
            assertThat(t.getContent()).isEqualTo("0179");
        }
    }

    @Nested
    @DisplayName("Uptime Tests")
    class UptimeTests {
        @Test
        void uptime() {
            var t = TemplateFactory.TXT.get("uptime");
            assertThat(t.getContent()).isEqualTo("0 minutes 0 minutos 0 minutes");
        }

        @Test
        void uptimeInFrench() {
            var t = TemplateFactory.HTML.get("uptime");
            t.setAttribute(Uptime.class.getName(), 547800300076L);
            assertThat(t.getContent())
                    .isEqualTo("17 ann&eacute;es, 4 mois, 2 semaines, 1 jour, 6 heures, 45 minutes");
        }

        @Test
        void uptimeInJson() {
            var t = TemplateFactory.JSON.get("uptime");
            t.setAttribute(Uptime.class.getName(), 5999964460000L);
            assertThat(t.getContent()).isEqualTo("190 years 3 months 4 days 47 minutes");
        }

        @Test
        void uptimeInMinutes() {
            var t = TemplateFactory.HTML.get("uptime");
            t.setAttribute(Uptime.class.getName(), 120000L);
            assertThat(t.getContent()).isEqualTo("2 minutes");
        }

        @Test
        void uptimeInMonth() {
            var t = TemplateFactory.JSON.get("uptime");
            t.setAttribute(Uptime.class.getName(), 2592000000L);
            assertThat(t.getContent()).isEqualTo("1 month");
        }

        @Test
        void uptimeInWeeks() {
            var t = TemplateFactory.TXT.get("uptime");
            t.setAttribute(Uptime.class.getName(), 1209600000L);
            assertThat(t.getContent()).isEqualTo("2 weeks 2 semanas 2 weeks");
        }

        @Test
        void uptimeWithFormatting() {
            var t = TemplateFactory.TXT.get("uptime2");
            t.setAttribute(Uptime.class.getName(), 547800388076L);
            assertThat(t.getContent()).isEqualTo("17YRS-4MOS-2WKS-1D-6H-46M");
        }

        @Test
        void uptimeWithZero() {
            var t = TemplateFactory.JSON.get("uptime");
            t.setAttribute(Uptime.class.getName(), 0L);
            assertThat(t.getContent()).isEqualTo("0 minutes");
        }
    }
}
