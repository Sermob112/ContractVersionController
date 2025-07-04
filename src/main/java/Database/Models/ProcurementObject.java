package Database.Models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "procurement_objects")
public class ProcurementObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",  columnDefinition = "TEXT")

    private String name;

@Column(name = "type",  columnDefinition = "TEXT")
private String type;

@Column(name = "ktru_okpd2_codes",  columnDefinition = "TEXT")
private String ktruOkpd2Codes;

@Column(name = "quantity", precision = 19, scale = 3)

private BigDecimal quantity;

@Column(name = "unit",columnDefinition = "TEXT")
private String unit;

@Column(name = "price_per_unit", precision = 19, scale = 2)

private BigDecimal pricePerUnit;

@Column(name = "vat_rate", columnDefinition = "TEXT")

private String vatRate;

@Column(name = "country_of_origin",   columnDefinition = "TEXT")
private String countryOfOrigin;

@Column(name = "total_amount", precision = 19, scale = 2)

private BigDecimal totalAmount;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "contract_id")
private Contract contract;

public void setId(Long id) {
    this.id = id;
}

public void setName(String name) {
    this.name = name;
}

public void setType(String type) {
    this.type = type;
}

public void setKtruOkpd2Codes(String ktruOkpd2Codes) {
    this.ktruOkpd2Codes = ktruOkpd2Codes;
}

public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
}

public void setUnit(String unit) {
    this.unit = unit;
}

public void setPricePerUnit(BigDecimal pricePerUnit) {
    this.pricePerUnit = pricePerUnit;
}

public void setVatRate(String vatRate) {
    this.vatRate = vatRate;
}

public void setCountryOfOrigin(String countryOfOrigin) {
    this.countryOfOrigin = countryOfOrigin;
}

public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
}
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getKtruOkpd2Codes() {
        return ktruOkpd2Codes;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public String getVatRate() {
        return vatRate;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

}
