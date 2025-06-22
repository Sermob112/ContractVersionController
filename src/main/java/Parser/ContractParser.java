package Parser;



import Database.Hooks.DataBaseServices;
import Database.Models.Contract;
import Parser.DataHooks.ContractExtractor;
import Parser.URLParser.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContractParser {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public void fullParseProcess() {
        try {
            // 1. Парсинг списка контрактов (многостраничный)
            parseContractLinks();

            // 2. Парсинг версий контрактов
            parseContractVersions();

            // 3. Извлечение ссылок из журналов
            extractJournalLinks();

            // 4. Парсинг деталей контрактов и сохранение в БД
            parseAndSaveContractDetails();

        } catch (Exception e) {
            System.err.println("Ошибка в процессе парсинга:");
            e.printStackTrace();
        }
    }

    private void parseContractLinks() {
        System.out.println("Начало парсинга списка контрактов...");
        ParseUrlsMultithreaded parser = new ParseUrlsMultithreaded(
                "https://zakupki.gov.ru/epz/contract/search/results.html?morphology=on&search-filter=Дате+размещения&fz44=on&contractStageList_0=on&contractStageList_1=on&contractStageList_2=on&contractStageList_3=on&contractStageList=0%2C1%2C2%2C3&budgetLevelsIdNameHidden=%7B%7D&okpd2Ids=8874076&okpd2IdsCodes=30.1&sortBy=UPDATE_DATE&pageNumber=1&sortDirection=false&recordsPerPage=_50&showLotsInfoHidden=false",
                "contract_links.txt",
                4
        );
        parser.processAllPagesMultithreaded();
        System.out.println("Парсинг списка контрактов завершен");
    }

    private void parseContractVersions() {
        System.out.println("Начало парсинга версий контрактов...");
        VersionUrlParser parser = new VersionUrlParser();
        parser.processAllContractLinksMultithreaded(4);
        System.out.println("Парсинг версий контрактов завершен");
    }

    private void extractJournalLinks() {
        System.out.println("Начало извлечения ссылок из журналов...");
        JournalLinksExtractorMultithreaded extractor = new JournalLinksExtractorMultithreaded(
                "event_journal_links.txt",
                "contract_detail_links.txt",
                4
        );
        extractor.processAllJournalPages();
        System.out.println("Извлечение ссылок из журналов завершено");
    }

    private void parseAndSaveContractDetails() {
        System.out.println("Начало парсинга деталей контрактов...");
        ContractExtractor extractor = new ContractExtractor(4);
        extractor.extractAllContracts();

        Map<String, Map<String, String>> contractsData = extractor.getContractsData();
        List<Contract> contractsToSave = convertToContractEntities(contractsData);

        if (!contractsToSave.isEmpty()) {
            DataBaseServices.batchProcessContracts(contractsToSave);
            System.out.println("Успешно сохранено контрактов: " + contractsToSave.size());
        } else {
            System.out.println("Нет контрактов для сохранения");
        }
    }

    private List<Contract> convertToContractEntities(Map<String, Map<String, String>> contractsData) {
        List<Contract> contracts = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : contractsData.entrySet()) {
            try {
                Contract contract = createContractFromData(entry.getKey(), entry.getValue());
                contracts.add(contract);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке контракта " + entry.getKey() + ": " + e.getMessage());
            }
        }
        return contracts;
    }

    private Contract createContractFromData(String contractUrl, Map<String, String> contractInfo) {
        Contract contract = new Contract();

        // Основная информация
        contract.setNoticeNumber(contractUrl);
        contract.setContractStatus(contractInfo.get("Статус"));
        contract.setCustomer(contractInfo.get("Заказчик"));
        contract.setContractNumber(contractInfo.get("Номер контракта"));
        contract.setPurchaseObjects(contractInfo.get("Объекты закупки"));

        // Обработка цены контракта
        String priceStr = contractInfo.get("Цена контракта");
        if (priceStr != null && !priceStr.isEmpty()) {
            priceStr = priceStr.replaceAll("[^\\d,]", "").replace(",", ".");
            contract.setContractPrice(Double.parseDouble(priceStr));
        }

        // Обработка дат
        contract.setContractConclusion(parseDate(contractInfo.get("Дата заключения")));
        contract.setExecutionPeriod(parseDate(contractInfo.get("Срок исполнения")));
        contract.setPostedDate(parseDate(contractInfo.get("Дата размещения")));
        contract.setUpdatedDate(parseDate(contractInfo.get("Дата обновления")));

        // Системная информация
        contract.setVersion("1.0");
        contract.setLastParsingUpdate(new Date());

        return contract;
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.err.println("Ошибка парсинга даты: " + dateStr);
            return null;
        }
    }
}