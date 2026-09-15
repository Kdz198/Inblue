# Mentor Review Module — FE Integration Guide

Tài liệu mô tả **flow hiện tại** của vòng Mentor Review (sau khi bổ sung bước mentor duyệt lịch hẹn) và toàn bộ endpoint FE cần tích hợp.

Đối tượng sử dụng:

- **Candidate/User**: chọn mentor, đề xuất lịch phỏng vấn, chờ mentor duyệt, vào phòng họp, đánh giá mentor.
- **Mentor**: duyệt/từ chối lịch hẹn, phỏng vấn, chấm điểm ứng viên theo STAR.
- **Admin**: gán mentor cho vòng.

## Mục lục

1. Quy ước chung
2. Thay đổi so với luồng cũ
3. Flow tổng quan
4. State machine phía FE
5. Contract dữ liệu và TypeScript types
6. Flow chi tiết theo từng vai trò
7. FE implementation notes
8. Bảng tra nhanh toàn bộ endpoint

---

## 1. Quy ước chung

### Base URL và authentication

```http
https://<api-host>/api/...
Authorization: Bearer <access_token>
Content-Type: application/json
```

Riêng các endpoint dành cho Mentor (`schedule-decision`, `mentor/pending-schedules`) backend tự xác định mentor từ JWT, **FE không gửi mentorId**. Backend đọc `sub` (Mentor.id khi tài khoản mentor đăng nhập) và fallback tra theo claim `email`.

### Enum `ApplicationDetail.status`

| Giá trị | Ý nghĩa |
|---|---|
| `AWAITING_MENTOR` | Chờ Admin gán mentor |
| `AWAITING_CANDIDATE_SELECT_MENTOR` | Admin đề xuất nhiều mentor, chờ ứng viên chọn 1 |
| `PENDING` | Đã có mentor — chờ ứng viên đề xuất lịch (nếu chưa có `sessionId`) hoặc chờ tới giờ phỏng vấn (nếu đã có `sessionId`) |
| `AWAITING_MENTOR_SCHEDULE_APPROVAL` | **(Mới)** Ứng viên đã đề xuất lịch ONLINE, chờ mentor duyệt. Chưa có phòng họp |
| `SLOT_PICKED` | Dùng cho luồng Kiosk, không dùng trong luồng web |
| `AI_EVALUATED` | Vòng AI, không dùng ở Mentor Review |
| `COMPLETED` | Vòng đã chốt điểm và kết quả |

### Enum `Session.status`

`DRAFT` → `SCHEDULED` → `PAID` → `ONGOING` → `COMPLETED`, ngoài ra có `REJECTED`, `CANCELED`.

Ở vòng Mentor Review, session được tạo với status `SCHEDULED` ngay khi mentor duyệt lịch.

---

## 2. Thay đổi so với luồng cũ

| | Luồng cũ | Luồng hiện tại |
|---|---|---|
| Khi ứng viên chọn lịch ONLINE | Tạo phòng Daily.co ngay, `Session = SCHEDULED`, status giữ `PENDING` | Chỉ **lưu đề xuất lịch**, status chuyển `AWAITING_MENTOR_SCHEDULE_APPROVAL`, **chưa** có phòng họp |
| Mentor xác nhận lịch | Không có bước này | `POST /api/application-details/{id}/schedule-decision` |
| Khi mentor từ chối | — | Ghi lý do, xoá mentorId, đẩy về `AWAITING_CANDIDATE_SELECT_MENTOR` (còn mentor khác) hoặc `AWAITING_MENTOR` (hết mentor) |
| Hình thức OFFLINE | Tạo session COMPLETED ngay | **Giữ nguyên**, không qua bước duyệt |

---

## 3. Flow tổng quan

