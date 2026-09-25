import fs from 'node:fs/promises';
import path from 'node:path';

const root = process.cwd();
const reportDir = path.join(root, 'target', 'surefire-reports');

const specs = [
  ['AdminManagementServiceImpl', 'Admin dashboard JD/application statistics', 'ACTIVE', 'EXE_FE/src/services/admin-application.manager.ts calls /api/admin/open-jds, /api/admin/jds/{jdId}/applications and application detail endpoints.', 2],
  ['ApiClientImpl', 'External AI/compiler API adapter boundary', 'ACTIVE', 'Active interview, entry-test coding and speech flows call backend services that delegate to ApiClient.', 3],
  ['ApplicationDetailServiceImpl', 'Application scoring, reviewer and mentor assignment', 'ACTIVE', 'EXE_FE/src/services/application-detail.manager.ts and staff grading pages call reviewer, HR-score and mentor-selection endpoints.', 2],
  ['ApplicationServiceImpl', 'Application creation, lookup and round transition', 'ACTIVE', 'EXE_FE/src/services/application.manager.ts and src/hooks/useApplication.ts call /api/applications, /me and /{id}.', 2],
  ['CandidateProfileImpl', 'Candidate profile CRUD and association preservation', 'ACTIVE', 'EXE_FE/src/services/candidate-profile.manager.ts calls candidate profile list, create, update and user lookup endpoints.', 2],
  ['ChatServiceImpl', 'Chat sessions, contacts and message history', 'ACTIVE', 'EXE_FE/src/services/chat.manager.ts is used by production messenger flows and calls /api/chat/sessions and /api/messages/contacts.', 4],
  ['CodeReviewProblemServiceImpl', 'Code review problem CRUD and generation', 'ACTIVE', 'EXE_FE/src/hooks/useCodeReviewProblems.ts and the application CodeReviewModule consume code-review problem endpoints.', 1],
  ['CodingProblemServiceImpl', 'Coding problem CRUD and generation', 'ACTIVE', 'EXE_FE/src/services/coding-problem.manager.ts and coding submission screens consume coding problem endpoints.', 1],
  ['CompanyServiceImpl', 'Company search and administration', 'ACTIVE', 'EXE_FE/src/services/company.manager.ts and company production pages call /api/companies and company job endpoints.', 2],
  ['CompetencyChartServiceImpl', 'Competency chart calculation', 'ACTIVE', 'EXE_FE/src/services/competency-chart.manager.ts calls /api/applications/{applicationId}/competency-chart from production result views.', 4],
  ['DashboardServiceImpl', 'Dashboard aggregate counts', 'ACTIVE', 'EXE_FE/src/services/dashboard-admin.manager.ts calls total-user, total-mentor, total-income and total-session endpoints.', 0],
  ['FaceAnalysisServiceImpl', 'Computer-vision face behavior analysis', 'UNUSED_OR_UNVERIFIED', 'Only interview-analysis service/config exists; no complete production caller flow was verified. Explicitly excluded by scope prompt.', 0],
  ['InterviewProcessServiceImpl', 'AI interview start, answer and timeout', 'ACTIVE', 'EXE_FE/src/services/kiosk/kioskApi.service.ts and AI interview UI call /api/v1/interview/start, submit and timeout.', 5],
  ['InterviewSessionServiceImpl', 'AI interview session setup, cache and history', 'ACTIVE', 'EXE_FE/src/hooks/useInterviewSession.ts calls config-options, generate-job-requirement, create-session, session and cache endpoints.', 2],
  ['InterviewTemplateServiceImpl', 'Interview template administration', 'ACTIVE', 'EXE_FE/src/services/interview-template.manager.ts calls production /api/templates CRUD endpoints.', 1],
  ['JdPurchaseServiceImpl', 'JD purchase entitlement and history', 'ACTIVE', 'EXE_FE/src/services/jd-purchase.manager.ts calls /api/jd-purchases/check and /api/jd-purchases/me in the application flow.', 1],
  ['JobDescriptionServiceImpl', 'Job search, lifecycle and round lookup', 'ACTIVE', 'EXE_FE/src/services/job-description.manager.ts and src/hooks/useJobDescription.ts call list, search, detail, create, update and toggle endpoints.', 4],
  ['JourneySummaryServiceImpl', 'Application journey summary', 'ACTIVE', 'EXE_FE/src/services/competency-chart.manager.ts calls journey-summary endpoints from final competency and kiosk result views.', 4],
  ['KioskBookingServiceImpl', 'Kiosk booking, cancellation and entry', 'ACTIVE', 'EXE_FE/src/services/kiosk.manager.ts and production kiosk routes call pick-slot, booking lookup and kiosk entry.', 5],
  ['KioskServiceImpl', 'Kiosk CRUD, schedules, slots and history', 'ACTIVE', 'EXE_FE/src/services/kiosk.manager.ts and /user/kiosk production routes call kiosk, schedules, slots and history endpoints.', 6],
  ['MailServiceImpl', 'Transactional email boundary', 'ACTIVE', 'Forgot/reset password invokes backend mail delivery; the frontend entry points are in EXE_FE/src/services/auth.manager.ts.', 0],
  ['MentorFeedbackServiceImpl', 'Candidate feedback for mentor sessions', 'ACTIVE', 'EXE_FE/src/services/mentor-feedback.manager.ts and production feedback pages call mentor-feedback CRUD endpoints.', 4],
  ['MentorReviewServiceImpl', 'Mentor review and round completion', 'ACTIVE', 'EXE_FE/src/services/mentor-review.manager.ts and mentor review pages call mentor-review endpoints.', 4],
  ['MentorServiceImpl', 'Mentor profile, availability and password', 'ACTIVE', 'EXE_FE/src/services/mentor.manager.ts and mentor list/detail/admin pages call mentor CRUD and change-password endpoints.', 5],
  ['NotificationServiceImpl', 'Notification list, creation and read state', 'ACTIVE', 'EXE_FE/src/services/notification.manager.ts and production notification pages use /api/notifications.', 1],
  ['PasswordResetServiceImpl', 'Password reset OTP lifecycle', 'ACTIVE', 'EXE_FE/src/services/auth.manager.ts calls /api/auth/forgot-password and /api/auth/reset-password.', 1],
  ['PaymentServiceImpl', 'JD/session payment and webhook state', 'ACTIVE', 'EXE_FE/src/services/jd-purchase.manager.ts and payment callback pages call /api/payments/pay and cancellation/recovery flows.', 3],
  ['PostServiceImpl', 'Community posts, comments and likes', 'ACTIVE', 'EXE_FE/src/services/post.manager.ts, src/hooks/usePostFeed.ts and community/blog feeds call post, like and comment endpoints.', 3],
  ['ProctoringServiceImpl', 'Interview proctoring snapshots', 'UNUSED_OR_UNVERIFIED', 'No verified production screen sends /api/v1/proctoring/track events with a complete contract. Explicitly excluded by scope prompt.', 0],
  ['QuestionBankServiceImpl', 'Question bank administration and generation', 'ACTIVE', 'EXE_FE staff QuestionBankManagement page and question-bank manager call CRUD and generation endpoints.', 2],
  ['QuestionCategoryServiceImpl', 'Question category administration', 'ACTIVE', 'EXE_FE QuestionBankCategoryTab and question-category manager call category CRUD endpoints.', 2],
  ['RoundServiceImpl', 'JD round setup, update and AI plan', 'ACTIVE', 'EXE_FE/src/hooks/useRound.ts and RoundCanvasEditor call setup/update, application-order and generation endpoints.', 6],
  ['SessionServiceImpl', 'Mentor session creation, join, payment and status', 'ACTIVE', 'EXE_FE/src/hooks/useSession.ts, useDailyTracking.ts and mock-interview routes call session lifecycle endpoints.', 6],
  ['SpeechServiceImpl', 'Interview TTS and transcript enhancement', 'ACTIVE', 'EXE_FE kiosk interview service calls TTS/voices and production AI interview uses speech endpoints.', 3],
  ['UserScheduleServiceImpl', 'Candidate and mentor schedules', 'ACTIVE', 'EXE_FE user and mentor Overview pages consume schedule hooks for calendar events.', 4],
  ['UserServiceImpl', 'User registration, profile, CV and password', 'ACTIVE', 'EXE_FE/src/services/auth.manager.ts, users-admin.manager.ts and account pages call user create/profile/CV/password endpoints.', 7],
  ['JobRecommendationServiceImpl', 'Recommendation threshold and candidate matching', 'ACTIVE', 'EXE_FE job-description.manager.ts calls recommendations and job-recommendation-admin.manager.ts updates the threshold.', 4],
  ['AuthController', 'Login, OAuth redirect and reset controller contract', 'ACTIVE', 'EXE_FE/src/services/auth.manager.ts calls login, forgot-password, reset-password and handles OAuth callback routes.', 5],
  ['InblueApplicationTests', 'Application context smoke test', 'LEGACY', 'Technical smoke test retained from the existing workbook; it is not a frontend product function.', 0],
  ['AdminEntryTestServiceImpl', 'Entry test administration', 'ACTIVE', 'EXE_FE/src/services/entry-test-admin.manager.ts and Admin EntryTestManagement page call /api/admin/entry-tests CRUD.', 1],
  ['AdminLevelScaleServiceImpl', 'Competency level-scale administration', 'ACTIVE', 'EXE_FE/src/services/entry-test-admin.manager.ts calls /api/admin/level-scales CRUD and set endpoints.', 1],
  ['CareerPreferenceServiceImpl', 'Career preference onboarding and skip', 'ACTIVE', 'EXE_FE entry-test.manager.ts calls /api/me/career-preference read, exists, update and skip.', 1],
  ['EntryTestServiceImpl', 'Entry test start, code run, submit and result', 'ACTIVE', 'EXE_FE entry-test.manager.ts calls start, coding/run, submit and attempt result endpoints.', 3],
  ['UserCompetencyServiceImpl', 'Current competency and level resolution', 'ACTIVE', 'EXE_FE entry-test manager calls /api/me/competency and Account page renders the competency.', 2],
  ['TopDevCrawlerServiceImpl', 'TopDev job search and import', 'ACTIVE', 'EXE_FE AdminDashboard enables /admin/topdev-job-import and topdev-job-import.manager.ts calls search/categories/import.', 5],
  ['EmailSubmissionServiceImpl', 'Email round submission and grading', 'ACTIVE', 'EXE_FE/src/hooks/useEmailSubmission.ts and EmailSimulatorModule call /api/email-submissions/{id} in a production application round.', 5],
  ['PineconeEmbeddingServiceImpl', 'Skill embedding generation', 'ACTIVE', 'CareerPreferenceServiceImpl invokes EmbeddingService for the active frontend career-preference flow.', 3],
];

