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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.codehaus.plexus.util.DirectoryScanner;

public class IsolatedClassloaderMavenLauncher
    implements MavenLauncher
{

    private File project;

    private File userSettings;

    private String[] goals;

    private long remoteAccessDelay;

    public int execute()
        throws Exception
    {
        // TODO actually parse m2.conf

        ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();
        URLClassLoader cl = new URLClassLoader( getMavenClasspath(), extCL );

        if ( remoteAccessDelay > 0 )
        {
            Class<?> filexCLass = cl.loadClass( "org.apache.maven.performance.FilexWagon" );
            Field delayField = filexCLass.getField( "delay" );
            delayField.set( null, remoteAccessDelay );
        }

        Class<?> cliUtilsClass = null;
        Method cliUtilsRemoveShutdownHookMethod = null;
        try
        {
            cliUtilsClass = cl.loadClass( "org.codehaus.plexus.util.cli.CommandLineUtils" );
        }
        catch ( ClassNotFoundException e )
        {
            // must be maven 2.x
            cliUtilsClass = cl.loadClass( "hidden.org.codehaus.plexus.util.cli.CommandLineUtils" );
        }

        try
        {
            cliUtilsRemoveShutdownHookMethod = cliUtilsClass.getMethod( "removeShutdownHook", boolean.class );
        }
        catch ( NoSuchMethodException e )
        {
            throw new IllegalArgumentException( "Unsupported plexus-utils version, must be 1.5.13 or newer", e );
        }

        Class<?> cworldClass;
        try
        {
            // 3.0
            cworldClass = cl.loadClass( "org.codehaus.plexus.classworlds.ClassWorld" );
        }
        catch ( ClassNotFoundException e )
        {
            // 2.1
            cworldClass = cl.loadClass( "org.codehaus.classworlds.ClassWorld" );
        }
        Constructor<?> cworldConstructor = cworldClass.getConstructor( String.class, ClassLoader.class );
        Object cworld = cworldConstructor.newInstance( "plexus.core", cl );

        Class<?> cliClass = cl.loadClass( "org.apache.maven.cli.MavenCli" );
        Object cli = cliClass.newInstance();

        Method mainMethod = cliClass.getMethod( "main", String[].class, cworldClass );

        ClassLoader origCL = Thread.currentThread().getContextClassLoader();
        String origMavenHome = System.getProperty( "maven.home" );

        Thread.currentThread().setContextClassLoader( cl );
        System.setProperty( "maven.home", getMavenHome().getCanonicalPath() );
        try
        {
            Object result = mainMethod.invoke( cli, getArgs(), cworld );
            
            return ((Integer) result).intValue();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( origCL );
            if ( origMavenHome != null )
            {
                System.setProperty( "maven.home", origMavenHome );
            }
            if ( cliUtilsRemoveShutdownHookMethod != null )
            {
                cliUtilsRemoveShutdownHookMethod.invoke( null, Boolean.TRUE );
            }
        }
    }

    private URL[] getMavenClasspath()
        throws IOException
    {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( getMavenHome() );
        ds.setIncludes( new String[] { "boot/plexus-classworlds-*.jar", "boot/classworlds-*.jar", "lib/*.jar" } );
//        ds.setExcludes( new String[] { "**/wagon-file-*.jar" } );
        ds.scan();

        String[] files = ds.getIncludedFiles();

        ArrayList<URL> urls = new ArrayList<URL>();
        urls.add( new File( "target/test-classes" ).getCanonicalFile().toURI().toURL() );

        for ( int i = 0; i < files.length; i++ )
        {
            File file = new File( ds.getBasedir(), files[i] );
            urls.add(file.toURI().toURL());
        }

        return urls.toArray( new URL[ urls.size() ] );
    }

    private File getMavenHome()
    {
        // return new File( "/tmp/apache-maven-3.0-SNAPSHOT" );
        // return new File( "/opt/maven" );
        return new File( System.getProperty( "test.maven.home" ) );
    }

    private String[] getArgs()
        throws Exception
    {
        ArrayList<String> args = new ArrayList<String>();
        args.add( "-f" );
        args.add( new File( project, "pom.xml" ).getCanonicalPath() );
        args.add( "-s" );
        args.add( userSettings.getCanonicalPath() );

        args.add( "-q" );
        args.add( "-B" );

        for ( String goal : goals )
        {
            args.add( goal );
        }

        return args.toArray( new String[args.size()] );
    }

    public void setGoals( String... goals )
    {
        this.goals = goals;
    }

    public void setProject( File project )
    {
        this.project = project;
    }

    public void setUserSettings( File userSettings )
    {
        this.userSettings = userSettings;
    }

    public void setRemoteAccessDelay( long delay )
    {
        this.remoteAccessDelay = delay;
    }
}