```text
AWAITING_MENTOR
   │ Admin gán 1 mentor (assign-mentor)
   │ hoặc Admin đề xuất nhiều mentor (assign-mentors) → AWAITING_CANDIDATE_SELECT_MENTOR
   │                                                      │ ứng viên chọn (select-mentor)
   ▼                                                      ▼
PENDING  (đã có mentorId, chưa có sessionId)
   │ ứng viên đề xuất lịch ONLINE  (POST /api/sessions/create-for-round)
   ▼
AWAITING_MENTOR_SCHEDULE_APPROVAL
   │
   ├─ mentor DUYỆT   → tạo phòng Daily.co (Session=SCHEDULED)
   │                 → PENDING (đã có sessionId) → chờ tới giờ → ONGOING → COMPLETED
   │
   └─ mentor TỪ CHỐI (bắt buộc lý do) → xoá mentorId, lưu lý do
           ├─ còn mentor khác trong assignedMentorIds → AWAITING_CANDIDATE_SELECT_MENTOR
           └─ hết mentor                              → AWAITING_MENTOR

Sau khi Session = COMPLETED:
   mentor chấm ứng viên (POST /api/mentor-reviews)
   ứng viên chấm mentor (POST /api/mentor-feedbacks)
   → có ĐỦ CẢ HAI thì backend mới set ApplicationDetail = COMPLETED,
     tính finalScore = rating/100 * round.maxScore, so passThreshold ra PASSED/FAILED,
     rồi tự động mở vòng kế tiếp.
```

---

## 4. State machine phía FE

Step hiển thị cho ứng viên được suy ra từ **cả** `ApplicationDetail.status` **và** `Session.status`, theo đúng thứ tự ưu tiên dưới đây:

| # | Điều kiện | Step |
|---:|---|---|
| 1 | `status` ∈ {`COMPLETED`, `AI_EVALUATED`} hoặc `session.status === "COMPLETED"` | `RESULT` |
| 2 | `session.status === "ONGOING"` | `IN_CALL` |
| 3 | có `sessionId` và `session.status` ∈ {`PAID`, `SCHEDULED`, `DRAFT`} | `WAITING` |
| 4 | `status === "AWAITING_MENTOR_SCHEDULE_APPROVAL"` | `AWAITING_SCHEDULE_APPROVAL` **(mới)** |
| 5 | `status === "AWAITING_MENTOR"` | `AWAITING_MENTOR` |
| 6 | `status === "AWAITING_CANDIDATE_SELECT_MENTOR"` | `SELECT_MENTOR` |
| 7 | `status` ∈ {`PENDING`, `SLOT_PICKED`, `SUBMITTED`} | `SCHEDULE` |

Lưu ý thứ tự: điều kiện (1)-(3) dựa trên session phải được kiểm tra **trước** status, vì `Session.status` là nguồn sự thật cho việc "buổi phỏng vấn đã diễn ra".

`sessionId` lấy theo thứ tự: `detail.sessionInfo.sessionId ?? detail.sessionId`.

---

## 5. Contract dữ liệu và TypeScript types

### `ApplicationDetail.sessionInfo` (jsonb)

```ts
interface RoundSessionInfo {
  sessionId?: number | null;
  meetingType?: "ONLINE" | "OFFLINE" | null;
  startTime?: string | null;   // thời điểm bắt đầu vòng thi
  endTime?: string | null;     // deadline vòng thi

  // === Bổ sung cho luồng mentor duyệt lịch ===
  pendingJoinTime?: string | null;        // ISO-8601, giờ hẹn ứng viên đề xuất, đang chờ duyệt
  pendingDurationMinutes?: number | null; // thời lượng (phút) của đề xuất
  mentorRejectReason?: string | null;     // lý do mentor từ chối gần nhất
  mentorRejectedAt?: string | null;       // ISO-8601
  rejectedMentorId?: number | null;       // mentor đã từ chối
}
```

Quy tắc dữ liệu cần nhớ:

- Khi ứng viên gửi đề xuất mới, backend **xoá** `mentorRejectReason` / `mentorRejectedAt` / `rejectedMentorId` → banner "bị từ chối" tự biến mất.
- Khi mentor **duyệt**, backend xoá `pendingJoinTime` / `pendingDurationMinutes`, set `sessionId` + `meetingType = ONLINE`.
- Khi mentor **từ chối**, backend xoá `pendingJoinTime`, `pendingDurationMinutes` và **cả `meetingType`** (để ứng viên chọn lại hình thức từ đầu), đồng thời set `mentorId = null`.

### DTO của 2 endpoint mới

