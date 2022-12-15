package org.zith.expr.ctxwl.core.reading.impl.readingsession;

import com.google.common.base.Preconditions;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Entity
@Table(
        indexes = {
                @Index(name = ReadingSessionEntity.INDEX_GROUP_SERIAL, columnList = "group, serial", unique = true),
                @Index(name = "index_group_status", columnList = "group, status")
        }
)
public class ReadingSessionEntity {
    public static final String INDEX_GROUP_SERIAL = "index_group_serial";

    private Long id;
    private String group;
    private Long serial;
    private String status;
    private Instant creationTime;
    private Instant updateTime;
    private Instant completionTime;
    private Instant terminationTime;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(columnDefinition = "text")
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getSerial() {
        return serial;
    }

    public void setSerial(Long serial) {
        this.serial = serial;
    }

    @Column(columnDefinition = "text")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        Preconditions.checkArgument(
                Optional.ofNullable(creationTime)
                        .map(v -> v.equals(v.truncatedTo(ChronoUnit.MICROS)))
                        .orElse(true));
        this.creationTime = creationTime;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        Preconditions.checkArgument(
                Optional.ofNullable(updateTime)
                        .map(v -> v.equals(v.truncatedTo(ChronoUnit.MICROS)))
                        .orElse(true));
        this.updateTime = updateTime;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Instant completionTime) {
        Preconditions.checkArgument(
                Optional.ofNullable(completionTime)
                        .map(v -> v.equals(v.truncatedTo(ChronoUnit.MICROS)))
                        .orElse(true));
        this.completionTime = completionTime;
    }

    @Column(columnDefinition = "timestamp with time zone")
    public Instant getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(Instant terminationTime) {
        Preconditions.checkArgument(
                Optional.ofNullable(terminationTime)
                        .map(v -> v.equals(v.truncatedTo(ChronoUnit.MICROS)))
                        .orElse(true));
        this.terminationTime = terminationTime;
    }
}
