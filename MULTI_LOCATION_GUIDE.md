# 다중 지점 관리 시스템 가이드

## 📍 개요

전국의 여러 지점/현장에서 사용할 수 있도록 지점별 구매 담당자 관리 기능이 추가되었습니다. 각 지점의 작업자가 보낸 구매신청은 해당 지점의 담당자들에게 자동으로 이메일이 전송됩니다.

## ✨ 주요 기능

### 1. 지점 관리
- 각 지점별로 고유한 정보 관리
- 지점명, 코드, 주소, 지역 등
- 활성화/비활성화 상태 관리

### 2. 계층적 담당자 구조
각 지점마다 다음 3가지 유형의 담당자를 설정할 수 있습니다:

1. **주담당자** (필수, 1명)
   - 해당 지점의 메인 구매 담당자
   - 모든 구매신청 이메일 수신

2. **부담당자** (선택, 복수 가능)
   - 주담당자를 보조하는 담당자들
   - 모든 구매신청 이메일 수신

3. **상급자** (선택, 복수 가능)
   - 관리/감독 역할
   - 모든 구매신청 이메일 참조 수신

### 3. 사용자 지점 배정
- 사용자는 자신이 근무하는 지점을 선택
- 선택한 지점은 프로필에 저장
- 언제든지 변경 가능

### 4. 자동 이메일 라우팅
- 구매신청 제출 시 사용자의 지점 확인
- 해당 지점의 모든 담당자 (주담당자 + 부담당자 + 상급자)에게 이메일 전송
- 지점이 없는 경우 기본 관리자에게 전송

## 🏗️ 데이터 구조

### Location (지점) 모델

```kotlin
data class Location(
    val id: String,                              // 고유 ID
    val name: String,                            // 지점명 (예: "서울 본사")
    val code: String,                            // 지점코드 (예: "SEL-001")
    val address: String,                         // 주소
    val phoneNumber: String?,                    // 전화번호
    val region: String,                          // 지역 (예: "서울")
    
    // 담당자 정보
    val primaryManagerEmail: String,             // 주담당자 이메일 (필수)
    val primaryManagerName: String,              // 주담당자 이름
    val secondaryManagerEmails: List<String>,    // 부담당자 이메일 목록
    val secondaryManagerNames: List<String>,     // 부담당자 이름 목록
    val supervisorEmails: List<String>,          // 상급자 이메일 목록
    val supervisorNames: List<String>,           // 상급자 이름 목록
    
    val isActive: Boolean,                       // 활성 상태
    val notes: String                            // 비고
)
```

### User (사용자) 모델 업데이트

```kotlin
data class User(
    // ... 기존 필드들 ...
    val locationId: String?,        // 배정된 지점 ID
    val locationName: String?       // 배정된 지점명 (캐시)
)
```

## 🔧 설정 방법

### 1. 지점 등록 (관리자)

Firebase Console에서 직접 등록하거나 앱 내에서 추가:

```javascript
// Firestore에 지점 추가 예시
{
  "id": "location-seoul-001",
  "name": "서울 본사",
  "code": "SEL-001",
  "address": "서울시 강남구...",
  "region": "서울",
  "primaryManagerEmail": "manager@company.com",
  "primaryManagerName": "김담당",
  "secondaryManagerEmails": ["sub1@company.com", "sub2@company.com"],
  "secondaryManagerNames": ["이부담", "박부담"],
  "supervisorEmails": ["supervisor@company.com"],
  "supervisorNames": ["최상급"],
  "isActive": true,
  "createdAt": 1699999999999
}
```

### 2. 사용자 지점 선택

#### 앱에서 선택하는 방법:
1. 앱 실행 후 메뉴 → **"지점 변경"** 선택
2. 활성화된 지점 목록에서 본인 지점 선택
3. 선택 완료

#### 관리자가 직접 배정:
Firebase Console → users 컬렉션 → 사용자 문서:
```javascript
{
  "locationId": "location-seoul-001",
  "locationName": "서울 본사"
}
```

## 📧 이메일 전송 흐름

```
사용자 구매신청 제출
    ↓
사용자의 지점 확인
    ↓
┌─────────────────────┬─────────────────────┐
│  지점 배정됨         │  지점 미배정         │
└─────────────────────┴─────────────────────┘
    ↓                        ↓
지점 정보 조회           기본 관리자에게 전송
    ↓                   (AppConfig.MANAGER_EMAIL)
모든 담당자 이메일 수집
(주담당자 + 부담당자 + 상급자)
    ↓
각 담당자에게 개별 이메일 전송
    ↓
이메일에 지점명 표시
```

## 📱 사용 예시

### 시나리오 1: 서울 본사 직원

1. **사용자**: 김철수 (서울 본사 근무)
2. **지점 선택**: "서울 본사" 선택
3. **구매신청**: 노트북 구매신청 제출
4. **이메일 전송**:
   - 주담당자: manager-seoul@company.com
   - 부담당자: sub-seoul@company.com
   - 상급자: supervisor-seoul@company.com
5. **이메일 제목**: "알림: 구매신청 도착 - 서울 본사"

### 시나리오 2: 부산 지점 직원

1. **사용자**: 이영희 (부산 지점 근무)
2. **지점 선택**: "부산 지점" 선택
3. **구매신청**: 사무용품 구매신청 제출
4. **이메일 전송**:
   - 주담당자: manager-busan@company.com
   - 상급자: supervisor-busan@company.com
5. **이메일 제목**: "알림: 구매신청 도착 - 부산 지점"

