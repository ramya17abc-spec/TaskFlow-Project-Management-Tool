# TaskFlow — Smart Team Workspace

TaskFlow is a responsive project-management workspace with a Next.js frontend and a separate Java 21 / Spring Boot REST API. The API uses MySQL, Spring Data JPA, Hibernate, Spring Security, BCrypt password hashing, and short-lived signed JWT access tokens. The frontend uses SWR for authenticated API data; the sample workspace is clearly marked as demo mode and does not persist demo edits across reloads.

## Features

- Email/password sign-up and sign-in; BCrypt (cost 12) password hashing and 1-hour JWTs.
- Project creation, project membership, team member listing, and profile updates.
- Kanban columns with native drag-and-drop, task assignment, priority, deadline, status, and comments.
- Search and priority filtering, notifications, project progress metrics, and responsive navigation.
- API authorization checks for project membership, admin-only project changes/invites, and per-user notifications.
- Request validation, structured API errors, CORS allow-list, MySQL persistence, and a seeded development workspace.

## Repository layout

```text
app/                         Next.js App Router shell and global styling
components/taskflow-workspace.tsx  Interactive frontend and API client
backend/                     Spring Boot API and MySQL configuration
  src/main/java/com/taskflow/api/
  src/main/resources/application.yml
  src/test/java/             Unit tests
  docker-compose.yml         MySQL + API containers
  Dockerfile
  pom.xml
.vscode/                     Suggested Java tasks and launch setup
```

## Requirements

- Node.js 20+ and pnpm 12 (the repository pins pnpm in `package.json`).
- JDK 21 and Maven 3.9+ for running the backend outside Docker.
- Docker Desktop / Docker Engine for the easiest MySQL setup.

## Run the full app with Docker MySQL + local Spring Boot

1. Start MySQL from the `backend` directory:

   ```bash
   cd backend
   docker compose up -d mysql
   ```

2. In a second terminal, start Spring Boot from `backend`:

   ```bash
   mvn spring-boot:run
   ```

   The API listens at `http://localhost:8080`. On first startup, Hibernate creates/updates the schema and the development seed creates a sample workspace.

3. In a third terminal at the repository root, install and start the frontend:

   ```bash
   pnpm install
   pnpm dev
   ```

   Open `http://localhost:3000`. The API URL defaults to `http://localhost:8080`; set `NEXT_PUBLIC_TASKFLOW_API_URL` if it differs. Set `FRONTEND_ORIGIN` in the backend environment when the frontend is served from a different origin.

### Demo sign-in

- Email: `demo@taskflow.local`
- Password: `TaskFlow123!`

The seed also creates `olivia@taskflow.local`, `phoenix@taskflow.local`, and `lana@taskflow.local`, each with the same development-only password. Change or disable the seed before deploying: set `SEED_DEMO=false` and use a unique `JWT_SECRET` of at least 32 bytes. Never use the included local MySQL or JWT credentials in production.

## Run the API and MySQL together in Docker

