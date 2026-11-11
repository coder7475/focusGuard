const { withAndroidManifest, withDangerousMod, withStringsXml } = require('expo/config-plugins');
const fs = require('fs');
const path = require('path');

module.exports = function withFocusEnforcer(config) {
  // 1. Inject Android Permissions and Service Declarations
  config = withAndroidManifest(config, async (config) => {
    const androidManifest = config.modResults.manifest;

    // Required Permissions
    const permissions = [
      'android.permission.FOREGROUND_SERVICE',
      'android.permission.FOREGROUND_SERVICE_SPECIAL_USE',
      'android.permission.SCHEDULE_EXACT_ALARM',
      'android.permission.POST_NOTIFICATIONS',
      'android.permission.RECEIVE_BOOT_COMPLETED',
      'android.permission.USE_FULL_SCREEN_INTENT',
      'android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS',
      'android.permission.SYSTEM_ALERT_WINDOW'
    ];

    if (!androidManifest['uses-permission']) {
      androidManifest['uses-permission'] = [];
    }

    permissions.forEach((perm) => {
      const exists = androidManifest['uses-permission'].some(
        (p) => p.$['android:name'] === perm
      );
      if (!exists) {
        androidManifest['uses-permission'].push({ $: { 'android:name': perm } });
      }
    });

    const app = androidManifest.application[0];
    if (!app.service) app.service = [];
    if (!app.receiver) app.receiver = [];

    // Accessibility Service Declaration
    app.service.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.FocusAccessibilityService',
        'android:permission': 'android.permission.BIND_ACCESSIBILITY_SERVICE',
        'android:exported': 'true'
      },
      'intent-filter': [
        {
          action: [
            { $: { 'android:name': 'android.accessibilityservice.AccessibilityService' } }
          ]
        }
      ],
      'meta-data': [
        {
          $: {
            'android:name': 'android.accessibilityservice',
            'android:resource': '@xml/accessibility_service_config'
          }
        }
      ]
    });

    // Foreground Ticker Service Declaration
    app.service.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.FocusForegroundService',
        'android:foregroundServiceType': 'specialUse',
        'android:exported': 'false'
      },
      property: [
        {
          $: {
            'android:name': 'android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE',
            'android:value': 'Focus lock countdown ticker and state preservation'
          }
        }
      ]
    });

    // Device Boot Completed Receiver Declaration
    app.receiver.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.BootReceiver',
        'android:exported': 'true'
      },
      'intent-filter': [
        {
          action: [
            { $: { 'android:name': 'android.intent.action.BOOT_COMPLETED' } },
            { $: { 'android:name': 'android.intent.action.LOCKED_BOOT_COMPLETED' } }
          ]
        }
      ]
    });

    // Exact Alarm Receiver Declaration
    app.receiver.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.AlarmReceiver',
        'android:exported': 'false'
      }
    });

    // Package Visibility Queries (Android 11+ / API 30+)
    if (!androidManifest.queries) {
      androidManifest.queries = [];
    }
    if (androidManifest.queries.length === 0) {
      androidManifest.queries.push({});
    }

    const queriesObj = androidManifest.queries[0];
    if (!queriesObj.intent) queriesObj.intent = [];
    if (!queriesObj.package) queriesObj.package = [];

    const hasHomeIntent = queriesObj.intent.some((i) =>
      i.action?.some((a) => a.$ && a.$['android:name'] === 'android.intent.action.MAIN') &&
      i.category?.some((c) => c.$ && c.$['android:name'] === 'android.intent.category.HOME')
    );
    if (!hasHomeIntent) {
      queriesObj.intent.push({
        action: [{ $: { 'android:name': 'android.intent.action.MAIN' } }],
        category: [{ $: { 'android:name': 'android.intent.category.HOME' } }],
      });
    }

    const hasLauncherIntent = queriesObj.intent.some((i) =>
      i.action?.some((a) => a.$ && a.$['android:name'] === 'android.intent.action.MAIN') &&
      i.category?.some((c) => c.$ && c.$['android:name'] === 'android.intent.category.LAUNCHER')
    );
    if (!hasLauncherIntent) {
      queriesObj.intent.push({
        action: [{ $: { 'android:name': 'android.intent.action.MAIN' } }],
        category: [{ $: { 'android:name': 'android.intent.category.LAUNCHER' } }],
      });
    }

    const hasDialIntent = queriesObj.intent.some((i) =>
      i.action?.some((a) => a.$ && a.$['android:name'] === 'android.intent.action.DIAL')
    );
    if (!hasDialIntent) {
      queriesObj.intent.push({
        action: [{ $: { 'android:name': 'android.intent.action.DIAL' } }],
      });
    }

    const hasSmsIntent = queriesObj.intent.some((i) =>
      i.action?.some((a) => a.$ && a.$['android:name'] === 'android.intent.action.SENDTO')
    );
    if (!hasSmsIntent) {
      queriesObj.intent.push({
        action: [{ $: { 'android:name': 'android.intent.action.SENDTO' } }],
        data: [{ $: { 'android:scheme': 'smsto' } }],
      });
    }

    const targetOemPackages = [
      'com.transsion.XOSLauncher',
      'com.transsion.hilauncher',
      'com.transsion.smartcaller',
    ];
    targetOemPackages.forEach((pkgName) => {
      const exists = queriesObj.package.some((p) => p.$ && p.$['android:name'] === pkgName);
      if (!exists) {
        queriesObj.package.push({ $: { 'android:name': pkgName } });
      }
    });

    return config;
  });

  // 2. Add description string for Accessibility Service
  config = withStringsXml(config, (config) => {
    const strings = config.modResults.resources.string || [];
    const exists = strings.some((s) => s.$ && s.$.name === 'accessibility_service_description');
    if (!exists) {
      strings.push({
        $: { name: 'accessibility_service_description' },
        _: 'SleepGuard accessibility service enforces focus locks and blocks non-allowlisted apps during an active session.'
      });
      config.modResults.resources.string = strings;
    }
    return config;
  });

  // 3. Write accessibility_service_config.xml resource file
  config = withDangerousMod(config, [
    'android',
    async (config) => {
      const resXmlDir = path.join(
        config.modRequest.platformProjectRoot,
        'app/src/main/res/xml'
      );
      fs.mkdirSync(resXmlDir, { recursive: true });

      const xmlContent = `<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100" />
`;
      fs.writeFileSync(path.join(resXmlDir, 'accessibility_service_config.xml'), xmlContent);
      return config;
    }
  ]);

  // 4. Ensure ProGuard keep rules for native enforcer module
  config = withDangerousMod(config, [
    'android',
    async (config) => {
      const proguardPath = path.join(
        config.modRequest.platformProjectRoot,
        'app/proguard-rules.pro'
      );
      if (fs.existsSync(proguardPath)) {
        let content = fs.readFileSync(proguardPath, 'utf8');
        const rule = '-keep class com.coder7575.sleepguard.enforcer.** { *; }';
        if (!content.includes(rule)) {
          content += `\n# FocusGuard Native Enforcer Module\n-keep class com.coder7575.sleepguard.enforcer.** { *; }\n-keepclassmembers class com.coder7575.sleepguard.enforcer.** { *; }\n`;
          fs.writeFileSync(proguardPath, content);
        }
      }
      return config;
    }
  ]);

  return config;
};
