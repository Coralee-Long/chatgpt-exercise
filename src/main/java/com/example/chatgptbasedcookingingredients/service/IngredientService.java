package com.example.chatgptbasedcookingingredients.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {

	 private final ChatGptApiService apiService;

	 public String categorize(String ingredient) {
			return apiService.getChatGptResponse(ingredient);
	 }
}