### 시나리오 3: 지점 미배정 사용자

1. **사용자**: 박미정 (지점 선택 안 함)
2. **구매신청**: 장비 구매신청 제출
3. **이메일 전송**:
   - 기본 관리자: AppConfig.MANAGER_EMAIL
4. **이메일 제목**: "알림: 구매신청 도착"

## 🎯 UI/UX 변경사항

### MainActivity
- **환영 메시지에 지점 정보 표시**
  ```
  김철수님 (구매팀)
  사용자
  📍 서울 본사
  ```

- **메뉴에 "지점 변경" 추가**
  - 메뉴 → 지점 변경 → 지점 선택 화면

### LocationSelectionActivity (새로 추가)
- 활성화된 모든 지점 목록 표시
- 지점명, 지역, 코드 표시
- 지점 선택 시 즉시 저장
- "나중에 선택하기" 버튼

### 이메일 템플릿
- 제목에 지점명 추가
- 본문 헤더에 지점명 강조 표시
- 신청자 정보에 지점 표시

## 🔐 권한 및 보안

### 지점 정보 접근
- 모든 사용자: 활성화된 지점 목록 조회 가능
- 자신의 지점 선택/변경 가능

### 담당자 정보
- 담당자 이메일은 Firestore에만 저장
- 앱 화면에는 노출되지 않음 (보안)

### Firestore 규칙 예시

```javascript
match /locations/{locationId} {
  // 모든 인증된 사용자는 활성 지점 읽기 가능
  allow read: if request.auth != null && resource.data.isActive == true;
  
  // 관리자만 생성/수정/삭제 가능
  allow write: if request.auth != null && 
    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
}
```

## 📊 Firebase 구조

```
Firestore
├── locations/ (지점 컬렉션)
│   ├── {locationId}/
│   │   ├── id: "location-001"
│   │   ├── name: "서울 본사"
│   │   ├── primaryManagerEmail: "..."
│   │   ├── secondaryManagerEmails: [...]
│   │   ├── supervisorEmails: [...]
│   │   └── ...
│   └── {locationId}/
│       └── ...
│
└── users/ (사용자 컬렉션)
    ├── {userId}/
    │   ├── name: "김철수"
    │   ├── locationId: "location-001"
    │   ├── locationName: "서울 본사"
    │   └── ...
    └── {userId}/
        └── ...
```

## 🔍 문제 해결

### 이메일이 담당자에게 전송되지 않는 경우

1. **지점 정보 확인**
   ```kotlin
   // 지점 정보 조회
   val locationHelper = LocationHelper.getInstance()
   val result = locationHelper.getLocation(locationId)
   ```

2. **담당자 이메일 확인**
   - Firebase Console → locations → 해당 지점
   - `primaryManagerEmail` 필드 확인
   - Gmail 주소인지 확인

3. **사용자 지점 배정 확인**
   - Firebase Console → users → 해당 사용자
   - `locationId`, `locationName` 필드 확인

### 지점 목록이 표시되지 않는 경우

1. **지점 활성화 상태 확인**
   - `isActive: true` 인지 확인

2. **Firestore 규칙 확인**
   - 사용자가 locations 컬렉션 읽기 권한이 있는지 확인

### 로그 확인

```bash
adb logcat | grep "Location\|Gmail"
```

주요 로그:
- "Location retrieved successfully"
- "Emails sent to X out of Y recipients"
- "Gmail notification sent to X recipients at Y location"

## 📝 API 레퍼런스

### LocationHelper

```kotlin
// 지점 저장
suspend fun saveLocation(location: Location): Result<String>

// 지점 조회
suspend fun getLocation(locationId: String): Result<Location?>

// 활성 지점 목록
suspend fun getAllActiveLocations(): Result<List<Location>>

// 지역별 지점
suspend fun getLocationsByRegion(region: String): Result<List<Location>>

// 담당자 추가
suspend fun addSecondaryManager(locationId: String, email: String, name: String): Result<Unit>
suspend fun addSupervisor(locationId: String, email: String, name: String): Result<Unit>

// 지점 비활성화
suspend fun deactivateLocation(locationId: String): Result<Unit>
```

### GmailHelper (업데이트)

```kotlin
// 단일 수신자
suspend fun sendPurchaseRequestEmail(
    request: PurchaseRequest,
    adminEmail: String
): Result<Unit>

// 복수 수신자 (NEW!)
suspend fun sendPurchaseRequestEmailToMultiple(
    request: PurchaseRequest,
    recipientEmails: List<String>,
    locationName: String
): Result<Unit>
```

## 🚀 향후 개선 사항

### 단기
- [ ] 앱 내에서 지점 등록/수정 UI 추가
- [ ] 지점별 구매신청 통계
- [ ] 담당자 앱 내에서 관리

### 중기
- [ ] 지점별 예산 관리
- [ ] 지점 간 구매신청 이관
- [ ] 지점별 알림 설정

### 장기
- [ ] 지점별 대시보드
- [ ] 지점별 승인 워크플로우
- [ ] 다국어 지원 (지점별)

## 🎓 교육 자료

### 관리자용
1. Firebase Console에서 지점 등록 방법
2. 담당자 이메일 설정 방법
3. 사용자에게 지점 배정 방법

### 사용자용
1. 앱에서 지점 선택 방법
2. 지점 변경 방법
3. 구매신청 시 지점 확인 방법

---

**버전**: 1.2  
**업데이트**: 2024-11-11  
**작성자**: GitHub Copilot

