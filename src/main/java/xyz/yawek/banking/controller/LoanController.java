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
import xyz.yawek.banking.model.Loan;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.model.rest.LoanRequest;
import xyz.yawek.banking.service.LoanService;
import xyz.yawek.banking.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanController {

    private final UserService userService;
    private final LoanService loanService;

    @SuppressWarnings("unused")
    @PostMapping("/take")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> takeLoan(@RequestBody @Valid LoanRequest loanRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.loadByEmail(authentication.getName());
        loanService.createLoan(loanService.buildFromUserRequest(user, loanRequest));

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unused")
    @GetMapping("/loans")
    @PreAuthorize("hasAuthority('USER')")
    public Page<Loan> loans(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.loadByEmail(authentication.getName());
        return loanService.getLoansByUser(user, pageable);
    }

}
