package com.dapex.aog.dto;

public class Chapters {

    private String chapterName;

    private Long chapterId;

    public Chapters(String chapterName, Long chapterId) {
        this.chapterName = chapterName;
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    @Override
    public String toString() {
        return "ChapterList{" +
                "chapterName='" + chapterName + '\'' +
                ", chapterId=" + chapterId +
                '}';
    }

}
