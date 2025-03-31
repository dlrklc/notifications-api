package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

public class AddUserLambda implements RequestHandler<Map<String, Object>, String> {
    // DynamoDB client
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private static final String TABLE_NAME = "users";  // DynamoDB table name

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {

        String body = (String) event.get("body");

        // Parse the body into a Map
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> bodyMap = null;
        try {
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Generate userId using UUID (v4)
        String userId = UUID.randomUUID().toString();

        // Extract data from the input map
        String username = (String)bodyMap.get("username");
        String email = (String)bodyMap.get("email");
        String phoneNumber = (String)bodyMap.get("phoneNumber");

        // Validate input
        if (userId == null || email == null || phoneNumber == null || username == null) {
            return "Error: Missing user data!";
        }

        // Create a map of attributes to be inserted into DynamoDB
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("userId", AttributeValue.builder().s(userId).build());
        item.put("username", AttributeValue.builder().s(username).build());
        item.put("email", AttributeValue.builder().s(email).build());
        item.put("phoneNumber", AttributeValue.builder().s(phoneNumber).build());

        // Create a PutItemRequest
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        try {
            // Put the item into DynamoDB
            PutItemResponse putItemResponse = dynamoDbClient.putItem(putItemRequest);
            // Return success message
            return "User added successfully!";
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return "Error: Could not add user!";
        }
    }
}
