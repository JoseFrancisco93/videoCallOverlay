import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:video_call_overlay/video_call_overlay_method_channel.dart';

void main() {
  MethodChannelVideoCallOverlay platform = MethodChannelVideoCallOverlay();
  const MethodChannel channel = MethodChannel('video_call_overlay');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
