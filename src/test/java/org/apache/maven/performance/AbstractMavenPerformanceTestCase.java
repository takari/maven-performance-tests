package org.apache.maven.performance;

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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.cli.MavenCli;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.test.internal.performance.InternalDimensions;
import org.eclipse.test.internal.performance.InternalPerformanceMeter;
import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.eval.AssertChecker;
import org.eclipse.test.internal.performance.eval.Evaluator;
import org.eclipse.test.internal.performance.eval.RelativeBandChecker;
import org.eclipse.test.performance.PerformanceTestCase;

public abstract class AbstractMavenPerformanceTestCase
    extends PerformanceTestCase
{
    protected MavenLauncher getMavenLauncher()
        throws IOException
    {
        MavenLauncher launcher = new IsolatedClassloaderMavenLauncher();
        launcher.setUserSettings( getUserSettingsFile() );
        return launcher;
    }

    protected File getProject( String path )
        throws IOException
    {
        File src = new File( "projects", path );
        File dst = new File( "target/projects", path );

        if ( dst.isDirectory() )
        {
            FileUtils.deleteDirectory( dst );
        }
        else if ( dst.isFile() )
        {
            if ( !dst.delete() )
            {
                throw new IOException( "Can't delete file " + dst.toString() );
            }
        }

        FileUtils.copyDirectoryStructure( src, dst );

        return dst;
    }

    @Override
    protected void assertPerformance()
    {
        assertPerformance( new RelativeBandChecker( InternalDimensions.CPU_TIME, 0.8f, 1.1f ),
                           new RelativeBandChecker( InternalDimensions.RCHAR, 0.8f, 1.1f ),
                           new RelativeBandChecker( InternalDimensions.WCHAR, 0.8f, 1.1f ) );
    }

    protected void assertPerformance( AssertChecker... assertCheckers )
    {
        Evaluator e = new Evaluator();
        e.setAssertCheckers( assertCheckers );
        e.evaluate( fPerformanceMeter );

        String scenarioName = ( (InternalPerformanceMeter) fPerformanceMeter ).getScenarioName();

        @SuppressWarnings( "unchecked" )
        Map failures = DB.queryFailure( scenarioName, PerformanceTestPlugin.getVariations() );

        if ( failures != null )
        {
            assertTrue( failures.values().toString(), failures.isEmpty() );
        }
    }

    protected File getUserSettingsFile()
        throws IOException
    {
        return new File( "settings.xml" ).getCanonicalFile();
    }

    protected void purgeLocalRepository()
        throws IOException
    {
        try
        {
            // local repo is defined in settings.xml
            FileUtils.deleteDirectory( getLocalRepository() );
        }
        catch ( IOException e )
        {
            // happens on Windows due to failure to delete some JAR, not critical
            e.printStackTrace();
        }
    }

    protected File getLocalRepository()
        throws IOException
    {
        return new File( "localRepo" ).getCanonicalFile();
    }

    static String getMavenVersion()
        throws IOException
    {
        Properties properties = getBuildProperties();
        String version = properties.getProperty( "version" );
        if ( version.endsWith( "-SNAPSHOT" ) )
        {
            long timestamp = Long.valueOf( properties.getProperty( "timestamp" ) );
            SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            String qualifier = format.format( new Date( timestamp ) );
            version = version.replace( "-SNAPSHOT", qualifier );
        }
        return version;
    }

    static Properties getBuildProperties()
        throws IOException
    {
        Properties properties = new Properties();
        InputStream resourceAsStream = null;
        try
        {
            String path = "/org/apache/maven/messages/build.properties";
            resourceAsStream = MavenCli.class.getResourceAsStream( path );

            if ( resourceAsStream != null )
            {
                properties.load( resourceAsStream );
            }
            else
            {
                throw new IOException( "Classpath resource not found " + path );
            }
        }
        finally
        {
            IOUtil.close( resourceAsStream );
        }

        return properties;
    }

    static
    {
        try
        {
            // TODO I do not enjoy this, I honestly don't.
            System.setProperty( "eclipse.perf.config", "build=" + getMavenVersion() );

            /*
             * hudson uses -Dmaven.ext.class.path to enable its maven intergation but surefire blindly copies all
             * -Dproperties to the forked jvm which enables hudson integration for the test builds. as a workaround, set
             * the system property to a dummy value
             */
            System.getProperties().remove( "maven.ext.class.path" );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
