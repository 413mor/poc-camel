package com.example.poc;

import static mx.isban.agavecloud.dataaccess.utils.DataAccessConstants.CODE_SUCCESFULLY;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import mx.isban.agavecloud.commons.exception.ExceptionDataAccess;
import mx.isban.agavecloud.dataaccess.channels.cics.dto.ResponseMessageCicsDTO;

@Component
public class BeanCamelJMS {
	
	public String createRequestMessage(Exchange exchange) throws ExceptionDataAccess {
		return "    DEIFSNMXPE6810901123451O00X2CAN                IDTRANS 1                       1A                    9999APP1VERSION1ARQUITECTURA  ALTAIRNUMERO DE OPERACION APLICATIVA                                                                                                                                                                                                                                           000                                                                                                                                                                                                                                                                                                                                000000000                                                                                                                                                                                                                                                        000                                                                                                     ";
	}
	
	public void createResponseMap(Exchange exchange) {
		String body = exchange.getIn().getBody(String.class);

        System.out.println("Message body: " + body);
        ResponseMessageCicsDTO cicsDTO = new ResponseMessageCicsDTO();
        cicsDTO.setResponseMessage(body);
        cicsDTO.setCodeError(CODE_SUCCESFULLY);
        cicsDTO.setMessageError("EJECUCION EXITOSA");
        exchange.getOut().setBody(cicsDTO);
	}
	//Recipient List -> dirigir
	//load balancing 
}
