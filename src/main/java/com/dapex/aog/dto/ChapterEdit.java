package com.dapex.aog.dto;

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
