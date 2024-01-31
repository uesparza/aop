package com.dapex.aog.dto;

import java.util.List;

/**
 * Created by mmacpher on 12/19/18.
 */
public class SearchChapterInput {

    private String searchTerm;

    private Long platform;

    private List<Long> chapters;

    public String getSearchTerm() {

        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {

        this.searchTerm = searchTerm;
    }

    public Long getPlatform() {

        return platform;
    }

    public void setPlatform(Long platform) {

        this.platform = platform;
    }

    public List<Long> getChapters() {

        return chapters;
    }

    public void setChapters(List<Long> chapters) {

        this.chapters = chapters;
    }

}
