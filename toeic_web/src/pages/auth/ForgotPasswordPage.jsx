import { useState } from "react";
import { Link } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { ROUTES } from "../../constants/routes";
import { useForm } from "../../hooks/useForm";
import { authService } from "../../services/authService";

export function ForgotPasswordPage() {
  const { values, handleChange } = useForm({ email: "" });
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await authService.forgotPassword(values.email);
      setDone(true);
    } catch (err) {
      setError(err.message || "Không gửi được yêu cầu");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold">Quên mật khẩu</h2>
      <p className="mt-2 text-sm text-slate-500">Nhập email đã đăng ký để xác nhận yêu cầu đặt lại mật khẩu.</p>
      <form className="mt-8 space-y-4" onSubmit={onSubmit}>
        {error ? <Alert tone="error">{error}</Alert> : null}
        {done ? (
          <Alert tone="warning">
            Nếu email đã đăng ký, bạn sẽ nhận được liên kết đặt lại mật khẩu. Hãy kiểm tra cả thư mục spam.
          </Alert>
        ) : null}
        <Input
          label="Email"
          name="email"
          type="email"
          value={values.email}
          onChange={handleChange}
          required
        />
        <Button type="submit" className="w-full" loading={loading}>
          Gửi yêu cầu
        </Button>
      </form>
      <Link to={ROUTES.LOGIN} className="mt-6 inline-block text-sm font-semibold text-primary">
        Quay lại đăng nhập
      </Link>
    </div>
  );
}
