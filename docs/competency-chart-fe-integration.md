# FE Integration: Competency Chart + Holobox

## Kiosk Flow: Find Applications By Email

The kiosk does not have an `applicationId` when the screen opens. The expected flow is:

1. The candidate enters their email.
2. FE calls the endpoint below to find the user and load all applications owned by that user.
3. FE displays the applications as selectable items.
4. After the candidate selects an application, FE uses its `id` to load the competency chart and journey summary.

### Find Applications By Email

```http
GET /api/applications/by-email?email=candidate@example.com
```

The backend uses `UserRepository.findByEmail(email)` and then `ApplicationRepository.findAllByUserIdAndStatusNot(userId, IN_PROGRESS)`. The response contains applications only; it does not expose the user's password or the full `User` entity.

Applications with status `IN_PROGRESS` are excluded from this kiosk endpoint. Only applications with a final status such as `PASSED`, `FAILED`, or `SOFT_FAILED` are returned.

Success response:

```json
{
  "traceId": "f3e2d1c0...",
  "data": [
    {
      "id": 133,
      "userId": 8,
      "jdId": 41,
      "currentRoundOrder": 4,
      "status": "PASSED",
      "overallScore": 91,
      "isDeleted": false,
      "createdAt": "2026-08-06T16:26:56.768967",
      "updatedAt": "2026-08-06T16:39:27.274386"
    }
  ]
}
```

The project wraps list responses as `{ traceId, data }` through `SuccessResponseHandler`.

If the email does not belong to a user, the backend returns `404`:

```json
{
  "traceId": "f3e2d1c0...",
  "error": "User not found with email: candidate@example.com"
}
```

If the user exists but has no applications, the response is successful with an empty `data` array.

### TypeScript Types

```ts
export type ApplicationStatus =
  | "IN_PROGRESS"
  | "PASSED"
  | "FAILED"
  | "SOFT_FAILED";

export type Application = {
  id: number;
  userId: number;
  jdId: number;
  currentRoundOrder: number;
  status: ApplicationStatus;
  overallScore: number;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
};

export type ApplicationsByEmailResponse = {
  traceId?: string;
  data: Application[];
};
```

### FE Fetch Function

```ts
export async function getApplicationsByEmail(email: string): Promise<Application[]> {
  const query = new URLSearchParams({ email: email.trim() });
  const response = await fetch(`/api/applications/by-email?${query.toString()}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });

  const body = await response.json();

  if (!response.ok) {
    throw new Error(body?.error ?? "Cannot find applications for this email");
  }

  return body.data ?? [];
}
```

### Kiosk Selection Example

```ts
const [applications, setApplications] = useState<Application[]>([]);
const [selectedApplicationId, setSelectedApplicationId] = useState<number | null>(null);

async function handleSearch(email: string) {
  const result = await getApplicationsByEmail(email);
  setApplications(result);
  setSelectedApplicationId(null);
}

