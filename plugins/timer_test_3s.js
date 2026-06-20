globalThis.pluginInfo = {
    description: "Timer Test 3s Plugin",
    pluginEngine: 1,
    debugger: false,
    trigger: {
        periodic: "3s",
    },
    inputs: [],
    outputs: [],
}

globalThis.main = function() {
    console.log('[' + new Date().toISOString() + '] 3s timer fired!');
}
