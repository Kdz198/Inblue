# Entry Test Module — FE Integration Guide

Tài liệu này mô tả flow nghiệp vụ và contract API hiện tại của module Entry Test. Đối tượng sử dụng gồm:

- Candidate/User: chọn định hướng, làm bài, chạy thử code, nộp bài và xem năng lực.
- Màn hình quản trị: cấu hình đề Entry Test và thang quy đổi level.

## Mục lục

1. Quy ước chung, authentication, response và enum.
2. Flow tổng quan cho Candidate và Admin.
3. Mục đích các bảng/model.
4. TypeScript types khuyến nghị.
5. Candidate/User endpoints.
6. Admin Entry Test endpoints.
7. Admin Level Scale endpoints.
8. FE implementation notes.
9. Bảng tra nhanh toàn bộ endpoint.

## 1. Quy ước chung

### Base URL và authentication

Các URL trong tài liệu là path tương đối so với API host:

```text
https://<api-host>/api/...
```

Gửi access token ở mọi request:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Hình dạng success response

Các controller Entry Test nằm trong package `fpt.org.inblue.entrytest.controller`, vì vậy success response hiện được trả trực tiếp, không bọc trong `{ "data": ... }`.

Ví dụ endpoint trả object:

```json
{
  "userId": 7,
  "targetRole": "BE"
}
```

Endpoint trả danh sách sẽ trả array trực tiếp:

```json
[
  { "id": 1 },
  { "id": 2 }
]
```

### Error response

Lỗi nghiệp vụ thường có dạng:

```json
{
  "error": "Entry test attempt is not in progress",
  "traceId": "7c8f4d..."
}
```

Lỗi validation của `run code` có thể có field cụ thể:

```json
{
  "sourceCode": "sourceCode cannot be empty",
  "traceId": "7c8f4d..."
}
```

FE không nên phụ thuộc tuyệt đối vào message tiếng Anh. Hãy xử lý theo HTTP status và dùng `error` hoặc field validation để hiển thị message.

### Enum dùng chung

```ts
export type TargetRole =
  | "BE"
  | "FE"
  | "QA_QC"
  | "BA"
  | "DEVOPS"
  | "DATA";

export type TargetLevel =
  | "INTERN"
  | "FRESHER"
  | "JUNIOR"
  | "MIDDLE";

export type AttemptStatus =
  | "IN_PROGRESS"
  | "SUBMITTED"
  | "GRADED"
  | "EXPIRED";

export type EntryTestSectionType =
  | "COMMON_QUIZ"
  | "SPECIFIC_QUIZ"
  | "SPECIFIC_CODING";

export type EntryTestItemType =
  | "QUESTION_BANK"
  | "CODING_PROBLEM";

export type CompilerLanguage =
  | "PYTHON"
  | "JS"
  | "JAVA"
  | "CPP"
  | "CSHARP"
  | "C"
  | "TYPESCRIPT"
  | "GO"
  | "KOTLIN"
  | "SWIFT"
  | "RUST"
  | "RUBY"
  | "PHP"
  | "DART"
  | "SCALA"
  | "ELIXIR"
  | "ERLANG"
  | "RACKET";
```

Backend cũng normalize `JAVASCRIPT` thành `JS`, và `C#`/`C_SHARP` thành `CSHARP`. FE nên gửi đúng enum chuẩn để contract ổn định.

---

# 2. Flow tổng quan

## 2.1 Candidate flow

```text
Đăng nhập
   │
   ▼
Kiểm tra Career Preference
   │
   ├── Chưa có → hiển thị form chọn role/language/level
   │
   └── Có rồi
          │
          ▼
     Start Entry Test
          │
          ├── Render COMMON_QUIZ
          ├── Render SPECIFIC_QUIZ
          └── Render SPECIFIC_CODING
                    │
                    ├── Run thử → visible examples
                    └── Lưu source code ở FE
          │
          ▼
     Submit toàn bộ answers một lần
          │
          ├── Quiz được BE so đáp án
          └── Code được sandbox chấm bằng hidden tests
          │
          ▼
     Attempt = GRADED
          │
          ├── Xem kết quả attempt
          └── Xem competency hiện tại
```

Flow API khuyến nghị:

1. `GET /api/me/career-preference/exists`.
2. Nếu `false`, gọi `PUT /api/me/career-preference` sau khi user hoàn thành form.
3. Chỉ gọi `POST /api/entry-tests/start` khi user xác nhận bắt đầu.
4. Lưu nguyên `attemptId` và các `itemId` BE trả về.
5. Với coding, gọi `POST /api/entry-tests/{attemptId}/coding/run` khi user bấm Run.
6. Khi user bấm Submit, gửi tất cả answer tới `POST /api/entry-tests/{attemptId}/submit` đúng một lần.
7. Dùng response submit hoặc `GET /api/entry-tests/attempts/{attemptId}/result` để hiển thị kết quả.
8. Gọi `GET /api/me/competency` để hiển thị level/năng lực mới nhất.

