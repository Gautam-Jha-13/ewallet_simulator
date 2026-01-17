import { useState } from "react";
import api from "../api/axios";
import { useNavigate, Link } from "react-router-dom";

export default function Register() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    initialBalance: 1000,
  });

  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();

    // ✅ Frontend validation
    if (form.initialBalance < 1000) {
      alert("Minimum initial balance is ₹1000");
      return;
    }

    try {
      await api.post("/setup/user", form);
      alert("Account created successfully");
      navigate("/login");
    } catch (err) {
      alert(err.response?.data?.message || "Registration failed");
    }
  };

  return (
    <div className="container" style={{ maxWidth: "420px" }}>
      <h2 style={{ textAlign: "center", marginBottom: "1rem" }}>
        Create Wallet Account
      </h2>

      <form onSubmit={submit} className="card">
        {/* NAME */}
        <label>Full Name</label>
        <input
          type="text"
          placeholder="John Doe"
          required
          value={form.name}
          onChange={(e) =>
            setForm({ ...form, name: e.target.value })
          }
        />

        {/* EMAIL */}
        <label>Email</label>
        <input
          type="email"
          placeholder="john@email.com"
          required
          value={form.email}
          onChange={(e) =>
            setForm({ ...form, email: e.target.value })
          }
        />

        {/* PASSWORD */}
        <label>Password</label>
        <input
          type="password"
          placeholder="********"
          required
          value={form.password}
          onChange={(e) =>
            setForm({ ...form, password: e.target.value })
          }
        />

        {/* INITIAL BALANCE */}
        <label>Initial Balance (₹)</label>
        <input
          type="number"
          min={1000}
          step={100}
          value={form.initialBalance}
          onChange={(e) =>
            setForm({
              ...form,
              initialBalance: Number(e.target.value),
            })
          }
        />

        <small style={{ color: "#666" }}>
          Minimum balance required: ₹1000
        </small>

        <button type="submit" style={{ marginTop: "1rem" }}>
          Create Account
        </button>
      </form>

      <p style={{ textAlign: "center", marginTop: "1rem" }}>
        Already have an account?{" "}
        <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
