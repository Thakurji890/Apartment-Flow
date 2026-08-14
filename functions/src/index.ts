import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as crypto from "crypto";

admin.initializeApp();
const db = admin.firestore();

/**
 * Utility to generate a deterministic idempotency key
 */
function generateIdempotentId(eventType: string, entityId: string, recipientId: string): string {
    const hash = crypto.createHash("sha256");
    hash.update(`${eventType}_${entityId}_${recipientId}`);
    return hash.digest("hex");
}

/**
 * Utility to send a notification to a specific user
 */
async function dispatchNotification(
    recipientId: string, 
    notificationData: any,
    eventType: string,
    entityId: string
) {
    // 1. Check Preferences
    const prefsDoc = await db.collection("users").doc(recipientId).collection("notificationPreferences").doc("default").get();
    const prefs = prefsDoc.data() || {};

    const type = notificationData.type;
    
    // Evaluate explicit disabled categories
    if (type === "EXPENSE" && prefs.expensesEnabled === false) return;
    if (type === "SETTLEMENT" && prefs.settlementsEnabled === false) return;
    if (type === "CHORE" && prefs.choresEnabled === false) return;

    // Evaluate Quiet Hours
    if (prefs.quietHoursEnabled) {
        const currentHour = new Date().getHours();
        const start = prefs.quietHoursStartHour || 22;
        const end = prefs.quietHoursEndHour || 7;
        let inQuietHours = false;
        
        if (start > end) {
            inQuietHours = currentHour >= start || currentHour < end;
        } else {
            inQuietHours = currentHour >= start && currentHour < end;
        }

        // Delay or drop non-critical notifications
        if (inQuietHours && notificationData.priority !== "CRITICAL" && notificationData.priority !== "HIGH") {
            functions.logger.info(`Quiet hours active for ${recipientId}, skipping non-critical notification.`);
            return;
        }
    }

    // 2. Generate Idempotent ID
    const notificationId = generateIdempotentId(eventType, entityId, recipientId);

    // 3. Write to Inbox
    const inboxRef = db.collection("users").doc(recipientId).collection("notifications").doc(notificationId);
    
    // Check if it already exists to prevent duplicate
    const existing = await inboxRef.get();
    if (existing.exists) {
        functions.logger.info(`Duplicate notification suppressed: ${notificationId}`);
        return;
    }

    await inboxRef.set({
        ...notificationData,
        id: notificationId,
        createdAt: Date.now(),
        isRead: false
    });

    // 4. Send via FCM
    const tokensSnapshot = await db.collection("users").doc(recipientId).collection("fcmTokens").get();
    const tokens = tokensSnapshot.docs.map(doc => doc.id);

    if (tokens.length > 0) {
        const payload = {
            notification: {
                title: notificationData.title,
                body: notificationData.body
            },
            data: {
                deepLink: notificationData.deepLink || "",
                channelId: getChannelId(type),
                type: type
            },
            tokens: tokens
        };

        try {
            const response = await admin.messaging().sendMulticast(payload);
            
            // Clean up invalid tokens
            if (response.failureCount > 0) {
                const failedTokens: string[] = [];
                response.responses.forEach((resp, idx) => {
                    if (!resp.success) {
                        if (resp.error?.code === "messaging/invalid-registration-token" ||
                            resp.error?.code === "messaging/registration-token-not-registered") {
                            failedTokens.push(tokens[idx]);
                        }
                    }
                });
                
                if (failedTokens.length > 0) {
                    const batch = db.batch();
                    failedTokens.forEach(token => {
                        batch.delete(db.collection("users").doc(recipientId).collection("fcmTokens").doc(token));
                    });
                    await batch.commit();
                }
            }
        } catch (error) {
            functions.logger.error("Error sending FCM:", error);
        }
    }
}

function getChannelId(type: string): string {
    switch (type) {
        case "EXPENSE":
        case "SETTLEMENT": return "financial_channel";
        case "CHORE": return "chores_channel";
        case "APARTMENT": return "apartment_channel";
        case "ACCOUNT": return "security_channel";
        default: return "general_channel";
    }
}

// ----------------------------------------------------------------------
// Example Trigger: Chore Assigned
// ----------------------------------------------------------------------
export const onChoreCreated = functions.firestore
    .document("apartments/{apartmentId}/chores/{choreId}")
    .onCreate(async (snap, context) => {
        const chore = snap.data();
        const { apartmentId, choreId } = context.params;

        if (!chore.assignedTo) return;

        const notificationData = {
            apartmentId: apartmentId,
            type: "CHORE",
            title: "New Chore Assigned",
            body: `You have been assigned: ${chore.name}`,
            priority: "NORMAL",
            deepLink: `app://apartmentflow/chore_details/${apartmentId}/${choreId}`,
            relatedEntityType: "CHORE",
            relatedEntityId: choreId
        };

        await dispatchNotification(
            chore.assignedTo, 
            notificationData, 
            "CHORE_ASSIGNED", 
            choreId
        );
    });

// ----------------------------------------------------------------------
// Example Trigger: Settlement Requested
// ----------------------------------------------------------------------
export const onSettlementCreated = functions.firestore
    .document("apartments/{apartmentId}/settlements/{settlementId}")
    .onCreate(async (snap, context) => {
        const settlement = snap.data();
        const { apartmentId, settlementId } = context.params;

        // Ensure this is a settlement where debtor is requesting confirmation from creditor
        if (settlement.status !== "PENDING") return;

        const notificationData = {
            apartmentId: apartmentId,
            type: "SETTLEMENT",
            title: "Settlement Request",
            body: `Someone requested to settle a debt of $${settlement.amount}.`,
            priority: "HIGH",
            deepLink: `app://apartmentflow/settlement_details/${apartmentId}/${settlementId}`,
            relatedEntityType: "SETTLEMENT",
            relatedEntityId: settlementId
        };

        // Notify the creditor
        await dispatchNotification(
            settlement.creditorId, 
            notificationData, 
            "SETTLEMENT_REQUESTED", 
            settlementId
        );
    });

