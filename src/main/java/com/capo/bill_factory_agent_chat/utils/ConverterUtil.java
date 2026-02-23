package com.capo.bill_factory_agent_chat.utils;

import org.springframework.http.codec.ServerSentEvent;

import com.capo.bill_factory_agent_chat.response.DataMessage;


public class ConverterUtil {
	
	public static DataMessage setDataMessage(String data) {
		DataMessage dataMessage= new DataMessage();
		dataMessage.setMessage(data);
		return dataMessage;
	}
	
	public static ServerSentEvent<DataMessage> setServerSentEvent(DataMessage data, String eventName) {
		return ServerSentEvent.<DataMessage>builder()
				.id("1")
				.comment("prueba")
				.event(eventName)
			    .data(data)
			    .build();
	}
}
