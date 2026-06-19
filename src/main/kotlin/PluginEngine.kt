package org.example

sealed interface PluginEngine {
    fun attemptExecute()
    fun shouldTrigger(triggerType: String, databaseGroup: String, type: String): Boolean
    fun populateInputs(database: PluginDataSource)
    fun collectOutputs(): List<Any>
}