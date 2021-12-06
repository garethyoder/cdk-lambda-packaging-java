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
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Locale;

@Configuration(proxyBeanMethods = false)
public class DynamoDBConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    @Value("${amazon.aws.accesskey:}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey:}")
    private String amazonAWSSecretKey;

    @Value("${amazon.aws.region:us-east-1}")
    private String region;

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        LOGGER.info("DynamoDB region: " + region);
        LOGGER.info("DynamoDB key: " + amazonAWSAccessKey.toLowerCase(Locale.ROOT));
        DynamoDbEnhancedClient client;
        if (Strings.isBlank(amazonAWSAccessKey)) {
            LOGGER.info("DynamoDB initialized with IAM credentials");
            client = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(getDynamoDbClient())
                    .build();
        } else {
            LOGGER.info("DynamoDB initialized with static credentials");
            client = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(getDynamoDbClient())
                    .build();
        }

        return client;
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return getDynamoDbClient();
    }

    private DynamoDbClient getDynamoDbClient() {
        DynamoDbClient client;
        if (Strings.isBlank(amazonAWSAccessKey)) {
            LOGGER.info("DynamoDB initialized with IAM credentials");
            client = DynamoDbClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .httpClient(UrlConnectionHttpClient.builder().build())
                    .build();
        } else {
            LOGGER.info("DynamoDB initialized with static credentials");
            client = DynamoDbClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(awsCredentialsProvider())
                    .httpClient(UrlConnectionHttpClient.builder().build())
                    .build();
        }

        return client;
    }

    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(amazonAWSAccessKey, amazonAWSSecretKey));
    }
}
