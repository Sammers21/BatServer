package service.controllers;

import org.apache.log4j.BasicConfigurator;
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
import service.entity.Token;
import service.entity.User;
import service.repository.TokenRepository;
import service.repository.UserRepository;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LogoutControllerTest {

    private static final Logger log = Logger.getLogger(LogoutControllerTest.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private MockMvc mvc;

    @Before
    public void clean() {

        BasicConfigurator.configure();

        userRepository.deleteAll();
        tokenRepository.deleteAll();

        User user = new User("pavel", "bestprogrammer");
        Token token = new Token("bestToken", "pavel");

        userRepository.save(user);
        tokenRepository.save(token);

    }


    @Test
    public void succesfullLogout() throws Exception {


        mvc.perform(get("/bestToken/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out")));



    }
    @Test
    public void unSuccesfullLogout() throws Exception {


        mvc.perform(get("/worstToken/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Invalid auth_token")));
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