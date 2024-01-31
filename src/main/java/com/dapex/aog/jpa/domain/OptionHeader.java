package com.dapex.aog.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.dapex.aog.config.Constants;

@Entity
@Table(name = Constants.OPTION_HEADER_TABLE)
public class OptionHeader {

    @Id
    @Column(name = "hierarchy_id")
    private Long hierarchyId;

    @Column(name = "definition")
    private String definition;

    @Column(name = "overview")
    private String overview;

    @Column(name = "full_text")
    private String fullText;

    public OptionHeader() { }

    public OptionHeader(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public Long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    @Override
    public String toString() {
        return "OptionHeader{" +
                "hierarchyId=" + hierarchyId +
                ", definition='" + definition + '\'' +
                ", overview='" + overview + '\'' +
                ", fullText='" + fullText + '\'' +
                '}';
    }

}
