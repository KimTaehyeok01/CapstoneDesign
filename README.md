# 🏅 레저스포츠 맞춤형 추천 플랫폼: MyLeisure
> **2025 소프트웨어 캡스톤 디자인 II 프로젝트** > 사용자의 심리 상태와 위치 데이터를 기반으로 최적의 레저 활동을 제안하는 지능형 안드로이드 애플리케이션

---

## 📺 Project Overview
* **개발 기간**: 2025.03 - 2025.09 (약 6개월)
* **팀원**: 김태혁(팀장, Backend/DB), 박영민(Frontend), 한솔웅(UI/UX)
* [cite_start]**핵심 가치**: 의사결정 피로도 감소, 사용자 컨디션 기반 맞춤형 레저 큐레이션 [cite: 439, 466]

---

## ✨ Key Features (핵심 기능)

### 🧠 1. 심리 상태 기반 AI 추천 (Psychological Curation)
* [cite_start]**감정/에너지 분석**: 사용자가 직접 슬라이드 바를 통해 현재 기분과 에너지 레벨을 입력하면, 분석 알고리즘을 통해 최적의 스포츠 장르를 매칭합니다. [cite: 439, 448, 450]
* [cite_start]**상황 맞춤 필터링**: 실내/실외 선호도에 따라 맞춤형 추천 리스트를 카드 형태로 제공합니다. [cite: 452, 457]

### 📍 2. 위치 및 날씨 연동 서비스 (Geo-Location)
* [cite_start]**내 주변 추천**: Google Maps API와 연동하여 현재 위치 반경 3km 이내의 레저 장소를 실시간으로 탐색합니다. [cite: 305, 310, 311]
* [cite_start]**실시간 날씨 API**: OpenWeather API를 통해 현재 기온과 하늘 상태를 반영하여 활동 가능 여부를 직관적으로 보여줍니다. [cite: 176, 442]

### 🔍 3. 스마트 검색 및 필터링
* [cite_start]**멀티 필터**: 유형, 계절, 지역별 다중 조건을 조합하여 Firestore 데이터베이스에서 고도화된 검색을 수행합니다. [cite: 183, 207]
* [cite_start]**랜덤 추천**: '오늘의 랜덤 추천' 주사위 기능을 통해 결정 장애를 겪는 사용자에게 재미 요소를 가미한 추천을 제공합니다. [cite: 162, 164]

---

## 🛠 Tech Stack (기술 스택)

### **Frontend & Mobile**
<img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white"> <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Google%20Maps%20API-4285F4?style=for-the-badge&logo=google-maps&logoColor=white">

### **Backend & Infrastructure**
<img src="https://img.shields.io/badge/Firebase%20Auth-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"> <img src="https://img.shields.io/badge/Cloud%20Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"> <img src="https://img.shields.io/badge/Firebase%20Storage-FFCA28?style=for-the-badge&logo=firebase&logoColor=black">

### **Integrations**
<img src="https://img.shields.io/badge/OpenWeather%20API-EB6E4B?style=for-the-badge&logo=openweather&logoColor=white"> <img src="https://img.shields.io/badge/Glide-A8A8A8?style=for-the-badge&logo=glide&logoColor=black">

---

## 🏛 System Architecture

---

## 📈 Impact & Expectation (기대 효과)
* [cite_start]**의사결정 피로도 해결**: 개인의 컨디션(기분, 에너지)만으로 활동을 제안받아 선택의 고민을 혁신적으로 줄임 [cite: 466]
* [cite_start]**공유 및 확장성**: 추천 장소를 카카오톡 등으로 즉시 공유하거나 지도로 주변 환경을 파악하는 등 직관적인 UX 구현 [cite: 461, 463, 467]

---

## 📸 Screenshots
| 메인 화면 | 심리 입력 | 추천 결과 | 지도 보기 |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/메인_이미지_주소" width="200"> | <img src="https://github.com/user-attachments/assets/심리입력_이미지_주소" width="200"> | <img src="https://github.com/user-attachments/assets/결과_이미지_주소" width="200"> | <img src="https://github.com/user-attachments/assets/지도_이미지_주소" width="200"> |

---

## 📂 Project Structure
* [cite_start]`OnboardingActivity`: 자동 로그인 여부에 따른 초기 진입점 처리 [cite: 2, 7, 12]
* [cite_start]`SearchActivity`: Firestore 연동 다중 조건 검색 로직 [cite: 182, 183]
* [cite_start]`PlaceDetailActivity`: 장소별 상세 정보 로드 및 즐겨찾기(찜) 기능 관리 [cite: 274, 280, 283]
* [cite_start]`FeelingInputActivity`: 사용자 심리 데이터 수집 및 Firestore 저장 [cite: 403, 404, 411]
