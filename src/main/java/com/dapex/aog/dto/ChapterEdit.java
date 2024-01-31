package com.dapex.aog.dto;

/**
 * Created by mmacpher on 12/21/18.
 */
public class ChapterEdit {

    private Long aogId;

    private ReturnChapter chapter;

    public Long getAogId() {
        return aogId;
    }

    public void setAogId(Long aogId) {
        this.aogId = aogId;
    }

    public ReturnChapter getChapter() {
        return chapter;
    }

    public void setChapter(ReturnChapter chapter) {
        this.chapter = chapter;
    }

    @Override
    public String toString() {
        return "ChapterEdit{" +
                "aogId=" + aogId +
                ", chapter=" + chapter +
                '}';
    }

}
