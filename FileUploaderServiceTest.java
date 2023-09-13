package com.ef.connector360.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ef.connector360.Connector360Application;
import com.ef.connector360.rest.HttpClient;
import com.ef.connector360.model.Media;
import com.ef.connector360.model.MessageStatus;
import com.ef.connector360.model.Settings;
import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class FileUploaderServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(FileUploaderServiceTest.class);
    @InjectMocks
    FileUploaderService fileUploaderService;
    private Media media;
    private MessageStatus messageStatus;
    @Mock
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws JSONException {
        media = new Media();
        media.setMedia("https://Expertflow.com/image.png");
        media.setMimeType("image/png");
        media.setAPIKey360("api-key");
        media.setSize(3);
        media.setFileName("Expertflow");

        messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"video/mp4\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");

    }

    @Test
    void testUploadMediaToFileEngine_WhenSuccessful_ThenReturnsMessageStatusWithCode200() {
        Connector360Application.settings = Mockito.mock(Settings.class);
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        try {
            when(httpClient.sendPostRequest(Mockito.anyString(), Mockito.any(String.class))
            ).thenReturn(messageStatus);
            assertEquals(200,
                    fileUploaderService.uploadMediaToFileEngine(media, "12345").getStatusCode(),
                    "HTTP Status Code");

        } catch (IOException ioException) {
            logger.error("Error Message : " + ExceptionUtils.getMessage(ioException));
            logger.error("Stack Trace : " + ExceptionUtils.getStackTrace(ioException));
        }


    }

    @Test
    void testUploadMediaToFileEngine_WhenUnSuccessful_ThenReturnsMessageStatusWithErrorCode() {
        messageStatus.setStatusCode(500);
        Connector360Application.settings = Mockito.mock(Settings.class);
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        try {
            when(httpClient.sendPostRequest(Mockito.anyString(), Mockito.any(String.class))
            ).thenReturn(messageStatus);

            assertEquals(500,
                    fileUploaderService.uploadMediaToFileEngine(media, "12345").getStatusCode(),
                    "HTTP Status Code");

        } catch (IOException ioException) {
            logger.error("Error Message : " + ExceptionUtils.getMessage(ioException));
            logger.error("Stack Trace : " + ExceptionUtils.getStackTrace(ioException));
        }


    }
}