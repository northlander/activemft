package co.nordlander.activemft.repository;

import co.nordlander.activemft.domain.TransferEvent;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the TransferEvent entity.
 */
public interface TransferEventRepository extends JpaRepository<TransferEvent,Long> {

}
