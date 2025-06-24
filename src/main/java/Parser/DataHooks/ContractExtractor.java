package Parser.DataHooks;

import Parser.URLParser.WebDriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContractExtractor {

    private final String inputFilePath = "contract_detail_links.txt";
    private final int threadCount;
    private final Map<String, Map<String, String>> contractsData = new ConcurrentHashMap<>();

    public ContractExtractor(int threadCount) {
        this.threadCount = threadCount;
    }

    public void extractAllContracts() {
        List<String> contractLinks = readLinksFromFile();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : contractLinks) {
            executor.submit(() -> extractContractData(link));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread pool interrupted.");
        }

        printCollectedData();
    }

    private void extractContractData(String contractUrl) {
        WebDriver driver = WebDriverFactory.create();
        Map<String, String> contractInfo = new HashMap<>();

        try {
            driver.get(contractUrl);

            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            try {
                // Ищем версию контракта
                WebElement versionSpan = driver.findElement(By.cssSelector("span.navBreadcrumb__text > span[data-tooltip]"));
                String contractVersion = versionSpan.getText().trim();
                contractInfo.put("Версия контракта", contractVersion);
            } catch (NoSuchElementException e) {
                System.err.println("Не удалось извлечь версию контракта: " + contractUrl);
            }

            try {
                WebElement reestrElement = driver.findElement(By.cssSelector("span.cardMainInfo__purchaseLink a"));
                String href = reestrElement.getAttribute("href");

                // Извлекаем значение параметра reestrNumber
                String reestrNumber = "";
                Pattern pattern = Pattern.compile("reestrNumber=([0-9]+)");
                Matcher matcher = pattern.matcher(href);
                if (matcher.find()) {
                    reestrNumber = matcher.group(1);
                    contractInfo.put("Реестровый номер", reestrNumber);
                }
            } catch (NoSuchElementException e) {
                System.err.println("Не удалось извлечь реестровый номер: " + contractUrl);
            }
            try {
                WebElement mainInfoCard = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div.cardMainInfo.row")));

                List<WebElement> sections = mainInfoCard.findElements(By.cssSelector(".cardMainInfo__section"));

                for (WebElement section : sections) {
                    String title = "";
                    String value = "";

                    try {
                        title = section.findElement(By.cssSelector(".cardMainInfo__title")).getText().trim();
                        value = section.findElement(By.cssSelector(".cardMainInfo__content")).getText().trim();
                    } catch (NoSuchElementException ignore) {
                    }


                    switch (title) {
                        case "Контракт":
                            contractInfo.put("Номер контракта", value);
                            break;
                        case "Заказчик":
                            contractInfo.put("Заказчик", value);
                            break;
                        case "Объекты закупки":
                            contractInfo.put("Объекты закупки", value);
                            break;
                        case "Заключение контракта":
                            contractInfo.put("Дата заключения", value);
                            break;
                        case "Срок исполнения":
                            contractInfo.put("Срок исполнения", value);
                            break;
                        case "Размещен контракт в реестре контрактов":
                            contractInfo.put("Дата размещения", value);
                            break;
                        case "Обновлен контракт в реестре контрактов":
                            contractInfo.put("Дата обновления", value);
                            break;
                    }
                }

                // Отдельно статус
                try {
                    WebElement status = mainInfoCard.findElement(By.cssSelector("span.cardMainInfo__state"));
                    contractInfo.put("Статус", status.getText().trim());
                } catch (NoSuchElementException ignore) {}

                // Отдельно цена контракта
                try {
                    WebElement price = mainInfoCard.findElement(By.cssSelector("span.cardMainInfo__content.cost"));
                    contractInfo.put("Цена контракта", price.getText().trim());
                } catch (NoSuchElementException ignore) {}

            } catch (TimeoutException e) {
                System.err.println("Основная карточка контракта не найдена: " + contractUrl);
            }

            if (!contractInfo.isEmpty()) {
                contractsData.put(contractUrl, contractInfo);
                System.out.println("Обработан контракт: " + contractUrl);
                printContractInfo(contractInfo);
            } else {
                System.err.println("Не удалось извлечь данные для контракта: " + contractUrl);
            }

        } catch (Exception e) {
            System.err.println("Общая ошибка при обработке контракта: " + contractUrl + " → " + e.getMessage());
        } finally {
            driver.quit();
        }
    }



    // Вспомогательный метод для безопасного извлечения данных
    private void safelyPutContractInfo(Map<String, String> contractInfo, String fieldName,
                                       WebElement parent, String cssSelector) {
        try {
            String value = getTextFromElement(parent, cssSelector);
            if (!value.isEmpty()) {
                contractInfo.put(fieldName, value);
            }
        } catch (Exception e) {
            System.err.println("Не удалось извлечь поле '" + fieldName + "' для контракта");
            // Можно добавить логирование при необходимости
        }
    }

    // Модифицированный метод getTextFromElement
    private String getTextFromElement(WebElement parent, String cssSelector) {
        try {
            WebElement element = parent.findElement(By.cssSelector(cssSelector));
            return element.getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
    public Map<String, Map<String, String>> getContractsData() {
        return this.contractsData;
    }


    private void printContractInfo(Map<String, String> contractInfo) {
        System.out.println("\n=== Информация о контракте ===");
        contractInfo.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("=============================\n");
    }

    private void printCollectedData() {
        System.out.println("\n=== Собраны данные по всем контрактам ===");
        contractsData.forEach((url, data) -> {
            System.out.println("\nURL: " + url);
            data.forEach((key, value) -> System.out.println(key + ": " + value));
        });
        System.out.println("\nВсего обработано контрактов: " + contractsData.size());
    }

    private List<String> readLinksFromFile() {
        try {
            return Files.readAllLines(Paths.get(inputFilePath))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла с ссылками: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}