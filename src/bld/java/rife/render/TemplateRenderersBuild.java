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

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.ExecOperation;
import rife.bld.extension.JacocoReportOperation;
import rife.bld.extension.PmdOperation;
import rife.bld.extension.TestsBadgeOperation;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishInfo;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;

public class TemplateRenderersBuild extends Project {
    static final String TEST_RESULTS_DIR = "build/test-results/test/";
    private final TestsBadgeOperation testsBadgeOperation = new TestsBadgeOperation();

    public TemplateRenderersBuild() {
        pkg = "rife.render";
        name = "rife2-template-renderers";
        version = version(1, 3, 0, "SNAPSHOT");

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES, RIFE2_SNAPSHOTS);

        scope(compile)
                .include(dependency("com.uwyn.rife2", "rife2", version(1, 9, 1)));
        scope(test)
                .include(dependency("com.uwyn.rife2", "bld-extensions-testing-helpers",
                        version(0, 9, 0, "SNAPSHOT")))
                .include(dependency("org.junit.jupiter", "junit-jupiter",
                        version(5, 13, 4)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone",
                        version(1, 13, 4)))
                .include(dependency("org.assertj", "assertj-core",
                        version(3, 27, 4)));

        javadocOperation().javadocOptions()
                .docTitle("<a href=\"https://rife2.com\">RIFE2</a> Template Renderers")
                .author()
                .docLint(NO_MISSING)
                .link("https://rife2.github.io/rife2/");

        publishOperation()
                .repository(version.isSnapshot() ? CENTRAL_SNAPSHOTS
                        .withCredentials(property("sonatypeUser"), property("sonatypePassword"))
                        : CENTRAL_RELEASES
                        .withCredentials(property("sonatypeUser"), property("sonatypePassword")))
                .repository(version.isSnapshot() ? RIFE2_SNAPSHOTS
                        .withCredentials(property("rife2Username"), property("rife2Password"))
                        : RIFE2_RELEASES
                        .withCredentials(property("rife2Username"), property("rife2Password")))
                .info(new PublishInfo()
                        .groupId("com.uwyn.rife2")
                        .artifactId("rife2-renderers")
                        .name("RIFE2 Template Renderers")
                        .description("Template Renderers for the RIFE2 web framework")
                        .url("https://github.com/rife2/rife2-template-renderers")
                        .developer(new PublishDeveloper()
                                .id("ethauvin")
                                .name("Erik C. Thauvin")
                                .email("erik@thauvin.net")
                                .url("https://erik.thauvin.net/"))
                        .developer(new PublishDeveloper()
                                .id("gbevin")
                                .name("Geert Bevin")
                                .email("gbevin@uwyn.com")
                                .url("https://github.com/gbevin"))
                        .license(new PublishLicense()
                                .name("The Apache License, Version 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
                        .scm(new PublishScm()
                                .connection("scm:git:https://github.com/rife2/rife2-template-renderers.git")
                                .developerConnection("scm:git:git@github.com:rife2/rife2-template-renderers.git")
                                .url("https://github.com/rife2/rife2-template-renderers"))
                        .signKey(property("signKey"))
                        .signPassphrase(property("signPassphrase")));
    }

    public static void main(String[] args) {
        new TemplateRenderersBuild().start(args);
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws Exception {
        var op = new JacocoReportOperation().fromProject(this);
        op.testToolOptions("--reports-dir=" + TEST_RESULTS_DIR);

        Exception ex = null;
        try {
            op.execute();
        } catch (Exception e) {
            ex = e;
        }

        renderWithXunitViewer();

        if (ex != null) {
            throw ex;
        }
    }

    @BuildCommand(summary = "Runs PMD analysis")
    public void pmd() throws Exception {
        new PmdOperation()
                .fromProject(this)
                .failOnViolation(true)
                .ruleSets("config/pmd.xml")
                .execute();
    }

    private void renderWithXunitViewer() throws Exception {
        var xunitViewer = new File("/usr/bin/xunit-viewer");
        if (xunitViewer.exists() && xunitViewer.canExecute()) {
            var reportsDir = "build/reports/tests/test/";

            Files.createDirectories(Path.of(reportsDir));

            new ExecOperation()
                    .fromProject(this)
                    .command(xunitViewer.getPath(), "-r", TEST_RESULTS_DIR, "-o", reportsDir + "index.html")
                    .execute();
        }
    }

    @Override
    public void test() throws Exception {
        var op = testsBadgeOperation
                .url(property("testsBadgeUrl"))
                .apiKey(property("testsBadgeApiKey"))
                .fromProject(this);
        op.testToolOptions().reportsDir(new File(TEST_RESULTS_DIR));


        Exception ex = null;
        try {
            op.executeOnce();
        } catch (Exception e) {
            ex = e;
        }

        renderWithXunitViewer();

        if (ex != null) {
            throw ex;
        }
    }
}
