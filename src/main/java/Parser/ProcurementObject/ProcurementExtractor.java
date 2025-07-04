package Parser.ProcurementObject;


import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Parser.URLParser.WebDriverFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProcurementExtractor {
    private final String inputFilePath = "procurement_objects_links.txt";
    private final int threadCount;
    private final Map<String, List<Map<String, String>>> procurementData = new ConcurrentHashMap<>();
    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public ProcurementExtractor(int threadCount, JProgressBar progressBar, JLabel statusLabel) {
        this.threadCount = threadCount;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    public void extractAllProcurements() {
        List<String> procurementLinks = readLinksFromFile();
        int total = procurementLinks.size();
        AtomicInteger processed = new AtomicInteger(0);

        updateProgress(0, total);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : procurementLinks) {
            executor.submit(() -> {
                extractProcurementData(link);
                int done = processed.incrementAndGet();
                updateProgress(done, total);
                updateStatus("Обработано контрактов: " + done + " из " + total);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus("Обработка прервана");
        }

        printCollectedData();
    }

    private void extractProcurementData(String procurementUrl) {
        WebDriver driver = WebDriverFactory.create();
        List<Map<String, String>> procurementItems = new ArrayList<>();

        try {
            driver.get(procurementUrl);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            try {
                WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("table#contract_subjects")));

                List<WebElement> rows = table.findElements(By.cssSelector("tbody tr.tableBlock__row:not([id^='ktru-tr-'])"));

                for (WebElement row : rows) {
                    Map<String, String> itemData = new LinkedHashMap<>();

                    // Наименование объекта закупки и его характеристики
                    WebElement nameCell = row.findElement(By.cssSelector("td.tableBlock__col:nth-child(2)"));
                    itemData.put("Наименование", getElementTextSafely(nameCell, "div.padBtm5"));

                    // Страна происхождения (извлекаем из текста)
                    String countryText = getElementTextSafely(nameCell, "div.grey-main-light:nth-of-type(1)");
                    if (countryText.startsWith("Страна происхождения:")) {
                        itemData.put("Страна происхождения", countryText.replace("Страна происхождения:", "").trim());
                    }

                    // Характеристики
                    itemData.put("Характеристики", getElementTextSafely(nameCell, "div.grey-main-light:nth-of-type(2)"));

                    // Позиции по КТРУ, ОКПД2
                    WebElement ktruCell = row.findElement(By.cssSelector("td.tableBlock__col:nth-child(3)"));
                    String ktruText = ktruCell.getText().trim();

                    // Извлекаем код КТРУ из скобок
                    Pattern pattern = Pattern.compile("\\((\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{3})\\)");
                    Matcher matcher = pattern.matcher(ktruText);
                    if (matcher.find()) {
                        itemData.put("КТРУ", matcher.group(1));
                    } else {
                        itemData.put("КТРУ", "");
                    }

                    // Описание КТРУ (текст до переноса строки)
                    String ktruDescription = ktruText.split("\n")[0].trim();
                    itemData.put("Описание КТРУ", ktruDescription);

                    // Тип объекта закупки
                    WebElement typeCell = row.findElement(By.cssSelector("td.tableBlock__col:nth-child(4)"));
                    itemData.put("Тип объекта", typeCell.getText().trim());

                    // Количество и единицы измерения
                    WebElement quantityCell = row.findElement(By.cssSelector("td.tableBlock__col:nth-child(5)"));
                    String quantityText = quantityCell.getText().trim();
                    itemData.put("Количество", quantityText.replaceAll("[^0-9]", "")); // Оставляем только цифры
                    itemData.put("Единица измерения", quantityText.contains("ШТ") ? "ШТ" :
                            quantityText.contains("КГ") ? "КГ" :
                                    quantityText.contains("Л") ? "Л" : "Ед.");

                    // Цена за единицу
                    WebElement priceCell = row.findElement(By.cssSelector("td.tableBlock__col:nth-child(6)"));
                    itemData.put("Цена за единицу", priceCell.getText().replace("&nbsp;", "").trim());

                    // Сумма
                    WebElement sumCell = row.findElement(By.cssSelector("td.tableBlock__col:nth-child(7)"));
                    String sumText = sumCell.getText();
                    itemData.put("Сумма", sumText.split("Ставка")[0].replace("&nbsp;", "").trim());
                    itemData.put("Ставка НДС", sumText.contains("Ставка НДС") ?
                            sumText.split("Ставка НДС:")[1].trim() : "Без НДС");

                    procurementItems.add(itemData);
                }

                if (!procurementItems.isEmpty()) {
                    procurementData.put(procurementUrl, procurementItems);
                    System.out.println("Обработан контракт: " + procurementUrl);
                    printProcurementInfo(procurementItems);
                } else {
                    System.err.println("Не удалось извлечь данные для контракта: " + procurementUrl);
                }

            } catch (TimeoutException e) {
                System.err.println("Таблица объектов закупки не найдена: " + procurementUrl);
            }

        } catch (Exception e) {
            System.err.println("Общая ошибка при обработке контракта: " + procurementUrl + " → " + e.getMessage());
        } finally {
            WebDriverFactory.closeAllDrivers();
            driver.quit();
        }
    }

    private void printProcurementInfo(List<Map<String, String>> procurementItems) {
        System.out.println("\n=== Информация об объектах закупки ===");
        for (Map<String, String> item : procurementItems) {
            item.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println("----------------------");
        }
        System.out.println("=============================\n");
    }

    private void printCollectedData() {
        System.out.println("\n=== Собраны данные по всем объектам закупки ===");
        procurementData.forEach((url, items) -> {
            System.out.println("\nURL: " + url);
            items.forEach(item -> {
                item.forEach((key, value) -> System.out.println(key + ": " + value));
                System.out.println("----------------------");
            });
        });
        System.out.println("\nВсего обработано контрактов: " + procurementData.size());
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

    private String getElementTextSafely(WebElement parent, String cssSelector) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public Map<String, List<Map<String, String>>> getProcurementData() {
        return this.procurementData;
    }
}