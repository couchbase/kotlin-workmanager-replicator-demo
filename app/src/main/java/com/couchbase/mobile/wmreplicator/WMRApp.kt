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
package com.couchbase.mobile.wmreplicator

import android.app.Application
import android.content.Context
import com.couchbase.mobile.wmreplicator.data.DbService
import com.couchbase.mobile.wmreplicator.vm.WMRViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import java.lang.ref.WeakReference

class WMRApp : Application() {

    @Suppress("USELESS_CAST")
    override fun onCreate() {
        super.onCreate()

        val weakContext = WeakReference(this@WMRApp as Context)

        // Enable Koin dependency injection framework
        GlobalContext.startKoin {
            // inject Android context
            androidContext(this@WMRApp)

            // dependency register modules
            modules(
                module {
                    // these casts *do* appear to be necessary
                    single { DbService(weakContext) as DbService }
                    viewModel { WMRViewModel(get()) }
                })
        }
    }
}