## 2.2 Admin flow

```text
Admin cấu hình EntryTest
   ├── thời gian
   ├── tổng điểm
   └── từng section: loại câu, số lượng, điểm/câu

Admin cấu hình LevelScale
   ├── theo TargetRole cụ thể
   ├── hoặc targetRole = null làm scale dùng chung
   └── minScore / maxScore / minCodingScore
```

Khi user start bài:

- BE lấy Entry Test active được cập nhật gần nhất.
- BE chọn ngẫu nhiên câu hỏi theo cấu hình.
- BE tạo snapshot của đề vào attempt. Việc admin sửa ngân hàng câu hỏi sau đó không làm thay đổi attempt đang làm.

Khi user submit:

- BE tính `finalScore`.
- BE tìm Level Scale active phù hợp với role, khoảng điểm và điểm coding tối thiểu.
- BE cập nhật `UserCompetency` và đặt `needRetest = false` cho career preference.

---

# 3. Mục đích các bảng/model

## `users`

Thông tin tài khoản. Một user có thể có nhiều `EntryTestAttempt` và nhiều bản ghi `UserCompetency` theo quan hệ JPA `LAZY`.

## `user_career_preference`

Lưu định hướng hiện tại của user:

- `targetRole`: vị trí mục tiêu.
- `languagesJson`: ngôn ngữ/kỹ năng user chọn.
- `careerGoal`: mục tiêu tự do.
- `targetLevel`: level mong muốn.
- `needRetest`: có cần làm lại Entry Test sau khi đổi hướng chính hay không.
- `isActive`: preference hiện có hiệu lực hay không.

Hiện `userId` đồng thời là primary key, nên mỗi user có một preference hiện tại.

## `entry_test`

Template/cấu hình bài thi:

- Tên bài thi.
- Tổng điểm.
- Thời gian làm bài.
- Danh sách section và số lượng câu/điểm từng section.
- Trạng thái active.

Đây không phải bài làm của user; nó là cấu hình để sinh bài.

## `entry_test_attempt`

Một lần user bắt đầu làm Entry Test:

- Thuộc về một `User`.
- Lưu snapshot role/language và toàn bộ câu được chọn tại thời điểm start.
- Lưu answers, điểm từng phần, tổng điểm và level kết quả.
- Status chính trong flow hiện tại: `IN_PROGRESS → GRADED`.

`itemId` như `COMMON-1`, `SPECIFIC-1`, `CODING-1` chỉ có ý nghĩa trong attempt đó. FE phải dùng chính `itemId` BE trả về, không tự sinh.

## `level_scale`

Quy tắc đổi điểm thành level:

- `targetRole`: role áp dụng; `null` nghĩa là scale dùng chung.
- `level`: kết quả `INTERN/FRESHER/JUNIOR/MIDDLE`.
- `minScore`, `maxScore`: khoảng tổng điểm.
- `minCodingScore`: ngưỡng điểm coding tối thiểu, có thể `null`.
- `isActive`: chỉ scale active được dùng khi chấm.

## `user_competency`

Snapshot năng lực mới nhất sau Entry Test và các bước cập nhật sau này:

- Role/languages tại thời điểm đánh giá.
- Level hiện tại.
- Tổng điểm và điểm từng section.
- `lastEntryTestAttemptId` để truy ngược lần thi gần nhất.
- `lastEvaluatedAt` là thời điểm đánh giá gần nhất.

API trả `userId`, không trả object graph `User`.

## Bảng nguồn liên quan

- `question_bank`: nguồn câu hỏi quiz.
- `coding_problem`: đề code, visible examples, code stub và hidden tests.
- Hidden tests không được trả cho FE. Chỉ BE lấy từ database và gửi sang sandbox khi submit.

---

# 4. FE data types khuyến nghị

