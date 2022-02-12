package me.jackson.restapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc // Spring Boot Test 에서 MockMvc를 계속 사용하기 위한 어노테이션
@AutoConfigureRestDocs // REST docs를 만들어주는 어노테이션 (boot)
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test") // test pofile이라고 명시할 경우 기존 어노테이션에서 쓰는 application-test.properties + test.properties로 봐야함
@Ignore// 테스트가 실행되지 않도록!
public class BaseControllerTest {

    @Autowired
    // mockMvc를 단위테스트라고 볼 수는 없다
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelMapper modelMapper;
}
