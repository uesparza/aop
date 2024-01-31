package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContainerCreate {

    @JsonProperty("aogId")
    private Long aogId;

    @JsonProperty("parentId")
    private Long parentId;

    @JsonProperty("containerLabel")
    private String containerLabel;

    @JsonProperty("containerName")
    private String containerName;

    @JsonProperty("predecessorId")
    private Long predecessorId;

    @JsonProperty("successorId")
    private Long successorId;

    @JsonProperty("hasSubChapters")
    private boolean hasSubChapters;

    @JsonProperty("hierarchyId")
    private Long hierarchyId;

    @JsonProperty("seqNumber")
    private Long seqNumber;

    public ContainerCreate() { }

    public ContainerCreate(ContainerCreate container) {
        this.aogId = container.getAogId();
        this.parentId = container.getParentId();
        this.containerLabel = container.getContainerLabel();
        this.containerName = container.getContainerName();
        this.predecessorId = container.getPredecessorId();
        this.successorId = container.getSuccessorId();
        this.hasSubChapters = container.hasSubChapters();
        this.hierarchyId = container.getHierarchyId();
        this.seqNumber = container.getSeqNumber();
    }

    public Long getAogId() {
        return aogId;
    }

    public void setAogId(Long aogId) {
        this.aogId = aogId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getContainerLabel() {
        return containerLabel;
    }

    public void setContainerLabel(String containerLabel) {
        this.containerLabel = containerLabel;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Long getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(Long predecessorId) {
        this.predecessorId = predecessorId;
    }

    public Long getSuccessorId() {
        return successorId;
    }

    public void setSuccessorId(Long successorId) {
        this.successorId = successorId;
    }

    public boolean hasSubChapters() {
        return hasSubChapters;
    }

    public void setHasSubChapters(boolean hasSubChapters) {
        this.hasSubChapters = hasSubChapters;
    }

    public Long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public Long getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(Long seqNumber) {
        this.seqNumber = seqNumber;
    }

    @Override
    public String toString() {
        return "ContainerRemove{" +
                "aogId=" + aogId +
                ", parentId=" + parentId +
                ", containerLabel=" + containerLabel +
                ", containerName=" + containerName +
                ", predecessorId=" + predecessorId +
                ", successorId=" + successorId +
                ", hasSubChapters=" + hasSubChapters +
                ", hierarchyId=" + hierarchyId +
                ", seqNumber=" + seqNumber +
                '}';
    }
}