const aliases = new Map([
  ['ApplicationServiceActiveFlowTest', 'ApplicationServiceImpl'],
  ['CandidateProfileServiceActiveFlowTest', 'CandidateProfileImpl'],
  ['JobDescriptionServiceActiveFlowTest', 'JobDescriptionServiceImpl'],
  ['JobRecommendationServiceActiveFlowTest', 'JobRecommendationServiceImpl'],
  ['NotificationServiceActiveFlowTest', 'NotificationServiceImpl'],
  ['PasswordResetServiceActiveFlowTest', 'PasswordResetServiceImpl'],
  ['QuestionCategoryServiceActiveFlowTest', 'QuestionCategoryServiceImpl'],
  ['SessionServiceImplActiveFlowTest', 'SessionServiceImpl'],
]);

const xmlDecode = value => String(value ?? '').replaceAll('&quot;', '"').replaceAll('&apos;', "'")
  .replaceAll('&lt;', '<').replaceAll('&gt;', '>').replaceAll('&amp;', '&');

function attrs(text) {
  const result = {};
  for (const match of text.matchAll(/([\w:-]+)="([^"]*)"/g)) result[match[1]] = xmlDecode(match[2]);
  return result;
}

function functionForTestClass(simpleName) {
  if (simpleName === 'InblueApplicationTests') return simpleName;
  if (aliases.has(simpleName)) return aliases.get(simpleName);
  return simpleName.replace(/ActiveFlowTest$/, '').replace(/Test$/, '');
}

