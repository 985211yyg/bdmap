import 'dart:async';
import 'dart:html';

import 'package:flutter/services.dart';

class Bdmap {
  static const MethodChannel _channel =
      const MethodChannel('com.reemii.driver.channel.bamap');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static void init(String token) async {
    await _channel.invokeMethod('init', {'token': token});
  }

}
