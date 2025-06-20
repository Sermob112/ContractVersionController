package Parser.URLParser;

import Webdriver.Implementations.RandomUserAgent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import Webdriver.Implementations.ChromeDriverSetup;
public class WebDriverFactory {

    public static WebDriver create() {
        String userAgent = RandomUserAgent.getRandomUserAgent();
        ChromeDriverSetup driverSetup = new ChromeDriverSetup(userAgent);
        WebDriver driver = driverSetup.setupDriver();

        return  driver;
    }
}