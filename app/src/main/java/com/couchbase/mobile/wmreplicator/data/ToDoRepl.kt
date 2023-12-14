//
// Copyright (c) 2023 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.mobile.wmreplicator.data

import com.couchbase.lite.BasicAuthenticator
import com.couchbase.lite.ReplicatorConfigurationFactory
import com.couchbase.lite.URLEndpoint
import com.couchbase.lite.WorkManagerReplicatorConfiguration
import com.couchbase.lite.WorkManagerReplicatorFactory
import com.couchbase.lite.newConfig
import org.koin.java.KoinJavaComponent.getKoin
import java.net.URI


/**
 * It is necessary to create a singleton instance of WorkManagerReplicatorFactory
 * for each replication process.
 * The Android system may request the tag from an instance of the factory that
 * is not the instance that was used to create it.  Worse than that, the system may
 * request the tag from a completely different instance of the entire application
 * process. The application may have been terminated and restarted and must still
 * return the correct tag.  The app may not even be completely initialized.
 * The tag and the replicator configuration pretty much have to be established at
 * compile time.
 *
 * This replicator will continue to run until it is stopped, even if the application
 * that runs it is stopped.  e parsimonious with user's batteries!  Stop your
 * Work Manager jobs!
 */
object ToDoRepl : WorkManagerReplicatorFactory {
    private const val TAG = "todoReplicator"
    private const val DB = "todo"
    private const val REMOTE = "ws://192.168.89.163:4985/todo"
    private const val REMOTE_USER = "admin"
    private const val REMOTE_PWD = "password"
    private val collections = listOf("_default.tasks", "_default.lists")

    override val tag: String
        get() = TAG

    /**
     * This example demonstrates the use of the familiar ReplicatorConfiguration
     * to configure the WorkManagerReplicator.  Note, however, that any changes
     * to the "continuous" property will be ignored
     */
    override fun getConfig(): WorkManagerReplicatorConfiguration {
        val dbSvc = getKoin().get<DbService>()
        val config = ReplicatorConfigurationFactory.newConfig(
            URLEndpoint(URI(REMOTE)),
            mapOf(dbSvc.getCollections(DB, collections) to null),
            authenticator = BasicAuthenticator(REMOTE_USER, REMOTE_PWD.toCharArray())
        )
        return WorkManagerReplicatorConfiguration.Companion.from(config)
    }
}
