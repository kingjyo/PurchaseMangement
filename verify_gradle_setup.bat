@echo off
REM Gradle Setup Verification Script for Windows
REM This script checks if all required Gradle build files are present

echo ========================================
echo Gradle Build Setup Verification
echo ========================================
echo.

set ERROR_COUNT=0

echo Checking required files...
echo.

REM Check settings.gradle.kts
if exist "settings.gradle.kts" (
    echo [OK] settings.gradle.kts found
) else (
    echo [ERROR] settings.gradle.kts NOT FOUND
    set /a ERROR_COUNT+=1
)

REM Check build.gradle.kts
if exist "build.gradle.kts" (
    echo [OK] build.gradle.kts found
) else (
    echo [ERROR] build.gradle.kts NOT FOUND
    set /a ERROR_COUNT+=1
)

REM Check app/build.gradle.kts
if exist "app\build.gradle.kts" (
    echo [OK] app\build.gradle.kts found
) else (
    echo [ERROR] app\build.gradle.kts NOT FOUND
    set /a ERROR_COUNT+=1
)

REM Check gradlew.bat
if exist "gradlew.bat" (
    echo [OK] gradlew.bat found
) else (
    echo [ERROR] gradlew.bat NOT FOUND
    set /a ERROR_COUNT+=1
)

REM Check gradlew
if exist "gradlew" (
    echo [OK] gradlew found
) else (
    echo [ERROR] gradlew NOT FOUND
    set /a ERROR_COUNT+=1
)

REM Check gradle wrapper jar
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [OK] gradle\wrapper\gradle-wrapper.jar found
) else (
    echo [ERROR] gradle\wrapper\gradle-wrapper.jar NOT FOUND
    set /a ERROR_COUNT+=1
)

REM Check gradle wrapper properties
if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo [OK] gradle\wrapper\gradle-wrapper.properties found
) else (
    echo [ERROR] gradle\wrapper\gradle-wrapper.properties NOT FOUND
    set /a ERROR_COUNT+=1
)

echo.
echo ========================================
echo Summary
echo ========================================

if %ERROR_COUNT%==0 (
    echo [SUCCESS] All required Gradle files are present!
    echo.
    echo You can now build the project with:
    echo   gradlew.bat build
    echo.
    echo Or open it in Android Studio.
    exit /b 0
) else (
    echo [FAILED] %ERROR_COUNT% required file(s) missing!
    echo.
    echo Please try the following:
    echo 1. Pull the latest code: git pull origin main
    echo 2. Re-clone the repository if files are still missing
    echo 3. Check TROUBLESHOOTING_KR.md for detailed help
    echo.
    exit /b 1
)
