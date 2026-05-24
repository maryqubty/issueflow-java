package com.att.tdp.issueflow;

import com.att.tdp.issueflow.dto.request.CreateUserRequest;
import com.att.tdp.issueflow.dto.request.UpdateUserRequest;
import com.att.tdp.issueflow.dto.response.UserResponse;
import com.att.tdp.issueflow.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private CreateUserRequest buildRequest(String suffix) {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("user_" + suffix);
        req.setEmail("user_" + suffix + "@example.com");
        req.setFullName("User " + suffix);
        req.setRole(Role.DEVELOPER);
        req.setPassword("secret");
        return req;
    }

    @Test
    void createAndGetUser() {
        CreateUserRequest req = buildRequest("cg1");
        ResponseEntity<UserResponse> created = restTemplate.postForEntity("/users", req, UserResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getUsername()).isEqualTo("user_cg1");

        Long id = created.getBody().getId();
        ResponseEntity<UserResponse> fetched = restTemplate.getForEntity("/users/" + id, UserResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().getEmail()).isEqualTo("user_cg1@example.com");
    }

    @Test
    void getAll() {
        restTemplate.postForEntity("/users", buildRequest("all1"), UserResponse.class);
        ResponseEntity<UserResponse[]> all = restTemplate.getForEntity("/users", UserResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotEmpty();
    }

    @Test
    void updateUser() {
        ResponseEntity<UserResponse> created = restTemplate.postForEntity("/users", buildRequest("upd1"), UserResponse.class);
        Long id = created.getBody().getId();

        UpdateUserRequest upd = new UpdateUserRequest();
        upd.setFullName("Updated Name");
        upd.setRole(Role.ADMIN);
        ResponseEntity<Void> updated = restTemplate.postForEntity("/users/update/" + id, upd, Void.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<UserResponse> fetched = restTemplate.getForEntity("/users/" + id, UserResponse.class);
        assertThat(fetched.getBody().getFullName()).isEqualTo("Updated Name");
        assertThat(fetched.getBody().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deleteUser() {
        ResponseEntity<UserResponse> created = restTemplate.postForEntity("/users", buildRequest("del1"), UserResponse.class);
        Long id = created.getBody().getId();

        restTemplate.delete("/users/" + id);
        ResponseEntity<String> fetched = restTemplate.getForEntity("/users/" + id, String.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void notFoundReturns404() {
        ResponseEntity<String> res = restTemplate.getForEntity("/users/999999", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateUsernameReturns409() {
        restTemplate.postForEntity("/users", buildRequest("dup1"), UserResponse.class);
        ResponseEntity<String> second = restTemplate.postForEntity("/users", buildRequest("dup1"), String.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
