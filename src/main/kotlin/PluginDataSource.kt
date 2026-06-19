package org.example

interface PluginDataSource {
    fun get(databaseGroup: String, type: String): Any?
}