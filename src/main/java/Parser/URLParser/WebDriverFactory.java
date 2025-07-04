package Parser.URLParser;

import Webdriver.Implementations.RandomUserAgent;
import org.openqa.selenium.WebDriver;
import Webdriver.Implementations.ChromeDriverSetup;

import java.util.ArrayList;
import java.util.List;

public class WebDriverFactory {
    private static final List<WebDriver> drivers = new ArrayList<>();
    public static WebDriver create() {

        String userAgent = RandomUserAgent.getRandomUserAgent();
        ChromeDriverSetup driverSetup = new ChromeDriverSetup(userAgent);
        WebDriver driver = driverSetup.setupDriver();

        return  driver;
    }
    public static void closeAllDrivers() {
        synchronized (drivers) {
            for (WebDriver driver : drivers) {
                try {
                    if (driver != null) {
                        driver.quit();
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при закрытии драйвера: " + e.getMessage());
                }
            }
            drivers.clear();
        }
    }



    public void close() {
        closeAllDrivers();
    }

    // Регистрируем shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(WebDriverFactory::closeAllDrivers));
    }
}