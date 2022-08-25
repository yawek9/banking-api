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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.yawek.banking.model.RefreshToken;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.model.rest.RefreshTokenRequest;
import xyz.yawek.banking.model.rest.TokenResponse;
import xyz.yawek.banking.service.RefreshTokenService;
import xyz.yawek.banking.service.TokenService;
import xyz.yawek.banking.service.UserService;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @SuppressWarnings("unused")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user) {
        user.setRoles(Set.of("USER"));
        userService.registerUser(user);
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unused")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid User user) {
        try {
            User persistentUser = userService.loadByEmail(user.getEmail());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            return ResponseEntity.ok(new TokenResponse(
                    tokenService.createToken(persistentUser),
                    refreshTokenService.createNewToken(persistentUser).getToken(),
                    user.getEmail()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @SuppressWarnings("unused")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        Optional<RefreshToken> tokenOptional = refreshTokenService.validateTokenAndIssueNew(
                refreshTokenRequest.getEmail(), refreshTokenRequest.getRefreshToken());
        if (tokenOptional.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        User user = userService.loadByEmail(refreshTokenRequest.getEmail());
        TokenResponse tokenResponse = new TokenResponse(
                tokenService.createToken(user),
                tokenOptional.get().getToken(),
                refreshTokenRequest.getEmail());
        return ResponseEntity.ok(tokenResponse);
    }

}