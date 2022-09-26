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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import xyz.yawek.banking.model.rest.UserRequest;
import xyz.yawek.banking.service.RefreshTokenService;
import xyz.yawek.banking.service.TokenService;
import xyz.yawek.banking.service.UserService;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "/auth", produces = {"application/json"})
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @SuppressWarnings("unused")
    @Operation(summary = "Register a user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered",
                    content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "Wrong credentials format",
                    content = @Content),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already taken",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRequest userRequest) {
        User user = userService.buildFromRequest(userRequest);
        user.setRoles(Set.of("USER"));
        userService.registerUser(user);
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unused")
    @Operation(summary = "Login a user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User logged",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Wrong credentials format", content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found", content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Wrong credentials", content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest userRequest) {
        try {
            User user = userService.loadByEmail(userRequest.getEmail());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(),
                            userRequest.getPassword()));
            return ResponseEntity.ok(new TokenResponse(
                    tokenService.createToken(user),
                    refreshTokenService.createNewToken(user).getToken(),
                    user.getEmail()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @SuppressWarnings("unused")
    @Operation(summary = "Create new tokens using current refresh token")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens refreshed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Bad/expired token", content = @Content)
    })
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
