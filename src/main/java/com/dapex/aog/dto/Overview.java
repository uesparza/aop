package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Overview {

    private Long id;

    private String text;

    public Overview(@JsonProperty("id") Long id, @JsonProperty("text") String text) {
        this.id = id;
        this.text = text;
    }

    //copy constructor
    public Overview(Overview overview) {
        this.id = overview.getId();
        this.text = overview.getText();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Overview{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }

}
