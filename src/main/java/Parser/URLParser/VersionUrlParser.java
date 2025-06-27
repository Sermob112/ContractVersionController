package Parser.URLParser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class VersionUrlParser {
    private final String inputFilePath;
    private final String outputFilePath;
    private final String processedLinksFilePath;
    private final int delayBetweenPagesMs;

    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public VersionUrlParser(String inputFilePath, String outputFilePath,
                            String processedLinksFilePath, int delayBetweenPagesMs,
                            JProgressBar progressBar, JLabel statusLabel) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.processedLinksFilePath = processedLinksFilePath;
        this.delayBetweenPagesMs = delayBetweenPagesMs;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
    }

    public VersionUrlParser(JProgressBar progressBar, JLabel statusLabel) {
        this("contract_links.txt", "event_journal_links.txt",
                "processed_contract_links.txt", 1000,
                progressBar, statusLabel);
    }
    public void processAllContractLinksMultithreaded(int threadCount) {
        List<String> contractLinks = readContractLinksFromFile();
        List<String> processedLinks = readProcessedLinksFromFile();
        Set<String> processedSet = new HashSet<>(processedLinks);

        List<String> linksToProcess = contractLinks.stream()
                .filter(link -> !processedSet.contains(link))
                .collect(Collectors.toList());

        AtomicInteger processedCount = new AtomicInteger(0);
        updateProgress(0, linksToProcess.size());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (String link : linksToProcess) {
            executor.submit(() -> {
                try {
                    new ContractProcessingTask(link, outputFilePath, processedLinksFilePath).run();
                    int count = processedCount.incrementAndGet();
                    updateProgress(count, linksToProcess.size());
                    updateStatus("Парсинг версий контрактов... Обработано: " + count + " из " + linksToProcess.size());
                    Thread.sleep(delayBetweenPagesMs);
                } catch (Exception e) {
                    System.err.println("Ошибка при обработке ссылки: " + link);
                }
            });
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

    private void updateStatus(String message) {
        System.out.println(message);
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(message));
        }
    }

    private void updateProgress(int value, int max) {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setMaximum(max);
                progressBar.setValue(value);
                progressBar.setStringPainted(true);
            });
        }
    }
}