# 문제 해결 가이드 (Troubleshooting Guide)

## Gradle 빌드 오류

### 오류: "Directory 'D:\Purchase\PurchaseMangement' does not contain a Gradle build"

이 오류는 Gradle이 필요한 빌드 파일을 찾을 수 없을 때 발생합니다.

#### 원인
1. 잘못된 디렉토리에 있음
2. Gradle 파일이 누락됨
3. Git에서 최신 코드를 가져오지 않음

#### 해결 방법

**1단계: 올바른 디렉토리에 있는지 확인**

```bash
# Windows 명령 프롬프트 또는 PowerShell에서
cd D:\Purchase\PurchaseMangement
dir settings.gradle.kts
```

`settings.gradle.kts` 파일이 보여야 합니다. 파일이 없다면 다음 단계로 진행하세요.

**2단계: 최신 코드 가져오기**

```bash
git fetch origin
git pull origin main
```

**3단계: 필수 파일 확인**

프로젝트 루트에 다음 파일들이 있어야 합니다:

```
PurchaseMangement/
├── settings.gradle.kts          ✓ 반드시 있어야 함
├── build.gradle.kts             ✓ 반드시 있어야 함
├── gradlew.bat                  ✓ Windows용 Gradle 래퍼
├── gradlew                      ✓ Linux/Mac용 Gradle 래퍼
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar   ✓ 래퍼 JAR 파일
│       └── gradle-wrapper.properties ✓ 래퍼 설정
└── app/
    └── build.gradle.kts         ✓ 앱 빌드 파일
```

파일 확인:
```bash
# Windows
dir *.kts
dir gradle\wrapper\

# 또는 PowerShell
ls *.kts
ls gradle\wrapper\
```

**4단계: 파일이 여전히 없다면 - 저장소 다시 복제**

```bash
# 상위 디렉토리로 이동
cd D:\Purchase

# 기존 폴더 이름 변경 (백업)
ren PurchaseMangement PurchaseMangement_old

# 저장소 새로 복제
git clone https://github.com/kingjyo/PurchaseMangement.git
cd PurchaseMangement

# Gradle 파일 확인
dir settings.gradle.kts
```

**5단계: 빌드 테스트**

```bash
gradlew.bat clean build
```

### 오류: "Could not resolve all artifacts for configuration"

네트워크 연결 문제로 발생합니다.

#### 해결 방법:

1. **인터넷 연결 확인**
   - 브라우저에서 https://google.com 접속 테스트
   - 방화벽이 Gradle을 차단하는지 확인

2. **재시도**
   ```bash
   gradlew.bat build --refresh-dependencies
   ```

3. **VPN 사용** (특정 저장소가 차단된 경우)

4. **오프라인 모드로 빌드** (의존성이 이미 캐시된 경우)
   ```bash
   gradlew.bat build --offline
   ```

## Android Studio 문제

### 프로젝트를 가져올 수 없음

**해결 방법:**

1. Android Studio 종료
2. 프로젝트의 `.idea` 폴더와 `.gradle` 폴더 삭제
   ```bash
   # Windows 탐색기에서 직접 삭제하거나
   rd /s /q .idea
   rd /s /q .gradle
   ```
3. Android Studio에서 프로젝트 다시 열기
4. Gradle Sync 완료 대기

### Gradle Sync 실패

**해결 방법:**

1. **File → Invalidate Caches / Restart** 실행
2. Android Studio 재시작
3. Gradle 데몬 중지 후 재시도:
   ```bash
   gradlew.bat --stop
   ```
4. Android Studio에서 다시 프로젝트 열기

## 일반적인 Windows 명령어

### 프로젝트 디렉토리로 이동
```bash
cd /d D:\Purchase\PurchaseMangement
```

### Gradle 버전 확인
```bash
gradlew.bat --version
```

### 빌드 정리
```bash
gradlew.bat clean
```

### 디버그 APK 빌드
```bash
gradlew.bat assembleDebug
```

### 연결된 기기에 앱 설치
```bash
gradlew.bat installDebug
```

### 테스트 실행
```bash
gradlew.bat test
```

## Git 관련 문제

### 파일이 추적되지 않음

```bash
# 현재 상태 확인
git status

# 추적되지 않은 파일 확인
git ls-files
```

### 파일 권한 문제 (Windows)

Windows에서는 파일 권한 문제가 거의 없지만, 실행 권한이 필요한 경우:

```bash
# PowerShell에서 관리자 권한으로 실행
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## 추가 도움말

### Gradle 데몬 관련

Gradle 데몬이 응답하지 않을 때:

```bash
# 모든 Gradle 데몬 중지
gradlew.bat --stop

# 다시 빌드
gradlew.bat build
```

### 캐시 문제

```bash
# Gradle 캐시 정리
gradlew.bat clean cleanBuildCache

# 의존성 새로 다운로드
gradlew.bat build --refresh-dependencies
```

### JDK 버전 확인

```bash
java -version
```

필요한 JDK 버전: **JDK 17 이상**

JDK 17이 없다면:
1. [Oracle JDK 다운로드](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. 또는 [OpenJDK 다운로드](https://adoptium.net/temurin/releases/?version=17)

### Android SDK 경로 설정

`local.properties` 파일 생성 (프로젝트 루트에):

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

또는 환경 변수 설정:
```bash
setx ANDROID_HOME "C:\Users\YourUsername\AppData\Local\Android\Sdk"
```

## 문의사항

문제가 계속되면:
1. 오류 메시지 전체를 복사
2. 실행한 명령어 기록
3. GitHub Issues에 문의

## 유용한 링크

- [Gradle 공식 문서](https://docs.gradle.org/)
- [Android 개발자 가이드](https://developer.android.com/?hl=ko)
- [Kotlin 문서](https://kotlinlang.org/docs/home.html)
- [Firebase 문서](https://firebase.google.com/docs?hl=ko)
