package com.civicvote.election;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
@WebMvcTest(ElectionController.class)
@Import({SecurityConfig.class,JwtAuthFilter.class,SecurityAutoConfiguration.class,ServletWebSecurityAutoConfiguration.class})
@TestPropertySource(properties="security.jwt.secret=unit-test-secret-that-is-at-least-32-bytes-long")
class ElectionAuthorizationTest {
    @Autowired MockMvc mvc;
    @MockitoBean ElectionRepository elections;
    @Test
    void voterCanReadElectionList()throws Exception{
        when(elections.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/elections").with(user("voter").roles("VOTER"))).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test
    void electionDetailsIncludeSystemNotaWithoutCountingItAsARealCandidate()throws Exception{
        Election election=new Election("Town Council","",Instant.now().plusSeconds(600),Instant.now().plusSeconds(1200));
        election.getCandidates().add(new Candidate(election,"Candidate A","Party A",""));
        when(elections.findById(1L)).thenReturn(java.util.Optional.of(election));
        mvc.perform(get("/api/elections/1").with(user("voter").roles("VOTER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SCHEDULED"))
            .andExpect(jsonPath("$.candidates.length()").value(2))
            .andExpect(jsonPath("$.candidates[1].id").value(-1))
            .andExpect(jsonPath("$.candidates[1].name").value("NOTA (None of the Above)"));
    }
    @Test @SuppressWarnings("null")
    void adminCannotAddAnotherNotaCandidate()throws Exception{
        Election election=new Election("Town Council","",Instant.now().plusSeconds(600),Instant.now().plusSeconds(1200));
        when(elections.findById(1L)).thenReturn(java.util.Optional.of(election));
        mvc.perform(post("/api/elections/1/candidates").with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"None of the Above\",\"party\":\"\",\"manifesto\":\"\"}"))
            .andExpect(status().isBadRequest());
        verify(elections,never()).save(any(Election.class));
    }
    @Test @SuppressWarnings("null")
    void voterCannotCreateElection()throws Exception{
        mvc.perform(post("/api/elections").with(user("voter").roles("VOTER")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Unauthorized\",\"description\":\"\",\"startsAt\":\""+Instant.now().plusSeconds(600)+"\",\"endsAt\":\""+Instant.now().plusSeconds(1200)+"\"}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(elections);
    }
    @Test @SuppressWarnings("null")
    void electionWithInvalidDatesIsRejected()throws Exception{
        mvc.perform(post("/api/elections").with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Invalid dates\",\"description\":\"\",\"startsAt\":\""+Instant.now().plusSeconds(1200)+"\",\"endsAt\":\""+Instant.now().plusSeconds(600)+"\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(elections);
    }
}
