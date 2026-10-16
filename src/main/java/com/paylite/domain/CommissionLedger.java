package com.paylite.domain;

import com.paylite.domain.enumeration.AgentCardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A CommissionLedger.
 */
@Entity
@Table(name = "commission_ledger")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CommissionLedger implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId;

    @NotNull
    @Size(max = 16)
    @Column(name = "from_pan", length = 16, nullable = false)
    private String fromPan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private AgentCardType cardType;

    @NotNull
    @Column(name = "destination_account", nullable = false)
    private String destinationAccount;

    @NotNull
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @NotNull
    @Column(name = "transferred_amount", nullable = false)
    private Long transferredAmount;

    @NotNull
    @Column(name = "commission_amount", nullable = false)
    private Long commissionAmount;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CommissionLedger id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public CommissionLedger requestId(String requestId) {
        this.setRequestId(requestId);
        return this;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFromPan() {
        return this.fromPan;
    }

    public CommissionLedger fromPan(String fromPan) {
        this.setFromPan(fromPan);
        return this;
    }

    public void setFromPan(String fromPan) {
        this.fromPan = fromPan;
    }

    public AgentCardType getCardType() {
        return this.cardType;
    }

    public CommissionLedger cardType(AgentCardType cardType) {
        this.setCardType(cardType);
        return this;
    }

    public void setCardType(AgentCardType cardType) {
        this.cardType = cardType;
    }

    public String getDestinationAccount() {
        return this.destinationAccount;
    }

    public CommissionLedger destinationAccount(String destinationAccount) {
        this.setDestinationAccount(destinationAccount);
        return this;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public Long getTotalAmount() {
        return this.totalAmount;
    }

    public CommissionLedger totalAmount(Long totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTransferredAmount() {
        return this.transferredAmount;
    }

    public CommissionLedger transferredAmount(Long transferredAmount) {
        this.setTransferredAmount(transferredAmount);
        return this;
    }

    public void setTransferredAmount(Long transferredAmount) {
        this.transferredAmount = transferredAmount;
    }

    public Long getCommissionAmount() {
        return this.commissionAmount;
    }

    public CommissionLedger commissionAmount(Long commissionAmount) {
        this.setCommissionAmount(commissionAmount);
        return this;
    }

    public void setCommissionAmount(Long commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public CommissionLedger createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommissionLedger)) {
            return false;
        }
        return getId() != null && getId().equals(((CommissionLedger) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CommissionLedger{" +
            "id=" + getId() +
            ", requestId='" + getRequestId() + "'" +
            ", fromPan='" + getFromPan() + "'" +
            ", cardType='" + getCardType() + "'" +
            ", destinationAccount='" + getDestinationAccount() + "'" +
            ", totalAmount=" + getTotalAmount() +
            ", transferredAmount=" + getTransferredAmount() +
            ", commissionAmount=" + getCommissionAmount() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
