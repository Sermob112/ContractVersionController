package org.example;

import Database.Hooks.HibernateUtil;
import Parser.URLParser.ParseUrls;
import Parser.URLParser.VersionUrlParser;
import Webdriver.Implementations.ChromeDriverSetup;
import Webdriver.Implementations.RandomUserAgent;
import mainForm.MainWindow;
import org.openqa.selenium.WebDriver;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        try {
            // Можно указать путь к конфигурационному файлу, если он отличается от стандартного
            // HibernateUtil.initialize("путь/к/вашему/config.json");

            // Получаем SessionFactory (инициализация произойдет автоматически)
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate успешно инициализирован");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации Hibernate:");
            e.printStackTrace();
            return; // Завершаем работу приложения, если не удалось инициализировать Hibernate
        }

        String userAgent = RandomUserAgent.getRandomUserAgent();
        ChromeDriverSetup driverSetup = new ChromeDriverSetup(userAgent);
        WebDriver driver = driverSetup.setupDriver();

//        try {
//            ParseUrls parser = new ParseUrls(driver);
//
//            // Парсим ссылки с реального сайта
//            String targetUrl = "https://zakupki.gov.ru/epz/contract/search/results.html?morphology=on&search-filter=Дате+размещения&fz44=on&contractStageList_0=on&contractStageList_1=on&contractStageList_2=on&contractStageList_3=on&contractStageList=0%2C1%2C2%2C3&budgetLevelsIdNameHidden=%7B%7D&okpd2Ids=8874076&okpd2IdsCodes=30.1&sortBy=UPDATE_DATE&pageNumber=1&sortDirection=false&recordsPerPage=_50&showLotsInfoHidden=false";
//            List<String> contractLinks = parser.parseAllContractLinks(targetUrl);
//            parser.saveUniqueLinksToFile(contractLinks);
//
//            // Читаем и выводим количество уникальных ссылок с обработкой исключений
//            try {
//                Path path = Paths.get("contract_links.txt");
//                if (Files.exists(path)) {
//                    int uniqueLinksCount = Files.readAllLines(path).size();
//                    System.out.println("Processing completed. Total unique links: " + uniqueLinksCount);
//                } else {
//                    System.out.println("Processing completed. File not created (no new links)");
//                }
//            } catch (IOException e) {
//                System.err.println("Error reading links file: " + e.getMessage());
//            }
//
//        } finally {
//            driver.quit();
//        }

        VersionUrlParser parser = new VersionUrlParser(); // null, т.к. WebDriver в потоках
        parser.processAllContractLinksMultithreaded(4);



//        SwingUtilities.invokeLater(() -> {
//            // Создаем экземпляр вашей формы
//            MainWindow mainWindow = new MainWindow();
//
//            // Создаем главное окно
//            JFrame frame = new JFrame("Мое приложение");
//            frame.setContentPane(mainWindow.MainPanel); // Устанавливаем главную панель
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.pack(); // Автоподбор размера
//            frame.setLocationRelativeTo(null); // Центрирование
//            frame.setVisible(true);
//        });
    }
}