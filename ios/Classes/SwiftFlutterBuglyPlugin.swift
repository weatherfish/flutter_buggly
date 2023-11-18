import Flutter
import UIKit
import Bugly

public class SwiftFlutterBuglyPlugin: NSObject, FlutterPlugin {
    
    private var p_channel: String?
    private var p_version: String?
    private var p_isDebugMode: String?
    private var p_config: BuglyConfig?
    
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_bugly", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterBuglyPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "getPlatformVersion":
            result("iOS " + UIDevice.current.systemVersion)
        case "setUserId" :
            if let params = call.arguments as? NSDictionary,
               let userId = params["userId"] as? String {
                Bugly.setUserIdentifier(userId)
            }
            result(true)
        case "init":
            createConfigIfNeed()
            if let params = call.arguments as? NSDictionary,
               let isDebug = params["isDebug"] as? Int {
                p_config?.debugMode = (isDebug == 1)
            }
            Bugly.start(withAppId: "f328876846", config: p_config)
            result(true);
        case "setChannel":
            createConfigIfNeed()
            if let params = call.arguments as? NSDictionary,
               let channel = params["channel"] as? String {
                p_config?.channel = channel
            }
            result(true)
        case "setVersion":
            createConfigIfNeed()
            if let params = call.arguments as? NSDictionary,
               let channel = params["version"] as? String {
                p_config?.version = channel
            }
            result(true)
        case "setUserValue":
            if let params = call.arguments as? NSDictionary,
               let userInfo = params["userInfo"] as? [String: String] {
                userInfo.forEach { (key: String, value: String) in
                    Bugly.setUserValue(value, forKey: key)
                }
            }
            result(true)
        case "postException":
            if let params = call.arguments as? NSDictionary,
               let reason = params["crash_message"] as? String,
               let detail = params["crash_detail"] as? String {
                Bugly.reportException(withCategory: 4, name: "Flutter Exception", reason: reason, callStack: detail.split(separator: "\n"), extraInfo: Dictionary(), terminateApp: false);
            }
            result(true)
            
        case "flutterLog":
            if let params = call.arguments as? NSDictionary,
               let message = params["logMessage"] as? NSDictionary {
                
                if let msgInfo = message["LogInfo"] as? String,
                   let level = message["level"] as? String {
                    
                    switch level {
                    case "v":
                        BuglyLog.level(.verbose, logs: msgInfo)
                        break
                    case "d":
                        BuglyLog.level(.debug, logs: msgInfo)
                        break
                    case "i":
                        BuglyLog.level(.info, logs: msgInfo)
                        break
                    case "w":
                        BuglyLog.level(.warn, logs: msgInfo)
                        break
                    case "e":
                        BuglyLog.level(.error, logs: msgInfo)
                        break
                    default: break
                    }
                    
                }
                
            }
            
            result(true)
        default: break
        }
    }
    
    private func createConfigIfNeed() {
        if (p_config == nil) {
            p_config = BuglyConfig()
            p_config?.reportLogLevel = .verbose
            p_config?.applicationGroupIdentifier = "group.sg.partying.lcb.ios"
        }
    }
    
}

