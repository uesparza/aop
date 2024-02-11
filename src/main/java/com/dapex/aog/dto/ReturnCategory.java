package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.dapex.aog.jpa.domain.Attachment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReturnCategory {

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("standard")
    private String standard;

    @JsonProperty("nonstandard")
    private String nonstandard;

    @JsonProperty("bar")
    private String bar;

    @JsonProperty("additionalInformation")
    private String additionalInformation;

    @JsonProperty("chapterName")
    private String chapterName;

    @JsonProperty("subChapter")
    private String subChapter;

    @JsonProperty("definitions")
    private List<Definition> definitions;

    @JsonProperty("overviews")
    private List<Overview> overviews;

    @JsonProperty("attachments")
    private List<Attachment> attachments;

    @JsonProperty("noExceptions")
    private String noExceptions;

    @JsonProperty("lastUpdated")
    private Date lastUpdated;

    @JsonProperty("owner")
    private Long owner;

    @JsonProperty("seqNumber")
    private Long seqNumber;

    @JsonProperty("match")
    private boolean match;

    public ReturnCategory() {
        setupLists();
    }

    /**
     * Copy constructor for Chapters
     * @param category the category for the deep copy
     */
    public ReturnCategory(ReturnCategory category) {
        this.categoryName = category.getCategoryName();
        this.categoryId = category.getCategoryId();
        this.standard = category.getStandard();
        this.nonstandard = category.getNonstandard();
        this.bar = category.getBar();
        this.additionalInformation = category.getAdditionalInformation();
        this.chapterName = category.getChapterName();
        this.subChapter = category.getSubChapter();
        this.definitions = initDefinitions(category);
        this.overviews = initOverviews(category);
        this.attachments = initAttachments(category);
        this.noExceptions = category.getNoExceptions();
        this.lastUpdated = category.getLastUpdated();
        this.owner = category.getOwner();
        this.seqNumber = category.getSeqNumber();
        this.match = category.isMatch();
    }

    public ReturnCategory(String categoryName, Long categoryId, Long seqNumber) {
        setupLists();
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.seqNumber = seqNumber;
    }

    public ReturnCategory(String categoryName, Long categoryId, Long seqNumber, String chapterName) {
        setupLists();
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.seqNumber = seqNumber;
        this.chapterName = chapterName;
    }

    private void setupLists() {
        this.definitions = new ArrayList<>();
        this.overviews = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.categoryName = "";
        this.standard = "";
        this.nonstandard = "";
        this.bar = "";
        this.chapterName = "";
        this.subChapter = "";
        this.noExceptions = "";
        this.match = false;
    }

    private ArrayList<Definition> initDefinitions(ReturnCategory category) {
        if (category.getDefinitions() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(category.getDefinitions());
    }

    private ArrayList<Overview> initOverviews(ReturnCategory category) {
        if (category.getOverviews() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(category.getOverviews());
    }

    private ArrayList<Attachment> initAttachments(ReturnCategory category) {
        if (category.getAttachments() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(category.getAttachments());
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getNonstandard() {
        return nonstandard;
    }

    public void setNonstandard(String nonstandard) {
        this.nonstandard = nonstandard;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getSubChapter() {
        return subChapter;
    }

    public void setSubChapter(String subChapter) {
        this.subChapter = subChapter;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definition) {
        this.definitions = definition;
    }

    public List<Overview> getOverviews() {
        return overviews;
    }

    public void setOverviews(List<Overview> overviews) {
        this.overviews = overviews;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachments(Attachment attachment) {
        this.attachments.add(attachment);
    }

    public boolean removeAttachments(Attachment attachment) {
        return this.attachments.remove(attachment);
    }

    public String getNoExceptions() {
        return noExceptions;
    }

    public void setNoExceptions(String noExceptions) {
        this.noExceptions = noExceptions;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
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
        return "Category{" +
                "categoryName='" + categoryName + '\'' +
                ", categoryId=" + categoryId +
                ", standard='" + standard + '\'' +
                ", nonstandard='" + nonstandard + '\'' +
                ", bar='" + bar + '\'' +
                ", additionalInformation='" + additionalInformation + '\'' +
                ", chapterName='" + chapterName + '\'' +
                ", subChapter='" + subChapter + '\'' +
                ", definitions=" + definitions +
                ", overviews=" + overviews +
                ", attachments=" + attachments +
                ", noExceptions='" + noExceptions + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", owner=" + owner +
                ", seqNumber=" + seqNumber +
                ", match=" + match +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnCategory)) return false;

        ReturnCategory that = (ReturnCategory) o;

        return getCategoryId().equals(that.getCategoryId());
    }

    @Override
    public int hashCode() {
        return getCategoryId().hashCode();
    }

}
