# SBridge
SBridge是一个Android端的JSBridge，实现Web和Native的方法互相调用，支持同步/异步调用，支持回调，支持参数转换
## 快速使用
### 引入SBridge
#### Android端
1. 创建自定义WebView，继承BridgeWebView
	```
	class MyWebView:BridgeWebView {
	    constructor(context: Context) : super(context)
	    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
	    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
	        context,
	        attrs,
	        defStyleAttr
	    )
	}
	```
2. 创建自定义WebView，继承WebView，按照BridgeWebView方式，构造SBridge
适合引入到已有自定义WebView的项目
	```
	class MyWebView:WebView {
	    //...
	
	    private val bridge = SBridge(this)
	
	    init {
	        super.setWebViewClient(bridge.getWebViewClientProxy())
	        super.setWebChromeClient(bridge.getWebChromeClientProxy())
	    }
	
	    override fun setWebChromeClient(client: WebChromeClient?) {
	        bridge.setWebChromeClient(client)
	    }
	
	    override fun setWebViewClient(client: WebViewClient) {
	        bridge.setWebViewClient(client)
	    }
	
	    fun addNativeObj(name: String, obj: Any) {
	        bridge.addNativeObj(name, obj)
	    }
	
	    fun addConverter(converter: ParameterConverter<*>) {
	        bridge.addConverter(converter)
	    }
	
	    fun callWebMethod(method: String, args: JSONObject, nativeCallback: NativeCallback) {
	        bridge.callWebMethod(method, args, nativeCallback)
	    }
	
	    fun setAsyncExecutor(executor: Executor) {
	        bridge.setAsyncExecutor(executor)
	    }
	}
	```

#### Web端
SBridge在Android端内置了jsbridge.js文件，Web端可使用native://jsbridge.js以加载js文件。
```
<script src="native://jsbridge.js"></script>
```

### Web调用Native方法
1. 定义对象类  
提供给Web调用的方法需要用@NativeMethod注解暴露，未注解的方法不允许调用
下面分别定义了
	- 同步函数add
	-  异步函数getUserInfo
	```kotlin
	/**
	 *
	 * @author chenrong
	 * @date 2023/4/7
	 */
	class CommonObj {
	
	    @NativeMethod
	    fun add(a:Int,b:int): Int {
	        return a+b
	    }
	    
	    @NativeMethod
	    fun getUserInfo(callback: WebCallback) {
	        thread {
	            Thread.sleep(1000)
	            callback.onNext("user name is sanron")
	        }
	    }
	}
	```
2. 注册对象类  
使用SBridge.addNativeObj方法注册上面定义好的对象
	```kotlin
	binding.webview.addNativeObj("common", CommonObj())
	```

3. Web端调用  
Web端在window上挂在了Native属性，在上面Android端注册的common类，会在Web端挂载到Native对象上，即Native.common
Web端调用方式如下
	```js
	//同步add
	var result = Native.common.add(1,2);
	
	//异步
	Native.common.getUserInfo(function(userInfo){
		console.log(userInfo)
	})
	```
### Native调用Web方法
1. Web端定义方法    
将要暴露的方法挂载到WebInterface对象上，参数固定为两个，第一个为android端传入的json，第二个是callback用于回传结果
	```js
	WebInterface.addEventListener = function(params, callback) {
	    window.addEventListener(params.event, function(event) {
	        callback(event,true)
	    })
	}
	```
2. Android端调用  
使用callWebMethod方法
	```kotlin 
	val args = JSONObject().apply {
	   put("event","click")
	}
	binding.webview.callWebMethod("addEventListener", args, object : NativeCallback {
	   override fun onNext(value: String?) {
	       Logger.d("addEvent callback,${value}")
	   }
	
	   override fun onCallError(errorMsg: String?) {
	   }
	})
	```

## Native对象
### 定义
```kotlin 
class Common{
   @NativeMethod
    fun fun1(){
    }

    @NativeMethod
    fun fun2(){
    }
    //..............
}
```

### 设置调用线程
通过设置@NativeMethod注解的async属性，配置是否异步执行
```kotlin
@NativeMethod(async = true)
fun asyncFun(){
}
```

### 参数类型
默认支持各类基础类型,JSONObject,JSONArray,WebCallback

### 回调WebCallback
定义异步方法时，使用传入的WebCallback参数进行回传值，WebCallback有两个方法
- onNext，回传值
- onRelease，释放callback，释放web端的function资源

在onRelease方法调用之前，onNext可一直使用
当前库使用了弱引用队列方法实现了WebCallback自动release，可以选择不调用onRelease依靠框架自己释放

### 自定义参数类型转换
支持将数据转为任意类型
1. ParameterConverter 接口
	```kotlin
	interface ParameterConverter<O> {
	
		/**
	     * input,通过JSONArray.get(key)方法获取到的原始值
	     * 
	     * 返回null 转换不成功
	     * 返回Result类对象 转换成功
	     */
	    fun convert(
	        input: Any?,
	        paramClazz: Class<*>,
	        annotations: Array<Annotation>
	    ): Result<O>?
	    
		 /**
	     * 返回true,支持paramClazz类型的参数转换
	     * false,不支持
	     */
	    fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean
	
	    class Result<O>(
	        val value: O? = null
	    )
	}
	```
2. 实现接口
	```kotlin
	//Gson转换为任意对象
	class GsonConverter(val gson: Gson = GsonBuilder().create()) : ParameterConverter<Any> {
	
	    override fun convert(
	        input: Any?,
	        paramClazz: Class<*>,
	        annotations: Array<Annotation>
	    ): ParameterConverter.Result<Any>? {
	        if (input == null || input == JSONObject.NULL) {
	            return ParameterConverter.Result(null)
	        }
	        kotlin.runCatching {
	            return ParameterConverter.Result(gson.fromJson(input.toString(), paramClazz))
	        }
	        return null
	    }
	
	    override fun handles(paramClazz: Class<*>, annotations: Array<Annotation>): Boolean {
	        return true
	    }
	}
	```

3. 添加转换器
	```kotlin
	binding.webview.addConverter(GsonConverter())
	```

