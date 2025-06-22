package Parser.URLParser;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class JournalLinksExtractorMultithreaded {

    private final String inputFilePath;
    private final String outputFilePath;
    private final int threadCount;

    public JournalLinksExtractorMultithreaded(String inputFilePath, String outputFilePath, int threadCount) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.threadCount = threadCount;
    }

    public void processAllJournalPages() {
        List<String> journalLinks = readLinksFromFile();
        Set<String> extractedLinks = ConcurrentHashMap.newKeySet();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : journalLinks) {
            executor.submit(() -> processLink(link, extractedLinks));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread pool interrupted.");
        }

        writeLinksToFile(new ArrayList<>(extractedLinks));
    }

    private void processLink(String link, Set<String> resultSet) {
        WebDriver driver = WebDriverFactory.create();

        try {
            driver.get(link);

            // Явное ожидание полной загрузки DOM
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//table[contains(@class,'table')]")));

            List<WebElement> rows = table.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

            int limit = Math.min(2, rows.size());
            for (int i = 0; i < limit; i++) {
                try {
                    WebElement row = rows.get(i);
                    WebElement linkElement = row.findElement(By.cssSelector("td a"));
                    String href = linkElement.getAttribute("href");

                    if (href != null && !href.trim().isEmpty()) {
                        resultSet.add(href);
                        System.out.println("[✓] " + href);
                    }
                } catch (Exception e) {
                    System.err.println("[!] Error extracting link from row in: " + link);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to extract links from: " + link + " → " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private List<String> readLinksFromFile() {
        try {
            return Files.readAllLines(Paths.get(inputFilePath))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void writeLinksToFile(List<String> links) {
        try {
            Path path = Paths.get(outputFilePath);
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(path, links, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved " + links.size() + " links.");
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }
}
