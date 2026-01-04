import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import java.io.File;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "Features",
        plugin = {
                "pretty",
                "html:build/reports/cucumber/index.html",
                "json:build/reports/cucumber/report.json"
        }
)
public class ExampleTest {

    static {
        // Make sure the folder exists before Cucumber tries to write index.html / report.json
        new File("build/reports/cucumber").mkdirs();
    }
}
