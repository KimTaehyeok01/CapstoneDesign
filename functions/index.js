const { onCall } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();

// ✅ Gen 2 + Node.js 22 함수 선언
exports.recommendPlacesByGPT = onCall(
  {
    region: "asia-northeast3",
    cpu: 1,
    timeoutSeconds: 60,
    memory: "512MiB",
  },
  async (data, context) => {
    try {
      const userId = data.userId;
      if (!userId) throw new Error("userId가 없습니다.");

      const userDoc = await db.collection("users").doc(userId).get();
      if (!userDoc.exists) throw new Error("사용자 정보 없음");

      const user = userDoc.data();
      const height = user.height || "알 수 없음";
      const season = user.season || "계절 미지정";
      const interests = Array.isArray(user.interests)
        ? user.interests.join(", ")
        : "관심사 없음";

      const prompt = `신장 ${height}cm, 계절은 ${season}, 관심사는 ${interestCategory}, 관심계절은 ${interestSeasons} 인 사용자를 위한 레저 장소 5곳을 추천해줘.
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
            Authorization: `Bearer ${process.env.OPENAI_API_KEY || ""}`,
            "Content-Type": "application/json",
          },
        }
      );

      return { result: response.data.choices[0].message.content };
    } catch (err) {
      console.error("🔥 오류:", err.message);
      throw new Error("추천 실패: " + err.message);
    }
  }
);
