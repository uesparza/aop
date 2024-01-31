package com.dapex.aog.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;
import com.dapex.aog.config.Constants;

@Entity
@Table(name = Constants.OPTION_REVIEW_TABLE)
public class OptionReview {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hierarchy_id")
    private Long hierarchyId;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "review_date")
    private Date reviewDate;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getHierarchyId() {

        return hierarchyId;
    }

    public void setHierarchyId(Long hierarchyId) {

        this.hierarchyId = hierarchyId;
    }

    public Long getApproverId() {

        return approverId;
    }

    public void setApproverId(Long approverId) {

        this.approverId = approverId;
    }

    public Date getReviewDate() {

        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {

        this.reviewDate = reviewDate;
    }

    @Override
    public String toString() {

        return "OptionReview{" +
                "id=" + id +
                ", hierarchyId=" + hierarchyId +
                ", approverId=" + approverId +
                ", reviewDate=" + reviewDate +
                '}';
    }

}
