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
                WebElement mainInfoCard = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div.cardMainInfo.row")));

                // Извлечение основных данных с обработкой возможных ошибок для каждого поля
                safelyPutContractInfo(contractInfo, "Номер контракта", mainInfoCard, "span.cardMainInfo__content");
                safelyPutContractInfo(contractInfo, "Статус", mainInfoCard, "span.cardMainInfo__state");
                safelyPutContractInfo(contractInfo, "Заказчик", mainInfoCard, "span.cardMainInfo__content a");
                safelyPutContractInfo(contractInfo, "Цена контракта", mainInfoCard, "span.cardMainInfo__content.cost");
                safelyPutContractInfo(contractInfo, "Объекты закупки", mainInfoCard, "span.text-break.d-block");

                // Даты
                safelyPutContractInfo(contractInfo, "Дата заключения", mainInfoCard,
                        "div.cardMainInfo__section:nth-of-type(1) span.cardMainInfo__content");
                safelyPutContractInfo(contractInfo, "Срок исполнения", mainInfoCard,
                        "div.cardMainInfo__section:nth-of-type(2) span.cardMainInfo__content");

                // Дополнительные поля, которые могут отсутствовать
                safelyPutContractInfo(contractInfo, "Дата размещения", mainInfoCard,
                        "div.cardMainInfo__section:nth-of-type(3) span.cardMainInfo__content");
                safelyPutContractInfo(contractInfo, "Дата обновления", mainInfoCard,
                        "div.cardMainInfo__section:nth-of-type(4) span.cardMainInfo__content");

            } catch (TimeoutException e) {
                System.err.println("Основная карточка контракта не найдена: " + contractUrl);
            }

            // Сохраняем данные по URL контракта, даже если собраны не все поля
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