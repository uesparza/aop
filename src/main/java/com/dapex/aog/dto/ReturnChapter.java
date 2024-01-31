package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.dapex.aog.jpa.domain.Attachment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmacpher on 12/3/18.
 */
public class ReturnChapter {

    @JsonProperty("chapterName")
    private String chapterName;

    @JsonProperty("chapterId")
    private Long chapterId;

    @JsonProperty("overviews")
    private List<Overview> overviews;

    @JsonProperty("definitions")
    private List<Definition> definitions;

    @JsonProperty("subChapterList")
    private List<ReturnSubChapter> subChapterList;

    @JsonProperty("categories")
    private List<ReturnCategory> categories;

    @JsonProperty("attachments")
    private List<Attachment> attachments;

    @JsonProperty("seqNumber")
    private Long seqNumber;

    @JsonProperty("match")
    private boolean match;

    public ReturnChapter() {
        super();
        setUpArrayLists();
    }

    public ReturnChapter(String chapterName, Long chapterId, Long seqNumber) {
        this.chapterName = chapterName;
        this.chapterId = chapterId;
        this.seqNumber = seqNumber;
        this.match = false;
        setUpArrayLists();
    }

    /**
     * Copy constructor for Chapters
     *
     * @param chapter the chapterName for the deep copy
     */
    public ReturnChapter(ReturnChapter chapter) {
        this.chapterName = chapter.getChapterName();
        this.chapterId = chapter.getChapterId();
        this.seqNumber = chapter.getSeqNumber();
        this.match = chapter.isMatch();
        setUpArrayLists();

        if (chapter.getDefinitions() != null && chapter.getDefinitions().size() > 0) {
            setUpDefs(chapter.getDefinitions());
        }

        if (chapter.getOverviews() != null && chapter.getOverviews().size() > 0) {
            setUpOverviews(chapter.getOverviews());
        }

        if (chapter.getSubChapterList() != null && chapter.getSubChapterList().size() > 0) {
            setUpSubChapterList(chapter.getSubChapterList());
        }

        if (chapter.getCategories() != null && chapter.getCategories().size() > 0) {
            setUpCategoryList(chapter.getCategories());
        }

        if (chapter.attachments != null && chapter.attachments.size() > 0) {
            setUpAttachmentList(chapter.getAttachments());
        }
    }

    private void setUpArrayLists() {

        this.subChapterList = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.definitions = new ArrayList<>();
        this.overviews = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    //copy method for definitions
    //Need to refactor
    private void setUpDefs(List<Definition> defs) {

        for (Definition def : defs) {
            if (def != null) {
                Definition definition = new Definition(def);
                this.definitions.add(definition);
            }
        }
    }

    //copy method for overviews
    //Need to refactor
    private void setUpOverviews(List<Overview> ovs) {

        for (Overview overview : ovs) {
            if (overview != null) {
                Overview ov = new Overview(overview);
                this.overviews.add(ov);
            }
        }
    }

    //copy method for the subChapterList
    private void setUpSubChapterList(List<ReturnSubChapter> returnSubChapterList) {

        for (ReturnSubChapter sub : returnSubChapterList) {
            if (sub != null) {
                ReturnSubChapter ret = new ReturnSubChapter(sub);
                this.subChapterList.add(ret);
            }
        }
    }

    //copy method for the subChapterList
    private void setUpCategoryList(List<ReturnCategory> categoryList) {

        for (ReturnCategory cat : categoryList) {
            if (cat != null) {
                ReturnCategory ret = new ReturnCategory(cat);
                this.categories.add(ret);
            }
        }
    }

    private void setUpAttachmentList(List<Attachment> attachmentList) {

        for (Attachment attachment : attachmentList) {
            if (attachment != null) {
                this.attachments.add(attachment);
            }
        }
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public List<Overview> getOverviews() {
        return overviews;
    }

    public void setOverviews(List<Overview> overviews) {
        this.overviews = overviews;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public List<ReturnSubChapter> getSubChapterList() {
        return subChapterList;
    }

    public void setSubChapterList(List<ReturnSubChapter> subChapterList) {
        this.subChapterList = subChapterList;
    }

    public void addSubChapter(ReturnSubChapter subChapter) {
        int index = 0;
        while (index < subChapterList.size()) {
            ReturnSubChapter returnSubChapter = subChapterList.get(index);
            if (subChapter.getSeqNumber() < returnSubChapter.getSeqNumber()) {
                this.subChapterList.add(index, subChapter);
                return;
            }
            index++;
        }
        this.subChapterList.add(index, subChapter);
    }

    public void removeSubChapter(ReturnSubChapter subChapter) {
        this.subChapterList.remove(subChapter);
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

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
    }

    public boolean removeAttachment(Attachment attachment) {
        return this.attachments.remove(attachment);
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    @Override
    public String toString() {
        return "ReturnChapter{" +
                "chapterName='" + chapterName + '\'' +
                ", chapterId=" + chapterId +
                ", overviews=" + overviews +
                ", definitions=" + definitions +
                ", seqNumber=" + seqNumber +
                ", subChapterList=" + subChapterList +
                ", categories=" + categories +
                ", attachments=" + attachments +
                ", match=" + match +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnChapter)) return false;

        ReturnChapter that = (ReturnChapter) o;

        return getChapterId().equals(that.getChapterId());
    }

    @Override
    public int hashCode() {
        return getChapterId().hashCode();
    }

}
