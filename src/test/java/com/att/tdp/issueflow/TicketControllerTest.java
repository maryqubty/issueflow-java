package com.att.tdp.issueflow;

import com.att.tdp.issueflow.dto.request.CreateProjectRequest;
import com.att.tdp.issueflow.dto.request.CreateTicketRequest;
import com.att.tdp.issueflow.dto.request.CreateUserRequest;
import com.att.tdp.issueflow.dto.response.ImportResultResponse;
import com.att.tdp.issueflow.dto.response.ProjectResponse;
import com.att.tdp.issueflow.dto.response.TicketResponse;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.enums.Priority;
import com.att.tdp.issueflow.enums.Role;
import com.att.tdp.issueflow.enums.TicketStatus;
import com.att.tdp.issueflow.enums.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class TicketControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private Long projectId;

    @BeforeEach
    void setup() {
        String suffix = String.valueOf(System.nanoTime());

        CreateUserRequest user = new CreateUserRequest();
        user.setUsername("tkt_owner_" + suffix);
        user.setEmail(suffix + "@tkt.com");
        user.setFullName("TktOwner");
        user.setRole(Role.ADMIN);
        user.setPassword("secret");
        UserResponse owner = restTemplate.postForEntity("/users", user, UserResponse.class).getBody();

        CreateProjectRequest proj = new CreateProjectRequest();
        proj.setName("Tkt Project " + suffix);
        proj.setDescription("desc");
        proj.setOwnerId(owner.getId());
        ProjectResponse project = restTemplate.postForEntity("/projects", proj, ProjectResponse.class).getBody();
        projectId = project.getId();
    }

    private TicketResponse createTicket(String title) {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setTitle(title);
        req.setStatus(TicketStatus.TODO);
        req.setPriority(Priority.MEDIUM);
        req.setType(TicketType.BUG);
        req.setProjectId(projectId);
        return restTemplate.postForEntity("/tickets", req, TicketResponse.class).getBody();
    }

    @Test
    void createAndGetTicket() {
        TicketResponse ticket = createTicket("Fix bug");
        assertThat(ticket).isNotNull();
        assertThat(ticket.getTitle()).isEqualTo("Fix bug");
        assertThat(ticket.getProjectId()).isEqualTo(projectId);

        ResponseEntity<TicketResponse> fetched = restTemplate.getForEntity("/tickets/" + ticket.getId(), TicketResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getTicketsByProject() {
        createTicket("Ticket 1");
        createTicket("Ticket 2");
        ResponseEntity<TicketResponse[]> all = restTemplate.getForEntity(
                "/tickets?projectId=" + projectId, TicketResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void softDeleteAndRestore() {
        TicketResponse ticket = createTicket("To Delete");
        Long id = ticket.getId();

        restTemplate.delete("/tickets/" + id);

        ResponseEntity<String> fetched = restTemplate.getForEntity("/tickets/" + id, String.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<TicketResponse[]> deleted = restTemplate.getForEntity(
                "/tickets/deleted?projectId=" + projectId, TicketResponse[].class);
        assertThat(deleted.getBody()).isNotNull();
        boolean found = false;
        for (TicketResponse t : deleted.getBody()) {
            if (t.getId().equals(id)) { found = true; break; }
        }
        assertThat(found).isTrue();

        restTemplate.postForEntity("/tickets/" + id + "/restore", null, Void.class);
        ResponseEntity<TicketResponse> restored = restTemplate.getForEntity("/tickets/" + id, TicketResponse.class);
        assertThat(restored.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void exportCsv() {
        createTicket("Export Me");
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/tickets/export?projectId=" + projectId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Export Me");
        assertThat(response.getBody()).contains("title");
    }

    @Test
    void importCsv() {
        String csv = "title,description,status,priority,type,assigneeId\n" +
                "Imported Ticket,desc,TODO,HIGH,BUG,\n" +
                "Another Ticket,,IN_PROGRESS,LOW,FEATURE,\n";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("projectId", projectId);
        body.add("file", new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() { return "tickets.csv"; }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<ImportResultResponse> response = restTemplate.postForEntity(
                "/tickets/import", request, ImportResultResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCreated()).isEqualTo(2);
        assertThat(response.getBody().getFailed()).isEqualTo(0);
    }
}
