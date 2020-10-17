package me.singleneuron.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.singleneuron.base.Conditional
import me.singleneuron.base.bridge.CardMsgList
import me.singleneuron.data.CardMsgCheckResult
import nil.nadph.qnotified.BuildConfig
import nil.nadph.qnotified.MainHook
import nil.nadph.qnotified.R
import nil.nadph.qnotified.activity.EulaActivity
import nil.nadph.qnotified.activity.OmegaTestFuncActivity
import nil.nadph.qnotified.hook.BaseDelayableHook
import nil.nadph.qnotified.ui.ViewBuilder.newListItemHookSwitchInit
import nil.nadph.qnotified.util.LicenseStatus
import nil.nadph.qnotified.util.Utils
import java.io.BufferedReader
import java.io.File
import java.io.IOException

fun ViewGroup.addViewConditionally(view: View, condition: Boolean) {
    if (condition) {
        this.addView(view)
    }
}

fun <T> ViewGroup.addViewConditionally(context: Context, title: String, desc: String, hook: T) where T : BaseDelayableHook, T : Conditional {
    addViewConditionally(newListItemHookSwitchInit(context, title, desc, hook), hook.condition)
}

@Throws(IOException::class)
fun readFile(file: File): String {
    return file.readText()
}

@Throws(IOException::class)
fun readFromBufferedReader(bufferedReader: BufferedReader): String {
    return bufferedReader.readText()
}

fun Intent.dump() {
    dumpIntent(this)
}

fun dumpIntent(intent: Intent) {
    Utils.logd(intent.toString())
    Utils.logd(intent.extras.toString())
    Utils.logd(Log.getStackTraceString(Throwable()))
}

fun checkCardMsg(originString: String): CardMsgCheckResult {
    return CardMsgCheckResult(true)

}

private fun decodePercent(string:String): String {
    var produceString = string
    val regex = Regex("""%[0-9a-fA-F]{2}""",RegexOption.IGNORE_CASE)
    while (true) {
        if (!regex.containsMatchIn(produceString)) return produceString
        produceString = regex.replace(produceString){matchResult ->
            val hex = matchResult.value.substring(1)
            try {
                val char = Integer.valueOf(hex,16).toChar().toString()
                Utils.logd("replace $hex -> $char")
                return@replace char
            } catch (e:Exception) {
                Utils.log(e)
                return@replace hex
            }
        }
        Utils.logd("processing string: $produceString")
        //Thread.sleep(1000)
    }
}

fun showEulaDialog(activity: Activity) {
    if (BuildConfig.DEBUG) {
        MainHook.startProxyActivity(activity,OmegaTestFuncActivity::class.java)
        return
    }
    val linearLayout = LinearLayout(activity)
    linearLayout.orientation = LinearLayout.VERTICAL
    val textView = TextView(activity)
    textView.text = "此插件已被修改，去除了内置的消息上报功能跟卡片消息管制功能的监控。\n当然希望你不要到处传播此版本，防范小学生人人有责。\n我并未删除群发消息的限制，依旧只能发5字，毕竟正经人谁用群发啊。\n最后，此版本依旧适用开发者的用户协议，请您遵守。"
    textView.setTextColor(Color.RED)
    val editText = EditText(activity)
    editText.isEnabled = false
    editText.visibility = View.INVISIBLE
    linearLayout.addView(textView)
    linearLayout.addView(editText)
    val builder = MaterialAlertDialogBuilder(activity, R.style.MaterialDialog)
            .setView(linearLayout)
            .setCancelable(false)
            .setPositiveButton("我已阅读并同意用户协议"){ _: DialogInterface, _: Int ->
                MainHook.startProxyActivity(activity,OmegaTestFuncActivity::class.java)
            }
            .setNeutralButton("阅读用户协议"){ _: DialogInterface, _: Int ->
                MainHook.startProxyActivity(activity,EulaActivity::class.java)
                activity.finish()
            }
            .setNegativeButton("取消"){ _: DialogInterface, _: Int ->
            }
    val dialog = builder.create()
    dialog.show()
    val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            button.isEnabled = editText.text.toString() == "我已阅读并同意用户协议"
        }
    })
    button.isEnabled = true
    Thread {
        var time = 1
        do {
            Utils.runOnUiThread { button.text = "我已阅读并同意用户协议 ($time)" }
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } while (--time!=0)
    }.start()
}

