package Parser.URLParser;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JournalLinksExtractorMultithreaded {

    private final String inputFilePath;
    private final String outputFilePath;
    private final int threadCount;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public JournalLinksExtractorMultithreaded(String inputFilePath, String outputFilePath, int threadCount,
                                              JProgressBar progressBar, JLabel statusLabel) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.threadCount = threadCount;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    public void processAllJournalPages() {
        List<String> journalLinks = readLinksFromFile();
        Set<String> extractedLinks = ConcurrentHashMap.newKeySet();
        AtomicInteger processedCount = new AtomicInteger(0);
        int total = journalLinks.size();

        updateProgress(0, total);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : journalLinks) {
            executor.submit(() -> {
                processLink(link, extractedLinks);

                int done = processedCount.incrementAndGet();
                updateProgress(done, total);
                updateStatus("Обработано журналов: " + done + " из " + total);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus("Обработка прервана!");
        }

        writeLinksToFile(new ArrayList<>(extractedLinks));
    }

    private void processLink(String link, Set<String> resultSet) {
        WebDriver driver = WebDriverFactory.create();

        try {
            driver.get(link);

            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//table[contains(@class,'table')]")));

            List<WebElement> rows = table.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));

            for (WebElement row : rows) {
                try {
                    // Ищем только <a> внутри первой <td>
                    WebElement firstCell = row.findElement(By.tagName("td"));
                    List<WebElement> links = firstCell.findElements(By.tagName("a"));

                    for (WebElement a : links) {
                        String href = a.getAttribute("href");
                        if (href != null && href.contains("/epz/contract/contractCard/common-info.html")) {
                            String fullUrl = href.startsWith("http") ? href : "https://zakupki.gov.ru" + href;
                            if (resultSet.add(fullUrl)) {
                                System.out.println("[✓] " + fullUrl);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[!] Ошибка в строке таблицы: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Не удалось обработать ссылку " + link + ": " + e.getMessage());
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

    private void updateStatus(String message) {
        System.out.println(message);
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(message));
        }
    }

    private void updateProgress(int current, int total) {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setMaximum(total);
                progressBar.setValue(current);
                progressBar.setStringPainted(true);
            });
        }
    }

}
