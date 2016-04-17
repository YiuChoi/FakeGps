package name.caiyao.fakegps;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.Settings;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by 蔡小木 on 2016/4/17 0017.
 */
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", loadPackageParam.classLoader, "getString",
                ContentResolver.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String requested = (String) param.args[1];
                        if (requested.equals(Settings.Secure.ALLOW_MOCK_LOCATION)) {
                            param.setResult("0");
                            XposedBridge.log("Hook android.provider.Settings.Secure");
                        }
                    }
                });

        // at API level 18, the function Location.isFromMockProvider is added
        if (Build.VERSION.SDK_INT >= 18) {
            XposedHelpers.findAndHookMethod("android.location.Location", loadPackageParam.classLoader, "isFromMockProvider",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false);
                            XposedBridge.log("android.location.Location");
                        }
                    });
        }
    }
}
