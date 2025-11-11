#!/bin/bash
# Gradle Setup Verification Script for Linux/Mac
# This script checks if all required Gradle build files are present

echo "========================================"
echo "Gradle Build Setup Verification"
echo "========================================"
echo ""

ERROR_COUNT=0

echo "Checking required files..."
echo ""

# Check settings.gradle.kts
if [ -f "settings.gradle.kts" ]; then
    echo "[OK] settings.gradle.kts found"
else
    echo "[ERROR] settings.gradle.kts NOT FOUND"
    ((ERROR_COUNT++))
fi

# Check build.gradle.kts
if [ -f "build.gradle.kts" ]; then
    echo "[OK] build.gradle.kts found"
else
    echo "[ERROR] build.gradle.kts NOT FOUND"
    ((ERROR_COUNT++))
fi

# Check app/build.gradle.kts
if [ -f "app/build.gradle.kts" ]; then
    echo "[OK] app/build.gradle.kts found"
else
    echo "[ERROR] app/build.gradle.kts NOT FOUND"
    ((ERROR_COUNT++))
fi

# Check gradlew.bat
if [ -f "gradlew.bat" ]; then
    echo "[OK] gradlew.bat found"
else
    echo "[ERROR] gradlew.bat NOT FOUND"
    ((ERROR_COUNT++))
fi

# Check gradlew
if [ -f "gradlew" ]; then
    echo "[OK] gradlew found"
    # Check if it's executable
    if [ -x "gradlew" ]; then
        echo "     gradlew is executable"
    else
        echo "     [WARNING] gradlew is not executable, fixing..."
        chmod +x gradlew
    fi
else
    echo "[ERROR] gradlew NOT FOUND"
    ((ERROR_COUNT++))
fi

# Check gradle wrapper jar
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "[OK] gradle/wrapper/gradle-wrapper.jar found"
else
    echo "[ERROR] gradle/wrapper/gradle-wrapper.jar NOT FOUND"
    ((ERROR_COUNT++))
fi

# Check gradle wrapper properties
if [ -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    echo "[OK] gradle/wrapper/gradle-wrapper.properties found"
else
    echo "[ERROR] gradle/wrapper/gradle-wrapper.properties NOT FOUND"
    ((ERROR_COUNT++))
fi

echo ""
echo "========================================"
echo "Summary"
echo "========================================"

if [ $ERROR_COUNT -eq 0 ]; then
    echo "[SUCCESS] All required Gradle files are present!"
    echo ""
    echo "You can now build the project with:"
    echo "  ./gradlew build"
    echo ""
    echo "Or open it in Android Studio."
    exit 0
else
    echo "[FAILED] $ERROR_COUNT required file(s) missing!"
    echo ""
    echo "Please try the following:"
    echo "1. Pull the latest code: git pull origin main"
    echo "2. Re-clone the repository if files are still missing"
    echo "3. Check TROUBLESHOOTING_KR.md for detailed help"
    echo ""
    exit 1
fi
