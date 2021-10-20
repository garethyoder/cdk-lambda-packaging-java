package helloworld;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import helloworld.model.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.UUID;
import java.util.function.Function;

@SpringBootApplication
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Value("${amazon.aws.accesskey:}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey:}")
    private String amazonAWSSecretKey;

    public App() {
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public Function<Message<String>, Entity> save() {

        return name -> {

            DynamoDBMapper mapper = initDynamoDbClient();

            Entity entity = new Entity(UUID.randomUUID().toString(), "fullName", name.getPayload());

            mapper.save(entity);

            return entity;
        };
    }

    private DynamoDBMapper initDynamoDbClient() {
        AmazonDynamoDB dynamoDBClient;
        LOGGER.info("DynamoDB initialized with static credentials");
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials()))
                .withRegion(Regions.US_EAST_1)
                .build();

        return new DynamoDBMapper(dynamoDBClient);
    }

    private AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }

}
