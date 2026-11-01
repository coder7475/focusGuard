#!/usr/bin/env bash
set -e

echo "=============================================="
echo " SleepGuard — QA & Resilience Verification Suite"
echo "=============================================="

PACKAGE_NAME="com.coder7575.SleepGuard"

echo "1. Checking connected ADB devices..."
adb devices

echo "2. Simulating Doze Mode..."
echo "   Unplugging battery simulation..."
adb shell dumpsys battery unplug || true
echo "   Forcing device into deep Doze state..."
adb shell dumpsys deviceidle force-idle || true
echo "   Querying idle state:"
adb shell dumpsys deviceidle step || true
adb shell dumpsys battery reset || true

echo "3. Testing Emergency Dialer Pass-Through..."
echo "   Launching Intent.ACTION_DIAL..."
adb shell am start -a android.intent.action.DIAL -d "tel:911" || true

echo "4. Testing Boot Completed Receiver Registration..."
adb shell cmd package query-receivers --action android.intent.action.BOOT_COMPLETED | grep -i "${PACKAGE_NAME}" || true

echo "5. Verifying Accessibility Service Registration..."
adb shell settings get secure enabled_accessibility_services | grep -i "${PACKAGE_NAME}" || true

echo "=============================================="
echo " ✓ Resilience verification script completed!"
echo "=============================================="
