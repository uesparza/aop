package com.dapex.aog.jpa.domain;

import com.dapex.aog.config.Constants;
import org.springframework.context.annotation.Bean;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDateTime;

//import javax.persistence.*;



@Entity
@Table(name = Constants.ATTACHMENTS_TABLE)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hierarchy_id")
    private Long hierarchyId;

    @Column(name = "bucket")
    private String bucketName;

    @Column(name = "filename")
    private String filename;

    @Column(name = "ahref")
    private String ahref;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public Attachment() { }

    public Attachment(Long hierarchyId, String ahref, String bucketName, LocalDateTime timestamp) {
        this.hierarchyId = hierarchyId;
        this.ahref = ahref;
        this.bucketName = bucketName;
        this.filename = ahref.substring(
                ahref.indexOf(Constants.OPTUM_OBJ_STORE) + Constants.OPTUM_OBJ_STORE.length());
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucket) {
        this.bucketName = bucket;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAhref() {
        return ahref;
    }

    public void setAhref(String ahref) {
        this.ahref = ahref;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", hierarchy_id=" + hierarchyId +
                ", bucketName='" + bucketName + '\'' +
                ", filename='" + filename + '\'' +
                ", ahref='" + ahref + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
