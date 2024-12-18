package com.example.chatgptbasedcookingingredients.dto;

// ChatGptMessage: Represents a single message in the conversation
public record ChatGptMessage(
		String role,
		String content
) {
}
