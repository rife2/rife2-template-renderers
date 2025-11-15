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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Disables tests on CI condition.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class NotWindowsJdk17Condition implements ExecutionCondition {
    private static final String JAVA_VERSION = System.getProperty("java.version");
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (isWindowsJdk17()) {
            return ConditionEvaluationResult.disabled("Test disabled on Windows JDK 17");
        } else {
            return ConditionEvaluationResult.enabled("Test enabled on Windows JDK 17");
        }
    }

    static boolean isWindowsJdk17() {
        return (OS_NAME.contains("windows") && JAVA_VERSION.startsWith("17"));
    }
}
