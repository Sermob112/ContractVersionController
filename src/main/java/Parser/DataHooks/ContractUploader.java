package Parser.DataHooks;

import Database.Models.Contract;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ContractUploader {


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public ContractUploader() {

    }

    public List<Contract> uploadContracts(Map<String, Map<String, String>> contractsData) {

        List<Contract> contracts = new ArrayList<>();

            for (Map.Entry<String, Map<String, String>> entry : contractsData.entrySet()) {
                String contractUrl = entry.getKey();
                Map<String, String> contractInfo = entry.getValue();

                Contract contract = new Contract();

                // Заполнение полей контракта
                try {
                    // Основная информация
                    contract.setNoticeNumber(contractInfo.get("Реестровый номер")); // Используем URL как номер извещения
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
                    contract.setVersion(contractInfo.get("Версия контракта"));
                    contract.setLastParsingUpdate(new Date());

                    // Сохранение или обновление контракта

                    contracts.add(contract);

                } catch (Exception e) {
                    System.err.println("Ошибка при обработке контракта " + contractUrl + ": " + e.getMessage());
                    // Продолжаем обработку следующих контрактов
                }
            }

        return contracts;
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