package com.dapex.aog.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class TableOfContents {

    private LinkedHashMap<String, ArrayList<String>> chapters;

    public TableOfContents() {

    }


    public LinkedHashMap<String, ArrayList<String>> getChapters() {

        return chapters;
    }

    private void setChapters(LinkedHashMap<String, ArrayList<String>> chapters) {

        this.chapters = chapters;
    }


    /**
     * Adds a new chapter to the Table of Contents. Handles logic if the list is empty. Both of these
     * paramaters are used to populate the @chapters object above. The chapter is our key. If we have no
     * sub-chapter, this means we make a key that has no values (which has no sub-chapters). If it has values,
     * then these are the sub-chapters for the Chapter key. The very first time we make a chapter, it will
     * have no sub-chapters yet. At this point, it will have a null key. If we find a sub-chapter, we first have
     * to remove the null chapter, then start appending onto it.
     *
     * @param chapter    : This is the chapter name as a String.
     * @param subchapter : This is the subchapter name as a String - this can be null.
     **/
    public void addChapter(String chapter, String subchapter) {

        if (this.getChapters() != null) {
            LinkedHashMap<String, ArrayList<String>> chaps = this.getChapters();
            //Chapter Exists! Add a sub chapter to it.
            if (chaps.containsKey(chapter)) {
                //The first key is null when inserted so we need to make sure to remove it
                chaps.get(chapter).add(subchapter);
                chaps.get(chapter).remove(null);
            } else {
                //Chapter does not exist. Make a chapter with no subchapters
                chaps.put(chapter, new ArrayList<>(Collections.singletonList(subchapter)));
            }
        } else {
            LinkedHashMap<String, ArrayList<String>> chapMap = new LinkedHashMap<>();
            chapMap.put(chapter, new ArrayList<>(Collections.singletonList(subchapter)));
            setChapters(chapMap);
        }
    }

}
