package com.paylite.domain;

import com.paylite.domain.enumeration.AgentCardType;
import com.paylite.domain.enumeration.P2POperationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A P2POperation.
 */
@Entity
@Table(name = "p2poperation")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class P2POperation implements Serializable {

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
    @Column(name = "amount", nullable = false)
    private Long amount;

    @NotNull
    @Column(name = "commission_amount", nullable = false)
    private Long commissionAmount;

    @NotNull
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @NotNull
    @Column(name = "from_pan", nullable = false)
    private String fromPan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_type", nullable = false)
    private AgentCardType fromType;

    @NotNull
    @Column(name = "from_expire_date", nullable = false)
    private LocalDate fromExpireDate;

    @NotNull
    @Column(name = "to_pan", nullable = false)
    private String toPan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_type", nullable = false)
    private AgentCardType toType;

    @NotNull
    @Column(name = "to_expire_date", nullable = false)
    private LocalDate toExpireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private P2POperationStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public P2POperation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public P2POperation requestId(String requestId) {
        this.setRequestId(requestId);
        return this;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getAmount() {
        return this.amount;
    }

    public P2POperation amount(Long amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getCommissionAmount() {
        return this.commissionAmount;
    }

    public P2POperation commissionAmount(Long commissionAmount) {
        this.setCommissionAmount(commissionAmount);
        return this;
    }

    public void setCommissionAmount(Long commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public Long getTotalAmount() {
        return this.totalAmount;
    }

    public P2POperation totalAmount(Long totalAmount) {
        this.setTotalAmount(totalAmount);
        return this;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getFromPan() {
        return this.fromPan;
    }

    public P2POperation fromPan(String fromPan) {
        this.setFromPan(fromPan);
        return this;
    }

    public void setFromPan(String fromPan) {
        this.fromPan = fromPan;
    }

    public AgentCardType getFromType() {
        return this.fromType;
    }

    public P2POperation fromType(AgentCardType fromType) {
        this.setFromType(fromType);
        return this;
    }

    public void setFromType(AgentCardType fromType) {
        this.fromType = fromType;
    }

    public LocalDate getFromExpireDate() {
        return this.fromExpireDate;
    }

    public P2POperation fromExpireDate(LocalDate fromExpireDate) {
        this.setFromExpireDate(fromExpireDate);
        return this;
    }

    public void setFromExpireDate(LocalDate fromExpireDate) {
        this.fromExpireDate = fromExpireDate;
    }

    public String getToPan() {
        return this.toPan;
    }

    public P2POperation toPan(String toPan) {
        this.setToPan(toPan);
        return this;
    }

    public void setToPan(String toPan) {
        this.toPan = toPan;
    }

    public AgentCardType getToType() {
        return this.toType;
    }

    public P2POperation toType(AgentCardType toType) {
        this.setToType(toType);
        return this;
    }

    public void setToType(AgentCardType toType) {
        this.toType = toType;
    }

    public LocalDate getToExpireDate() {
        return this.toExpireDate;
    }

    public P2POperation toExpireDate(LocalDate toExpireDate) {
        this.setToExpireDate(toExpireDate);
        return this;
    }

    public void setToExpireDate(LocalDate toExpireDate) {
        this.toExpireDate = toExpireDate;
    }

    public P2POperationStatus getStatus() {
        return this.status;
    }

    public P2POperation status(P2POperationStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(P2POperationStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return this.failureReason;
    }

    public P2POperation failureReason(String failureReason) {
        this.setFailureReason(failureReason);
        return this;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public P2POperation createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public P2POperation updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof P2POperation)) {
            return false;
        }
        return getId() != null && getId().equals(((P2POperation) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "P2POperation{" +
            "id=" + getId() +
            ", requestId='" + getRequestId() + "'" +
            ", amount=" + getAmount() +
            ", commissionAmount=" + getCommissionAmount() +
            ", totalAmount=" + getTotalAmount() +
            ", fromPan='" + getFromPan() + "'" +
            ", fromType='" + getFromType() + "'" +
            ", fromExpireDate='" + getFromExpireDate() + "'" +
            ", toPan='" + getToPan() + "'" +
            ", toType='" + getToType() + "'" +
            ", toExpireDate='" + getToExpireDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", failureReason='" + getFailureReason() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
