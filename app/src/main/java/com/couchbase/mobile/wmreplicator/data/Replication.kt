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

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.couchbase.lite.ReplicatorStatus
import com.couchbase.lite.WorkManagerReplicatorFactory
import com.couchbase.lite.toReplicatorStatus
import java.util.concurrent.TimeUnit

/**
 * Unit of information passed to the VM:
 * * done == true if no work manager jobs are running
 * * replStatus != null if the replicator is actually running, right now
 */
data class WorkState(
    val done: Boolean = true,
    val replStatus: ReplicatorStatus? = null
)

/**
 * Unfortunately, there does not seem to be any way to forget a job: the request for
 * work info will return info for all of the jobs we've ever run.... most of then cancelled.
 * We adopt the following strategy:
 * * if the work manager has never heard of this tag (empty info list) return null
 * * if there are *any* jobs that are not stopped, report on the first one
 * * if there are no running jobs, just report say we're stopped.
 * The stop() method cancels *ALL* jobs with the tag.  After the call to stop(), the
 * LiveData returned by watch() will report on a random cancelled task.
 */
class Replication(private val wmrFactory: WorkManagerReplicatorFactory) {
    fun watch(ctxt: ComponentActivity): LiveData<WorkState?> {
        val tag = wmrFactory.tag
        return WorkManager.getInstance(ctxt).getWorkInfosByTagLiveData(tag).map { info ->
            // There are no jobs with this tag
            if (info.isEmpty()) {
                null
            } else {
                // look for running jobs with this tag
                val running = info.filter { !it.state.isFinished }
                // if there are no running jobs with this tag we're stopped
                // if there are running jobs, just report on the first one
                if (running.isEmpty()) {
                    WorkState()
                } else {
                    // toReplicatorStatus will return null unless the replicator
                    // is actually running.  In the latter case, it will return
                    // the replicator status.
                    WorkState(false, running[0].progress.toReplicatorStatus(tag))
                }
            }
        }
    }

    /**
     * Enqueue a new job
     */
    fun start(ctxt: ComponentActivity) {
        WorkManager.getInstance(ctxt).enqueue(
            wmrFactory.periodicWorkRequestBuilder(
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
        )
    }

    /**
     * Stop *ALL* jobs with this tag
     */
    fun stop(ctxt: Context) {
        WorkManager.getInstance(ctxt).cancelAllWorkByTag(wmrFactory.tag)
    }
}
