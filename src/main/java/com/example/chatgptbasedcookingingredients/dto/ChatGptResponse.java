package com.example.chatgptbasedcookingingredients.dto;

import java.util.List;

public record ChatGptResponse(
		List<ChatGptChoice> choices
) {
}
