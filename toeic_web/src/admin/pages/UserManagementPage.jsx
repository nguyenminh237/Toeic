import { Search } from "lucide-react";
import { useState } from "react";
import { Alert } from "../../components/common/Alert";
import { Badge } from "../../components/common/Badge";
import { Button } from "../../components/common/Button";
import { ErrorState } from "../../components/common/ErrorState";
import { Input } from "../../components/common/Input";
import { Spinner } from "../../components/common/Spinner";
import { formatDateTime } from "../../utils/format";
import { DataTable } from "../components/DataTable";
import { useAdminUsers } from "../hooks/useAdminUsers";

export function UserManagementPage() {
  const { users, query, setQuery, loading, error, reload, toggleStatus, changeRole } = useAdminUsers();
  const [actionError, setActionError] = useState("");

  async function onSearch(event) {
    event.preventDefault();
    reload(query);
  }

  if (loading) return <Spinner label="Đang tải users..." />;
  if (error) return <ErrorState error={error} onRetry={() => reload(query)} />;

  return (
    <div>
      <form className="mb-5 flex gap-3" onSubmit={onSearch}>
        <Input
          name="q"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Tìm username / email"
          className="max-w-sm flex-1"
        />
        <Button type="submit">
          <Search className="h-4 w-4" />
          Tìm
        </Button>
      </form>
      {actionError ? <div className="mb-4"><Alert tone="error">{actionError}</Alert></div> : null}
      <DataTable
        rows={users}
        columns={[
          { key: "username", label: "Username" },
          { key: "email", label: "Email" },
          { key: "fullName", label: "Họ tên" },
          { key: "role", label: "Role", render: (row) => <Badge tone={row.role === "ADMIN" ? "amber" : "blue"}>{row.role}</Badge> },
          {
            key: "enabled",
            label: "Trạng thái",
            render: (row) => <Badge tone={row.enabled === false ? "red" : "green"}>{row.enabled === false ? "Khóa" : "Hoạt động"}</Badge>,
          },
          { key: "createdAt", label: "Tạo lúc", render: (row) => formatDateTime(row.createdAt) },
          {
            key: "actions",
            label: "",
            render: (row) => (
              <div className="flex flex-wrap gap-2">
                <Button
                  variant="secondary"
                  onClick={async () => {
                    try {
                      await toggleStatus(row);
                    } catch (err) {
                      setActionError(err.message);
                    }
                  }}
                >
                  {row.enabled === false ? "Mở khóa" : "Khóa"}
                </Button>
                <Button
                  variant="ghost"
                  onClick={async () => {
                    try {
                      await changeRole(row, row.role === "ADMIN" ? "USER" : "ADMIN");
                    } catch (err) {
                      setActionError(err.message);
                    }
                  }}
                >
                  Đổi role
                </Button>
              </div>
            ),
          },
        ]}
      />
    </div>
  );
}
