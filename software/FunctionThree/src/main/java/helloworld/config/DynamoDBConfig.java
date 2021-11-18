package helloworld.config;

import helloworld.App;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration(proxyBeanMethods = false)
public class DynamoDBConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Value("${amazon.aws.accesskey:''}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey:''}")
    private String amazonAWSSecretKey;

    @Value("${amazon.aws.test:''}")
    private String test;

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        LOGGER.info("DynamoDB access key: " + test);
        DynamoDbEnhancedClient client;
        if (Strings.isBlank(amazonAWSAccessKey)) {
            LOGGER.info("DynamoDB initialized with IAM credentials");
            client = DynamoDbEnhancedClient.create();
        } else {
            LOGGER.info("DynamoDB initialized with static credentials");
            client = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(getDynamoDbClient())
                    .build();
        }

        return client;
    }

    @Bean
    public DynamoDbClient DynamoDbClient() {
        return getDynamoDbClient();
    }

    private DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }

    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(amazonAWSAccessKey, amazonAWSSecretKey));
    }
}
