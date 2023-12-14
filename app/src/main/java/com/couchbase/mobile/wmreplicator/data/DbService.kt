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
import com.couchbase.lite.Collection
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.LogDomain
import com.couchbase.lite.LogLevel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.zip.ZipInputStream

/**
 * Standard DB management:
 */
class DbService(private val context: WeakReference<Context>) {
    /**
     * Init Couchbase Lite.
     * Must be called before any database interactions
     */
    fun init() {
        context.get()?.let { CouchbaseLite.init(it, true) }
            ?: throw java.lang.IllegalStateException("Context is null")

        val consoleLogger = Database.log.console
        consoleLogger.level = LogLevel.DEBUG
        consoleLogger.domains = LogDomain.ALL_DOMAINS
    }

    /**
     * Return the named database.
     * If it does not exist, attempt to unzip the resource <dbName>.cblite.zip
     * and then Database.copy it to the default db directory
     */
    fun getDatabase(dbName: String): Database {
        if (Database.exists(dbName, null))
            return Database(dbName)

        val ctxt = context.get() ?: throw java.lang.IllegalStateException("Context is null")

        val unzipDir = File(ctxt.cacheDir, "tmp")
        if (unzipDir.exists() && !deleteRecursive(unzipDir)) {
            throw IllegalStateException("Failed deleting unzip tmp directory")
        }
        if (!unzipDir.mkdirs()) {
            throw IllegalStateException("Failed creating unzip tmp directory")
        }

        val datasetName = "${dbName}.cblite2"
        try {
            ctxt.assets.open("${datasetName}.zip").use { inStream -> unzip(inStream, unzipDir) }
        } catch (e: IOException) {
            throw IllegalStateException("Failed unzipping dataset: $datasetName", e)
        }

        try {
            Database.copy(File(unzipDir, datasetName), dbName, DatabaseConfiguration())
        } catch (e: CouchbaseLiteException) {
            throw IllegalStateException("Failed copying dataset: $datasetName to $dbName", e)
        }

        return Database(dbName)
    }

    /**
     * Get the named collections from the named database
     */
    fun getCollections(dbName: String, collections: List<String>): kotlin.collections.Collection<Collection> {
        val db = getDatabase(dbName)
        return collections
            .map {
                val coll = it.split(".")
                db.getCollection(coll[1], coll[0])
                    ?: throw java.lang.IllegalStateException("collection not found: ${it}")
            }
    }


    @Throws(IOException::class)
    private fun unzip(src: InputStream?, dst: File?) {
        val buffer = ByteArray(1024)
        src?.use { sis ->
            ZipInputStream(sis).use { zis ->
                var ze = zis.nextEntry
                while (ze != null) {
                    val newFile = File(dst, ze.name)
                    if (ze.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        File(newFile.parent!!).mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            while (true) {
                                val len = zis.read(buffer)
                                if (len < 0) {
                                    break
                                }
                                fos.write(buffer, 0, len)
                            }
                        }
                    }
                    ze = zis.nextEntry
                }
                zis.closeEntry()
            }
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) =
        !fileOrDirectory.exists() || (deleteContents(fileOrDirectory) && fileOrDirectory.delete())

    private fun deleteContents(fileOrDirectory: File?): Boolean {
        if (fileOrDirectory == null || !fileOrDirectory.isDirectory) {
            return true
        }

        val contents = fileOrDirectory.listFiles() ?: return true
        var succeeded = true
        for (file in contents) {
            if (!deleteRecursive(file)) {
                succeeded = false
            }
        }

        return succeeded
    }
}
