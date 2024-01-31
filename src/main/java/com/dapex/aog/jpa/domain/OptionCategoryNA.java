package com.dapex.aog.jpa.domain;

import com.dapex.aog.dto.CategoryRow;
import com.dapex.aog.dto.ReturnCategory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.dapex.aog.config.Constants;
import com.dapex.aog.utils.ProcessingUtility;

@Entity
@Table(name = Constants.OPTION_CATEGORY_TABLE)
public class OptionCategoryNA {

    @Id
    @Column(name = "hierarchy_id")
    private Long hierarchyId;

    @Column(name = "standard")
    private String standard;

    @Column(name = "non_standard")
    private String nonStandard;

    @Column(name = "bar_approval")
    private String barApproval;

    @Column(name = "excluded")
    private String excluded;

    @Column(name = "additional_information")
    private String additionalInformation;

    @Column(name = "full_text")
    private String fullText;

    public OptionCategoryNA() { }

    public OptionCategoryNA(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
        this.standard = Constants.LEFT_BLANK;
        this.nonStandard = Constants.LEFT_BLANK;
        this.barApproval = Constants.LEFT_BLANK;
        this.excluded = Constants.LEFT_BLANK;
    }

    public OptionCategoryNA(Long hierarchyId, CategoryRow row) {
        this.hierarchyId = hierarchyId;
        this.standard = row.getStandard();
        this.nonStandard = row.getNonstandard();
        this.barApproval = row.getBar();
        this.excluded = Constants.LEFT_BLANK;
        this.fullText = ProcessingUtility.htmlToRaw(this.standard, this.nonStandard,
                this.barApproval, this.excluded, this.additionalInformation);
    }

    public OptionCategoryNA(ReturnCategory category) {
        this.hierarchyId = category.getCategoryId();
        this.standard = category.getStandard();
        this.nonStandard = category.getNonstandard();
        this.barApproval = category.getBar();
        this.excluded = category.getNoExceptions();
        this.additionalInformation = category.getAdditionalInformation();
        this.fullText = ProcessingUtility.htmlToRaw(this.standard, this.nonStandard,
                this.barApproval, this.excluded, this.additionalInformation);
    }

    public Long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getNonStandard() {
        return nonStandard;
    }

    public void setNonStandard(String nonStandard) {
        this.nonStandard = nonStandard;
    }

    public String getBarApproval() {
        return barApproval;
    }

    public void setBarApproval(String barApproval) {
        this.barApproval = barApproval;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getExcluded() {
        return excluded;
    }

    public void setExcluded(String excluded) {
        this.excluded = excluded;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    @Override
    public String toString() {

        return "OptionCategoryNA{" +
                "hierarchyId=" + hierarchyId +
                ", standard='" + standard + '\'' +
                ", nonStandard='" + nonStandard + '\'' +
                ", barApproval='" + barApproval + '\'' +
                ", excluded='" + excluded + '\'' +
                ", fullText='" + fullText + '\'' +
                '}';
    }

}
