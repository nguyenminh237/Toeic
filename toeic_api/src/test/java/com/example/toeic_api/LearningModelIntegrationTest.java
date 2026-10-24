package com.example.toeic_api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.toeic_api.modules.auth.model.PasswordResetToken;
import com.example.toeic_api.modules.auth.repository.PasswordResetRepository;
import com.example.toeic_api.modules.auth.service.PasswordResetService;
import com.example.toeic_api.modules.auth.service.TokenService;
import com.example.toeic_api.modules.content.dto.request.ContentBundleRequest;
import com.example.toeic_api.modules.content.dto.request.ContentRequests;
import com.example.toeic_api.modules.content.model.GroupDocument;
import com.example.toeic_api.modules.content.service.ContentBundleService;
import com.example.toeic_api.modules.content.service.ContentService;
import com.example.toeic_api.modules.practice.dto.request.ListeningSectionRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingPassageRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingQuestionRequest;
import com.example.toeic_api.modules.practice.dto.request.ReadingSectionRequest;
import com.example.toeic_api.modules.practice.service.PracticeAdminService;
import com.example.toeic_api.modules.practice.service.PracticeService;
import com.example.toeic_api.modules.tests.dto.request.TestAnswerRequest;
import com.example.toeic_api.modules.tests.dto.request.TestPartRequest;
import com.example.toeic_api.modules.tests.dto.request.TestSubmitRequest;
import com.example.toeic_api.modules.tests.dto.request.TestSuiteRequest;
import com.example.toeic_api.modules.tests.dto.response.TestViews;
import com.example.toeic_api.modules.tests.service.TestAdminService;
import com.example.toeic_api.modules.tests.service.TestService;
import com.example.toeic_api.modules.users.model.User;
import com.example.toeic_api.modules.users.model.UserRole;
import com.example.toeic_api.modules.users.repository.UserRepository;
import com.example.toeic_api.modules.users.service.UserAdminService;
import com.example.toeic_api.modules.users.service.UserService;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyCategoryRequest;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularySetRequest;
import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyWordCreateRequest;
import com.example.toeic_api.modules.vocabulary.service.VocabularyService;
import com.example.toeic_api.shared.exception.AppException;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LearningModelIntegrationTest {
    @Autowired ContentService content;
    @Autowired ContentBundleService bundles;
    @Autowired PracticeService practice;
    @Autowired PracticeAdminService practiceAdmin;
    @Autowired TestAdminService testAdmin;
    @Autowired TestService tests;
    @Autowired UserRepository users;
    @Autowired UserService userService;
    @Autowired UserAdminService userAdmin;
    @Autowired PasswordEncoder encoder;
    @Autowired TokenService tokens;
    @Autowired PasswordResetService resets;
    @Autowired PasswordResetRepository resetTokens;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @MockBean Clock clock;
    @MockBean com.example.toeic_api.modules.ai.service.GroqExplanationService groq;
    private User user;
    private Instant time;

    @BeforeEach
    void setup() {
        time = Instant.now();
        when(clock.instant()).thenAnswer(i -> time);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        user = createUser(UserRole.USER);
        as(user);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private User createUser(UserRole role) {
        String id = UUID.randomUUID().toString();
        return users.saveAndFlush(
                User.builder()
                        .username("u" + id)
                        .email(id + "@example.com")
                        .password(encoder.encode("password123"))
                        .role(role)
                        .enabled(true)
                        .build());
    }

    private void as(User u) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                u.getUsername(),
                                "",
                                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))));
    }

    private Long group(int part, List<GroupDocument> documents) {
        return content.saveGroup(
                        null,
                        new ContentRequests.Group(
                                part,
                                "Context",
                                "audio.mp3",
                                null,
                                null,
                                null,
                                "Transcript",
                                documents))
                .getId();
    }

    private Long question(Long group, int position, String correct) {
        int part = content.group(group).getPart().getId();
        var opts = new ArrayList<ContentRequests.Option>();
        for (String label : List.of("A", "B", "C", "D").subList(0, part == 2 ? 3 : 4))
            opts.add(new ContentRequests.Option(label, "Choice " + label, label.equals(correct)));
        return content.saveQuestion(
                        null,
                        new ContentRequests.Question(
                                group,
                                position,
                                "Original question",
                                null,
                                part == 2,
                                "Original explanation",
                                opts))
                .getId();
    }

    private Long testPart(int part) {
        var suite =
                testAdmin.createSuite(
                        TestSuiteRequest.builder()
                                .suiteName("Suite " + UUID.randomUUID())
                                .source("Test")
                                .build());
        return testAdmin
                .createTestPart(
                        TestPartRequest.builder()
                                .suiteId(suite.getId())
                                .partNumber(part)
                                .partType(part <= 4 ? "listening" : "reading")
                                .timeLimit(5)
                                .questionCount(999)
                                .build())
                .getId();
    }

    private TestAnswerRequest answer(Long q, String choice) {
        return TestAnswerRequest.builder().questionId(q).selectedAnswer(choice).build();
    }

    @Test
    void partTwoHasThreeChoicesAndRejectsFourth() {
        Long q = question(group(2, List.of()), 1, "A");
        assertEquals(3, practice.question(q, true).getOptions().size());
        assertThrows(AppException.class, () -> practice.answer(q, "D", true));
        assertNull(practice.question(q, true).getCorrectAnswer());
    }

    @Test
    void legacyPassageAuthoringAndAdminGroupReadsRemainUsable() throws Exception {
        var set =
                practiceAdmin.createReadingSection(
                        ReadingSectionRequest.builder().partId(7L).name("Legacy adapter").build());
        var request =
                ReadingPassageRequest.builder()
                        .sectionId(set.id())
                        .passageTitle("Email")
                        .passageContent("Original body")
                        .passageOrder(1)
                        .build();
        var passage = practiceAdmin.createPassage(request);
        assertTrue(practice.passages(set.id()).stream().anyMatch(p -> p.id().equals(passage.id())));
        var q =
                practiceAdmin.createReadingQuestion(
                        ReadingQuestionRequest.builder()
                                .sectionId(set.id())
                                .passageId(passage.id())
                                .questionNumber(1)
                                .questionText("What happened?")
                                .correctAnswer("A")
                                .optionA("First")
                                .optionB("Second")
                                .optionC("Third")
                                .optionD("Fourth")
                                .build());
        assertEquals(passage.id(), q.getPassageId());
        content.saveGroup(
                passage.id(),
                new ContentRequests.Group(
                        7,
                        "Multiple documents",
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(
                                new GroupDocument("Email", "Body one", null),
                                new GroupDocument("Notice", "Body two", null))));
        assertThrows(AppException.class, () -> practiceAdmin.updatePassage(passage.id(), request));
        assertEquals(2, content.group(passage.id()).getDocuments().size());
        var admin = user;
        String token = tokens.issue(admin);
        mvc.perform(
                        get("/api/v1/workspace/content/groups")
                                .param("partNumber", "7")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].documents.length()").value(2));
    }

    @Test
    void partSixBundleSavesOnePassageFourQuestionsAndMembershipTogether() throws Exception {
        var admin = createUser(UserRole.ADMIN);
        as(admin);
        var set =
                practiceAdmin.createReadingSection(
                        ReadingSectionRequest.builder().partId(6L).name("Text completion").build());
        var request = bundleRequest(new ContentBundleRequest.Destination("sets", set.id(), 11));
        mvc.perform(
                        post("/api/v1/admin/content/bundles")
                                .header("Authorization", "Bearer " + tokens.issue(admin))
                                .contentType("application/json")
                                .content(
                                        new com.fasterxml.jackson.databind.ObjectMapper()
                                                .writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.questions.length()").value(4))
                .andExpect(jsonPath("$.result.questions[3].questionNumber").value(4));
        assertEquals(
                4,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM practice_set_items WHERE set_id=?",
                        Integer.class,
                        set.id()));
        var groupId =
                jdbc.queryForObject(
                        "SELECT q.group_id FROM practice_set_items i JOIN questions q ON"
                                + " q.id=i.question_id WHERE i.set_id=? LIMIT 1",
                        Long.class,
                        set.id());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM group_documents WHERE group_id=?",
                        Integer.class,
                        groupId));
        as(admin);
        var original = content.questions(groupId);
        var update =
                new ContentBundleRequest(
                        request.group(),
                        content.group(groupId).getVersion(),
                        original.stream()
                                .map(
                                        q ->
                                                new com.example.toeic_api.modules.content.dto
                                                        .request.ContentBundleRequest.Item(
                                                        q.getId(),
                                                        q.getVersion(),
                                                        q.getPosition(),
                                                        "Edited " + q.getPosition(),
                                                        null,
                                                        false,
                                                        null,
                                                        request.questions().getFirst().options()))
                                .toList(),
                        null);
        var saved = bundles.save(groupId, update);
        assertEquals(
                original.stream().map(q -> q.getId()).toList(),
                saved.questions().stream().map(q -> q.getId()).toList());
    }

    @Test
    void batchKeepsIndependentPassagesAndReusesExistingMembershipPositions() throws Exception {
        var section =
                practiceAdmin.createReadingSection(
                        ReadingSectionRequest.builder()
                                .partId(6L)
                                .name("Multiple contexts")
                                .build());
        var destination = new ContentBundleRequest.Destination("sets", section.id(), null);
        var first = bundleRequest(destination);
        var second =
                new ContentBundleRequest(
                        new ContentRequests.Group(
                                6,
                                "Different email",
                                null,
                                null,
                                null,
                                null,
                                null,
                                List.of(
                                        new GroupDocument(
                                                "Second",
                                                "A different passage with four blanks.",
                                                null))),
                        null,
                        first.questions(),
                        destination);
        var batch =
                new com.example.toeic_api.modules.content.dto.request.ContentBatchRequest(
                        List.of(
                                new com.example.toeic_api.modules.content.dto.request
                                        .ContentBatchRequest.Entry(null, first),
                                new com.example.toeic_api.modules.content.dto.request
                                        .ContentBatchRequest.Entry(null, second)));
        var result = bundles.saveBatch(batch);
        assertNotEquals(result.get(0).id(), result.get(1).id());
        assertEquals(
                "Read (1) ____ then (2) ____ and (3) ____ before (4) ____.",
                content.group(result.get(0).id()).getDocuments().getFirst().getContent());
        assertEquals(
                "A different passage with four blanks.",
                content.group(result.get(1).id()).getDocuments().getFirst().getContent());
        var updates =
                result.stream()
                        .map(
                                r ->
                                        new com.example.toeic_api.modules.content.dto.request
                                                .ContentBatchRequest.Entry(
                                                r.id(),
                                                new ContentBundleRequest(
                                                        r.id().equals(result.get(0).id())
                                                                ? first.group()
                                                                : second.group(),
                                                        r.version(),
                                                        r.questions().stream()
                                                                .map(
                                                                        q ->
                                                                                new ContentBundleRequest
                                                                                        .Item(
                                                                                        q.getId(),
                                                                                        q
                                                                                                .getVersion(),
                                                                                        q
                                                                                                .getQuestionNumber(),
                                                                                        q
                                                                                                .getQuestionText(),
                                                                                        null,
                                                                                        false,
                                                                                        null,
                                                                                        first.questions()
                                                                                                .getFirst()
                                                                                                .options()))
                                                                .toList(),
                                                        destination)))
                        .toList();
        bundles.saveBatch(
                new com.example.toeic_api.modules.content.dto.request.ContentBatchRequest(updates));
        assertEquals(
                8,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM practice_set_items WHERE set_id=?",
                        Integer.class,
                        section.id()));
        assertEquals(
                8,
                jdbc.queryForObject(
                        "SELECT MAX(position) FROM practice_set_items WHERE set_id=?",
                        Integer.class,
                        section.id()));
    }

    @Test
    @Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void failingSecondContextRollsBackWholeBatch() {
        int before = jdbc.queryForObject("SELECT COUNT(*) FROM question_groups", Integer.class);
        var valid = bundleRequest(null);
        var invalid =
                new ContentBundleRequest(
                        valid.group(), null, valid.questions().subList(0, 3), null);
        var batch =
                new com.example.toeic_api.modules.content.dto.request.ContentBatchRequest(
                        List.of(
                                new com.example.toeic_api.modules.content.dto.request
                                        .ContentBatchRequest.Entry(null, valid),
                                new com.example.toeic_api.modules.content.dto.request
                                        .ContentBatchRequest.Entry(null, invalid)));
        assertThrows(AppException.class, () -> bundles.saveBatch(batch));
        assertEquals(
                before, jdbc.queryForObject("SELECT COUNT(*) FROM question_groups", Integer.class));
    }

    @Test
    void bundlesCanAppendToExistingPracticeSectionWithoutChoosingAStartNumber() {
        var set =
                practiceAdmin.createReadingSection(
                        ReadingSectionRequest.builder().partId(6L).name("Append bundles").build());
        var destination = new ContentBundleRequest.Destination("sets", set.id(), null);
        bundles.save(null, bundleRequest(destination));
        bundles.save(null, bundleRequest(destination));
        assertEquals(
                8,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM practice_set_items WHERE set_id=?",
                        Integer.class,
                        set.id()));
        assertEquals(
                8,
                jdbc.queryForObject(
                        "SELECT MAX(position) FROM practice_set_items WHERE set_id=?",
                        Integer.class,
                        set.id()));
    }

    private ContentBundleRequest bundleRequest(ContentBundleRequest.Destination destination) {
        var group =
                new ContentRequests.Group(
                        6,
                        "Email",
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(
                                new GroupDocument(
                                        "Email",
                                        "Read (1) ____ then (2) ____ and (3) ____ before (4) ____.",
                                        null)));
        var rows =
                java.util.stream.IntStream.rangeClosed(1, 4)
                        .mapToObj(
                                n ->
                                        new com.example.toeic_api.modules.content.dto.request
                                                .ContentBundleRequest.Item(
                                                null,
                                                null,
                                                n,
                                                "Blank " + n,
                                                null,
                                                false,
                                                null,
                                                List.of(
                                                        new ContentRequests.Option(
                                                                "A", "one", true),
                                                        new ContentRequests.Option(
                                                                "B", "two", false),
                                                        new ContentRequests.Option(
                                                                "C", "three", false),
                                                        new ContentRequests.Option(
                                                                "D", "four", false))))
                        .toList();
        return new ContentBundleRequest(group, null, rows, destination);
    }

    @Test
    @Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void invalidBundleDestinationRollsBackGroupAndEveryQuestion() {
        int groups = jdbc.queryForObject("SELECT COUNT(*) FROM question_groups", Integer.class);
        int questions = jdbc.queryForObject("SELECT COUNT(*) FROM questions", Integer.class);
        assertThrows(
                AppException.class,
                () ->
                        bundles.save(
                                null,
                                bundleRequest(
                                        new com.example.toeic_api.modules.content.dto.request
                                                .ContentBundleRequest.Destination(
                                                "sets", -999L, 1))));
        assertEquals(
                groups, jdbc.queryForObject("SELECT COUNT(*) FROM question_groups", Integer.class));
        assertEquals(
                questions, jdbc.queryForObject("SELECT COUNT(*) FROM questions", Integer.class));
    }

    @Test
    void partSixBundleRejectsThreeQuestionsAndCrossGroupIds() {
        var request = bundleRequest(null);
        assertThrows(
                AppException.class,
                () ->
                        bundles.save(
                                null,
                                new com.example.toeic_api.modules.content.dto.request
                                        .ContentBundleRequest(
                                        request.group(),
                                        null,
                                        request.questions().subList(0, 3),
                                        null)));
        var saved = bundles.save(null, request);
        assertThrows(
                AppException.class,
                () ->
                        bundles.save(
                                null,
                                new com.example.toeic_api.modules.content.dto.request
                                        .ContentBundleRequest(
                                        request.group(),
                                        null,
                                        saved.questions().stream()
                                                .map(
                                                        q ->
                                                                new com.example.toeic_api.modules
                                                                        .content.dto.request
                                                                        .ContentBundleRequest.Item(
                                                                        q.getId(),
                                                                        q.getVersion(),
                                                                        q.getQuestionNumber(),
                                                                        q.getQuestionText(),
                                                                        null,
                                                                        false,
                                                                        null,
                                                                        request.questions()
                                                                                .getFirst()
                                                                                .options()))
                                                .toList(),
                                        null)));
    }

    @Test
    void openApiDiscoversControllersAcrossBusinessModules() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/users/me']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/content/groups']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reading/sections/{partNumber}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/tests/start/{partId}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/vocabulary/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/dashboard']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/uploads']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/content/explanations']").exists());
    }

    @Test
    void practiceSectionsExposeNameAndDescriptionWithoutFiveLevelLimit() throws Exception {
        var admin = createUser(UserRole.ADMIN);
        as(admin);
        for (int n = 0; n < 7; n++) {
            practiceAdmin.createReadingSection(
                    ReadingSectionRequest.builder()
                            .partId(5L)
                            .name("  Grammar " + n + "  ")
                            .description("Practice grammar " + n)
                            .build());
        }
        String token = tokens.issue(admin);
        mvc.perform(get("/api/v1/reading/sections/5").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(7))
                .andExpect(jsonPath("$.result[0].name").value("Grammar 0"))
                .andExpect(jsonPath("$.result[0].description").value("Practice grammar 0"))
                .andExpect(jsonPath("$.result[0].levelNumber").doesNotExist());
        mvc.perform(
                        post("/api/v1/admin/reading/sections")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(
                                        "{\"partId\":5,\"name\":\"\",\"description\":\"Description\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void historyApiIsPaginatedAndOmitsSnapshotBodies() throws Exception {
        Long q = question(group(5, List.of()), 1, "A");
        Long part = testPart(5);
        testAdmin.attach(part, q, 1);
        tests.startTest(null, part);
        mvc.perform(
                        get("/api/v1/tests/results")
                                .param("page", "0")
                                .param("size", "1")
                                .header("Authorization", "Bearer " + tokens.issue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.items[0].totalCount").value(1))
                .andExpect(jsonPath("$.result.items[0].questions.length()").value(0));
    }

    @Test
    void learnerCannotGenerateAdminExplanations() throws Exception {
        mvc.perform(
                        post("/api/v1/admin/content/explanations")
                                .header("Authorization", "Bearer " + tokens.issue(user))
                                .contentType("application/json")
                                .content(
                                        "{\"partNumber\":5,\"questionText\":\"Choose\",\"correctAnswer\":\"A\",\"options\":{\"A\":\"one\",\"B\":\"two\",\"C\":\"three\",\"D\":\"four\"}}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(groq);
    }

    @Test
    void adminCanGenerateDraftButMissingReadingContextIsRejected() throws Exception {
        var admin = createUser(UserRole.ADMIN);
        String token = tokens.issue(admin);
        when(groq.generate(any())).thenReturn("Teaching explanation");
        String request =
                "{\"partNumber\":5,\"questionText\":\"Choose\",\"correctAnswer\":\"A\",\"options\":{\"A\":\"one\",\"B\":\"two\",\"C\":\"three\",\"D\":\"four\"}}";
        mvc.perform(
                        post("/api/v1/admin/content/explanations")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.explanation").value("Teaching explanation"));
        mvc.perform(
                        post("/api/v1/admin/content/explanations")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(request.replace("\"partNumber\":5", "\"partNumber\":7")))
                .andExpect(status().isBadRequest());
        verify(groq, times(1)).generate(any());
    }

    @Test
    void editingQuestionInvalidatesCurrentProgressButKeepsHistoricalOutcome() {
        Long g = group(1, List.of());
        Long q = question(g, 1, "A");
        var set =
                practiceAdmin.createListeningSection(
                        ListeningSectionRequest.builder().partId(1L).name("Versioned").build());
        practiceAdmin.attach(set.id(), q, 1);
        practice.answer(q, "A", true);
        practice.status(q, null, true, true);
        content.saveQuestion(
                q,
                new ContentRequests.Question(
                        g,
                        1,
                        "Changed",
                        null,
                        false,
                        "New explanation",
                        List.of(
                                new ContentRequests.Option("A", "First", false),
                                new ContentRequests.Option("B", "Second", true),
                                new ContentRequests.Option("C", "Third", false),
                                new ContentRequests.Option("D", "Fourth", false))));
        var current = practice.progress(true).getFirst();
        assertEquals("UNANSWERED", current.status());
        assertTrue(current.bookmarked());
        assertEquals(0, practice.summaries(1, true).getFirst().correct());
        assertTrue(
                jdbc.queryForObject(
                        "SELECT correct FROM practice_answer_events WHERE user_id=?",
                        Boolean.class,
                        user.getId()));
        assertEquals("CORRECT", practice.answer(q, "B", true).status());
    }

    @Test
    void bookmarkAndResetDoNotEraseAnswerHistoryOrChangeAttemptTime() {
        Long q = question(group(1, List.of()), 1, "A");
        var first = practice.answer(q, "A", true);
        var mark = practice.status(q, null, true, true);
        assertEquals("CORRECT", mark.status());
        assertTrue(mark.bookmarked());
        assertEquals(first.lastAttempted(), mark.lastAttempted());
        practice.status(q, "UNANSWERED", null, true);
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM practice_answer_events WHERE user_id=?",
                        Integer.class,
                        user.getId()));
        assertEquals(1, practice.progress(true).getFirst().attemptCount());
        assertNull(practice.progress(true).getFirst().correctAnswer());
    }

    @Test
    void summaryCountsActualAnswersAndDatabaseRejectsDuplicateProgress() {
        Long q = question(group(1, List.of()), 1, "A");
        var set =
                practiceAdmin.createListeningSection(
                        ListeningSectionRequest.builder().partId(1L).name("Section").build());
        practiceAdmin.attach(set.id(), q, 1);
        practice.status(q, null, true, true);
        assertEquals(1, practice.summaries(1, true).getFirst().unanswered());
        practice.answer(q, "A", true);
        assertEquals(1, practice.summaries(1, true).getFirst().correct());
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "INSERT INTO"
                                    + " user_question_progress(user_id,question_id,bookmarked,attempt_count)"
                                    + " VALUES(?,?,FALSE,0)",
                                user.getId(),
                                q));
    }

    @Test
    void oneQuestionIsReusableAcrossPracticeAndTestWithoutCopyingContent() {
        Long q = question(group(5, List.of()), 1, "A");
        var set =
                practiceAdmin.createReadingSection(
                        ReadingSectionRequest.builder().partId(5L).name("Section").build());
        practiceAdmin.attach(set.id(), q, 1);
        Long part = testPart(5);
        testAdmin.attach(part, q, 1);
        assertEquals(q, practice.questions(set.id(), false, false).getFirst().getId());
        assertEquals(q, tests.getQuestionsByPart(part).getFirst().getId());
    }

    @Test
    void attemptResumesAndGradingUsesSnapshotAfterContentEditOrArchive() {
        Long q = question(group(5, List.of()), 1, "A");
        Long part = testPart(5);
        testAdmin.attach(part, q, 1);
        var a = tests.startTest(null, part);
        assertEquals(a.id(), tests.startTest(null, part).id());
        assertNull(a.questions().getFirst().getCorrectAnswer());
        tests.draft(a.id(), List.of(answer(q, "A")));
        assertEquals("A", tests.startTest(null, part).selectedAnswers().get(q));
        var live = content.question(q);
        live.setQuestionText("Edited");
        live.getOptions().forEach(o -> o.setCorrect(o.getLabel().equals("B")));
        content.archiveQuestion(q);
        testAdmin.deleteTestPart(part);
        var result =
                tests.submitTest(
                        null,
                        TestSubmitRequest.builder()
                                .resultId(a.id())
                                .answers(List.of(answer(q, "A")))
                                .build());
        assertEquals(100, result.accuracyPercent());
        assertEquals("Original question", result.answers().getFirst().questionText());
        assertEquals("A", result.answers().getFirst().correctAnswer());
        var retry =
                tests.submitTest(
                        null,
                        TestSubmitRequest.builder()
                                .resultId(a.id())
                                .answers(List.of(answer(q, "B")))
                                .build());
        assertEquals(100, retry.accuracyPercent());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM test_attempt_items WHERE attempt_id=?",
                        Integer.class,
                        a.id()));
    }

    @Test
    void expiredAttemptGradesOnlyPreviouslySavedAnswers() {
        Long q = question(group(5, List.of()), 1, "A");
        Long part = testPart(5);
        testAdmin.attach(part, q, 1);
        var a = tests.startTest(null, part);
        tests.draft(a.id(), List.of(answer(q, "B")));
        time = time.plusSeconds(301);
        var result =
                tests.submitTest(
                        null,
                        TestSubmitRequest.builder()
                                .resultId(a.id())
                                .answers(List.of(answer(q, "A")))
                                .build());
        assertEquals(0, result.accuracyPercent());
        assertEquals("B", result.answers().getFirst().selectedAnswer());
        assertEquals(300, result.timeSpent());
    }

    @Test
    void ownershipAndForeignAnswersAreEnforced() {
        Long q = question(group(5, List.of()), 1, "A");
        Long part = testPart(5);
        testAdmin.attach(part, q, 1);
        var a = tests.startTest(null, part);
        assertThrows(AppException.class, () -> tests.draft(a.id(), List.of(answer(999999L, "A"))));
        User other = createUser(UserRole.USER);
        as(other);
        assertThrows(AppException.class, () -> tests.getTestResult(a.id()));
        assertThrows(
                AppException.class,
                () -> tests.submitTest(null, TestSubmitRequest.builder().resultId(a.id()).build()));
        assertThrows(AppException.class, () -> userService.getUserById(user.getId()));
    }

    @Test
    void completeMultiDocumentContextIsPreservedAndPartialListeningGroupRejected() {
        Long g =
                group(
                        7,
                        List.of(
                                new GroupDocument("Email", "First text", null),
                                new GroupDocument("Notice", "Second text", null)));
        Long q = question(g, 1, "A");
        Long part = testPart(7);
        testAdmin.attach(part, q, 1);
        assertEquals(
                2,
                tests.startTest(null, part).questions().getFirst().getGroup().documents().size());
        Long listeningGroup = group(3, List.of());
        Long first = question(listeningGroup, 1, "A");
        question(listeningGroup, 2, "B");
        question(listeningGroup, 3, "C");
        Long p = testPart(3);
        testAdmin.attach(p, first, 1);
        assertThrows(AppException.class, () -> tests.startTest(null, p));
    }

    @Test
    void apiRejectsRevokedTokensAndChangedAccountPermissions() throws Exception {
        String token = tokens.issue(user);
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        tokens.revoke(tokens.verify(token, true));
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        as(user);
        String fresh = tokens.issue(user);
        userAdmin.setUserEnabled(user.getId(), false);
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + fresh))
                .andExpect(status().isUnauthorized());
        assertThrows(AppException.class, () -> tokens.verify(fresh, true));
    }

    @Test
    void learnerCannotWriteIntoUnknownVocabularySetOrGetExamSolutions() throws Exception {
        Long q = question(group(5, List.of()), 1, "A");
        Long p = testPart(5);
        testAdmin.attach(p, q, 1);
        String token = tokens.issue(user);
        mvc.perform(
                        get("/api/v1/tests/parts/" + p + "/questions")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.result[0].explanation").doesNotExist());
        mvc.perform(
                        post("/api/v1/vocabulary/words")
                                .contentType("application/json")
                                .content(
                                        "{\"setId\":1,\"word\":\"test\",\"meaning\":\"test\",\"partOfSpeech\":\"noun\"}")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(
                        post("/api/v1/auth/reset-password/" + user.getId())
                                .contentType("application/json")
                                .content("{\"newPassword\":\"hacked123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetTokenIsHashedSingleUseAndRevokesExistingSessions() {
        String raw = "random-single-use-secret";
        PasswordResetToken reset = new PasswordResetToken();
        reset.setTokenHash(PasswordResetService.hash(raw));
        reset.setUser(user);
        reset.setAuthVersion(user.getAuthVersion());
        reset.setExpiresAt(LocalDateTime.now(clock).plusMinutes(20));
        resetTokens.saveAndFlush(reset);
        String old = tokens.issue(user);
        resets.reset(raw, "changed123");
        assertTrue(
                encoder.matches(
                        "changed123", users.findById(user.getId()).orElseThrow().getPassword()));
        assertThrows(AppException.class, () -> tokens.verify(old, false));
        assertThrows(AppException.class, () -> resets.reset(raw, "again123"));
        assertNotEquals(raw, reset.getTokenHash());
    }

    @Test
    void questionCannotBeAttachedAcrossPartsAndOptionsRequireExactlyOneCorrect() {
        Long q = question(group(5, List.of()), 1, "A");
        Long part = testPart(1);
        assertThrows(AppException.class, () -> testAdmin.attach(part, q, 1));
        assertThrows(
                AppException.class,
                () ->
                        content.saveQuestion(
                                null,
                                new ContentRequests.Question(
                                        group(2, List.of()),
                                        1,
                                        "Bad",
                                        null,
                                        true,
                                        null,
                                        List.of(
                                                new ContentRequests.Option("A", "a", true),
                                                new ContentRequests.Option("B", "b", true),
                                                new ContentRequests.Option("C", "c", false)))));
    }

    @Test
    @Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void concurrentRequestsCreateOneAttemptOneProgressAndOneSubmission() throws Exception {
        Long q = question(group(5, List.of()), 1, "A");
        Long part = testPart(5);
        testAdmin.attach(part, q, 1);
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var gate = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<TestViews.Attempt> start =
                    () -> {
                        as(user);
                        try {
                            gate.await();
                            return tests.startTest(null, part);
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    };
            var one = pool.submit(start);
            var two = pool.submit(start);
            gate.countDown();
            var attempt = one.get(15, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(attempt.id(), two.get(15, java.util.concurrent.TimeUnit.SECONDS).id());
            var answerGate = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Void> answer =
                    () -> {
                        as(user);
                        try {
                            answerGate.await();
                            practice.answer(q, "A", false);
                            return null;
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    };
            var a = pool.submit(answer);
            var b = pool.submit(answer);
            answerGate.countDown();
            a.get(15, java.util.concurrent.TimeUnit.SECONDS);
            b.get(15, java.util.concurrent.TimeUnit.SECONDS);
            as(user);
            assertEquals(2, practice.progress(false).getFirst().attemptCount());
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM user_question_progress WHERE user_id=?",
                            Integer.class,
                            user.getId()));
            assertEquals(
                    2,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM practice_answer_events WHERE user_id=?",
                            Integer.class,
                            user.getId()));
            var submitGate = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<TestViews.Attempt> submit =
                    () -> {
                        as(user);
                        try {
                            submitGate.await();
                            return tests.submitTest(
                                    null,
                                    TestSubmitRequest.builder()
                                            .resultId(attempt.id())
                                            .answers(List.of(answer(q, "A")))
                                            .build());
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    };
            var first = pool.submit(submit);
            var second = pool.submit(submit);
            submitGate.countDown();
            assertEquals(
                    first.get(15, java.util.concurrent.TimeUnit.SECONDS).score(),
                    second.get(15, java.util.concurrent.TimeUnit.SECONDS).score());
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM test_attempt_items WHERE attempt_id=?",
                            Integer.class,
                            attempt.id()));
        }
    }

    @Autowired
    com.example.toeic_api.modules.vocabulary.service.VocabularyAdminService vocabularyAdmin;

    @Autowired VocabularyService vocabulary;

    @Autowired
    com.example.toeic_api.modules.vocabulary.service.VocabularyImportService vocabularyImports;

    private Long vocabularySet() {
        var category =
                vocabularyAdmin.createCategory(
                        VocabularyCategoryRequest.builder()
                                .categoryName("Import " + UUID.randomUUID())
                                .build());
        return vocabularyAdmin
                .createSet(
                        VocabularySetRequest.builder()
                                .categoryId(category.getId())
                                .setName("Import set")
                                .build())
                .getId();
    }

    @Test
    void
            reviewedVocabularyPersistsSeparateSensesOptionalRelationsAndFamilyAndUpdatesWithoutDuplicatePositions() {
        Long setId = vocabularySet();
        var common =
                new com.example.toeic_api.modules.vocabulary.dto.request.VocabularyLearningContent
                        .Sense(
                        "tính phí",
                        "verb",
                        450,
                        "The hotel charges a fee.",
                        "Khách sạn thu phí.",
                        null,
                        List.of(),
                        List.of(),
                        List.of("charge a fee"));
        var advanced =
                new com.example.toeic_api.modules.vocabulary.dto.request.VocabularyLearningContent
                        .Sense("trách nhiệm", "noun", 850, null, null, null, null, null, null);
        var request =
                VocabularyWordCreateRequest.builder()
                        .setId(setId)
                        .word("charge")
                        .meaning("tính phí")
                        .partOfSpeech("verb")
                        .senses(List.of(common, advanced))
                        .family(
                                List.of(
                                        new com.example.toeic_api.modules.vocabulary.dto.request
                                                .VocabularyLearningContent.FamilyMember(
                                                "chargeable", "adjective", "có tính phí")))
                        .build();
        var saved =
                vocabularyImports
                        .confirm(
                                new com.example.toeic_api.modules.vocabulary.dto.request
                                        .VocabularyImportRequests.Confirm(
                                        setId, true, List.of(request)))
                        .getFirst();
        assertEquals(2, saved.getSenses().size());
        assertEquals("chargeable", saved.getFamily().getFirst().word());
        assertTrue(saved.getSenses().getFirst().antonyms().isEmpty());
        assertEquals(850, saved.getSenses().get(1).minimumTargetScore());
        assertEquals(
                2,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vocabulary_senses WHERE word_id=?",
                        Integer.class,
                        saved.getId()));
        request.setSenses(List.of(common));
        request.setFamily(List.of());
        var updated = vocabularyAdmin.updateWord(saved.getId(), request);
        assertEquals(1, updated.getSenses().size());
        assertTrue(updated.getFamily().isEmpty());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vocabulary_senses WHERE word_id=?",
                        Integer.class,
                        saved.getId()));
        assertThrows(
                AppException.class,
                () ->
                        vocabularyImports.confirm(
                                new com.example.toeic_api.modules.vocabulary.dto.request
                                        .VocabularyImportRequests.Confirm(
                                        setId, true, List.of(request))));
    }

    @Test
    @Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void reviewedVocabularyBatchRollsBackIfALaterWordCannotBeStored() {
        Long setId = vocabularySet();
        var first =
                VocabularyWordCreateRequest.builder()
                        .setId(setId)
                        .word("invoice")
                        .meaning("hóa đơn")
                        .partOfSpeech("noun")
                        .build();
        var second =
                VocabularyWordCreateRequest.builder()
                        .setId(setId)
                        .word("deadline")
                        .meaning("hạn chót")
                        .partOfSpeech("x".repeat(300))
                        .build();
        assertThrows(
                RuntimeException.class,
                () ->
                        vocabularyImports.confirm(
                                new com.example.toeic_api.modules.vocabulary.dto.request
                                        .VocabularyImportRequests.Confirm(
                                        setId, true, List.of(first, second))));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vocabulary_word_sets WHERE set_id=?",
                        Integer.class,
                        setId));
    }

    @Test
    void vocabularyImportRequiresAdminAndExplicitReview() throws Exception {
        String token = tokens.issue(user);
        mvc.perform(
                        post("/api/v1/admin/vocabulary/import/generate")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content("{\"targetScore\":650,\"terms\":[\"invoice\"]}"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/v1/admin/vocabulary/import/confirm")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(
                                        "{\"setId\":1,\"reviewed\":true,\"words\":[{\"setId\":1,\"word\":\"invoice\",\"meaning\":\"hóa"
                                            + " đơn\",\"partOfSpeech\":\"noun\"}]}"))
                .andExpect(status().isForbidden());
        as(user);
        var request =
                VocabularyWordCreateRequest.builder()
                        .setId(vocabularySet())
                        .word("invoice")
                        .meaning("hóa đơn")
                        .partOfSpeech("noun")
                        .build();
        assertThrows(
                AppException.class,
                () ->
                        vocabularyImports.confirm(
                                new com.example.toeic_api.modules.vocabulary.dto.request
                                        .VocabularyImportRequests.Confirm(
                                        request.getSetId(), false, List.of(request))));
        assertTrue(vocabulary.getWordsBySet(request.getSetId()).isEmpty());
    }

    @Test
    void vocabularyIsReusableAndUpdatingSynonymsKeepsTheAggregateConsistent() {
        var category =
                vocabularyAdmin.createCategory(
                        VocabularyCategoryRequest.builder()
                                .categoryName("Category " + UUID.randomUUID())
                                .build());
        var first =
                vocabularyAdmin.createSet(
                        VocabularySetRequest.builder()
                                .categoryId(category.getId())
                                .setName("First")
                                .build());
        var second =
                vocabularyAdmin.createSet(
                        VocabularySetRequest.builder()
                                .categoryId(category.getId())
                                .setName("Second")
                                .build());
        var request =
                VocabularyWordCreateRequest.builder()
                        .setId(first.getId())
                        .word("agreement")
                        .meaning("thỏa thuận")
                        .partOfSpeech("noun")
                        .synonyms(Set.of("contract"))
                        .build();
        var word = vocabulary.createWord(request);
        assertEquals(Set.of("contract"), word.getSynonyms());
        vocabularyAdmin.attachWord(second.getId(), word.getId());
        assertEquals(word.getId(), vocabulary.getWordsBySet(second.getId()).getFirst().getId());
        request.setSynonyms(Set.of("arrangement"));
        assertEquals(
                Set.of("arrangement"),
                vocabularyAdmin.updateWord(word.getId(), request).getSynonyms());
        vocabularyAdmin.deleteSet(first.getId());
        assertEquals(word.getId(), vocabulary.getWordById(word.getId()).getId());
        assertEquals(1, vocabulary.getWordsBySet(second.getId()).size());
    }

    private String json(Object value) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
    }

    private Long workspaceCreate(User actor, String path, String body) throws Exception {
        var response =
                mvc.perform(
                                post("/api/v1/workspace/" + path)
                                        .header("Authorization", "Bearer " + tokens.issue(actor))
                                        .contentType("application/json")
                                        .content(body))
                        .andExpect(status().isOk())
                        .andReturn();
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response.getResponse().getContentAsString())
                .path("result")
                .path("id")
                .asLong();
    }

    @Test
    void userCanCreatePrivatePracticeBundlesForReadingAndListening() throws Exception {
        var reading =
                workspaceCreate(
                        user, "reading/sections", "{\"partId\":6,\"name\":\"My documents\"}");
        var listening =
                workspaceCreate(user, "listening/sections", "{\"partId\":2,\"name\":\"My audio\"}");
        mvc.perform(
                        post("/api/v1/workspace/content/bundles")
                                .header("Authorization", "Bearer " + tokens.issue(user))
                                .contentType("application/json")
                                .content(
                                        json(
                                                bundleRequest(
                                                        new ContentBundleRequest.Destination(
                                                                "sets", reading, 1)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.questions.length()").value(4));
        as(user);
        Long audioGroup = group(2, List.of());
        Long audioQuestion = question(audioGroup, 1, "B");
        mvc.perform(
                        put("/api/v1/workspace/content/sets/" + listening + "/items")
                                .header("Authorization", "Bearer " + tokens.issue(user))
                                .contentType("application/json")
                                .content("{\"questionId\":" + audioQuestion + ",\"position\":1}"))
                .andExpect(status().isOk());
        for (String kind : List.of("reading", "listening")) {
            Long set = kind.equals("reading") ? reading : listening;
            mvc.perform(
                            get("/api/v1/" + kind + "/questions/section/" + set)
                                    .header("Authorization", "Bearer " + tokens.issue(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(kind.equals("reading") ? 4 : 1));
        }
        assertEquals(
                user.getId(),
                jdbc.queryForObject(
                        "SELECT owner_id FROM practice_sets WHERE id=?", String.class, reading));
        assertEquals(
                user.getId(),
                jdbc.queryForObject(
                        "SELECT owner_id FROM question_groups WHERE id=?",
                        String.class,
                        audioGroup));
        verifyNoInteractions(groq);
    }

    @Test
    void privatePracticeRejectsOtherUsersAndAdminAcrossListsReadsWritesAndMemberships()
            throws Exception {
        Long group = group(5, List.of());
        Long question = question(group, 1, "A");
        var section =
                practiceAdmin.createReadingSection(
                        ReadingSectionRequest.builder().partId(5L).name("Private section").build());
        practiceAdmin.attach(section.id(), question, 1);
        for (var stranger : List.of(createUser(UserRole.USER), createUser(UserRole.ADMIN))) {
            String token = tokens.issue(stranger);
            for (String path :
                    List.of(
                            "/reading/sections/5",
                            "/reading/parts/5/summary",
                            "/workspace/content/groups?partNumber=5"))
                mvc.perform(get("/api/v1" + path).header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.result.length()").value(0));
            for (String path :
                    List.of(
                            "/reading/questions/" + question,
                            "/reading/questions/section/" + section.id(),
                            "/workspace/content/groups/" + group + "/questions"))
                mvc.perform(get("/api/v1" + path).header("Authorization", "Bearer " + token))
                        .andExpect(status().isNotFound());
            mvc.perform(
                            delete("/api/v1/workspace/content/groups/" + group)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
            mvc.perform(
                            put("/api/v1/workspace/reading/sections/" + section.id())
                                    .header("Authorization", "Bearer " + token)
                                    .contentType("application/json")
                                    .content("{\"partId\":5,\"name\":\"Hijack\"}"))
                    .andExpect(status().isNotFound());
            mvc.perform(
                            post("/api/v1/reading/answer")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType("application/json")
                                    .content(
                                            "{\"questionId\":"
                                                    + question
                                                    + ",\"selectedAnswer\":\"A\"}"))
                    .andExpect(status().isNotFound());
            as(stranger);
            var own =
                    practiceAdmin.createReadingSection(
                            ReadingSectionRequest.builder().partId(5L).name("Own section").build());
            assertThrows(AppException.class, () -> practiceAdmin.attach(own.id(), question, 1));
            assertThrows(AppException.class, () -> practiceAdmin.detach(section.id(), question));
        }
        as(user);
        assertEquals("Private section", practice.set(section.id(), false).getName());
        assertEquals(1, content.questions(group).size());
    }

    @Test
    void usersSeeAllSevenDefaultPartsButCannotMutateThemOrUseAdminRoutes() throws Exception {
        String token = tokens.issue(user);
        mvc.perform(get("/api/v1/listening/parts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(4));
        mvc.perform(get("/api/v1/reading/parts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(3));
        for (String kind : List.of("reading", "listening")) {
            String body =
                    kind.equals("reading")
                            ? "{\"partNumber\":5,\"description\":\"Changed\"}"
                            : "{\"partNumber\":1,\"description\":\"Changed\"}";
            String id = kind.equals("reading") ? "5" : "1";
            mvc.perform(
                            post("/api/v1/workspace/" + kind + "/parts")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType("application/json")
                                    .content(body))
                    .andExpect(status().isForbidden());
            mvc.perform(
                            put("/api/v1/workspace/" + kind + "/parts/" + id)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType("application/json")
                                    .content(body))
                    .andExpect(status().isForbidden());
            mvc.perform(
                            delete("/api/v1/workspace/" + kind + "/parts/" + id)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(
                        post("/api/v1/admin/reading/sections")
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content("{\"partId\":5,\"name\":\"Private\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/vocabulary/categories")).andExpect(status().isUnauthorized());
    }

    @Test
    void manualVocabularyAndReviewedImportAreAvailableToUserAndStayPrivate() throws Exception {
        Long category =
                workspaceCreate(user, "vocabulary/categories", "{\"categoryName\":\"My words\"}");
        Long set =
                workspaceCreate(
                        user,
                        "vocabulary/sets",
                        "{\"categoryId\":" + category + ",\"setName\":\"Work\"}");
        Long word =
                workspaceCreate(
                        user,
                        "vocabulary/words",
                        "{\"setId\":"
                                + set
                                + ",\"word\":\"invoice\",\"meaning\":\"hóa"
                                + " đơn\",\"partOfSpeech\":\"noun\"}");
        String body =
                "{\"setId\":"
                        + set
                        + ",\"reviewed\":true,\"words\":[{\"setId\":"
                        + set
                        + ",\"word\":\"bill\",\"meaning\":\"hóa đơn\",\"partOfSpeech\":\"noun\"}]}";
        mvc.perform(
                        post("/api/v1/workspace/vocabulary/import/confirm")
                                .header("Authorization", "Bearer " + tokens.issue(user))
                                .contentType("application/json")
                                .content(body))
                .andExpect(status().isOk());
        as(user);
        assertEquals(2, vocabulary.getWordsBySet(set).size());
        assertTrue(vocabulary.getWordById(word).getFamily().isEmpty());
        verifyNoInteractions(groq);
        for (var stranger : List.of(createUser(UserRole.USER), createUser(UserRole.ADMIN))) {
            String token = tokens.issue(stranger);
            mvc.perform(
                            get("/api/v1/vocabulary/categories")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.length()").value(0));
            for (String path :
                    List.of(
                            "categories/" + category,
                            "sets/" + category,
                            "sets/" + set + "/detail",
                            "sets/" + set + "/words",
                            "words/" + word))
                mvc.perform(
                                get("/api/v1/vocabulary/" + path)
                                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isNotFound());
            for (String path : List.of("categories/" + category, "sets/" + set, "words/" + word))
                mvc.perform(
                                delete("/api/v1/workspace/vocabulary/" + path)
                                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isNotFound());
            mvc.perform(
                            post("/api/v1/workspace/vocabulary/import/confirm")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType("application/json")
                                    .content(body.replace("bill", "contract")))
                    .andExpect(status().isNotFound());
            as(stranger);
            var ownCategory =
                    vocabularyAdmin.createCategory(
                            VocabularyCategoryRequest.builder().categoryName("My words").build());
            var ownSet =
                    vocabularyAdmin.createSet(
                            VocabularySetRequest.builder()
                                    .categoryId(ownCategory.getId())
                                    .setName("Own")
                                    .build());
            assertThrows(
                    AppException.class, () -> vocabularyAdmin.attachWord(ownSet.getId(), word));
            assertThrows(
                    AppException.class,
                    () ->
                            vocabularyAdmin.updateSet(
                                    set,
                                    VocabularySetRequest.builder()
                                            .categoryId(ownCategory.getId())
                                            .setName("Hijack")
                                            .build()));
        }
        assertEquals(
                user.getId(),
                jdbc.queryForObject(
                        "SELECT owner_id FROM vocabulary_words WHERE id=?", String.class, word));
    }

    @Test
    void privateMediaRequiresItsCreatorEvenWhenUrlIsKnown() throws Exception {
        byte[] png =
                Base64.getDecoder()
                        .decode(
                                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aD1sAAAAASUVORK5CYII=");
        var response =
                mvc.perform(
                                multipart("/api/v1/workspace/uploads")
                                        .file(
                                                new org.springframework.mock.web.MockMultipartFile(
                                                        "file", "photo.png", "image/png", png))
                                        .param("type", "image")
                                        .header("Authorization", "Bearer " + tokens.issue(user)))
                        .andExpect(status().isOk())
                        .andReturn();
        String url =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(response.getResponse().getContentAsString())
                        .path("result")
                        .path("url")
                        .asText();
        mvc.perform(get(url).header("Authorization", "Bearer " + tokens.issue(user)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(png))
                .andExpect(header().string("Cache-Control", "no-store"));
        for (var stranger : List.of(createUser(UserRole.USER), createUser(UserRole.ADMIN))) {
            mvc.perform(get(url).header("Authorization", "Bearer " + tokens.issue(stranger)))
                    .andExpect(status().isNotFound());
            as(stranger);
            assertThrows(
                    AppException.class,
                    () ->
                            content.saveGroup(
                                    null,
                                    new ContentRequests.Group(
                                            1,
                                            "Stolen asset",
                                            null,
                                            url,
                                            null,
                                            null,
                                            null,
                                            List.of())));
        }
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(
                        multipart("/api/v1/workspace/uploads")
                                .file(
                                        new org.springframework.mock.web.MockMultipartFile(
                                                "file",
                                                "fake.png",
                                                "image/png",
                                                "<svg/>".getBytes()))
                                .param("type", "image")
                                .header("Authorization", "Bearer " + tokens.issue(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unassignedLegacyContentIsPreservedWithoutBeingPublished() {
        var group = content.group(group(5, List.of()));
        jdbc.update("UPDATE question_groups SET owner_id=NULL WHERE id=?", group.getId());
        entityManager.clear();
        assertTrue(content.listGroups(5).isEmpty());
        assertThrows(AppException.class, () -> content.group(group.getId()));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM question_groups WHERE id=?",
                        Integer.class,
                        group.getId()));
    }

    @Test
    void userCanGenerateExplanationOnlyWithTheirOwnSavedContext() throws Exception {
        Long group = group(5, List.of());
        when(groq.generate(any())).thenReturn("A short explanation");
        String body =
                "{\"partNumber\":5,\"groupId\":"
                        + group
                        + ",\"questionText\":\"Choose\",\"correctAnswer\":\"A\",\"options\":{\"A\":\"one\",\"B\":\"two\",\"C\":\"three\",\"D\":\"four\"}}";
        mvc.perform(
                        post("/api/v1/workspace/content/explanations")
                                .header("Authorization", "Bearer " + tokens.issue(user))
                                .contentType("application/json")
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.explanation").value("A short explanation"));
        mvc.perform(
                        post("/api/v1/workspace/content/explanations")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokens.issue(createUser(UserRole.USER)))
                                .contentType("application/json")
                                .content(body))
                .andExpect(status().isNotFound());
        verify(groq, times(1)).generate(any());
    }

    @Autowired jakarta.persistence.EntityManager entityManager;
}
