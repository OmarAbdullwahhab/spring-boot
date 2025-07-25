/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.testsupport.gradle.testkit;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * An {@link Extension} for managing the lifecycle of a {@link GradleBuild} stored in a
 * field named {@code gradleBuild}.
 *
 * @author Andy Wilkinson
 * @author Scott Frederick
 */
public class GradleBuildExtension implements BeforeEachCallback, AfterEachCallback {

	private static final Pattern GRADLE_VERSION_PATTERN = Pattern.compile("\\[Gradle .+]");

	private final Dsl dsl = Dsl.GROOVY;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		GradleBuild gradleBuild = extractGradleBuild(context);
		gradleBuild.scriptProperty("parentRootDir", findParentRootDir().getAbsolutePath());
		URL scriptUrl = findDefaultScript(context);
		if (scriptUrl != null) {
			gradleBuild.script(scriptUrl.getFile());
		}
		URL settingsUrl = getSettings(context);
		if (settingsUrl != null) {
			gradleBuild.settings(settingsUrl.getFile());
		}
		gradleBuild.before();
	}

	private File findParentRootDir() {
		File dir = new File("").getAbsoluteFile();
		int depth = 0;
		while (dir != null && !hasGradleBuildFiles(dir)) {
			Assert.state(depth++ < 5, "Unable to find parent root");
			dir = dir.getParentFile();
		}
		Assert.state(dir != null, "Unable to find parent root");
		return dir;
	}

	private boolean hasGradleBuildFiles(File dir) {
		return new File(dir, "settings.gradle").exists() && new File(dir, "build.gradle").exists()
				&& new File(dir, "gradle.properties").exists();
	}

	private GradleBuild extractGradleBuild(ExtensionContext context) throws Exception {
		Object testInstance = context.getRequiredTestInstance();
		Field gradleBuildField = ReflectionUtils.findField(testInstance.getClass(), "gradleBuild");
		gradleBuildField.setAccessible(true);
		GradleBuild gradleBuild = (GradleBuild) gradleBuildField.get(testInstance);
		return gradleBuild;
	}

	private URL findDefaultScript(ExtensionContext context) {
		URL scriptUrl = getScriptForTestMethod(context);
		if (scriptUrl != null) {
			return scriptUrl;
		}
		return getScriptForTestClass(context.getRequiredTestClass());
	}

	private URL getScriptForTestMethod(ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		String name = testClass.getSimpleName() + "-" + removeGradleVersion(context.getRequiredTestMethod().getName())
				+ this.dsl.getExtension();
		return testClass.getResource(name);
	}

	private String removeGradleVersion(String methodName) {
		return GRADLE_VERSION_PATTERN.matcher(methodName).replaceAll("").trim();
	}

	private URL getScriptForTestClass(Class<?> testClass) {
		return testClass.getResource(testClass.getSimpleName() + this.dsl.getExtension());
	}

	private URL getSettings(ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		return testClass.getResource("settings.gradle");
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		extractGradleBuild(context).after();
	}

}
