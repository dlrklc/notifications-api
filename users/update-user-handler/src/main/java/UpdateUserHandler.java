import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import java.util.HashMap;
import java.util.Map;


public class UpdateUserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    // DynamoDB client
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private static final String TABLE_NAME = "users";  // DynamoDB table name
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper for JSON parsing

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        // Extract path parameter userId from the event
        String userId = event.getPathParameters().get("userId");  // Extract userId path parameter
        if (userId == null || userId.isEmpty()) {
            return createResponse(400, "{\"message\": \"userId must be provided\"}");
        }

        String requestBody = event.getBody();  // Extract the body of the request

        String phoneNumber = extractPhoneNumber(requestBody);
        String email = extractEmail(requestBody);

        if (email == null && phoneNumber == null) {
            return createResponse(400, "{\"message\": \"Email or phone number are required\"}");
        }

        try {
            updateUserInDynamoDB(userId, phoneNumber, email);
            return createResponse(200, "{\"message\": \"User updated successfully!\"}");
        } catch (DynamoDbException e) {
            e.printStackTrace();
            return createResponse(500, "{\"message\": \"Failed to update user. Error: " + e.getMessage() + "\"}");
        }
    }

    // Method to update user in DynamoDB
    private void updateUserInDynamoDB(String userId, String phoneNumber, String email) {

        // Create the UpdateItemRequest
        Map<String, AttributeValue> updatedValues = new HashMap<>();
        String updateExpression = "SET ";

        // Conditionally add email to update request if it's not null
        if (email != null) {
            updateExpression += "email = :email";
            updatedValues.put(":email", AttributeValue.builder().s(email).build());
        }

        // Conditionally add phoneNumber to update request if it's not null
        if (phoneNumber != null) {
            updateExpression += ", phoneNumber = :phone";
            updatedValues.put(":phoneNumber", AttributeValue.builder().s(phoneNumber).build());
        }

        // Create the UpdateItemRequest
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("userId", AttributeValue.builder().s(userId).build()))  // Primary key (userId)
                .updateExpression(updateExpression)  // Update expression
                .expressionAttributeValues(updatedValues) // Values to update
                .build();

        // Execute the update request
        UpdateItemResponse updateItemResponse = dynamoDbClient.updateItem(updateItemRequest);

        //System.out.println("Update response: " + updateItemResponse);
    }

    // Helper method to create an API Gateway response
    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    // Method to safely extract phoneNumber from the request body using Jackson
    private String extractPhoneNumber(String requestBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            return jsonNode.has("phoneNumber") ? jsonNode.get("phoneNumber").asText() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if JSON parsing fails or field is missing
        }
    }

    // Method to safely extract email from the request body using Jackson
    private String extractEmail(String requestBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            return jsonNode.has("email") ? jsonNode.get("email").asText() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if JSON parsing fails or field is missing
        }
    }
}
