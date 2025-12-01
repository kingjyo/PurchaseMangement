# Purchase Management App

구매 관리 앱 - 사무실 부품 주문을 위한 Android 애플리케이션

This is a purchase management app for my office. Previously, they ordered parts by voice, not text, which was difficult. I made this app for easy use, especially for older users who need a simple interface.

---

**📖 빠른 시작 (Quick Start):** [한국어 빠른 시작 가이드](QUICKSTART_KR.md)  
**🔧 문제 해결 (Troubleshooting):** [한국어 문제 해결 가이드](TROUBLESHOOTING_KR.md)

---

## Project Structure

This is an Android application built with:
- **Kotlin** as the primary language
- **Gradle 8.11.1** for build management
- **Android Gradle Plugin 8.4.2**
- **Firebase** for backend services
- **Node.js/npm** for Firebase Functions

## Prerequisites

- **JDK 17** or higher
- **Android SDK** (API level 24 or higher)
- **Android Studio** (recommended) or IntelliJ IDEA
- **Node.js** and **npm** (for Firebase Functions)
- **Git**

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/kingjyo/PurchaseMangement.git
cd PurchaseMangement
```

### 2. Verify Gradle Build Files

**Quick Verification (Recommended):**

Run the verification script to check if all required files are present:

```bash
# Windows
verify_gradle_setup.bat

# Linux/Mac
./verify_gradle_setup.sh
```

**Manual Verification:**

Ensure the following files exist in your project root:
- `settings.gradle.kts` - Gradle settings file
- `build.gradle.kts` - Root build configuration
- `app/build.gradle.kts` - App module build configuration
- `gradlew` and `gradlew.bat` - Gradle wrapper scripts
- `gradle/wrapper/gradle-wrapper.properties` - Wrapper configuration
- `gradle/wrapper/gradle-wrapper.jar` - Wrapper JAR

If any files are missing, pull the latest changes:
```bash
git pull origin main
```

### 3. Build the Project

#### On Windows:
```bash
gradlew.bat build
```

#### On Linux/Mac:
```bash
./gradlew build
```

### 4. Setup Firebase Functions (Optional)

If working with Firebase Functions:

```bash
cd functions
npm install
```

## Development

### Running the App

#### From Android Studio:
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select a device/emulator
4. Click Run (Shift+F10)

#### From Command Line:

**Build Debug APK:**
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

**Install on Connected Device:**
```bash
# Windows
gradlew.bat installDebug

# Linux/Mac
./gradlew installDebug
```

### Running Tests

```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

### Firebase Functions Linting

```bash
cd functions
npm run lint
```

## Troubleshooting

**For detailed troubleshooting in Korean, see [TROUBLESHOOTING_KR.md](TROUBLESHOOTING_KR.md)**

### Error: "Directory does not contain a Gradle build"

This error means Gradle cannot find the required build files. Try these solutions:

1. **Verify you're in the correct directory:**
   ```bash
   # You should be in the root directory where settings.gradle.kts exists
   dir settings.gradle.kts  # Windows
   ls -la settings.gradle.kts  # Linux/Mac
   ```

2. **Pull the latest changes:**
   ```bash
   git fetch origin
   git pull origin main
   ```

3. **Check if Gradle files are present:**
   ```bash
   # Windows
   dir *.kts
   dir gradle\wrapper\

   # Linux/Mac
   ls -la *.kts
   ls -la gradle/wrapper/
   ```

4. **Re-clone the repository** if files are still missing:
   ```bash
   cd ..
   git clone https://github.com/kingjyo/PurchaseMangement.git
   cd PurchaseMangement
   ```

5. **Clean and rebuild:**
   ```bash
   # Windows
   gradlew.bat clean build

   # Linux/Mac
   ./gradlew clean build
   ```

### Error: "Could not resolve dependencies"

This usually indicates a network issue. Solutions:

1. **Check your internet connection**
2. **Use a VPN** if certain repositories are blocked
3. **Wait and retry** - repository servers may be temporarily down

### Android Studio Cannot Import Project

1. **Close Android Studio**
2. **Delete `.idea` folder** and `.gradle` folder in project root
3. **Reopen the project** in Android Studio
4. **Let Gradle sync complete**

### Gradle Daemon Issues

```bash
# Stop all Gradle daemons
gradlew.bat --stop  # Windows
./gradlew --stop    # Linux/Mac

# Then try building again
```

## Project Configuration

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)
- **Compile SDK:** 35
- **JVM Target:** 17
- **Kotlin Version:** 2.1.0

## Additional Resources

- [Gradle Documentation](https://docs.gradle.org/)
- [Android Developer Guide](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase Documentation](https://firebase.google.com/docs)