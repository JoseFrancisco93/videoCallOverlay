
import 'video_call_overlay_platform_interface.dart';

class VideoCallOverlay {
  Future<String?> getPlatformVersion() {
    return VideoCallOverlayPlatform.instance.getPlatformVersion();
  }
}
