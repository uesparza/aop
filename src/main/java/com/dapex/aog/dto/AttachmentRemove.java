package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.dapex.aog.jpa.domain.Attachment;

/**
 * Created by taddai on 2/28/19.
 */
public class AttachmentRemove {

    @JsonProperty("aogId")
    private Long aogId;

    @JsonProperty("containerId")
    private Long containerId;

    @JsonProperty("attachmentId")
    private Long attachmentId;

    public AttachmentRemove(Long aogId, Long containerId, Long attachmentId) {
        this.aogId = aogId;
        this.containerId = containerId;
        this.attachmentId = attachmentId;
    }

    public AttachmentRemove(Attachment attachment) {
        this.containerId = attachment.getHierarchyId();
        this.attachmentId = attachment.getId();
    }

    public AttachmentRemove() {
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

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Override
    public String toString() {
        return "AttachmentRemove{" +
                "aogId=" + aogId +
                ", containerId=" + containerId +
                ", attachmentId=" + attachmentId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachmentRemove that = (AttachmentRemove) o;

        if (!aogId.equals(that.aogId)) return false;
        if (!containerId.equals(that.containerId)) return false;
        return attachmentId.equals(that.attachmentId);
    }

    @Override
    public int hashCode() {
        int result = aogId.hashCode();
        result = 31 * result + containerId.hashCode();
        result = 31 * result + attachmentId.hashCode();
        return result;
    }
}
