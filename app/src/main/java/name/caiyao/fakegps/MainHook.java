package name.caiyao.fakegps;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by 蔡小木 on 2016/4/17 0017.
 */
public class MainHook implements IXposedHookLoadPackage {

    public static boolean isHook = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        final Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        Uri uri = Uri.parse("content://name.caiyao.fakegps.SettingProvider/fake");
        Cursor cursor = systemContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex("key"));
            String value = cursor.getString(cursor.getColumnIndex("value"));
            if (key.equals("hook")) {
                if (value.equals("1")) {
                    isHook = true;
                } else if (value.equals("0")) {
                    isHook = false;
                }
            }
            cursor.close();
        }else{
            XposedBridge.log("cusor is null");
        }

        if ((loadPackageParam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
            return;
        XposedBridge.log(loadPackageParam.packageName + ",hook;" + isHook);
        if (!isHook) {
            return;
        }
        XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", loadPackageParam.classLoader, "getString",
                ContentResolver.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String requested = (String) param.args[1];
                        if (requested.equals(Settings.Secure.ALLOW_MOCK_LOCATION)) {
                            param.setResult("0");
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
                        }
                    });
        }

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getNetworkOperatorName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getNetworkOperator", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getSimOperatorName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getSimOperator", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getSimCountryIso", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getNetworkCountryIso", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getNetworkType", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(0);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getPhoneType", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(0);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getCurrentPhoneType", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(0);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getDataState", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(2);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getSimState", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(0);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(0);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(0, 0);
                        param.setResult(gsmCellLocation);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", loadPackageParam.classLoader,
                "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
                        gsmCellLocation.setLacAndCid(0, 0);
                        param.setResult(gsmCellLocation);
                    }
                });

        if (Build.VERSION.SDK_INT > 22) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                    "getPhoneCount", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(1);
                        }
                    });
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                    "getNeighboringCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(new ArrayList<>());
                        }
                    });
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPackageParam.classLoader,
                    "getAllCellInfo", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(new ArrayList<>());
                        }
                    });
            XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", loadPackageParam.classLoader,
                    "onCellInfoChanged", List.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(new ArrayList<>());
                        }
                    });
        }

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", loadPackageParam.classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(new ArrayList<>());
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", loadPackageParam.classLoader, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(3);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", loadPackageParam.classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPackageParam.classLoader, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-E0");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPackageParam.classLoader, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPackageParam.classLoader, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });


        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", loadPackageParam.classLoader,
                "getTypeName", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult("WIFI");
                    }
                });
        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", loadPackageParam.classLoader,
                "isConnectedOrConnecting", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", loadPackageParam.classLoader,
                "isConnected", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.net.NetworkInfo", loadPackageParam.classLoader,
                "isAvailable", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });

        XposedHelpers.findAndHookMethod("android.telephony.CellInfo", loadPackageParam.classLoader,
                "isRegistered", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });
    }
}
