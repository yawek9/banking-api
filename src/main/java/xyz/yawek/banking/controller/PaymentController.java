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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.yawek.banking.model.Payment;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.model.rest.PaymentRequest;
import xyz.yawek.banking.service.PaymentService;
import xyz.yawek.banking.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/payment", produces = {"application/json"})
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;
    private final PaymentService paymentService;

    @SuppressWarnings("unused")
    @Operation(summary = "Send payment")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment sent",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Not enough balance",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User did not send token or sent token is expired",
                    content = @Content
            )
    })
    @PostMapping("/pay")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> pay(@RequestBody @Valid PaymentRequest payment) {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        User sender = userService.loadByEmail(auth.getName());
        User receiver = userService.loadByEmail(payment.getReceiverEmail());
        paymentService.makePayment(sender, receiver, payment.getAmount());
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unused")
    @Operation(summary = "Get user's received and sent payments")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments data",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User did not send token or sent token is expired",
                    content = @Content
            )
    })
    @GetMapping("/payments")
    @PreAuthorize("hasAuthority('USER')")
    public Page<Payment> payments(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.loadByEmail(authentication.getName());
        return paymentService.getPageableByUser(pageable, user);
    }

}
