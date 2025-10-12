const { onCall } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");
const axios = require("axios");

// 1. 사용할 비밀 키를 정의합니다.
const openaiApiKey = defineSecret("OPENAI_API_KEY");

admin.initializeApp();
const db = admin.firestore();

exports.recommendPlacesByGPT = onCall(
  {
    region: "asia-northeast3",
    cpu: 1,
    timeoutSeconds: 60,
    memory: "512MiB",
    secrets: [openaiApiKey], // 2. 함수가 이 비밀 키를 사용하도록 설정합니다.
  },
  async (request) => {
    try {
      const userId = request.data.userId;
      if (!userId) {
        throw new functions.https.HttpsError("invalid-argument", "userId가 없습니다.");
      }

      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) {
        throw new functions.https.HttpsError("not-found", "사용자 정보가 없습니다.");
      }

      const user = userDoc.data();

      // Firestore 문서의 필드 이름과 일치하도록 변수를 수정합니다.
      const height = user.height || "정보 없음";
      const interestCategory = Array.isArray(user.interestCategory)
        ? user.interestCategory.join(", ")
        : "관심사 없음";
      const interestSeasons = Array.isArray(user.interestSeasons)
        ? user.interestSeasons.join(", ")
        : "선호 계절 없음";

      const prompt = `신장 ${height}cm, 관심사는 ${interestCategory}, 관심계절은 ${interestSeasons} 인 사용자를 위한 레저 장소 5곳을 추천해줘.
      다음 형식의 JSON 배열로 응답해줘:

      [
        {
          "name": "장소 이름",
          "address": "주소",
          "region": "지역 (예: 강릉, 부산)",
          "price": "이용요금 상세 정보"
        },
        ...
      ]

      ※ JSON 형식을 엄격히 지켜줘. 설명 없이 JSON 배열만 응답해줘.
      `;

      const response = await axios.post(
        "https://api.openai.com/v1/chat/completions",
        {
          model: "gpt-3.5-turbo",
          messages: [{ role: "user", content: prompt }],
        },
        {
          headers: {
            // 3. 비밀 키 값을 올바르게 가져와 사용합니다.
            "Authorization": `Bearer ${openaiApiKey.value()}`,
            "Content-Type": "application/json",
          },
        }
      );

      const gptResponseString = response.data.choices[0].message.content;

      // 4. GPT가 보낸 JSON 형식의 문자열을 실제 JSON 객체로 파싱합니다.
      const recommendedPlaces = JSON.parse(gptResponseString);

      // 5. 파싱된 JSON 객체를 결과로 반환합니다.
      return { result: recommendedPlaces };

    } catch (err) {
      console.error("🔥 오류:", err);
      throw new functions.https.HttpsError("internal", "추천을 생성하는데 실패했습니다.");
    }
  }
);