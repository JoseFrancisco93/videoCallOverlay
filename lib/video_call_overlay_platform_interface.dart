import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'video_call_overlay_method_channel.dart';

abstract class VideoCallOverlayPlatform extends PlatformInterface {
  /// Constructs a VideoCallOverlayPlatform.
  VideoCallOverlayPlatform() : super(token: _token);

  static final Object _token = Object();

  static VideoCallOverlayPlatform _instance = MethodChannelVideoCallOverlay();

  /// The default instance of [VideoCallOverlayPlatform] to use.
  ///
  /// Defaults to [MethodChannelVideoCallOverlay].
  static VideoCallOverlayPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [VideoCallOverlayPlatform] when
  /// they register themselves.
  static set instance(VideoCallOverlayPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
