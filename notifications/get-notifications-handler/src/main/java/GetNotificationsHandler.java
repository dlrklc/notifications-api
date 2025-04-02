import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetNotificationsHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final String TABLE_NAME = "notifications";

    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        List<Map<String, String>> notifications = new ArrayList<>();

        // Construct scan request
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        try {
            // Perform the scan
            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            // Process the results
            for (Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> item : scanResponse.items()) {
                Map<String, String> notification = new HashMap<>();
                if (item.containsKey("notificationId")) {
                    notification.put("notificationId", item.get("notificationId").s());
                }
                if (item.containsKey("message")) {
                    notification.put("message", item.get("message").s());
                }
                if (item.containsKey("createdAt")) {
                    notification.put("createdAt", item.get("createdAt").s());
                }
                if (item.containsKey("updatedAt")) {
                    notification.put("updatedAt", item.get("updatedAt").s());
                }
                notifications.add(notification);
            }

            // Check if any users are returned
            if (notifications.isEmpty()) {
                return createResponse(404, "{\"message\": \"No notifications found\"}");
            }

        } catch (Exception e) {
            return createResponse(500, "{\"message\": \"Failed to retrieve notifications. Error: " + e.getMessage() + "\"}");
        }

        return createResponse(200, "{\"notifications\": " + notifications + "}");
    }

    // Helper method to create an API Gateway response
    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

}
