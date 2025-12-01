# 빠른 시작 가이드 (Quick Start Guide)

## Windows 사용자를 위한 3단계 설정

### 1단계: 저장소 복제
```cmd
git clone https://github.com/kingjyo/PurchaseMangement.git
cd PurchaseMangement
```

### 2단계: 설정 확인
```cmd
verify_gradle_setup.bat
```

결과가 "[SUCCESS] All required Gradle files are present!"이면 다음 단계로 진행하세요.

### 3단계: 프로젝트 열기

**방법 A: Android Studio 사용 (권장)**
1. Android Studio 실행
2. File → Open
3. `PurchaseMangement` 폴더 선택
4. Gradle Sync 완료 대기
5. Run 버튼 클릭 (Shift+F10)

**방법 B: 명령줄 사용**
```cmd
REM 디버그 빌드
gradlew.bat assembleDebug

REM 연결된 기기에 설치
gradlew.bat installDebug
```

---

## 문제가 발생한 경우

### "Directory does not contain a Gradle build" 오류

**해결책:**

1. **올바른 디렉토리에 있는지 확인**
   ```cmd
   cd /d D:\Purchase\PurchaseMangement
   dir settings.gradle.kts
   ```
   
   `settings.gradle.kts` 파일이 보여야 합니다.

2. **파일이 없다면 최신 코드 다운로드**
   ```cmd
   git pull origin main
   ```

3. **여전히 안 된다면 다시 복제**
   ```cmd
   cd /d D:\Purchase
   git clone https://github.com/kingjyo/PurchaseMangement.git
   cd PurchaseMangement
   verify_gradle_setup.bat
   ```

### 기타 문제

자세한 해결 방법은 다음 문서를 참조하세요:
- [TROUBLESHOOTING_KR.md](TROUBLESHOOTING_KR.md) - 상세한 문제 해결 가이드
- [README.md](README.md) - 전체 문서 (영문)

---

## 필수 요구사항

- **JDK 17 이상** - [다운로드](https://adoptium.net/temurin/releases/?version=17)
- **Android Studio** - [다운로드](https://developer.android.com/studio)
- **Git** - [다운로드](https://git-scm.com/download/win)

---

## 주요 명령어

```cmd
REM 프로젝트 빌드
gradlew.bat build

REM 빌드 정리
gradlew.bat clean

REM 디버그 APK 생성
gradlew.bat assembleDebug

REM 앱 설치 (기기 연결 필요)
gradlew.bat installDebug

REM 테스트 실행
gradlew.bat test

REM Gradle 데몬 중지
gradlew.bat --stop
```

---

## 도움이 필요하신가요?

1. 우선 [TROUBLESHOOTING_KR.md](TROUBLESHOOTING_KR.md) 확인
2. 그래도 해결 안 되면 GitHub Issues에 문의
3. 오류 메시지 전체를 복사해서 공유해주세요

---

## 참고 링크

- [Android 개발 시작하기](https://developer.android.com/training/basics/firstapp?hl=ko)
- [Gradle 빌드 도구](https://gradle.org/)
- [Kotlin 프로그래밍](https://kotlinlang.org/docs/getting-started.html)
