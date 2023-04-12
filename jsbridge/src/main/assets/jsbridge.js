(function() {
    if (window.JsBridgeReady) {
        return;
    }

    var CALL_URL = 'jsbridge://call_native?call=';
    var CALLBACK_ID_PREFIX = "CALLBACK_ID_" + new Date().getTime() + "_";
    var __native = {};
    __native.__callbackIdIndex = 1;
    __native.__callbacks = {};

    __native.__generateCallbackId = function() {
        return CALLBACK_ID_PREFIX +
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
    
    __native.__call = function(obj, method, args) {
        var callbackIds = [];
        for (var i = 0; i < args.length; i++) {
            if (args[i] && typeof args[i] == 'function') {
                var callId = __native.__generateCallbackId();
                callbackIds.push(callId);
                __native.__callbacks[callId] = __native.__generateCallback(callId, args[i]);
                //替换为id
                args[i] = callId;
            }
        }
        var result = prompt(CALL_URL + JSON.stringify({
            'method': method,
            'obj': obj,
            'args': args
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
                                return __native.__call(prop, method, args || []);
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
                keepAlive: keepAlive
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
