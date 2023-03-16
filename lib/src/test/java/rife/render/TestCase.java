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
import rife.tools.Localization;

import static org.assertj.core.api.Assertions.assertThat;

class TestCase {
    static final String FOO = "foo";
    static final String SAMPLE_TEXT = "This is a test.";

    @Test
    void testCapitalize() {
        var t = TemplateFactory.TXT.get("capitalize");
        t.setAttribute(FOO, SAMPLE_TEXT.toLowerCase(Localization.getLocale()));
        assertThat(t.getContent()).isEqualTo(SAMPLE_TEXT);
    }

    @Test
    void testLowercase() {
        var t = TemplateFactory.TXT.get("lowercase");
        var bean = new ValueBean("this IS a TEST.");
        t.setBean(bean);
        assertThat(t.getContent()).isEqualTo(bean.getValue() + ": this is a test.");
    }

    @Test
    void testSwapCase() {
        var t = TemplateFactory.TXT.get("swapCase");
        t.setAttribute(FOO, "tHiS iS a TeSt");
        assertThat(t.getContent()).isEqualTo("ThIs Is A tEsT");
    }

    @Test
    void testTrim() {
        var t = TemplateFactory.TXT.get("trim");
        t.setAttribute(FOO, "\t" + SAMPLE_TEXT + " \n");
        assertThat(t.getContent()).isEqualTo(SAMPLE_TEXT);
    }

    @Test
    void testUncapitalize() {
        var t = TemplateFactory.TXT.get("uncapitalize");
        t.setAttribute(FOO, SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo(SAMPLE_TEXT.toLowerCase(Localization.getLocale()));
    }

    @Test
    void testUppercase() {
        var t = TemplateFactory.TXT.get("uppercase");
        t.setAttribute("bar", SAMPLE_TEXT);
        assertThat(t.getContent()).isEqualTo(SAMPLE_TEXT.toUpperCase(Localization.getLocale()));
    }
}