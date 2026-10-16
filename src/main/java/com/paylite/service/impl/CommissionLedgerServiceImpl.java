package com.paylite.service.impl;

import com.paylite.domain.CommissionLedger;
import com.paylite.repository.CommissionLedgerRepository;
import com.paylite.service.CommissionLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CommissionLedgerServiceImpl implements CommissionLedgerService {

    private static final Logger LOG = LoggerFactory.getLogger(CommissionLedgerServiceImpl.class);
    private final CommissionLedgerRepository commissionLedgerRepository;

    public CommissionLedgerServiceImpl(CommissionLedgerRepository commissionLedgerRepository) {
        this.commissionLedgerRepository = commissionLedgerRepository;
    }

    @Override
    public CommissionLedger record(CommissionLedger commissionLedger) {
        LOG.debug(
            "NEW commission ledger recoded with fromPan {}, totalAmount {}, commission {}",
            commissionLedger.getFromPan(),
            commissionLedger.getTotalAmount(),
            commissionLedger.getCommissionAmount()
        );

        return commissionLedgerRepository.save(commissionLedger);
    }
}
