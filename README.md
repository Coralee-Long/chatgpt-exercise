# ChatGPT API Integration with Spring Boot

**A short walk-through of building a Spring Boot project that
integrates the OpenAI ChatGPT API to perform tasks like classifying
ingredients.**

<!-- TOC -->
  * [1. Project Setup](#1-project-setup)
  * [2. Project Structure](#2-project-structure)
  * [3. Core Components](#3-core-components)
  * [4. IngredientController](#4-ingredientcontroller)
  * [5. IngredientService](#5-ingredientservice)
  * [7. IngredientService](#7-ingredientservice)
  * [8. ChatGptApiService](#8-chatgptapiservice)
  * [9. DTOs (Data Transfer Objects)](#9-dtos-data-transfer-objects)
    * [1. ChatGptRequest](#1-chatgptrequest)
    * [2. ChatGptMessage](#2-chatgptmessage)
    * [3. ChatGptResponse](#3-chatgptresponse)
    * [4. ChatGptChoice](#4-chatgptchoice)
    * [5. ChatGptResponseFormat](#5-chatgptresponseformat)
  * [10. Testing with Postman](#10-testing-with-postman)
    * [1. Request Setup](#1-request-setup)
    * [2. Headers](#2-headers)
    * [3. Request Body](#3-request-body)
    * [4. Response](#4-response)
  * [11. Error Handling](#11-error-handling)
    * [Common Errors and Fixes](#common-errors-and-fixes)
  * [EXTRA: How to Kill a Process on a Port](#extra-how-to-kill-a-process-on-a-port)
    * [On Mac/Linux:](#on-maclinux)
<!-- TOC -->



## 1. Project Setup

1. **Create a Spring Boot Project**:
    - Use Spring Initializr to generate a Maven project with dependencies:
        - `Spring Web`
        - `Lombok`

2. **Add Dependencies**:
   Add the following to your `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
   </dependency>
   ```
3. **Environment Setup:**
- Add your OpenAI API key in application.properties:

    `chatgpt.api.key=${YOUR_API_KEY}`



## 2. Project Structure

```xml
src/
├── main/java/com/example/
│   ├── controller/
│   │   └── IngredientController.java
│   ├── service/
│   │   ├── IngredientService.java
│   │   └── ChatGptApiService.java
│   ├── dto/
│   │   ├── ChatGptChoice.java
│   │   ├── ChatGptMessage.java
│   │   ├── ChatGptRequest.java
│   │   ├── ChatGptResponse.java
│   │   └── ChatGptResponseFormat.java
│   └── Application.java
└── resources/
└── application.properties
```



## 3. Core Components

1. **Controller**: Handles HTTP requests and maps input to the service layer.
2. **Service**: Contains the business logic and communicates with external services.
3. **DTOs**: Represent the request and response objects for clean and structured data handling.

    ### Component Breakdown

   - **Controller**: Receives HTTP requests and passes the input to the service. Returns the output to the client.
   - **Service**: Calls the API service to interact with ChatGPT and processes the response.
   - **ChatGptApiService**: Handles communication with the OpenAI API, sends prompts, and retrieves results.
   - **DTOs (Data Transfer Objects)**: Classes used to map the request sent to and response received from the ChatGPT API.



## 4. IngredientController

The `IngredientController` is the entry point for HTTP requests. 

It handles the input from the client, delegates processing to the `IngredientService`, and returns the response.

**Code Example:**

```java
@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public Map<String, String> categorizeIngredient(@RequestBody Map<String, String> body) {
        String ingredient = body.get("ingredient");
        String classification = ingredientService.categorize(ingredient);
        return Map.of("ingredient", ingredient, "classification", classification);
    }
}
```

**Key Points:**
- `@RestController`: Marks the class as a REST controller that handles HTTP 
requests.
- `@RequestMapping("/ingredients")`: Maps the base endpoint to `/ingredients`.
- `@PostMapping`: Handles HTTP POST requests.
- `@RequestBody`: Parses incoming JSON into a `Map<String, String>`.
- `Service Delegation`: The controller delegates classification logic to the 
  `IngredientService`.

**Example:**
```html
// input
    {
      "ingredient": "cheese"
    }
    
// output
    { 
      "ingredient": "cheese",
      "classification": "regular"
    }
```

## 5. IngredientService

The `IngredientService` contains the business logic. It calls the `ChatGptApiService` to interact with the ChatGPT API and processes the response to return the classification.

**Code Example:**

```java
package com.example.chatgptbasedcookingingredients.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final ChatGptApiService apiService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson for JSON parsing

    public String categorize(String ingredient) {
        try {
            // Call the API service to get the response
            String jsonResponse = apiService.getChatGptResponse(ingredient);

            // Parse JSON and extract the "classification" field
            Map<String, String> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            return responseMap.get("classification");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ChatGPT response", e);
        }
    }
}
```

**Key Points:**
- **`@Service`**: Marks this class as a Spring service.
- **Delegation to ChatGptApiService**: The service calls `getChatGptResponse()` to get the API response.
- **JSON Parsing**:
    - Uses **Jackson's ObjectMapper** to convert the API response (a JSON string) into a `Map`.
    - Extracts the `classification` value.
- **Error Handling**: Wraps potential parsing errors with a custom runtime exception.



## 7. IngredientService

The `IngredientService` delegates the task of interacting with the ChatGPT API to the `ChatGptApiService`.

**Code Example:**

```java
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
```

**Key Points:**
- **`@Service`**: Marks this class as a Spring service.
- **Delegation**: The `categorize` method directly calls `getChatGptResponse()` from the `ChatGptApiService`.
- **Simplicity**: This implementation keeps the service lightweight and avoids additional processing.



## 8. ChatGptApiService

The `ChatGptApiService` handles communication with the OpenAI ChatGPT API. It builds the request, sends it, and retrieves the response.

**Code Example:**

```java
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

@Service
public class ChatGptApiService {

    private final RestClient restClient;

    @Value("${app.chatgpt.api.key}")
    private String apiKey;

    public ChatGptApiService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String getChatGptResponse(String ingredient) {
        // Build the request object
        ChatGptRequest request = new ChatGptRequest(
                "gpt-3.5-turbo",
                List.of(new ChatGptMessage("user", 
                    "Classify the ingredient '" + ingredient + "' as vegan, vegetarian, or regular. Respond in JSON format.")
                ),
                new ChatGptResponseFormat("json_object")
        );

        // Make the API request
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
```

**Key Points:**
- **`@Service`**: Marks this class as a Spring service.
- **`RestClient`**: Handles HTTP requests to the ChatGPT API.
- **Request Building**: Constructs a `ChatGptRequest` with:
    - `model`: Specifies the GPT model (e.g., `gpt-3.5-turbo`).
    - `messages`: Contains a `ChatGptMessage` with the user prompt.
    - `response_format`: Ensures the response is in `json_object` format.
- **Headers**: Adds the OpenAI API key in the `Authorization` header.
- **Response Parsing**: Retrieves the response and extracts the content field.



## 9. DTOs (Data Transfer Objects)

**DTOs are used to structure the data for communication between the 
application and the ChatGPT API. They represent both the request payload and 
the response received.**

### 1. ChatGptRequest

**Purpose**: Represents the payload sent to the ChatGPT API.

**Code Example:**
```java
package com.example.chatgptbasedcookingingredients.dto;

import java.util.List;

public record ChatGptRequest(
        String model,
        List<ChatGptMessage> messages,
        ChatGptResponseFormat response_format
) {}
```

### 2. ChatGptMessage

**Purpose**: Represents a message in the request.

**Code Example:**
```java
package com.example.chatgptbasedcookingingredients.dto;

public record ChatGptMessage(
        String role,
        String content
) {}
```

### 3. ChatGptResponse

**Purpose**: Represents the response received from the ChatGPT API.

**Code Example:**
```java
package com.example.chatgptbasedcookingingredients.dto;

import java.util.List;

public record ChatGptResponse(
        List<ChatGptChoice> choices
) {}
```

### 4. ChatGptChoice

**Purpose**: Represents a single choice within the API response.

**Code Example:**
```java
package com.example.chatgptbasedcookingingredients.dto;

public record ChatGptChoice(
        ChatGptMessage message
) {}
```

### 5. ChatGptResponseFormat

**Purpose**: Specifies the response format for the API.

**Code Example:**
```java
package com.example.chatgptbasedcookingingredients.dto;

public record ChatGptResponseFormat(
        String type
) {}
```

**Key Points:**
- **Request DTOs**:
    - `ChatGptRequest`: Combines the model, messages, and response format.
    - `ChatGptMessage`: Holds the role (`user`) and content (the prompt).
- **Response DTOs**:
    - `ChatGptResponse`: Represents the entire API response.
    - `ChatGptChoice`: Extracts the response message.
- **Clean Structure**: Using records simplifies the code and makes DTOs immutable.



## 10. Testing with Postman

### 1. Request Setup

- **Method**: POST
- **URL**: `http://localhost:8080/ingredients`

### 2. Headers

- **Key**: `Content-Type`
- **Value**: `application/json`

### 3. Request Body

**Example Input:**
```json
{
    "ingredient": "cheese"
}
```

### 4. Response

**Example Output:**
```json
{
  "ingredient": "cheese",
  "classification": "regular"
}
```

**Key Points:**
- Use `Content-Type: application/json` in the headers to ensure the request body is interpreted correctly.
- The API will classify the ingredient and return both the input and its classification.
- Ensure your application is running on the correct port (`8080` by default).



## 11. Error Handling

### Common Errors and Fixes

1. **400 Bad Request**
    - **Cause**: The ChatGPT API requires the prompt to include "Respond in JSON format."
    - **Fix**: Ensure the prompt explicitly requests a JSON response.

2. **500 Internal Server Error**
    - **Cause**: Issues such as null values, API key misconfiguration, or response parsing errors.
    - **Fix**:
        - Check your API key configuration.
        - Wrap parsing logic in a try-catch block.

3. **Port Conflict**
    - **Cause**: Another process is already using the default port (`8080`).
    - **Fix**: Kill the conflicting process or change the port in 
      **application.properties:** `server.port=8081`

**Key Points:**
- Always include "Respond in JSON format" in the API prompt to ensure valid responses.
- Handle potential JSON parsing errors with a try-catch block in the service layer.
- Monitor the application logs for detailed error messages to diagnose issues.



## EXTRA: How to Kill a Process on a Port

If another process is using the default port (`8080`), follow these steps to free the port:

### On Mac/Linux:

1. **Find the Process ID (PID)**:
   Open the terminal and run:
   ```xml
   lsof -i :8080
   ```
    **This lists the process using port 8080. Example Output:**
    ```sql
    COMMAND   PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
    java     1234 user   45u  IPv6 12345      0t0  TCP *:8080 (LISTEN)
    ```

2. **Kill the Process: Use the kill command to stop the process:**
    ```xml
    kill -9 <PID> // Replace <PID> with the process ID from the previous step (e.g., 1234).
    ```