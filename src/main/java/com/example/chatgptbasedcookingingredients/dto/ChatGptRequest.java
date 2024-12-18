package com.example.chatgptbasedcookingingredients.dto;

import java.util.List;

// ChatGptRequest: The structure for the API request sent to OpenAI
public record ChatGptRequest(
		String model,
		List<ChatGptMessage> messages,
		ChatGptResponseFormat response_format
) {

}
