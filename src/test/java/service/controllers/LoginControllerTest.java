package service.controllers;


import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import service.entity.User;
import service.objects.RegisterUserObject;
import service.repository.UserRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTest {

    private static final Logger log = Logger.getLogger(LoginControllerTest.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mvc;

    private MediaType contentType = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8")
    );

    @Before
    public void clean() {
        userRepository.deleteAll();
    }


    @Test
    public void invalidPassword() throws Exception {

        userRepository.save(new User("someusername", "somepassword"));

        mvc.perform(post("/login")
                .content(this.json(new RegisterUserObject("someusername", "invalidpassword")))
                .contentType(contentType))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").isString());

    }


    @Test
    public void validPassword() throws Exception {

        userRepository.save(new User("someusername", "somepassword"));

        mvc.perform(post("/login")
                .content(this.json(new RegisterUserObject("someusername", "somepassword")))
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());

    }

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters).filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }


    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
