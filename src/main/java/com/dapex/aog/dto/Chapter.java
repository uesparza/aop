package com.dapex.aog.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Chapter {

    private String title;

    private List<String> overviews;

    private Map<String, String> definitions;

    private String approver;

    private String dateOfApproval;

    private List<SubChapter> subChapters;

    private List<Table> tables;

    /**
     * @param title          The title of the Chapter.
     * @param overviews       Text outside of the table and/or text inside table assigned to no category (One big row).
     * @param definitions     Text inside the table that's assigned to a category but does not fall under Standard/Bar.
     * @param tables         List of table objects assigned to this Chapter/Sub-Chapter.
     * @param approver       The MSID needed to approve the chapter for signoff.
     * @param dateOfApproval The date of which the @approver signed off on the request.
     */
    public Chapter(String title, List<String> overviews, Map<String, String> definitions,
                   String approver, String dateOfApproval, List<Table> tables) {

        this.title = title;
        this.overviews = overviews;
        this.definitions = definitions;
        this.approver = approver;
        this.dateOfApproval = dateOfApproval;
        this.tables = tables;
    }

    //This is for Chapters that DO have a sub-chapter
    public Chapter(String title, List<SubChapter> subChapters, String approver, String dateOfApproval) {

        this.title = title;
        this.subChapters = subChapters;
        this.approver = approver;
        this.dateOfApproval = dateOfApproval;
    }

    /**
     * Adds a sub-chapter to our Chapter object. Handles if there are already Sub-Chapters or not.
     *
     * @param sub The SubChapter we want to add to our @Chapter Object.
     */

    public void addSubChapter(SubChapter sub) {

        List<SubChapter> sc = getSubChapters();
        if (sc != null) {
            sc.add(sub);
            this.subChapters = sc;
        } else {
            this.subChapters = new ArrayList<>(
                    Collections.singletonList(new SubChapter(sub.getTitle(), sub.getDefinitions(), sub.getTables()))
            );
        }
    }

    public List<Table> getTables() {

        return tables;
    }

    public void setTables(List<Table> tables) {

        this.tables = tables;
    }

    public List<SubChapter> getSubChapters() {

        return subChapters;
    }

    public void setSubChapters(List<SubChapter> subchapters) {

        this.subChapters = subchapters;
    }

    public String getApprover() {

        return approver;
    }

    public void setApprover(String approver) {

        this.approver = approver;
    }

    public String getDateOfApproval() {

        return dateOfApproval;
    }

    public void setDateOfApproval(String dateOfApproval) {

        this.dateOfApproval = dateOfApproval;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public List<String> getOverviews() {

        return overviews;
    }

    public void setOverviews(List<String> overviews) {

        this.overviews = overviews;
    }

    public Map<String, String> getDefinitions() {

        return definitions;
    }

    public void setDefinitions(Map<String, String> definitions) {

        this.definitions = definitions;
    }

    public String toString() {

        ObjectMapper mapper = new ObjectMapper();

        String jsonString = "";
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            jsonString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonString;
    }
}
