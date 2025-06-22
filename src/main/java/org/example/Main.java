package org.example;

import Database.Hooks.HibernateUtil;

import Parser.ContractParser;
import Parser.DataHooks.ContractExtractor;
import Parser.DataHooks.ContractUploader;
import Parser.URLParser.JournalLinksExtractorMultithreaded;
import Parser.URLParser.ParseUrls;

import Parser.URLParser.ParseUrlsMultithreaded;
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
            // Инициализация Hibernate
            initializeHibernate();

            // Создаем и запускаем парсер контрактов
            ContractParser contractParser = new ContractParser();
            contractParser.fullParseProcess();

        } catch (Exception e) {
            System.err.println("Ошибка в основном потоке:");
            e.printStackTrace();
        }
    }

    private static void initializeHibernate() {
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate успешно инициализирован");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации Hibernate:");
            e.printStackTrace();
            throw new RuntimeException("Не удалось инициализировать Hibernate", e);
        }
    }
//        try {
//            // Можно указать путь к конфигурационному файлу, если он отличается от стандартного
//            // HibernateUtil.initialize("путь/к/вашему/config.json");
//
//            // Получаем SessionFactory (инициализация произойдет автоматически)
//            HibernateUtil.getSessionFactory();
//            System.out.println("Hibernate успешно инициализирован");
//        } catch (Exception e) {
//            System.err.println("Ошибка инициализации Hibernate:");
//            e.printStackTrace();
//            return; // Завершаем работу приложения, если не удалось инициализировать Hibernate
//        }
//
//        ParseUrlsMultithreaded parser = new ParseUrlsMultithreaded(
//                "https://zakupki.gov.ru/epz/contract/search/results.html?morphology=on&search-filter=Дате+размещения&fz44=on&contractStageList_0=on&contractStageList_1=on&contractStageList_2=on&contractStageList_3=on&contractStageList=0%2C1%2C2%2C3&budgetLevelsIdNameHidden=%7B%7D&okpd2Ids=8874076&okpd2IdsCodes=30.1&sortBy=UPDATE_DATE&pageNumber=1&sortDirection=false&recordsPerPage=_50&showLotsInfoHidden=false",
//                "contract_links.txt",
//                4   // количество потоков
//        );
//        parser.processAllPagesMultithreaded();
//
//
//        VersionUrlParser parser = new VersionUrlParser(); // null, т.к. WebDriver в потоках
//        parser.processAllContractLinksMultithreaded(4);
//
//        JournalLinksExtractorMultithreaded extractor = new JournalLinksExtractorMultithreaded(
//                "event_journal_links.txt",
//                "contract_detail_links.txt",
//                4 // количество потоков
//        );
//        extractor.processAllJournalPages();
//
//        ContractExtractor extractor = new ContractExtractor(4); // 5 потоков
//        extractor.extractAllContracts();
//        ContractUploader uploader = new ContractUploader();
//        try {
//            uploader.uploadContracts(extractor.getContractsData());
//        } finally {
//            uploader.close();
//        }

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