```ts
export type EntryTestSectionConfig = {
  sectionType: EntryTestSectionType;
  itemType: EntryTestItemType;
  itemCount: number;
  totalScore: number;
  scorePerItem: number;
  displayOrder: number;
};

export type EntryTestQuestion = {
  itemId: string;
  questionBankId: number;
  questionText: string;
  options: string[];
  categoryName: string | null;
  difficulty: string | null;
  maxScore: number;
  displayOrder: number;
};

export type CodingVisibleExample = {
  inputs: string[];
  output: string;
  explanation?: string | null;
};

export type EntryTestCodingItem = {
  itemId: string;
  codingProblemId: number;
  title: string;
  difficulty: string;
  problemStatement: string;
  rulesAndConstraints: string[];
  visibleExamples: CodingVisibleExample[];
  codeStubs: Partial<Record<CompilerLanguage, string>>;
  paramTypes: string[];
  returnType: string;
  executionTimeLimitMs: number;
  memoryLimitMb: number;
  maxScore: number;
  displayOrder: number;
};

export type EntryTestStartResponse = {
  attemptId: number;
  entryTestId: number;
  timeLimitMinutes: number;
  selectedLanguagesJson: string[];
  sectionConfigs: EntryTestSectionConfig[];
  commonQuizItemsJson: EntryTestQuestion[];
  specificQuizItemsJson: EntryTestQuestion[];
  specificCodingItemsJson: EntryTestCodingItem[];
};

export type EntryTestAnswerResult = {
  itemId: string;
  sectionType: EntryTestSectionType;
  answerType: EntryTestItemType;
  answerJson: Record<string, unknown>;
  score: number;
  isCorrect: boolean;
  gradedAt: string;
};

export type EntryTestAttemptResponse = {
  id: number;
  userId: number;
  careerPreferenceId: number;
  entryTestId: number;
  selectedLanguagesJson: string[];
  commonQuizItemsJson: EntryTestQuestion[];
  specificQuizItemsJson: EntryTestQuestion[];
  specificCodingItemsJson: EntryTestCodingItem[];
  answersJson: EntryTestAnswerResult[] | null;
  status: AttemptStatus;
  startedAt: string;
  submittedAt: string | null;
  commonQuizScore: number | null;
  specificQuizScore: number | null;
  specificCodingScore: number | null;
  finalScore: number | null;
  resultLevel: TargetLevel | null;
  resultSnapshotJson: Record<string, unknown> | null;
  createdAt: string;
  updatedAt: string;
};
```

---

# 5. Candidate/User endpoints

## 5.1 Kiểm tra đã có career preference chưa

```http
GET /api/me/career-preference/exists
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`:

```json
true
```

hoặc:

```json
false
```

FE dùng endpoint này để quyết định hiển thị onboarding form hay nút Start Test.

## 5.2 Lấy career preference hiện tại

```http
GET /api/me/career-preference
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`:

```json
{
  "userId": 7,
  "targetRole": "BE",
  "languagesJson": ["JAVA", "SPRING_BOOT"],
  "careerGoal": "Become a backend engineer",
  "targetLevel": "JUNIOR",
  "needRetest": true,
  "isActive": true,
  "createdAt": "2026-08-29T10:00:00",
  "updatedAt": "2026-08-29T10:00:00"
}
```

Nếu chưa có preference: `404 Career preference not found`.

## 5.3 Tạo hoặc cập nhật career preference

```http
PUT /api/me/career-preference
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "targetRole": "BE",
  "languagesJson": ["JAVA", "SPRING_BOOT"],
  "careerGoal": "Become a backend engineer",
  "targetLevel": "JUNIOR"
}
```

Response `200 OK`:

```json
{
  "userId": 7,
  "targetRole": "BE",
  "languagesJson": ["JAVA", "SPRING_BOOT"],
  "careerGoal": "Become a backend engineer",
  "targetLevel": "JUNIOR",
  "needRetest": true,
  "isActive": true,
  "createdAt": "2026-08-29T10:00:00",
  "updatedAt": "2026-08-29T10:05:00"
}
```

Quy tắc:

- `targetRole` bắt buộc.
- Nếu đổi `targetRole` hoặc `languagesJson`, BE đặt `needRetest = true`.

## 5.4 Bỏ qua bước chọn preference

```http
POST /api/me/career-preference/skip
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`:

```json
{
  "userId": 7,
  "targetRole": null,
  "languagesJson": null,
  "careerGoal": null,
  "targetLevel": null,
  "needRetest": true,
  "isActive": true,
  "createdAt": "2026-08-29T10:00:00",
  "updatedAt": "2026-08-29T10:00:00"
}
```

Lưu ý: sau khi Skip, FE không nên cho gọi Start Entry Test cho tới khi user đã chọn `targetRole`, vì việc sinh câu hỏi specific cần role.

## 5.5 Bắt đầu Entry Test

```http
POST /api/entry-tests/start
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK` rút gọn:

