package com.att.tdp.issueflow;

import com.att.tdp.issueflow.dto.request.CreateProjectRequest;
import com.att.tdp.issueflow.dto.request.CreateUserRequest;
import com.att.tdp.issueflow.dto.request.UpdateProjectRequest;
import com.att.tdp.issueflow.dto.response.ProjectResponse;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Long ownerId;

    @BeforeEach
    void setup() {
        CreateUserRequest user = new CreateUserRequest();
        user.setUsername("proj_owner_" + System.nanoTime());
        user.setEmail(System.nanoTime() + "@owner.com");
        user.setFullName("Owner");
        user.setRole(Role.ADMIN);
        user.setPassword("secret");
        UserResponse owner = restTemplate.postForEntity("/users", user, UserResponse.class).getBody();
        ownerId = owner.getId();
    }

    private ProjectResponse createProject(String name) {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setName(name);
        req.setDescription("Test project");
        req.setOwnerId(ownerId);
        return restTemplate.postForEntity("/projects", req, ProjectResponse.class).getBody();
    }

    @Test
    void createAndGetProject() {
        ProjectResponse project = createProject("My Project");
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo("My Project");

        ResponseEntity<ProjectResponse> fetched = restTemplate.getForEntity("/projects/" + project.getId(), ProjectResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void updateProject() {
        ProjectResponse project = createProject("Old Name");
        UpdateProjectRequest req = new UpdateProjectRequest();
        req.setName("New Name");
        restTemplate.patchForObject("/projects/" + project.getId(), req, Void.class);

        ResponseEntity<ProjectResponse> fetched = restTemplate.getForEntity("/projects/" + project.getId(), ProjectResponse.class);
        assertThat(fetched.getBody().getName()).isEqualTo("New Name");
    }

    @Test
    void softDeleteAndRestore() {
        ProjectResponse project = createProject("To Delete");
        Long id = project.getId();

        restTemplate.delete("/projects/" + id);

        ResponseEntity<String> fetched = restTemplate.getForEntity("/projects/" + id, String.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<ProjectResponse[]> deleted = restTemplate.getForEntity("/projects/deleted", ProjectResponse[].class);
        assertThat(deleted.getBody()).isNotNull();
        boolean found = false;
        for (ProjectResponse p : deleted.getBody()) {
            if (p.getId().equals(id)) { found = true; break; }
        }
        assertThat(found).isTrue();

        restTemplate.postForEntity("/projects/" + id + "/restore", null, Void.class);
        ResponseEntity<ProjectResponse> restored = restTemplate.getForEntity("/projects/" + id, ProjectResponse.class);
        assertThat(restored.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAll() {
        createProject("Proj A");
        ResponseEntity<ProjectResponse[]> all = restTemplate.getForEntity("/projects", ProjectResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotEmpty();
    }
}
