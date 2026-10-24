import { LogIn } from "lucide-react";
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { ROUTES } from "../../constants/routes";
import { useAuth } from "../../hooks/useAuth";
import { useForm } from "../../hooks/useForm";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { values, handleChange } = useForm({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const from = location.state?.from || ROUTES.DASHBOARD;

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(values);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || "Đăng nhập thất bại");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold">Đăng nhập</h2>
      <p className="mt-2 text-sm text-slate-500">Dùng username và mật khẩu đã đăng ký.</p>
      <form className="mt-8 space-y-4" onSubmit={onSubmit}>
        {error ? <Alert tone="error">{error}</Alert> : null}
        <Input
          label="Tên đăng nhập"
          name="username"
          value={values.username}
          onChange={handleChange}
          required
        />
        <Input
          label="Mật khẩu"
          name="password"
          type="password"
          value={values.password}
          onChange={handleChange}
          required
        />
        <div className="flex justify-end">
          <Link to={ROUTES.FORGOT_PASSWORD} className="text-sm font-medium text-primary">
            Quên mật khẩu?
          </Link>
        </div>
        <Button type="submit" className="w-full" loading={loading}>
          <LogIn className="h-4 w-4" />
          Vào học
        </Button>
      </form>
      <p className="mt-6 text-sm text-slate-500">
        Chưa có tài khoản?{" "}
        <Link to={ROUTES.REGISTER} className="font-semibold text-primary">
          Đăng ký
        </Link>
      </p>
    </div>
  );
}