```json
{
  "attemptId": 102,
  "entryTestId": 3,
  "timeLimitMinutes": 60,
  "selectedLanguagesJson": ["JAVA", "SPRING_BOOT"],
  "sectionConfigs": [
    {
      "sectionType": "COMMON_QUIZ",
      "itemType": "QUESTION_BANK",
      "itemCount": 15,
      "totalScore": 30.0,
      "scorePerItem": 2.0,
      "displayOrder": 1
    },
    {
      "sectionType": "SPECIFIC_QUIZ",
      "itemType": "QUESTION_BANK",
      "itemCount": 12,
      "totalScore": 30.0,
      "scorePerItem": 2.5,
      "displayOrder": 2
    },
    {
      "sectionType": "SPECIFIC_CODING",
      "itemType": "CODING_PROBLEM",
      "itemCount": 1,
      "totalScore": 40.0,
      "scorePerItem": 40.0,
      "displayOrder": 3
    }
  ],
  "commonQuizItemsJson": [
    {
      "itemId": "COMMON-1",
      "questionBankId": 11,
      "questionText": "Which collection does not allow duplicate elements?",
      "options": ["A. List", "B. Set", "C. Queue", "D. Array"],
      "categoryName": "COMMON",
      "difficulty": "EASY",
      "maxScore": 2.0,
      "displayOrder": 1
    }
  ],
  "specificQuizItemsJson": [
    {
      "itemId": "SPECIFIC-1",
      "questionBankId": 51,
      "questionText": "What does @Transactional provide?",
      "options": ["A. ...", "B. ...", "C. ...", "D. ..."],
      "categoryName": "BE_JAVA",
      "difficulty": "MEDIUM",
      "maxScore": 2.5,
      "displayOrder": 1
    }
  ],
  "specificCodingItemsJson": [
    {
      "itemId": "CODING-1",
      "codingProblemId": 8,
      "title": "Two Sum",
      "difficulty": "EASY",
      "problemStatement": "Return indices of two numbers whose sum equals target.",
      "rulesAndConstraints": ["Do not use global variables"],
      "visibleExamples": [
        {
          "inputs": ["[2,7,11,15]", "9"],
          "output": "[0,1]",
          "explanation": "2 + 7 = 9"
        }
      ],
      "codeStubs": {
        "JAVA": "class Solution { ... }",
        "PYTHON": "class Solution: ..."
      },
      "paramTypes": ["int[]", "int"],
      "returnType": "int[]",
      "executionTimeLimitMs": 1000,
      "memoryLimitMb": 256,
      "maxScore": 40.0,
      "displayOrder": 1
    }
  ]
}
```

Quan trọng:

- Response không có `correctAnswer`.
- Response không có hidden test cases.
- Mỗi lần gọi Start hiện tạo một attempt mới. FE cần disable double-click và không tự retry POST nếu chưa xác định request trước đã thất bại thật sự.
- FE nên lưu `attemptId`, `startedAt` cục bộ nếu cần và tính countdown từ `timeLimitMinutes`. Hiện response start không có `startedAt` và BE chưa cung cấp endpoint đồng bộ timer riêng.

Lỗi thường gặp:

- `400 Career preference not found`.
- `400 Not enough items for <SECTION>...`.
- `404 User not found`.

## 5.6 Run thử code

Run chỉ thực thi code với `visibleExamples` trong đề. Nó không chấm hidden tests và không cập nhật điểm attempt.

```http
POST /api/entry-tests/{attemptId}/coding/run
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "itemId": "CODING-1",
  "language": "JAVA",
  "sourceCode": [
    "class Solution {",
    "    public int[] twoSum(int[] nums, int target) {",
    "        return new int[] {0, 1};",
    "    }",
    "}"
  ]
}
```

Response `200 OK`:

```json
{
  "status": "COMPLETED",
  "passedTestCases": 1,
  "totalTestCases": 1,
  "executionTimeMs": 24,
  "errorMessage": null,
  "testCases": [
    {
      "index": 0,
      "status": "PASSED",
      "input": "[[2,7,11,15],9]",
      "expectedOutput": "[0,1]",
      "actualOutput": "[0,1]",
      "executionTimeMs": 20,
      "errorMessage": null
    }
  ]
}
```

FE behavior khuyến nghị:

- Disable nút Run trong lúc request đang pending.
- Hiển thị kết quả từng visible test case.
- Không dùng kết quả Run để tự tính điểm.
- Giữ source code trong state/local draft để đưa lại vào Submit.

Lỗi thường gặp:

- `400`: attempt không còn `IN_PROGRESS`.
- `400`: item không thuộc attempt.
- `400`: language không hỗ trợ hoặc source code rỗng.
- `403`: attempt thuộc user khác.
- `404`: attempt không tồn tại.
- `502`: sandbox không trả kết quả.

## 5.7 Submit Entry Test

