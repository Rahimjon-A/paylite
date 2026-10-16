package com.paylite.web.rest;

import static com.paylite.domain.CommissionLedgerAsserts.*;
import static com.paylite.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paylite.IntegrationTest;
import com.paylite.domain.CommissionLedger;
import com.paylite.domain.enumeration.AgentCardType;
import com.paylite.repository.CommissionLedgerRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
 * Integration tests for the {@link CommissionLedgerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CommissionLedgerResourceIT {

    private static final String DEFAULT_REQUEST_ID = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_ID = "BBBBBBBBBB";

    private static final String DEFAULT_FROM_PAN = "AAAAAAAAAA";
    private static final String UPDATED_FROM_PAN = "BBBBBBBBBB";

    private static final AgentCardType DEFAULT_CARD_TYPE = AgentCardType.UZCARD;
    private static final AgentCardType UPDATED_CARD_TYPE = AgentCardType.HUMO;

    private static final String DEFAULT_DESTINATION_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_DESTINATION_ACCOUNT = "BBBBBBBBBB";

    private static final Long DEFAULT_TOTAL_AMOUNT = 1L;
    private static final Long UPDATED_TOTAL_AMOUNT = 2L;

    private static final Long DEFAULT_TRANSFERRED_AMOUNT = 1L;
    private static final Long UPDATED_TRANSFERRED_AMOUNT = 2L;

    private static final Long DEFAULT_COMMISSION_AMOUNT = 1L;
    private static final Long UPDATED_COMMISSION_AMOUNT = 2L;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/commission-ledgers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CommissionLedgerRepository commissionLedgerRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCommissionLedgerMockMvc;

    private CommissionLedger commissionLedger;

    private CommissionLedger insertedCommissionLedger;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CommissionLedger createEntity() {
        return new CommissionLedger()
            .requestId(DEFAULT_REQUEST_ID)
            .fromPan(DEFAULT_FROM_PAN)
            .cardType(DEFAULT_CARD_TYPE)
            .destinationAccount(DEFAULT_DESTINATION_ACCOUNT)
            .totalAmount(DEFAULT_TOTAL_AMOUNT)
            .transferredAmount(DEFAULT_TRANSFERRED_AMOUNT)
            .commissionAmount(DEFAULT_COMMISSION_AMOUNT)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CommissionLedger createUpdatedEntity() {
        return new CommissionLedger()
            .requestId(UPDATED_REQUEST_ID)
            .fromPan(UPDATED_FROM_PAN)
            .cardType(UPDATED_CARD_TYPE)
            .destinationAccount(UPDATED_DESTINATION_ACCOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .transferredAmount(UPDATED_TRANSFERRED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        commissionLedger = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCommissionLedger != null) {
            commissionLedgerRepository.delete(insertedCommissionLedger);
            insertedCommissionLedger = null;
        }
    }

    @Test
    @Transactional
    void createCommissionLedger() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CommissionLedger
        var returnedCommissionLedger = om.readValue(
            restCommissionLedgerMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(commissionLedger))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CommissionLedger.class
        );

        // Validate the CommissionLedger in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCommissionLedgerUpdatableFieldsEquals(returnedCommissionLedger, getPersistedCommissionLedger(returnedCommissionLedger));

        insertedCommissionLedger = returnedCommissionLedger;
    }

    @Test
    @Transactional
    void createCommissionLedgerWithExistingId() throws Exception {
        // Create the CommissionLedger with an existing ID
        commissionLedger.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRequestIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setRequestId(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFromPanIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setFromPan(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCardTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setCardType(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDestinationAccountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setDestinationAccount(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setTotalAmount(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTransferredAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setTransferredAmount(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCommissionAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setCommissionAmount(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        commissionLedger.setCreatedAt(null);

        // Create the CommissionLedger, which fails.

        restCommissionLedgerMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCommissionLedgers() throws Exception {
        // Initialize the database
        insertedCommissionLedger = commissionLedgerRepository.saveAndFlush(commissionLedger);

        // Get all the commissionLedgerList
        restCommissionLedgerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(commissionLedger.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestId").value(hasItem(DEFAULT_REQUEST_ID)))
            .andExpect(jsonPath("$.[*].fromPan").value(hasItem(DEFAULT_FROM_PAN)))
            .andExpect(jsonPath("$.[*].cardType").value(hasItem(DEFAULT_CARD_TYPE.toString())))
            .andExpect(jsonPath("$.[*].destinationAccount").value(hasItem(DEFAULT_DESTINATION_ACCOUNT)))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(DEFAULT_TOTAL_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].transferredAmount").value(hasItem(DEFAULT_TRANSFERRED_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].commissionAmount").value(hasItem(DEFAULT_COMMISSION_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getCommissionLedger() throws Exception {
        // Initialize the database
        insertedCommissionLedger = commissionLedgerRepository.saveAndFlush(commissionLedger);

        // Get the commissionLedger
        restCommissionLedgerMockMvc
            .perform(get(ENTITY_API_URL_ID, commissionLedger.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(commissionLedger.getId().intValue()))
            .andExpect(jsonPath("$.requestId").value(DEFAULT_REQUEST_ID))
            .andExpect(jsonPath("$.fromPan").value(DEFAULT_FROM_PAN))
            .andExpect(jsonPath("$.cardType").value(DEFAULT_CARD_TYPE.toString()))
            .andExpect(jsonPath("$.destinationAccount").value(DEFAULT_DESTINATION_ACCOUNT))
            .andExpect(jsonPath("$.totalAmount").value(DEFAULT_TOTAL_AMOUNT.intValue()))
            .andExpect(jsonPath("$.transferredAmount").value(DEFAULT_TRANSFERRED_AMOUNT.intValue()))
            .andExpect(jsonPath("$.commissionAmount").value(DEFAULT_COMMISSION_AMOUNT.intValue()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingCommissionLedger() throws Exception {
        // Get the commissionLedger
        restCommissionLedgerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCommissionLedger() throws Exception {
        // Initialize the database
        insertedCommissionLedger = commissionLedgerRepository.saveAndFlush(commissionLedger);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the commissionLedger
        CommissionLedger updatedCommissionLedger = commissionLedgerRepository.findById(commissionLedger.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCommissionLedger are not directly saved in db
        em.detach(updatedCommissionLedger);
        updatedCommissionLedger
            .requestId(UPDATED_REQUEST_ID)
            .fromPan(UPDATED_FROM_PAN)
            .cardType(UPDATED_CARD_TYPE)
            .destinationAccount(UPDATED_DESTINATION_ACCOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .transferredAmount(UPDATED_TRANSFERRED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .createdAt(UPDATED_CREATED_AT);

        restCommissionLedgerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedCommissionLedger.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedCommissionLedger))
            )
            .andExpect(status().isOk());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCommissionLedgerToMatchAllProperties(updatedCommissionLedger);
    }

    @Test
    @Transactional
    void putNonExistingCommissionLedger() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        commissionLedger.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommissionLedgerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, commissionLedger.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCommissionLedger() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        commissionLedger.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommissionLedgerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCommissionLedger() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        commissionLedger.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommissionLedgerMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCommissionLedgerWithPatch() throws Exception {
        // Initialize the database
        insertedCommissionLedger = commissionLedgerRepository.saveAndFlush(commissionLedger);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the commissionLedger using partial update
        CommissionLedger partialUpdatedCommissionLedger = new CommissionLedger();
        partialUpdatedCommissionLedger.setId(commissionLedger.getId());

        partialUpdatedCommissionLedger
            .fromPan(UPDATED_FROM_PAN)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .transferredAmount(UPDATED_TRANSFERRED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT);

        restCommissionLedgerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCommissionLedger.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCommissionLedger))
            )
            .andExpect(status().isOk());

        // Validate the CommissionLedger in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommissionLedgerUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCommissionLedger, commissionLedger),
            getPersistedCommissionLedger(commissionLedger)
        );
    }

    @Test
    @Transactional
    void fullUpdateCommissionLedgerWithPatch() throws Exception {
        // Initialize the database
        insertedCommissionLedger = commissionLedgerRepository.saveAndFlush(commissionLedger);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the commissionLedger using partial update
        CommissionLedger partialUpdatedCommissionLedger = new CommissionLedger();
        partialUpdatedCommissionLedger.setId(commissionLedger.getId());

        partialUpdatedCommissionLedger
            .requestId(UPDATED_REQUEST_ID)
            .fromPan(UPDATED_FROM_PAN)
            .cardType(UPDATED_CARD_TYPE)
            .destinationAccount(UPDATED_DESTINATION_ACCOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .transferredAmount(UPDATED_TRANSFERRED_AMOUNT)
            .commissionAmount(UPDATED_COMMISSION_AMOUNT)
            .createdAt(UPDATED_CREATED_AT);

        restCommissionLedgerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCommissionLedger.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCommissionLedger))
            )
            .andExpect(status().isOk());

        // Validate the CommissionLedger in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommissionLedgerUpdatableFieldsEquals(
            partialUpdatedCommissionLedger,
            getPersistedCommissionLedger(partialUpdatedCommissionLedger)
        );
    }

    @Test
    @Transactional
    void patchNonExistingCommissionLedger() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        commissionLedger.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommissionLedgerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, commissionLedger.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCommissionLedger() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        commissionLedger.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommissionLedgerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isBadRequest());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCommissionLedger() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        commissionLedger.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommissionLedgerMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(commissionLedger))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the CommissionLedger in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCommissionLedger() throws Exception {
        // Initialize the database
        insertedCommissionLedger = commissionLedgerRepository.saveAndFlush(commissionLedger);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the commissionLedger
        restCommissionLedgerMockMvc
            .perform(delete(ENTITY_API_URL_ID, commissionLedger.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return commissionLedgerRepository.count();
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

    protected CommissionLedger getPersistedCommissionLedger(CommissionLedger commissionLedger) {
        return commissionLedgerRepository.findById(commissionLedger.getId()).orElseThrow();
    }

    protected void assertPersistedCommissionLedgerToMatchAllProperties(CommissionLedger expectedCommissionLedger) {
        assertCommissionLedgerAllPropertiesEquals(expectedCommissionLedger, getPersistedCommissionLedger(expectedCommissionLedger));
    }

    protected void assertPersistedCommissionLedgerToMatchUpdatableProperties(CommissionLedger expectedCommissionLedger) {
        assertCommissionLedgerAllUpdatablePropertiesEquals(
            expectedCommissionLedger,
            getPersistedCommissionLedger(expectedCommissionLedger)
        );
    }
}
