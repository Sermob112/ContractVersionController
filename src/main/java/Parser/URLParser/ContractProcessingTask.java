package Parser.URLParser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContractProcessingTask implements Runnable {
    private final String contractLink;
    private final String outputFilePath;
    private final String processedLinksFilePath;

    public ContractProcessingTask(String contractLink, String outputFilePath, String processedLinksFilePath) {
        this.contractLink = contractLink;
        this.outputFilePath = outputFilePath;
        this.processedLinksFilePath = processedLinksFilePath;
    }

    @Override
    public void run() {
        WebDriver driver = WebDriverFactory.create(); // нужно реализовать фабрику WebDriver
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            System.out.println("Processing: " + contractLink);
            driver.get(contractLink);

            WebElement tabsContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.container div.tabsNav")
            ));
            List<WebElement> tabs = tabsContainer.findElements(By.cssSelector("a.tabsNav__item"));
            String eventJournalLink = null;

            for (WebElement tab : tabs) {
                if (tab.getText().contains("Журнал версий")) {
                    eventJournalLink = tab.getAttribute("href");
                    break;
                }
            }

            if (eventJournalLink == null && !tabs.isEmpty()) {
                String firstTabHref = tabs.get(0).getAttribute("href");
                eventJournalLink = generateVersionJournalLink(firstTabHref);
            }

            if (eventJournalLink != null) {
                synchronized (VersionUrlParser.class) {
                    appendToFile(outputFilePath, eventJournalLink);
                }
                System.out.println("✔ Found: " + eventJournalLink);
            }

            synchronized (VersionUrlParser.class) {
                appendToFile(processedLinksFilePath, contractLink);
            }

        } catch (Exception e) {
            System.err.println("Error processing: " + contractLink + " -> " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private String generateVersionJournalLink(String tabHref) {
        Pattern pattern = Pattern.compile("reestrNumber=([^&]+).*contractInfoId=([^&]+)");
        Matcher matcher = pattern.matcher(tabHref);
        if (matcher.find()) {
            return String.format("https://zakupki.gov.ru/epz/contract/contractCard/journal-version.html?reestrNumber=%s&contractInfoId=%s",
                    matcher.group(1), matcher.group(2));
        }
        return null;
    }

    private void appendToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, (content + System.lineSeparator()).getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
