import 'dart:async';
import 'package:flutter/services.dart';

class Bdmap {
  static const MethodChannel _channel =
      const MethodChannel('com.reemii.driver.channel.bamap');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  //初始话
  static void init({String token, String traceId, String staffId}) async {
    await _channel.invokeMethod(
        'init', { 'traceId': traceId, 'staffId': staffId});
  }
}
