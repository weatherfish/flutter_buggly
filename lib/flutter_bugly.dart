import 'dart:io';

import 'flutter_bugly_platform_interface.dart';

class FlutterBugly {
  static final FlutterBugly instance = FlutterBugly();

  Future<String?> getPlatformVersion() {
    return FlutterBuglyPlatform.instance.getPlatformVersion();
  }

  /*
  * 设置用户uid
  * */
  Future<void> setUserId(String? userId) async {
    await FlutterBuglyPlatform.instance.setUserId(userId);
  }

  /*
  * 初始化
  * */
  Future<void> init(String androidAppId, String iosAppId, bool? isDebug) async {
    if (Platform.isAndroid) {
      await FlutterBuglyPlatform.instance.init(androidAppId, isDebug ?? false);
    } else if (Platform.isIOS) {
      await FlutterBuglyPlatform.instance.init(iosAppId, isDebug ?? false);
    }
  }

  /*
  * 设置用户其他信息
  * */
  Future<void> setUserValue(String? key, String? value) async {
    await FlutterBuglyPlatform.instance.setUserValue(key, value);
  }

  Future<void> setChannel(String? channel) async {
    await FlutterBuglyPlatform.instance.setChannel(channel);
  }

  Future<void> setVersion(String? version) async {
    await FlutterBuglyPlatform.instance.setVersion(version);
  }

  Future<void> setDeviceModel(String? deviceModel) async {
    await FlutterBuglyPlatform.instance.setDeviceModel(deviceModel);
  }

  Future<void> reportError(dynamic error, dynamic stackTrace) async {
    await FlutterBuglyPlatform.instance.reportFlutterError(error, stackTrace);
  }

  Future<void> reportFlutterLog(Map info) async {
    await FlutterBuglyPlatform.instance.reportFlutterLog(info);
  }
}
