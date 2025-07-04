package Database.Models;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts")
public class Contract {
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

    public String getPurchaseObjects() {
        return purchaseObjects;
    }

    public void setPurchaseObjects(String purchaseObjects) {
        this.purchaseObjects = purchaseObjects;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDate postedDate) {
        this.postedDate = postedDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getLastParsingUpdate() {
        return lastParsingUpdate;
    }

    public void setLastParsingUpdate(LocalDate lastParsingUpdate) {
        this.lastParsingUpdate = lastParsingUpdate;
    }

    public String getRegistryNumber() {
        return registryNumber;
    }

    public void setRegistryNumber(String registryNumber) {
        this.registryNumber = registryNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcurementNoticeNumber() {
        return procurementNoticeNumber;
    }

    public void setProcurementNoticeNumber(String procurementNoticeNumber) {
        this.procurementNoticeNumber = procurementNoticeNumber;
    }

    public String getProcurementIdentificationCode() {
        return procurementIdentificationCode;
    }

    public void setProcurementIdentificationCode(String procurementIdentificationCode) {
        this.procurementIdentificationCode = procurementIdentificationCode;
    }

    public String getElectronicContractId() {
        return electronicContractId;
    }

    public void setElectronicContractId(String electronicContractId) {
        this.electronicContractId = electronicContractId;
    }

    public String getSoleSupplierBasis() {
        return soleSupplierBasis;
    }

    public void setSoleSupplierBasis(String soleSupplierBasis) {
        this.soleSupplierBasis = soleSupplierBasis;
    }

    public String getSoleSupplierDocumentDetails() {
        return soleSupplierDocumentDetails;
    }

    public void setSoleSupplierDocumentDetails(String soleSupplierDocumentDetails) {
        this.soleSupplierDocumentDetails = soleSupplierDocumentDetails;
    }

    public String getBankingTreasurySupportInfo() {
        return bankingTreasurySupportInfo;
    }

    public void setBankingTreasurySupportInfo(String bankingTreasurySupportInfo) {
        this.bankingTreasurySupportInfo = bankingTreasurySupportInfo;
    }

    public LocalDate getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDate conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Double getContractPrice() {
        return contractPrice;
    }

    public void setContractPrice(Double contractPrice) {
        this.contractPrice = contractPrice;
    }

    public BigDecimal getIncludingVat() {
        return includingVat;
    }

    public void setIncludingVat(BigDecimal includingVat) {
        this.includingVat = includingVat;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getExecutionPeriod() {
        return executionPeriod;
    }

    public void setExecutionPeriod(LocalDate executionPeriod) {
        this.executionPeriod = executionPeriod;
    }

    public String getContractStageId() {
        return contractStageId;
    }

    public void setContractStageId(String contractStageId) {
        this.contractStageId = contractStageId;
    }

    public BigDecimal getAdvanceAmount() {
        return advanceAmount;
    }

    public void setAdvanceAmount(BigDecimal advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public String getPenaltyDeductionApplied() {
        return penaltyDeductionApplied;
    }

    public void setPenaltyDeductionApplied(String penaltyDeductionApplied) {
        this.penaltyDeductionApplied = penaltyDeductionApplied;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public BigDecimal getTreasuryGuaranteeAmount() {
        return treasuryGuaranteeAmount;
    }

    public void setTreasuryGuaranteeAmount(BigDecimal treasuryGuaranteeAmount) {
        this.treasuryGuaranteeAmount = treasuryGuaranteeAmount;
    }

    public String getNationalRegimeInfo() {
        return nationalRegimeInfo;
    }

    public void setNationalRegimeInfo(String nationalRegimeInfo) {
        this.nationalRegimeInfo = nationalRegimeInfo;
    }

    public String getContractGuaranteeInfo() {
        return contractGuaranteeInfo;
    }

    public void setContractGuaranteeInfo(String contractGuaranteeInfo) {
        this.contractGuaranteeInfo = contractGuaranteeInfo;
    }

    public String getQualityGuaranteeInfo() {
        return qualityGuaranteeInfo;
    }

    public void setQualityGuaranteeInfo(String qualityGuaranteeInfo) {
        this.qualityGuaranteeInfo = qualityGuaranteeInfo;
    }

    public String getDeliveryPlaceInfo() {
        return deliveryPlaceInfo;
    }

    public void setDeliveryPlaceInfo(String deliveryPlaceInfo) {
        this.deliveryPlaceInfo = deliveryPlaceInfo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Из старой модели
    @Column(name = "notice_number", columnDefinition = "TEXT")
    private String noticeNumber;

    @Column(name = "contract_status", columnDefinition = "TEXT")
    private String contractStatus;

    @Column(name = "customer", columnDefinition = "TEXT")
    private String customer;

    @Column(name = "purchase_objects", columnDefinition = "TEXT")
    private String purchaseObjects;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "version", columnDefinition = "TEXT")
    private String version;

    @Column(name = "last_parsing_update")
    private LocalDate lastParsingUpdate;

    // Из новой модели (расширенной)
    @Column(name = "registry_number", columnDefinition = "TEXT")
    private String registryNumber;

    @Column(name = "status", columnDefinition = "TEXT")
    private String status;

    @Column(name = "procurement_notice_number", columnDefinition = "TEXT")
    private String procurementNoticeNumber;

    @Column(name = "procurement_identification_code", columnDefinition = "TEXT")
    private String procurementIdentificationCode;

    @Column(name = "electronic_contract_id", columnDefinition = "TEXT")
    private String electronicContractId;

    @Column(name = "sole_supplier_basis", columnDefinition = "TEXT")
    private String soleSupplierBasis;

    @Column(name = "sole_supplier_document_details", columnDefinition = "TEXT")
    private String soleSupplierDocumentDetails;

    @Column(name = "banking_treasury_support_info", columnDefinition = "TEXT")
    private String bankingTreasurySupportInfo;

    @Column(name = "conclusion_date")
    private LocalDate conclusionDate;

    @Column(name = "contract_number", columnDefinition = "TEXT")
    private String contractNumber;

    @Column(name = "subject", columnDefinition = "TEXT")
    private String subject;

    @Column(name = "contract_price")
    private Double contractPrice;

    @Column(name = "including_vat", precision = 19, scale = 2)
    private BigDecimal includingVat;

    @Column(name = "currency", columnDefinition = "TEXT")
    private String currency;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "execution_period")
    private LocalDate executionPeriod;

    @Column(name = "contract_stage_id", columnDefinition = "TEXT")
    private String contractStageId;

    @Column(name = "advance_amount", precision = 19, scale = 2)
    private BigDecimal advanceAmount;

    @Column(name = "penalty_deduction_applied", columnDefinition = "TEXT")
    private String penaltyDeductionApplied;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "treasury_guarantee_amount", precision = 19, scale = 2)
    private BigDecimal treasuryGuaranteeAmount;

    @Column(name = "national_regime_info", columnDefinition = "TEXT")
    private String nationalRegimeInfo;

    @Column(name = "contract_guarantee_info", columnDefinition = "TEXT")
    private String contractGuaranteeInfo;

    @Column(name = "quality_guarantee_info", columnDefinition = "TEXT")
    private String qualityGuaranteeInfo;

    @Column(name = "delivery_place_info", columnDefinition = "TEXT")
    private String deliveryPlaceInfo;
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcurementObject> procurementObjects = new ArrayList<>();

    
    public List<ProcurementObject> getProcurementObjects() {
        return procurementObjects;
    }

    public void setProcurementObjects(List<ProcurementObject> procurementObjects) {
        this.procurementObjects = procurementObjects;
    }



}