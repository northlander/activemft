package co.nordlander.activemft.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import co.nordlander.activemft.domain.util.CustomDateTimeDeserializer;
import co.nordlander.activemft.domain.util.CustomDateTimeSerializer;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A TransferEvent.
 */
@Entity
@Table(name = "TRANSFEREVENT")
@Document(indexName="transferevent")
public class TransferEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "transfer_id", nullable = false)
    private String transferId;

    @NotNull
    @Column(name = "state", nullable = false)
    private String state;

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Column(name = "timestamp", nullable = false)
    private DateTime timestamp;

    @NotNull
    @Column(name = "size", nullable = false)
    private Integer size;

    @Column(name = "filename")
    private String filename;

    @ManyToOne
    private TransferJob transferJob;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public TransferJob getTransferJob() {
        return transferJob;
    }

    public void setTransferJob(TransferJob transferJob) {
        this.transferJob = transferJob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransferEvent transferEvent = (TransferEvent) o;

        if ( ! Objects.equals(id, transferEvent.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TransferEvent{" +
                "id=" + id +
                ", transferId='" + transferId + "'" +
                ", state='" + state + "'" +
                ", timestamp='" + timestamp + "'" +
                ", size='" + size + "'" +
                ", filename='" + filename + "'" +
                '}';
    }
}
