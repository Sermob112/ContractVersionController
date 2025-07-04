package Parser.DataHooks;

import Database.Models.Contract;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;



public class ContractModelFiller {
    public void fillContractModel(Contract contract, Map<String, Object> participantData) {
        // Получаем данные из разных разделов
        Map<String, String> generalInfo = getSectionData(participantData, "Общая информация");
        Map<String, String> commonData = getSectionData(participantData, "Общие данные");
        Map<String, String> nationalRegime = getSectionData(participantData, "Информация о применении к закупке национального режима");
        Map<String, String> contractGuarantee = getSectionData(participantData, "Обеспечение исполнения контракта");
        Map<String, String> qualityGuarantee = getSectionData(participantData, "Информация о гарантии качества товаров, работ, услуг");
        Map<String, String> deliveryPlace = getSectionData(participantData, "Информация о месте поставки товара, выполнения работы или оказания услуги");

        // Заполняем поля контракта с проверкой на null/пустоту
        contract.setRegistryNumber(getValueOrNull(generalInfo, "Реестровый номер контракта"));
        contract.setStatus(getValueOrNull(generalInfo, "Статус контракта"));
        contract.setProcurementNoticeNumber(getValueOrNull(generalInfo, "Номер извещения об осуществлении закупки"));
        contract.setProcurementIdentificationCode(getValueOrNull(generalInfo, "Идентификационный код закупки (ИКЗ)"));
        contract.setElectronicContractId(getValueOrNull(generalInfo, "Идентификатор контракта, заключенного в электронной форме"));
        contract.setSoleSupplierBasis(getValueOrNull(generalInfo, "Основание заключения контракта с единственным поставщиком"));
        contract.setSoleSupplierDocumentDetails(getValueOrNull(generalInfo, "Реквизиты документа, подтверждающего основание заключения контракта"));
        contract.setBankingTreasurySupportInfo(getValueOrNull(generalInfo, "Информация о банковском и (или) казначейском сопровождении контракта"));

        contract.setConclusionDate(parseDate(getValueOrNull(commonData, "Дата заключения контракта")));
        contract.setContractNumber(getValueOrNull(commonData, "Номер контракта"));
        contract.setSubject(getValueOrNull(commonData, "Предмет контракта"));
        contract.setContractPrice(parseDouble(getValueOrNull(commonData, "Цена контракта")));
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

        // Особый случай для qualityGuaranteeInfo - собираем из нескольких полей
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
    }



//    public void fillSupplierModel(Supplier supplier, Map<String, Object> suppliersData) {
//        try {
//            // Получаем список поставщиков из данных (ключ "Поставщики" вместо "Информация о поставщиках")
//            List<Map<String, String>> suppliersList = (List<Map<String, String>>) suppliersData.get("Поставщики");
//
//            if (suppliersList != null && !suppliersList.isEmpty()) {
//                // Берем первого поставщика
//                Map<String, String> supplierInfo = suppliersList.get(0);
//
//                // Заполняем модель поставщика
//                supplier.setName(supplierInfo.get("Организация"));
//                supplier.setInn(supplierInfo.get("ИНН"));
//
//                // Обработка страны и кода страны
//                String country = supplierInfo.get("Страна");
//                String countryCode = supplierInfo.get("Код страны");
//                if (country != null) {
//                    supplier.setCountryName(country);
//                    if (countryCode != null) {
//                        supplier.setCountryCode(countryCode);
//                    }
//                }
//
//                // Адреса
//                supplier.setAddress(supplierInfo.get("Адрес места нахождения"));
//                supplier.setPostalAddress(supplierInfo.get("Почтовый адрес"));
//
//                // Контакты
//                supplier.setPhone(supplierInfo.get("Телефон"));
//                supplier.setEmail(supplierInfo.get("Email"));
//
//                // Дополнительные поля (если есть в данных)
//                if (supplierInfo.containsKey("ОГРН")) {
//                    supplier.setOgrn(supplierInfo.get("ОГРН"));
//                }
//                if (supplierInfo.containsKey("КПП")) {
//                    supplier.setKpp(supplierInfo.get("КПП"));
//                }
//
//                if (supplierInfo.containsKey("Статус")) {
//                    supplier.setStatus(supplierInfo.get("Статус"));
//                }
//
//                // Определяем тип поставщика
//                String orgName = supplierInfo.get("Организация");
//                if (orgName != null) {
//                    if (orgName.contains("ИП") || orgName.contains("Индивидуальный предприниматель")) {
//                        supplier.setType("Индивидуальный предприниматель");
//                    } else if (orgName.contains("ООО") || orgName.contains("АО") || orgName.contains("ПАО")) {
//                        supplier.setType("Юридическое лицо");
//                    } else {
//                        // Если не удалось определить по названию, проверяем ИНН
//                        String inn = supplierInfo.get("ИНН");
//                        if (inn != null && inn.length() == 12) { // ИНН физлица/ИП
//                            supplier.setType("Индивидуальный предприниматель");
//                        } else {
//                            supplier.setType("Юридическое лицо");
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("Ошибка при заполнении данных поставщика: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    private String getStringValue(Map<String, Object> map, String key) {
        return map.containsKey(key) ? map.get(key).toString() : null;
    }

    public static Map<String, String> getSectionData(Map<String, Object> participantData, String sectionName) {
        Object section = participantData.get(sectionName);
        if (section instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> sectionMap = (Map<String, String>) section;
            return sectionMap;
        }
        return null;
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
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim().replaceAll("[^\\d.]", ""),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            System.out.println("Ошибка парсинга даты: " + dateStr);
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
