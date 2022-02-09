package me.jackson.restapi.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jackson.restapi.common.RestDocsConfiguration;
import me.jackson.restapi.common.TestDescription;
import me.jackson.restapi.events.Event;
import me.jackson.restapi.events.EventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc // Spring Boot Test 에서 MockMvc를 계속 사용하기 위한 어노테이션
@AutoConfigureRestDocs // REST docs를 만들어주는 어노테이션 (boot)
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class IndexControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

    @Test
    public void index() throws Exception {

        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.events").exists());

    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generateEvent);

        // When
        mockMvc.perform(get("/api/evnets")
                .param("page","1")
                .param("size", "10")
                .param("sort", "name,DESC")
            )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
        ;
    }

    public void generateEvent(int index) {
        Event event = Event.builder()
                .name("event " + index)
                .description("test event")
                .build();

        eventRepository.save(event);
    }
}
