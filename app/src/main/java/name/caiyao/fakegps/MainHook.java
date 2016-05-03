package name.caiyao.fakegps;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.Settings;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by 蔡小木 on 2016/4/17 0017.
 */
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.contains("tencent") && !loadPackageParam.packageName.contains("alibaba") && !loadPackageParam.packageName.contains("baidu"))
            return;
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
                            param.setResult(getCell(0, 0, 0, 0, 0, 0));
                        }
                    });
            XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", loadPackageParam.classLoader,
                    "onCellInfoChanged", List.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(getCell(0, 0, 0, 0, 0, 0));
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

    private static ArrayList getCell(int mcc, int mnc, int lac, int cid, int sid, int networkType) {
        ArrayList arrayList = new ArrayList();
        CellInfoGsm cellInfoGsm = (CellInfoGsm) XposedHelpers.newInstance(CellInfoGsm.class);
        XposedHelpers.callMethod(cellInfoGsm, "setCellIdentity", XposedHelpers.newInstance(CellIdentityGsm.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(
                lac), Integer.valueOf(cid)}));
        CellInfoCdma cellInfoCdma = (CellInfoCdma) XposedHelpers.newInstance(CellInfoCdma.class);
        XposedHelpers.callMethod(cellInfoCdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityCdma.class, new Object[]{Integer.valueOf(lac), Integer.valueOf(sid), Integer.valueOf(cid), Integer.valueOf(0), Integer.valueOf(0)}));
        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) XposedHelpers.newInstance(CellInfoWcdma.class);
        XposedHelpers.callMethod(cellInfoWcdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityWcdma.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid), Integer.valueOf(300)}));
        CellInfoLte cellInfoLte = (CellInfoLte) XposedHelpers.newInstance(CellInfoLte.class);
        XposedHelpers.callMethod(cellInfoLte, "setCellIdentity", XposedHelpers.newInstance(CellIdentityLte.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(cid), Integer.valueOf(300), Integer.valueOf(lac)}));
        switch (networkType) {
            case 1:
            case 2:
                arrayList.add(cellInfoGsm);
                break;
            case 13:
                arrayList.add(cellInfoLte);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 12:
            case 14:
                arrayList.add(cellInfoCdma);
                break;
            case 3:
            case 8:
            case 9:
            case 10:
            case 15:
                arrayList.add(cellInfoWcdma);
                break;
        }
        return arrayList;
    }
}