```http
POST /api/entry-tests/{attemptId}/submit
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "answers": [
    {
      "itemId": "COMMON-1",
      "answerJson": {
        "selectedOption": "B"
      }
    },
    {
      "itemId": "SPECIFIC-1",
      "answerJson": {
        "selectedOption": "A"
      }
    },
    {
      "itemId": "CODING-1",
      "answerJson": {
        "language": "JAVA",
        "sourceCode": [
          "class Solution {",
          "    public int[] twoSum(int[] nums, int target) {",
          "        return new int[] {0, 1};",
          "    }",
          "}"
        ]
      }
    }
  ]
}
```

Quy tắc request:

- Mỗi `itemId` chỉ xuất hiện tối đa một lần.
- Không gửi item ngoài attempt.
- Quiz dùng `answerJson.selectedOption`.
- Coding bắt buộc có `language` và `sourceCode`.
- Không gửi `score`, `passed`, `total` hoặc `testResult`. BE không tin dữ liệu chấm điểm từ client.
- Có thể không gửi câu user bỏ trống; câu đó không được cộng điểm.

Khi submit coding, BE lấy toàn bộ hidden tests từ database, gửi sang sandbox và tính:

```text
codingItemScore = passedHiddenTests / totalHiddenTests × maxScore
```

Response `200 OK` rút gọn:

```json
{
  "id": 102,
  "userId": 7,
  "careerPreferenceId": 7,
  "entryTestId": 3,
  "selectedLanguagesJson": ["JAVA", "SPRING_BOOT"],
  "commonQuizItemsJson": [
    {
      "itemId": "COMMON-1",
      "questionBankId": 11,
      "questionText": "Which collection does not allow duplicate elements?",
      "options": ["A. List", "B. Set", "C. Queue", "D. Array"],
      "categoryName": "COMMON",
      "difficulty": "EASY",
      "maxScore": 2.0,
      "displayOrder": 1
    }
  ],
  "specificQuizItemsJson": [],
  "specificCodingItemsJson": [],
  "answersJson": [
    {
      "itemId": "COMMON-1",
      "sectionType": "COMMON_QUIZ",
      "answerType": "QUESTION_BANK",
      "answerJson": {
        "selectedOption": "B"
      },
      "score": 2.0,
      "isCorrect": true,
      "gradedAt": "2026-08-29T11:05:00"
    },
    {
      "itemId": "CODING-1",
      "sectionType": "SPECIFIC_CODING",
      "answerType": "CODING_PROBLEM",
      "answerJson": {
        "language": "JAVA",
        "sourceCode": ["class Solution { ... }"]
      },
      "score": 32.0,
      "isCorrect": false,
      "gradedAt": "2026-08-29T11:05:02"
    }
  ],
  "status": "GRADED",
  "startedAt": "2026-08-29T10:05:00",
  "submittedAt": "2026-08-29T11:05:02",
  "commonQuizScore": 26.0,
  "specificQuizScore": 25.0,
  "specificCodingScore": 32.0,
  "finalScore": 83.0,
  "resultLevel": "JUNIOR",
  "resultSnapshotJson": {
    "commonQuizScore": 26.0,
    "specificQuizScore": 25.0,
    "specificCodingScore": 32.0,
    "finalScore": 83.0
  },
  "createdAt": "2026-08-29T10:05:00",
  "updatedAt": "2026-08-29T11:05:02"
}
```

Sau success:

- Attempt chuyển thành `GRADED`.
- Career preference chuyển `needRetest = false`.
- User competency được tạo/cập nhật.
- FE nên khóa form và điều hướng sang result.

Lỗi thường gặp:

- `400 Entry test attempt is not in progress` khi submit lại.
- `400 Duplicate entry test item: ...`.
- `400 Unknown entry test item: ...`.
- `400 Coding answer must contain language and sourceCode`.
- `400 Level scale is not configured for this score`.
- `403` nếu attempt thuộc user khác.
- `404` nếu attempt/coding problem không tồn tại.

## 5.8 Lấy kết quả attempt

```http
GET /api/entry-tests/attempts/{attemptId}/result
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`: cùng schema `EntryTestAttemptResponse` của endpoint Submit.

Ví dụ tối giản:

```json
{
  "id": 102,
  "userId": 7,
  "entryTestId": 3,
  "status": "GRADED",
  "commonQuizScore": 26.0,
  "specificQuizScore": 25.0,
  "specificCodingScore": 32.0,
  "finalScore": 83.0,
  "resultLevel": "JUNIOR"
}
```

Endpoint hiện trả attempt thuộc current user ở mọi status. FE nên chỉ render màn hình kết quả hoàn chỉnh khi `status === "GRADED"`.

## 5.9 Lấy competency hiện tại

```http
GET /api/me/competency
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`:

```json
{
  "id": 15,
  "userId": 7,
  "careerPreferenceId": 7,
  "targetRole": "BE",
  "languagesJson": ["JAVA", "SPRING_BOOT"],
  "currentLevel": "JUNIOR",
  "currentScore": 83.0,
  "commonQuizScore": 26.0,
  "specificQuizScore": 25.0,
  "specificCodingScore": 32.0,
  "competencySnapshotJson": {
    "level": "JUNIOR",
    "finalScore": 83.0,
    "commonQuizScore": 26.0,
    "specificQuizScore": 25.0,
    "specificCodingScore": 32.0
  },
  "lastEntryTestAttemptId": 102,
  "lastEvaluatedAt": "2026-08-29T11:05:02",
  "createdAt": "2026-08-29T11:05:02",
  "updatedAt": "2026-08-29T11:05:02"
}
```

Nếu user chưa từng được đánh giá: `404 User competency not found`.

---

# 6. Admin Entry Test endpoints

## Entry Test object mẫu

```json
{
  "id": 3,
  "name": "Software Engineer Entry Test",
  "totalScore": 100.0,
  "timeLimitMinutes": 60,
  "sectionConfigs": [
    {
      "sectionType": "COMMON_QUIZ",
      "itemType": "QUESTION_BANK",
      "itemCount": 15,
      "totalScore": 30.0,
      "scorePerItem": 2.0,
      "displayOrder": 1
    },
    {
      "sectionType": "SPECIFIC_QUIZ",
      "itemType": "QUESTION_BANK",
      "itemCount": 12,
      "totalScore": 30.0,
      "scorePerItem": 2.5,
      "displayOrder": 2
    },
    {
      "sectionType": "SPECIFIC_CODING",
      "itemType": "CODING_PROBLEM",
      "itemCount": 1,
      "totalScore": 40.0,
      "scorePerItem": 40.0,
      "displayOrder": 3
    }
  ],
  "isActive": true,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T09:00:00"
}
```

## 6.1 Lấy tất cả Entry Test configurations

```http
GET /api/admin/entry-tests
Authorization: Bearer <access_token>
```

Response `200 OK`:

```json
[
  {
    "id": 3,
    "name": "Software Engineer Entry Test",
    "totalScore": 100.0,
    "timeLimitMinutes": 60,
    "sectionConfigs": [],
    "isActive": true,
    "createdAt": "2026-08-20T09:00:00",
    "updatedAt": "2026-08-29T09:00:00"
  }
]
```

## 6.2 Lấy Entry Test active

```http
GET /api/admin/entry-tests/active
Authorization: Bearer <access_token>
```

Response `200 OK`: một Entry Test object như mẫu trên.

Nếu không có active test: `404 Active entry test not found`.

## 6.3 Lấy Entry Test theo ID

```http
GET /api/admin/entry-tests/{id}
Authorization: Bearer <access_token>
```

Response `200 OK`:

```json
{
  "id": 3,
  "name": "Software Engineer Entry Test",
  "totalScore": 100.0,
  "timeLimitMinutes": 60,
  "sectionConfigs": [],
  "isActive": true,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T09:00:00"
}
```

## 6.4 Tạo Entry Test

```http
POST /api/admin/entry-tests
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Software Engineer Entry Test v2",
  "totalScore": 100.0,
  "timeLimitMinutes": 60,
  "sectionConfigs": [
    {
      "sectionType": "COMMON_QUIZ",
      "itemType": "QUESTION_BANK",
      "itemCount": 15,
      "totalScore": 30.0,
      "scorePerItem": 2.0,
      "displayOrder": 1
    },
    {
      "sectionType": "SPECIFIC_QUIZ",
      "itemType": "QUESTION_BANK",
      "itemCount": 12,
      "totalScore": 30.0,
      "scorePerItem": 2.5,
      "displayOrder": 2
    },
    {
      "sectionType": "SPECIFIC_CODING",
      "itemType": "CODING_PROBLEM",
      "itemCount": 1,
      "totalScore": 40.0,
      "scorePerItem": 40.0,
      "displayOrder": 3
    }
  ],
  "isActive": true
}
```

Response `200 OK`: Entry Test object đã tạo, có `id/createdAt/updatedAt`.

## 6.5 Cập nhật Entry Test