```ts
// POST /api/application-details/{id}/schedule-decision
interface ScheduleDecisionRequest {
  approved: boolean;
  reason?: string; // BẮT BUỘC khi approved = false, tối đa 1000 ký tự
}

// GET /api/application-details/mentor/pending-schedules
interface MentorPendingScheduleResponse {
  applicationDetailId?: number;
  applicationId?: number;
  roundId?: number;
  roundName?: string;
  roundOrder?: number;
  jobTitle?: string;

  candidateUserId?: number;
  candidateName?: string;
  candidateEmail?: string;
  candidateAvatarUrl?: string;

  mentorId?: number;
  meetingType?: "ONLINE" | "OFFLINE";
  proposedJoinTime?: string;        // ISO-8601
  proposedDurationMinutes?: number;
  requestedAt?: string;             // ISO-8601
}
```

---

## 6. Flow chi tiết theo từng vai trò

### 6.1 Admin — gán mentor

Màn hình danh sách các vòng cần gán mentor: lọc `status` ∈ {`AWAITING_MENTOR`, `AWAITING_CANDIDATE_SELECT_MENTOR`}. Vòng đang ở `AWAITING_MENTOR_SCHEDULE_APPROVAL` **không** hiện ở đây (đã có mentor rồi).

Hai cách gán:

- Gán thẳng 1 mentor → `PUT /{id}/assign-mentor?mentorId=` → status về `PENDING`.
- Đề xuất nhiều mentor → `PUT /{id}/assign-mentors` → status `AWAITING_CANDIDATE_SELECT_MENTOR`, `mentorId = null`.

Sau khi mentor từ chối lịch, hồ sơ sẽ quay lại 1 trong 2 trạng thái trên và admin có thể gán lại bình thường.

### 6.2 Candidate — đề xuất lịch và chờ duyệt

1. **Step `SELECT_MENTOR`** (nếu status `AWAITING_CANDIDATE_SELECT_MENTOR`): gọi `GET /{id}/assigned-mentors` để render danh sách, chọn xong gọi `PUT /{id}/select-mentor?mentorId=`.
2. **Step `SCHEDULE`**: form chọn thời gian + thời lượng + hình thức. Submit `POST /api/sessions/create-for-round`.
   - ONLINE → status chuyển `AWAITING_MENTOR_SCHEDULE_APPROVAL`, response trả về `id = 0`, `status = null` (chưa có session thật) → **không được dựa vào `response.id`**, hãy refetch application detail.
   - OFFLINE → tạo session ngay như cũ.
3. **Step `AWAITING_SCHEDULE_APPROVAL`**: hiển thị giờ đã đề xuất lấy từ `sessionInfo.pendingJoinTime` / `pendingDurationMinutes`. Không polling — refetch `GET /api/application-details/{id}` khi vào trang, khi tab lấy lại focus, hoặc khi ứng viên bấm nút "Làm mới".
4. **Nếu bị từ chối**: status quay về `AWAITING_CANDIDATE_SELECT_MENTOR` hoặc `AWAITING_MENTOR`. Hiển thị banner cảnh báo lấy từ `sessionInfo.mentorRejectReason` + `mentorRejectedAt` ở cả 3 step `AWAITING_MENTOR`, `SELECT_MENTOR`, `SCHEDULE`.
5. **Nếu được duyệt**: status về `PENDING` và đã có `sessionId` → FE tự nhảy sang step `WAITING`, lấy `roomUrl` từ `GET /api/sessions/{id}`.

### 6.3 Mentor — duyệt/từ chối lịch

1. Gọi `GET /api/application-details/mentor/pending-schedules` để lấy danh sách lịch chờ duyệt của chính mình. List rỗng thì ẩn hẳn khu vực này.
2. **Duyệt**: `POST /{id}/schedule-decision` với `{ approved: true }`. Nên có dialog xác nhận vì thao tác này tạo phòng họp thật và chốt lịch với ứng viên.
3. **Từ chối**: mở dialog bắt buộc nhập lý do (validate không rỗng, ≤ 1000 ký tự) rồi gọi `{ approved: false, reason }`.
4. Sau khi thành công, invalidate: danh sách pending-schedules, `applicationDetails`, `sessions`, `assignedMentors`.

### 6.4 Sau buổi phỏng vấn

- Khi 2 bên rời phòng, webhook Daily.co set `Session.status = COMPLETED`.
- Mentor chấm ứng viên: `POST /api/mentor-reviews` (chỉ chấp nhận khi session đã `COMPLETED`).
- Ứng viên chấm mentor: `POST /api/mentor-feedbacks`.
- Vòng chỉ `COMPLETED` khi **có đủ cả hai**. FE nên invalidate `applicationDetails` + `applications` sau mỗi lần submit để timeline cập nhật.

