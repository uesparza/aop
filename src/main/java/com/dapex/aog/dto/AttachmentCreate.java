package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Created by taddai on 2/28/19.
 */
public class AttachmentCreate {

    @JsonProperty("aogId")
    private Long aogId;

    @JsonProperty("containerId")
    private Long containerId;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("aHref")
    private String aHref;

    @JsonProperty("bucketName")
    private String bucketName;

    @JsonProperty("attachmentId")
    private Long attachmentId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public AttachmentCreate(AttachmentCreate attachmentCreate) {
        this.aogId = attachmentCreate.getAogId();
        this.containerId = attachmentCreate.getContainerId();
        this.fileName = attachmentCreate.getFileName();
        this.aHref = attachmentCreate.getaHref();
        this.bucketName = attachmentCreate.getBucketName();
        this.attachmentId = attachmentCreate.getAttachmentId();
        this.timestamp = attachmentCreate.getTimestamp();
    }

    public AttachmentCreate() {
    }

    public Long getAogId() {
        return aogId;
    }

    public void setAogId(Long aogId) {
        this.aogId = aogId;
    }

    public Long getContainerId() {
        return containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getaHref() {
        return aHref;
    }

    public void setaHref(String aHref) {
        this.aHref = aHref;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AttachmentCreate{" +
                "aogId=" + aogId +
                ", containerId=" + containerId +
                ", fileName='" + fileName + '\'' +
                ", aHref='" + aHref + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", attachmentId=" + attachmentId +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachmentCreate that = (AttachmentCreate) o;

        if (!aogId.equals(that.aogId)) return false;
        if (!containerId.equals(that.containerId)) return false;
        return attachmentId != null ? attachmentId.equals(that.attachmentId) : that.attachmentId == null;
    }

    @Override
    public int hashCode() {
        int result = aogId.hashCode();
        result = 31 * result + containerId.hashCode();
        result = 31 * result + (attachmentId != null ? attachmentId.hashCode() : 0);
        return result;
    }
}
