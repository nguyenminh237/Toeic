import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Input } from "../../components/common/Input";
import { ROUTES } from "../../constants/routes";
import { useForm } from "../../hooks/useForm";
import { authService } from "../../services/authService";

export function ResetPasswordPage() {
  const { userId } = useParams();
  const { values, handleChange } = useForm({ newPassword: "", confirmPassword: "" });
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);

  async function onSubmit(event) {
    event.preventDefault();
    setError("");
    if (values.newPassword !== values.confirmPassword) {
      setError("Mật khẩu xác nhận không khớp");
      return;
    }
    setLoading(true);
    try {
      await authService.resetPassword(userId, values.newPassword);
      setDone(true);
    } catch (err) {
      setError(err.message || "Không đặt lại được mật khẩu");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold">Đặt lại mật khẩu</h2>
      <p className="mt-2 text-sm text-slate-500">Liên kết dùng một lần, có hiệu lực trong 20 phút.</p>
      <form className="mt-8 space-y-4" onSubmit={onSubmit}>
        {error ? <Alert tone="error">{error}</Alert> : null}
        {done ? <Alert tone="success">Đã đổi mật khẩu. Hãy đăng nhập lại.</Alert> : null}
        <Input
          label="Mật khẩu mới"
          name="newPassword"
          type="password"
          value={values.newPassword}
          onChange={handleChange}
          required
        />
        <Input
          label="Xác nhận mật khẩu"
          name="confirmPassword"
          type="password"
          value={values.confirmPassword}
          onChange={handleChange}
          required
        />
        <Button type="submit" className="w-full" loading={loading} disabled={done}>
          Cập nhật mật khẩu
        </Button>
      </form>
      <Link to={ROUTES.LOGIN} className="mt-6 inline-block text-sm font-semibold text-primary">
        Đăng nhập
      </Link>
    </div>
  );
}