---

## 7. FE implementation notes

- **Không polling định kỳ**: luồng này không có yêu cầu real-time nên FE không tự động gọi lại API theo interval (vd `setInterval` mỗi 30s). Thay vào đó, refetch `GET /api/application-details/{id}` (khi chưa có `sessionId`) hoặc `GET /api/sessions/{id}` (khi đã có `sessionId`, để lấy trạng thái `ONGOING`/`COMPLETED` từ webhook) tại các thời điểm: vào trang / mount component, tab lấy lại focus (`visibilitychange`), sau mỗi action ghi dữ liệu (xem invalidate ở 6.3.4), hoặc khi người dùng bấm nút "Làm mới" thủ công. Nút "Vào phòng" có thể tự enable theo giờ hẹn (`pendingJoinTime`/`sessionInfo.startTime` so với giờ local) thay vì chờ server báo `ONGOING`.
- **Không tạo lại đề xuất khi đã có phòng**: backend trả 400 `"Vòng phỏng vấn này đã có phòng họp"` nếu `sessionId != null`. FE không nên hiện form đặt lịch ở trạng thái đó.
- **Gửi lại đề xuất khi đang chờ duyệt** là hợp lệ (backend ghi đè đề xuất cũ), dùng cho trường hợp ứng viên đổi ý trước khi mentor kịp xử lý.
- **Điều kiện hiện form đặt lịch** ở màn hình lịch sử ứng tuyển là `status === "PENDING" && sessionInfo.meetingType == null`. Vì vậy backend đã chủ động xoá `meetingType` khi mentor từ chối — FE không cần xử lý thêm.
- **Race condition**: nếu 2 tab cùng bấm duyệt, lần thứ hai nhận 400 `"Vòng phỏng vấn này không có lịch hẹn nào đang chờ duyệt"`. Hiển thị lỗi và refetch danh sách thay vì retry.
- **i18n**: toàn bộ chuỗi của luồng này nằm ở namespace `mentorSchedule` (en/vi/ja).

---

## 8. Bảng tra nhanh toàn bộ endpoint

### 8.1 Gán và chọn mentor

| Method | Endpoint | Vai trò | Mô tả |
|---|---|---|---|
| `PUT` | `/api/application-details/{id}/assign-mentor?mentorId={mentorId}` | Admin | Gán thẳng 1 mentor → status `PENDING` |
| `PUT` | `/api/application-details/{id}/assign-mentors` | Admin | Body `{ "mentorIds": [1,2,3] }` → status `AWAITING_CANDIDATE_SELECT_MENTOR` |
| `GET` | `/api/application-details/{id}/assigned-mentors` | Candidate | Danh sách `MentorResponse[]` được đề xuất |
| `PUT` | `/api/application-details/{id}/select-mentor?mentorId={mentorId}` | Candidate | Chọn 1 mentor → status `PENDING`. Lỗi 400 nếu mentorId không nằm trong `assignedMentorIds` |

### 8.2 Đề xuất và duyệt lịch hẹn

| Method | Endpoint | Vai trò | Mô tả |
|---|---|---|---|
| `POST` | `/api/sessions/create-for-round` | Candidate | Đề xuất lịch. Body bên dưới |
| `GET` | `/api/application-details/mentor/pending-schedules` | Mentor | Danh sách `MentorPendingScheduleResponse[]` chờ mentor hiện tại duyệt |
| `POST` | `/api/application-details/{id}/schedule-decision` | Mentor | Duyệt/từ chối. Body `ScheduleDecisionRequest`. Trả về `ApplicationDetail` đã cập nhật |

`POST /api/sessions/create-for-round`:

```json
{
  "applicationDetailId": 123,
  "joinTime": "2026-09-20T14:00:00+07:00",
  "duration": 45,
  "offline": false
}
```

`joinTime` gửi lên dạng ISO-8601 **kèm offset múi giờ của trình duyệt** (FE đang build chuỗi `YYYY-MM-DDTHH:mm:00±HH:MM` từ giờ local, xem `ScheduleStep`). `duration` tính bằng phút, mặc định 60 nếu không gửi. `offline = true` sẽ bỏ qua toàn bộ bước duyệt lịch.

