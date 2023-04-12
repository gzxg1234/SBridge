package com.sanron.jsbridge.obj

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.Toast
import com.sanron.jsbridge.annotation.NativeMethod
import com.sanron.jsbridge.json
import com.sanron.jsbridge.web.WebCallback
import org.json.JSONObject

/**
 *
 * @author chenrong
 * @date 2023/4/10
 */
class UIObj(val context: Context) {


    private val dialogMap = mutableMapOf<Int, Dialog>()

    @NativeMethod(async = false)
    fun toast(obj: JSONObject) {
        Toast.makeText(context, obj.optString("msg"), Toast.LENGTH_SHORT)
            .show()
    }

    @NativeMethod(async = false)
    fun dismissAlert(obj: JSONObject) {
        dialogMap.remove(obj.optInt("id"))?.dismiss()
    }

    @NativeMethod(async = false)
    fun showAlert(obj: JSONObject, callback: WebCallback): Int {
        val dialog = AlertDialog.Builder(context).run {
            if (obj.has("leftButton")) {
                setNegativeButton(obj.optString("leftButton"), null)
            }
            if (obj.has("rightButton")) {
                setPositiveButton(obj.optString("rightButton"), null)
            }
            setMessage(obj.optString("message"))
            setOnDismissListener {
                dialogMap.remove(it.hashCode())
                callback.onNext(
                    json(
                        "event" to "onDismiss"
                    ).toString()
                )
            }
            if (obj.has("title")) {
                setTitle(obj.optString("title"))
            }
            setCancelable(obj.optBoolean("cancelable", true))
            create()
        }
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
            callback.onNext(
                json(
                    "event" to "onClick",
                    "button" to "left",
                ).toString()
            )
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            callback.onNext(
                json(
                    "event" to "onClick",
                    "button" to "right",
                ).toString()
            )
        }
        return dialog.hashCode().also {
            dialogMap[it] = dialog
        }
    }

}