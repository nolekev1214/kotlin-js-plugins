# kotlin-js-plugins

A Kotlin/JVM plugin engine that loads and executes JavaScript plugins via **GraalVM Polyglot** (GraalJS). Plugins are event-driven: they declare a trigger and a set of typed inputs, and run automatically when their trigger fires and all inputs are available.

## Requirements

- JDK 17+ (GraalVM recommended for best performance)
- Maven 3.x

## Commands

```bash
# Compile
mvn compile

# Run
mvn exec:java

# Build JAR
mvn package

# Run tests
mvn test
```

## How it works

1. **`Database`** stores typed values in a `ConcurrentHashMap` keyed by Java class name. On each insert it enqueues a `DatabaseTriggerEvent` onto a shared `ArrayBlockingQueue<TriggerEvent>`.
2. **`PluginExecutor`** runs a daemon thread that blocks on the queue. For each event, it checks every loaded `PluginEngine` — if the plugin's declared trigger matches, it calls `populateInputs` then `attemptExecute`.
3. **`PluginEngine`** wraps a single GraalVM `Context`. It reads `pluginInfo` and `main` from the JS global scope at load time. `populateInputs` fetches each declared input from `Database` and injects it as a GraalVM binding; if any input is missing the plugin does not execute.

Plugins are loaded from `./plugins/` relative to the working directory at runtime.

## Writing a plugin

Each `.js` file in `./plugins/` must export two globals — `pluginInfo` and `main`:

```js
globalThis.pluginInfo = {
    description: "My Plugin",
    pluginEngine: 1,      // must match PLUGIN_ENGINE_VERSION
    debugger: false,      // true enables the GraalVM inspector
    trigger: {
        databaseGroup: "globals",
        type: "java.lang.String",
    },
    inputs: [
        { databaseGroup: "globals", name: "myVar", type: "java.lang.String" }
    ],
    outputs: [],
}

globalThis.main = function() {
    console.log("triggered with: " + globalThis.myVar)
}
```

### `pluginInfo` fields

| Field | Description |
|---|---|
| `description` | Human-readable plugin name |
| `pluginEngine` | Must equal `PLUGIN_ENGINE_VERSION` (currently `1`) |
| `debugger` | Set to `true` to attach the GraalVM inspector |
| `trigger` | Object describing what fires this plugin (see below) |
| `inputs` | Array of values to fetch from the database before running |
| `outputs` | Reserved, not yet implemented |

### Trigger types

**Database trigger** — fires when a value of the given type is inserted into a database group:

```js
trigger: {
    databaseGroup: "globals",
    type: "java.lang.String",   // fully-qualified Java class name
}
```

### Inputs

Each input entry fetches a value from the database and injects it into the JS context as a named binding:

```js
{ databaseGroup: "globals", name: "bindingName", type: "java.lang.String" }
```

`type` is a fully-qualified Java class name (e.g. `"java.lang.Integer"`, `"java.lang.Double"`). If any input is missing from the database when the plugin triggers, `main` is not called.

### Host access

Plugins run with `HostAccess.ALL` and unrestricted `allowHostClassLookup`, giving JavaScript full access to JVM classes and instances passed through the database.

## Example plugin

`plugins/test.js` — greets each new string inserted into the globals database:

```js
globalThis.pluginInfo = {
    description: "Greeter Plugin",
    pluginEngine: 1,
    debugger: false,
    trigger: {
        databaseGroup: "globals",
        type: "java.lang.String",
    },
    inputs: [
        { databaseGroup: "globals", name: "name", type: "java.lang.String" }
    ],
    outputs: [],
}

globalThis.main = function() {
    console.log('Hello ' + globalThis.name + ' from JS')
}
```

## Known limitations

- `collectOutputs()` in `PluginEngine` is not yet implemented.
- Only one value per type per database group is stored; inserting a second `String` into `globals` overwrites the first.
