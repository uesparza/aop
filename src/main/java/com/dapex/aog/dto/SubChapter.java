package com.dapex.aog.dto;

import java.util.List;
import java.util.Map;

public class SubChapter {

    private String title;

    private Map<String, String> definitions;

    private List<Table> tables;


    /**
     * @param title      The title of the Chapter.
     * @param definitions Text inside the table that's assigned to a category but does not fall under Standard/Bar.
     * @param tables     List of table objects assigned to this Chapter/Sub-Chapter.
     */
    public SubChapter(String title, Map<String, String> definitions, List<Table> tables) {

        this.title = title;
        this.definitions = definitions;
        this.tables = tables;
    }


    public List<Table> getTables() {

        return tables;
    }

    public void setTables(List<Table> tables) {

        this.tables = tables;
    }


    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public Map<String, String> getDefinitions() {

        return definitions;
    }

    public void setDefinitions(Map<String, String> definitions) {

        this.definitions = definitions;
    }

}
