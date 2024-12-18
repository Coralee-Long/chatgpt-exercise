package com.example.chatgptbasedcookingingredients.service;

import com.example.chatgptbasedcookingingredients.dto.ChatGptMessage;
import com.example.chatgptbasedcookingingredients.dto.ChatGptRequest;
import com.example.chatgptbasedcookingingredients.dto.ChatGptResponse;
import com.example.chatgptbasedcookingingredients.dto.ChatGptResponseFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

// ApiService: Handles all interactions with the ChatGPT API by:
// Building request, sending it to ChatGPT, and returning the response.

// @Service annotation marks this as a Spring-managed service, making it injectable elsewhere.
@Service
public class ChatGptApiService {

	 private final RestClient restClient;

	 @Value ("${chatgpt.api.key}")
	 private String apiKey;

	 // Constructor
	 public ChatGptApiService (RestClient.Builder builder) {
			this.restClient =
					builder.build();
	 }


	 public String getChatGptResponse (String ingredient) {
			// Update the prompt to include "Respond in JSON format"
			String prompt = "Classify the ingredient '" + ingredient + "' as vegan, vegetarian, or regular. Respond in JSON format.";

			// Create the ChatGPT request
			ChatGptRequest request = new ChatGptRequest(
					"gpt-4o-mini",
					List.of(new ChatGptMessage("user", prompt)),
					new ChatGptResponseFormat("json_object")
			);

			// Make the API call
			ChatGptResponse response = restClient.post()
					.uri("https://api.openai.com/v1/chat/completions")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.body(ChatGptResponse.class);

			// Extract and return the response content
			return response.choices().get(0).message().content();
	 }

}