```http
PUT /api/admin/entry-tests/{id}
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request mẫu:

```json
{
  "name": "Software Engineer Entry Test v2",
  "timeLimitMinutes": 75,
  "isActive": true
}
```

Response `200 OK`:

```json
{
  "id": 3,
  "name": "Software Engineer Entry Test v2",
  "totalScore": 100.0,
  "timeLimitMinutes": 75,
  "sectionConfigs": [],
  "isActive": true,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T12:00:00"
}
```

Mapper update bỏ qua field `null`, nên FE có thể gửi partial body. Nếu muốn xóa/reset một field về `null`, contract hiện tại không hỗ trợ qua update này.

## 6.6 Deactivate Entry Test

Đây là soft deactivate, không xóa row.

```http
DELETE /api/admin/entry-tests/{id}
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`:

```json
{
  "id": 3,
  "name": "Software Engineer Entry Test v2",
  "totalScore": 100.0,
  "timeLimitMinutes": 75,
  "sectionConfigs": [],
  "isActive": false,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T12:05:00"
}
```

---

# 7. Admin Level Scale endpoints

## Level Scale object mẫu

```json
{
  "id": 21,
  "targetRole": "BE",
  "level": "JUNIOR",
  "minScore": 70.0,
  "maxScore": 84.99,
  "minCodingScore": 20.0,
  "isActive": true,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T09:00:00"
}
```

## 7.1 Lấy tất cả Level Scales

```http
GET /api/admin/level-scales
Authorization: Bearer <access_token>
```

Response `200 OK`:

```json
[
  {
    "id": 21,
    "targetRole": "BE",
    "level": "JUNIOR",
    "minScore": 70.0,
    "maxScore": 84.99,
    "minCodingScore": 20.0,
    "isActive": true,
    "createdAt": "2026-08-20T09:00:00",
    "updatedAt": "2026-08-29T09:00:00"
  }
]
```

## 7.2 Lấy Level Scale theo ID

```http
GET /api/admin/level-scales/{id}
Authorization: Bearer <access_token>
```

Response `200 OK`: một Level Scale object như mẫu trên.

Nếu không tồn tại: `404 Level scale not found`.

## 7.3 Tạo Level Scale

```http
POST /api/admin/level-scales
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "targetRole": "BE",
  "level": "JUNIOR",
  "minScore": 70.0,
  "maxScore": 84.99,
  "minCodingScore": 20.0,
  "isActive": true
}
```

Response `200 OK`: Level Scale object đã tạo.

Validation:

- `level` bắt buộc.
- `minScore` và `maxScore` bắt buộc.
- `minScore` không được lớn hơn `maxScore`.
- `targetRole = null` được hiểu là scale dùng chung.

## 7.4 Upsert toàn bộ scale của một role

Endpoint này tiện cho màn hình admin chỉnh nhiều level rồi bấm Save một lần.

```http
POST /api/admin/level-scales/set
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "targetRole": "BE",
  "scales": [
    {
      "level": "INTERN",
      "minScore": 0.0,
      "maxScore": 49.99,
      "minCodingScore": 0.0,
      "isActive": true
    },
    {
      "level": "FRESHER",
      "minScore": 50.0,
      "maxScore": 69.99,
      "minCodingScore": 10.0,
      "isActive": true
    },
    {
      "level": "JUNIOR",
      "minScore": 70.0,
      "maxScore": 84.99,
      "minCodingScore": 20.0,
      "isActive": true
    },
    {
      "level": "MIDDLE",
      "minScore": 85.0,
      "maxScore": 100.0,
      "minCodingScore": 30.0,
      "isActive": true
    }
  ]
}
```

Response `200 OK`:

```json
[
  {
    "id": 19,
    "targetRole": "BE",
    "level": "INTERN",
    "minScore": 0.0,
    "maxScore": 49.99,
    "minCodingScore": 0.0,
    "isActive": true,
    "createdAt": "2026-08-20T09:00:00",
    "updatedAt": "2026-08-29T13:00:00"
  },
  {
    "id": 21,
    "targetRole": "BE",
    "level": "JUNIOR",
    "minScore": 70.0,
    "maxScore": 84.99,
    "minCodingScore": 20.0,
    "isActive": true,
    "createdAt": "2026-08-20T09:00:00",
    "updatedAt": "2026-08-29T13:00:00"
  }
]
```

`scales` không được rỗng. Upsert match bản ghi cũ theo `level` trong cùng `targetRole`.

## 7.5 Cập nhật một Level Scale

```http
PUT /api/admin/level-scales/{id}
Authorization: Bearer <access_token>
Content-Type: application/json
```

Request:

```json
{
  "minScore": 72.0,
  "maxScore": 86.99,
  "minCodingScore": 22.0,
  "isActive": true
}
```

Response `200 OK`:

```json
{
  "id": 21,
  "targetRole": "BE",
  "level": "JUNIOR",
  "minScore": 72.0,
  "maxScore": 86.99,
  "minCodingScore": 22.0,
  "isActive": true,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T13:10:00"
}
```

Update bỏ qua field `null`, sau đó validate object hoàn chỉnh.

## 7.6 Deactivate Level Scale

```http
DELETE /api/admin/level-scales/{id}
Authorization: Bearer <access_token>
```

Request body: không có.

Response `200 OK`:

```json
{
  "id": 21,
  "targetRole": "BE",
  "level": "JUNIOR",
  "minScore": 72.0,
  "maxScore": 86.99,
  "minCodingScore": 22.0,
  "isActive": false,
  "createdAt": "2026-08-20T09:00:00",
  "updatedAt": "2026-08-29T13:15:00"
}
```

---

# 8. FE implementation notes

## State tối thiểu cho màn hình làm bài

```ts
type QuizDraft = Record<string, string>;