async function handleSelectApplication(applicationId: number) {
  setSelectedApplicationId(applicationId);
  const chart = await getCompetencyChart(applicationId, accessToken);
  // Render radar/bar charts and build the Holobox script from chart.
}
```

Display at least `id`, `jdId`, `status`, `overallScore`, and `createdAt` in the selection list. After selection, use the selected `id` with:

```http
GET /api/applications/{applicationId}/competency-chart
GET /api/applications/{applicationId}/journey-summary
```

Tài liệu này dùng cho màn hình đọc kết quả năng lực ứng viên sau khi toàn bộ vòng tuyển dụng đã hoàn thành.

## Endpoint

```http
GET /api/applications/{applicationId}/competency-chart
Authorization: Bearer <access_token>
```

Endpoint này chỉ đọc dữ liệu đã lưu trong bảng `JourneySummary`. FE không cần trigger LLM.

Nếu summary/charts chưa được generate, BE trả `404`.

## Khi Nào Có Data

Data chart chỉ có sau khi:

1. Candidate đã đi tới vòng cuối.
2. Round cuối được mark completed qua flow `moveToNextRound`.
3. Tất cả `ApplicationDetail.finalResult` của các round đều khác `null`.
4. BE publish event `AllRoundsCompletedEvent`.
5. Async listener gọi AI workspace `SUMMARY_REPORT("summary-report-gen")`.
6. AI trả response có `competencyChart`.
7. BE lưu `competencyChart` vào `JourneySummary`.

Vì listener chạy `@Async`, sau khi hoàn thành round cuối có thể cần polling vài giây.

## Success Response

Do backend có `SuccessResponseHandler`, object response sẽ có thêm `traceId` ở root.

```json
{
  "applicationId": 101,
  "candidateName": "Nguyen Van A",
  "jobTitle": "Backend Developer Intern",
  "overallLevel": "PRACTITIONER",
  "overallScore": 72.5,
  "technicalSkillAreas": [
    {
      "skillArea": "Software Construction",
      "score": 78,
      "level": "PRACTITIONER",
      "sourceRounds": ["Coding", "Code Review"]
    },
    {
      "skillArea": "Software Quality",
      "score": 70,
      "level": "PRACTITIONER",
      "sourceRounds": ["Code Review"]
    }
  ],
  "behavioralSkills": [
    {
      "skillName": "Communication Skills",
      "score": 74,
      "sourceRounds": ["Email Simulator"]
    },
    {
      "skillName": "Team Participation Skills",
      "score": 68,
      "sourceRounds": ["Mentor Review"]
    }
  ],
  "traceId": "9d14e7f2a3..."
}
```

## Error Response

```json
{
  "error": "Competency chart is not available yet. Summary report may not have been generated by AI.",
  "traceId": "9d14e7f2a3..."
}
```

FE nên hiển thị trạng thái: "Báo cáo năng lực đang được tạo, vui lòng thử lại sau."

## TypeScript Types

```ts
export type CompetencyLevel =
  | "TECHNICIAN"
  | "ENTRY_LEVEL_PRACTITIONER"
  | "PRACTITIONER"
  | "TECHNICAL_LEADER"
  | "SENIOR_SOFTWARE_ENGINEER";

export type SkillAreaScore = {
  skillArea: string;
  score: number;
  level: CompetencyLevel;
  sourceRounds: string[];
};

export type BehavioralSkillScore = {
  skillName: string;
  score: number;
  sourceRounds: string[];
};

