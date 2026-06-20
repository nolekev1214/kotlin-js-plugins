globalThis.pluginInfo = {
    description: "Timer Test Plugin",
    pluginEngine: 1,
    debugger: false,
    trigger: {
        periodic: "5s",
    },
    inputs: [],
    outputs: [],
}

globalThis.main = function() {
    console.log('Timer fired!');
}
