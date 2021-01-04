package me.nextalone.hook

import android.os.Looper
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import nil.nadph.qnotified.SyncUtils
import nil.nadph.qnotified.config.ConfigManager
import nil.nadph.qnotified.hook.BaseDelayableHook
import nil.nadph.qnotified.step.DexDeobfStep
import nil.nadph.qnotified.step.Step
import nil.nadph.qnotified.util.DexKit
import nil.nadph.qnotified.util.LicenseStatus
import nil.nadph.qnotified.util.Utils
import nil.nadph.qnotified.util.Utils.logi
import java.lang.reflect.Method

object RemoveIntimateDrawer : BaseDelayableHook() {
    private const val kr_remove_intimate_drawer: String = "kr_remove_intimate_drawer"
    var isInit = false

    override fun getPreconditions(): Array<Step> {
        return arrayOf(DexDeobfStep(DexKit.C_IntimateDrawer))
    }

    override fun init(): Boolean {
        if (isInited) return true
        return try {
            for (m: Method in DexKit.doFindClass(DexKit.C_IntimateDrawer).declaredMethods) {
                if (m.name == "a" && m.returnType == View::class.java) {
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam?) {
                            if (LicenseStatus.sDisableCommonHooks) return
                            if (!isEnabled) return
                            param?.result = null
                        }
                    })
                    logi("Remove Intimate Drawer completed")
                }
            }
            isInit = true
            true
        } catch (t: Throwable) {
            Utils.log(t)
            false
        }
    }

    override fun isEnabled(): Boolean {
        return try {
            ConfigManager.getDefaultConfig().getBooleanOrFalse(kr_remove_intimate_drawer)
        } catch (e: java.lang.Exception) {
            Utils.log(e)
            false
        }
    }

    override fun getEffectiveProc(): Int {
        return SyncUtils.PROC_MAIN
    }

    override fun setEnabled(enabled: Boolean) {
        try {
            val mgr = ConfigManager.getDefaultConfig()
            mgr.allConfig[kr_remove_intimate_drawer] = enabled
            mgr.save()
        } catch (e: Exception) {
            Utils.log(e)
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Utils.showToast(Utils.getApplication(), Utils.TOAST_TYPE_ERROR, e.toString() + "", Toast.LENGTH_SHORT)
            } else {
                SyncUtils.post { Utils.showToast(Utils.getApplication(), Utils.TOAST_TYPE_ERROR, e.toString() + "", Toast.LENGTH_SHORT) }
            }
        }
    }

    override fun isInited(): Boolean {
        return isInit
    }
}
