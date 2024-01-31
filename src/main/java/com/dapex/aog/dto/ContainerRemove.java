package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContainerRemove {

    @JsonProperty("aogId")
    private Long aogId;

    @JsonProperty("containerId")
    private Long containerId;

    public ContainerRemove() { }

    public ContainerRemove(ContainerRemove container) {
        this.aogId = container.getAogId();
        this.containerId = container.getContainerId();
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

    @Override
    public String toString() {
        return "ContainerRemove{" +
                "aogId=" + aogId +
                ", containerId=" + containerId +
                '}';
    }

}
