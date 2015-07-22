package co.nordlander.activemft.repository;

import co.nordlander.activemft.domain.TransferJob;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the TransferJob entity.
 */
public interface TransferJobRepository extends JpaRepository<TransferJob,Long> {

}