const caseTypeOverrides = new Map([
  ['openJdStatsGroupSoftFailedApplicationsAsFailed', 'N'],
  ['upsertLevelScaleSetUpdatesExistingAndCreatesMissingLevels', 'N'],
  ['skipPreferenceCreatesActiveRetestMarkerWhenMissing', 'N'],
  ['parseIdAndTypeHandleMalformedParticipantKey', 'A'],
  ['applyForPaidJdRequiresPurchasedPackage', 'A'],
  ['resolveLevelHonorsMinimumCodingScoreBoundary', 'B'],
  ['getAssignedMentorsSkipsDeletedMentor', 'A'],
  ['getMyPurchasesMarksExpiredAndEnrichesJobAndPayment', 'B'],
]);

function caseType(method) {
  if (caseTypeOverrides.has(method)) return caseTypeOverrides.get(method);
  if (/^empty|contextLoads|Boundary|PreservesEmpty|ReturnsEmpty|EmptyCollection|NoneAssigned|NonPositive|RoundsToTwoDecimals|ShortLivedOtp/i.test(method)) return 'B';
  if (/Rejects|Forbids|Cannot|Missing|Unknown|Invalid|Failure|Wrong|Outside|Unsupported|Duplicate|DoesNotWrite|SoftFailed|Expired/i.test(method)) return 'A';
  return 'N';
}

