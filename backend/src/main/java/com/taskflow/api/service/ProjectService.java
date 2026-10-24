package com.taskflow.api.service;

import com.taskflow.api.dto.ApiModels.*;
import com.taskflow.api.model.*;
import com.taskflow.api.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class ProjectService {
    private final ProjectRepository projects; private final ProjectMemberRepository members; private final UserRepository users; private final TaskRepository tasks; private final CurrentUser current;
    public ProjectService(ProjectRepository projects,ProjectMemberRepository members,UserRepository users,TaskRepository tasks,CurrentUser current){this.projects=projects;this.members=members;this.users=users;this.tasks=tasks;this.current=current;}
    @Transactional(readOnly=true) public List<ProjectResponse> list(){User user=current.get();Map<Long,Project> found=new LinkedHashMap<>();projects.findByOwner_Id(user.getId()).forEach(p->found.put(p.getId(),p));members.findByUser_Id(user.getId()).forEach(m->found.put(m.getProject().getId(),m.getProject()));return found.values().stream().map(this::toResponse).toList();}
    @Transactional(readOnly=true) public ProjectResponse get(Long id){return toResponse(requireAccess(id));}
    @Transactional public ProjectResponse create(ProjectRequest request){User owner=current.get();Project project=projects.save(new Project(request.name(),request.description(),request.color()==null?"violet":request.color(),owner));members.save(new ProjectMember(project,owner,"ADMIN"));return toResponse(project);}
    @Transactional public ProjectResponse update(Long id,ProjectRequest request){Project project=requireAdmin(id);project.update(request.name(),request.description(),request.color()==null?project.getColor():request.color());return toResponse(project);}
    @Transactional(readOnly=true) public List<MemberResponse> listMembers(Long id){requireAccess(id);return members.findByProject_Id(id).stream().map(this::memberResponse).toList();}
    @Transactional public MemberResponse addMember(Long id,MemberRequest request){Project project=requireAdmin(id);User user=users.findByEmail(request.email().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No account exists for that email"));if(members.existsByProject_IdAndUser_Id(id,user.getId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"This person is already a project member");return memberResponse(members.save(new ProjectMember(project,user,request.role()==null?"MEMBER":request.role())));}
    public Project requireAccess(Long id){Project project=projects.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found"));User user=current.get();if(!project.getOwner().getId().equals(user.getId())&&!members.existsByProject_IdAndUser_Id(id,user.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Project access required");return project;}
    public Project requireAdmin(Long id){Project project=requireAccess(id);User user=current.get();boolean owner=project.getOwner().getId().equals(user.getId());boolean admin=members.findByProject_IdAndUser_Id(id,user.getId()).map(m->m.getRole().equals("ADMIN")).orElse(false);if(!owner&&!admin)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Project admin access required");return project;}
    private ProjectResponse toResponse(Project p){return new ProjectResponse(p.getId(),p.getName(),p.getDescription(),p.getColor(),p.getOwner().getId(),p.getOwner().getDisplayName(),tasks.countByProject_IdAndStatus(p.getId(),TaskStatus.BACKLOG)+tasks.countByProject_IdAndStatus(p.getId(),TaskStatus.IN_PROGRESS)+tasks.countByProject_IdAndStatus(p.getId(),TaskStatus.IN_REVIEW)+tasks.countByProject_IdAndStatus(p.getId(),TaskStatus.DONE),tasks.countByProject_IdAndStatus(p.getId(),TaskStatus.DONE),p.getCreatedAt());}
    private MemberResponse memberResponse(ProjectMember m){User u=m.getUser();return new MemberResponse(u.getId(),u.getDisplayName(),u.getEmail(),m.getRole(),m.getJoinedAt());}
}
