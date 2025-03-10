/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.artifacts.configurations.ConfigurationRoles;
import org.gradle.api.internal.artifacts.configurations.RoleBasedConfigurationContainerInternal;
import org.gradle.api.plugins.internal.JavaPluginHelper;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;
import org.gradle.api.tasks.SourceSet;
import org.gradle.jvm.component.internal.JvmSoftwareComponentInternal;

import javax.inject.Inject;

/**
 * <p>A {@link Plugin} which extends the capabilities of the {@link JavaPlugin Java plugin} by cleanly separating
 * the API and implementation dependencies of a library.</p>
 *
 * @since 3.4
 * @see <a href="https://docs.gradle.org/current/userguide/java_library_plugin.html">Java Library plugin reference</a>
 */
public abstract class JavaLibraryPlugin implements Plugin<Project> {

    private final JvmEcosystemUtilities jvmEcosystemUtilities;

    @Inject
    public JavaLibraryPlugin(JvmEcosystemUtilities jvmEcosystemUtilities) {
        this.jvmEcosystemUtilities = jvmEcosystemUtilities;
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        JvmSoftwareComponentInternal component = JavaPluginHelper.getJavaComponent(project);
        SourceSet sourceSet = component.getSourceSet();

        // TODO: Why do we not do this in createApiElements?
        jvmEcosystemUtilities.configureClassesDirectoryVariant(component.getApiElementsConfiguration(), sourceSet);

        RoleBasedConfigurationContainerInternal configurations = (RoleBasedConfigurationContainerInternal) project.getConfigurations();

        Configuration api = configurations.maybeCreateWithRole(sourceSet.getApiConfigurationName(), ConfigurationRoles.INTENDED_BUCKET, false, false);
        api.setDescription("API dependencies for " + sourceSet + ".");
        api.setVisible(false);

        Configuration compileOnlyApi = configurations.maybeCreateWithRole(sourceSet.getCompileOnlyApiConfigurationName(), ConfigurationRoles.INTENDED_BUCKET, false, false);
        compileOnlyApi.setDescription("Compile only API dependencies for " + sourceSet + ".");
        compileOnlyApi.setVisible(false);

        component.getApiElementsConfiguration().extendsFrom(api, compileOnlyApi);
        component.getImplementationConfiguration().extendsFrom(api);
        component.getCompileOnlyConfiguration().extendsFrom(compileOnlyApi);

        // Make compileOnlyApi visible to tests.
        JvmTestSuite defaultTestSuite = JavaPluginHelper.getDefaultTestSuite(project);
        configurations.getByName(defaultTestSuite.getSources().getCompileOnlyConfigurationName()).extendsFrom(compileOnlyApi);
    }
}
