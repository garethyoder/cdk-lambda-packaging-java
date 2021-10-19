package helloworld;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.FunctionType;
import org.springframework.cloud.function.context.FunctionalSpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import java.util.function.Function;

@SpringBootConfiguration
public class App implements ApplicationContextInitializer<GenericApplicationContext> {

    public static void main(String[] args) {
        FunctionalSpringApplication.run(App.class, args);
    }

    public Function<String, String> lowercase() {
        return value -> value.toLowerCase();
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        context.registerBean("lowercase", FunctionRegistration.class,
                () -> new FunctionRegistration<>(lowercase())
                        .type(FunctionType.from(String.class).to(String.class)));
    }

}
