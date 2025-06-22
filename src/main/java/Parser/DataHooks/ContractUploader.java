package Parser.DataHooks;

import Database.Models.Contract;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ContractUploader {

    private final EntityManagerFactory emf;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public ContractUploader() {
        this.emf = Persistence.createEntityManagerFactory("your-persistence-unit-name");
    }

    public void uploadContracts(Map<String, Map<String, String>> contractsData) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            for (Map.Entry<String, Map<String, String>> entry : contractsData.entrySet()) {
                String contractUrl = entry.getKey();
                Map<String, String> contractInfo = entry.getValue();

                Contract contract = new Contract();

                // Заполнение полей контракта
                try {
                    // Основная информация
                    contract.setNoticeNumber(contractUrl); // Используем URL как номер извещения
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

                    // Сохранение или обновление контракта
                    em.persist(contract);

                } catch (Exception e) {
                    System.err.println("Ошибка при обработке контракта " + contractUrl + ": " + e.getMessage());
                    // Продолжаем обработку следующих контрактов
                }
            }

            em.getTransaction().commit();
            System.out.println("Успешно загружено контрактов: " + contractsData.size());

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Ошибка при загрузке контрактов: " + e.getMessage());
        } finally {
            em.close();
        }
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

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}