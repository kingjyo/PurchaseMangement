const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Sends notification to admins
 * @param {string} title - Title of the notification
 * @param {string} body - Body of the notification
 */
async function notifyAdmin(title, body) {
  try {
    const adminDoc = await admin.firestore()
        .collection("users")
        .doc("admin")
        .get();

    const fcmToken = adminDoc.get("fcmToken");
    console.log("FCM 토큰:", fcmToken);

    if (!fcmToken) {
      console.log("관리자 FCM 토큰 없음");
      return;
    }

    // 최신 FCM v1 API 사용
    const message = {
      notification: {
        title: title,
        body: body,
      },
      token: fcmToken,
    };

    const response = await admin.messaging().send(message);
    console.log("관리자 푸시알림 전송 성공:", response);
  } catch (e) {
    console.error("관리자 푸시알림 전송 실패:", e);

    // 토큰 무효화 처리
    if (e.code === "messaging/registration-token-not-registered" ||
        e.code === "messaging/invalid-registration-token") {
      console.log("FCM 토큰이 무효함. DB에서 삭제 필요");
    }
  }
}

// 구매요청 Firestore 트리거 (v2 API)
exports.onNewPurchaseRequest = onDocumentCreated(
    {
      document: "users/{id}",
      region: "asia-northeast3",
    },
    async (event) => {
      const snap = event.data;
      const data = snap.data();
      const item = data.item || "구매항목";
      const applicant = data.applicantName || "신청자";

      await notifyAdmin(
          "새 구매신청 도착",
          `${applicant}님의 "${item}" 신청이 등록되었습니다.`,
      );
    },
);
