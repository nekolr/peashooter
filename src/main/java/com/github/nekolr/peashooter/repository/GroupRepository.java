package com.github.nekolr.peashooter.repository;

import com.github.nekolr.peashooter.entity.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GroupRepository  extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
}
