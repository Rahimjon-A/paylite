package com.paylite.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paylite.domain.enumeration.AgentCardStatus;
import com.paylite.domain.enumeration.AgentCardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A AgentCard.
 */
@Entity
@Table(name = "agent_card")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AgentCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 16, max = 16)
    @Column(name = "pan", length = 16, nullable = false, unique = true)
    private String pan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AgentCardType type;

    @NotNull
    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AgentCardStatus status;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "agentCards" }, allowSetters = true)
    private Agent agent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AgentCard id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPan() {
        return this.pan;
    }

    public AgentCard pan(String pan) {
        this.setPan(pan);
        return this;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public AgentCardType getType() {
        return this.type;
    }

    public AgentCard type(AgentCardType type) {
        this.setType(type);
        return this;
    }

    public void setType(AgentCardType type) {
        this.type = type;
    }

    public LocalDate getExpireDate() {
        return this.expireDate;
    }

    public AgentCard expireDate(LocalDate expireDate) {
        this.setExpireDate(expireDate);
        return this;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public AgentCardStatus getStatus() {
        return this.status;
    }

    public AgentCard status(AgentCardStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AgentCardStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public AgentCard createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public AgentCard agent(Agent agent) {
        this.setAgent(agent);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentCard)) {
            return false;
        }
        return getId() != null && getId().equals(((AgentCard) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AgentCard{" +
            "id=" + getId() +
            ", pan='" + getPan() + "'" +
            ", type='" + getType() + "'" +
            ", expireDate='" + getExpireDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
