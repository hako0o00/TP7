import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import java.io.File;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "feature",
        plugin = {
                "pretty",
                "json:build/cucumber/cucumber.json",
                "html:build/cucumber/cucumber.html"
        }
)

public class ExampleTest {
    
}