Response (ONLINE, đang chờ duyệt) — lưu ý `id = 0` và `status = null`. `joinTime` trong response luôn trả theo format `yyyy-MM-dd HH:mm:ss.SSS` giờ `Asia/Ho_Chi_Minh`:

```json
{
  "id": 0,
  "userId": 42,
  "mentorId": 7,
  "joinTime": "2026-09-20 14:00:00.000",
  "duration": 45,
  "totalPrice": 0,
  "status": null
}
```

`POST /api/application-details/{id}/schedule-decision`:

```json
{ "approved": true }
```

```json
{ "approved": false, "reason": "Mình bận khung giờ này, bạn chọn giúp buổi tối trong tuần nhé" }
```

Các lỗi có thể gặp:

| HTTP | Message | Nguyên nhân |
|---:|---|---|
| 400 | `Vòng phỏng vấn này không có lịch hẹn nào đang chờ duyệt` | Status không phải `AWAITING_MENTOR_SCHEDULE_APPROVAL` |
| 400 | `Vui lòng nhập lý do từ chối lịch hẹn` | `approved = false` nhưng thiếu `reason` |
| 400 | `Lý do từ chối không được vượt quá 1000 ký tự` | `reason` quá dài |
| 400 | `Vòng phỏng vấn này đã có phòng họp` | Gọi `create-for-round` khi `sessionId != null` |
| 403 | `Bạn không phải mentor được gán cho vòng phỏng vấn này` | Mentor trong JWT khác `mentorId` của vòng |
| 403 | `Chỉ mentor mới được thực hiện thao tác này` | Token không phải tài khoản mentor |

### 8.3 Theo dõi vòng và phòng họp

| Method | Endpoint | Vai trò | Mô tả |
|---|---|---|---|
| `GET` | `/api/application-details/{id}` | All | Chi tiết 1 vòng — dùng để poll trạng thái |
| `GET` | `/api/application-details/application/{applicationId}` | All | Tất cả các vòng của 1 hồ sơ |
| `GET` | `/api/sessions/{id}` | All | Chi tiết session (roomUrl, status, joinTime) |
| `GET` | `/api/sessions/{userId}/by-user` | All | Tất cả session liên quan tới 1 user |
| `POST` | `/api/sessions/join-session` | All | Ghi nhận user vào phòng. Body `{ sessionName, userId, participantId, isMentor }` (`sessionName` chính là `roomName`) |

### 8.4 Chấm điểm sau phỏng vấn

| Method | Endpoint | Vai trò | Mô tả |
|---|---|---|---|
| `POST` | `/api/mentor-reviews` | Mentor | Chấm ứng viên. Chỉ nhận khi `Session.status === "COMPLETED"` |
| `PUT` | `/api/mentor-reviews` | Mentor | Sửa review, body có `id` |
| `GET` | `/api/mentor-reviews/{sessionId}` | All | Lấy review theo **sessionId** |
| `POST` | `/api/mentor-feedbacks` | Candidate | Chấm mentor. Body `{ sessionId, mentorId, userId, rating, comment }` |
| `GET` | `/api/mentor-feedbacks/{sessionId}` | All | Feedback theo sessionId |

`POST /api/mentor-reviews`:

```json
{
  "sessionId": 88,
  "mentorId": 7,
  "userId": 42,
  "rating": 85,
  "situationNote": "...",
  "taskNote": "...",
  "actionNote": "...",
  "resultNote": "...",
  "strength": "...",
  "weakness": "...",
  "improve": "..."
}
```

`rating` thang 0-100.

**Giới hạn độ dài các field text**: entity khai báo tất cả là `TEXT`, nhưng schema DB thực tế đang là:

| Cột | Kiểu thật trong DB |
|---|---|
| `situationnote`, `tasknote` | `varchar(255)` |
| `actionnote`, `resultnote`, `strength`, `weakness`, `improve` | `text` |

Vì vậy FE đang giới hạn **tất cả** field ở 255 ký tự (`MENTOR_REVIEW_NOTE_MAX_LENGTH`) cho an toàn, và bắt lỗi `value too long for type character varying(255)` để hiện thông báo thân thiện. Nếu muốn cho nhập dài hơn, cần migration đổi 2 cột trên sang `text` trước.
