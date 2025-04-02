import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Map;

public class DeleteUserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    // DynamoDB client
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private static final String TABLE_NAME = "users";  // DynamoDB table name

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        // Extract path parameter userId from the event
        String userId = event.getPathParameters().get("userId");  // Extract userId path parameter
        if (userId == null || userId.isEmpty()) {
            return createResponse(400, "{\"message\": \"userId must be provided\"}");
        }

        // Delete user from DynamoDB
        try {
            deleteUserFromDynamoDB(userId);
            return createResponse(200, "{\"message\": \"User deleted successfully!\"}");
        } catch (DynamoDbException e) {
            e.printStackTrace();
            return createResponse(500, "{\"message\": \"Failed to delete user. Error: " + e.getMessage() + "\"}");
        }
    }

    // Method to delete user from DynamoDB
    private void deleteUserFromDynamoDB(String userId) {
        // Create the DeleteItemRequest
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("userId", AttributeValue.builder().s(userId).build())) // Primary key (userId)
                .build();

        // Execute the delete request
        DeleteItemResponse deleteItemResponse = dynamoDbClient.deleteItem(deleteItemRequest);

        //System.out.println("Delete response: " + deleteItemResponse);
    }

    // Helper method to create an API Gateway response
    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }
}