function expected(method) {
  const words = method.replace(/([a-z0-9])([A-Z])/g, '$1 $2').replaceAll('_', ' ').toLowerCase();
  return words.charAt(0).toUpperCase() + words.slice(1) + '.';
}

function formatDate(date) {
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' }).formatToParts(date);
  const get = type => parts.find(part => part.type === type)?.value;
  return `${get('year')}-${get('month')}-${get('day')} ${get('hour')}:${get('minute')}`;
}

async function listFiles(directory, suffix) {
  const result = [];
  for (const entry of await fs.readdir(directory, { withFileTypes: true })) {
    const full = path.join(directory, entry.name);
    if (entry.isDirectory()) result.push(...await listFiles(full, suffix));
    else if (entry.name.endsWith(suffix)) result.push(full);
  }
  return result;
}

const testFiles = await listFiles(path.join(root, 'src', 'test', 'java'), '.java');
const testSources = new Map();
for (const file of testFiles) {
  const content = await fs.readFile(file, 'utf8');
  const classMatch = content.match(/\bclass\s+(\w+)/);
  if (classMatch && !content.slice(0, classMatch.index).trimStart().startsWith('//')) {
    testSources.set(classMatch[1], { file, lines: content.split(/\r?\n/) });
  }
}

const productionFiles = await listFiles(path.join(root, 'src', 'main', 'java'), '.java');
const productionSources = new Map();
for (const file of productionFiles) {
  const content = await fs.readFile(file, 'utf8');
  const classMatch = content.match(/\bclass\s+(\w+)/);
  if (classMatch) productionSources.set(classMatch[1], { file, content });
}

const executedByFunction = new Map();
const reportFiles = (await fs.readdir(reportDir)).filter(name => /^TEST-.*\.xml$/.test(name)).sort();
for (const reportName of reportFiles) {
  const reportPath = path.join(reportDir, reportName);
  const xml = await fs.readFile(reportPath, 'utf8');
  const stat = await fs.stat(reportPath);
  for (const match of xml.matchAll(/<testcase\b([^>]*?)(?:\/>|>([\s\S]*?)<\/testcase>)/g)) {
    const attributes = attrs(match[1]);
    const body = match[2] ?? '';
    const suiteName = attrs(xml.match(/<testsuite\b([^>]*)>/)?.[1] ?? '').name;
    const fullClass = attributes.classname || suiteName;
    const simpleClass = fullClass.split('.').at(-1);
    const functionName = functionForTestClass(simpleClass);
    const source = testSources.get(simpleClass);
    const lineIndex = source?.lines.findIndex(line => new RegExp(`\\b${attributes.name}\\s*\\(`).test(line)) ?? -1;
    const relativeSource = source ? path.relative(root, source.file).replaceAll('\\', '/') : '';
    const failure = body.match(/<(failure|error)\b([^>]*)>/);
    const skipped = body.match(/<skipped\b([^>]*)\/?\s*>/);
    const actualResult = failure ? 'FAIL' : skipped ? 'SKIPPED' : 'PASS';
    const failureAttrs = attrs(failure?.[2] ?? '');
    const skippedAttrs = attrs(skipped?.[1] ?? '');
    const item = {
      test_method: attributes.name,
      source: `${relativeSource}:${lineIndex + 1}`,
      case_type: caseType(attributes.name),
      expected_result: expected(attributes.name),
      actual_result: actualResult,
      skip_reason: actualResult === 'SKIPPED' ? (skippedAttrs.message || 'Test was skipped by its JUnit condition.') : '',
      test_log: `target/surefire-reports/${reportName.replace(/^TEST-/, '').replace(/\.xml$/, '.txt')}`,
      executed_date: formatDate(stat.mtime),
      defect_message: actualResult === 'FAIL' ? (failureAttrs.message || 'Surefire reported a failed assertion or test error.') : '',
    };
    if (!executedByFunction.has(functionName)) executedByFunction.set(functionName, []);
    executedByFunction.get(functionName).push(item);
  }
}

