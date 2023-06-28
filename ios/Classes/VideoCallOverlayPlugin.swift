import Flutter
import UIKit

public class VideoCallOverlayPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "video_call_overlay", binaryMessenger: registrar.messenger())
    let instance = VideoCallOverlayPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("Version de iOS " + UIDevice.current.systemVersion)
  }
}