type CodingDraft = Record<
  string,
  {
    language: CompilerLanguage;
    sourceCode: string[];
  }
>;

type EntryTestState = {
  attemptId: number;
  test: EntryTestStartResponse;
  quizDrafts: QuizDraft;
  codingDrafts: CodingDraft;
  submitting: boolean;
};
```

Build payload submit:

```ts
function buildSubmitPayload(
  quizDrafts: QuizDraft,
  codingDrafts: CodingDraft,
) {
  const quizAnswers = Object.entries(quizDrafts).map(
    ([itemId, selectedOption]) => ({
      itemId,
      answerJson: { selectedOption },
    }),
  );

  const codingAnswers = Object.entries(codingDrafts).map(
    ([itemId, value]) => ({
      itemId,
      answerJson: {
        language: value.language,
        sourceCode: value.sourceCode,
      },
    }),
  );

  return { answers: [...quizAnswers, ...codingAnswers] };
}
```

Chống duplicate ở FE trước khi gửi:

```ts
function assertUniqueItemIds(
  answers: Array<{ itemId: string }>,
) {
  const ids = new Set<string>();

  for (const answer of answers) {
    if (ids.has(answer.itemId)) {
      throw new Error(`Duplicate itemId: ${answer.itemId}`);
    }
    ids.add(answer.itemId);
  }
}
```

## Checklist trước khi Submit

- `attemptId` đến từ response Start.
- Mọi `itemId` đến từ chính attempt hiện tại.
- Không có `itemId` trùng.
- `selectedOption` nên là ký hiệu `A/B/C/D`.
- Coding có `language` và `sourceCode` không rỗng.
- Không gửi điểm hoặc kết quả test do client tự tính.
- Disable Submit ngay lần click đầu tiên.
- Nếu request timeout, ưu tiên gọi Result bằng `attemptId` để kiểm tra trạng thái trước khi cho submit lại.

## Những giới hạn FE cần biết ở phiên bản hiện tại

- Start chưa idempotent; gọi nhiều lần tạo nhiều attempt.
- Không có endpoint resume/list attempts riêng.
- Không có autosave answer ở BE; FE phải tự giữ draft cho đến khi Submit.
- Timer hiện chủ yếu do FE quản lý; chưa có endpoint đồng bộ thời gian còn lại.
- Result endpoint có thể trả attempt `IN_PROGRESS`; FE phải kiểm tra `status`.
- Skip preference không đủ dữ liệu để sinh specific test; không gọi Start ngay sau Skip.

---

# 9. Quick endpoint reference

| Method | URL | Mục đích |
|---|---|---|
| GET | `/api/me/career-preference/exists` | Kiểm tra đã có preference |
| GET | `/api/me/career-preference` | Lấy preference hiện tại |
| PUT | `/api/me/career-preference` | Tạo/cập nhật preference |
| POST | `/api/me/career-preference/skip` | Bỏ qua onboarding preference |
| POST | `/api/entry-tests/start` | Tạo attempt và lấy đề |
| POST | `/api/entry-tests/{attemptId}/coding/run` | Chạy thử visible examples |
| POST | `/api/entry-tests/{attemptId}/submit` | Nộp toàn bộ bài và chấm điểm |
| GET | `/api/entry-tests/attempts/{attemptId}/result` | Lấy attempt/result |
| GET | `/api/me/competency` | Lấy competency mới nhất |
| GET | `/api/admin/entry-tests` | Danh sách cấu hình Entry Test |
| GET | `/api/admin/entry-tests/active` | Cấu hình active gần nhất |
| GET | `/api/admin/entry-tests/{id}` | Chi tiết cấu hình |
| POST | `/api/admin/entry-tests` | Tạo cấu hình |
| PUT | `/api/admin/entry-tests/{id}` | Cập nhật cấu hình |
| DELETE | `/api/admin/entry-tests/{id}` | Deactivate cấu hình |
| GET | `/api/admin/level-scales` | Danh sách scale |
| GET | `/api/admin/level-scales/{id}` | Chi tiết scale |
| POST | `/api/admin/level-scales` | Tạo scale |
| POST | `/api/admin/level-scales/set` | Upsert scale theo role |
| PUT | `/api/admin/level-scales/{id}` | Cập nhật scale |
| DELETE | `/api/admin/level-scales/{id}` | Deactivate scale |
