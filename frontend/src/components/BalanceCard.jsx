import { useEffect, useState } from "react";

export default function BalanceCard({ balance, prevBalance }) {
  const [displayBalance, setDisplayBalance] = useState(balance);
  const isIncrease = balance >= prevBalance;

  // Animate number
  useEffect(() => {
    let start = displayBalance;
    let end = balance;
    let duration = 300;
    let startTime = null;

    function animate(ts) {
      if (!startTime) startTime = ts;
      const progress = Math.min((ts - startTime) / duration, 1);
      setDisplayBalance(Math.floor(start + (end - start) * progress));
      if (progress < 1) requestAnimationFrame(animate);
    }

    requestAnimationFrame(animate);
  }, [balance]);

  return (
    <div
      className={`balance-card ${
        isIncrease ? "up pulse-green" : "down pulse-red"
      }`}
    >
      <div className="balance-title">Wallet Balance</div>
      <div className="balance-amount">₹ {displayBalance}</div>
    </div>
  );
}
