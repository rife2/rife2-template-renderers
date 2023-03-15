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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import rife.template.TemplateFactory;

import static org.assertj.core.api.Assertions.assertThat;
class TestEncode {
    @Test
    void testEncodeUrl() {
        var t = TemplateFactory.HTML.get("encodeUrl");
        t.setAttribute("foo", "a test &");
        var encodedValue = "a%20test%20%26";
        assertThat(t.getContent()).isEqualTo(encodedValue + "<br>\n" + encodedValue);
    }
}
