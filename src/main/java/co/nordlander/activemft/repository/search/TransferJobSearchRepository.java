package co.nordlander.activemft.repository.search;

import co.nordlander.activemft.domain.TransferJob;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the TransferJob entity.
 */
public interface TransferJobSearchRepository extends ElasticsearchRepository<TransferJob, Long> {
}
