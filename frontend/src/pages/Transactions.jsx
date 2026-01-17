import { useEffect, useState } from "react";
import api from "../api/axios";
import TransactionTable from "../components/TransactionTable";

export default function Transactions() {
  const [txs, setTxs] = useState([]);
  const [walletId, setWalletId] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Get wallet info
        const walletRes = await api.get("/wallet/balance");
        setWalletId(walletRes.data.walletId);

        // Get transactions
        const txRes = await api.get("/wallet/transactions");
        setTxs(txRes.data);
      } catch (err) {
        console.error("Failed to load transactions", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <p style={{ textAlign: "center" }}>Loading transactions...</p>;
  }

  return (
    <div style={styles.wrapper}>
      <div style={styles.card}>
        <h2 style={styles.title}>📜 Transaction History</h2>
        <p style={styles.subtitle}>
          All incoming and outgoing wallet activity
        </p>

        <TransactionTable txs={txs} myWalletId={walletId} />
      </div>
    </div>
  );
}

/* ===================== STYLES ===================== */
const styles = {
  wrapper: {
    minHeight: "80vh",
    display: "flex",
    justifyContent: "center",
    padding: "2rem 1rem",
  },
  card: {
    width: "100%",
    maxWidth: "900px",
    background: "#fff",
    borderRadius: "16px",
    padding: "2rem",
    boxShadow: "0 15px 40px rgba(0,0,0,0.12)",
  },
  title: {
    marginBottom: "0.4rem",
  },
  subtitle: {
    color: "#6b7280",
    fontSize: "0.9rem",
    marginBottom: "1.5rem",
  },
};
