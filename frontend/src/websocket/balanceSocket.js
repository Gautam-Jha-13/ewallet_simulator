import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

let stompClient = null;
let subscription = null;

/**
 * Connect WebSocket for real-time wallet balance updates
 */
export const connectBalanceSocket = (walletId, onBalanceUpdate) => {
  const token = localStorage.getItem("token");

  if (!token) {
    console.warn("⚠️ No JWT token found, WebSocket not connected");
    return;
  }

  // Prevent duplicate connections
  if (stompClient && stompClient.active) {
    return;
  }

  const socket = new SockJS("http://localhost:8080/ws");

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    debug: () => {},

    onConnect: () => {
      console.log("✅ WebSocket connected");

      // Clean previous subscription
      if (subscription) {
        subscription.unsubscribe();
      }

      subscription = stompClient.subscribe(
        `/topic/wallet/${walletId}`,
        (message) => {
          const balance = JSON.parse(message.body);
          onBalanceUpdate(balance);
        }
      );
    },

    onStompError: (frame) => {
      console.error("❌ STOMP error:", frame.headers["message"]);
    },

    onWebSocketClose: () => {
      console.log("🔌 WebSocket closed");
    },
  });

  stompClient.activate();
};

/**
 * Disconnect WebSocket safely
 */
export const disconnectBalanceSocket = () => {
  if (subscription) {
    subscription.unsubscribe();
    subscription = null;
  }

  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    console.log("🔌 WebSocket disconnected");
  }
};
