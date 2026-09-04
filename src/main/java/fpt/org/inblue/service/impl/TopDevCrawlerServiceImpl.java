package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.AnythingLlmWorkspace;
import fpt.org.inblue.enums.JobDescriptionStatus;
import fpt.org.inblue.enums.TargetLevel;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.dto.request.CreateCompanyRequest;
import fpt.org.inblue.model.dto.request.CreateJobDescriptionRequest;
import fpt.org.inblue.model.dto.request.TopDevJobImportRequest;
import fpt.org.inblue.model.dto.response.SkillTagExtractionResponse;
import fpt.org.inblue.model.dto.response.TopDevJobImportResponse;
import fpt.org.inblue.model.dto.response.TopDevJobPreviewResponse;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.repository.JobDescriptionRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.EmbeddingService;
import fpt.org.inblue.service.JobDescriptionService;
import fpt.org.inblue.service.TopDevCrawlerService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class TopDevCrawlerServiceImpl implements TopDevCrawlerService {

    private static final String TOPDEV_SEARCH_URL = "https://topdev.vn/jobs/search";
    private static final String TOPDEV_API_URL = "https://api.topdev.vn/td/v2/jobs/";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; InblueJobImporter/1.0)";
    private static final int REQUEST_TIMEOUT_MILLIS = 15_000;
    private static final int DEFAULT_CATEGORY_ID = 2;

    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobDescriptionService jobDescriptionService;
    private final ApiClient apiClient;
    private final EmbeddingService embeddingService;

    public TopDevCrawlerServiceImpl(
            ObjectMapper objectMapper,
            CompanyRepository companyRepository,
            JobDescriptionRepository jobDescriptionRepository,
            JobDescriptionService jobDescriptionService,
            ApiClient apiClient,
            EmbeddingService embeddingService) {
        this.objectMapper = objectMapper;
        this.companyRepository = companyRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.jobDescriptionService = jobDescriptionService;
        this.apiClient = apiClient;
        this.embeddingService = embeddingService;
    }

    @Override
    @Transactional
    public TopDevJobImportResponse importJob(TopDevJobImportRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CustomException("JD title không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new CustomException("Company name không được để trống", HttpStatus.BAD_REQUEST);
        }
        if (request.getSourceJobId() != null
                && !request.getSourceJobId().isBlank()
                && jobDescriptionRepository
                        .findFirstBySourceJobIdAndIsDeletedFalse(request.getSourceJobId())
                        .isPresent()) {
            throw new CustomException("JD TopDev này đã được import trước đó", HttpStatus.CONFLICT);
        }

        String companyName = request.getCompanyName().trim();
        Company company = companyRepository
                .findFirstByNameIgnoreCaseAndIsDeletedFalse(companyName)
                .orElse(null);
        boolean companyCreated = false;

        if (company == null) {
            CreateCompanyRequest companyRequest = new CreateCompanyRequest();
            companyRequest.setName(companyName);
            companyRequest.setDescription(
                    "Imported from " + (request.getSource() == null ? "external source" : request.getSource()));
            companyRequest.setStatus("ACTIVE");
            company = new Company();
            company.setName(companyRequest.getName());
            company.setDescription(
                    request.getCompanyDescription() != null
                                    && !request.getCompanyDescription().isBlank()
                            ? request.getCompanyDescription()
                            : companyRequest.getDescription());
            company.setLogoUrl(request.getCompanyLogo());
            company.setStatus(companyRequest.getStatus());
            company.setIsDeleted(false);
            company = companyRepository.save(company);
            companyCreated = true;
        } else {
            boolean companyUpdated = false;
            if ((company.getLogoUrl() == null || company.getLogoUrl().isBlank())
                    && request.getCompanyLogo() != null
                    && !request.getCompanyLogo().isBlank()) {
                company.setLogoUrl(request.getCompanyLogo());
                companyUpdated = true;
            }
            if ((company.getDescription() == null
                            || company.getDescription().isBlank()
                            || company.getDescription().startsWith("Imported from "))
                    && request.getCompanyDescription() != null
                    && !request.getCompanyDescription().isBlank()) {
                company.setDescription(request.getCompanyDescription());
                companyUpdated = true;
            }
            if (companyUpdated) {
                company = companyRepository.save(company);
            }
        }

        List<String> skillTags = extractSkillTagsFromAnythingLlm(request, company);
        if (skillTags.isEmpty()) {
            skillTags = parseTopDevSkillTags(request.getSkills());
        }

        float[] skillEmbedding = null;
        if (!skillTags.isEmpty()) {
            skillEmbedding = embeddingService.generateEmbedding(String.join(", ", skillTags));
        }

        CreateJobDescriptionRequest jdRequest = CreateJobDescriptionRequest.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .requirements(request.getRequirements() == null ? request.getSkills() : request.getRequirements())
                .benefits(request.getBenefits())
                .skillTags(skillTags)
                .skillEmbedding(skillEmbedding)
                .level(request.getRequestedLevel())
                .status(JobDescriptionStatus.DRAFT)
                .companyId(company.getId())
                .sourceJobId(request.getSourceJobId())
                .currency("VND")
                .build();

        JobDescription jobDescription;
        try {
            jobDescription = jobDescriptionService.create(jdRequest);
        } catch (java.io.IOException exception) {
            throw new CustomException("Không thể tạo Job Description từ dữ liệu TopDev", HttpStatus.BAD_GATEWAY);
        }

        return TopDevJobImportResponse.builder()
                .companyId(company.getId())
                .jobDescriptionId(jobDescription.getId())
                .companyName(company.getName())
                .jobDescriptionTitle(jobDescription.getTitle())
                .jobDescriptionStatus(jobDescription.getStatus())
                .companyCreated(companyCreated)
                .build();
    }

    private List<String> extractSkillTagsFromAnythingLlm(TopDevJobImportRequest request, Company company) {
        TopDevJobPreviewResponse payload = TopDevJobPreviewResponse.builder()
                .source(request.getSource())
                .sourceUrl(request.getSourceUrl())
                .sourceJobId(request.getSourceJobId())
                .title(request.getTitle())
                .companyName(company.getName())
                .companyLogo(company.getLogoUrl())
                .companyDescription(company.getDescription())
                .location(request.getLocation())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .benefits(request.getBenefits())
                .skills(request.getSkills())
                .salary(request.getSalary())
                .requestedLevel(request.getRequestedLevel())
                .build();

        try {
            SkillTagExtractionResponse response = apiClient.sendChatToAnythingLlm(
                    AnythingLlmWorkspace.SKILL_TAGS,
                    payload,
                    "topdev-skill-tags-"
                            + (request.getSourceJobId() == null ? request.getTitle() : request.getSourceJobId()),
                    true,
                    null,
                    SkillTagExtractionResponse.class);
            return normalizeSkillTags(response == null ? null : response.getSkillTags());
        } catch (Exception exception) {
            return List.of();
        }
    }

    private List<String> parseTopDevSkillTags(String skills) {
        if (skills == null || skills.isBlank()) {
            return List.of();
        }
        return normalizeSkillTags(List.of(skills.split("[,;|\\n]")));
    }

    private List<String> normalizeSkillTags(List<String> skillTags) {
        if (skillTags == null || skillTags.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String skill : skillTags) {
            if (skill == null) {
                continue;
            }
            String value = skill.trim().replaceAll("\\s+", " ");
            if (!value.isBlank()) {
                normalized.add(value);
            }
        }
        return new ArrayList<>(normalized);
    }

    @Override
    public List<TopDevJobPreviewResponse> searchJobs(
            String keyword, TargetLevel level, List<Integer> jobCategoryIds, int page, int limit) {
        if (page < 1) {
            throw new CustomException("Page phải lớn hơn hoặc bằng 1", HttpStatus.BAD_REQUEST);
        }

        List<Integer> categories =
                jobCategoryIds == null || jobCategoryIds.isEmpty() ? List.of(DEFAULT_CATEGORY_ID) : jobCategoryIds;

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String searchKeyword = level == null || level.name().isBlank()
                ? normalizedKeyword
                : (normalizedKeyword.isBlank() ? level.name() : normalizedKeyword + " " + level.name());
        String searchUrl = buildSearchUrl(searchKeyword, categories, page);

        try {
            Document searchDocument = Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT_MILLIS)
                    .get();

            Set<String> detailUrls = extractDetailUrls(searchDocument);
            List<TopDevJobPreviewResponse> results = new ArrayList<>();

            for (String detailUrl : detailUrls) {
                if (results.size() >= limit) {
                    break;
                }
                try {
                    results.add(crawlDetail(detailUrl, level));
                } catch (Exception ignored) {
                    // Một JD lỗi không làm hỏng toàn bộ kết quả crawl.
                }
            }

            return results;
        } catch (IOException exception) {
            throw new CustomException("Không thể crawl dữ liệu việc làm từ TopDev", HttpStatus.BAD_GATEWAY);
        }
    }

    private String buildSearchUrl(String keyword, List<Integer> categoryIds, int page) {
        String categories = categoryIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("2");
        String url = TOPDEV_SEARCH_URL + "?job_categories_ids=" + categories + "&page=" + page;
        if (keyword != null && !keyword.isBlank()) {
            url += "&keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
        }
        return url;
    }

    private Set<String> extractDetailUrls(Document searchDocument) {
        Set<String> urls = new LinkedHashSet<>();
        for (Element link : searchDocument.select("a[href*=/detail-jobs/]")) {
            String href = link.absUrl("href");
            if (href != null && !href.isBlank()) {
                urls.add(href.split("\\?")[0]);
            }
        }
        return urls;
    }

    private TopDevJobPreviewResponse crawlDetail(String detailUrl, TargetLevel requestedLevel) throws IOException {
        Document detailDocument = Jsoup.connect(detailUrl)
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT_MILLIS)
                .get();

        JsonNode jobPosting = findJobPosting(detailDocument);
        if (jobPosting == null) {
            throw new IOException("TopDev JobPosting metadata not found");
        }

        JsonNode organization = jobPosting.path("hiringOrganization");
        JsonNode location = jobPosting.path("jobLocation");
        JsonNode baseSalary = jobPosting.path("baseSalary");
        String companyUrl = textOrNull(organization, "sameAs");
        String fallbackCompanyDescription = cleanHtml(textOrNull(organization, "description"));
        CompanyPageData companyPageData = crawlCompanyPage(companyUrl);
        String companyDescription =
                companyPageData.description() == null ? fallbackCompanyDescription : companyPageData.description();
        String companyLogo = companyPageData.logo() == null ? textOrNull(organization, "logo") : companyPageData.logo();
        String rawJobDescription = textOrNull(jobPosting, "description");
        JobSections jobSections = fetchFullJobSections(detailUrl);
        if (jobSections.isEmpty()) {
            jobSections = extractJobSections(rawJobDescription);
        }

        String sourceJobId = extractJobId(detailUrl);
        JobDescription existingJobDescription = sourceJobId == null
                ? null
                : jobDescriptionRepository
                        .findFirstBySourceJobIdAndIsDeletedFalse(sourceJobId)
                        .orElse(null);

        return TopDevJobPreviewResponse.builder()
                .source("TOPDEV")
                .sourceUrl(detailUrl)
                .sourceJobId(sourceJobId)
                .isExist(existingJobDescription != null)
                .existingJobDescriptionId(existingJobDescription == null ? null : existingJobDescription.getId())
                .title(textOrNull(jobPosting, "title"))
                .companyName(textOrNull(organization, "name"))
                .companyLogo(companyLogo)
                .companyDescription(companyDescription)
                .location(extractLocation(location))
                .description(
                        jobSections.description() == null ? cleanHtml(rawJobDescription) : jobSections.description())
                .requirements(jobSections.requirements())
                .benefits(jobSections.benefits())
                .skills(textOrNull(jobPosting, "skills"))
                .salary(textOrNull(baseSalary.path("value"), "value"))
                .postedAt(parseDate(jobPosting.path("datePosted")))
                .validThrough(parseDate(jobPosting.path("validThrough")))
                .requestedLevel(requestedLevel)
                .build();
    }

    private JobSections fetchFullJobSections(String detailUrl) throws IOException {
        String jobId = extractJobId(detailUrl);
        if (jobId == null) {
            return new JobSections(null, null, null);
        }

        String apiUrl = TOPDEV_API_URL + jobId + "?locale=vi_VN";
        Document apiDocument = Jsoup.connect(apiUrl)
                .userAgent(USER_AGENT)
                .header("X-Topdev-Source", "FrontendV4-Server")
                .ignoreContentType(true)
                .timeout(REQUEST_TIMEOUT_MILLIS)
                .get();

        JsonNode response = objectMapper.readTree(apiDocument.body().text());
        JsonNode data = response.path("data");
        if (data.isMissingNode() || data.isNull()) {
            return new JobSections(null, null, null);
        }

        String description = cleanRichText(textOrNull(data, "responsibilities_original"));
        String requirements = cleanRichText(textOrNull(data, "requirements_original"));
        String benefits = extractBenefits(data.path("benefits_original"));
        return new JobSections(description, requirements, benefits);
    }

    private String extractJobId(String detailUrl) {
        if (detailUrl == null || detailUrl.isBlank()) {
            return null;
        }
        Matcher matcher = Pattern.compile("-(\\d+)(?:\\?.*)?$").matcher(detailUrl);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractBenefits(JsonNode benefitsNode) {
        if (benefitsNode == null || benefitsNode.isNull() || benefitsNode.isMissingNode()) {
            return null;
        }
        if (benefitsNode.isArray()) {
            List<String> sections = new ArrayList<>();
            for (JsonNode benefit : benefitsNode) {
                String value = cleanRichText(textOrNull(benefit, "value"));
                if (value != null && !value.isBlank()) {
                    sections.add(value);
                }
            }
            return sections.isEmpty() ? null : String.join("\n\n", sections);
        }
        return cleanHtml(benefitsNode.asText());
    }

    private String cleanRichText(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        appendRichText(Jsoup.parse(html).body(), result);
        return result.toString()
                .replace('\u00a0', ' ')
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n[ \\t]+", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private void appendRichText(Node node, StringBuilder result) {
        if (node instanceof TextNode textNode) {
            result.append(textNode.getWholeText());
            return;
        }

        if (node instanceof Element element) {
            String tagName = element.tagName();
            if ("li".equals(tagName)) {
                result.append("• ");
            }
            if ("br".equals(tagName)) {
                result.append('\n');
                return;
            }
            for (Node child : element.childNodes()) {
                appendRichText(child, result);
            }
            if ("li".equals(tagName) || "p".equals(tagName) || "div".equals(tagName)) {
                result.append('\n');
            }
            return;
        }

        for (Node child : node.childNodes()) {
            appendRichText(child, result);
        }
    }

    private JsonNode findJobPosting(Document document) throws IOException {
        Elements scripts = document.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            String data = script.data();
            if (data == null || data.isBlank()) {
                continue;
            }
            JsonNode node = objectMapper.readTree(data);
            if ("JobPosting".equals(node.path("@type").asText())) {
                return node;
            }
        }
        return null;
    }

    private CompanyPageData crawlCompanyPage(String companyUrl) {
        if (companyUrl == null || companyUrl.isBlank()) {
            return new CompanyPageData(null, null);
        }
        try {
            Document companyDocument = Jsoup.connect(companyUrl)
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT_MILLIS)
                    .get();

            Element overviewTitle = companyDocument.select("span").stream()
                    .filter(element ->
                            "Company Overview".equalsIgnoreCase(element.text().trim()))
                    .findFirst()
                    .orElse(null);

            String description = null;
            if (overviewTitle != null && overviewTitle.parent() != null) {
                Element overviewContent = overviewTitle.parent().selectFirst("div.paragraph");
                if (overviewContent == null) {
                    overviewContent = overviewTitle.parent().selectFirst("div.text-sm");
                }
                if (overviewContent != null && !overviewContent.text().isBlank()) {
                    description = overviewContent.text().trim();
                }
            }
            String logo = companyDocument.select("img[alt*=company-image][class*=object-contain]").stream()
                    .map(image -> image.absUrl("src"))
                    .filter(url -> url != null && url.startsWith("https://salt.topdev.vn/"))
                    .findFirst()
                    .orElse(null);
            return new CompanyPageData(description, logo);
        } catch (Exception ignored) {
            // Nếu company page lỗi thì caller sẽ dùng dữ liệu fallback từ JobPosting metadata.
        }
        return new CompanyPageData(null, null);
    }

    private record CompanyPageData(String description, String logo) {}

    private String extractLocation(JsonNode location) {
        JsonNode item = location.isArray() && !location.isEmpty() ? location.get(0) : location;
        JsonNode address = item.path("address");
        String locality = textOrNull(address, "addressLocality");
        String region = textOrNull(address, "addressRegion");
        if (locality == null) {
            return region;
        }
        return region == null || region.equals(locality) ? locality : locality + ", " + region;
    }

    private String cleanHtml(String value) {
        return value == null ? null : Jsoup.parse(value).text();
    }

    private JobSections extractJobSections(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return new JobSections(null, null, null);
        }

        String text =
                Jsoup.parse(rawHtml.replaceAll("(?i)<br\\s*/?>", "\n")).text().trim();
        Pattern headingPattern = Pattern.compile(
                "(?i)Your\\s+role\\s*&\\s*responsibilities|Your\\s+skills\\s*&\\s*qualifications|Benefits(?:\\s+for\\s+you)?");
        Matcher matcher = headingPattern.matcher(text);
        List<SectionMatch> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(new SectionMatch(matcher.group(), matcher.start(), matcher.end()));
        }

        String description = null;
        String requirements = null;
        String benefits = null;
        for (int i = 0; i < matches.size(); i++) {
            SectionMatch current = matches.get(i);
            int contentEnd = i + 1 < matches.size() ? matches.get(i + 1).start() : text.length();
            String content = text.substring(current.end(), contentEnd).trim();
            String heading = current.heading().toLowerCase();
            if (heading.startsWith("your role")) {
                description = content;
            } else if (heading.startsWith("your skills")) {
                requirements = content;
            } else if (heading.startsWith("benefits")) {
                benefits = content;
            }
        }
        return new JobSections(description, requirements, benefits);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? null : value.asText();
    }

    private LocalDate parseDate(JsonNode node) {
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(node.asText());
        } catch (Exception ignored) {
            return null;
        }
    }

    private record SectionMatch(String heading, int start, int end) {}

    private record JobSections(String description, String requirements, String benefits) {
        private boolean isEmpty() {
            return (description == null || description.isBlank())
                    && (requirements == null || requirements.isBlank())
                    && (benefits == null || benefits.isBlank());
        }
    }
}