From `backend/`:

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`. To run the frontend at the same time, use the root commands above in another terminal. To stop services, run `docker compose down` from `backend/`.

## Run the backend without Docker

Create a MySQL database and user, then export the variables below (or put them in your IDE run configuration):

```text
DB_URL=jdbc:mysql://localhost:3306/taskflow?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=taskflow
DB_PASSWORD=<your-local-db-password>
JWT_SECRET=<random-secret-at-least-32-bytes>
JWT_EXPIRATION_MS=3600000
FRONTEND_ORIGIN=http://localhost:3000
SEED_DEMO=true
```

Example random secret generation:

```bash
openssl rand -base64 48
```

Then run from `backend/`:

```bash
mvn test
mvn spring-boot:run
```

`backend/.env.example` lists the configuration names. Spring Boot does not automatically load a `.env` file; configure variables in your shell, Docker Compose, or VS Code launch configuration.

## VS Code setup

1. Open the repository root in VS Code.
2. Install the recommended Java Extension Pack (Red Hat Java, Maven for Java, and Debugger for Java) and the Extension Pack for Java.
3. Install the JDK 21 and Maven 3.9+ extensions/toolchain on your machine.
4. Use **Terminal → Run Task** for the included `TaskFlow: Run API` and `TaskFlow: Run frontend` tasks, or use the `TaskFlow API` launch configuration under **Run and Debug**.
5. To run integration flows, start MySQL first with `docker compose up -d mysql` from `backend/`.

## REST API

All routes are under `/api`. Except health, sign-up, and login, send `Authorization: Bearer <token>`.

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/health` | Liveness check |
| POST | `/api/auth/signup` | Create user and return token |
| POST | `/api/auth/login` | Authenticate and return token |
| GET | `/api/auth/me` | Current user |
| GET / PATCH | `/api/users/me` | Read/update current profile |
| GET / POST | `/api/projects` | List/create projects |
| GET / PUT | `/api/projects/{id}` | Read/update a project (admin for update) |
| GET / POST | `/api/projects/{id}/members` | List/add project members (admin for add) |
| GET / POST | `/api/projects/{id}/tasks` | Filter/create tasks |
| GET | `/api/tasks` | Tasks across all projects accessible to the user |
| GET | `/api/tasks/mine` | Tasks assigned to current user |
| PUT / PATCH / DELETE | `/api/tasks/{id}` | Update, change status, or delete a task |
| GET / POST | `/api/tasks/{id}/comments` | List/add comments |
| GET | `/api/notifications` | Current user notifications |
| PATCH | `/api/notifications/{id}/read` | Mark a notification as read |

Task filters: `q`, `status` (`BACKLOG`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`), and `priority` (`LOW`, `MEDIUM`, `HIGH`). Example: `GET /api/projects/1/tasks?status=IN_PROGRESS&priority=HIGH`.

## Data model

- `users` own projects, participate in projects, create/receive assigned tasks, and author comments.
- `projects` have an owner and `project_members`; tasks belong to one project.
- `tasks` reference the creator and optional assignee, plus status, priority, due date, and tag.
- `task_comments` belong to a task and an author; `notifications` belong to a recipient and may reference a project/task.

Hibernate manages the development schema with `ddl-auto: update`. For production, use versioned schema migrations (for example Flyway), set `SEED_DEMO=false`, use managed MySQL credentials, rotate a high-entropy JWT secret, and configure the exact trusted frontend origin. Avoid exposing MySQL publicly.

## Testing checklist

- [ ] `mvn test` succeeds in `backend/`.
- [ ] `GET http://localhost:8080/api/health` returns `{"status":"ok","service":"taskflow-api"}`.
- [ ] Sign up a new account with a password of at least 10 characters; signing up with the same email returns a conflict.
- [ ] Log in with the seeded demo account; invalid credentials return 401.
- [ ] With the returned token, list projects, create a project, and add an existing seeded user as a member.
- [ ] Create a task with an assignee, priority, and future due date; confirm it appears on the board.
- [ ] Drag a task between columns; confirm the status persists after refresh.
- [ ] Add and reload a task comment; confirm an assigned teammate receives a notification.
- [ ] Change the priority filter and search; confirm only matching tasks are shown.
- [ ] Confirm an unauthenticated project request returns 401 and a non-member request returns 403.
- [ ] At a narrow viewport, open and close the navigation drawer and verify the board remains usable.

## Upload to GitHub

1. Create a new empty repository on GitHub (do not add a README if you already have this one).
2. From this project directory, initialize Git if needed and connect the repository:

   ```bash
   git init
   git add .
   git commit -m "Build TaskFlow team workspace"
   git branch -M main
   git remote add origin https://github.com/<your-account>/<your-repository>.git
   git push -u origin main
   ```

3. Keep `.env`, database passwords, JWT secrets, build output, and backend `target/` out of commits. For publishing from v0, connect GitHub in project settings or use the project’s Publish flow; do not upload real secrets to the repository.