export type CompetencyChartResponse = {
  applicationId: number;
  candidateName: string;
  jobTitle: string;
  overallLevel: CompetencyLevel;
  overallScore: number;
  technicalSkillAreas: SkillAreaScore[];
  behavioralSkills: BehavioralSkillScore[];
  traceId?: string;
};
```

## Fetch Function

```ts
export async function getCompetencyChart(
  applicationId: number,
  accessToken: string
): Promise<CompetencyChartResponse> {
  const res = await fetch(`/api/applications/${applicationId}/competency-chart`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
  });

  const body = await res.json();

  if (!res.ok) {
    throw new Error(body?.error ?? "Cannot load competency chart");
  }

  return body;
}
```

## Polling Sau Khi Hoàn Thành Round Cuối

Vì BE generate summary async, FE có thể poll endpoint chart sau khi gọi `moveToNextRound` thành công ở vòng cuối.

```ts
export async function waitForCompetencyChart(
  applicationId: number,
  accessToken: string,
  options = { retries: 10, intervalMs: 3000 }
): Promise<CompetencyChartResponse> {
  for (let attempt = 1; attempt <= options.retries; attempt += 1) {
    try {
      return await getCompetencyChart(applicationId, accessToken);
    } catch (error) {
      if (attempt === options.retries) {
        throw error;
      }
      await new Promise((resolve) => setTimeout(resolve, options.intervalMs));
    }
  }

  throw new Error("Competency chart is not available yet");
}
```

## Data Cho Radar Chart

Radar chart dùng `technicalSkillAreas`.

```ts
const radarData = chart.technicalSkillAreas.map((item) => ({
  subject: item.skillArea,
  score: item.score,
  fullMark: 100,
  level: item.level,
  sourceRounds: item.sourceRounds,
}));
```

Ví dụ với Recharts:

```tsx
import {
  PolarAngleAxis,
  PolarGrid,
  PolarRadiusAxis,
  Radar,
  RadarChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

type Props = {
  chart: CompetencyChartResponse;
};

export function TechnicalRadarChart({ chart }: Props) {
  const data = chart.technicalSkillAreas.map((item) => ({
    subject: item.skillArea,
    score: item.score,
    fullMark: 100,
    level: item.level,
    sourceRounds: item.sourceRounds.join(", "),
  }));

  return (
    <ResponsiveContainer width="100%" height={360}>
      <RadarChart data={data}>
        <PolarGrid />
        <PolarAngleAxis dataKey="subject" />
        <PolarRadiusAxis angle={90} domain={[0, 100]} />
        <Radar
          name="Technical competency"
          dataKey="score"
          stroke="#2563eb"
          fill="#2563eb"
          fillOpacity={0.28}
        />
        <Tooltip />
      </RadarChart>
    </ResponsiveContainer>
  );
}
```

## Data Cho Bar Chart

Bar chart dùng `behavioralSkills`.

```ts
const barData = chart.behavioralSkills.map((item) => ({
  name: item.skillName,
  score: item.score,
  sourceRounds: item.sourceRounds,
}));
```

## Label Hiển Thị Competency Level

```ts
export const competencyLevelLabel: Record<CompetencyLevel, string> = {
  TECHNICIAN: "Technician",
  ENTRY_LEVEL_PRACTITIONER: "Entry Level Practitioner",
  PRACTITIONER: "Practitioner",
  TECHNICAL_LEADER: "Technical Leader",
  SENIOR_SOFTWARE_ENGINEER: "Senior Software Engineer",
};
```

## Text Cho Holobox Đọc Kết Quả

Holobox nên đọc kết quả dạng tường thuật ngắn, không đọc từng con số quá dài. FE có thể generate script từ response chart như sau:

```ts
export function buildHoloboxCompetencyScript(chart: CompetencyChartResponse) {
  const strongestTechnical = [...chart.technicalSkillAreas].sort(
    (a, b) => b.score - a.score
  )[0];

  const weakestTechnical = [...chart.technicalSkillAreas].sort(
    (a, b) => a.score - b.score
  )[0];

  const strongestBehavioral = [...chart.behavioralSkills].sort(
    (a, b) => b.score - a.score
  )[0];

  const parts = [
    `Báo cáo năng lực của ${chart.candidateName} cho vị trí ${chart.jobTitle}.`,
    `Điểm tổng quan là ${Math.round(chart.overallScore)} trên 100, tương ứng mức ${competencyLevelLabel[chart.overallLevel]}.`,
  ];

  if (strongestTechnical) {
    parts.push(
      `Năng lực kỹ thuật nổi bật nhất là ${strongestTechnical.skillArea}, đạt ${Math.round(strongestTechnical.score)} điểm.`
    );
  }

  if (weakestTechnical) {
    parts.push(
      `Khu vực cần cải thiện thêm là ${weakestTechnical.skillArea}, hiện ở mức ${competencyLevelLabel[weakestTechnical.level]}.`
    );
  }

  if (strongestBehavioral) {
    parts.push(
      `Về hành vi làm việc, điểm mạnh đáng chú ý là ${strongestBehavioral.skillName}.`
    );
  }

  parts.push(
    "Biểu đồ radar thể hiện các vùng năng lực kỹ thuật theo chuẩn SWECom, còn biểu đồ cột thể hiện các kỹ năng hành vi được ghi nhận trong quá trình đánh giá."
  );

  return parts.join(" ");
}
```

FE truyền string này vào Holobox/TTS component:

```tsx
const script = buildHoloboxCompetencyScript(chart);

<Holobox
  title="Competency Summary"
  text={script}
  language="vi-VN"
/>
```

Nếu Holobox nhận messages thay vì text:

```ts
const holoboxMessage = {
  role: "assistant",
  content: buildHoloboxCompetencyScript(chart),
  metadata: {
    applicationId: chart.applicationId,
    jobTitle: chart.jobTitle,
    overallScore: chart.overallScore,
    overallLevel: chart.overallLevel,
  },
};
```

## UI State Gợi Ý

- Loading: đang tải báo cáo năng lực.
- 404: báo cáo đang được tạo hoặc ứng viên chưa hoàn thành toàn bộ vòng.
- Empty chart: hiển thị message không có dữ liệu chart, không render chart rỗng.
- Success: hiển thị overall score, level, radar chart, behavioral bar chart, Holobox đọc kết quả.

## Lưu Ý Tích Hợp

- `score` luôn nằm trong khoảng `0-100`.
- `technicalSkillAreas` dùng cho radar chart.
- `behavioralSkills` dùng cho bar chart.
- `sourceRounds` dùng cho tooltip hoặc detail drawer.
- Không gọi endpoint này để trigger AI.
- Sau round cuối nên poll endpoint này vài lần vì BE generate bằng async event.
