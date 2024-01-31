package com.dapex.aog.jpa.domain;

import com.dapex.aog.config.Constants;
import com.dapex.aog.dto.ContainerCreate;
import com.dapex.aog.dto.ReturnCategory;
import com.dapex.aog.dto.ReturnChapter;
import com.dapex.aog.dto.ReturnSubChapter;
import org.springframework.lang.NonNull;

import javax.persistence.*;

@Entity
@Table(name = Constants.OPTION_HIERARCHY_TABLE)
public class OptionHierarchy implements Comparable<OptionHierarchy> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "label")
    private String label;

    @Column(name = "label_txt")
    private String labelTxt;

    @Column(name = "seq_nbr")
    private Long seqNbr;

    public OptionHierarchy() { }

    public OptionHierarchy(Long parentId, String label, String labelTxt, Long seqNbr) {
        this.parentId = parentId;
        this.label = label;
        this.labelTxt = labelTxt;
        this.seqNbr = seqNbr;
    }

    public OptionHierarchy(ContainerCreate container) {
        this.parentId = container.getParentId();
        this.label = container.getContainerLabel();
        this.labelTxt = container.getContainerName();
        this.seqNbr = container.getSeqNumber();
    }

    public OptionHierarchy(Long parentId, ReturnChapter chapter) {
        this.parentId = parentId;
        this.label = Constants.CHAPTER_NAME;
        this.labelTxt = chapter.getChapterName();
        this.seqNbr = chapter.getSeqNumber();
        this.id = chapter.getChapterId();
    }

    public OptionHierarchy(Long parentId, ReturnSubChapter subChapter) {
        this.parentId = parentId;
        this.label = Constants.SUBCHAPTER_NAME;
        this.labelTxt = subChapter.getSubChapterName();
        this.seqNbr = subChapter.getSeqNumber();
        this.id = subChapter.getSubChapterId();
    }

    public OptionHierarchy(Long parentId, ReturnCategory category) {
        this.parentId = parentId;
        this.label = Constants.CATEGORY_NAME;
        this.labelTxt = category.getCategoryName();
        this.seqNbr = category.getSeqNumber();
        this.id = category.getCategoryId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelTxt() {
        return labelTxt;
    }

    public void setLabelTxt(String labelTxt) {
        this.labelTxt = labelTxt;
    }

    public Long getSeqNbr() {
        return seqNbr;
    }

    public void setSeqNbr(Long seqNbr) {

        this.seqNbr = seqNbr;
    }

    @Override
    public String toString() {

        return "OptionHierarchy{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", label='" + label + '\'' +
                ", labelTxt='" + labelTxt + '\'' +
                ", seqNbr='" + seqNbr + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull OptionHierarchy hierarchy) {
        try {
            return this.seqNbr > hierarchy.getSeqNbr() ? 1 : this.seqNbr < hierarchy.getSeqNbr() ? -1 : 0;
        } catch (Exception e) {
            return 0;
        }

    }

}
