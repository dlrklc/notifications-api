package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetUsersLambda implements RequestHandler<Object, List<Map<String, String>>> {

    private static final String TABLE_NAME = "users";

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

    @Override
    public List<Map<String, String>> handleRequest(Object input, Context context) {
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
        } catch (Exception e) {
            System.err.println("Error scanning the DynamoDB table: " + e.getMessage());
        }

        return users;
    }
}
