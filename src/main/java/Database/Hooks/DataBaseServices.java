package Database.Hooks;


import Database.Models.Contract;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.Date;
import java.util.List;

public class DataBaseServices {

    // Add a new contract to the database
    public static void addContract(Contract contract) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(contract);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static Date getLastParsingDate() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Contract firstContract = session.createQuery("from Contract", Contract.class)
                    .setMaxResults(1)
                    .uniqueResult();

            if (firstContract != null) {
                return firstContract.getLastParsingUpdate();
            } else {
                System.out.println("Нет записей в таблице Contract.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении даты последнего парсинга:");
            e.printStackTrace();
            return null;
        }
    }

    // Update an existing contract
    public static void updateContract(Contract contract) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(contract);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // Find contract by notice number and update if exists, otherwise add new
    public static void addOrUpdateContract(Contract contract) {
        Contract existing = findContractByNoticeNumber(contract.getNoticeNumber());
        if (existing != null) {
            // Update the existing contract with new data
            existing.setContractStatus(contract.getContractStatus());
            existing.setCustomer(contract.getCustomer());
            existing.setContractNumber(contract.getContractNumber());
            existing.setPurchaseObjects(contract.getPurchaseObjects());
            existing.setContractPrice(contract.getContractPrice());
            existing.setContractConclusion(contract.getContractConclusion());
            existing.setExecutionPeriod(contract.getExecutionPeriod());
            existing.setPostedDate(contract.getPostedDate());
            existing.setUpdatedDate(contract.getUpdatedDate());
            existing.setVersion(contract.getVersion());
            existing.setLastParsingUpdate(new Date()); // Update parsing timestamp

            updateContract(existing);
        } else {
            contract.setLastParsingUpdate(new Date());
            addContract(contract);
        }
    }

    // Find contract by notice number
    public static Contract findContractByNoticeNumber(String noticeNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Contract> query = session.createQuery(
                    "FROM Contract WHERE noticeNumber = :noticeNumber", Contract.class);
            query.setParameter("noticeNumber", noticeNumber);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Batch insert/update for multiple contracts
    public static void batchProcessContracts(List<Contract> contracts) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            for (int i = 0; i < contracts.size(); i++) {
                Contract contract = contracts.get(i);
                Contract existing = findContractByNoticeNumber(contract.getNoticeNumber());

                if (existing != null) {
                    // Update existing
                    existing.setContractStatus(contract.getContractStatus());
                    existing.setCustomer(contract.getCustomer());
                    existing.setContractNumber(contract.getContractNumber());
                    existing.setPurchaseObjects(contract.getPurchaseObjects());
                    existing.setContractPrice(contract.getContractPrice());
                    existing.setContractConclusion(contract.getContractConclusion());
                    existing.setExecutionPeriod(contract.getExecutionPeriod());
                    existing.setPostedDate(contract.getPostedDate());
                    existing.setUpdatedDate(contract.getUpdatedDate());
                    existing.setVersion(contract.getVersion());
                    existing.setLastParsingUpdate(new Date());

                    session.merge(existing);
                } else {
                    // Add new
                    contract.setLastParsingUpdate(new Date());
                    session.persist(contract);
                }

                // Flush and clear session periodically to manage memory
                if (i % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // Get all contracts (for testing/debugging)
    public static List<Contract> getAllContracts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Contract", Contract.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}