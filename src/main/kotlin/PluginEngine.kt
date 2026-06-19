package org.example

sealed interface PluginEngine {
    fun attemptExecute()
    fun shouldTrigger(trigger: TriggerEvent): Boolean
    fun populateInputs(database: PluginDataSource)
    fun collectOutputs(): List<Any>
}