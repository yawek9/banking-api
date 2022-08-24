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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import xyz.yawek.banking.BaseTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
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
				jsonPath("$.token").isNotEmpty(),
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

}
