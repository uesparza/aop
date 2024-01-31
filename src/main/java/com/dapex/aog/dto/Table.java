package com.dapex.aog.dto;

import java.util.List;

public class Table {

    private List<CategoryRow> rows;

    public Table(List<CategoryRow> rows) {

        this.rows = rows;
    }

    public List<CategoryRow> getRows() {

        return rows;
    }

    public void setRows(List<CategoryRow> rows) {

        this.rows = rows;
    }

}
