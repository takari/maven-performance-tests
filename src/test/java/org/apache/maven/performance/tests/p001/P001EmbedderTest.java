package org.apache.maven.performance.tests.p001;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.performance.AbstractMavenPerformanceTestCase;
import org.apache.maven.performance.FilexWagon;
import org.apache.maven.performance.NullLoggerManager;
import org.apache.maven.plugin.ExtensionRealmCache;
import org.apache.maven.plugin.PluginDescriptorCache;
import org.apache.maven.plugin.PluginRealmCache;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.project.ProjectRealmCache;
import org.apache.maven.project.ProjectSorter;
import org.apache.maven.project.artifact.MavenMetadataCache;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.DirectoryScanner;
import org.eclipse.test.internal.performance.InternalDimensions;
import org.eclipse.test.internal.performance.eval.RelativeBandChecker;

public class P001EmbedderTest
    extends AbstractMavenPerformanceTestCase
{
    private static final long REMOTE_ACCESS_DELAY = 100L;

    private static final int EXECUTION_COUNT = 5;

    private DefaultPlexusContainer container;

    private MavenExecutionRequestPopulator populator;

    private SettingsBuilder settingsBuilder;

    private ProjectBuilder projectBuilder;

    private LifecycleExecutor lifecycleExecutor;

    private MavenMetadataCache metadataCache;

    private PluginRealmCache pluginRealmCache;

    private ProjectRealmCache projectRealmCache;

    private ExtensionRealmCache extensionRealmCache;

    private PluginDescriptorCache pluginDescriptorCache;

    private RepositorySystem repositorySystem;

    private Maven maven;

    private ModelReader modelReader;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        container = new DefaultPlexusContainer();
        container.setLoggerManager( new NullLoggerManager() );

        this.populator = container.lookup( MavenExecutionRequestPopulator.class );
        this.projectBuilder = container.lookup( ProjectBuilder.class );
        this.settingsBuilder = container.lookup( SettingsBuilder.class );
        this.lifecycleExecutor = container.lookup( LifecycleExecutor.class );
        this.metadataCache = container.lookup( MavenMetadataCache.class );
        this.pluginRealmCache = container.lookup( PluginRealmCache.class );
        this.pluginDescriptorCache = container.lookup( PluginDescriptorCache.class );
        this.projectRealmCache = container.lookup( ProjectRealmCache.class );
        this.extensionRealmCache = container.lookup( ExtensionRealmCache.class );
        this.repositorySystem = container.lookup( RepositorySystem.class );
        this.maven = container.lookup( Maven.class );
        this.modelReader = container.lookup( ModelReader.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        projectBuilder = null;
        settingsBuilder = null;
        populator = null;
        lifecycleExecutor = null;
        metadataCache = null;
        pluginRealmCache = null;
        pluginDescriptorCache = null;
        pluginRealmCache = null;
        extensionRealmCache = null;
        maven = null;
        modelReader = null;

        container.dispose();
        container = null;

        super.tearDown();
    }

    public void testCalculateExecutionPlan()
        throws Exception
    {
        System.out.println( getName() + "#setup" );
        purgeLocalRepository();
        install( "p001/libs" );
        install( "p001/core" );

        File basedir = getProject( "p001/plugins" );

        // warm up
        System.out.println( getName() + "#warmup" );
        doCalculateExecutionPlan( basedir, false );

        for ( int i = 0; i < EXECUTION_COUNT; i++ )
        {
            System.out.println( getName() + "#" + i );

            startMeasuring();

            doCalculateExecutionPlan( basedir, true );

            stopMeasuring();
        }

        commitMeasurements();
        assertPerformance();
    }

    public void testCalculateExecutionPlanNoPoms()
        throws Exception
    {
        System.out.println( getName() + "#setup" );
        purgeLocalRepository();
        install( "p001/libs" );
        install( "p001/core" );

        File basedir = getProject( "p001/plugins" );

        // warm up
        System.out.println( getName() + "#warmup" );
        doCalculateExecutionPlan( basedir, false );

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( new File( getLocalRepository(), "p001/libs" ) );
        ds.setIncludes( new String[] { "**/pom.xml" } );
        ds.scan();

        for ( String path : ds.getIncludedFiles() )
        {
            assertTrue( new File( ds.getBasedir(), path ).delete() );
        }

        for ( int i = 0; i < EXECUTION_COUNT; i++ )
        {
            System.out.println( getName() + "#" + i );

            startMeasuring();

            doCalculateExecutionPlan( basedir, true );

            stopMeasuring();
        }

        commitMeasurements();
        assertPerformance();
    }

    private void doCalculateExecutionPlan( File basedir, boolean delay )
        throws Exception, IOException
    {
        List<String> modules = getModules( basedir );

        flushCaches();

        if ( delay )
        {
            FilexWagon.delay = REMOTE_ACCESS_DELAY;
        }

        try
        {
            MavenExecutionRequest request = createExecutionRequest();
            populator.populateDefaults( request );

            for ( String module : modules )
            {
                request.setPom( new File( basedir, module + "/pom.xml" ) );
                request.setGoals( Arrays.asList( "deploy" ) );

                MavenExecutionResult result = readProjectWithDependencies( request );
                MavenExecutionPlan plan = calculateExecutionPlan( request, result.getProject() );

                assertFalse( result.getExceptions().toString(), result.hasExceptions() );
                assertEquals( module, result.getProject().getArtifactId() );

                assertEquals( plan.getMojoExecutions().toString(), 8, plan.getMojoExecutions().size() );
            }
        }
        finally
        {
            FilexWagon.delay = 0;
        }
    }

    private void flushCaches()
    {
        metadataCache.flush();
        pluginRealmCache.flush();
        pluginDescriptorCache.flush();
        projectRealmCache.flush();
        extensionRealmCache.flush();
    }

    private List<String> getModules( File basedir )
        throws Exception
    {
        ArrayList<MavenProject> projects = new ArrayList<MavenProject>();

        Model model = modelReader.read( new File( basedir, "pom.xml" ), null );

        MavenExecutionRequest request = createExecutionRequest();
        populator.populateDefaults(request);

        for ( String modulePath : model.getModules() )
        {
            ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
            configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            MavenProject project = projectBuilder.build( new File( basedir, modulePath + "/pom.xml" ), configuration ).getProject();
            projects.add( project );
        }

        ArrayList<String> result = new ArrayList<String>();

        for ( MavenProject project : new ProjectSorter( projects ).getSortedProjects() )
        {
            result.add( project.getArtifactId() );
        }

        return result;
    }

    public MavenExecutionRequest createExecutionRequest()
        throws Exception
    {
        SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
        settingsRequest.setUserSettingsFile( getUserSettingsFile() );
        settingsRequest.setGlobalSettingsFile( MavenCli.DEFAULT_GLOBAL_SETTINGS_FILE );

        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setUserSettingsFile( settingsRequest.getUserSettingsFile() );
        request.setGlobalSettingsFile( settingsRequest.getGlobalSettingsFile() );
        request.setSystemProperties( System.getProperties() );
        populator.populateFromSettings( request, settingsBuilder.build( settingsRequest ).getEffectiveSettings() );
        return request;
    }

    public MavenExecutionResult readProjectWithDependencies( MavenExecutionRequest request )
    {
        File pomFile = request.getPom();
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        ProjectBuildingResult projectBuildingResult;
        try
        {
            ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
            configuration.setResolveDependencies( true );
            projectBuildingResult = projectBuilder.build( pomFile, configuration );
            result.setProject( projectBuildingResult.getProject() );
            result.setArtifactResolutionResult( projectBuildingResult.getArtifactResolutionResult() );
        }
        catch ( ProjectBuildingException ex )
        {
            return result.addException( ex );
        }
        return result;
    }

    public MavenExecutionPlan calculateExecutionPlan( MavenExecutionRequest request, MavenProject project )
        throws Exception
    {
        MavenSession session = newSession( request, project );
        List<String> goals = request.getGoals();
        return lifecycleExecutor.calculateExecutionPlan( session, goals.toArray( new String[goals.size()] ) );
    }

    public MavenSession newSession( MavenExecutionRequest request, MavenProject project )
        throws Exception
    {
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        return new MavenSession( container, request, result, project );
    }

    protected void install( String path )
        throws Exception
    {
        File basedir = getProject( path );

        MavenExecutionRequest request = createExecutionRequest();
        request.setPom( new File( basedir, "pom.xml" ) );
        request.setGoals( Arrays.asList( "install" ) );

        populator.populateDefaults( request );

        MavenExecutionResult result = maven.execute( request );

        assertFalse( result.getExceptions().toString(), result.hasExceptions() );
    }

    public void testResolveMissingArtifact()
        throws Exception
    {
        System.out.println( getName() + "#setup" );
        purgeLocalRepository();

        // warm up
        System.out.println( getName() + "#warmup" );
        doTestResolveMissingArtifact( false );

        System.out.println( getName() + "#runs" );
        for ( int i = 0; i < 100; i++ )
        {
            startMeasuring();

            doTestResolveMissingArtifact( true );

            stopMeasuring();
        }

        commitMeasurements();
        /*
         * Upon success, this test has an average execution time of about 4ms. Upon failure, the remote access delay
         * increases this by at least 100ms so we can afford the more lax band checking here, thereby stabilizing the
         * test.
         */
        assertPerformance( new RelativeBandChecker( InternalDimensions.SYSTEM_TIME, 0.5f, 10.0f ) );
    }

    private void doTestResolveMissingArtifact( boolean delay )
        throws Exception
    {
        flushCaches();

        if ( delay )
        {
            FilexWagon.delay = REMOTE_ACCESS_DELAY;
        }

        try 
        {
            MavenExecutionRequest executionRequest = createExecutionRequest();
            populator.populateDefaults( executionRequest );
            Artifact artifact = repositorySystem.createArtifactWithClassifier( "missing", "missing", "1.0", "jar", "sources" );
            ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                .setArtifact( artifact )
                .setLocalRepository( executionRequest.getLocalRepository() )
                .setRemoteRepositories( executionRequest.getRemoteRepositories() );
            repositorySystem.resolve( request );
        }
        finally
        {
            FilexWagon.delay = 0;
        }
    }

    
}
