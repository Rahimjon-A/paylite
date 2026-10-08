package com.paylite.web.rest;

import static com.paylite.domain.P2POperationAsserts.*;
import static com.paylite.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.IntegrationTest;
import com.paylite.domain.P2POperation;
import com.paylite.domain.enumeration.AgentCardType;
import com.paylite.domain.enumeration.P2POperationStatus;
import com.paylite.repository.P2PRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link P2PResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class P2PResourceIT {

    private static final String DEFAULT_REQUEST_ID = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_ID = "BBBBBBBBBB";

    private static final Long DEFAULT_AMOUNT = 1L;
    private static final Long UPDATED_AMOUNT = 2L;

    private static final Long DEFAULT_COMMISSION_AMOUNT = 1L;
    private static final Long UPDATED_COMMISSION_AMOUNT = 2L;

    private static final Long DEFAULT_TOTAL_AMOUNT = 1L;
    private static final Long UPDATED_TOTAL_AMOUNT = 2L;

    private static final String DEFAULT_FROM_PAN = "AAAAAAAAAA";
    private static final String UPDATED_FROM_PAN = "BBBBBBBBBB";

    private static final AgentCardType DEFAULT_FROM_TYPE = AgentCardType.UZCARD;
    private static final AgentCardType UPDATED_FROM_TYPE = AgentCardType.HUMO;

    private static final LocalDate DEFAULT_FROM_EXPIRE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_FROM_EXPIRE_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_TO_PAN = "AAAAAAAAAA";
    private static final String UPDATED_TO_PAN = "BBBBBBBBBB";

    private static final AgentCardType DEFAULT_TO_TYPE = AgentCardType.UZCARD;
    private static final AgentCardType UPDATED_TO_TYPE = AgentCardType.HUMO;

    private static final LocalDate DEFAULT_TO_EXPIRE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TO_EXPIRE_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final P2POperationStatus DEFAULT_STATUS = P2POperationStatus.CREATED;
    private static final P2POperationStatus UPDATED_STATUS = P2POperationStatus.VALIDATING;

    private static final String DEFAULT_FAILURE_REASON = "AAAAAAAAAA";
    private static final String UPDATED_FAILURE_REASON = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/p-2-p-operations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private P2PRepository p2PRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restP2POperationMockMvc;

    private P2POperation p2POperation;

    private P2POperation insertedP2POperation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2POperation createEntity() {
        return new P2POperation()
            .requestId(DEFAULT_REQUEST_ID)
            .amount(DEFAULT_AMOUNT)
            .commissionAmount(DEFAULT_COMMISSION_AMOUNT)
            .totalAmount(DEFAULT_TOTAL_AMOUNT)
            .fromPan(DEFAULT_FROM_PAN)
            .fromType(DEFAULT_FROM_TYPE)
            .fromExpireDate(DEFAULT_FROM_EXPIRE_DATE)
            .toPan(DEFAULT_TO_PAN)
            .toType(DEFAULT_TO_TYPE)
            .toExpireDate(DEFAULT_TO_EXPIRE_DATE)
            .status(DEFAULT_STATUS)
            .failureReason(DEFAULT_FAILURE_REASON)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static P2POperation createUpdatedEntity() {
        return new P2POperation()
            .requestId(UPDATED_REQUEST_ID)
            .amount(UPDATED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .fromPan(UPDATED_FROM_PAN)
            .fromType(UPDATED_FROM_TYPE)
            .fromExpireDate(UPDATED_FROM_EXPIRE_DATE)
            .toPan(UPDATED_TO_PAN)
            .toType(UPDATED_TO_TYPE)
            .toExpireDate(UPDATED_TO_EXPIRE_DATE)
            .status(UPDATED_STATUS)
            .failureReason(UPDATED_FAILURE_REASON)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    void initTest() {
        p2POperation = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedP2POperation != null) {
            p2PRepository.delete(insertedP2POperation);
            insertedP2POperation = null;
        }
    }

    @Test
    @Transactional
    void createP2POperation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the P2POperation
        var returnedP2POperation = om.readValue(
            restP2POperationMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            P2POperation.class
        );

        // Validate the P2POperation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertP2POperationUpdatableFieldsEquals(returnedP2POperation, getPersistedP2POperation(returnedP2POperation));

        insertedP2POperation = returnedP2POperation;
    }

    @Test
    @Transactional
    void createP2POperationWithExistingId() throws Exception {
        // Create the P2POperation with an existing ID
        p2POperation.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRequestIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setRequestId(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setAmount(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCommissionAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setCommissionAmount(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setTotalAmount(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFromPanIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setFromPan(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFromTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setFromType(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFromExpireDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setFromExpireDate(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkToPanIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setToPan(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkToTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setToType(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkToExpireDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setToExpireDate(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setStatus(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setCreatedAt(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUpdatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        p2POperation.setUpdatedAt(null);

        // Create the P2POperation, which fails.

        restP2POperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllP2POperations() throws Exception {
        // Initialize the database
        insertedP2POperation = p2PRepository.saveAndFlush(p2POperation);

        // Get all the p2POperationList
        restP2POperationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(p2POperation.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestId").value(hasItem(DEFAULT_REQUEST_ID)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].commissionAmount").value(hasItem(DEFAULT_COMMISSION_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(DEFAULT_TOTAL_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].fromPan").value(hasItem(DEFAULT_FROM_PAN)))
            .andExpect(jsonPath("$.[*].fromType").value(hasItem(DEFAULT_FROM_TYPE.toString())))
            .andExpect(jsonPath("$.[*].fromExpireDate").value(hasItem(DEFAULT_FROM_EXPIRE_DATE.toString())))
            .andExpect(jsonPath("$.[*].toPan").value(hasItem(DEFAULT_TO_PAN)))
            .andExpect(jsonPath("$.[*].toType").value(hasItem(DEFAULT_TO_TYPE.toString())))
            .andExpect(jsonPath("$.[*].toExpireDate").value(hasItem(DEFAULT_TO_EXPIRE_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].failureReason").value(hasItem(DEFAULT_FAILURE_REASON)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getP2POperation() throws Exception {
        // Initialize the database
        insertedP2POperation = p2PRepository.saveAndFlush(p2POperation);

        // Get the p2POperation
        restP2POperationMockMvc
            .perform(get(ENTITY_API_URL_ID, p2POperation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(p2POperation.getId().intValue()))
            .andExpect(jsonPath("$.requestId").value(DEFAULT_REQUEST_ID))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.commissionAmount").value(DEFAULT_COMMISSION_AMOUNT.intValue()))
            .andExpect(jsonPath("$.totalAmount").value(DEFAULT_TOTAL_AMOUNT.intValue()))
            .andExpect(jsonPath("$.fromPan").value(DEFAULT_FROM_PAN))
            .andExpect(jsonPath("$.fromType").value(DEFAULT_FROM_TYPE.toString()))
            .andExpect(jsonPath("$.fromExpireDate").value(DEFAULT_FROM_EXPIRE_DATE.toString()))
            .andExpect(jsonPath("$.toPan").value(DEFAULT_TO_PAN))
            .andExpect(jsonPath("$.toType").value(DEFAULT_TO_TYPE.toString()))
            .andExpect(jsonPath("$.toExpireDate").value(DEFAULT_TO_EXPIRE_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.failureReason").value(DEFAULT_FAILURE_REASON))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingP2POperation() throws Exception {
        // Get the p2POperation
        restP2POperationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingP2POperation() throws Exception {
        // Initialize the database
        insertedP2POperation = p2PRepository.saveAndFlush(p2POperation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the p2POperation
        P2POperation updatedP2POperation = p2PRepository.findById(p2POperation.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedP2POperation are not directly saved in db
        em.detach(updatedP2POperation);
        updatedP2POperation
            .requestId(UPDATED_REQUEST_ID)
            .amount(UPDATED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .fromPan(UPDATED_FROM_PAN)
            .fromType(UPDATED_FROM_TYPE)
            .fromExpireDate(UPDATED_FROM_EXPIRE_DATE)
            .toPan(UPDATED_TO_PAN)
            .toType(UPDATED_TO_TYPE)
            .toExpireDate(UPDATED_TO_EXPIRE_DATE)
            .status(UPDATED_STATUS)
            .failureReason(UPDATED_FAILURE_REASON)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restP2POperationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedP2POperation.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedP2POperation))
            )
            .andExpect(status().isOk());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedP2POperationToMatchAllProperties(updatedP2POperation);
    }

    @Test
    @Transactional
    void putNonExistingP2POperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        p2POperation.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2POperationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, p2POperation.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(p2POperation))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchP2POperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        p2POperation.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2POperationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(p2POperation))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamP2POperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        p2POperation.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2POperationMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(p2POperation)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateP2POperationWithPatch() throws Exception {
        // Initialize the database
        insertedP2POperation = p2PRepository.saveAndFlush(p2POperation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the p2POperation using partial update
        P2POperation partialUpdatedP2POperation = new P2POperation();
        partialUpdatedP2POperation.setId(p2POperation.getId());

        partialUpdatedP2POperation
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .fromExpireDate(UPDATED_FROM_EXPIRE_DATE)
            .toPan(UPDATED_TO_PAN)
            .toType(UPDATED_TO_TYPE)
            .createdAt(UPDATED_CREATED_AT);

        restP2POperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2POperation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedP2POperation))
            )
            .andExpect(status().isOk());

        // Validate the P2POperation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertP2POperationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedP2POperation, p2POperation),
            getPersistedP2POperation(p2POperation)
        );
    }

    @Test
    @Transactional
    void fullUpdateP2POperationWithPatch() throws Exception {
        // Initialize the database
        insertedP2POperation = p2PRepository.saveAndFlush(p2POperation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the p2POperation using partial update
        P2POperation partialUpdatedP2POperation = new P2POperation();
        partialUpdatedP2POperation.setId(p2POperation.getId());

        partialUpdatedP2POperation
            .requestId(UPDATED_REQUEST_ID)
            .amount(UPDATED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .fromPan(UPDATED_FROM_PAN)
            .fromType(UPDATED_FROM_TYPE)
            .fromExpireDate(UPDATED_FROM_EXPIRE_DATE)
            .toPan(UPDATED_TO_PAN)
            .toType(UPDATED_TO_TYPE)
            .toExpireDate(UPDATED_TO_EXPIRE_DATE)
            .status(UPDATED_STATUS)
            .failureReason(UPDATED_FAILURE_REASON)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restP2POperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedP2POperation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedP2POperation))
            )
            .andExpect(status().isOk());

        // Validate the P2POperation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertP2POperationUpdatableFieldsEquals(partialUpdatedP2POperation, getPersistedP2POperation(partialUpdatedP2POperation));
    }

    @Test
    @Transactional
    void patchNonExistingP2POperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        p2POperation.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restP2POperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, p2POperation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(p2POperation))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchP2POperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        p2POperation.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2POperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(p2POperation))
            )
            .andExpect(status().isBadRequest());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamP2POperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        p2POperation.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restP2POperationMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(p2POperation))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the P2POperation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteP2POperation() throws Exception {
        // Initialize the database
        insertedP2POperation = p2PRepository.saveAndFlush(p2POperation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the p2POperation
        restP2POperationMockMvc
            .perform(delete(ENTITY_API_URL_ID, p2POperation.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return p2PRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected P2POperation getPersistedP2POperation(P2POperation p2POperation) {
        return p2PRepository.findById(p2POperation.getId()).orElseThrow();
    }

    protected void assertPersistedP2POperationToMatchAllProperties(P2POperation expectedP2POperation) {
        assertP2POperationAllPropertiesEquals(expectedP2POperation, getPersistedP2POperation(expectedP2POperation));
    }

    protected void assertPersistedP2POperationToMatchUpdatableProperties(P2POperation expectedP2POperation) {
        assertP2POperationAllUpdatablePropertiesEquals(expectedP2POperation, getPersistedP2POperation(expectedP2POperation));
    }
}
