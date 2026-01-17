import { useEffect, useRef, useState } from "react";
import api from "../api/axios";
import BalanceCard from "../components/BalanceCard";
import {
  connectBalanceSocket,
  disconnectBalanceSocket,
} from "../websocket/balanceSocket";

export default function Dashboard() {
  const [balance, setBalance] = useState(0);
  const [walletId, setWalletId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [recentTxs, setRecentTxs] = useState([]);

  const prevBalance = useRef(0);

  // ===============================
  // FETCH BALANCE + TRANSACTIONS
  // ===============================
  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const balanceRes = await api.get("/wallet/balance");
        setBalance(balanceRes.data.balance);
        setWalletId(balanceRes.data.walletId);
        prevBalance.current = balanceRes.data.balance;

        const txRes = await api.get("/wallet/transactions");
        setRecentTxs(txRes.data.slice(0, 5)); // last 5
      } catch (err) {
        console.error("Dashboard load failed", err);
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  // ===============================
  // WEBSOCKET BALANCE UPDATE
  // ===============================
  useEffect(() => {
    if (!walletId) return;

    connectBalanceSocket(walletId, (newBalance) => {
      prevBalance.current = balance;
      setBalance(newBalance);
    });

    return () => disconnectBalanceSocket();
  }, [walletId, balance]);

  if (loading) {
    return (
      <div className="page-center">
        <div className="card">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="page-center">
      <div className="dashboard-wrapper">
        <h1 className="page-title">Dashboard</h1>
        <p className="page-subtitle">Real Time Balance Update!</p>

        {/* BALANCE CARD */}
        <BalanceCard
          balance={balance}
          prevBalance={prevBalance.current}
        />

        {/* RECENT TRANSACTIONS */}
        <div className="card recent-card">
          <h3>🧾 Recent Transactions</h3>

          {recentTxs.length === 0 && (
            <p className="muted">No recent transactions</p>
          )}

          {recentTxs.map((tx) => {
            const isDebit = tx.type === "DEBIT";

            return (
              <div key={tx.id} className="tx-row">
                <div>
                  <span
                    className={`tx-badge ${
                      isDebit ? "debit" : "credit"
                    }`}
                  >
                    {tx.type}
                  </span>
                  <span className="tx-date">
                    {new Date(tx.timestamp).toLocaleDateString()}
                  </span>
                </div>

                <div
                  className={`tx-amount ${
                    isDebit ? "debit" : "credit"
                  }`}
                >
                  {isDebit ? "-" : "+"}₹{tx.amount}
                </div>
              </div>
            );
          })}
        </div>

        <div className="dashboard-hint">
          🔴 Live balance updates enabled
        </div>
      </div>
    </div>
  );
}
