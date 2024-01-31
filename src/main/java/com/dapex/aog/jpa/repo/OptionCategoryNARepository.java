package com.dapex.aog.jpa.repo;

import com.dapex.aog.jpa.domain.OptionCategoryNA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OptionCategoryNARepository extends JpaRepository<OptionCategoryNA, Long> {

    OptionCategoryNA findByHierarchyId(Long id);

    // (TODO) - determine if this is needed under assumption of no more duplicate rows
    @Transactional
    @Modifying
    @Query(value = "UPDATE aog_option_category_na aocn " +
            "SET aocn.standard = :standard, " +
            "aocn.non_standard = :non_standard, " +
            "aocn.bar_approval = :bar_approval, " +
            "aocn.excluded = :excluded, " +
            "aocn.additional_information = :additional_information " +
            "WHERE aocn.hierarchy_id = :hierarchy_id", nativeQuery = true)
    int updateField(@Param("hierarchy_id") Long hierarchyId, @Param("standard") String standard, @Param("non_standard") String nonStandard,
                    @Param("bar_approval") String barApproval, @Param("excluded") String excluded, @Param("additional_information") String additionalInformation);


}
