package com.paylite.domain.enumeration;

/**
 * The P2POperationStatus enumeration.
 */
public enum P2POperationStatus {
    CREATED,
    VALIDATING,
    BALANCE_CHECKED,
    WITHDRAWN,
    PAYING,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED,
    COMPENSATION_FAILED,
}
