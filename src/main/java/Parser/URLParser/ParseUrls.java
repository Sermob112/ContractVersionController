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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParseUrls {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String outputFilePath = "contract_links.txt"; // Путь к файлу

    public ParseUrls(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public List<String> parseAllContractLinks(String baseUrl) {
        List<String> allLinks = new ArrayList<>();
        String firstPageUrl = replacePageNumberInUrl(baseUrl, 1); // Заменяем или добавляем pageNumber=1
        driver.get(firstPageUrl);

        int currentPage = 1;
        int totalRecords = getTotalRecordsCount();
        int maxPages = (int) Math.ceil((double) totalRecords / 50);
        System.out.println("Total records: " + totalRecords + ", max pages: " + maxPages);

        while (currentPage <= maxPages) {
            String pageUrl = replacePageNumberInUrl(baseUrl, currentPage); // Меняем номер страницы
            System.out.println("Processing page: " + currentPage + " - " + pageUrl);

            List<String> pageLinks = parseContractLinksFromPage(pageUrl);
            if (pageLinks.isEmpty()) {
                System.err.println("No links found on page " + currentPage);
                break;
            }

            allLinks.addAll(pageLinks);
            System.out.println("Found " + pageLinks.size() + " links on this page");
            System.out.println("Total links collected: " + allLinks.size());

            currentPage++;

            try {
                Thread.sleep(1000); // Задержка между страницами
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return allLinks;
    }

    /**
     * Заменяет параметр pageNumber в URL или добавляет его, если его нет.
     */
    private String replacePageNumberInUrl(String url, int newPageNumber) {
        if (url.contains("pageNumber=")) {
            // Если pageNumber уже есть, заменяем его значение
            return url.replaceAll("pageNumber=\\d+", "pageNumber=" + newPageNumber);
        } else {
            // Если нет, добавляем параметр
            return url + (url.contains("?") ? "&" : "?") + "pageNumber=" + newPageNumber;
        }
    }

    private int getTotalRecordsCount() {
        try {
            WebElement totalElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.d-flex div.search-results__total")
            ));

            String totalText = totalElement.getText()
                    .replaceAll("[^0-9]", "")
                    .trim();

            return Integer.parseInt(totalText);
        } catch (Exception e) {
            System.err.println("Could not determine total records count: " + e.getMessage());
            return 0;
        }
    }

    private List<String> parseContractLinksFromPage(String pageUrl) {
        List<String> links = new ArrayList<>();

        try {
            driver.get(pageUrl);

            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("div.registry-entry__header-mid__number a[target='_blank']")
            ));

            List<WebElement> contractElements = driver.findElements(
                    By.cssSelector("div.registry-entry__header-mid__number a[target='_blank']")
            );

            for (WebElement element : contractElements) {
                String href = element.getAttribute("href");
                if (href != null && !href.isEmpty()) {
                    links.add(href);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing page " + pageUrl + ": " + e.getMessage());
        }

        return links;
    }

    public void saveUniqueLinksToFile(List<String> links) {
        try {
            // Создаём файл если его нет
            Path path = Paths.get(outputFilePath);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            // Читаем существующие ссылки из файла
            Set<String> existingLinks = new HashSet<>(Files.readAllLines(path));

            // Фильтруем только новые уникальные ссылки
            Set<String> newUniqueLinks = new HashSet<>(links);
            newUniqueLinks.removeAll(existingLinks);

            // Дописываем новые ссылки в файл
            if (!newUniqueLinks.isEmpty()) {
                Files.write(path, newUniqueLinks, StandardOpenOption.APPEND);
                System.out.println("Added " + newUniqueLinks.size() + " new links to file");
            } else {
                System.out.println("No new links to add");
            }
        } catch (IOException e) {
            System.err.println("Error saving links to file: " + e.getMessage());
        }
    }


}