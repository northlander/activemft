package co.nordlander.activemft.web.rest;

import com.codahale.metrics.annotation.Timed;
import co.nordlander.activemft.domain.TransferJob;
import co.nordlander.activemft.repository.TransferJobRepository;
import co.nordlander.activemft.repository.search.TransferJobSearchRepository;
import co.nordlander.activemft.service.ReceiverService;
import co.nordlander.activemft.web.rest.util.PaginationUtil;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing TransferJob.
 */
@RestController
@RequestMapping("/api")
public class TransferJobResource {

    private final Logger log = LoggerFactory.getLogger(TransferJobResource.class);

    @Inject
    private TransferJobRepository transferJobRepository;
    
    @Inject
    private ReceiverService receiverService;

    @Inject
    private TransferJobSearchRepository transferJobSearchRepository;

    /**
     * POST  /transferJobs -> Create a new transferJob.
     * @throws SchedulerException 
     * @throws ParseException 
     * @throws NoSuchMethodException 
     * @throws ClassNotFoundException 
     */
    @RequestMapping(value = "/transferJobs",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    
    @Timed
    public ResponseEntity<Void> create(@Valid @RequestBody TransferJob transferJob) throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException {
        log.debug("REST request to save TransferJob : {}", transferJob);
        if (transferJob.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new transferJob cannot already have an ID").build();
        }
        transferJob = transferJobRepository.save(transferJob);
        transferJob = transferJobSearchRepository.save(transferJob);
        receiverService.initTransferJob(transferJob);
        
        
        return ResponseEntity.created(new URI("/api/transferJobs/" + transferJob.getId())).build();
    }

    /**
     * PUT  /transferJobs -> Updates an existing transferJob.
     * @throws SchedulerException 
     * @throws ParseException 
     * @throws NoSuchMethodException 
     * @throws ClassNotFoundException 
     */
    @RequestMapping(value = "/transferJobs",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@Valid @RequestBody TransferJob transferJob) throws URISyntaxException, ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException {
        log.debug("REST request to update TransferJob : {}", transferJob);
        if (transferJob.getId() == null) {
            return create(transferJob);
        }
        transferJob = transferJobRepository.save(transferJob);
        transferJob = transferJobSearchRepository.save(transferJob);
        receiverService.reinitTransferJob(transferJob);
        
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /transferJobs -> get all the transferJobs.
     */
    @RequestMapping(value = "/transferJobs",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<TransferJob>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<TransferJob> page = transferJobRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transferJobs", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /transferJobs/:id -> get the "id" transferJob.
     */
    @RequestMapping(value = "/transferJobs/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TransferJob> get(@PathVariable Long id) {
        log.debug("REST request to get TransferJob : {}", id);
        return Optional.ofNullable(transferJobRepository.findOne(id))
            .map(transferJob -> new ResponseEntity<>(
                transferJob,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /transferJobs/:id -> delete the "id" transferJob.
     * @throws ParseException 
     * @throws SchedulerException 
     * @throws NoSuchMethodException 
     * @throws ClassNotFoundException 
     */
    @RequestMapping(value = "/transferJobs/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id){
        log.debug("REST request to delete TransferJob : {}", id);
        TransferJob transferJob = transferJobRepository.findOne(id);
        
        try{
        	receiverService.deinitTransferJob(transferJob);	
        }catch(Exception e){
        	log.warn("Unabled to deinit job "+ transferJob.getName() + ". Already deleted?",e);
        }

        transferJobRepository.delete(id);
        transferJobSearchRepository.delete(id);
    }

    /**
     * SEARCH  /_search/transferJobs/:query -> search for the transferJob corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/transferJobs/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<TransferJob> search(@PathVariable String query) {
        return StreamSupport
            .stream(transferJobSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
