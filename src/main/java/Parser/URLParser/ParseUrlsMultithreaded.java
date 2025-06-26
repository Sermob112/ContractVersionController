
package Parser.URLParser;

import Parser.URLParser.ParseUrls;
import Parser.URLParser.WebDriverFactory;
import org.openqa.selenium.WebDriver;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParseUrlsMultithreaded {
    private final AtomicInteger completedRecords = new AtomicInteger(0);
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private int totalRecords = 0;
    private int pagesToProcess = 0;
    private final String baseUrl;
    private final String outputFilePath;
    private final int threadCount;

    public ParseUrlsMultithreaded(String baseUrl, String outputFilePath, int threadCount, JProgressBar progressBar, JLabel statusLabel) {
        this.baseUrl = baseUrl;
        this.outputFilePath = outputFilePath;
        this.threadCount = threadCount;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    public boolean processAllPagesMultithreaded() {
        int totalRecords = getTotalRecordCountFromSite();
        if (totalRecords == 0) {
            updateStatus("Не удалось получить количество записей с сайта.");
            return false;
        }

        File file = new File(outputFilePath);
        if (!file.exists()) {
            updateStatus("Файл ссылок не существует — будет выполнен парсинг.");
        } else if (file.length() == 0) {
            updateStatus("Файл ссылок пуст — будет выполнен парсинг.");
        } else {
            int existingLinks = getExistingLinkCount();
            if (existingLinks == totalRecords) {
                updateStatus("Файл уже содержит все актуальные ссылки — парсинг не требуется.");
                return false;
            }
        }


        pagesToProcess = (int) Math.ceil(totalRecords / 50.0);

        SwingUtilities.invokeLater(() -> {
            progressBar.setMinimum(0);
            progressBar.setMaximum(totalRecords);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
        });

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 1; i <= pagesToProcess; i++) {
            final int pageNumber = i;
            executor.submit(() -> processPage(pageNumber));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus("Многопоточность была прервана.");
        }

        return true;
    }

    private int getTotalRecordCountFromSite() {
        WebDriver driver = WebDriverFactory.create();
        try {
            driver.get(baseUrl);
            ParseUrls parser = new ParseUrls(driver);
            return parser.getTotalRecordsCount();
        } catch (Exception e) {
            updateStatus("Ошибка при получении количества записей: " + e.getMessage());
            return 0;
        } finally {
            driver.quit();
        }
    }

    private int getExistingLinkCount() {
        File file = new File(outputFilePath);
        if (!file.exists()) {
            return 0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int count = 0;
            while (reader.readLine() != null) {
                count++;
            }
            return count;
        } catch (IOException e) {
            updateStatus("Ошибка при чтении файла ссылок: " + e.getMessage());
            return 0;
        }
    }

    private void processPage(int pageNumber) {
        WebDriver driver = WebDriverFactory.create();
        try {
            String pageUrl = ParseUrls.replacePageNumberInUrl(baseUrl, pageNumber);
            driver.get(pageUrl);

            ParseUrls parser = new ParseUrls(driver);
            List<String> links = parser.parseContractLinksFromPage(pageUrl);

            if (links.isEmpty()) {
                updateStatus("[!] Страница " + pageNumber + " — ссылок нет.");
            } else {
                parser.saveUniqueLinksToFile(links, outputFilePath);
                int newTotal = completedRecords.addAndGet(links.size()); // ✅ увеличиваем по количеству ссылок
                updateStatus("[✓] Страница " + pageNumber + " — сохранено " + links.size() + " ссылок.");

                // Обновляем прогресс
                SwingUtilities.invokeLater(() -> progressBar.setValue(newTotal));
            }
        } catch (Exception e) {
            updateStatus("Ошибка на странице " + pageNumber + ": " + e.getMessage());
        } finally {
            driver.quit();
        }
    }
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
}
