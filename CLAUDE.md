# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```
mvn compile
```

**Build JAR:**
```
mvn package
```

**Run:**
```
mvn exec:java
```

**Run tests:**
```
mvn test
```

## Architecture

This is a Kotlin/JVM plugin engine that loads and executes JavaScript plugins via **GraalVM Polyglot** (GraalJS). The core flow is event-driven:

1. **`Database`** stores typed values in a `ConcurrentHashMap` keyed by Java class name. On each insert, it enqueues a `DatabaseTriggerEvent` (identifying `databaseGroup` + Java type string) onto a shared `ArrayBlockingQueue<TriggerEvent>`.

2. **`Timer`** schedules periodic `DurationTriggerEvent`s onto the same queue using a `ScheduledExecutorService`.

3. **`PluginExecutor`** runs a daemon thread that blocks on `triggerEventsQueue.take()`. For each event, it checks every loaded `PluginEngine` — if the plugin's declared trigger matches, it calls `populateInputs` then `attemptExecute`.

4. **`PluginEngine`** wraps a single GraalVM `Context`. It reads `pluginInfo` and `main` from the JS global scope at load time. `populateInputs` fetches each declared input from `Database` (via `PluginDataSource`) and injects it as a GraalVM binding; if any input is missing, `inputsSatisfied` stays false and `main` is not called.

### Plugin contract (JavaScript)

Each `.js` file in `./plugins/` must export two globals:

```js
globalThis.pluginInfo = {
    description: "...",
    pluginEngine: 1,          // must match PLUGIN_ENGINE_VERSION in PluginEngine.kt
    debugger: false,          // true enables GraalVM inspector
    trigger: {
        // database trigger:
        databaseGroup: "globals",
        type: "java.lang.String",
        // OR periodic trigger:
        // periodic: "5s",
    },
    inputs: [
        { databaseGroup: "globals", name: "bindingName", type: "java.lang.String" }
    ],
    outputs: [],
}

globalThis.main = function() { /* runs when triggered and all inputs are satisfied */ }
```

- Trigger type is inferred from the shape of the `trigger` object: presence of `periodic` key → `DurationTriggerEvent`; otherwise → `DatabaseTriggerEvent`.
- `type` strings for database triggers/inputs are Java fully-qualified class names (e.g., `"java.lang.String"`, `"java.lang.Integer"`).
- Each plugin runs in its own isolated `Context`. Host access is fully open (`HostAccess.ALL`, `allowHostClassLookup { true }`), giving JS full access to JVM classes.
- Plugins are loaded from the `./plugins/` directory relative to the working directory at runtime (not the project root).

### Key design constraints

- The trigger queue (`ArrayBlockingQueue` capacity 10) can overflow if database inserts happen faster than the executor thread drains it — the `Thread.sleep` calls in `Main.kt` are a known workaround for this.
- `Timer.scheduleFromPlugins` deduplicates periodic triggers by duration, so multiple plugins with the same interval share one scheduler task.
- `collectOutputs()` in `PluginEngine` is not yet implemented.
