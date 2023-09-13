package com.ef.connector360.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ef.cim.objectmodel.Attribute;
import com.ef.cim.objectmodel.AudioMessage;
import com.ef.cim.objectmodel.Channel;
import com.ef.cim.objectmodel.ChannelConnector;
import com.ef.cim.objectmodel.CimMessage;
import com.ef.cim.objectmodel.ContactMessage;
import com.ef.cim.objectmodel.FileMessage;
import com.ef.cim.objectmodel.ImageMessage;
import com.ef.cim.objectmodel.LocationMessage;
import com.ef.cim.objectmodel.StickerMessage;
import com.ef.cim.objectmodel.ValueType;
import com.ef.cim.objectmodel.VideoMessage;
import com.ef.connector360.Connector360Application;
import com.ef.connector360.model.HeaderValidation;
import com.ef.connector360.model.Media;
import com.ef.connector360.model.MessageStatus;
import com.ef.connector360.model.Settings;
import com.ef.connector360.utility.Utility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InboundMessageProcessorServiceTest {

    @InjectMocks
    InboundMessageProcessorService inboundMessageProcessorService;
    @Mock
    FileUploaderService fileUploaderService;
    @Mock
    ConnectorConfigurationsRepositoryImpl channelRepository;
    @Mock
    Channel channel;

    @Test
    void testConstructCimMessage_WhenMessageTypeIsText_ThenReturnsCimMessage() throws JsonProcessingException {
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        CimMessage cimMessage =
                inboundMessageProcessorService.constructCimMessage(this.createTextMessageInstance(), headerValidation
                );

        assert cimMessage != null;
        assertEquals("PLAIN", cimMessage.getBody().getType().toString());
        assertEquals("Hello this is an answer", cimMessage.getBody().getMarkdownText());
    }

    @Test
    void testConstructCimMessage_WhenInvalidInput_ThenLogsExceptionAndReturnsNull() {

        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(null, null);
        assertNull(cimMessage);
    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsLocation_ThenReturnsCimLocationMessage()
            throws JsonProcessingException {
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(this.createLocationMessageInstance(),
                headerValidation);

        assert cimMessage != null;
        assertEquals("LOCATION", cimMessage.getBody().getType().toString());
        assertEquals(38.9806263495, ((LocationMessage) cimMessage.getBody()).getLocation().getLatitude());
    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsContact_ThenReturnsCimContactMessage()
            throws JsonProcessingException {
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(this.createContactMessageInstance(),
                headerValidation);

        assert cimMessage != null;
        assertEquals("CONTACT", cimMessage.getBody().getType().toString());
        assertEquals("Kerry Fisher",
                ((ContactMessage) cimMessage.getBody()).getContacts().get(0).getName().getFormattedName());

    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsImage_ThenReturnsCimImageMessage()
            throws IOException {
        Connector360Application.settings = Mockito.mock(Settings.class);
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"image/jpeg\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");
        channel = this.createChannelInstance();
        Mockito.doReturn(channel.getChannelConnector().getChannelProviderConfigs()).when(channelRepository).getConnectorConfigurations(Mockito.anyString());
        Mockito.doReturn(messageStatus).when(fileUploaderService)
                .uploadMediaToFileEngine(Mockito.any(Media.class), Mockito.anyString()
                );
        CimMessage cimMessage =
                inboundMessageProcessorService.constructCimMessage(this.createImageMessageInstance(), headerValidation
                );
        assert cimMessage != null;
        assertEquals("IMAGE", cimMessage.getBody().getType().toString());
        assertEquals("Check out my new phone!",
                ((ImageMessage) cimMessage.getBody()).getCaption());
    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsVideo_ThenReturnsCimVideoMessage()
            throws IOException {
        Connector360Application.settings = Mockito.mock(Settings.class);
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"video/mp4\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");
        channel = this.createChannelInstance();
        Mockito.doReturn(channel.getChannelConnector().getChannelProviderConfigs()).when(channelRepository).getConnectorConfigurations(Mockito.anyString());
        Mockito.doReturn(messageStatus).when(fileUploaderService)
                .uploadMediaToFileEngine(Mockito.any(Media.class), Mockito.anyString()
                );

        CimMessage cimMessage =
                inboundMessageProcessorService.constructCimMessage(this.createVideoMessageInstance(), headerValidation
                );
        assert cimMessage != null;
        assertEquals("VIDEO", cimMessage.getBody().getType().toString());
        assertEquals("Check out my new phone!",
                ((VideoMessage) cimMessage.getBody()).getCaption());
    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsVoice_ThenReturnsCimAudioMessage()
            throws IOException {
        Connector360Application.settings = Mockito.mock(Settings.class);
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"video/mp4\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");
        channel = this.createChannelInstance();
        Mockito.doReturn(channel.getChannelConnector().getChannelProviderConfigs()).when(channelRepository).getConnectorConfigurations(Mockito.anyString());
        Mockito.doReturn(messageStatus).when(fileUploaderService)
                .uploadMediaToFileEngine(Mockito.any(Media.class), Mockito.anyString()
                );

        CimMessage cimMessage =
                inboundMessageProcessorService.constructCimMessage(this.createVoiceMessageInstance(), headerValidation
                );
        assert cimMessage != null;
        assertEquals("AUDIO", cimMessage.getBody().getType().toString());
        assertEquals(60184, ((AudioMessage) cimMessage.getBody()).getAttachment().getSize());
    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsAudio_ThenReturnsCimAudioMessage()
            throws IOException {
        Connector360Application.settings = Mockito.mock(Settings.class);
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"video/mp4\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");
        channel = this.createChannelInstance();
        Mockito.doReturn(channel.getChannelConnector().getChannelProviderConfigs()).when(channelRepository).getConnectorConfigurations(Mockito.anyString());
        Mockito.doReturn(messageStatus).when(fileUploaderService)
                .uploadMediaToFileEngine(Mockito.any(Media.class), Mockito.anyString()
                );

        CimMessage cimMessage =
                inboundMessageProcessorService.constructCimMessage(this.createAudioMessageInstance(), headerValidation
                );
        assert cimMessage != null;
        assertEquals("AUDIO", cimMessage.getBody().getType().toString());
        assertEquals(60184, ((AudioMessage) cimMessage.getBody()).getAttachment().getSize());
    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsSticker_ThenReturnsCimStickerMessage()
            throws IOException {
        Connector360Application.settings = Mockito.mock(Settings.class);
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"video/mp4\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");
        channel = this.createChannelInstance();
        Mockito.doReturn(channel.getChannelConnector().getChannelProviderConfigs()).when(channelRepository).getConnectorConfigurations(Mockito.anyString());
        Mockito.doReturn(messageStatus).when(fileUploaderService)
                .uploadMediaToFileEngine(Mockito.any(Media.class), Mockito.anyString()
                );

        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(this.createStickerMessageInstance(),
                headerValidation);
        assert cimMessage != null;
        assertEquals("STICKER", cimMessage.getBody().getType().toString());
        assertEquals(Utility.buildImageThumbnailUrl("ME645425c97a07ff7698024f35d1690a8a"),
                ((StickerMessage) cimMessage.getBody()).getMediaUrl());

    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsDocument_ThenReturnsCimFileMessage()
            throws IOException {
        Connector360Application.settings = Mockito.mock(Settings.class);
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        Mockito.doReturn("https://file-engine.com").when(Connector360Application.settings).getFileEngineUrl();

        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setStatusCode(200);
        messageStatus.setBody("{\n"
                + "   \"message\":\"File uploaded successfully\",\n"
                + "   \"etag\":\"061439c97cade24334b1a6151d003be1\",\n"
                + "   \"name\":\"ME645425c97a07ff7698024f35d1690a8a\",\n"
                + "   \"type\":\"video/mp4\",\n"
                + "   \"size\":\"60184\"\n"
                + "}");
        channel = this.createChannelInstance();
        Mockito.doReturn(channel.getChannelConnector().getChannelProviderConfigs()).when(channelRepository).getConnectorConfigurations(Mockito.anyString());
        Mockito.doReturn(messageStatus).when(fileUploaderService)
                .uploadMediaToFileEngine(Mockito.any(Media.class), Mockito.anyString()
                );

        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(this.createDocumentMessageInstance(),
                headerValidation);
        assert cimMessage != null;
        assertEquals("FILE", cimMessage.getBody().getType().toString());
        assertEquals("80skaraokesonglistartist", ((FileMessage) cimMessage.getBody()).getCaption());

    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsListReply_ThenReturnsCimPlainMessage()
            throws IOException {
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(this.createListReplyMessageInstance(),
                headerValidation);
        assert cimMessage != null;
        assertEquals("PLAIN", cimMessage.getBody().getType().toString());
        assertEquals("Expertflow", cimMessage.getBody().getMarkdownText());

    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsButtonReply_ThenReturnsCimPlainMessage()
            throws IOException {
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        CimMessage cimMessage = inboundMessageProcessorService.constructCimMessage(this.createButtonReplyMessageInstance(),
                headerValidation);
        assert cimMessage != null;
        assertEquals("PLAIN", cimMessage.getBody().getType().toString());
        assertEquals("Expertflow", cimMessage.getBody().getMarkdownText());

    }

    @Test
    void testConstructCimMessage_WhenMessageTypeIsQuickButtonReply_ThenReturnsCimPlainMessage()
            throws IOException {
        HeaderValidation headerValidation = new HeaderValidation();
        headerValidation.setServiceIdentifier("123");
        CimMessage cimMessage =
                inboundMessageProcessorService.constructCimMessage(this.createQuickButtonReplyMessageInstance(),
                        headerValidation);
        assert cimMessage != null;
        assertEquals("PLAIN", cimMessage.getBody().getType().toString());
        assertEquals("Expertflow", cimMessage.getBody().getMarkdownText());

    }

    private LinkedHashMap<String, Object> createTextMessageInstance() throws JsonProcessingException {
        String textMessageStr = "{\n"
                + "  \"contacts\": [ {\n"
                + "    \"profile\": {\n"
                + "        \"name\": \"Kerry Fisher\"\n"
                + "    },\n"
                + "    \"wa_id\": \"16315551234\"\n"
                + "  } ],\n"
                + "  \"messages\":[{\n"
                + "    \"from\": \"16315551234\",\n"
                + "    \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "    \"timestamp\": \"1518694235\",\n"
                + "    \"text\": {\n"
                + "      \"body\": \"Hello this is an answer\"\n"
                + "    },\n"
                + "    \"type\": \"text\"\n"
                + "  }]\n"
                + "} ";

        return new ObjectMapper().readValue(textMessageStr, LinkedHashMap.class);

    }

    private LinkedHashMap<String, Object> createLocationMessageInstance() throws JsonProcessingException {
        String locationMessageStr = "{\n"
                + "  \"contacts\": [ {\n"
                + "    \"profile\": {\n"
                + "        \"name\": \"Kerry Fisher\"\n"
                + "    },\n"
                + "    \"wa_id\": \"16315551234\"\n"
                + "  } ],\n"
                + " \"messages\":[{\n"
                + "   \"from\":\"16315551234\",\n"
                + "   \"id\":\"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "   \"location\":{\n"
                + "      \"address\":\"Main Street Beach, Santa Cruz, CA\",\n"
                + "      \"latitude\":38.9806263495,\n"
                + "      \"longitude\":-131.9428612257,\n"
                + "      \"name\":\"Main Street Beach\",\n"
                + "      \"url\":\"https://foursquare.com/v/4d7031d35b5df7744\"},\n"
                + "   \"timestamp\":\"1521497875\",\n"
                + "   \"type\":\"location\"\n"
                + "  }]\n"
                + "} ";
        return new ObjectMapper().readValue(locationMessageStr, LinkedHashMap.class);

    }

    private LinkedHashMap<String, Object> createContactMessageInstance() throws JsonProcessingException {
        String contactMessageStr = "{\n"
                + "  \"contacts\": [\n"
                + "    {\n"
                + "      \"profile\": {\n"
                + "        \"name\": \"Kerry Fisher\"\n"
                + "      },\n"
                + "      \"wa_id\": \"16315551234\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"contacts\": [\n"
                + "        {\n"
                + "          \"addresses\": [\n"
                + "            {\n"
                + "              \"city\": \"Menlo Park\",\n"
                + "              \"country\": \"United States\",\n"
                + "              \"country_code\": \"us\",\n"
                + "              \"state\": \"CA\",\n"
                + "              \"street\": \"1 Hacker Way\",\n"
                + "              \"type\": \"WORK\",\n"
                + "              \"zip\": \"94025\"\n"
                + "            }\n"
                + "          ],\n"
                + "          \"birthday\": \"2012-08-18\",\n"
                + "          \"emails\": [\n"
                + "            {\n"
                + "              \"email\": \"kfish@fb.com\",\n"
                + "              \"type\": \"WORK\"\n"
                + "            }\n"
                + "          ],\n"
                + "          \"name\": {\n"
                + "            \"first_name\": \"Kerry\",\n"
                + "            \"formatted_name\": \"Kerry Fisher\",\n"
                + "            \"last_name\": \"Fisher\"\n"
                + "          },\n"
                + "          \"org\": {\n"
                + "            \"company\": \"Facebook\"\n"
                + "          },\n"
                + "          \"phones\": [\n"
                + "            {\n"
                + "              \"phone\": \"+1 (940) 555-1234\",\n"
                + "              \"type\": \"CELL\"\n"
                + "            }\n"
                + "          ],\n"
                + "          \"urls\": [\n"
                + "            {\n"
                + "              \"url\": \"https://www.facebook.com\",\n"
                + "              \"type\": \"WORK\"\n"
                + "            }\n"
                + "          ]\n"
                + "        }\n"
                + "      ],\n"
                + "      \"from\": \"16505551234\",\n"
                + "      \"id\": \"ABGGFlA4dSRvAgo6C4Z53hMh1ugR\",\n"
                + "      \"timestamp\": \"1537248012\",\n"
                + "      \"type\": \"contacts\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        return new ObjectMapper().readValue(contactMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createImageMessageInstance() throws JsonProcessingException {
        String imageMessageStr = "{\n"
                + "  \"contacts\": [\n"
                + "    {\n"
                + "      \"profile\": {\n"
                + "        \"name\": \"Kerry Fisher\"\n"
                + "      },\n"
                + "      \"wa_id\": \"16315551234\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"from\": \"16315551234\",\n"
                + "      \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "      \"image\": {\n"
                + "        \"id\": \"b1c68f38-8734-4ad3-b4a1-ef0c10d683\",\n"
                + "        \"mime_type\": \"image/jpeg\",\n"
                + "        \"sha256\": \"29ed500fa64eb55fc19dc4124acb300e5dcc54a0f822a301ae99944db\",\n"
                + "        \"caption\": \"Check out my new phone!\"\n"
                + "      },\n"
                + "      \"timestamp\": \"1521497954\",\n"
                + "      \"type\": \"image\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        return new ObjectMapper().readValue(imageMessageStr, LinkedHashMap.class);

    }

    private LinkedHashMap<String, Object> createVideoMessageInstance() throws JsonProcessingException {
        String imageMessageStr = "{\n"
                + "  \"contacts\": [\n"
                + "    {\n"
                + "      \"profile\": {\n"
                + "        \"name\": \"Kerry Fisher\"\n"
                + "      },\n"
                + "      \"wa_id\": \"16315551234\"\n"
                + "    }\n"
                + "  ],\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"from\": \"16315551234\",\n"
                + "      \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "      \"video\": {\n"
                + "        \"id\": \"b1c68f38-8734-4ad3-b4a1-ef0c10d683\",\n"
                + "        \"mime_type\": \"video/mp4\",\n"
                + "        \"sha256\": \"29ed500fa64eb55fc19dc4124acb300e5dcc54a0f822a301ae99944db\",\n"
                + "        \"caption\": \"Check out my new phone!\"\n"
                + "      },\n"
                + "      \"timestamp\": \"1521497954\",\n"
                + "      \"type\": \"video\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        return new ObjectMapper().readValue(imageMessageStr, LinkedHashMap.class);

    }

    private LinkedHashMap<String, Object> createVoiceMessageInstance() throws JsonProcessingException {
        String voiceMessageStr = "{\n"
                + "    \"messages\":[{\n"
                + "        \"from\": \"16315551234\",\n"
                + "        \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "        \"timestamp\": \"1521827831\",\n"
                + "        \"type\": \"voice\",\n"
                + "        \"voice\": {\n"
                + "            \"file\": \"/usr/local/wamedia/shared/463e/b7ec/ff4e4d9bb1101879cbd411b2\",\n"
                + "            \"id\": \"463eb7ec-ff4e-4d9b-b110-1879cbd411b2\",\n"
                + "            \"mime_type\": \"audio/ogg; codecs=opus\",\n"
                + "            \"sha256\": \"fa9e1807d936b7cebe63654ea3a7912b1fa9479220258d823590521ef53b0710\"}\n"
                + "  }]\n"
                + "}";
        return new ObjectMapper().readValue(voiceMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createAudioMessageInstance() throws JsonProcessingException {
        String audioMessageStr = "{\n"
                + "    \"messages\":[{\n"
                + "        \"from\": \"16315551234\",\n"
                + "        \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "        \"timestamp\": \"1521827831\",\n"
                + "        \"type\": \"audio\",\n"
                + "        \"audio\": {\n"
                + "            \"file\": \"/usr/local/wamedia/shared/463e/b7ec/ff4e4d9bb1101879cbd411b2\",\n"
                + "            \"id\": \"463eb7ec-ff4e-4d9b-b110-1879cbd411b2\",\n"
                + "            \"mime_type\": \"audio/mpeg\",\n"
                + "            \"sha256\": \"fa9e1807d936b7cebe63654ea3a7912b1fa9479220258d823590521ef53b0710\"}\n"
                + "  }]\n"
                + "}";
        return new ObjectMapper().readValue(audioMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createStickerMessageInstance() throws JsonProcessingException {
        String stickerMessageStr = "{\n"
                + "  \"messages\":[{\n"
                + "        \"from\": \"16315551234\",\n"
                + "        \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "        \"timestamp\": \"1521827831\",\n"
                + "        \"type\": \"sticker\",\n"
                + "        \"sticker\": {\n"
                + "            \"id\": \"b1c68f38-8734-4ad3-b4a1-ef0c10d683\",\n"
                + "            \"metadata\": {\n"
                + "                \"sticker-pack-id\": \"463eb7ec-ff4e-4d9b-b110-1879cbd411b2\",\n"
                + "                \"sticker-pack-name\" : \"Happy New Year\",\n"
                + "                \"sticker-pack-publisher\" : \"Kerry Fisher\",\n"
                + "                \"emojis\": [\"\uD83D\uDC25\", \"\uD83D\uDE03\"],\n"
                + "                \"ios-app-store-link\" : \"https://apps.apple.com/app/id3133333\",\n"
                + "                \"android-app-store-link\" : \"https://play.google.com/store/apps/details?id=com.example\",\n"
                + "                \"is-first-party-sticker\" : 0 \n"
                + "            },\n"
                + "            \"mime_type\": \"image/webp\",\n"
                + "            \"sha256\": \"fa9e1807d936b7cebe63654ea3a7912b1fa9479220258d823590521ef53b0710\"\n"
                + "        }  \n"
                + "    }]\n"
                + "}";
        return new ObjectMapper().readValue(stickerMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createDocumentMessageInstance() throws JsonProcessingException {
        String documentMessageStr = "{\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"from\": \"16315551234\",\n"
                + "      \"id\": \"ABGGFlA5FpafAgo6tHcNmNjXmuSf\",\n"
                + "      \"timestamp\": \"1522189546\",\n"
                + "      \"type\": \"document\",\n"
                + "      \"document\": {\n"
                + "        \"caption\": \"80skaraokesonglistartist\",\n"
                + "        \"file\": \"/usr/local/wamedia/shared/fc233119-733f-49c-bcbd-b2f68f798e33\",\n"
                + "        \"id\": \"fc233119-733f-49c-bcbd-b2f68f798e33\",\n"
                + "        \"mime_type\": \"application/pdf\",\n"
                + "        \"sha256\": \"3b11fa6ef2bde1dd14726e09d3edaf782120919d06f6484f32d5d5caa4b8e\"\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        return new ObjectMapper().readValue(documentMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createListReplyMessageInstance() throws JsonProcessingException {
        String listReplyMessageStr = "{\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"context\": {\n"
                + "        \"from\": \"sender_wa_id_of_context_message\",\n"
                + "        \"group_id\": \"group_id_of_context_message\",\n"
                + "        \"id\": \"message_id_of_context_message\",\n"
                + "        \"mentions\": [\n"
                + "          \"wa_id1\",\n"
                + "          \"wa_id2\"\n"
                + "        ]\n"
                + "      },\n"
                + "      \"from\": \"sender_wa_id\",\n"
                + "      \"group_id\": \"group_id\",\n"
                + "      \"id\": \"message_id\",\n"
                + "      \"timestamp\": \"1521497875\",\n"
                + "      \"type\": \"interactive\",\n"
                + "      \"interactive\": {\n"
                + "        \"type\": \"list_reply\",\n"
                + "        \"list_reply\": {\n"
                + "          \"title\": \"Expertflow\",\n"
                + "          \"id\": \"unique-row-identifier-here\",\n"
                + "          \"description\": \"row-description-content-here\"\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        return new ObjectMapper().readValue(listReplyMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createButtonReplyMessageInstance() throws JsonProcessingException {
        String buttonReplyMessageStr = "{\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"context\": {\n"
                + "        \"from\": \"sender_wa_id_of_context_message\",\n"
                + "        \"group_id\": \"group_id_of_context_message\",\n"
                + "        \"id\": \"message_id_of_context_message\",\n"
                + "        \"mentions\": [\n"
                + "          \"wa_id1\",\n"
                + "          \"wa_id2\"\n"
                + "        ]\n"
                + "      },\n"
                + "      \"from\": \"sender_wa_id\",\n"
                + "      \"group_id\": \"group_id\",\n"
                + "      \"id\": \"message_id\",\n"
                + "      \"timestamp\": \"1521497875\",\n"
                + "      \"type\": \"interactive\",\n"
                + "      \"interactive\": {\n"
                + "        \"type\": \"button_reply\",\n"
                + "        \"button_reply\": {\n"
                + "          \"id\": \"111\",\n"
                + "          \"title\": \"Expertflow\"\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        return new ObjectMapper().readValue(buttonReplyMessageStr, LinkedHashMap.class);
    }

    private LinkedHashMap<String, Object> createQuickButtonReplyMessageInstance() throws JsonProcessingException {
        String quickButtonReplyMessageStr = "{\n"
                + "  \"messages\": [\n"
                + "    {\n"
                + "      \"button\": {\n"
                + "        \"payload\": \"No-Button-Payload\",\n"
                + "        \"text\": \"Expertflow\"\n"
                + "      },\n"
                + "      \"context\": {\n"
                + "        \"from\": \"16315558007\",\n"
                + "        \"id\": \"gBGGFmkiWVVPAgkgQkwi7IORac0\"\n"
                + "      },\n"
                + "      \"from\": \"16505551234\",\n"
                + "      \"id\": \"ABGGFmkiWVVPAgo-sKD87hgxPHdF\",\n"
                + "      \"timestamp\": \"1591210827\",\n"
                + "      \"type\": \"button\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        return new ObjectMapper().readValue(quickButtonReplyMessageStr, LinkedHashMap.class);
    }

    private Channel createChannelInstance() {
        Channel channel = new Channel();
        ChannelConnector channelConnector = new ChannelConnector();
        Attribute hostUrl = new Attribute();
        hostUrl.setType(ValueType.String50);
        hostUrl.setKey("HOST-URL");
        hostUrl.setValue("expertflow.com");
        Attribute apiKey = new Attribute();
        apiKey.setType(ValueType.String50);
        apiKey.setKey("API-KEY");
        apiKey.setValue("expertflow");
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(0, hostUrl);
        attributes.add(1, apiKey);
        channelConnector.setChannelProviderConfigs(attributes);
        channel.setChannelConnector(channelConnector);
        return channel;
    }

}