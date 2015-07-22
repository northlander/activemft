package co.nordlander.activemft.web.rest;

import co.nordlander.activemft.Application;
import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.repository.TransferEventRepository;
import co.nordlander.activemft.repository.search.TransferEventSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TransferEventResource REST controller.
 *
 * @see TransferEventResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class TransferEventResourceTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String DEFAULT_TRANSFER_ID = "SAMPLE_TEXT";
    private static final String UPDATED_TRANSFER_ID = "UPDATED_TEXT";
    private static final String DEFAULT_STATE = "SAMPLE_TEXT";
    private static final String UPDATED_STATE = "UPDATED_TEXT";

    private static final DateTime DEFAULT_TIMESTAMP = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_TIMESTAMP = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_TIMESTAMP_STR = dateTimeFormatter.print(DEFAULT_TIMESTAMP);

    private static final Integer DEFAULT_SIZE = 0;
    private static final Integer UPDATED_SIZE = 1;
    private static final String DEFAULT_FILENAME = "SAMPLE_TEXT";
    private static final String UPDATED_FILENAME = "UPDATED_TEXT";

    @Inject
    private TransferEventRepository transferEventRepository;

    @Inject
    private TransferEventSearchRepository transferEventSearchRepository;

    private MockMvc restTransferEventMockMvc;

    private TransferEvent transferEvent;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TransferEventResource transferEventResource = new TransferEventResource();
        ReflectionTestUtils.setField(transferEventResource, "transferEventRepository", transferEventRepository);
        ReflectionTestUtils.setField(transferEventResource, "transferEventSearchRepository", transferEventSearchRepository);
        this.restTransferEventMockMvc = MockMvcBuilders.standaloneSetup(transferEventResource).build();
    }

    @Before
    public void initTest() {
        transferEvent = new TransferEvent();
        transferEvent.setTransferId(DEFAULT_TRANSFER_ID);
        transferEvent.setState(DEFAULT_STATE);
        transferEvent.setTimestamp(DEFAULT_TIMESTAMP);
        transferEvent.setSize(DEFAULT_SIZE);
        transferEvent.setFilename(DEFAULT_FILENAME);
    }

    @Test
    @Transactional
    public void createTransferEvent() throws Exception {
        int databaseSizeBeforeCreate = transferEventRepository.findAll().size();

        // Create the TransferEvent
        restTransferEventMockMvc.perform(post("/api/transferEvents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferEvent)))
                .andExpect(status().isCreated());

        // Validate the TransferEvent in the database
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(databaseSizeBeforeCreate + 1);
        TransferEvent testTransferEvent = transferEvents.get(transferEvents.size() - 1);
        assertThat(testTransferEvent.getTransferId()).isEqualTo(DEFAULT_TRANSFER_ID);
        assertThat(testTransferEvent.getState()).isEqualTo(DEFAULT_STATE);
        assertThat(testTransferEvent.getTimestamp().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testTransferEvent.getSize()).isEqualTo(DEFAULT_SIZE);
        assertThat(testTransferEvent.getFilename()).isEqualTo(DEFAULT_FILENAME);
    }

    @Test
    @Transactional
    public void checkTransferIdIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferEventRepository.findAll()).hasSize(0);
        // set the field null
        transferEvent.setTransferId(null);

        // Create the TransferEvent, which fails.
        restTransferEventMockMvc.perform(post("/api/transferEvents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferEvent)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(0);
    }

    @Test
    @Transactional
    public void checkStateIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferEventRepository.findAll()).hasSize(0);
        // set the field null
        transferEvent.setState(null);

        // Create the TransferEvent, which fails.
        restTransferEventMockMvc.perform(post("/api/transferEvents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferEvent)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(0);
    }

    @Test
    @Transactional
    public void checkTimestampIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferEventRepository.findAll()).hasSize(0);
        // set the field null
        transferEvent.setTimestamp(null);

        // Create the TransferEvent, which fails.
        restTransferEventMockMvc.perform(post("/api/transferEvents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferEvent)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(0);
    }

    @Test
    @Transactional
    public void checkSizeIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferEventRepository.findAll()).hasSize(0);
        // set the field null
        transferEvent.setSize(null);

        // Create the TransferEvent, which fails.
        restTransferEventMockMvc.perform(post("/api/transferEvents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferEvent)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(0);
    }

    @Test
    @Transactional
    public void getAllTransferEvents() throws Exception {
        // Initialize the database
        transferEventRepository.saveAndFlush(transferEvent);

        // Get all the transferEvents
        restTransferEventMockMvc.perform(get("/api/transferEvents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(transferEvent.getId().intValue())))
                .andExpect(jsonPath("$.[*].transferId").value(hasItem(DEFAULT_TRANSFER_ID.toString())))
                .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE.toString())))
                .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP_STR)))
                .andExpect(jsonPath("$.[*].size").value(hasItem(DEFAULT_SIZE)))
                .andExpect(jsonPath("$.[*].filename").value(hasItem(DEFAULT_FILENAME.toString())));
    }

    @Test
    @Transactional
    public void getTransferEvent() throws Exception {
        // Initialize the database
        transferEventRepository.saveAndFlush(transferEvent);

        // Get the transferEvent
        restTransferEventMockMvc.perform(get("/api/transferEvents/{id}", transferEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(transferEvent.getId().intValue()))
            .andExpect(jsonPath("$.transferId").value(DEFAULT_TRANSFER_ID.toString()))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE.toString()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP_STR))
            .andExpect(jsonPath("$.size").value(DEFAULT_SIZE))
            .andExpect(jsonPath("$.filename").value(DEFAULT_FILENAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingTransferEvent() throws Exception {
        // Get the transferEvent
        restTransferEventMockMvc.perform(get("/api/transferEvents/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTransferEvent() throws Exception {
        // Initialize the database
        transferEventRepository.saveAndFlush(transferEvent);

		int databaseSizeBeforeUpdate = transferEventRepository.findAll().size();

        // Update the transferEvent
        transferEvent.setTransferId(UPDATED_TRANSFER_ID);
        transferEvent.setState(UPDATED_STATE);
        transferEvent.setTimestamp(UPDATED_TIMESTAMP);
        transferEvent.setSize(UPDATED_SIZE);
        transferEvent.setFilename(UPDATED_FILENAME);
        restTransferEventMockMvc.perform(put("/api/transferEvents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferEvent)))
                .andExpect(status().isOk());

        // Validate the TransferEvent in the database
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(databaseSizeBeforeUpdate);
        TransferEvent testTransferEvent = transferEvents.get(transferEvents.size() - 1);
        assertThat(testTransferEvent.getTransferId()).isEqualTo(UPDATED_TRANSFER_ID);
        assertThat(testTransferEvent.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testTransferEvent.getTimestamp().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testTransferEvent.getSize()).isEqualTo(UPDATED_SIZE);
        assertThat(testTransferEvent.getFilename()).isEqualTo(UPDATED_FILENAME);
    }

    @Test
    @Transactional
    public void deleteTransferEvent() throws Exception {
        // Initialize the database
        transferEventRepository.saveAndFlush(transferEvent);

		int databaseSizeBeforeDelete = transferEventRepository.findAll().size();

        // Get the transferEvent
        restTransferEventMockMvc.perform(delete("/api/transferEvents/{id}", transferEvent.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<TransferEvent> transferEvents = transferEventRepository.findAll();
        assertThat(transferEvents).hasSize(databaseSizeBeforeDelete - 1);
    }
}
