# 네이버 로그인 설정 가이드

## 문제 해결: "[-1] client info invalid" 오류

네이버 로그인에서 "[-1] client info invalid" 오류가 발생하는 경우, 네이버 개발자 센터에서 발급받은 클라이언트 ID와 클라이언트 시크릿이 올바르게 설정되지 않았기 때문입니다.

## 설정 방법

### 1. 네이버 개발자 센터에서 앱 등록

1. [네이버 개발자 센터](https://developers.naver.com/main/) 접속
2. 로그인 후 "애플리케이션 등록" 클릭
3. 필수 정보 입력:
   - 애플리케이션 이름: `구매신청` (또는 원하는 이름)
   - 사용 API: `네이버 로그인` 선택
   - 제공 정보: `회원이름`, `이메일주소`, `프로필사진` 등 필요한 정보 선택
   - 환경: `Android` 선택
   - 패키지명: `com.accompany.purchaseManagement`
   - 키 해시: 앱의 키 해시 입력 (아래 명령어로 확인)

### 2. 키 해시 확인 방법

#### 디버그 키 해시 (개발용)
```bash
# Windows (PowerShell)
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# macOS/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### 릴리즈 키 해시 (배포용)
```bash
keytool -list -v -keystore [YOUR_KEYSTORE_PATH] -alias [YOUR_KEY_ALIAS]
```

SHA1 fingerprint를 Base64로 인코딩한 값을 네이버 개발자 센터에 등록하세요.

### 3. 클라이언트 ID와 시크릿 설정

네이버 개발자 센터에서 발급받은 정보를 앱에 적용합니다:

#### 방법 1: strings.xml 파일 수정 (권장)

`app/src/main/res/values/strings.xml` 파일에서 다음 값을 수정:

```xml
<!-- 네이버 로그인 -->
<string name="naver_client_id">발급받은_클라이언트_ID</string>
<string name="naver_client_secret">발급받은_클라이언트_시크릿</string>
<string name="naver_client_name">구매신청</string>
```

예시:
```xml
<string name="naver_client_id">abcd1234efgh5678</string>
<string name="naver_client_secret">XyZ9876aBcD</string>
<string name="naver_client_name">구매신청</string>
```

#### 방법 2: BuildConfig 사용 (더 안전)

보안을 위해 BuildConfig를 사용하는 방법:

1. `local.properties` 파일에 추가:
```properties
naver.client.id=발급받은_클라이언트_ID
naver.client.secret=발급받은_클라이언트_시크릿
```

2. `app/build.gradle.kts` 파일 수정:
```kotlin
android {
    defaultConfig {
        // ... 기존 설정
        
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${properties.getProperty("naver.client.id")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${properties.getProperty("naver.client.secret")}\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

3. NaverAuthHelper.kt에서 사용:
```kotlin
NaverIdLoginSDK.initialize(
    activity,
    BuildConfig.NAVER_CLIENT_ID,
    BuildConfig.NAVER_CLIENT_SECRET,
    naverClientName
)
```

### 4. 테스트

1. 앱을 실행하고 "네이버 아이디로 로그인" 버튼 클릭
2. 네이버 로그인 화면이 정상적으로 나타나는지 확인
3. 로그인 성공 후 메인 화면으로 이동하는지 확인

## 주의사항

- ⚠️ **절대로** 클라이언트 시크릿을 Git에 커밋하지 마세요!
- `.gitignore`에 `local.properties`가 포함되어 있는지 확인하세요
- 릴리즈 빌드 시에는 반드시 릴리즈 키 해시를 등록해야 합니다
- 네이버 개발자 센터에서 앱의 상태가 "검수 완료" 또는 "서비스 적용"이어야 합니다

## 문제 해결

### "[-1] client info invalid" 오류
- 클라이언트 ID/시크릿이 올바른지 확인
- 패키지명이 정확한지 확인 (`com.accompany.purchaseManagement`)
- 키 해시가 올바르게 등록되었는지 확인
- 네이버 개발자 센터에서 앱 상태 확인

### "[-2] invalid_request" 오류
- 네이버 개발자 센터에서 필수 제공 정보를 모두 선택했는지 확인

### 키 해시 불일치 오류
- 현재 사용 중인 키스토어의 키 해시를 다시 확인
- 디버그/릴리즈 키 해시를 각각 등록

## 참고 자료

- [네이버 로그인 API 개발 가이드](https://developers.naver.com/docs/login/overview/)
- [네이버 아이디로 로그인 Android SDK](https://developers.naver.com/docs/login/android/android.md)
