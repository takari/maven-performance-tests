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

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.providers.file.FileWagon;

public class FilexWagon
    extends FileWagon
{

    public static long delay;

    public static volatile long count;

    @Override
    public void get( String resourceName, File destination )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
//        if ( delay > 0 )
//        {
//            System.err.println( "get       : " + resourceName );
//        }

        count++;

        sleep();

        super.get( resourceName, destination );
    }

    @Override
    public boolean getIfNewer( String resourceName, File destination, long timestamp )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
//        if ( delay > 0 )
//        {
//            System.err.println( "getIfNewer: " + resourceName );
//        }
            
        count++;

        sleep();

        return super.getIfNewer( resourceName, destination, timestamp );
    }

    private void sleep()
        throws TransferFailedException
    {
        if ( delay >= 0 )
        {
            try
            {
                Thread.sleep( delay );
            }
            catch ( InterruptedException e )
            {
                throw new TransferFailedException( e.getMessage() );
            }
        }
    }
}
