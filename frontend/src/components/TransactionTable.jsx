export default function TransactionTable({ txs }) {
  if (!txs || txs.length === 0) {
    return <p style={styles.empty}>No transactions found</p>;
  }

  return (
    <div style={styles.card}>
      <table style={styles.table}>
        <thead>
          <tr>
            <th>#</th>
            <th>Type</th>
            <th>From</th>
            <th>To</th>
            <th style={{ textAlign: "right" }}>Amount</th>
            <th>Date</th>
          </tr>
        </thead>

        <tbody>
          {txs.map((tx, i) => {
            const isDebit = tx.type === "DEBIT";

            return (
              <tr key={tx.transactionId}>
                <td>{i + 1}</td>

                {/* TYPE */}
                <td>
                  <span
                    style={{
                      ...styles.badge,
                      background: isDebit ? "#fee2e2" : "#dcfce7",
                      color: isDebit ? "#b91c1c" : "#166534",
                    }}
                  >
                    {tx.type}
                  </span>
                </td>

                {/* FROM */}
                <td>
                  {isDebit ? (
                    <span style={styles.me}>You</span>
                  ) : (
                    `Wallet #${tx.counterpartyWalletId}`
                  )}
                </td>

                {/* TO */}
                <td>
                  {isDebit ? (
                    `Wallet #${tx.counterpartyWalletId}`
                  ) : (
                    <span style={styles.me}>You</span>
                  )}
                </td>

                {/* AMOUNT */}
                <td
                  style={{
                    textAlign: "right",
                    fontWeight: "700",
                    color: isDebit ? "#dc2626" : "#16a34a",
                  }}
                >
                  {isDebit ? "-" : "+"}₹{tx.amount}
                </td>

                {/* DATE */}
                <td style={{ color: "#6b7280", fontSize: "0.85rem" }}>
                  {new Date(tx.timestamp).toLocaleString("en-IN", {
                    dateStyle: "medium",
                    timeStyle: "short",
                  })}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

/* ===================== STYLES ===================== */

const styles = {
  card: {
    marginTop: "1rem",
    background: "#fff",
    borderRadius: "16px",
    padding: "1.5rem",
    boxShadow: "0 15px 40px rgba(0,0,0,0.12)",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
  },
  badge: {
    padding: "0.3rem 0.8rem",
    borderRadius: "999px",
    fontSize: "0.75rem",
    fontWeight: "800",
    letterSpacing: "0.04em",
  },
  me: {
    fontWeight: "700",
    color: "#1e3a8a",
  },
  empty: {
    marginTop: "1.5rem",
    textAlign: "center",
    color: "#6b7280",
  },
};
