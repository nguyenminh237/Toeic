import { UserPlus } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { ROUTES } from "../../constants/routes";
import { useAuth } from "../../hooks/useAuth";
import { useForm } from "../../hooks/useForm";

export function RegisterPage() {
  const { register, login } = useAuth();
  const navigate = useNavigate();
  const { values, handleChange } = useForm({
    username: "",
    email: "",
    password: "",
    fullName: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(values);
      await login({ username: values.username, password: values.password });
      navigate(ROUTES.DASHBOARD, { replace: true });
    } catch (err) {
      setError(err.message || "Đăng ký thất bại");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold">Tạo tài khoản</h2>
      <p className="mt-2 text-sm text-slate-500">Username tối thiểu 3 ký tự, mật khẩu tối thiểu 6 ký tự.</p>
      <form className="mt-8 space-y-4" onSubmit={onSubmit}>
        {error ? <Alert tone="error">{error}</Alert> : null}
        <Input label="Họ tên" name="fullName" value={values.fullName} onChange={handleChange} />
        <Input label="Tên đăng nhập" name="username" value={values.username} onChange={handleChange} required />
        <Input label="Email" name="email" type="email" value={values.email} onChange={handleChange} required />
        <Input
          label="Mật khẩu"
          name="password"
          type="password"
          value={values.password}
          onChange={handleChange}
          required
        />
        <Button type="submit" className="w-full" loading={loading}>
          <UserPlus className="h-4 w-4" />
          Đăng ký
        </Button>
      </form>
      <p className="mt-6 text-sm text-slate-500">
        Đã có tài khoản?{" "}
        <Link to={ROUTES.LOGIN} className="font-semibold text-primary">
          Đăng nhập
        </Link>
      </p>
    </div>
  );
}
