package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class CategoryDTO {


    public CategoryDTO(){

    }
    public CategoryDTO(Integer platformId, Integer chapter, List<Long> categoryList, String fileName) {
        this.platformId = platformId;
        this.chapter = chapter;
        this.categoryList = categoryList;
        this.fileName = fileName;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public Integer getChapter() {
        return chapter;
    }

    public void setChapter(Integer chapter) {
        this.chapter = chapter;
    }

    public List<Long> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Long> categoryList) {
        this.categoryList = categoryList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    @JsonProperty("platformId")
    Integer platformId;
    @JsonProperty("chapter")
    Integer chapter;
    @JsonProperty("categoryList")
    List<Long> categoryList;
    @JsonProperty("fileName")
    String fileName;

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "platformId=" + platformId +
                ", chapter=" + chapter +
                ", categoryList=" + categoryList +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
