package Parser.URLParser;

import Webdriver.Implementations.RandomUserAgent;
import org.openqa.selenium.WebDriver;
import Webdriver.Implementations.ChromeDriverSetup;
public class WebDriverFactory {

    public static WebDriver create() {
        String userAgent = RandomUserAgent.getRandomUserAgent();
        ChromeDriverSetup driverSetup = new ChromeDriverSetup(userAgent);
        WebDriver driver = driverSetup.setupDriver();

        return  driver;
    }
}