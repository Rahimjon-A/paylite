package com.paylite.service.impl;

import com.paylite.domain.dto.CardInfo;
import com.paylite.domain.enumeration.AgentCardStatus;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class CardValidationService {

    public void validate(CardInfo card) {
        if (card.status() != AgentCardStatus.ACTIVE) {
            throw new IllegalStateException("Card " + card.pan() + " is not active");
        }

        if (card.expireDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Card " + card.pan() + " is expired");
        }
    }
}
