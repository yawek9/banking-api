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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;
import xyz.yawek.banking.BaseTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Order(4)
@DirtiesContext
public class LoanControllerTests extends BaseTest {

    @Test
    void testTakingLoan() throws Exception {
        String token = this.getToken(
                "example@example.com", "password");

        ObjectNode jsonLoanRequest = jsonMapper.createObjectNode();
        jsonLoanRequest.put("amount", 500);

        this.testJsonRequest(HttpMethod.POST, "/loan/take",
                Map.of("authorization", "Bearer " + token),
                jsonLoanRequest.toString(),
                status().is(200));
        assertEquals(500.0, userService.loadByEmail("example@example.com")
                .getBalance().doubleValue());
    }

    @Test
    void testGettingLoans() throws Exception {
        String token = this.getToken(
                "example@example.com", "password");

        ObjectNode jsonLoanRequest = jsonMapper.createObjectNode();
        jsonLoanRequest.put("amount", 500);

        this.testJsonRequest(HttpMethod.POST, "/loan/take",
                Map.of("authorization", "Bearer " + token),
                jsonLoanRequest.toString(), status().is(200));

        MvcResult loansResult = this.testJsonRequest(
                HttpMethod.GET, "/loan/loans",
                Map.of("authorization", "Bearer " + token),
                null, status().is(200));

        JsonNode loansArray = jsonMapper.readTree(
                loansResult.getResponse().getContentAsString())
                .get("content");
        assertEquals(500.0, loansArray.get(loansArray.size() - 1)
                .get("amount").doubleValue());
    }

}
