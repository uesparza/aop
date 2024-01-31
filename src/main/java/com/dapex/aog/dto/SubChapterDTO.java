package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SubChapterDTO {

    public SubChapterDTO() {
    }

    public SubChapterDTO(Integer platformId, List<Long> subChapterList, String fileName) {
        this.platformId = platformId;
        this.subChapterList = subChapterList;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "SubChapterDTO{" +
                "platformId=" + platformId +
                ", subChapterList=" + subChapterList +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    @JsonProperty("platformId")
    private
    Integer platformId;
    @JsonProperty("subChapterList")
    private
    List<Long> subChapterList;
    @JsonProperty("fileName")
    private
    String fileName;

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public List<Long> getSubChapterList() {
        return subChapterList;
    }

    public void setSubChapterList(List<Long> subChapterList) {
        this.subChapterList = subChapterList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