const functions = [];
const allCases = [];
const utcids = new Set();
const knownDefects = [];
for (let index = 0; index < specs.length; index++) {
  const [className, description, classification, frontendEvidence, gapCount] = specs[index];
  const rawCases = (executedByFunction.get(className) ?? []).sort((a, b) => a.test_method.localeCompare(b.test_method));
  const testCases = rawCases.map((item, caseIndex) => {
    const utcid = `UTCID${index + 1}_${String(caseIndex + 1).padStart(3, '0')}`;
    if (utcids.has(utcid)) throw new Error(`Duplicate UTCID: ${utcid}`);
    utcids.add(utcid);
    let defectId = '';
    if (item.actual_result === 'FAIL') {
      defectId = `DEF-${String(knownDefects.length + 1).padStart(3, '0')}`;
      knownDefects.push({ defect_id: defectId, description: item.defect_message, related_tests: [utcid] });
    }
    const { defect_message, ...clean } = item;
    const testCase = { utcid, ...clean, defect_id: defectId };
    allCases.push(testCase);
    return testCase;
  });
  const production = productionSources.get(className);
  const loc = production ? production.content.split(/\r?\n/).filter(line => line.trim() && !line.trim().startsWith('//')).length : 'N/A';
  const knownGapCount = classification === 'ACTIVE' ? gapCount : 0;
  const testRequirement = testCases.length
    ? `${testCases.length} executable test case(s) from the current Maven Surefire run. ${knownGapCount ? `${knownGapCount} known high-level scenario gap(s) remain.` : 'No known high-level scenario gap is recorded.'}`
    : classification === 'ACTIVE'
      ? `No executable unit test was produced for this active function; ${knownGapCount} known high-level scenario gap(s) remain.`
      : 'Excluded from executable business tests because the frontend production flow is unverified or the item is technical legacy.';
  functions.push({ no: index + 1, class_name: className, function_code: className, sheet_name: `Function ${index + 1}`,
    function_description: description, classification, frontend_evidence: frontendEvidence,
    pre_condition: 'JDK 21; JUnit 5; Mockito fixtures; external providers and repositories mocked; Maven Surefire report available.',
    lines_of_code: loc, created_by: 'Codex / QA', executed_by: 'Maven Surefire', test_requirement: testRequirement,
    known_gap_count: knownGapCount, test_cases: testCases });
}

const allowedResults = new Set(['PASS', 'FAIL', 'SKIPPED']);
const allowedTypes = new Set(['N', 'A', 'B']);
const requiredFields = ['utcid', 'test_method', 'source', 'case_type', 'expected_result', 'actual_result', 'skip_reason', 'test_log', 'executed_date', 'defect_id'];
for (const testCase of allCases) {
  for (const field of requiredFields) if (!(field in testCase)) throw new Error(`Missing ${field} in ${testCase.utcid}`);
  if (!allowedResults.has(testCase.actual_result)) throw new Error(`Invalid result in ${testCase.utcid}`);
  if (!allowedTypes.has(testCase.case_type)) throw new Error(`Invalid case type in ${testCase.utcid}`);
  if (!testCase.source || testCase.source.endsWith(':0')) throw new Error(`Unresolved source in ${testCase.utcid}`);
}
if (allCases.filter(item => allowedResults.has(item.actual_result)).length !== allCases.length) throw new Error('Test/result count mismatch');
const fingerprints = functions.filter(item => item.test_cases.length).map(item => JSON.stringify(item.test_cases.map(test => [test.test_method, test.source])));
if (new Set(fingerprints).size !== fingerprints.length) throw new Error('Two non-empty function test arrays are identical');
const unassigned = [...executedByFunction.keys()].filter(name => !specs.some(spec => spec[0] === name));
if (unassigned.length) throw new Error(`Surefire tests are not assigned to a function: ${unassigned.join(', ')}`);

const report = {
  project: { name: 'Design and Implementation of an AI-Powered Recruitment Process Simulation System with a Virtual AI Interview Room for Software Engineering Students', code: 'SU26SE018', creator: 'Tran Nhat Tan', reviewer: 'Nguyen Pham Thu Ha', issue_date: '2026-09-13', test_environment: 'JDK 21; Maven Wrapper; JUnit 5; Mockito; Surefire; external services mocked.' },
  change_log_entry: { effective_date: '2026-09-13', version: '4.4', change_description: 'Expanded current executable backend tests across active frontend flows, replaced stale workbook-only PASS evidence with Surefire results, and self-validated schema fields, result counts, UTCIDs and duplicate function content.', reference: 'backend-unit-test-scope-prompt.md; Report5_Unit_Test_v4.3.xlsx; current workspace Maven Surefire run' },
  functions,
  known_defects: knownDefects,
};

await fs.writeFile(path.join(root, 'unit-test-report.json'), `${JSON.stringify(report, null, 2)}\n`);
const counts = Object.fromEntries(['PASS', 'FAIL', 'SKIPPED'].map(result => [result, allCases.filter(item => item.actual_result === result).length]));
console.log(JSON.stringify({ functions: functions.length, test_cases: allCases.length, unique_utcids: utcids.size, results: counts, known_defects: knownDefects.length }));
