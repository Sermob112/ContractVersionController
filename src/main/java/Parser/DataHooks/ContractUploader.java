package Parser.DataHooks;

import Database.Models.Contract;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContractUploader {


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public ContractUploader() {

    }

    public List<Contract> uploadAndFillContracts(Map<String, Map<String, String>> contractsData, Map<String, Object> participantData) {
        List<Contract> contracts = new ArrayList<>();

        // Обработка данных из contractsData
        for (Map.Entry<String, Map<String, String>> entry : contractsData.entrySet()) {
            String contractUrl = entry.getKey();
            Map<String, String> contractInfo = entry.getValue();

            Contract contract = new Contract();

            try {
                // Заполнение полей из первого метода (uploadContracts)
                contract.setNoticeNumber(contractInfo.get("Реестровый номер"));
                contract.setContractStatus(contractInfo.get("Статус"));
                contract.setCustomer(contractInfo.get("Заказчик"));
                contract.setContractNumber(contractInfo.get("Номер контракта"));
                contract.setPurchaseObjects(contractInfo.get("Объекты закупки"));

                String priceStr = contractInfo.get("Цена контракта");
                if (priceStr != null && !priceStr.isEmpty()) {
                    priceStr = priceStr.replaceAll("[^\\d,]", "").replace(",", ".");
                    contract.setContractPrice(Double.parseDouble(priceStr));
                }

                contract.setConclusionDate(parseDate(contractInfo.get("Дата заключения")));
                contract.setExecutionPeriod(parseDate(contractInfo.get("Срок исполнения")));
                contract.setPostedDate(parseDate(contractInfo.get("Дата размещения")));
                contract.setUpdatedDate(parseDate(contractInfo.get("Дата обновления")));

                contract.setVersion(contractInfo.get("Версия контракта"));
                contract.setLastParsingUpdate(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());


                Object rawData = participantData.get(contractUrl);
                if (!(rawData instanceof Map)) {
                    System.err.println("Нет данных participantData для контракта: " + contractUrl);
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> contractParticipantData = (Map<String, Object>) rawData;
                // Заполнение полей из второго метода (fillContractModel)
                Map<String, String> generalInfo = getSectionData(contractParticipantData, "Общая информация");
                Map<String, String> commonData = getSectionData(contractParticipantData, "Общие данные");
                Map<String, String> nationalRegime = getSectionData(contractParticipantData, "Информация о применении к закупке национального режима");
                Map<String, String> contractGuarantee = getSectionData(contractParticipantData, "Обеспечение исполнения контракта");
                Map<String, String> qualityGuarantee = getSectionData(contractParticipantData, "Информация о гарантии качества товаров, работ, услуг");
                Map<String, String> deliveryPlace = getSectionData(contractParticipantData, "Информация о месте поставки товара, выполнения работы или оказания услуги");

                // Заполняем поля, если они не были установлены ранее
                if (contract.getRegistryNumber() == null) {
                    contract.setRegistryNumber(getValueOrNull(generalInfo, "Реестровый номер контракта"));
                }
                if (contract.getContractStatus() == null) {
                    contract.setStatus(getValueOrNull(generalInfo, "Статус контракта"));
                }
                contract.setProcurementNoticeNumber(getValueOrNull(generalInfo, "Номер извещения об осуществлении закупки"));
                contract.setProcurementIdentificationCode(getValueOrNull(generalInfo, "Идентификационный код закупки (ИКЗ)"));
                contract.setElectronicContractId(getValueOrNull(generalInfo, "Идентификатор контракта, заключенного в электронной форме"));
                contract.setSoleSupplierBasis(getValueOrNull(generalInfo, "Основание заключения контракта с единственным поставщиком"));
                contract.setSoleSupplierDocumentDetails(getValueOrNull(generalInfo, "Реквизиты документа, подтверждающего основание заключения контракта"));
                contract.setBankingTreasurySupportInfo(getValueOrNull(generalInfo, "Информация о банковском и (или) казначейском сопровождении контракта"));

                if (contract.getConclusionDate() == null) {
                    contract.setConclusionDate(parseDate(getValueOrNull(commonData, "Дата заключения контракта")));
                }
                if (contract.getContractNumber() == null) {
                    contract.setContractNumber(getValueOrNull(commonData, "Номер контракта"));
                }
                contract.setSubject(getValueOrNull(commonData, "Предмет контракта"));
                if (contract.getContractPrice() == null) {
                    contract.setContractPrice(parseDouble(getValueOrNull(commonData, "Цена контракта")));
                }
                contract.setIncludingVat(parseBigDecimal(getValueOrNull(commonData, "В том числе НДС")));
                contract.setCurrency(getValueOrNull(commonData, "Валюта контракта"));
                contract.setStartDate(parseDate(getValueOrNull(commonData, "Дата начала исполнения контракта")));
                contract.setEndDate(parseDate(getValueOrNull(commonData, "Дата окончания исполнения контракта")));
                contract.setContractStageId(getValueOrNull(commonData, "Идентификатор этапа контракта"));
                contract.setAdvanceAmount(parseBigDecimal(getValueOrNull(commonData, "Размер аванса")));
                contract.setPenaltyDeductionApplied(getValueOrNull(commonData, "Контрактом предусмотрено удержание суммы неисполненных требований об уплате неустоек (штрафов, пеней) из суммы, подлежащей оплате поставщику (подрядчику, исполнителю)"));
                contract.setAdditionalInfo(getValueOrNull(commonData, "Информация"));
                contract.setTreasuryGuaranteeAmount(parseBigDecimal(getValueOrNull(commonData, "В том числе сумма казначейского обеспечения обязательств, ₽")));

                contract.setNationalRegimeInfo(getValueOrNull(nationalRegime, "Информация"));
                contract.setContractGuaranteeInfo(getValueOrNull(contractGuarantee, "Размер обеспечения исполнения контракта, ₽"));

                // Обработка гарантии качества
                if (qualityGuarantee != null) {
                    String warrantyPeriod = getValueOrNull(qualityGuarantee, "Срок, на который предоставляется гарантия");
                    String warrantyRequirements = getValueOrNull(qualityGuarantee, "Информация о требованиях к гарантийному обслуживанию товара");
                    String manufacturerRequirements = getValueOrNull(qualityGuarantee, "Требования к гарантии производителя");

                    if (warrantyPeriod != null || warrantyRequirements != null || manufacturerRequirements != null) {
                        contract.setQualityGuaranteeInfo(
                                (warrantyPeriod != null ? "Срок гарантии: " + warrantyPeriod + "; " : "") +
                                        (warrantyRequirements != null ? "Требования к гарантийному обслуживанию: " + warrantyRequirements + "; " : "") +
                                        (manufacturerRequirements != null ? "Требования производителя: " + manufacturerRequirements : "")
                        );
                    } else {
                        contract.setQualityGuaranteeInfo(null);
                    }
                } else {
                    contract.setQualityGuaranteeInfo(null);
                }

                contract.setDeliveryPlaceInfo(getValueOrNull(deliveryPlace, "Место поставки товара, выполнения работы или оказания услуги"));

                contracts.add(contract);

            } catch (Exception e) {
                System.err.println("Ошибка при обработке контракта " + contractUrl + ": " + e.getMessage());
                // Продолжаем обработку следующих контрактов
            }
        }

        return contracts;
    }
    @SuppressWarnings("unchecked")
    private Map<String, String> getSectionData(Map<String, Object> data, String key) {
        Object section = data.get(key);
        if (section instanceof Map) {
            return (Map<String, String>) section;
        }
        return Collections.emptyMap();
    }
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            Date parsedDate = dateFormat.parse(dateStr); // dateFormat — твой SimpleDateFormat
            return parsedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (ParseException e) {
            System.err.println("Ошибка парсинга даты: " + dateStr);
            return null;
        }
    }



    public static String getValueOrNull(Map<String, String> dataMap, String key) {
        if (dataMap == null || key == null) {
            return null;
        }
        String value = dataMap.get(key);
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    public static BigDecimal parseBigDecimal(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) return null;
        try {
            // Удаляем всё, кроме цифр, точек, запятых и пробелов
            String cleaned = numberStr.replaceAll("[^\\d,.]", "").trim();
            // Заменяем запятую на точку и удаляем пробелы (разделители тысяч)
            cleaned = cleaned.replace(",", ".").replace(" ", "");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            System.out.println("Ошибка парсинга числа: " + numberStr);
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            // Заменяем запятую на точку для корректного парсинга (если нужно)
            value = value.replace(",", ".").trim();
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга double: " + value);
            return null;
        }
    }


}