const functions = require("firebase-functions");
const { onCall } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");
const axios = require("axios");

// 사용할 비밀 키를 정의합니다.
const openaiApiKey = defineSecret("OPENAI_API_KEY");

admin.initializeApp();
const db = admin.firestore();

// 1. AI 장소 추천 함수
exports.recommendPlacesByGPT = onCall(
  {
    region: "asia-northeast3",
    cpu: 1,
    timeoutSeconds: 60,
    memory: "512MiB",
    secrets: [openaiApiKey],
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
      const height = user.height || "정보 없음";
      const interestCategory = Array.isArray(user.interestCategory) ? user.interestCategory.join(", ") : "관심사 없음";
      const interestSeasons = Array.isArray(user.interestSeasons) ? user.interestSeasons.join(", ") : "선호 계절 없음";

      const prompt = `신장 ${height}cm, 관심사는 ${interestCategory}, 관심계절은 ${interestSeasons} 인 사용자를 위한 레저 장소 5곳을 추천해줘.
      다음 형식의 JSON 배열로 응답해줘:
      [
        {
          "name": "장소 이름",
          "address": "주소",
          "region": "지역 (예: 강릉, 부산)",
          "price": "이용요금 상세 정보"
        }
      ]
      ※ JSON 형식을 엄격히 지켜줘. 설명 없이 JSON 배열만 응답해줘.`;

      const response = await axios.post(
        "https://api.openai.com/v1/chat/completions",
        {
          model: "gpt-3.5-turbo",
          messages: [{ role: "user", content: prompt }],
        },
        {
          headers: {
            "Authorization": `Bearer ${openaiApiKey.value()}`,
            "Content-Type": "application/json",
          },
        }
      );

      const gptResponseString = response.data.choices[0].message.content;
      const recommendedPlaces = JSON.parse(gptResponseString);
      return { result: recommendedPlaces };

    } catch (err) {
      console.error("🔥 GPT 추천 함수 오류:", err);
      throw new functions.https.HttpsError("internal", "추천을 생성하는데 실패했습니다.");
    }
  }
);

// 2. 문의 답변 완료 시 푸시 알림 보내는 함수
exports.sendNotificationOnInquiryUpdate = functions.firestore
  .document("inquiries/{inquiryId}")
  .onUpdate(async (change, context) => {
    const beforeData = change.before.data();
    const afterData = change.after.data();

    if (beforeData.status === "pending" && afterData.status === "resolved") {
      const userId = afterData.userId;
      const inquiryTitle = afterData.title;

      if (!userId) {
        return console.log("User ID is missing.");
      }

      const userDoc = await admin.firestore().collection("users").doc(userId).get();
      if (!userDoc.exists) {
        return console.log("User document not found for user:", userId);
      }

      const fcmToken = userDoc.data().fcmToken;
      if (!fcmToken) {
        return console.log("FCM token is missing for user:", userId);
      }

      const payload = {
        notification: {
          title: "문의에 대한 답변이 완료되었습니다.",
          body: `'${inquiryTitle}' 문의에 대한 답변을 확인해보세요!`,
        },
      };

      try {
        await admin.messaging().sendToDevice(fcmToken, payload);
        console.log("Notification sent successfully to user:", userId);
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    }
    return null;
  });