package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.api.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {	

}