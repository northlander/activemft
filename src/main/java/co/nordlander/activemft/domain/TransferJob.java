package co.nordlander.activemft.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A TransferJob.
 */
@Entity
@Table(name = "TRANSFERJOB")
@Document(indexName="transferjob")
public class TransferJob implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "archive")
    private Boolean archive;

    @Column(name = "enabled")
    private Boolean enabled;

    @NotNull
    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "source_filepattern")
    private String sourceFilepattern;

    @NotNull
    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "source_username")
    private String sourceUsername;

    @Column(name = "source_password")
    private String sourcePassword;

    @NotNull
    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @NotNull
    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_filename")
    private String targetFilename;

    @Column(name = "target_username")
    private String targetUsername;

    @Column(name = "target_password")
    private String targetPassword;

    @OneToMany(mappedBy = "transferJob")
    @JsonIgnore
    private Set<TransferEvent> transferEvents = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceFilepattern() {
        return sourceFilepattern;
    }

    public void setSourceFilepattern(String sourceFilepattern) {
        this.sourceFilepattern = sourceFilepattern;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceUsername() {
        return sourceUsername;
    }

    public void setSourceUsername(String sourceUsername) {
        this.sourceUsername = sourceUsername;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }

    public void setSourcePassword(String sourcePassword) {
        this.sourcePassword = sourcePassword;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetFilename() {
        return targetFilename;
    }

    public void setTargetFilename(String targetFilename) {
        this.targetFilename = targetFilename;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getTargetPassword() {
        return targetPassword;
    }

    public void setTargetPassword(String targetPassword) {
        this.targetPassword = targetPassword;
    }

    public Set<TransferEvent> getTransferEvents() {
        return transferEvents;
    }

    public void setTransferEvents(Set<TransferEvent> transferEvents) {
        this.transferEvents = transferEvents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransferJob transferJob = (TransferJob) o;

        if ( ! Objects.equals(id, transferJob.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TransferJob{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", description='" + description + "'" +
                ", cronExpression='" + cronExpression + "'" +
                ", archive='" + archive + "'" +
                ", enabled='" + enabled + "'" +
                ", sourceUrl='" + sourceUrl + "'" +
                ", sourceFilepattern='" + sourceFilepattern + "'" +
                ", sourceType='" + sourceType + "'" +
                ", sourceUsername='" + sourceUsername + "'" +
                ", sourcePassword='" + sourcePassword + "'" +
                ", targetUrl='" + targetUrl + "'" +
                ", targetType='" + targetType + "'" +
                ", targetFilename='" + targetFilename + "'" +
                ", targetUsername='" + targetUsername + "'" +
                ", targetPassword='" + targetPassword + "'" +
                '}';
    }
}
