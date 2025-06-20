package Database.Models;


import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notice_number")
    private String noticeNumber; // номер извещения контракта

    @Column(name = "contract_status")
    private String contractStatus; // статус контракта

    @Column(name = "customer")
    private String customer; // Заказчик

    @Column(name = "contract_number")
    private String contractNumber; // номер Контракт

    @Column(name = "purchase_objects", columnDefinition = "TEXT")
    private String purchaseObjects; // Объекты закупки

    @Column(name = "contract_price")
    private Double contractPrice; // Цена контракта

    @Column(name = "contract_conclusion")
    private Date contractConclusion; // Заключение контракта

    @Column(name = "execution_period")
    private Date executionPeriod; // Срок исполнения

    @Column(name = "posted_date")
    private Date postedDate; // Размещен

    @Column(name = "updated_date")
    private Date updatedDate; // Обновлен

    @Column(name = "version")
    private String version; // Версия

    @Column(name = "last_parsing_update")
    private Date lastParsingUpdate; // Последнее обновление парсинга

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNoticeNumber() {
        return noticeNumber;
    }

    public void setNoticeNumber(String noticeNumber) {
        this.noticeNumber = noticeNumber;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getPurchaseObjects() {
        return purchaseObjects;
    }

    public void setPurchaseObjects(String purchaseObjects) {
        this.purchaseObjects = purchaseObjects;
    }

    public Double getContractPrice() {
        return contractPrice;
    }

    public void setContractPrice(Double contractPrice) {
        this.contractPrice = contractPrice;
    }

    public Date getContractConclusion() {
        return contractConclusion;
    }

    public void setContractConclusion(Date contractConclusion) {
        this.contractConclusion = contractConclusion;
    }

    public Date getExecutionPeriod() {
        return executionPeriod;
    }

    public void setExecutionPeriod(Date executionPeriod) {
        this.executionPeriod = executionPeriod;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Date postedDate) {
        this.postedDate = postedDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastParsingUpdate() {
        return lastParsingUpdate;
    }

    public void setLastParsingUpdate(Date lastParsingUpdate) {
        this.lastParsingUpdate = lastParsingUpdate;
    }
}