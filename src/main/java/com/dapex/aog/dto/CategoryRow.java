package com.dapex.aog.dto;

import java.util.List;

public class CategoryRow {

    private String category;

    private String categoryUnstyled;

    private String standard;

    private String nonstandard;

    private String bar;

    private List<String> embedds;

    /**
     * If its not in the standard 3 column setup, it will be caught as either a definition
     * or an overview by the Chapter object.
     *
     * @param category    The category for which standard non-standard and bar belong to
     * @param standard    The HTML in the column of "Standard" for the table
     * @param nonstandard the HTML in the column of "NonStandard" for the table
     * @param bar         the HTML in the column of "Requires Bar Approval" for the table
     */
    public CategoryRow(String category, String categoryUnstyled, String standard, String nonstandard, String bar, List<String> embedds) {

        this.category = category;
        this.categoryUnstyled = categoryUnstyled;
        this.standard = standard;
        this.nonstandard = nonstandard;
        this.bar = bar;
        this.embedds = embedds;
    }


    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    public String getCategoryUnstyled() {

        return categoryUnstyled;
    }

    public void setCategoryUnstyled(String categoryUnstyled) {

        this.categoryUnstyled = categoryUnstyled;
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

    public List<String> getEmbedds() {

        return embedds;
    }

    public void setEmbedds(List<String> embedds) {

        this.embedds = embedds;
    }

    public void addEmbed(String embedd) {

        getEmbedds().add(embedd);
    }

}
