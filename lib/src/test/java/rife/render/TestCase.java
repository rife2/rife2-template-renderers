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

class TestCase {
    @Test
    void testCapitalize() {
        var t = TemplateFactory.TXT.get("capitalize");
        t.setAttribute("foo", "this is a test");
        assertThat(t.getContent()).isEqualTo("This is a test");
    }

    @Test
    void testLowercase() {
        var t = TemplateFactory.TXT.get("lowercase");
        var bean = new ValueBean("this IS a TEST");
        t.setBean(bean);
        assertThat(t.getContent()).isEqualTo(bean.getValue() + ": this is a test");
    }
}