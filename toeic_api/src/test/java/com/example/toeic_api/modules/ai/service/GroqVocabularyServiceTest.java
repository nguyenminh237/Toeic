package com.example.toeic_api.modules.ai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.toeic_api.modules.vocabulary.dto.request.VocabularyImportRequests;
import com.example.toeic_api.shared.exception.*;
import com.fasterxml.jackson.databind.*;

import jakarta.validation.Validation;

import org.junit.jupiter.api.*;

import java.util.*;

class GroqVocabularyServiceTest {
    final ObjectMapper mapper = new ObjectMapper();
    final GroqJsonClient client = mock(GroqJsonClient.class);
    jakarta.validation.ValidatorFactory factory;
    GroqVocabularyService service;

    @BeforeEach
    void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        service =
                new GroqVocabularyService(
                        client, mapper, factory.getValidator(), "text-model", "vision-model");
    }

    @AfterEach
    void close() {
        factory.close();
    }

    JsonNode generated() throws Exception {
        return mapper.readTree(
                """
{"words":[{"word":"charge","senses":[{"meaning":"tính phí","partOfSpeech":"verb","minimumTargetScore":450,
"exampleSentence":"The hotel charges a fee.","exampleTranslation":"Khách sạn thu phí.","synonyms":[],"antonyms":[],"collocations":["charge a fee — thu phí"]}],"family":[]}]}
""");
    }

    @Test
    void filtersNonEnglishNoiseAndDeduplicatesExtraction() throws Exception {
        when(client.complete(anyString(), anyString(), any(), anyInt()))
                .thenReturn(
                        mapper.readTree(
                                "{\"terms\":[\"Invoice\",\"invoice\",\"hóa"
                                        + " đơn\",\"650\",\"the\",\"apply for\"]}"));
        assertEquals(List.of("invoice", "apply for"), service.extract("untrusted document", null));
        verify(client)
                .complete(
                        eq("text-model"),
                        contains("untrusted"),
                        eq("untrusted document"),
                        anyInt());
    }

    @Test
    void routesImagesToVisionAndNeverToTextOnlyModel() throws Exception {
        when(client.complete(anyString(), anyString(), any(), anyInt()))
                .thenReturn(mapper.readTree("{\"terms\":[\"invoice\"]}"));
        assertEquals(List.of("invoice"), service.extract(null, "data:image/jpeg;base64,abc"));
        verify(client)
                .complete(
                        eq("vision-model"), anyString(), argThat(x -> x instanceof List), anyInt());
    }

    @Test
    void generatesOptionalEmptyFieldsWithoutWritingAnything() throws Exception {
        when(client.complete(anyString(), anyString(), any(), anyInt())).thenReturn(generated());
        var word =
                service.generate(new VocabularyImportRequests.Generate(650, List.of("charge")))
                        .getFirst();
        assertNull(word.getSetId());
        assertEquals("tính phí", word.getMeaning());
        assertTrue(word.getFamily().isEmpty());
        assertTrue(word.getSenses().getFirst().antonyms().isEmpty());
        assertEquals(450, word.getSenses().getFirst().minimumTargetScore());
    }

    @Test
    void rejectsInventedWordsAndMeaningsAboveTheSelectedAim() throws Exception {
        var result = generated();
        ((com.fasterxml.jackson.databind.node.ObjectNode) result.path("words").get(0))
                .put("word", "unrequested");
        when(client.complete(anyString(), anyString(), any(), anyInt())).thenReturn(result);
        assertEquals(
                ErrorCode.AI_INVALID_RESPONSE,
                assertThrows(
                                AppException.class,
                                () ->
                                        service.generate(
                                                new VocabularyImportRequests.Generate(
                                                        650, List.of("charge"))))
                        .getErrorCode());
        result = generated();
        ((com.fasterxml.jackson.databind.node.ObjectNode)
                        result.path("words").get(0).path("senses").get(0))
                .put("minimumTargetScore", 850);
        when(client.complete(anyString(), anyString(), any(), anyInt())).thenReturn(result);
        assertThrows(
                AppException.class,
                () ->
                        service.generate(
                                new VocabularyImportRequests.Generate(650, List.of("charge"))));
    }

    @Test
    void rejectsEnglishDefinitionsInsteadOfVietnameseMeaningsAndFiltersVerbInflections()
            throws Exception {
        var result = generated();
        ((com.fasterxml.jackson.databind.node.ObjectNode)
                        result.path("words").get(0).path("senses").get(0))
                .put("meaning", "to demand payment");
        when(client.complete(anyString(), anyString(), any(), anyInt())).thenReturn(result);
        assertThrows(
                AppException.class,
                () ->
                        service.generate(
                                new VocabularyImportRequests.Generate(650, List.of("charge"))));
        result = generated();
        ((com.fasterxml.jackson.databind.node.ObjectNode) result.path("words").get(0))
                .set(
                        "family",
                        mapper.readTree(
                                "[{\"word\":\"charging\",\"partOfSpeech\":\"verb\",\"meaning\":\"đang"
                                    + " tính phí\"}]"));
        when(client.complete(anyString(), anyString(), any(), anyInt())).thenReturn(result);
        assertTrue(
                service.generate(new VocabularyImportRequests.Generate(650, List.of("charge")))
                        .getFirst()
                        .getFamily()
                        .isEmpty());
    }

    @Test
    void rejectsNonEnglishInputBeforeCallingProvider() {
        assertThrows(
                AppException.class,
                () ->
                        service.generate(
                                new VocabularyImportRequests.Generate(650, List.of("hóa đơn"))));
        verifyNoInteractions(client);
    }
}
