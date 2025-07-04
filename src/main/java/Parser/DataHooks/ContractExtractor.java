package Parser.DataHooks;

import Parser.URLParser.WebDriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.*;

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

public class ContractExtractor {

    private final String inputFilePath = "contract_detail_links.txt";
    private final int threadCount;
    private final Map<String, Map<String, String>> contractsDataHead = new ConcurrentHashMap<>();
    private final Map<String,  Object> contractsDataBody= new ConcurrentHashMap<>();
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    public ContractExtractor(int threadCount, JProgressBar progressBar, JLabel statusLabel) {
        this.threadCount = threadCount;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    public void extractAllContracts() {
        List<String> contractLinks = readLinksFromFile();
        int total = contractLinks.size();
        AtomicInteger processed = new AtomicInteger(0);

        updateProgress(0, total);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : contractLinks) {
            executor.submit(() -> {
                extractContractData(link);
                parseAndPrintGeneralContractData(link);
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

    private void extractContractData(String contractUrl) {
        WebDriver driver = WebDriverFactory.create();
        Map<String, String> contractInfo = new HashMap<>();

        try {
            driver.get(contractUrl);

            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
                contractsDataHead.put(contractUrl, contractInfo);
                System.out.println("Обработан контракт: " + contractUrl);
                printContractInfo(contractInfo);
            } else {
                System.err.println("Не удалось извлечь данные для контракта: " + contractUrl);
            }

        } catch (Exception e) {
            System.err.println("Общая ошибка при обработке контракта: " + contractUrl + " → " + e.getMessage());
        } finally {
            WebDriverFactory.closeAllDrivers();
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
    public Map<String, Map<String, String>> getContractsDataHead() {
        return this.contractsDataHead;
    }

    public Map<String, Object> getContractsDataBody() {
        return this.contractsDataBody;
    }


    private void printContractInfo(Map<String, String> contractInfo) {
        System.out.println("\n=== Информация о контракте ===");
        contractInfo.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("=============================\n");
    }

    private void printCollectedData() {
        System.out.println("\n=== Собраны данные по всем контрактам ===");
        contractsDataHead.forEach((url, data) -> {
            System.out.println("\nURL: " + url);
            data.forEach((key, value) -> System.out.println(key + ": " + value));
        });
        System.out.println("\nВсего обработано контрактов: " + contractsDataHead.size());
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

    public Map<String, Object> parseAndPrintGeneralContractData(String contractDraftUrl) {
        WebDriver driver = WebDriverFactory.create();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        Map<String, Object> contractData = new LinkedHashMap<>();

        try {
            driver.get(contractDraftUrl);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.container")));

            // Получаем все блоки с информацией
            List<WebElement> infoBlocks = driver.findElements(By.cssSelector("div.blockInfo"));

            for (WebElement block : infoBlocks) {
                // Извлекаем заголовок блока
                String blockTitle = getElementTextSafely(block, "h2.blockInfo__title, h2.blockInfo__title_sub");

                // Парсим все секции в блоке
                List<WebElement> sections = block.findElements(By.cssSelector("section.blockInfo__section"));
                Map<String, String> sectionData = new LinkedHashMap<>();

                for (WebElement section : sections) {
                    String title = getElementTextSafely(section, "span.section__title");
                    String value = getElementTextSafely(section, "span.section__info")
                            .replace("&nbsp;", " ") // Заменяем HTML-пробелы
                            .trim();

                    if (!title.isEmpty() && !value.isEmpty()) {
                        sectionData.put(title, value);
                    } else if (title.isEmpty() && !value.isEmpty()) {
                        // Для секций без заголовка (как в блоке национального режима)
                        sectionData.put("Информация", value);
                    }
                }

                // Обработка таблиц внутри блоков
                try {
                    WebElement table = block.findElement(By.cssSelector("table.blockInfo__table"));
                    Map<String, String> tableData = parseSimpleTable(table);
                    sectionData.putAll(tableData);
                } catch (NoSuchElementException e) {
                    // Таблица не найдена - это нормально
                }

                // Добавляем данные блока в общий результат
                if (!sectionData.isEmpty()) {
                    if (blockTitle != null && !blockTitle.isEmpty()) {
                        contractData.put(blockTitle, sectionData);
                    } else {
                        contractData.putAll(sectionData);
                    }
                }
            }

            // Вывод результата в консоль
            System.out.println("=== Результат парсинга общих данных контракта ===");
            for (Map.Entry<String, Object> entry : contractData.entrySet()) {
                System.out.println("\n" + entry.getKey() + ":");
                if (entry.getValue() instanceof Map) {
                    Map<?, ?> subMap = (Map<?, ?>) entry.getValue();
                    subMap.forEach((k, v) -> System.out.println("  " + k + ": " + v));
                } else {
                    System.out.println("  " + entry.getValue());
                }
            }
            if (!contractData.isEmpty()) {
                contractsDataBody.put(contractDraftUrl, contractData);
                System.out.println("Обработан контракт: " + contractDraftUrl);

            } else {
                System.err.println("Не удалось извлечь данные для контракта: " + contractDraftUrl);
            }

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге общих данных контракта: " + e.getMessage());
        }

        return contractData;
    }

    public Map<String, Object> parseSuppliersInfo(String url, WebDriver driver, WebDriverWait wait) {
        Map<String, Object> suppliersData = new LinkedHashMap<>();
        driver.get(url);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h2[contains(text(), 'Информация о поставщиках')]")));

            List<WebElement> tables = driver.findElements(
                    By.xpath("//h2[contains(text(), 'Информация о поставщиках')]/following::table[contains(@class, 'blockInfo__table')]"));

            List<Map<String, String>> suppliersList = new ArrayList<>();

            for (WebElement table : tables) {
                List<WebElement> rows = table.findElements(By.cssSelector("tbody tr.tableBlock__row"));

                for (WebElement row : rows) {
                    Map<String, String> supplierInfo = new LinkedHashMap<>();

                    // Получаем все ячейки в строке (6 колонок, последняя пустая)
                    List<WebElement> cells = row.findElements(By.cssSelector("td"));

                    // 1. Организация и ИНН (первая колонка)
                    WebElement orgCell = cells.get(0);
                    String orgText = orgCell.getText();
                    String[] orgLines = orgText.split("\n");

                    supplierInfo.put("Организация", orgLines[0].trim());

                    // ИНН
                    try {
                        String inn = orgCell.findElement(By.xpath(".//span[contains(@class,'grey-main-light') and contains(text(),'ИНН:')]/following-sibling::span"))
                                .getText().trim();
                        supplierInfo.put("ИНН", inn);
                    } catch (NoSuchElementException e) {
                        supplierInfo.put("ИНН", null);
                    }

                    // КПП
                    try {
                        String kpp = orgCell.findElement(By.xpath(".//span[contains(@class,'grey-main-light') and contains(text(),'КПП:')]/following-sibling::span"))
                                .getText().trim();
                        supplierInfo.put("КПП", kpp);
                    } catch (NoSuchElementException e) {
                        supplierInfo.put("КПП", null);
                    }

                    // 2. Страна и код страны (вторая колонка)
                    if (cells.size() > 1) {
                        String countryText = cells.get(1).getText().trim();
                        String[] countryParts = countryText.split("\n");
                        supplierInfo.put("Страна", countryParts[0].trim());
                        if (countryParts.length > 1) {
                            supplierInfo.put("Код страны", countryParts[1].trim());
                        }
                    }

                    // 3. Адрес места нахождения (третья колонка)
                    if (cells.size() > 2) {
                        supplierInfo.put("Адрес места нахождения", cells.get(2).getText().trim());
                    }

                    // 4. Почтовый адрес (четвертая колонка)
                    if (cells.size() > 3) {
                        supplierInfo.put("Почтовый адрес", cells.get(3).getText().trim());
                    }

                    // 5. Контактная информация (пятая колонка)
                    if (cells.size() > 4) {
                        String contactsText = cells.get(4).getText().trim();
                        String[] contacts = contactsText.split("\n");
                        if (contacts.length > 0) {
                            supplierInfo.put("Телефон", contacts[0].trim());
                        }
                        if (contacts.length > 1) {
                            supplierInfo.put("Email", contacts[1].trim());
                        }
                    }

                    suppliersList.add(supplierInfo);
                }
            }

            suppliersData.put("Поставщики", suppliersList);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге информации о поставщиках: " + e.getMessage());
            e.printStackTrace();
        }

        return suppliersData;
    }

    // Вспомогательный метод для безопасного получения текста элемента
    private String getElementTextSafely(WebElement parent, String cssSelector) {
        try {
            return parent.findElement(By.cssSelector(cssSelector)).getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public static Map<String, String> parseSimpleTable(WebElement element) {
        Map<String, String> tableData = new LinkedHashMap<>();
        try {
            List<WebElement> tables = element.findElements(By.cssSelector("table.printFormTbl"));
            for (WebElement table : tables) {
                List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() == 2) {
                        String key = cells.get(0).getText().trim();
                        String value = cells.get(1).getText().trim();
                        if (!key.isEmpty()) {
                            tableData.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при парсинге простой таблицы: " + e.getMessage());
        }
        return tableData;
    }
}