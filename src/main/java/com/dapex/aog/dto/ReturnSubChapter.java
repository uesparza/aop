package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ReturnSubChapter {

    @JsonProperty("subChapterName")
    private String subChapterName;

    @JsonProperty("subChapterId")
    private Long subChapterId;

    @JsonProperty("categories")
    private List<ReturnCategory> categories;

    @JsonProperty("seqNumber")
    private Long seqNumber;

    @JsonProperty("match")
    private boolean match;

    public ReturnSubChapter() {
        super();
        setUpArrayLists();
    }

    public ReturnSubChapter(String subChapterName, Long subChapterId, Long seqNumber) {
        this.subChapterName = subChapterName;
        this.subChapterId = subChapterId;
        this.seqNumber = seqNumber;
        this.match = false;
        setUpArrayLists();
    }

    public ReturnSubChapter(ReturnSubChapter subChapter) {
        this.subChapterName = subChapter.getSubChapterName();
        this.subChapterId = subChapter.getSubChapterId();
        this.seqNumber = subChapter.getSeqNumber();
        this.match = subChapter.isMatch();
        setUpArrayLists();

        if (subChapter.getCategories() != null && subChapter.getCategories().size() > 0) {
            setUpCategories(subChapter.getCategories());
        }
    }

    private void setUpArrayLists() {
        this.categories = new ArrayList<>();
    }

    //copy method for categories
    private void setUpCategories(List<ReturnCategory> returnCategories) {

        for (ReturnCategory category : returnCategories) {
            if (category != null) {
                ReturnCategory cat = new ReturnCategory(category);
                this.categories.add(cat);
            }
        }
    }

    public Long getSubChapterId() {
        return subChapterId;
    }

    public void setSubChapterId(Long subChapterId) {
        this.subChapterId = subChapterId;
    }

    public String getSubChapterName() {
        return subChapterName;
    }

    public void setSubChapterName(String subChapterName) {
        this.subChapterName = subChapterName;
    }

    public List<ReturnCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ReturnCategory> categories) {
        this.categories = categories;
    }

    public void addCategory(ReturnCategory category) {
        int index = 0;
        while (index < this.categories.size()) {
            ReturnCategory returnCategory = this.categories.get(index);
            if (category.getSeqNumber() < returnCategory.getSeqNumber()) {
                this.categories.add(index, category);
                return;
            }
            index++;
        }
        this.categories.add(index, category);
    }

    public void removeCategory(ReturnCategory category) {
        this.categories.remove(category);
    }

    public Long getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(Long seqNumber) {
        this.seqNumber = seqNumber;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    @Override
    public String toString() {
        return "SubChapter{" +
                "subChapterName='" + subChapterName + '\'' +
                ", subChapterId='" + subChapterId + '\'' +
                ", seqNumber=" + seqNumber +
                ", categories=" + categories +
                ", match=" + match +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnSubChapter)) return false;

        ReturnSubChapter that = (ReturnSubChapter) o;

        return getSubChapterId().equals(that.getSubChapterId());
    }

    @Override
    public int hashCode() {
        return getSubChapterId().hashCode();
    }

}
