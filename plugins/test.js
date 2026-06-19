globalThis.pluginInfo = {
    description: "Greeter Plugin",
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
            name: "name",
            type: "java.lang.String"
        },
    ],
    outputs: [],
}

globalThis.main = function() {
    console.log('Hello ' + globalThis.name + ' from JS');
}