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

import org.apache.maven.performance.AbstractMavenPerformanceTestCase;
import org.apache.maven.performance.MavenLauncher;
import org.eclipse.test.internal.performance.InternalDimensions;
import org.eclipse.test.internal.performance.eval.RelativeBandChecker;

public class P001MavenTest
    extends AbstractMavenPerformanceTestCase
{
    private static final int EXECUTION_COUNT = 1;
    private static final long REMOTE_ACCESS_DELAY = 100L;

    public void testCleanInstallCore()
        throws Exception
    {
        MavenLauncher launcher = getMavenLauncher();

        // set up
        System.out.println( getName() + "#setup" );
        purgeLocalRepository();
        launcher.setProject( getProject( "p001/libs" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();

        // warm up
        System.out.println( getName() + "#warmup" );
        launcher.setProject( getProject( "p001/core" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();

        // test
        launcher.setRemoteAccessDelay( REMOTE_ACCESS_DELAY );
        for ( int i = 0; i < EXECUTION_COUNT; i++ )
        {
            System.out.println( getName() + "#" + i );

            startMeasuring();
            int result = launcher.execute();
            stopMeasuring();

            assertEquals( "Maven return value", 0, result );

            // TODO validate results
        }

        commitMeasurements();
        assertPerformance();
    }

    public void testCleanInstallPlugins()
        throws Exception
    {
        MavenLauncher launcher = getMavenLauncher();

        // set up
        System.out.println( getName() + "#setup" );
        purgeLocalRepository();
        launcher.setProject( getProject( "p001/libs" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();
        launcher.setProject( getProject( "p001/core" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();

        // warm up
        System.out.println( getName() + "#warmup" );
        launcher.setProject( getProject( "p001/plugins" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();

        // test
        launcher.setRemoteAccessDelay( REMOTE_ACCESS_DELAY );
        for ( int i = 0; i < EXECUTION_COUNT; i++ )
        {
            System.out.println( getName() + "#" + i );

            startMeasuring();
            int result = launcher.execute();
            stopMeasuring();

            assertEquals( "Maven return value", 0, result );

            // TODO validate results
        }

        commitMeasurements();
        assertPerformance();
    }

    public void testValidateImport()
        throws Exception
    {
        MavenLauncher launcher = getMavenLauncher();

        // set up
        System.out.println( getName() + "#setup" );
        purgeLocalRepository();
        launcher.setProject( getProject( "p001/import/corporate-pom" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();
        launcher.setProject( getProject( "p001/import/imported-pom" ) );
        launcher.setGoals( "clean", "install" );
        launcher.execute();

        // warm up
        System.out.println( getName() + "#warmup" );
        launcher.setProject( getProject( "p001/import" ) );
        launcher.setGoals( "validate" );
        launcher.execute();

        // test
        launcher.setRemoteAccessDelay( REMOTE_ACCESS_DELAY );
        for ( int i = 0; i < EXECUTION_COUNT * 2; i++ )
        {
            System.out.println( getName() + "#" + i );

            startMeasuring();
            int result = launcher.execute();
            stopMeasuring();

            assertEquals( "Maven return value", 0, result );

            // TODO validate results
        }

        commitMeasurements();
        assertPerformance( new RelativeBandChecker( InternalDimensions.CPU_TIME, 0.8f, 1.2f ),
                           new RelativeBandChecker( InternalDimensions.RCHAR, 0.8f, 1.1f ),
                           new RelativeBandChecker( InternalDimensions.WCHAR, 0.8f, 1.1f ) );
    }

}
