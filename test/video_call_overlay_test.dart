import 'package:flutter_test/flutter_test.dart';
import 'package:video_call_overlay/video_call_overlay.dart';
import 'package:video_call_overlay/video_call_overlay_platform_interface.dart';
import 'package:video_call_overlay/video_call_overlay_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockVideoCallOverlayPlatform
    with MockPlatformInterfaceMixin
    implements VideoCallOverlayPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final VideoCallOverlayPlatform initialPlatform = VideoCallOverlayPlatform.instance;

  test('$MethodChannelVideoCallOverlay is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelVideoCallOverlay>());
  });

  test('getPlatformVersion', () async {
    VideoCallOverlay videoCallOverlayPlugin = VideoCallOverlay();
    MockVideoCallOverlayPlatform fakePlatform = MockVideoCallOverlayPlatform();
    VideoCallOverlayPlatform.instance = fakePlatform;

    expect(await videoCallOverlayPlugin.getPlatformVersion(), '42');
  });
}
