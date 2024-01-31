package com.dapex.aog.jpa.repo;

import com.dapex.aog.jpa.domain.OptionReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionReviewRepository extends JpaRepository<OptionReview, Long> {

    OptionReview findByHierarchyId(Long id);

}
