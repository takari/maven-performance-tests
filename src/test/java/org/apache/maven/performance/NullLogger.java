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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.logging.Logger;

public class NullLogger
    implements Logger
{

    public void debug( String message )
    {
    }

    public void debug( String message, Throwable throwable )
    {
    }

    public void error( String message )
    {
    }

    public void error( String message, Throwable throwable )
    {
    }

    public void fatalError( String message )
    {
    }

    public void fatalError( String message, Throwable throwable )
    {
    }

    public Logger getChildLogger( String name )
    {
        return this;
    }

    public String getName()
    {
        return "";
    }

    public int getThreshold()
    {
        return 0;
    }

    public void info( String message )
    {
    }

    public void info( String message, Throwable throwable )
    {
    }

    public boolean isDebugEnabled()
    {
        return false;
    }

    public boolean isErrorEnabled()
    {
        return false;
    }

    public boolean isFatalErrorEnabled()
    {
        return false;
    }

    public boolean isInfoEnabled()
    {
        return false;
    }

    public boolean isWarnEnabled()
    {
        return false;
    }

    public void setThreshold( int threshold )
    {
    }

    public void warn( String message )
    {
    }

    public void warn( String message, Throwable throwable )
    {
    }

}
