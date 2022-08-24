/*
 * This file is part of Banking API, licensed under GNU GPLv3 license.
 * Copyright (C) 2022 yawek9
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.yawek.banking;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.repository.UserRepository;
import xyz.yawek.banking.service.UserService;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;

    protected final JsonMapper jsonMapper =
            JsonMapper.builder()
//					.disable(MapperFeature.USE_ANNOTATIONS)
                    .build();

    @BeforeAll
    void loadExampleUsers() throws Exception {
        registerUser("example@example.com", "password");
        registerUser("example2@example.com", "password");
    }

    private void registerUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRoles(Set.of("USER"));
        userService.registerUser(user);
    }

    protected MvcResult testJsonRequest(
            HttpMethod method, String endpoint,
            Map<String, Object> headers, String content,
            ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                switch (method) {
                    case POST -> post(endpoint);
                    case PUT -> put(endpoint);
                    case PATCH -> patch(endpoint);
                    case DELETE -> delete(endpoint);
                    default -> get(endpoint);
                };
        if (headers != null)
            headers.keySet().forEach(key ->
                    requestBuilder.header(key, headers.get(key)));

        if (content != null)
            requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content);

        return mockMvc.perform(requestBuilder)
                .andExpectAll(resultMatchers)
                .andReturn();
    }

}
