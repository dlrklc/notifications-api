package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetUsersLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final String TABLE_NAME = "users";

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        List<Map<String, String>> users = new ArrayList<>();

        // Construct scan request
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        try {
            // Perform the scan
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            // Process the results
            for (Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> item : scanResponse.items()) {
                Map<String, String> user = new HashMap<>();
                if (item.containsKey("userId")) {
                    user.put("userId", item.get("userId").s());
                }
                if (item.containsKey("email")) {
                    user.put("email", item.get("email").s());
                }
                if (item.containsKey("phoneNumber")) {
                    user.put("phoneNumber", item.get("phoneNumber").s());
                }
                if (item.containsKey("username")) {
                    user.put("username", item.get("username").s());
                }
                users.add(user);
            }

            // Check if any users are returned
            if (users.isEmpty()) {
                return createResponse(404, "{\"message\": \"No users found\"}");
            }

        } catch (Exception e) {
            return createResponse(500, "{\"message\": \"Failed to retrieve users. Error: " + e.getMessage() + "\"}");
        }

        return createResponse(200, "{\"users\": " + users + "}");
    }

    // Helper method to create an API Gateway response
    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }
}
