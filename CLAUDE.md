# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android purchase management application built in Kotlin for office use. The app is designed to be simple and accessible for elderly users who primarily use voice commands rather than text input. It integrates with Firebase for backend services and Google Sheets for data export.

**Major Refactoring Completed**: The entire codebase has been restructured, debugged, and optimized for stability and performance.

## Development Commands

### Android Build Commands
```bash
# Build debug APK (Windows)
gradlew.bat assembleDebug

# Build release APK (Windows) 
gradlew.bat assembleRelease

# Clean build
gradlew.bat clean

# Run all tests
gradlew.bat test

# Run instrumented tests
gradlew.bat connectedAndroidTest

# Compile Kotlin code only
gradlew.bat app:compileDebugKotlin
```

### Firebase Functions Commands
```bash
# Install dependencies (run in functions/ directory)
cd functions && npm install

# Lint Firebase functions
cd functions && npm run lint

# Deploy Firebase functions
cd functions && npm run deploy

# Start Firebase emulators
cd functions && npm run serve

# View Firebase function logs
cd functions && npm run logs
```

## Architecture Overview

### Refactored Architecture (2024)

The app now follows a clean architecture pattern with proper separation of concerns:

1. **Application Layer**:
   - `PurchaseManagementApp.kt`: Enhanced Application class with proper Firebase initialization
   - Global error handling and memory management
   - Centralized constants and configuration

2. **Base Components**:
   - `base/BaseActivity.kt`: Common functionality for all activities
   - `fragments/BaseVoiceFragment.kt`: Voice input capabilities
   - Centralized error handling and memory management

3. **Data Layer** (`data/models/`):
   - `User.kt`: Unified user model for all data sources
   - `PurchaseRequest.kt`: Comprehensive purchase request model
   - `Livestock.kt`: Livestock management model
   - All models support Firebase, Google Sheets, and local storage

4. **Network Layer** (`network/`):
   - `NetworkManager.kt`: Connection monitoring and retry logic
   - `RetryInterceptor.kt`: Automatic request retry with exponential backoff
   - `ApiResult.kt`: Standardized API response handling

5. **Utilities** (`utils/`):
   - `FirestoreHelper.kt`: Safe Firestore operations with proper error handling
   - `PermissionManager.kt`: Comprehensive permission management
   - `MemoryManager.kt`: Memory optimization and leak prevention

6. **Fragment System** (`fragments/`):
   - Unified fragment structure with consistent voice input
   - Memory-efficient image handling
   - Proper lifecycle management

7. **Key Activities**:
   - `MainActivity.kt`: Completely rewritten with proper error handling
   - `LoginActivity2.kt`: OAuth authentication
   - `PurchaseRequestActivityV2.kt`: Multi-step form with fragments
   - `PurchaseStatusActivityV2.kt`: Status management
   - All activities extend `BaseActivity` for consistency

## Key Improvements Made

1. **Stability Enhancements**:
   - Fixed all memory leaks and null pointer exceptions
   - Proper lifecycle management throughout the app
   - Comprehensive error handling and user feedback

2. **Performance Optimizations**:
   - Image loading with proper caching and memory management
   - Network retry logic with exponential backoff
   - Efficient coroutine usage with proper cancellation

3. **Code Organization**:
   - Removed duplicate files and conflicting versions
   - Unified data models across all storage systems
   - Proper package structure with clear separation of concerns

4. **Permission Management**:
   - Android 13+ notification permissions
   - Proper runtime permission handling
   - User-friendly permission request flows

5. **Firebase Integration**:
   - Improved initialization with better error handling
   - Offline support with proper caching
   - FCM integration for push notifications

## Testing Approach

- Unit tests: `app/src/test/`
- Instrumented tests: `app/src/androidTest/`
- Test runner: AndroidJUnitRunner

## Critical Files to Understand

### Core Application
- `PurchaseManagementApp.kt`: Application entry point and global configuration
- `base/BaseActivity.kt`: Common activity functionality
- `AndroidManifest.xml`: Updated with proper permissions and activity declarations

### Data Layer
- `data/models/`: All data model classes (User, PurchaseRequest, Livestock)
- `utils/FirestoreHelper.kt`: Database operations
- `network/NetworkManager.kt`: Network communication

### UI Components
- `MainActivity.kt`: Main navigation hub
- `fragments/`: All form fragments with voice input support
- Layout files in `res/layout/`: UI definitions

### Configuration
- `app/build.gradle.kts`: Dependencies and build configuration
- `google-services.json`: Firebase configuration (sensitive)

## Maintenance Notes

- All duplicate and unused files have been removed
- Memory management is handled automatically through `MemoryManager`
- Network errors are handled gracefully with user-friendly messages
- The app is now production-ready with proper error handling and logging