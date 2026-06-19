globalThis.pluginInfo = {
    description: "Test Plugin",
    pluginEngine: 1,
    debugger: false,
    trigger: {
        triggerType: "onUpdate",
        databaseGroup: "globals",
        type: "java.lang.String",
    },
    inputs: [
        {
            databaseGroup: "globals",
            name: "param",
            type: "java.lang.String"
        },
    ],
    outputs: [],
}

globalThis.main = function() {
    console.log('Hello ' + globalThis.param + ' from JS');
}