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

package org.gradle.tooling.internal.provider.runner;

import org.gradle.StartParameter;
import org.gradle.api.logging.Logging;
import org.gradle.initialization.BuildRequestContext;
import org.gradle.initialization.GradleLauncherFactory;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.composite.CompositeBuildActionParameters;
import org.gradle.internal.composite.CompositeBuildActionRunner;
import org.gradle.internal.composite.CompositeBuildController;
import org.gradle.internal.composite.CompositeParameters;
import org.gradle.internal.composite.GradleParticipantBuild;
import org.gradle.internal.invocation.BuildAction;
import org.gradle.internal.invocation.BuildActionRunner;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.scopes.BuildSessionScopeServices;
import org.gradle.launcher.cli.ExecuteBuildAction;
import org.gradle.launcher.exec.BuildActionExecuter;
import org.gradle.launcher.exec.BuildActionParameters;
import org.gradle.launcher.exec.InProcessBuildActionExecuter;
import org.gradle.tooling.internal.provider.ExecuteBuildActionRunner;

public class CompositeBuildExecutionRunner implements CompositeBuildActionRunner {
    private static final org.gradle.api.logging.Logger LOGGER = Logging.getLogger(CompositeBuildExecutionRunner.class);

    public void run(BuildAction action, BuildRequestContext requestContext, CompositeBuildActionParameters actionParameters, CompositeBuildController buildController) {
        if (!(action instanceof ExecuteBuildAction)) {
            return;
        }
        executeTasksInProcess(action.getStartParameter(), actionParameters, requestContext, buildController.getBuildScopeServices());
        buildController.setResult(null);
    }

    private void executeTasksInProcess(StartParameter actionStartParameter, CompositeBuildActionParameters actionParameters, BuildRequestContext buildRequestContext, ServiceRegistry sharedServices) {
        GradleLauncherFactory gradleLauncherFactory = sharedServices.get(GradleLauncherFactory.class);
        CompositeParameters compositeParameters = actionParameters.getCompositeParameters();

        DefaultServiceRegistry compositeServices = createCompositeAwareServices(actionStartParameter, true, buildRequestContext, compositeParameters, sharedServices);

        StartParameter startParameter = actionStartParameter.newInstance();
        GradleParticipantBuild targetParticipant = compositeParameters.getTargetBuild();
        startParameter.setProjectDir(targetParticipant.getProjectDir());
        startParameter.setSearchUpwards(false);

        LOGGER.lifecycle("[composite-build] Executing tasks " + startParameter.getTaskNames() + " for participant: " + targetParticipant.getProjectDir());

        BuildActionRunner runner = new ExecuteBuildActionRunner();
        BuildActionExecuter<BuildActionParameters> buildActionExecuter = new InProcessBuildActionExecuter(gradleLauncherFactory, runner);
        ServiceRegistry buildScopedServices = new BuildSessionScopeServices(compositeServices, startParameter, ClassPath.EMPTY);

        buildActionExecuter.execute(new ExecuteBuildAction(startParameter), buildRequestContext, null, buildScopedServices);
    }

    private DefaultServiceRegistry createCompositeAwareServices(StartParameter buildStartParameter, boolean propagateFailures,
                                                                BuildRequestContext buildRequestContext, CompositeParameters compositeParameters, ServiceRegistry sharedServices) {
        return new CompositeBuildServicesBuilder().createCompositeAwareServices(buildStartParameter, propagateFailures, buildRequestContext, compositeParameters, sharedServices);
    }
}
