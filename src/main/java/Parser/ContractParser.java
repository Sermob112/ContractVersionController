package Parser;



import Database.Hooks.DataBaseServices;
import Database.Models.Contract;
import Database.Models.ProcurementObject;
import Parser.DataHooks.ContractExtractor;
import Parser.DataHooks.ContractUploader;
import Parser.ProcurementObject.LinkTransformer;
import Parser.ProcurementObject.ProcurementExtractor;
import Parser.URLParser.*;

import javax.swing.*;
import java.util.*;

public class ContractParser {
    private final JProgressBar progressBar;
    private final JLabel statusParser;// поле в ContractParser

    public ContractParser(JProgressBar progressBar, JLabel statusParser) {
        this.progressBar = progressBar;
        this.statusParser = statusParser;
    }
    public void fullParseProcess() {

        try {

////             1. Парсинг списка контрактов (многостраничный)
//            boolean linksParsed = parseContractLinks(); // теперь получаем результат
//
//            if (linksParsed) {
//                // Если парсили ссылки, значит нужно заново парсить версии
//                // 2. Парсинг версий контрактов
//                parseContractVersions();
//            } else {
//                updateStatus("Пропуск парсинга версий контрактов (ссылки не обновлялись)");
//            }
////
//            // 3. Извлечение ссылок из журналов
//            extractJournalLinks();

            // 4. Парсинг деталей контрактов и сохранение в БД
            parseAndSaveContractDetails();



        } catch (Exception e) {
            updateStatus("Ошибка в процессе парсинга:");
            e.printStackTrace();
        }
    }

    private boolean parseContractLinks() {
        updateStatus("Начало парсинга списка контрактов...");
        ParseUrlsMultithreaded parser = new ParseUrlsMultithreaded(
                "https://zakupki.gov.ru/epz/contract/search/results.html?morphology=on&search-filter=Дате+размещения&fz44=on&contractStageList_0=on&contractStageList_1=on&contractStageList_2=on&contractStageList_3=on&contractStageList=0%2C1%2C2%2C3&budgetLevelsIdNameHidden=%7B%7D&okpd2Ids=8874076&okpd2IdsCodes=30.1&sortBy=UPDATE_DATE&pageNumber=1&sortDirection=false&recordsPerPage=_50&showLotsInfoHidden=false", // твоя ссылка
                "contract_links.txt",
                4,
                progressBar,
                statusParser
        );
        boolean parsed = parser.processAllPagesMultithreaded();
        if (parsed) {
            updateStatus("Парсинг списка контрактов завершен");
        } else {
            updateStatus("Парсинг списка контрактов пропущен (актуальные данные уже есть)");
        }
        return parsed;
    }

    private void parseContractVersions() {
        updateStatus("Начало парсинга версий контрактов...");
        VersionUrlParser parser = new VersionUrlParser(progressBar, statusParser);
        parser.processAllContractLinksMultithreaded(4);
        updateStatus("Парсинг версий контрактов завершен");
    }

    private void extractJournalLinks() {
        updateStatus("Начало извлечения ссылок из журналов...");
        JournalLinksExtractorMultithreaded extractor = new JournalLinksExtractorMultithreaded(
                "event_journal_links.txt",
                "contract_detail_links.txt",
                4,
                progressBar,
                statusParser
        );
        extractor.processAllJournalPages();
        updateStatus("Извлечение ссылок из журналов завершено");
    }

    private void parseAndSaveContractDetails() {
        updateStatus("Начало парсинга деталей контрактов...");
        ContractExtractor extractor = new ContractExtractor(4, progressBar, statusParser); // передаём GUI
        extractor.extractAllContracts();

        Map<String, Map<String, String>> contractsData = extractor.getContractsDataHead();
        Map<String, Object> contractsDataBody = extractor.getContractsDataBody();
        List<Contract> contractsToSave = convertToContractEntities(contractsData, contractsDataBody);

        LinkTransformer   linkTransformer = new LinkTransformer();
        linkTransformer.transformLinks();

        ProcurementExtractor procurementExtractor = new ProcurementExtractor(4, progressBar, statusParser);
        procurementExtractor.extractAllProcurements();

        if (!contractsToSave.isEmpty()) {
            DataBaseServices.batchProcessContracts(contractsToSave);
            updateStatus("Успешно сохранено контрактов: " + contractsToSave.size());
        } else {
            updateStatus("Нет контрактов для сохранения");
        }

    }


    private List<Contract> convertToContractEntities(Map<String, Map<String, String>> contractsData, Map<String, Object> contractsDataBody) {
        ContractUploader contractUploader = new ContractUploader();
        List<Contract> contracts = contractUploader.uploadAndFillContracts(contractsData, contractsDataBody);
        // передаем весь Map

        return contracts;
    }

    private void updateStatus(String message) {
        System.out.println(message); // Для логов в консоли оставим
        SwingUtilities.invokeLater(() -> statusParser.setText(message));
    }



}