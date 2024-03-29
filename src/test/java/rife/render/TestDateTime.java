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
import static org.assertj.core.api.Assertions.assertThatCode;

class TestDateTime {
    @Test
    void testBestTime() {
        var t = TemplateFactory.HTML.get("beatTime");
        assertThat(t.getContent()).matches("@\\d{3}");
    }

    @Test
    void testDateIso() {
        var t = TemplateFactory.HTML.get("dateIso");
        assertThatCode(() -> RenderUtils.ISO_8601_DATE_FORMATTER.parse(t.getContent())).doesNotThrowAnyException();
    }

    @Test
    void testDateTimeIso() {
        var t = TemplateFactory.HTML.get("dateTimeIso");
        assertThatCode(() -> RenderUtils.ISO_8601_FORMATTER.parse(t.getContent())).doesNotThrowAnyException();
    }

    @Test
    void testDateTimeRfc2822() {
        var t = TemplateFactory.HTML.get("dateTimeRfc2822");
        assertThatCode(() -> RenderUtils.RFC_2822_FORMATTER.parse(t.getContent())).doesNotThrowAnyException();
    }

    @Test
    void testDateTimeUtc() {
        var t = TemplateFactory.HTML.get("dateTimeUtc");
        var content = t.getContent();
        assertThatCode(() -> RenderUtils.ISO_8601_FORMATTER.parse(content)).doesNotThrowAnyException();
        assertThat(content).endsWith("Z");

    }

    @Test
    void testTimeIso() {
        var t = TemplateFactory.HTML.get("timeIso");
        assertThatCode(() -> RenderUtils.ISO_8601_TIME_FORMATTER.parse(t.getContent())).doesNotThrowAnyException();
    }

    @Test
    void testYear() {
        var t = TemplateFactory.HTML.get("year");
        var year = java.time.Year.now().toString();
        assertThat(t.getContent()).isEqualTo(year + "<br>" + year);
    }
}
