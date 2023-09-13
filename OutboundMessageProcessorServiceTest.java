package com.ef.connector360.services;

import static org.junit.jupiter.api.Assertions.*;

import com.ef.cim.objectmodel.Attribute;
import com.ef.cim.objectmodel.ValueType;
import com.ef.connector360.dto.ConnectorConfigurationsDto;
import com.ef.connector360.model.Response;
import com.ef.connector360.rest.HttpClient;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OutboundMessageProcessorServiceTest {

    @InjectMocks
    OutboundMessageProcessorService outboundMessageProcessorService;
    @Mock
    ConnectorConfigurationsRepositoryImpl configurationsRepository;
    @Mock
    HttpClient httpClient;

    @Test
    void testProcessConnectorConfigurations_WhenInvalidConfigurations_ThenReturnResponseWithBadRequestStatusCode() {
        ConnectorConfigurationsDto configurationsDto = this.getConnectorConfigurations();
        configurationsDto.setConnectorConfigurations(null);
        Response response =
                outboundMessageProcessorService.processConnectorConfigurations(configurationsDto);
        assertEquals(HttpStatus.BAD_REQUEST.value(),response.getStatus(),"Response status");
    }

    @Test
    void testProcessConnectorConfigurations_WhenValidConfigurations_ThenReturnResponseWithOKStatusCode() {
        ConnectorConfigurationsDto configurationsDto = this.getConnectorConfigurations();
        Response response =
                outboundMessageProcessorService.processConnectorConfigurations(configurationsDto);
        assertEquals(HttpStatus.OK.value(),response.getStatus(),"Response status");
    }

    private ConnectorConfigurationsDto getConnectorConfigurations() {
        ConnectorConfigurationsDto configurationsDto = new ConnectorConfigurationsDto();
        configurationsDto.setServiceIdentifier("123");
        Attribute attribute = new Attribute();
        attribute.setKey("KEY");
        attribute.setType(ValueType.String50);
        attribute.setValue("Expertflow");
        List<Attribute> attributes = new ArrayList<>(1);
        attributes.add(attribute);
        configurationsDto.setConnectorConfigurations(attributes);
        return configurationsDto;
    }
}