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
import org.codehaus.plexus.logging.LoggerManager;

public class NullLoggerManager
    implements LoggerManager
{

    private Logger logger = new NullLogger();

    public int getActiveLoggerCount()
    {
        return 0;
    }

    public Logger getLoggerForComponent( String role )
    {
        return logger;
    }

    public Logger getLoggerForComponent( String role, String roleHint )
    {
        return logger;
    }

    public int getThreshold()
    {
        return 0;
    }

    public int getThreshold( String role )
    {
        return 0;
    }

    public int getThreshold( String role, String roleHint )
    {
        return 0;
    }

    public void returnComponentLogger( String role )
    {
    }

    public void returnComponentLogger( String role, String hint )
    {
    }

    public void setThreshold( int threshold )
    {
    }

    public void setThreshold( String role, int threshold )
    {
    }

    public void setThreshold( String role, String roleHint, int threshold )
    {
    }

    public void setThresholds( int threshold )
    {
    }

}
