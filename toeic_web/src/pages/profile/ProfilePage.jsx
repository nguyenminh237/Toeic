import { RefreshCw } from "lucide-react";
import { useState } from "react";
import { Alert } from "../../components/common/Alert";
import { Button } from "../../components/common/Button";
import { Card } from "../../components/common/Card";
import { ErrorState } from "../../components/common/ErrorState";
import { Input } from "../../components/common/Input";
import { PageHeader } from "../../components/common/PageHeader";
import { Spinner } from "../../components/common/Spinner";
import { useAuth } from "../../hooks/useAuth";
import { useForm } from "../../hooks/useForm";
import { userService } from "../../services/userService";
import { formatDateTime } from "../../utils/format";

export function ProfilePage() {
  const { user, refreshProfile, logout, booting } = useAuth();
  const { values, handleChange } = useForm({ currentPassword: "", newPassword: "" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (booting) return <Spinner />;
  if (!user) return <ErrorState error={{ message: "Không tìm thấy hồ sơ" }} />;

  async function onReset(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);
    try {
      await userService.changePassword(values.currentPassword, values.newPassword);
      await logout();
      setMessage("Đã đổi mật khẩu.");
    } catch (err) {
      setError(err.message || "Không đổi được mật khẩu");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        eyebrow="Tài khoản"
        title={user.fullName || user.username}
        description="Thông tin tài khoản và bảo mật."
        actions={
          <Button variant="ghost" onClick={refreshProfile}>
            <RefreshCw className="h-4 w-4" />
            Làm mới
          </Button>
        }
      />
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <dl className="space-y-3 text-sm">
            <div>
              <dt className="text-slate-500">Username</dt>
              <dd className="font-semibold">{user.username}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Email</dt>
              <dd className="font-semibold">{user.email}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Role</dt>
              <dd className="font-semibold">{user.role}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Tạo lúc</dt>
              <dd>{formatDateTime(user.createdAt)}</dd>
            </div>
          </dl>
        </Card>
        <Card>
          <h3 className="mb-4 text-lg font-semibold">Đổi mật khẩu</h3>
          {message ? <Alert tone="success">{message}</Alert> : null}
          {error ? <Alert tone="error">{error}</Alert> : null}
          <form className="mt-4 space-y-4" onSubmit={onReset}>
            <Input label="Mật khẩu hiện tại" name="currentPassword" type="password" value={values.currentPassword} onChange={handleChange} required />
            <Input
              label="Mật khẩu mới"
              name="newPassword"
              type="password"
              value={values.newPassword}
              onChange={handleChange}
              required
            />
            <Button type="submit" loading={loading}>
              Cập nhật
            </Button>
          </form>
          <Button className="mt-6" variant="ghost" onClick={logout}>
            Đăng xuất
          </Button>
        </Card>
      </div>
    </div>
  );
}
