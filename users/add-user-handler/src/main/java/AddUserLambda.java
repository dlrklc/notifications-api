import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

public class AddUserLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    // DynamoDB client
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private static final String TABLE_NAME = "users";  // DynamoDB table name
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper for JSON parsing

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        String requestBody = event.getBody();  // Extract the body of the request

        String username = extractUsername(requestBody);
        String phoneNumber = extractPhoneNumber(requestBody);
        String email = extractEmail(requestBody);

        // Generate userId using UUID (v4)
        String userId = UUID.randomUUID().toString();

        // Validate input
        if (userId == null || email == null || phoneNumber == null || username == null) {
            return createResponse(400, "{\"message\": \"Missing user data\"}");
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
            return createResponse(200, "{\"message\": \"User added successfully!\"}");
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return createResponse(500, "{\"message\": \"Failed to add user. Error: " + e.getMessage() + "\"}");
        }
    }

    // Helper method to create an API Gateway response
    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    // Method to safely extract username from the request body using Jackson
    private String extractUsername(String requestBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            return jsonNode.has("username") ? jsonNode.get("username").asText() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if JSON parsing fails or field is missing
        }
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
