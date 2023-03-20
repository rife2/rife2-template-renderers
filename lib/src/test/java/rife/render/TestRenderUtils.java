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

import static org.assertj.core.api.Assertions.assertThat;

class TestRenderUtils {
    static final String SAMPLE_GERMAN = "Möchten Sie ein paar Äpfel?";

    @Test
    void testAbbreviate() {
        assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 9, "")).as("max=9")
                .isEqualTo("This is a");

        assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, 0, "")).as("max=0").isEmpty();

        assertThat(RenderUtils.abbreviate(TestCase.SAMPLE_TEXT, -1, "")).as("max=-1")
                .isEqualTo(TestCase.SAMPLE_TEXT);
    }

    @Test
    void testMask() {
        var foo = "4342256562440179";

        assertThat(RenderUtils.mask(foo, "?", 4, false)).as("mask=?")
                .isEqualTo("????????????0179");

        assertThat(RenderUtils.mask(foo, "-", 22, true)).as("unmasked=22")
                .isEqualTo("----------------");

        assertThat(RenderUtils.mask(foo, "&bull;", -1, false)).as("mask=&bull;")
                .isEqualTo("&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;");
    }

    @Test
    void testNormalize() {
        assertThat(RenderUtils.normalize(SAMPLE_GERMAN)).isEqualTo("mochten-sie-ein-paar-apfel");
    }

    @Test
    void testRot13() {
        var encoded = "Zöpugra Fvr rva cnne Äcsry?";
        assertThat(RenderUtils.rot13(SAMPLE_GERMAN)).as("encode").isEqualTo(encoded);
        assertThat(RenderUtils.rot13(encoded)).as("decode").isEqualTo(SAMPLE_GERMAN);
    }
}
