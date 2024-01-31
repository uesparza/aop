package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ChapterDTO {


    public ChapterDTO() {
    }

    public ChapterDTO(Integer platformId, List<Long> chapterList, String fileName) {
        this.platformId = platformId;
        this.chapterList = chapterList;
        this.fileName = fileName;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public List<Long> getChapterList() {
        return chapterList;
    }

    public void setChapterList(List<Long> chapterList) {
        this.chapterList = chapterList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "ChapterDTO{" +
                "platformId=" + platformId +
                ", chapterList=" + chapterList +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    @JsonProperty("platformId")
    private
    Integer platformId;
    @JsonProperty("chapterList")
    private
    List<Long> chapterList;
    @JsonProperty("fileName")
    private
    String fileName;
}
