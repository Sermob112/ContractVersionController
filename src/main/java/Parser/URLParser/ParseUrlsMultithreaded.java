
package Parser.URLParser;

import Parser.URLParser.ParseUrls;
import Parser.URLParser.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import java.util.*;
import java.util.concurrent.*;

public class ParseUrlsMultithreaded {

    private final String baseUrl;
    private final String outputFilePath;
    private final int threadCount;

    public ParseUrlsMultithreaded(String baseUrl, String outputFilePath, int threadCount) {
        this.baseUrl = baseUrl;
        this.outputFilePath = outputFilePath;
        this.threadCount = threadCount;
    }

    public void processAllPagesMultithreaded() {
        int pagesToProcess = getPageCountFromSite();
        if (pagesToProcess == 0) {
            System.err.println("Нет доступных страниц для обработки.");
            return;
        }

        System.out.println("Будет обработано страниц: " + pagesToProcess);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 1; i <= pagesToProcess; i++) {
            final int pageNumber = i;
            futures.add(executor.submit(() -> processPage(pageNumber)));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Многопоточность была прервана.");
        }
    }

    private int getPageCountFromSite() {
        WebDriver driver = WebDriverFactory.create();
        try {
            driver.get(baseUrl);
            ParseUrls parser = new ParseUrls(driver);
            int totalRecords = parser.getTotalRecordsCount();
            return (int) Math.ceil(totalRecords / 50.0); // 50 записей на страницу
        } catch (Exception e) {
            System.err.println("Ошибка при получении количества записей: " + e.getMessage());
            return 0;
        } finally {
            driver.quit();
        }
    }

    private void processPage(int pageNumber) {
        WebDriver driver = WebDriverFactory.create();
        try {
            driver.get(baseUrl);
            ParseUrls parser = new ParseUrls(driver);
            String pageUrl = parser.replacePageNumberInUrl(baseUrl, pageNumber);
            driver.get(pageUrl);
            List<String> links = parser.parseContractLinksFromPage(pageUrl);

            if (links.isEmpty()) {
                System.out.println("[!] Страница " + pageNumber + " — ссылок нет.");
            } else {
                parser.saveUniqueLinksToFile(links, outputFilePath);
                System.out.println("[✓] Страница " + pageNumber + " — сохранено " + links.size() + " ссылок.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка на странице " + pageNumber + ": " + e.getMessage());
        } finally {
            driver.quit(); // обязательно закрыть драйвер после обработки
        }
    }
}