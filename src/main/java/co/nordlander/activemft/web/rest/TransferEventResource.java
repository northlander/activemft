package co.nordlander.activemft.web.rest;

import com.codahale.metrics.annotation.Timed;
import co.nordlander.activemft.domain.TransferEvent;
import co.nordlander.activemft.repository.TransferEventRepository;
import co.nordlander.activemft.repository.search.TransferEventSearchRepository;
import co.nordlander.activemft.web.rest.util.PaginationUtil;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing TransferEvent.
 */
@RestController
@RequestMapping("/api")
public class TransferEventResource {

    private final Logger log = LoggerFactory.getLogger(TransferEventResource.class);

    @Inject
    private TransferEventRepository transferEventRepository;

    @Inject
    private TransferEventSearchRepository transferEventSearchRepository;

    /**
     * POST  /transferEvents -> Create a new transferEvent.
     */
    @RequestMapping(value = "/transferEvents",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@Valid @RequestBody TransferEvent transferEvent) throws URISyntaxException {
        log.debug("REST request to save TransferEvent : {}", transferEvent);
        if (transferEvent.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new transferEvent cannot already have an ID").build();
        }
        transferEventRepository.save(transferEvent);
        transferEventSearchRepository.save(transferEvent);
        return ResponseEntity.created(new URI("/api/transferEvents/" + transferEvent.getId())).build();
    }

    /**
     * PUT  /transferEvents -> Updates an existing transferEvent.
     */
    @RequestMapping(value = "/transferEvents",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@Valid @RequestBody TransferEvent transferEvent) throws URISyntaxException {
        log.debug("REST request to update TransferEvent : {}", transferEvent);
        if (transferEvent.getId() == null) {
            return create(transferEvent);
        }
        transferEventRepository.save(transferEvent);
        transferEventSearchRepository.save(transferEvent);
      
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /transferEvents -> get all the transferEvents.
     */
    @RequestMapping(value = "/transferEvents",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<TransferEvent>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<TransferEvent> page = transferEventRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/transferEvents", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /transferEvents/:id -> get the "id" transferEvent.
     */
    @RequestMapping(value = "/transferEvents/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TransferEvent> get(@PathVariable Long id) {
        log.debug("REST request to get TransferEvent : {}", id);
        return Optional.ofNullable(transferEventRepository.findOne(id))
            .map(transferEvent -> new ResponseEntity<>(
                transferEvent,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /transferEvents/:id -> delete the "id" transferEvent.
     */
    @RequestMapping(value = "/transferEvents/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete TransferEvent : {}", id);
        transferEventRepository.delete(id);
        transferEventSearchRepository.delete(id);
    }

    /**
     * SEARCH  /_search/transferEvents/:query -> search for the transferEvent corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/transferEvents/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<TransferEvent> search(@PathVariable String query) {
        return StreamSupport
            .stream(transferEventSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
