"""Verify real process-death persistence on a disposable emulator, without network audio."""
import os
import re
import subprocess

ADB = ["adb", "-s", os.environ.get("ANDROID_SERIAL", "emulator-5554")]
PACKAGE = "org.erthi.rahma"
for apk in ("app/build/outputs/apk/debug/app-debug.apk",
            "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"):
    subprocess.run(ADB + ["install", "-r", "-t", apk], check=True, timeout=120)

for method in ("prepare", "verify"):
    result = subprocess.run(ADB + ["shell", "am", "instrument", "-w", "-r", "-e", "class",
        f"{PACKAGE}.ProcessRestartProbe#{method}",
        f"{PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner"],
        capture_output=True, text=True, check=True, timeout=120)
    output = result.stdout + result.stderr
    print(output, flush=True)
    if not re.search(r"OK\s*\(1 test\)", output) or "FAILURES!!!" in output:
        raise RuntimeError(f"Restart probe {method} did not pass exactly one test")
    if method == "prepare":
        for package in (PACKAGE, PACKAGE + ".test"):
            subprocess.run(ADB + ["shell", "am", "force-stop", package], check=True, timeout=30)
print("Draft and playback position survived process death.", flush=True)
