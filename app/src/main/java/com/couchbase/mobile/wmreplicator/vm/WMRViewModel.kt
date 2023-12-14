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
package com.couchbase.mobile.wmreplicator.vm

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.couchbase.mobile.wmreplicator.data.DbService
import com.couchbase.mobile.wmreplicator.data.Replication
import com.couchbase.mobile.wmreplicator.data.ToDoRepl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State of the UI:
 * * connected: the WorkManager has responded
 * * running: there is a ToDoRepl job in the work manager
 * * the next three fields are empty unless the replicator is running right now:
 *     they are straight out of ReplicatorStatus
 */
data class UIState(
    val connected: Boolean = false,
    val running: Boolean = false,
    val error: String = "",
    val state: String = "",
    val progress: String = ""
)

class WMRViewModel(private val dbSvc: DbService) : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val repl = Replication(ToDoRepl)

    fun connect(ctxt: ComponentActivity) {
        dbSvc.init()

        repl.watch(ctxt).observe(ctxt) { jobState ->
            if (jobState == null) {
                // Work Manager has never heard of this job
                _uiState.value = UIState(true, state = "NOT SCHEDULED")
                return@observe
            }

            // Work manager has heard of this job but all instances have been cancelled
            if (jobState.done) {
                _uiState.value = UIState(true, state = "FINISHED")
                return@observe
            }

            val replState = jobState.replStatus
            if (replState == null) {
                // The job is alive but is currently enqueued and waiting for execution
                _uiState.value = UIState(true, true, state = "WAITING")
                return@observe
            }

            // The job is executing right now
            _uiState.value = UIState(
                true,
                true,
                replState.error?.message ?: "",
                replState.activityLevel.toString(),
                if (replState.progress.completed == replState.progress.total) {
                    "Completed"
                } else {
                    "${replState.progress.total / replState.progress.completed}"
                }
            )
        }
    }

    /**
     * Start a ToDoRepl job
     */
    fun start(ctxt: ComponentActivity) {
        val state = uiState.value
        if (state.connected && (!state.running)) {
            repl.start(ctxt)
        }
    }


    /**
     * Stop *ALL* ToDoRepl jobs
     */
    fun stop(ctxt: ComponentActivity) {
        val state = uiState.value
        if (state.connected && (state.running)) {
            repl.stop(ctxt)
            _uiState.value = UIState(true)
        }
    }
}