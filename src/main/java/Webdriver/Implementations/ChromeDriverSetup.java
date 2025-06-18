package Webdriver.Implementations;


import Webdriver.interfaces.DriverSetup;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverSetup implements DriverSetup {
    private final String userAgent;

    public ChromeDriverSetup(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public WebDriver setupDriver() {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.setExperimentalOption("detach", false); // Закрывать браузер при завершении

        // Добавляем динамически заданный user-agent
        if (userAgent != null && !userAgent.isEmpty()) {
            options.addArguments("user-agent=" + userAgent);
        }

        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        return new ChromeDriver(options);
    }


}
