package co.nordlander.activemft.web.rest;

import co.nordlander.activemft.Application;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferJobRepository;
import co.nordlander.activemft.repository.search.TransferJobSearchRepository;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TransferJobResource REST controller.
 *
 * @see TransferJobResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class TransferJobResourceTest {

    private static final String DEFAULT_NAME = "SAMPLE_TEXT";
    private static final String UPDATED_NAME = "UPDATED_TEXT";
    private static final String DEFAULT_DESCRIPTION = "SAMPLE_TEXT";
    private static final String UPDATED_DESCRIPTION = "UPDATED_TEXT";
    private static final String DEFAULT_CRON_EXPRESSION = "SAMPLE_TEXT";
    private static final String UPDATED_CRON_EXPRESSION = "UPDATED_TEXT";

    private static final Boolean DEFAULT_ARCHIVE = false;
    private static final Boolean UPDATED_ARCHIVE = true;

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;
    private static final String DEFAULT_SOURCE_URL = "SAMPLE_TEXT";
    private static final String UPDATED_SOURCE_URL = "UPDATED_TEXT";
    private static final String DEFAULT_SOURCE_FILEPATTERN = "SAMPLE_TEXT";
    private static final String UPDATED_SOURCE_FILEPATTERN = "UPDATED_TEXT";
    private static final String DEFAULT_SOURCE_TYPE = "SAMPLE_TEXT";
    private static final String UPDATED_SOURCE_TYPE = "UPDATED_TEXT";
    private static final String DEFAULT_SOURCE_USERNAME = "SAMPLE_TEXT";
    private static final String UPDATED_SOURCE_USERNAME = "UPDATED_TEXT";
    private static final String DEFAULT_SOURCE_PASSWORD = "SAMPLE_TEXT";
    private static final String UPDATED_SOURCE_PASSWORD = "UPDATED_TEXT";
    private static final String DEFAULT_TARGET_URL = "SAMPLE_TEXT";
    private static final String UPDATED_TARGET_URL = "UPDATED_TEXT";
    private static final String DEFAULT_TARGET_TYPE = "SAMPLE_TEXT";
    private static final String UPDATED_TARGET_TYPE = "UPDATED_TEXT";
    private static final String DEFAULT_TARGET_FILENAME = "SAMPLE_TEXT";
    private static final String UPDATED_TARGET_FILENAME = "UPDATED_TEXT";
    private static final String DEFAULT_TARGET_USERNAME = "SAMPLE_TEXT";
    private static final String UPDATED_TARGET_USERNAME = "UPDATED_TEXT";
    private static final String DEFAULT_TARGET_PASSWORD = "SAMPLE_TEXT";
    private static final String UPDATED_TARGET_PASSWORD = "UPDATED_TEXT";

    @Inject
    private TransferJobRepository transferJobRepository;

    @Inject
    private TransferJobSearchRepository transferJobSearchRepository;

    private MockMvc restTransferJobMockMvc;

    private TransferJob transferJob;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TransferJobResource transferJobResource = new TransferJobResource();
        ReflectionTestUtils.setField(transferJobResource, "transferJobRepository", transferJobRepository);
        ReflectionTestUtils.setField(transferJobResource, "transferJobSearchRepository", transferJobSearchRepository);
        this.restTransferJobMockMvc = MockMvcBuilders.standaloneSetup(transferJobResource).build();
    }

    @Before
    public void initTest() {
        transferJob = new TransferJob();
        transferJob.setName(DEFAULT_NAME);
        transferJob.setDescription(DEFAULT_DESCRIPTION);
        transferJob.setCronExpression(DEFAULT_CRON_EXPRESSION);
        transferJob.setArchive(DEFAULT_ARCHIVE);
        transferJob.setEnabled(DEFAULT_ENABLED);
        transferJob.setSourceUrl(DEFAULT_SOURCE_URL);
        transferJob.setSourceFilepattern(DEFAULT_SOURCE_FILEPATTERN);
        transferJob.setSourceType(DEFAULT_SOURCE_TYPE);
        transferJob.setSourceUsername(DEFAULT_SOURCE_USERNAME);
        transferJob.setSourcePassword(DEFAULT_SOURCE_PASSWORD);
        transferJob.setTargetUrl(DEFAULT_TARGET_URL);
        transferJob.setTargetType(DEFAULT_TARGET_TYPE);
        transferJob.setTargetFilename(DEFAULT_TARGET_FILENAME);
        transferJob.setTargetUsername(DEFAULT_TARGET_USERNAME);
        transferJob.setTargetPassword(DEFAULT_TARGET_PASSWORD);
    }

    @Test
    @Transactional
    public void createTransferJob() throws Exception {
        int databaseSizeBeforeCreate = transferJobRepository.findAll().size();

        // Create the TransferJob
        restTransferJobMockMvc.perform(post("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isCreated());

        // Validate the TransferJob in the database
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(databaseSizeBeforeCreate + 1);
        TransferJob testTransferJob = transferJobs.get(transferJobs.size() - 1);
        assertThat(testTransferJob.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testTransferJob.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTransferJob.getCronExpression()).isEqualTo(DEFAULT_CRON_EXPRESSION);
        assertThat(testTransferJob.getArchive()).isEqualTo(DEFAULT_ARCHIVE);
        assertThat(testTransferJob.getEnabled()).isEqualTo(DEFAULT_ENABLED);
        assertThat(testTransferJob.getSourceUrl()).isEqualTo(DEFAULT_SOURCE_URL);
        assertThat(testTransferJob.getSourceFilepattern()).isEqualTo(DEFAULT_SOURCE_FILEPATTERN);
        assertThat(testTransferJob.getSourceType()).isEqualTo(DEFAULT_SOURCE_TYPE);
        assertThat(testTransferJob.getSourceUsername()).isEqualTo(DEFAULT_SOURCE_USERNAME);
        assertThat(testTransferJob.getSourcePassword()).isEqualTo(DEFAULT_SOURCE_PASSWORD);
        assertThat(testTransferJob.getTargetUrl()).isEqualTo(DEFAULT_TARGET_URL);
        assertThat(testTransferJob.getTargetType()).isEqualTo(DEFAULT_TARGET_TYPE);
        assertThat(testTransferJob.getTargetFilename()).isEqualTo(DEFAULT_TARGET_FILENAME);
        assertThat(testTransferJob.getTargetUsername()).isEqualTo(DEFAULT_TARGET_USERNAME);
        assertThat(testTransferJob.getTargetPassword()).isEqualTo(DEFAULT_TARGET_PASSWORD);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferJobRepository.findAll()).hasSize(0);
        // set the field null
        transferJob.setName(null);

        // Create the TransferJob, which fails.
        restTransferJobMockMvc.perform(post("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(0);
    }

    @Test
    @Transactional
    public void checkSourceUrlIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferJobRepository.findAll()).hasSize(0);
        // set the field null
        transferJob.setSourceUrl(null);

        // Create the TransferJob, which fails.
        restTransferJobMockMvc.perform(post("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(0);
    }

    @Test
    @Transactional
    public void checkSourceTypeIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferJobRepository.findAll()).hasSize(0);
        // set the field null
        transferJob.setSourceType(null);

        // Create the TransferJob, which fails.
        restTransferJobMockMvc.perform(post("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(0);
    }

    @Test
    @Transactional
    public void checkTargetUrlIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferJobRepository.findAll()).hasSize(0);
        // set the field null
        transferJob.setTargetUrl(null);

        // Create the TransferJob, which fails.
        restTransferJobMockMvc.perform(post("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(0);
    }

    @Test
    @Transactional
    public void checkTargetTypeIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(transferJobRepository.findAll()).hasSize(0);
        // set the field null
        transferJob.setTargetType(null);

        // Create the TransferJob, which fails.
        restTransferJobMockMvc.perform(post("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(0);
    }

    @Test
    @Transactional
    public void getAllTransferJobs() throws Exception {
        // Initialize the database
        transferJobRepository.saveAndFlush(transferJob);

        // Get all the transferJobs
        restTransferJobMockMvc.perform(get("/api/transferJobs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(transferJob.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
                .andExpect(jsonPath("$.[*].cronExpression").value(hasItem(DEFAULT_CRON_EXPRESSION.toString())))
                .andExpect(jsonPath("$.[*].archive").value(hasItem(DEFAULT_ARCHIVE.booleanValue())))
                .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED.booleanValue())))
                .andExpect(jsonPath("$.[*].sourceUrl").value(hasItem(DEFAULT_SOURCE_URL.toString())))
                .andExpect(jsonPath("$.[*].sourceFilepattern").value(hasItem(DEFAULT_SOURCE_FILEPATTERN.toString())))
                .andExpect(jsonPath("$.[*].sourceType").value(hasItem(DEFAULT_SOURCE_TYPE.toString())))
                .andExpect(jsonPath("$.[*].sourceUsername").value(hasItem(DEFAULT_SOURCE_USERNAME.toString())))
                .andExpect(jsonPath("$.[*].sourcePassword").value(hasItem(DEFAULT_SOURCE_PASSWORD.toString())))
                .andExpect(jsonPath("$.[*].targetUrl").value(hasItem(DEFAULT_TARGET_URL.toString())))
                .andExpect(jsonPath("$.[*].targetType").value(hasItem(DEFAULT_TARGET_TYPE.toString())))
                .andExpect(jsonPath("$.[*].targetFilename").value(hasItem(DEFAULT_TARGET_FILENAME.toString())))
                .andExpect(jsonPath("$.[*].targetUsername").value(hasItem(DEFAULT_TARGET_USERNAME.toString())))
                .andExpect(jsonPath("$.[*].targetPassword").value(hasItem(DEFAULT_TARGET_PASSWORD.toString())));
    }

    @Test
    @Transactional
    public void getTransferJob() throws Exception {
        // Initialize the database
        transferJobRepository.saveAndFlush(transferJob);

        // Get the transferJob
        restTransferJobMockMvc.perform(get("/api/transferJobs/{id}", transferJob.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(transferJob.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.cronExpression").value(DEFAULT_CRON_EXPRESSION.toString()))
            .andExpect(jsonPath("$.archive").value(DEFAULT_ARCHIVE.booleanValue()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED.booleanValue()))
            .andExpect(jsonPath("$.sourceUrl").value(DEFAULT_SOURCE_URL.toString()))
            .andExpect(jsonPath("$.sourceFilepattern").value(DEFAULT_SOURCE_FILEPATTERN.toString()))
            .andExpect(jsonPath("$.sourceType").value(DEFAULT_SOURCE_TYPE.toString()))
            .andExpect(jsonPath("$.sourceUsername").value(DEFAULT_SOURCE_USERNAME.toString()))
            .andExpect(jsonPath("$.sourcePassword").value(DEFAULT_SOURCE_PASSWORD.toString()))
            .andExpect(jsonPath("$.targetUrl").value(DEFAULT_TARGET_URL.toString()))
            .andExpect(jsonPath("$.targetType").value(DEFAULT_TARGET_TYPE.toString()))
            .andExpect(jsonPath("$.targetFilename").value(DEFAULT_TARGET_FILENAME.toString()))
            .andExpect(jsonPath("$.targetUsername").value(DEFAULT_TARGET_USERNAME.toString()))
            .andExpect(jsonPath("$.targetPassword").value(DEFAULT_TARGET_PASSWORD.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingTransferJob() throws Exception {
        // Get the transferJob
        restTransferJobMockMvc.perform(get("/api/transferJobs/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTransferJob() throws Exception {
        // Initialize the database
        transferJobRepository.saveAndFlush(transferJob);

		int databaseSizeBeforeUpdate = transferJobRepository.findAll().size();

        // Update the transferJob
        transferJob.setName(UPDATED_NAME);
        transferJob.setDescription(UPDATED_DESCRIPTION);
        transferJob.setCronExpression(UPDATED_CRON_EXPRESSION);
        transferJob.setArchive(UPDATED_ARCHIVE);
        transferJob.setEnabled(UPDATED_ENABLED);
        transferJob.setSourceUrl(UPDATED_SOURCE_URL);
        transferJob.setSourceFilepattern(UPDATED_SOURCE_FILEPATTERN);
        transferJob.setSourceType(UPDATED_SOURCE_TYPE);
        transferJob.setSourceUsername(UPDATED_SOURCE_USERNAME);
        transferJob.setSourcePassword(UPDATED_SOURCE_PASSWORD);
        transferJob.setTargetUrl(UPDATED_TARGET_URL);
        transferJob.setTargetType(UPDATED_TARGET_TYPE);
        transferJob.setTargetFilename(UPDATED_TARGET_FILENAME);
        transferJob.setTargetUsername(UPDATED_TARGET_USERNAME);
        transferJob.setTargetPassword(UPDATED_TARGET_PASSWORD);
        restTransferJobMockMvc.perform(put("/api/transferJobs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(transferJob)))
                .andExpect(status().isOk());

        // Validate the TransferJob in the database
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(databaseSizeBeforeUpdate);
        TransferJob testTransferJob = transferJobs.get(transferJobs.size() - 1);
        assertThat(testTransferJob.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testTransferJob.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testTransferJob.getCronExpression()).isEqualTo(UPDATED_CRON_EXPRESSION);
        assertThat(testTransferJob.getArchive()).isEqualTo(UPDATED_ARCHIVE);
        assertThat(testTransferJob.getEnabled()).isEqualTo(UPDATED_ENABLED);
        assertThat(testTransferJob.getSourceUrl()).isEqualTo(UPDATED_SOURCE_URL);
        assertThat(testTransferJob.getSourceFilepattern()).isEqualTo(UPDATED_SOURCE_FILEPATTERN);
        assertThat(testTransferJob.getSourceType()).isEqualTo(UPDATED_SOURCE_TYPE);
        assertThat(testTransferJob.getSourceUsername()).isEqualTo(UPDATED_SOURCE_USERNAME);
        assertThat(testTransferJob.getSourcePassword()).isEqualTo(UPDATED_SOURCE_PASSWORD);
        assertThat(testTransferJob.getTargetUrl()).isEqualTo(UPDATED_TARGET_URL);
        assertThat(testTransferJob.getTargetType()).isEqualTo(UPDATED_TARGET_TYPE);
        assertThat(testTransferJob.getTargetFilename()).isEqualTo(UPDATED_TARGET_FILENAME);
        assertThat(testTransferJob.getTargetUsername()).isEqualTo(UPDATED_TARGET_USERNAME);
        assertThat(testTransferJob.getTargetPassword()).isEqualTo(UPDATED_TARGET_PASSWORD);
    }

    @Test
    @Transactional
    public void deleteTransferJob() throws Exception {
        // Initialize the database
        transferJobRepository.saveAndFlush(transferJob);

		int databaseSizeBeforeDelete = transferJobRepository.findAll().size();

        // Get the transferJob
        restTransferJobMockMvc.perform(delete("/api/transferJobs/{id}", transferJob.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<TransferJob> transferJobs = transferJobRepository.findAll();
        assertThat(transferJobs).hasSize(databaseSizeBeforeDelete - 1);
    }
}
