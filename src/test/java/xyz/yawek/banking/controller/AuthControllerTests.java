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

package xyz.yawek.banking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;
import xyz.yawek.banking.BaseTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Order(1)
class AuthControllerTests extends BaseTest {

	@Test
	void testRegistration() throws Exception {
		ObjectNode json = jsonMapper.createObjectNode();
		json.put("email", "register@example.com");
		json.put("password", "password");

		// Test registration
		this.testJsonRequest(HttpMethod.POST, "/auth/register",
				null, json.toString(), status().is(200));

		assert userRepository.findByEmail("register@example.com").isPresent();

		// Test registration conflict
		this.testJsonRequest(HttpMethod.POST, "/auth/register",
				null, json.toString(), status().is(409));
	}

	@Test
	void testLogin() throws Exception {
		ObjectNode json = jsonMapper.createObjectNode();
		json.put("email", "example@example.com");
		json.put("password", "password");

		// Test correct payload
		this.testJsonRequest(HttpMethod.POST, "/auth/login",
				null, json.toString(),
				status().is(200),
				content().contentType(MediaType.APPLICATION_JSON),
				jsonPath("$.accessToken").isNotEmpty(),
				jsonPath("$.refreshToken").isNotEmpty(),
				jsonPath("$.email").isNotEmpty());

		// Test incorrect payloads

		json.put("password", "incorrect");
		this.testJsonRequest(HttpMethod.POST, "/auth/login",
				null, json.toString(),
				status().is(401));

		json.put("email", "example3@example.com");
		this.testJsonRequest(HttpMethod.POST, "/auth/login",
				null, json.toString(),
				status().is(404));
	}

	@Test
	void testRefreshToken() throws Exception {
		ObjectNode loginJson = jsonMapper.createObjectNode();
		loginJson.put("email", "example@example.com");
		loginJson.put("password", "password");

		MvcResult loginResult =
				this.testJsonRequest(HttpMethod.POST, "/auth/login",
				null, loginJson.toString(), status().is(200));
		JsonNode loginResponseNode = jsonMapper
				.readTree(loginResult.getResponse().getContentAsString());
		String email = loginResponseNode.get("email").textValue();
		String refreshToken = loginResponseNode.get("refreshToken").textValue();

		synchronized (this) {
			wait(1000);
		}

		ObjectNode refreshTokenJson = jsonMapper.createObjectNode();
		refreshTokenJson.put("email", email);
		refreshTokenJson.put("refreshToken", refreshToken);

		MvcResult refreshTokenResult =
				this.testJsonRequest(HttpMethod.POST, "/auth/refresh-token",
						null, refreshTokenJson.toString(), status().is(200));
		String newRefreshToken = new JsonMapper()
				.readTree(refreshTokenResult.getResponse().getContentAsString())
				.get("refreshToken").textValue();

		assert !refreshToken.equals(newRefreshToken);
	}

}
