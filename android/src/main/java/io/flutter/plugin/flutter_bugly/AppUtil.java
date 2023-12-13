package flutter_bugly;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class AppUtil {
    public static String getMetadata(Context context, String name) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String value = String.valueOf(appInfo.metaData.get(name));
                Log.i("bugly", "in getMetadata name = " + name + ", value = " + value);
                return value;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
