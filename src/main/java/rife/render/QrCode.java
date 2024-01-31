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

import rife.template.Template;
import rife.template.ValueRenderer;

/**
 * <p>Generates an SVG QR Code for a template value using <a href="https://goqr.me/">goQR.me</a>.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 *   &lt;!--v render:rife.render.QrCode:valueId/--&gt;
 *   {{v render:rife.render.QrCode:valueId/}}
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @see <a href="https://github.com/rife2/rife2-template-renderers/wiki/rife.render.QrCode">rife.render.QrCode</a>
 * @since 1.0
 */
public class QrCode implements ValueRenderer {
    /**
     * {@inheritDoc}
     */
    @Override
    public String render(Template template, String valueId, String differentiator) {
        var properties = RenderUtils.parsePropertiesString(template.getDefaultValue(valueId));
        var size = properties.getProperty("size", "150x150");
        return RenderUtils.qrCode(template.getValueOrAttribute(differentiator), size);
    }
}
