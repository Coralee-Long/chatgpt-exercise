package com.example.chatgptbasedcookingingredients.controller;


import com.example.chatgptbasedcookingingredients.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    String categorizeIngredient(@RequestBody String ingredient) {

        // TODO: This should return "vegan", "vegetarian" or "regular" depending on the ingredient.

        // Call the service to classify the ingredient
        return ingredientService.categorize(ingredient);
    }

}
