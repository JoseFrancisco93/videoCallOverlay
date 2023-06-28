import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'video_call_overlay_platform_interface.dart';

/// An implementation of [VideoCallOverlayPlatform] that uses method channels.
class MethodChannelVideoCallOverlay extends VideoCallOverlayPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('video_call_overlay');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
