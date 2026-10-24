package com.taskflow.api.repository;

import com.taskflow.api.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember,Long> { Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId,Long userId); List<ProjectMember> findByProject_Id(Long projectId); List<ProjectMember> findByUser_Id(Long userId); boolean existsByProject_IdAndUser_Id(Long projectId,Long userId); }
