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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;
import xyz.yawek.banking.BaseTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Order(2)
public class UserControllerTests extends BaseTest {

    @Test
    void testGettingBalance() throws Exception {
        String token = this.getToken(
                "example@example.com", "password");
        MvcResult balanceResult = testJsonRequest(
                HttpMethod.GET, "/user/balance",
                Map.of("authorization", "Bearer " + token),
                null, status().is(200));
        assertEquals(0.0, Double.parseDouble(
                balanceResult.getResponse().getContentAsString()));
    }

}
