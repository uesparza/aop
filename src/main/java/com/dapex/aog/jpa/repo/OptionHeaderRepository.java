package com.dapex.aog.jpa.repo;

import com.dapex.aog.jpa.domain.OptionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionHeaderRepository extends JpaRepository<OptionHeader, Long> {

    List<OptionHeader> findByHierarchyId(Long id);

}
