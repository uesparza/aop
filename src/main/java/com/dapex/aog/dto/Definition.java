package com.dapex.aog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Definition {

    private Long id;

    private String text;


    public Definition(@JsonProperty("id") Long id, @JsonProperty("text") String text) {
        this.id = id;
        this.text = text;
    }

    public Definition(Definition definition) {
        this.id = definition.getId();
        this.text = definition.getText();
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

        return "Definition{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }

}
