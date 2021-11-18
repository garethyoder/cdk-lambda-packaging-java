package helloworld;


import helloworld.model.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.HashMap;
import java.util.function.Function;

@SpringBootApplication
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Value("${amazon.aws.accesskey:}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey:}")
    private String amazonAWSSecretKey;

    private final DynamoDbClient client;

    public App(DynamoDbClient client) {
        this.client = client;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public Function<Message<String>, String> save() {

        return name -> {

            HashMap<String, AttributeValue> itemValues = new HashMap<String,AttributeValue>();

            // Add all content to the table
            itemValues.put("pk", AttributeValue.builder().s("Func3").build());
            itemValues.put("sk", AttributeValue.builder().s("2021").build());
            itemValues.put("data", AttributeValue.builder().s(name.getPayload()).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(Entity.TABLE_NAME)
                    .item(itemValues)
                    .build();

            try {
                client.putItem(request);
                return "Successfully put item into " + Entity.TABLE_NAME;

            } catch (ResourceNotFoundException e) {
                LOGGER.error("Error: The Amazon DynamoDB table {} can't be found.", Entity.TABLE_NAME);
                return "Error";
            } catch (DynamoDbException e) {
                LOGGER.error("Exception: The Amazon DynamoDB table can't be updated. {}", e.getMessage());
                return "Error";
            }
        };
    }


}
