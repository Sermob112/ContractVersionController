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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUrlParser {
    private final String inputFilePath;
    private final String outputFilePath;
    private final String processedLinksFilePath;
    private final int delayBetweenPagesMs;

    public VersionUrlParser(String inputFilePath, String outputFilePath,
                            String processedLinksFilePath, int delayBetweenPagesMs) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.processedLinksFilePath = processedLinksFilePath;
        this.delayBetweenPagesMs = delayBetweenPagesMs;
    }

    public VersionUrlParser() {
        this("contract_links.txt", "event_journal_links.txt",
                "processed_contract_links.txt", 1000);
    }

    public void processAllContractLinksMultithreaded(int threadCount) {
        List<String> contractLinks = readContractLinksFromFile();
        List<String> processedLinks = readProcessedLinksFromFile();
        Set<String> processedSet = new HashSet<>(processedLinks);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : contractLinks) {
            if (!processedSet.contains(link)) {
                executor.submit(new ContractProcessingTask(link, outputFilePath, processedLinksFilePath));
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted while waiting for tasks.");
        }
    }

    private List<String> readContractLinksFromFile() {
        try {
            return Files.readAllLines(Paths.get(inputFilePath));
        } catch (IOException e) {
            System.err.println("Error reading contract links file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> readProcessedLinksFromFile() {
        try {
            return Files.readAllLines(Paths.get(processedLinksFilePath));
        } catch (IOException e) {
            // Файл может не существовать - это нормально
            return new ArrayList<>();
        }
    }



    private String generateVersionJournalLink(String tabHref) {
        Pattern pattern = Pattern.compile("reestrNumber=([^&]+).*contractInfoId=([^&]+)");
        Matcher matcher = pattern.matcher(tabHref);

        if (matcher.find()) {
            String reestrNumber = matcher.group(1);
            String contractInfoId = matcher.group(2);
            return String.format("https://zakupki.gov.ru/epz/contract/contractCard/journal-version.html?reestrNumber=%s&contractInfoId=%s",
                    reestrNumber, contractInfoId);
        }

        return null;
    }

    private void createFileIfNotExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
    }

    private void appendToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, (content + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}