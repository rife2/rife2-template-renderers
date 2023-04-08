package rife.render;

import rife.bld.Project;
import rife.bld.extension.TestsBadgeOperation;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishInfo;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.*;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;

public class TemplateRenderersBuild extends Project {
    private final TestsBadgeOperation testsBadgeOperation = new TestsBadgeOperation();

    public TemplateRenderersBuild() {
        pkg = "rife.render";
        name = "rife2-template-renderers";
        version = version(1, 1, 1);

        javadocOperation().javadocOptions()
                .docTitle("<a href=\"https://rife2.com\">RIFE2</a> Template Renderers")
                .docLint(NO_MISSING)
                .link("https://rife2.github.io/rife2/");

        publishOperation()
                .repository(version.isSnapshot() ? repository("https://repo.rife2.com/snapshots")
                        .withCredentials(property("rife2Username"), property("rife2Password"))
                        : repository("https://repo.rife2.com/releases")
                        .withCredentials(property("rife2Username"), property("rife2Password")))
                .repository(MAVEN_CENTRAL)
                .info(new PublishInfo()
                        .groupId("com.uwyn.rife2")
                        .artifactId("rife2-renderers")
                        .name("RIFE2 Template Renderers")
                        .description("Template Renderers for the RIFE2 web framework")
                        .url("https://github.com/rife2/rife2-template-renderers")
                        .developer(new PublishDeveloper().id("ethauvin").name("Erik C. Thauvin").email("erik@thauvin.net")
                                .url("https://erik.thauvin.net/"))
                        .developer(new PublishDeveloper().id("gbevin").name("Geert Bevin").email("gbevin@uwyn.com")
                                .url("https://github.com/gbevin"))
                        .license(new PublishLicense().name("The Apache License, Version 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
                        .scm(new PublishScm().connection("scm:git:https://github.com/rife2/rife2-template-renderers.git")
                                .developerConnection("scm:git:git@github.com:rife2/rife2-template-renderers.git")
                                .url("https://github.com/rife2/rife2-template-renderers"))
                        .signKey(property("SIGN_KEY"))
                        .signPassphrase(property("SIGN_PASSPHRASE")));

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES, RIFE2_SNAPSHOTS);

        scope(compile)
                .include(dependency("com.uwyn.rife2", "rife2", version(1, 5, 18)));
        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 9, 2)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 9, 2)))
                .include(dependency("org.assertj:assertj-core:3.24.2"));
    }

    public static void main(String[] args) {
        new TemplateRenderersBuild().start(args);
    }

    public void test() throws Exception {
        testsBadgeOperation.executeOnce(() -> testsBadgeOperation
                .url(property("testsBadgeUrl"))
                .apiKey(property("testsBadgeApiKey"))
                .fromProject(this));
    }

    @Override
    public void precompile() throws Exception {
        // TODO remove when fixed in bld
    }
}