# 数据库启动指南（MySQL + Docker）

使用 Docker 启动 MySQL 8.0，并在首次启动时自动执行初始化脚本创建数据库和表。

## 0. 你需要准备什么

* 安装 Docker Desktop
* （可选）安装 MySQL Workbench 用于可视化查看

先确保 Docker Desktop 已配置代理，否则可能拉取镜像失败。

---

## 1. 拉取项目代码

确认项目根目录存在：

* `docker-compose.yml`
* `db-init/init.sql`

---

## 2. 启动 MySQL（第一次启动会自动建库建表）

在项目根目录执行：

```bash
docker compose up -d
```

检查容器是否启动成功：

```bash
docker ps
```

你应该能看到 `project-mysql` 状态为 `Up`。

---

## 3. 初始化脚本说明（重要）

初始化脚本位于：

* `db-init/` 目录（例如 `db-init/init.sql`）

Docker 的 MySQL 镜像只会在“首次创建数据卷（volume）时”自动执行这些脚本。

如果你修改了 `init.sql` 或第一次启动没成功，需要“重建数据卷”才能重新初始化：

```bash
docker compose down -v
docker compose up -d
```

注意：`down -v` 会清空数据库数据，相当于重置。

---

## 4. 连接数据库（推荐用 MySQL Workbench）

### 4.1 查看端口映射（确定 Workbench 填多少端口）

打开 `docker-compose.yml` 找到：

```yml
ports:
  - "3307:3306"
```

说明：

* 左边 `3307` 是你电脑上的端口（Workbench 填这个）
* 右边 `3306` 是容器内部 MySQL 端口（不用管）

### 4.2 在 Workbench 新建连接

MySQL Workbench → Home → `+` 新建连接：

* Connection Method: `Standard (TCP/IP)`
* Hostname: `127.0.0.1`
* Port: `3307`
* Username: `app`
* Password: `app123`（Store in Vault）

点击 `Test Connection`，成功后保存。

### 4.3 查看数据库和表

连接成功后：

* 左侧 `SCHEMAS` 找到 `social_training`
* 右键 `social_training` → `Set as Default Schema`
* 展开 `Tables` 查看表

如果没看到，点一下刷新按钮，或右键 `SCHEMAS` → `Refresh All`。

---

## 5. 用命令行快速验证（不装 Workbench 也行）

查看有哪些数据库：

```bash
docker exec -it project-mysql mysql -uapp -papp123 -e "SHOW DATABASES;"
```

查看 `social_training` 里有哪些表：

```bash
docker exec -it project-mysql mysql -uapp -papp123 -e "USE social_training; SHOW TABLES;"
```

---

### Q: 拉取 mysql:8.0 失败 / 连接 Docker Hub 超时

校园网常见，需要配置 Docker Desktop 的代理：
Docker Desktop → Settings → Proxies
开启 `Use system proxy` 或手动填写 HTTP/HTTPS proxy（例如 `http://127.0.0.1:7890`），保存后重启 Docker Desktop 再重试。

### Q: 数据库有了但没有表（SHOW TABLES 为空）

通常是初始化脚本没跑或 volume 不是首次创建。重置即可：

```bash
docker compose down -v
docker compose up -d
