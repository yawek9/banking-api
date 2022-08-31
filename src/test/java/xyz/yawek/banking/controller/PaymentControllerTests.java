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

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Order(3)
public class PaymentControllerTests extends BaseTest {

    @Test
    void testPay() throws Exception {
        String token = this.getToken(
                "example@example.com", "password");

        userRepository.findByEmail("example@example.com")
                .ifPresent(user -> {
                    user.setBalance(new BigDecimal("1.00"));
                    userRepository.save(user);
                });

        // Test with correct payload
        ObjectNode jsonPayment = jsonMapper.createObjectNode();
        jsonPayment.put("receiver", "example2@example.com");
        jsonPayment.put("amount", "1.00");

        this.testJsonRequest(HttpMethod.POST, "/payment/pay",
                Map.of("authorization", "Bearer " + token),
                jsonPayment.toString(),
                status().is(200));

        // Test when balance is not enough
        this.testJsonRequest(HttpMethod.POST, "/payment/pay",
                Map.of("authorization", "Bearer " + token),
                jsonPayment.toString(),
                status().is(400));
    }

    @Test
    void testPayments() throws Exception {
        String token = this.getToken(
                "example@example.com", "password");

        userRepository.findByEmail("example@example.com")
                .ifPresent(user -> {
                    user.setBalance(new BigDecimal("1.00"));
                    userRepository.save(user);
                });

        ObjectNode jsonPayment = jsonMapper.createObjectNode();
        jsonPayment.put("receiver", "example2@example.com");
        jsonPayment.put("amount", "1.00");

        this.testJsonRequest(HttpMethod.POST, "/payment/pay",
                Map.of("authorization", "Bearer " + token),
                jsonPayment.toString(), status().is(200));

        MvcResult paymentsResult = this.testJsonRequest(
                HttpMethod.GET, "/payment/payments",
                Map.of("authorization", "Bearer " + token),
                null, status().is(200));

        JsonNode paymentsArray = jsonMapper.readTree(
                paymentsResult.getResponse().getContentAsString());
        assert paymentsArray.get("content").get(0).get("amount")
                .doubleValue() == 1.0;
    }

}
