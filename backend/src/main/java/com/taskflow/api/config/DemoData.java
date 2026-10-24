package com.taskflow.api.config;

import com.taskflow.api.model.*;
import com.taskflow.api.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Configuration
public class DemoData {
    @Bean CommandLineRunner seedDemoData(@Value("${app.seed-demo:true}") boolean enabled,UserRepository users,ProjectRepository projects,ProjectMemberRepository members,TaskRepository tasks,CommentRepository comments,NotificationRepository notifications,PasswordEncoder encoder){
        return args->{if(!enabled||users.count()>0)return;
            User jordan=users.save(new User("demo@taskflow.local","Jordan Davis",encoder.encode("TaskFlow123!")));jordan.setJobTitle("Product designer");
            User olivia=users.save(new User("olivia@taskflow.local","Olivia Rhye",encoder.encode("TaskFlow123!")));User phoenix=users.save(new User("phoenix@taskflow.local","Phoenix Baker",encoder.encode("TaskFlow123!")));User lana=users.save(new User("lana@taskflow.local","Lana Steiner",encoder.encode("TaskFlow123!")));
            Project website=projects.save(new Project("Website redesign","A thoughtful refresh for the Studio North website.","violet",jordan));Project mobile=projects.save(new Project("Mobile app","A better mobile experience for our customers.","orange",jordan));Project brand=projects.save(new Project("Brand refresh","A cohesive identity across every touchpoint.","teal",jordan));
            members.save(new ProjectMember(website,jordan,"ADMIN"));members.save(new ProjectMember(website,olivia,"MEMBER"));members.save(new ProjectMember(website,phoenix,"MEMBER"));members.save(new ProjectMember(website,lana,"MEMBER"));members.save(new ProjectMember(mobile,jordan,"ADMIN"));members.save(new ProjectMember(mobile,olivia,"MEMBER"));members.save(new ProjectMember(brand,jordan,"ADMIN"));members.save(new ProjectMember(brand,lana,"MEMBER"));
            TaskItem a=tasks.save(new TaskItem("Map the onboarding journey","Review the current flow and document opportunities.",TaskStatus.BACKLOG,TaskPriority.HIGH,LocalDate.now().plusDays(1),"Research",website,olivia,jordan));
            tasks.save(new TaskItem("Write product positioning","Draft the primary value propositions for each audience.",TaskStatus.BACKLOG,TaskPriority.MEDIUM,LocalDate.now().plusDays(3),"Content",website,phoenix,jordan));
            tasks.save(new TaskItem("Audit competitor landing pages","Capture patterns and open questions.",TaskStatus.BACKLOG,TaskPriority.LOW,LocalDate.now().plusDays(5),"Research",website,lana,jordan));
            tasks.save(new TaskItem("Build responsive navigation","Implement the shared navigation across breakpoints.",TaskStatus.IN_PROGRESS,TaskPriority.HIGH,LocalDate.now(),"Development",website,jordan,jordan));
            tasks.save(new TaskItem("Create design system foundations","Set up type, spacing, color, and component tokens.",TaskStatus.IN_PROGRESS,TaskPriority.HIGH,LocalDate.now().plusDays(2),"Design",website,olivia,jordan));
            tasks.save(new TaskItem("Review homepage prototypes","Collect feedback and resolve final comments.",TaskStatus.IN_REVIEW,TaskPriority.MEDIUM,LocalDate.now(),"Design",website,phoenix,jordan));
            tasks.save(new TaskItem("Set up analytics events","Track the core homepage interactions.",TaskStatus.IN_REVIEW,TaskPriority.LOW,LocalDate.now().plusDays(4),"Development",website,jordan,jordan));
            TaskItem done=tasks.save(new TaskItem("Finalize brand guidelines","Publish a concise guide for the refreshed identity.",TaskStatus.DONE,TaskPriority.HIGH,LocalDate.now().minusDays(1),"Brand",brand,lana,jordan));
            tasks.save(new TaskItem("Design mobile checkout flow","Map the purchase journey on mobile.",TaskStatus.IN_PROGRESS,TaskPriority.MEDIUM,LocalDate.now().plusDays(4),"Design",mobile,olivia,jordan));
            comments.save(new TaskComment(a,phoenix,"I added the first-round interview notes to the project."));comments.save(new TaskComment(done,lana,"Guidelines are ready for the team to review."));
            notifications.save(new AppNotification(jordan,website,a,"Phoenix Baker mentioned you in "+a.getTitle()));
        };
    }
}
