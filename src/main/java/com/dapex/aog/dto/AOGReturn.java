package com.dapex.aog.dto;

import java.util.ArrayList;
import java.util.List;

public class AOGReturn {

    private List<ReturnChapter> results;

    public AOGReturn() {
        this.results = new ArrayList<>();
    }

    public List<ReturnChapter> getResults() {

        return results;
    }

    public void setResults(List<ReturnChapter> results) {

        this.results = results;
    }

    public void addChapter(ReturnChapter chapter) {
        if (this.results == null) {
            this.results = new ArrayList<>();
            this.results.add(chapter);
        } else {
            int index = 0;
            while (index < this.results.size()) {
                ReturnChapter returnChapter = this.results.get(index);
                if (chapter.getSeqNumber() < returnChapter.getSeqNumber()) {
                    this.results.add(index, chapter);
                    return;
                }
                index++;
            }
            this.results.add(index, chapter);
        }
    }

    public void setChapter(ReturnChapter chapter) {
        if (this.results == null) {
            this.results = new ArrayList<>();
            this.results.add(chapter);
        } else {
            int index = 0;
            while (index < this.results.size()) {
                ReturnChapter returnChapter = this.results.get(index);
                if (chapter.getChapterId().equals(returnChapter.getChapterId())) {
                    this.results.set(index, chapter);
                    return;
                }
                index++;
            }
            this.results.add(index, chapter);
        }
    }

    public void removeChapter(ReturnChapter chapter) {
        this.results.remove(chapter);
    }

    @Override
    public String toString() {
        return "AOGReturn{" +
                "results=" + results +
                '}';
    }

}
