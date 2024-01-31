package com.dapex.aog.jpa.repo;

import com.dapex.aog.jpa.domain.OptionHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionHierarchyRepository extends JpaRepository<OptionHierarchy, Long> {

    List<OptionHierarchy> findByParentIdIsNull();

    List<OptionHierarchy> findByParentIdAndLabel(Long id, String label);

    OptionHierarchy findOneByLabelTxt(String labelText);

    OptionHierarchy findOneById(Long id);

}
