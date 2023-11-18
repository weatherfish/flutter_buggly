package flutter_bugly;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterBuglyPlugin
 */
public class FlutterBuglyPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private CrashReport.UserStrategy strategy;
    private Context applicationContext;
    private static final String TAG = "FlutterBuglyPlugin";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_bugly");
        channel.setMethodCallHandler(this);
        applicationContext = flutterPluginBinding.getApplicationContext();
        strategy = new CrashReport.UserStrategy(flutterPluginBinding.getApplicationContext());
    }

    private void createConfigIfNeed() {
        if (strategy == null) {
            strategy = new CrashReport.UserStrategy(applicationContext);
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "setUserId":
                String userId;
                if (call.hasArgument("userId")) {
                    userId = call.argument("userId");
                    CrashReport.setUserId(applicationContext, userId);
                }
                result.success(true);
                break;
            case "init":
                createConfigIfNeed();
                String buglyAppId = "85c5c29cae";
                boolean isDebug = BuildConfig.DEBUG;
                if (call.hasArgument("isDebug")) {
                    int debug = call.argument("isDebug");
                    isDebug = (debug == 1);
                }
                Log.d(TAG, "Bugly init: isDebug=" + isDebug);
                BuglyLog.setCache(30 * 1024);
                PackageManager pm = applicationContext.getPackageManager();
                strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
                    /**
                     * Crash处理.
                     *
                     * @param crashType 错误类型：CRASHTYPE_JAVA，CRASHTYPE_NATIVE，CRASHTYPE_U3D ,CRASHTYPE_ANR
                     * @param errorType 错误的类型名
                     * @param errorMessage 错误的消息
                     * @param errorStack 错误的堆栈
                     * @return 返回额外的自定义信息上报
                     */
                    public Map<String, String> onCrashHandleStart(int crashType, String errorType, String errorMessage, String errorStack) {
                        HashMap crashMap = collectDeviceInfo(applicationContext);
                        crashMap.put("crashType", crashType + "");
                        crashMap.put("errorType", errorType);
                        crashMap.put("errorMessage", errorMessage);
                        crashMap.put("errorStack", errorStack);
                        saveCrashInfo2File(crashMap);
                        return crashMap;
                    }

                    /**
                     * Crash处理.
                     *
                     * @param crashType 错误类型：CRASHTYPE_JAVA，CRASHTYPE_NATIVE，CRASHTYPE_U3D ,CRASHTYPE_ANR
                     * @param errorType 错误的类型名
                     * @param errorMessage 错误的消息
                     * @param errorStack 错误的堆栈
                     * @return byte[] 额外的2进制内容进行上报
                     */
                    @Override
                    public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType, String errorMessage, String errorStack) {
                        try {
                            return "Extra data.".getBytes("UTF-8");
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });

                try {
                    final PackageInfo info = pm.getPackageInfo(applicationContext.getPackageName(), 0);
                    String versionInfo = info.versionName + " (" + info.versionCode + ")";
                    CrashReport.setAppVersion(applicationContext, versionInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                CrashReport.initCrashReport(applicationContext, buglyAppId, isDebug, strategy);
                result.success(true);
                break;
            case "setChannel":
                createConfigIfNeed();
                if (call.hasArgument("channel")) {
                    strategy.setAppChannel(call.argument("channel"));
                }
                String channelName = AppUtil.getMetadata(applicationContext, "UMENG_CHANNEL");
                strategy.setAppChannel(channelName);
                result.success(true);
                break;

            case "setVersion":
                createConfigIfNeed();
                if (call.hasArgument("version")) {
                    strategy.setAppVersion(call.argument("version"));
                }
                result.success(true);
                break;
            case "setDeviceModel":
                createConfigIfNeed();
                if (call.hasArgument("deviceModel")) {
                    String deviceModel = call.argument("deviceModel");
                    strategy.setDeviceModel(deviceModel);
                    CrashReport.setDeviceModel(applicationContext, deviceModel);
                    Log.d(TAG, "Bugly isetDeviceModel=" + deviceModel);
                }
                result.success(true);
                break;
            case "postException":
                String message = call.argument("crash_message");
                String detail = call.argument("crash_detail"); //调用Bugly数据上报接口
                CrashReport.postException(4, "Flutter Exception", message, detail, null);
                result.success(true);
                break;
            case "setUserValue":
                if (call.hasArgument("userInfo")) {
                    HashMap<String, String> userInfoMap = call.argument("userInfo");
                    Iterator<Map.Entry<String, String>> iterator = userInfoMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> item = iterator.next();
                        Log.d(TAG, "Bugly setUserValue: key=" + item.getKey() + "||value=" + item.getValue());
                        CrashReport.putUserData(applicationContext, item.getKey(), item.getValue());
                    }
                    result.success(true);
                }

                break;
            case "flutterLog":
                if (call.hasArgument("logMessage")) {
                    HashMap<String, String> map = call.argument("logMessage");
                    if (map != null && !map.isEmpty()) {
                        String messageInfo = map.get("LogInfo");
                        String tag = map.get("tag");
                        String level = map.get("level");
                        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(level) || TextUtils.isEmpty(messageInfo))
                            return;
                        switch (level) {
                            case "FINE":
                                BuglyLog.v(tag, messageInfo);
                                result.success(true);
                                break;
                            case "INFO":
                                BuglyLog.i(tag, messageInfo);
                                result.success(true);
                                break;
                            case "WARNING":
                                BuglyLog.w(tag, messageInfo);
                                result.success(true);
                                break;
                            default:
                                break;
                        }
                    }

                }
                break;
            default:
                break;
        }


    }


    /**
     * 【说明】：收集应用参数信息
     */
    private HashMap collectDeviceInfo(Context ctx) {
        HashMap<String, String> infos = new HashMap<>();
        try {
            PackageManager pm = ctx.getPackageManager();//获取应用包管理者对象
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                String packageName = pi.packageName;
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
                infos.put("packageName", packageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occurred when collect package info...", e);
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (IllegalAccessException e) {
                Log.e(TAG, "an error occurred when collect crash info...", e);
            }
        }
        return infos;
    }

    /**
     * 【说明】：保存错误信息到指定文件中
     */
    private void saveCrashInfo2File(HashMap<String, String> infos) {
        StringBuffer sbf = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sbf.append(key + "=" + value + "\n");
        }
        try {
            //格式化日期，作为文件名的一部分
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = dateFormat.format(new Date());
            long timestamp = System.currentTimeMillis();
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File dir = FileUtil.getCrashDir(applicationContext);
                String filePath = dir.getAbsoluteFile() + File.separator + fileName;
                FileOutputStream fos = new FileOutputStream(filePath);
                Log.e(TAG, "log file path:" + filePath);
                fos.write(sbf.toString().getBytes());
                fos.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "an error occurred while find file...", e);
        } catch (IOException e) {
            Log.e(TAG, "an error occurred while writing file...", e);
        }

    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
