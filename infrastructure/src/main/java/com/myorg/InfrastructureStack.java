package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.apigatewayv2.*;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegrationProps;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.core.BundlingOutput.ARCHIVED;

public class InfrastructureStack extends Stack {
    public InfrastructureStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public InfrastructureStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Map<String, String> environmentVariables = new HashMap<>();
        // environmentVariables.put("FUNC3_ACCESS_KEY", System.getenv("AWS_ACCESS_KEY"));
        // environmentVariables.put("FUNC3_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY"));
        environmentVariables.put("TEST", "test");
        environmentVariables.put("spring_cloud_function_definition", "get");

        List<String> functionOnePackagingInstructions = Arrays.asList(
                "-c",
                "cd FunctionOne " +
                "&& mvn clean install -P native -D skipTests " +
                "&& cp /asset-input/FunctionOne/target/function.zip /asset-output/"
        );

        List<String> functionTwoPackagingInstructions = Arrays.asList(
                "-c",
                "cd FunctionTwo " +
                "&& mvn clean install -P native -D skipTests " +
                "&& cp /asset-input/FunctionTwo/target/function.zip /asset-output/"
        );

        List<String> functionThreePackagingInstructions = Arrays.asList(
                "-c",
                "cd FunctionThree " +
                        "&& mvn clean install -P native -D skipTests " +
                        "&& cp /asset-input/FunctionThree/target/function.zip /asset-output/"
        );

        BundlingOptions.Builder builderOptions = BundlingOptions.builder()
                .command(functionOnePackagingInstructions)
                .image(DockerImage.fromRegistry("marksailes/al2-graalvm:al2-21.2.0"))
                .volumes(singletonList(
                        DockerVolume.builder()
                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                .containerPath("/root/.m2/")
                                .build()
                ))
                .user("root")
                .outputType(ARCHIVED);

//        Function functionOne = new Function(this, "FunctionOne", FunctionProps.builder()
//                .runtime(Runtime.PROVIDED_AL2)
//                .code(Code.fromAsset("../software/", AssetOptions.builder()
//                        .bundling(builderOptions
//                                .command(functionOnePackagingInstructions)
//                                .build())
//                        .build()))
//                .functionName("FunctionOneUppercase")
//                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
//                .memorySize(256)
//                .timeout(Duration.seconds(10))
//                .logRetention(RetentionDays.ONE_WEEK)
//                .build());
//
//        Function functionTwo = new Function(this, "FunctionTwo", FunctionProps.builder()
//                .runtime(Runtime.PROVIDED_AL2)
//                .code(Code.fromAsset("../software/", AssetOptions.builder()
//                        .bundling(builderOptions
//                                .command(functionTwoPackagingInstructions)
//                                .build())
//                        .build()))
//                .functionName("FunctionTwoLowercase")
//                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
//                .memorySize(256)
//                .timeout(Duration.seconds(10))
//                .logRetention(RetentionDays.ONE_WEEK)
//                .build());

        Role functionThreeRole = new Role(this, "functionThreeRole", RoleProps.builder()
                .roleName("functionThreeRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(Arrays.asList(
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole"),
                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBFullAccess")
                ))
                .build());


        Function functionThree = new Function(this, "FunctionThree", FunctionProps.builder()
                .runtime(Runtime.PROVIDED_AL2)
                .code(Code.fromAsset("../software/", AssetOptions.builder()
                        .bundling(builderOptions
                                .command(functionThreePackagingInstructions)
                                .build())
                        .build()))
                .functionName("FunctionThreeDynamo")
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .memorySize(256)
                .timeout(Duration.seconds(10))
                .logRetention(RetentionDays.ONE_WEEK)
                .environment(environmentVariables)
                .role(functionThreeRole)
                .build());

        HttpApi httpApi = new HttpApi(this, "chescolp-api", HttpApiProps.builder()
                .apiName("Chescolp")
                .build());

//        httpApi.addRoutes(AddRoutesOptions.builder()
//                .path("/one")
//                .methods(singletonList(HttpMethod.POST))
//                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
//                        .handler(functionOne)
//                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
//                        .build()))
//                .build());
////
//        httpApi.addRoutes(AddRoutesOptions.builder()
//                .path("/two")
//                .methods(singletonList(HttpMethod.POST))
//                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
//                        .handler(functionTwo)
//                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
//                        .build()))
//                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/three")
                .methods(singletonList(HttpMethod.POST))
                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                        .handler(functionThree)
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()))
                .build());

        new CfnOutput(this, "HttApi", CfnOutputProps.builder()
                .description("Url for Http Api")
                .value(httpApi.getApiEndpoint())
                .build());
    }
}
