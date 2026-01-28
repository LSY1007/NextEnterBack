# Advertisement System - 광고 시스템 구현 가이드

## 📋 개요
기업이 등록한 광고를 크레딧 페이지에 동적으로 표시하는 시스템입니다.

## 🎯 주요 기능

### 1. 사용자 기능
- ✅ 활성화된 광고 목록 자동 조회
- ✅ 우선순위에 따른 광고 정렬
- ✅ 광고 클릭 시 페이지 이동 또는 외부 링크 열기
- ✅ 로딩 상태 표시
- ✅ 광고가 없을 때 기본 메시지 표시

### 2. 기업 기능 (향후 구현)
- 광고 생성
- 광고 수정
- 광고 활성화/비활성화
- 광고 삭제
- 우선순위 설정

## 📂 파일 구조

### 백엔드 (Spring Boot)
```
src/main/java/org/zerock/nextenter/advertisement/
├── entity/
│   └── Advertisement.java          # 광고 엔티티
├── dto/
│   ├── AdvertisementDto.java       # 광고 DTO
│   └── AdvertisementRequest.java   # 광고 요청 DTO
├── repository/
│   └── AdvertisementRepository.java # 광고 레포지토리
├── service/
│   └── AdvertisementService.java   # 광고 서비스
└── controller/
    └── AdvertisementController.java # 광고 컨트롤러
```

### 프론트엔드 (React + TypeScript)
```
src/
├── api/
│   └── advertisement.ts            # 광고 API 서비스
└── features/credit/
    └── CreditPage.tsx              # 크레딧 페이지 (수정됨)
```

## 🔧 설치 및 설정

### 1. 데이터베이스 설정
```sql
-- 광고 테이블이 자동으로 생성됩니다 (JPA)
-- 샘플 데이터를 추가하려면:
mysql -u [username] -p [database] < sample_advertisements.sql
```

### 2. 백엔드 실행
```bash
cd NextEnterBack
./gradlew bootRun
```

### 3. 프론트엔드 실행
```bash
cd NextEnterFront
npm install  # 필요시
npm run dev
```

## 📡 API 엔드포인트

### 공개 API
- `GET /api/advertisements/active` - 활성화된 광고 목록 조회

### 기업 전용 API (인증 필요)
- `GET /api/advertisements/company/{companyId}` - 기업의 광고 목록 조회
- `POST /api/advertisements/company/{companyId}` - 광고 생성
- `PUT /api/advertisements/{advertisementId}/company/{companyId}` - 광고 수정
- `PATCH /api/advertisements/{advertisementId}/company/{companyId}/toggle` - 활성화/비활성화
- `DELETE /api/advertisements/{advertisementId}/company/{companyId}` - 광고 삭제

## 💾 데이터 모델

### Advertisement Entity
```java
{
  id: Long,                    // 광고 ID
  companyId: Long,             // 기업 ID
  title: String,               // 광고 제목
  description: String,         // 광고 설명
  backgroundColor: String,     // 배경 색상 (Tailwind CSS)
  buttonText: String,          // 버튼 텍스트
  targetUrl: String,           // 외부 링크 (선택)
  targetPage: String,          // 내부 페이지 메뉴 (선택)
  isActive: Boolean,           // 활성화 상태
  priority: Integer,           // 우선순위
  createdAt: LocalDateTime,    // 생성일시
  updatedAt: LocalDateTime     // 수정일시
}
```

## 🎨 UI 특징

### 광고 카드 디자인
- Gradient 배경 (Tailwind CSS)
- 호버 효과 (scale, shadow)
- 반응형 레이아웃
- 로딩 및 빈 상태 처리

### 배경 색상 예시
- `bg-gradient-to-r from-blue-500 to-purple-500`
- `bg-gradient-to-r from-green-500 to-teal-500`
- `bg-gradient-to-r from-orange-500 to-red-500`
- `bg-gradient-to-r from-indigo-500 to-purple-600`

## 🔄 동작 흐름

1. **사용자가 크레딧 페이지 접속**
   - CreditPage 컴포넌트 마운트
   - useEffect에서 `getActiveAdvertisements()` 호출

2. **광고 데이터 조회**
   - API: `GET /api/advertisements/active`
   - 우선순위 순으로 정렬된 활성 광고 반환

3. **광고 표시**
   - 광고가 있으면: 광고 카드 렌더링
   - 광고가 없으면: 빈 상태 메시지 표시
   - 로딩 중: 로딩 인디케이터 표시

4. **광고 클릭**
   - `targetPage` 있으면: 내부 페이지로 이동
   - `targetUrl` 있으면: 새 탭에서 외부 링크 열기

## 🧪 테스트 방법

### 1. 샘플 데이터 추가
```sql
-- sample_advertisements.sql 실행
mysql -u root -p nextenter < sample_advertisements.sql
```

### 2. API 테스트
```bash
# 활성 광고 조회
curl http://localhost:8080/api/advertisements/active

# Swagger UI에서 테스트
http://localhost:8080/swagger-ui.html
```

### 3. 프론트엔드 테스트
1. 크레딧 페이지 접속
2. "쿠폰 목록" 탭 확인
3. 광고가 표시되는지 확인
4. 광고 클릭 시 이동 확인

## 🚀 향후 개선 사항

### 기능 추가
- [ ] 기업용 광고 관리 페이지
- [ ] 광고 클릭 통계 (조회수, 클릭수)
- [ ] 광고 노출 기간 설정
- [ ] 광고 예산 관리
- [ ] A/B 테스트 기능

### UI 개선
- [ ] 광고 캐러셀 (여러 광고 슬라이드)
- [ ] 광고 애니메이션 효과
- [ ] 모바일 최적화
- [ ] 다크모드 지원

## 📝 주의사항

1. **보안**
   - 기업 전용 API는 인증 필요 (`@PreAuthorize("hasRole('COMPANY')")`)
   - Cross-Origin 설정 확인 (`@CrossOrigin`)

2. **성능**
   - 광고 데이터 캐싱 고려
   - 이미지 최적화 (향후 이미지 추가 시)

3. **데이터**
   - `companyId`는 실제 기업 테이블의 ID와 일치해야 함
   - 광고 우선순위는 높을수록 먼저 표시됨

## 🐛 트러블슈팅

### 광고가 표시되지 않는 경우
1. 백엔드 서버 실행 확인
2. API 응답 확인 (개발자 도구 Network 탭)
3. 데이터베이스에 활성 광고 존재 확인
4. CORS 설정 확인

### API 에러 발생 시
1. 백엔드 로그 확인
2. 데이터베이스 연결 확인
3. JWT 토큰 확인 (기업 API)

## 📞 문의
구현 관련 문의사항이 있으시면 개발팀에 문의해주세요.
