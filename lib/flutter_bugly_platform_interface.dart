import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_bugly_method_channel.dart';

abstract class FlutterBuglyPlatform extends PlatformInterface {
  /// Constructs a FlutterBuglyPlatform.
  FlutterBuglyPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterBuglyPlatform _instance = MethodChannelFlutterBugly();

  /// The default instance of [FlutterBuglyPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterBugly].
  static FlutterBuglyPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterBuglyPlatform] when
  /// they register themselves.
  static set instance(FlutterBuglyPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setUserId(String? userId) {
    throw UnimplementedError('setUserId() has not been implemented.');
  }

  Future<void> setChannel(String? channel) {
    throw UnimplementedError('setChannel() has not been implemented.');
  }

  Future<void> setVersion(String? version) {
    throw UnimplementedError('setVersion() has not been implemented.');
  }

  Future<void> init(String appId, bool isDebug) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future<void> setUserValue(String? key, String? value) async {
    throw UnimplementedError('setUserValue() has not been implemented.');
  }
  Future<void> setDeviceModel(String? deviceModel) async {
    throw UnimplementedError('setDeviceModel() has not been implemented.');
  }
  Future<void> reportFlutterError(dynamic error, dynamic stackTrace) async {
    throw UnimplementedError('reportFlutterError() has not been implemented.');
  }

  Future<void> reportFlutterLog(Map logInfo) async {
    throw UnimplementedError('reportFlutterError() has not been implemented.');
  }
}
