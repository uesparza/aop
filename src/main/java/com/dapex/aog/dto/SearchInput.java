package com.dapex.aog.dto;

/**
 * Created by mmacpher on 12/18/18.
 */
public class SearchInput {

    private String searchTerm;

    private Long platform;

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

    @Override
    public String toString() {
        return "SearchInput{" +
                "searchTerm='" + searchTerm + '\'' +
                ", platform=" + platform +
                '}';
    }

}
