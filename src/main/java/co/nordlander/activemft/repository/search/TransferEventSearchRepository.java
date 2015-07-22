package co.nordlander.activemft.repository.search;

import co.nordlander.activemft.domain.TransferEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the TransferEvent entity.
 */
public interface TransferEventSearchRepository extends ElasticsearchRepository<TransferEvent, Long> {
}
