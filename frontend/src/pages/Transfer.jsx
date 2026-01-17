import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

export default function Transfer() {
  const [toWalletId, setToWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();

    if (!toWalletId || !amount || Number(amount) <= 0) {
      alert("Enter valid wallet ID and amount");
      return;
    }

    try {
      setLoading(true);

      await api.post("/wallet/transfer", {
        toWalletId: Number(toWalletId),
        amount: Number(amount),
      });

      alert("✅ Transfer successful");
      navigate("/dashboard");

    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || "Transfer failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.wrapper}>
      <div style={styles.card}>
        <h2 style={styles.title}>💸 Transfer Money</h2>
        <p style={styles.subtitle}>
          Send money securely from your wallet
        </p>

        <form onSubmit={submit} style={styles.form}>
          {/* TO WALLET */}
          <label style={styles.label}>Recipient Wallet ID</label>
          <input
            type="number"
            placeholder="e.g. 102"
            value={toWalletId}
            onChange={(e) => setToWalletId(e.target.value)}
            style={styles.input}
            required
          />

          {/* AMOUNT */}
          <label style={styles.label}>Amount (₹)</label>
          <input
            type="number"
            placeholder="e.g. 500"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            style={styles.input}
            required
          />

          <button
            type="submit"
            disabled={loading}
            style={{
              ...styles.button,
              opacity: loading ? 0.7 : 1,
            }}
          >
            {loading ? "Processing..." : "Send Money"}
          </button>
        </form>
      </div>
    </div>
  );
}

/* ===================== STYLES ===================== */
const styles = {
  wrapper: {
    minHeight: "80vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "1rem",
  },
  card: {
    width: "100%",
    maxWidth: "420px",
    background: "#ffffff",
    borderRadius: "16px",
    padding: "2.2rem",
    boxShadow: "0 15px 40px rgba(0,0,0,0.12)",
  },
  title: {
    textAlign: "center",
    marginBottom: "0.4rem",
  },
  subtitle: {
    textAlign: "center",
    color: "#6b7280",
    fontSize: "0.9rem",
    marginBottom: "1.8rem",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "0.9rem",
  },
  label: {
    fontSize: "0.85rem",
    fontWeight: "600",
    color: "#374151",
  },
  input: {
    padding: "0.6rem 0.7rem",
    borderRadius: "8px",
    border: "1px solid #d1d5db",
    fontSize: "0.9rem",
  },
  button: {
    marginTop: "1rem",
    padding: "0.65rem",
    borderRadius: "10px",
    border: "none",
    background: "linear-gradient(90deg, #2563eb, #1e40af)",
    color: "#fff",
    fontSize: "0.95rem",
    fontWeight: "600",
    cursor: "pointer",
  },
};
