import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_bugly_platform_interface.dart';

/// An implementation of [FlutterBuglyPlatform] that uses method channels.
class MethodChannelFlutterBugly extends FlutterBuglyPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_bugly');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> setUserId(String? userId) async {
    if (userId == null) return;
    await methodChannel.invokeMethod('setUserId', {'userId': userId ?? ''});
  }

  @override
  Future<void> init(String appId, bool isDebug) async {
    await methodChannel.invokeMethod<void>('init', {'buglyAppId': appId, 'isDebug': isDebug ? 1 : 0});
  }

  @override
  Future<void> setUserValue(String? key, String? value) async {
    await methodChannel.invokeMethod<void>('setUserValue', {
      "userInfo": {key: value}
    });
  }

  @override
  Future<void> setChannel(String? channel) async {
    if (channel == null) return;
    await methodChannel.invokeMethod<void>('setChannel', {'channel': channel});
  }

  @override
  Future<void> setVersion(String? version) async {
    if (version == null) return;
    await methodChannel.invokeMethod<void>('setVersion', {'version': version});
  }

  @override
  Future<void> setDeviceModel(String? deviceModel) async {
    if (deviceModel == null) return;
    await methodChannel.invokeMethod<void>('setDeviceModel', {'deviceModel': deviceModel});
  }

  @override
  Future<void> reportFlutterError(dynamic error, dynamic stackTrace) async {
    if (error == null && stackTrace == null) return;
    await methodChannel.invokeMethod<void>(
        'postException', {'crash_message': error.toString(), 'crash_detail': stackTrace.toString()});
  }

  @override
  Future<void> reportFlutterLog(Map logInfo) async {
    if (logInfo.isEmpty) return;
    await methodChannel.invokeMethod<void>('flutterLog', {'logMessage': logInfo});
  }
}
