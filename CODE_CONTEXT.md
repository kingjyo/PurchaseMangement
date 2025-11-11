# 전체 코드 맥락 문서 (Code Context Documentation)

> 이 문서는 PurchaseManagement 앱의 전체 코드베이스 구조, 목적, 기능을 상세히 설명합니다.
> 작성일: 2024-11-11

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [아키텍처 구조](#아키텍처-구조)
3. [핵심 컴포넌트](#핵심-컴포넌트)
4. [데이터 모델](#데이터-모델)
5. [주요 기능](#주요-기능)
6. [기술 스택](#기술-스택)
7. [파일 구조](#파일-구조)
8. [비즈니스 로직](#비즈니스-로직)
9. [보안 및 권한](#보안-및-권한)
10. [개발 가이드](#개발-가이드)

---

## 프로젝트 개요

### 🎯 프로젝트 목적

**PurchaseManagement**는 사무실의 구매/발주 시스템을 디지털화하기 위해 개발된 Android 앱입니다.

#### 배경
- 기존에는 구두(음성)로만 주문하던 방식 사용
- 이로 인해 기록 관리가 어렵고 혼선 발생
- 특히 고령의 사용자들이 많아 접근성이 중요

#### 해결 방안
- 구두 주문을 텍스트/디지털 시스템으로 전환
- 음성 입력 기능 제공으로 고령 사용자 배려
- 구매신청부터 승인, 추적까지 전 과정 관리

### 📊 기본 정보

- **프로젝트명**: PurchaseManagement (구매신청 관리)
- **패키지명**: com.accompany.purchaseManagement
- **언어**: Kotlin 2.1.0
- **플랫폼**: Android (minSdk 24, targetSdk 35)
- **빌드 시스템**: Gradle 8.11.1
- **아키텍처**: Clean Architecture + MVVM

---

## 아키텍처 구조

### 🏗️ Clean Architecture 계층

```
┌─────────────────────────────────────────────────┐
│         Presentation Layer                      │
│  ┌──────────────────────────────────────────┐  │
│  │ Activities, ViewModels, Adapters         │  │
│  │ - MainActivity                            │  │
│  │ - PurchaseRequestActivityV2              │  │
│  │ - PurchaseStatusActivityV2               │  │
│  │ - LoginActivity2                         │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│            Domain Layer                         │
│  ┌──────────────────────────────────────────┐  │
│  │ Business Logic & Data Models             │  │
│  │ - User (사용자)                           │  │
│  │ - PurchaseRequest (구매신청)             │  │
│  │ - Livestock (가축)                        │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│              Data Layer                         │
│  ┌──────────────────────────────────────────┐  │
│  │ Data Sources & Network                   │  │
│  │ - FirestoreHelper (Firebase CRUD)        │  │
│  │ - NetworkManager (API 통신)              │  │
│  │ - GoogleSheetsHelper (시트 연동)         │  │
│  │ - Local DB Helper                        │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### 🔄 MVVM 패턴

- **View**: Activities, Fragments (XML layouts)
- **ViewModel**: `PurchaseViewModel` - LiveData로 상태 관리
- **Model**: Data classes (User, PurchaseRequest, Livestock)

### 🧱 베이스 클래스 계층

```
PurchaseManagementApp (Application)
    ↓
BaseActivity (모든 Activity의 부모)
    ↓
├── MainActivity
├── LoginActivity2
├── PurchaseRequestActivityV2
├── PurchaseStatusActivityV2
├── PurchaseHistoryActivity
├── CattleStatusActivity
├── UserManagementActivity
└── EditPurchaseRequestActivity
```

---

## 핵심 컴포넌트

### 1. 애플리케이션 클래스

#### `PurchaseManagementApp.kt`
앱의 진입점이자 전역 설정 관리

**역할:**
- Firebase 서비스 초기화 (Auth, Firestore, Storage, Messaging)
- 전역 예외 처리기 설정
- FCM 토큰 관리
- 사용자 세션 관리

**주요 상수:**
```kotlin
// Firestore 컬렉션
USERS_COLLECTION = "users"
PURCHASE_REQUESTS_COLLECTION = "purchase_requests"
LIVESTOCK_COLLECTION = "livestock"

// 사용자 역할
ROLE_ADMIN = "admin"
ROLE_MANAGER = "manager"
ROLE_USER = "user"

// SharedPreferences 키
PREFS_NAME = "UserPrefs"
KEY_IS_LOGGED_IN = "isLoggedIn"
KEY_USER_ID = "userId"
```

### 2. 베이스 Activity

#### `base/BaseActivity.kt`
모든 Activity의 공통 기능 제공

**기능:**
- 메모리 관리 (MemoryManager 통합)
- 권한 관리 (PermissionManager 통합)
- 로딩 상태 표시/숨김
- 안전한 Toast 메시지
- 코루틴 예외 처리
- 액션바 설정 헬퍼

**사용 예시:**
```kotlin
class MyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setupActionBar, showLoading 등 사용 가능
        setupActionBar("제목", showBackButton = true)
    }
}
```

### 3. 주요 Activity

#### `MainActivity.kt` - 메인 대시보드
- 사용자 환영 화면
- 역할별 버튼 표시/숨김
- 네비게이션 허브 역할
- 로그아웃 기능

**버튼 구성:**
- 구매신청 (모든 사용자)
- 구매현황 (모든 사용자)
- 구매기록 (모든 사용자)
- 가축현황 (모든 사용자)
- 관리자 메뉴 (Manager 이상만)

#### `LoginActivity2.kt` - 인증
- Google OAuth 로그인
- 사용자 정보 Firestore 동기화
- FCM 토큰 등록
- 세션 생성

#### `PurchaseRequestActivityV2.kt` - 구매신청
- 다단계 폼 방식
- 음성 입력 지원
- 사진 첨부 (Firebase Storage)
- 실시간 유효성 검사
- Firebase + Google Sheets 동시 저장

#### `PurchaseStatusActivityV2.kt` - 신청 현황
- 사용자별 신청 목록
- 관리자는 전체 신청 조회
- 상태별 필터링
- 승인/거부 기능 (관리자)
- 수정 기능 (신청자)

#### `PurchaseHistoryActivity.kt` - 신청 기록
- 과거 신청 조회
- 검색 및 필터링
- 상세 정보 보기
- Excel 내보내기

#### `CattleStatusActivity.kt` - 가축 관리
- 가축 목록 조회
- 건강 상태 관리
- 귀표번호 검색
- 가축 등록/수정

#### `UserManagementActivity.kt` - 사용자 관리
- 사용자 목록 (관리자 전용)
- 역할 변경
- 활성/비활성 상태 관리

### 4. 유틸리티 클래스

#### `GmailHelper.kt` - Gmail 알림 전송 (NEW!)
Gmail API를 통한 이메일 알림 시스템

**핵심 기능:**
- 구매신청 시 관리자에게 자동 이메일 발송
- HTML 형식의 보기 좋은 이메일
- 신청자 정보, 구매 정보, 첨부 사진 포함
- Google OAuth 인증 사용

**주요 메서드:**
```kotlin
// Gmail 서비스 초기화
fun initializeGmailService(account: GoogleSignInAccount): Boolean
fun initializeWithCurrentAccount(): Boolean

// 구매신청 이메일 전송
suspend fun sendPurchaseRequestEmail(
    request: PurchaseRequest,
    adminEmail: String = AppConfig.MANAGER_EMAIL
): Result<Unit>

// 테스트 이메일 전송
suspend fun sendTestEmail(to: String): Result<Unit>
```

**이메일 내용:**
- 📧 제목: "알림: 구매신청 도착"
- 👤 신청자 정보 (이름, 소속, 이메일)
- 🛒 구매 정보 (품목명, 수량, 장소, 용도 등)
- 📷 첨부 사진 (최대 3장 직접 표시)

#### `utils/FirestoreHelper.kt` - Firebase 데이터베이스
모든 Firestore CRUD 작업 관리

**주요 메서드:**
```kotlin
// 사용자
suspend fun saveUser(user: User): Result<Unit>
suspend fun getUser(userId: String): Result<User?>
suspend fun getUserByEmail(email: String): Result<User?>
suspend fun getAllUsers(): Result<List<User>>

// 구매신청
suspend fun savePurchaseRequest(request: PurchaseRequest): Result<String>
suspend fun getPurchaseRequest(requestId: String): Result<PurchaseRequest?>
suspend fun getPurchaseRequestsByUser(userId: String): Result<List<PurchaseRequest>>
suspend fun getAllPurchaseRequests(): Result<List<PurchaseRequest>>
suspend fun updatePurchaseRequestStatus(requestId: String, status: String): Result<Unit>

// 가축
suspend fun saveLivestock(livestock: Livestock): Result<String>
suspend fun getLivestock(livestockId: String): Result<Livestock?>
suspend fun getAllLivestock(): Result<List<Livestock>>
```

#### `utils/PermissionManager.kt` - 권한 관리
Android 권한 요청 및 처리 중앙 관리

**관리하는 권한:**
- 카메라 (CAMERA)
- 오디오 녹음 (RECORD_AUDIO)
- 저장소 (READ/WRITE_EXTERNAL_STORAGE, READ_MEDIA_IMAGES)
- 알림 (POST_NOTIFICATIONS - Android 13+)

#### `utils/MemoryManager.kt` - 메모리 최적화
메모리 누수 방지 및 모니터링

**기능:**
- Activity 생명주기 추적
- 메모리 사용량 로깅
- 이미지 캐시 관리
- 저메모리 경고 처리

#### `network/NetworkManager.kt` - 네트워크 관리
안정적인 네트워크 통신 지원

**기능:**
- 네트워크 연결 상태 모니터링 (LiveData)
- 자동 재시도 로직 (exponential backoff)
- HTTP 에러 처리 (401, 403, 404, 500 등)
- Retrofit 서비스 인스턴스 생성

**사용 예시:**
```kotlin
val result = networkManager.safeApiCall(
    retryCount = 3,
    delayMs = 1000
) {
    apiService.getData()
}

when (result) {
    is ApiResult.Success -> { /* 성공 처리 */ }
    is ApiResult.Error -> { /* 에러 처리 */ }
}
```

#### `network/RetryInterceptor.kt`
OkHttp 인터셉터로 자동 재시도

**설정:**
- 최대 재시도: 3회
- 지수 백오프 (1초, 2초, 4초)
- 5xx 에러 및 타임아웃 시 재시도

---

## 데이터 모델

### 1. User (사용자)

#### 구조
```kotlin
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val department: String = "",
    val role: String = ROLE_USER,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val fcmToken: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### 역할 시스템
- **ROLE_ADMIN**: 전체 관리 권한
  - 모든 기능 접근
  - 사용자 관리
  - 시스템 설정
  
- **ROLE_MANAGER**: 관리 권한
  - 모든 구매신청 조회/승인
  - 가축 관리
  - 통계 조회
  
- **ROLE_USER**: 일반 사용자
  - 구매신청 작성
  - 자신의 신청 조회/수정
  - 가축 조회

#### 주요 메서드
```kotlin
fun isAdmin(): Boolean = role == ROLE_ADMIN
fun isManager(): Boolean = role == ROLE_MANAGER || isAdmin()
fun getDisplayRole(): String  // "관리자", "매니저", "사용자"
fun toFirestoreMap(): Map<String, Any?>  // Firestore 저장용
```

### 2. PurchaseRequest (구매신청)

#### 구조
```kotlin
data class PurchaseRequest(
    val id: String = "",
    val localId: Long = 0L,
    
    // 신청자 정보 (자동 입력)
    val applicantId: String = "",
    val applicantName: String = "",
    val applicantDepartment: String = "",
    val applicantEmail: String = "",
    
    // 구매 정보
    val equipmentName: String = "",  // 장비/품목명
    val quantity: String = "",        // 수량
    val location: String = "",        // 장소
    val purpose: String = "",         // 용도
    val note: String = "",            // 기타사항
    
    // 사진 정보
    val photoUrls: List<String> = emptyList(),
    
    // 신청 정보
    val requestDate: String = "",
    val status: String = STATUS_PENDING,
    
    // 수정 정보
    val modifiedDate: String? = null,
    val modifyCount: Int = 0,
    
    // 처리 정보
    val processor: String? = null,
    val processedDate: String? = null,
    val processNote: String? = null
)
```

#### 상태 흐름
```
대기중 (PENDING)
    ↓
승인됨 (APPROVED) / 거부됨 (REJECTED)
    ↓
진행중 (IN_PROGRESS)
    ↓
완료됨 (COMPLETED) / 취소됨 (CANCELLED)
```

#### 상태별 색상
- 대기중: Orange
- 승인됨: Green
- 거부됨: Red
- 진행중: Blue
- 완료됨: Light Green
- 취소됨: Gray

#### 주요 메서드
```kotlin
fun canModify(): Boolean  // 수정 가능 여부
fun isCompleted(): Boolean  // 완료 상태 확인
fun getStatusColor(): Int  // 상태별 색상
fun validate(): List<String>  // 유효성 검사
fun toFirestoreMap(): Map<String, Any?>
```

### 3. Livestock (가축)

#### 구조
```kotlin
data class Livestock(
    val id: String = "",
    val localId: Long = 0L,
    val earTag: String = "",          // 귀표번호
    val species: String = "",         // 축종 (소, 돼지, 닭 등)
    val breed: String = "",           // 품종
    val gender: String = "",          // 성별
    val birthDate: String = "",       // 출생일
    val weight: String = "",          // 체중
    val mother: String = "",          // 모축
    val father: String = "",          // 부축
    val location: String = "",        // 위치
    val healthStatus: String = HEALTH_NORMAL,
    val note: String = "",
    val photoUrls: List<String> = emptyList(),
    val ownerId: String = "",
    val ownerName: String = "",
    val registrationDate: String = "",
    val isActive: Boolean = true
)
```

#### 축종 (Species)
- 소 (CATTLE)
- 돼지 (PIG)
- 닭 (CHICKEN)
- 염소 (GOAT)
- 양 (SHEEP)

#### 성별 (Gender)
- 수컷 (MALE)
- 암컷 (FEMALE)
- 거세 (CASTRATED)

#### 건강 상태 (Health Status)
- 정상 (NORMAL) - Green
- 질병 (SICK) - Red
- 부상 (INJURED) - Orange
- 임신 (PREGNANT) - Blue
- 폐사 (DEAD) - Gray

#### 주요 메서드
```kotlin
fun getAgeInMonths(): Int  // 나이 계산 (개월)
fun getHealthStatusColor(): Int  // 건강 상태별 색상
fun validate(): List<String>
fun toFirestoreMap(): Map<String, Any?>
```

---

## 주요 기능

### 1. 인증 및 사용자 관리

#### Google OAuth 로그인
```kotlin
// LoginActivity2.kt
private fun signInWithGoogle() {
    val signInIntent = googleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
}

private fun handleSignInResult(data: Intent?) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    val account = task.getResult(ApiException::class.java)
    firebaseAuthWithGoogle(account.idToken!!)
}
```

#### 세션 관리
```kotlin
// PurchaseManagementApp.kt
fun saveUserSession(userId: String, name: String, email: String, 
                    department: String, role: String) {
    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
        putBoolean(KEY_IS_LOGGED_IN, true)
        putString(KEY_USER_ID, userId)
        putString(KEY_USER_NAME, name)
        putString(KEY_USER_EMAIL, email)
        putString(KEY_USER_DEPARTMENT, department)
        putString(KEY_USER_ROLE, role)
        apply()
    }
}

fun clearUserSession() {
    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
    auth.signOut()
}
```

### 2. 구매신청 프로세스

#### 신청서 작성 흐름
1. **신청자 정보 자동 입력**
   - SharedPreferences에서 로그인 사용자 정보 로드
   - 이름, 부서, 이메일 자동 설정

2. **구매 정보 입력**
   - 장비/품목명 (음성 또는 텍스트)
   - 수량
   - 장소
   - 용도 (필수)
   - 기타사항

3. **사진 첨부 (선택)**
   - 카메라로 촬영
   - 갤러리에서 선택
   - Firebase Storage에 업로드
   - URL을 PurchaseRequest에 저장

4. **유효성 검사**
   ```kotlin
   fun validate(): List<String> {
       val errors = mutableListOf<String>()
       if (equipmentName.isBlank()) errors.add("장비/품목명을 입력해주세요")
       if (quantity.isBlank()) errors.add("수량을 입력해주세요")
       if (purpose.isBlank()) errors.add("용도를 입력해주세요")
       return errors
   }
   ```

5. **저장 및 알림**
   - Firestore에 저장
   - Google Sheets에 동시 저장 (백업)
   - **Gmail로 관리자에게 상세 알림 이메일 전송** ← NEW!
   - FCM 푸시 알림 발송

#### Gmail 이메일 알림 (NEW!)
구매신청 제출 시 관리자에게 자동으로 상세 알림 이메일 전송

**전송 프로세스:**
```kotlin
// PurchaseRequestActivityV2.kt
private suspend fun sendGmailNotification(...) {
    // PurchaseRequest 객체 생성
    val purchaseRequest = PurchaseRequest(...)
    
    // Gmail 이메일 전송
    val result = gmailHelper.sendPurchaseRequestEmail(
        request = purchaseRequest,
        adminEmail = AppConfig.MANAGER_EMAIL
    )
}
```

**이메일 특징:**
- ✉️ HTML 형식의 깔끔한 디자인
- 📋 신청자 정보 (이름, 소속, 이메일)
- 🛒 구매 정보 (품목명, 수량, 장소, 용도, 기타사항)
- 📷 첨부 사진 (최대 3장 직접 표시, 나머지 개수 안내)
- 🎨 상태별 색상 배지 (대기중, 승인됨 등)
- 📱 앱 확인 안내 메시지

**설정:**
- `AppConfig.MANAGER_EMAIL`에 관리자 이메일 설정
- Google OAuth 로그인 시 Gmail.SEND 권한 자동 요청
- 자세한 설정: `GMAIL_SETUP.md` 참조

#### 음성 입력
```kotlin
// SpeechRecognitionHelper.kt
class SpeechRecognitionHelper(private val activity: Activity) {
    
    fun startListening(onResult: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀하세요")
        }
        activity.startActivityForResult(intent, REQUEST_CODE_SPEECH)
    }
}
```

### 3. 상태 관리 및 승인 프로세스

#### 관리자 승인 플로우
```kotlin
// PurchaseStatusActivityV2.kt
private fun approvePurchaseRequest(request: PurchaseRequest) {
    lifecycleScope.launch {
        val result = firestoreHelper.updatePurchaseRequestStatus(
            requestId = request.id,
            status = PurchaseRequest.STATUS_APPROVED,
            processor = currentUser?.name,
            processNote = "승인됨"
        )
        
        if (result.isSuccess) {
            // FCM 알림 발송
            sendNotificationToUser(request.applicantId, "신청이 승인되었습니다")
            refreshList()
        }
    }
}
```

#### 상태 변경 이력
- 모든 상태 변경은 `processedDate`와 `processor` 기록
- `modifyCount`로 수정 횟수 추적
- `modifiedDate`로 마지막 수정 시간 저장

### 4. Firebase 통합

#### Firestore 컬렉션 구조
```
/users/{userId}
    - id, name, email, department, role, fcmToken 등

/purchase_requests/{requestId}
    - 모든 구매신청 정보
    - photoUrls 배열에 Storage URL 저장

/livestock/{livestockId}
    - 가축 정보
    - photoUrls 배열

/notifications/{notificationId}
    - 알림 내역

/crash_logs/{logId}
    - 크래시 로그 (자동 수집)
```

#### Storage 폴더 구조
```
/profile_images/{userId}/
    - 사용자 프로필 이미지

/purchase_request_images/{requestId}/
    - 구매신청 첨부 사진

/livestock_images/{livestockId}/
    - 가축 사진

/purchase_documents/{requestId}/
    - 구매 관련 문서
```

#### FCM 알림
```kotlin
// MyFirebaseMessagingService.kt
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    remoteMessage.notification?.let {
        showNotification(it.title, it.body)
    }
    
    remoteMessage.data.isNotEmpty().let {
        handleDataPayload(remoteMessage.data)
    }
}
```

### 5. Google Sheets 연동

#### 데이터 내보내기
```kotlin
// GoogleSheetsHelper.kt
suspend fun exportPurchaseRequests(requests: List<PurchaseRequest>) {
    val values = requests.map { request ->
        listOf(
            request.requestDate,
            request.applicantName,
            request.equipmentName,
            request.quantity,
            request.purpose,
            request.status
        )
    }
    
    sheetsService.spreadsheets().values()
        .append(spreadsheetId, range, ValueRange().setValues(values))
        .setValueInputOption("RAW")
        .execute()
}
```

### 6. 오프라인 지원

#### Firestore 캐싱
```kotlin
// PurchaseManagementApp.kt
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)  // 오프라인 지원
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()
```

#### 동기화 전략
- 오프라인 시 로컬 캐시에서 읽기
- 온라인 복귀 시 자동 동기화
- 충돌 시 서버 데이터 우선

---

## 기술 스택

### Android 플랫폼
- **Kotlin**: 2.1.0
- **Android SDK**: 24 (Nougat) ~ 35 (최신)
- **Gradle**: 8.11.1
- **Android Gradle Plugin**: 8.10.1 (버전 오류 - 8.7.x로 수정 필요)

### Firebase Services
```kotlin
// Firebase BOM 33.15.0 사용
implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
```

### 네트워킹
- **Retrofit**: 2.9.0 - REST API 통신
- **OkHttp**: 4.12.0 - HTTP 클라이언트
- **Gson**: 변환기 - JSON 파싱

### UI/UX
- **Material Design**: 1.11.0
- **Glide**: 4.16.0 - 이미지 로딩/캐싱
- **ViewBinding**: enabled - 타입 안전 뷰 접근
- **RecyclerView**: 1.3.2
- **SwipeRefreshLayout**: 1.1.0

### 비동기 처리
- **Coroutines**: 1.7.3
  - kotlinx-coroutines-core
  - kotlinx-coroutines-android
  - kotlinx-coroutines-play-services (Firebase 통합)

### 아키텍처 컴포넌트
- **Lifecycle**: 2.7.0
  - lifecycle-runtime-ktx
  - lifecycle-viewmodel-ktx
  - lifecycle-livedata-ktx
- **Navigation**: 2.7.6

### 인증
- **Google Play Services Auth**: 21.2.0
- **Firebase Auth**: BOM으로 관리

### Google APIs
- **Google Sheets API**: 2.2.0
- **Google OAuth**: 1.19.0

### 테스팅
- **JUnit**: 4.13.2
- **Espresso**: 3.5.1
- **AndroidX Test**: 1.5.2

---

## 파일 구조

### 전체 구조
```
PurchaseManagement/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/accompany/purchaseManagement/
│   │   │   │   ├── PurchaseManagementApp.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── LoginActivity2.kt
│   │   │   │   ├── PurchaseRequestActivityV2.kt
│   │   │   │   ├── PurchaseStatusActivityV2.kt
│   │   │   │   ├── PurchaseHistoryActivity.kt
│   │   │   │   ├── CattleStatusActivity.kt
│   │   │   │   ├── UserManagementActivity.kt
│   │   │   │   ├── EditPurchaseRequestActivity.kt
│   │   │   │   ├── ProfileSetupActivity.kt
│   │   │   │   ├── base/
│   │   │   │   │   └── BaseActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   └── models/
│   │   │   │   │       ├── User.kt
│   │   │   │   │       ├── PurchaseRequest.kt
│   │   │   │   │       └── Livestock.kt
│   │   │   │   ├── network/
│   │   │   │   │   ├── NetworkManager.kt
│   │   │   │   │   └── RetryInterceptor.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── FirestoreHelper.kt
│   │   │   │   │   ├── PermissionManager.kt
│   │   │   │   │   └── MemoryManager.kt
│   │   │   │   ├── adapters/
│   │   │   │   │   └── PhotoAdapter.kt
│   │   │   │   ├── PurchaseViewModel.kt
│   │   │   │   ├── SpeechRecognitionHelper.kt
│   │   │   │   ├── GoogleSheetsHelper.kt
│   │   │   │   ├── GoogleAuthHelper.kt
│   │   │   │   ├── FcmNotificationHelper.kt
│   │   │   │   ├── MyFirebaseMessagingService.kt
│   │   │   │   └── ValidationUtils.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/ (85개 XML 파일)
│   │   │   │   ├── values/
│   │   │   │   ├── drawable/
│   │   │   │   └── mipmap/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── google-services.json
├── functions/ (Firebase Cloud Functions)
│   ├── index.js
│   └── package.json
├── temp_disabled/ (비활성화된 코드)
│   ├── fragments/
│   └── LivestockConverter.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── AGENTS.md
├── CLAUDE.md
├── README.md
└── CODE_CONTEXT.md (이 파일)
```

### 주요 파일 설명

#### 설정 파일
- `build.gradle.kts` (root) - 프로젝트 전역 설정
- `app/build.gradle.kts` - 앱 모듈 의존성 및 빌드 설정
- `settings.gradle.kts` - 프로젝트 구조 정의
- `gradle.properties` - Gradle 설정
- `google-services.json` - Firebase 설정 (민감 정보)

#### 문서 파일
- `AGENTS.md` - 에이전트 시스템 설명 (DebugAgent, RefactorAgent 등)
- `CLAUDE.md` - Claude Code 가이드
- `README.md` - 프로젝트 소개
- `CODE_CONTEXT.md` - 전체 코드 맥락 (이 파일)

#### Firebase Functions
- `functions/index.js` - 서버리스 함수
- `functions/package.json` - Node.js 의존성

#### Temp Disabled
리팩토링 중 비활성화된 코드 (향후 재활용 가능)

---

## 비즈니스 로직

### 구매신청 워크플로우

```
[사용자] 구매신청 작성
    ↓
[시스템] 신청자 정보 자동 입력 (로그인 정보)
    ↓
[사용자] 구매 정보 입력 (장비명, 수량, 용도 등)
    ↓
[사용자] 사진 첨부 (선택)
    ↓
[시스템] 유효성 검사
    ↓ (통과)
[시스템] Firebase Storage에 사진 업로드
    ↓
[시스템] Firestore에 신청 저장 (status: 대기중)
    ↓
[시스템] Google Sheets에 백업
    ↓
[시스템] 관리자에게 FCM 알림 발송
    ↓
[관리자] 신청 검토
    ↓
[관리자] 승인 or 거부
    ↓
[시스템] 상태 업데이트 (status: 승인됨/거부됨)
    ↓
[시스템] 신청자에게 FCM 알림 발송
    ↓
(승인된 경우)
[관리자] 진행 상태 업데이트 (진행중 → 완료됨)
    ↓
[시스템] 신청자에게 완료 알림
```

### 권한 체크 로직

```kotlin
// MainActivity.kt
private fun updateButtonVisibility(user: User) {
    // 일반 기능 - 모든 사용자
    btnPurchaseRequest.visibility = Button.VISIBLE
    btnPurchaseStatus.visibility = Button.VISIBLE
    btnPurchaseHistory.visibility = Button.VISIBLE
    btnCattleStatus.visibility = Button.VISIBLE
    
    // 관리자 기능 - Manager 이상
    btnAdmin.visibility = if (user.isManager()) {
        Button.VISIBLE
    } else {
        Button.GONE
    }
}
```

### 데이터 동기화 전략

#### Firebase → Google Sheets
```kotlin
suspend fun syncToSheets(request: PurchaseRequest) {
    try {
        // 1. Firestore에 저장
        val id = firestoreHelper.savePurchaseRequest(request).getOrThrow()
        
        // 2. Google Sheets에 백업
        sheetsHelper.appendPurchaseRequest(request)
        
        Log.d(TAG, "Synced to both Firebase and Sheets")
    } catch (e: Exception) {
        Log.e(TAG, "Sync failed", e)
        // Firestore 저장은 성공했으므로 계속 진행
        // Sheets 동기화는 백그라운드 재시도
    }
}
```

#### 충돌 해결
- Firestore가 단일 진실 공급원(Single Source of Truth)
- Sheets는 백업 및 읽기 전용
- 충돌 시 Firestore 데이터 우선

---

## 보안 및 권한

### Android 권한

#### Manifest 선언
```xml
<!-- 필수 권한 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 기능별 권한 -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Android 버전별 권한 -->
<!-- Android 12 이하 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

#### 런타임 권한 요청
```kotlin
// PermissionManager.kt
fun requestCameraPermission(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity.requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }
}
```

### Firebase 보안 규칙 (예상)

#### Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // 사용자 컬렉션 - 자신의 문서만 읽기/쓰기
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId 
                   || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // 구매신청 - 역할별 권한
    match /purchase_requests/{requestId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth.uid == resource.data.applicantId 
                    || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['manager', 'admin'];
      allow delete: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // 가축 - 모든 인증 사용자 읽기, Manager 이상 쓰기
    match /livestock/{livestockId} {
      allow read: if request.auth != null;
      allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['manager', 'admin'];
    }
  }
}
```

#### Storage Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // 프로필 이미지 - 자신의 이미지만
    match /profile_images/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    // 구매신청 이미지 - 신청자만 업로드, 모두 읽기
    match /purchase_request_images/{requestId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // 가축 이미지 - Manager 이상만
    match /livestock_images/{livestockId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;  // 실제로는 역할 체크 필요
    }
  }
}
```

### 데이터 유효성 검사

#### 클라이언트 측
```kotlin
// ValidationUtils.kt
object ValidationUtils {
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validatePhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$"))
    }
    
    fun validateNotEmpty(value: String, fieldName: String): String? {
        return if (value.isBlank()) {
            "$fieldName을(를) 입력해주세요"
        } else {
            null
        }
    }
}
```

#### 서버 측 (Firebase Functions)
```javascript
// functions/index.js
exports.validatePurchaseRequest = functions.firestore
  .document('purchase_requests/{requestId}')
  .onCreate((snap, context) => {
    const data = snap.data();
    
    // 필수 필드 검증
    if (!data.equipmentName || !data.quantity || !data.purpose) {
      return snap.ref.delete();  // 유효하지 않은 문서 삭제
    }
    
    return null;
  });
```

### 민감 정보 보호

#### API 키 관리
```kotlin
// google-services.json - Git에서 제외
// .gitignore에 추가됨

// 네이버 로그인 키 - AndroidManifest.xml에 하드코딩 (보안 취약)
<meta-data
    android:name="com.naver.login.CLIENT_ID"
    android:value="eKRvsHfNRW7o1D41_IEU" />
```

**보안 개선 제안:**
- BuildConfig에 API 키 저장
- 환경 변수 사용
- Secret Manager 활용

---

## 개발 가이드

### 빌드 및 실행

#### Android 빌드
```bash
# Windows
gradlew.bat assembleDebug      # 디버그 APK 빌드
gradlew.bat assembleRelease    # 릴리스 APK 빌드
gradlew.bat clean              # 빌드 캐시 정리
gradlew.bat test               # 단위 테스트
gradlew.bat connectedAndroidTest  # 계측 테스트

# Linux/Mac
./gradlew assembleDebug
./gradlew assembleRelease
```

#### Firebase Functions
```bash
cd functions
npm install                # 의존성 설치
npm run lint              # ESLint 검사
npm run deploy            # Firebase에 배포
npm run serve             # 로컬 에뮬레이터
npm run logs              # 로그 확인
```

### 프로젝트 셋업

#### 1. 필수 요구사항
- JDK 17 이상
- Android Studio (최신 버전 권장)
- Node.js 18+ (Firebase Functions용)
- Firebase 계정
- Google Cloud 프로젝트

#### 2. 초기 설정
```bash
# 1. 저장소 클론
git clone https://github.com/kingjyo/PurchaseMangement.git
cd PurchaseMangement

# 2. Google Services 파일 추가
# Firebase Console에서 google-services.json 다운로드
# app/ 폴더에 배치

# 3. Gradle 동기화
./gradlew build

# 4. Firebase Functions 설정
cd functions
npm install
```

#### 3. Firebase 설정
```bash
# Firebase CLI 설치
npm install -g firebase-tools

# 로그인
firebase login

# 프로젝트 초기화
firebase init

# Functions 배포
firebase deploy --only functions
```

### 코딩 컨벤션

#### Kotlin 스타일
```kotlin
// 클래스명: PascalCase
class PurchaseManagementApp : Application()

// 함수명: camelCase
fun savePurchaseRequest()

// 상수: UPPER_SNAKE_CASE
const val PREFS_NAME = "UserPrefs"

// 변수: camelCase
val currentUser: User? = null

// private 멤버: 언더스코어 없이
private val firestore = Firebase.firestore
```

#### 파일 구조
```kotlin
// 1. Package 선언
package com.accompany.purchaseManagement

// 2. Import 문 (알파벳 순)
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// 3. 클래스 선언
class MainActivity : AppCompatActivity() {
    
    // 4. companion object
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // 5. 멤버 변수
    private lateinit var binding: ActivityMainBinding
    
    // 6. 생명주기 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    // 7. public 메서드
    fun publicMethod() {}
    
    // 8. private 메서드
    private fun privateMethod() {}
}
```

### 테스팅 가이드

#### 단위 테스트 작성
```kotlin
// app/src/test/java/.../UserTest.kt
class UserTest {
    
    @Test
    fun `사용자 역할 확인 - Admin`() {
        val user = User(role = User.ROLE_ADMIN)
        assertTrue(user.isAdmin())
        assertTrue(user.isManager())
    }
    
    @Test
    fun `구매신청 유효성 검사 - 필수 필드 누락`() {
        val request = PurchaseRequest(
            equipmentName = "",
            quantity = "10",
            purpose = "테스트"
        )
        val errors = request.validate()
        assertTrue(errors.isNotEmpty())
        assertTrue(errors.contains("장비/품목명을 입력해주세요"))
    }
}
```

#### UI 테스트
```kotlin
// app/src/androidTest/java/.../MainActivityTest.kt
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun 로그인_버튼_표시_확인() {
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun 구매신청_버튼_클릭() {
        onView(withId(R.id.btnPurchaseRequest))
            .perform(click())
        
        // PurchaseRequestActivityV2로 이동 확인
        intended(hasComponent(PurchaseRequestActivityV2::class.java.name))
    }
}
```

### 디버깅 팁

#### Logcat 필터링
```bash
# 태그로 필터
adb logcat -s PurchaseManagementApp

# 우선순위로 필터 (Error 이상만)
adb logcat *:E

# 특정 앱만
adb logcat --pid=$(adb shell pidof -s com.accompany.purchaseManagement)
```

#### Firebase Emulator
```bash
# 로컬 Firebase 에뮬레이터 실행
firebase emulators:start

# Firestore만
firebase emulators:start --only firestore

# Auth + Firestore
firebase emulators:start --only auth,firestore
```

### 릴리스 빌드

#### 서명 키 생성
```bash
keytool -genkey -v -keystore release.keystore \
  -alias purchase_key -keyalg RSA -keysize 2048 -validity 10000
```

#### gradle.properties에 서명 정보 추가
```properties
RELEASE_STORE_FILE=release.keystore
RELEASE_STORE_PASSWORD=your_password
RELEASE_KEY_ALIAS=purchase_key
RELEASE_KEY_PASSWORD=your_password
```

#### 릴리스 APK 빌드
```bash
./gradlew assembleRelease
# 출력: app/build/outputs/apk/release/app-release.apk
```

### 문제 해결

#### 빌드 오류
```bash
# Gradle 캐시 정리
./gradlew clean
rm -rf .gradle
rm -rf build

# 의존성 새로고침
./gradlew build --refresh-dependencies
```

#### Firebase 연결 오류
```bash
# google-services.json 확인
# Firebase Console에서 최신 버전 다운로드

# Firebase SDK 버전 확인
# build.gradle.kts에서 BOM 버전 확인
```

#### 메모리 부족
```gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

---

## 개선 사항 및 TODO

### 🐛 알려진 문제

1. **빌드 오류**
   - Android Gradle Plugin 8.10.1 버전 미존재
   - 해결: 8.7.x로 다운그레이드 필요

2. **보안 취약점**
   - AndroidManifest.xml에 API 키 하드코딩
   - 해결: BuildConfig 또는 Secret Manager 사용

3. **Fragment 비활성화**
   - temp_disabled/fragments/ 폴더의 파일들
   - 해결: 리팩토링 후 재활성화

### 🚀 향후 개선 계획

#### 단기 (1-2주)
- [ ] Android Gradle Plugin 버전 수정
- [ ] API 키 보안 강화
- [ ] 단위 테스트 커버리지 50% 이상
- [ ] Fragment 리팩토링 완료

#### 중기 (1-2개월)
- [ ] Jetpack Compose 마이그레이션 고려
- [ ] Room DB 추가 (로컬 캐싱 개선)
- [ ] Paging 3 적용 (대용량 리스트)
- [ ] WorkManager로 백그라운드 동기화

#### 장기 (3-6개월)
- [ ] 다국어 지원 (영어, 일본어)
- [ ] 태블릿 UI 최적화
- [ ] 웹 버전 개발 (Kotlin/JS)
- [ ] ML Kit 통합 (OCR, 이미지 인식)

### 📊 성능 최적화

#### 현재 성능 지표
- 앱 시작 시간: ~2초
- Firestore 쿼리: 평균 500ms
- 이미지 로딩: Glide 캐싱으로 최적화

#### 개선 목표
- [ ] 앱 시작 시간 1초 이하
- [ ] Firestore 인덱스 최적화
- [ ] 이미지 압축 알고리즘 개선
- [ ] ProGuard/R8 최적화

---

## 참고 자료

### 공식 문서
- [Android Developers](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Material Design](https://material.io/design)

### 라이브러리
- [Retrofit](https://square.github.io/retrofit/)
- [OkHttp](https://square.github.io/okhttp/)
- [Glide](https://github.com/bumptech/glide)
- [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### 프로젝트 문서
- [AGENTS.md](./AGENTS.md) - 에이전트 시스템
- [CLAUDE.md](./CLAUDE.md) - Claude Code 가이드
- [README.md](./README.md) - 프로젝트 소개

---

## 연락처 및 기여

### 프로젝트 정보
- **저장소**: https://github.com/kingjyo/PurchaseMangement
- **이슈 트래커**: GitHub Issues
- **브랜치 전략**: Git Flow

### 기여 가이드
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 코드 리뷰 체크리스트
- [ ] 코딩 컨벤션 준수
- [ ] 단위 테스트 작성
- [ ] 문서 업데이트
- [ ] 빌드 성공 확인
- [ ] 린트 오류 없음

---

**마지막 업데이트**: 2024-11-11  
**문서 버전**: 1.0  
**작성자**: GitHub Copilot

---

이 문서는 PurchaseManagement 프로젝트의 전체 코드 맥락을 이해하기 위한 종합 가이드입니다. 
프로젝트에 참여하는 모든 개발자는 이 문서를 숙지하시기 바랍니다.
