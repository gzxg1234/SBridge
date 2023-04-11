(function() {
    if (window.JsBridgeReady) {
        return;
    }

    var CALL_URL = 'jsbridge://call_native?call=';
    var CALLBACK_PREFIX = new Date().getTime() + "-";
    var __native = {};
    __native.__callbackIdIndex = 1;
    __native.__callbacks = {};

    __native.__generateCallbackId = function() {
        return CALLBACK_PREFIX +
            ((__native.__callbackIdIndex++) % Number.MAX_SAFE_INTEGER);
    }

    __native.__generateCallback = function(callId, callback) {
        return function(result) {
            if (result.release) {
                delete __native.__callbacks[callId];
            }
            if (result.next) {
                callback(result.value);
            }
        };
    }

    __native.__call = function(obj, method, args, ...callbacks) {
        var callbackIds = [];
        if (callbacks) {
            for (var i = 0; i < callbacks.length; i++) {
                if (callbacks[i]) {
                    var callId = __native.__generateCallbackId();
                    callbackIds.push(callId);
                    __native.__callbacks[callId] = __native.__generateCallback(callId, callbacks[i]);
                } else {
                    callbackIds.push(null);
                }
            }
        }
        var result = prompt(CALL_URL + JSON.stringify({
            'method': method,
            'obj': obj,
            'isAsync': true,
            'args': args,
            'callbackIds': callbackIds
        }));
        var resultObj = JSON.parse(result);
        if (resultObj.isSuccess) {
            return resultObj.value;
        } else {
            for (var id in callbackIds) {
                id && (delete __native.__callbacks[id]);
            }
            throw resultObj.value;
        }
    }

    window.___callbackFromNative = function(id, result) {
        var callback = __native.__callbacks[id];
        callback && callback(result);
    }


    var Native = new Proxy({}, {
        set: function(target, prop, value) {
            return;
        },
        get: function(target, prop) {
            if (target[prop] == undefined) {
                target[prop] = new Proxy({}, {
                    get: function(objTarget, method) {
                        if (objTarget[method] == undefined) {
                            objTarget[method] = function(...args) {
                                if (args && args.length > 0) {
                                    if (typeof args[0] == 'object') {
                                        return __native.__call(prop, method,
                                            args[0], ...(args.slice(1)));
                                    } else if (typeof args[0] == 'function') {
                                        return __native.__call(prop, method,
                                            null, ...args);
                                    }
                                } else {
                                    return __native.__call(prop, method, null);
                                }
                                throw "wrong arguments";
                            }
                        }
                        return objTarget[method];
                    }
                });
            }
            return target[prop];
        }
    });

    window.Native = Native;


    var WebInterface = {}

    function __toString(any) {
        if (typeof any == 'undefined') {
            return 'null';
        }
        if (typeof any == 'string') {
            return any;
        }
        return JSON.stringify(any)
    }

    window.___callFromNative = function(method, args, callbackId) {
        var methodFunc = WebInterface[method];
        if (!methodFunc) {
            Native.__Native.callbackFromWeb({
                id: callbackId,
                error: "method not define in web"
            })
            return
        }

        var callback = function(result, keepAlive) {
            Native.__Native.callbackFromWeb({
                id: callbackId,
                value: __toString(result),
                keepAlive: keepAlive || false
            });
        }
        try {
            methodFunc(args, callback)
        } catch (e) {
            Native.__Native.callbackFromWeb({
                id: callbackId,
                error: ("method invoke error,msg=" + e)
            })
            return
        }
    }

    window.WebInterface = WebInterface
    window.JsBridgeReady = true

    console.log('JsBridge is ready');
    var readyEvent = document.createEvent('Event');
    readyEvent.initEvent('onJsBridgeReady');
    window.dispatchEvent(readyEvent);
})();
