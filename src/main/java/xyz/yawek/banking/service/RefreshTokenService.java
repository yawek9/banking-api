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

package xyz.yawek.banking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.yawek.banking.model.RefreshToken;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.repository.RefreshTokenRepository;
import xyz.yawek.banking.security.JWTTokenProvider;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JWTTokenProvider tokenProvider;
    private final RefreshTokenRepository repository;

    @Value("${app.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public Optional<RefreshToken> validateTokenAndIssueNew(
            String email, String token) {
        if (tokenProvider.validateToken(token)) {
            Optional<RefreshToken> tokenOptional =
                    repository.findByToken(token);
            if (tokenOptional.isPresent()
                    && tokenOptional.get().getUser().getEmail().equals(email))
                return Optional.of(createNewToken(tokenOptional.get().getUser()));
        }
        return Optional.empty();
    }

    public RefreshToken createNewToken(User user) {
        repository.findByUser(user).ifPresent(repository::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiration(Instant.ofEpochMilli(
                System.currentTimeMillis() + refreshTokenExpiration * 1000));
        refreshToken.setToken(
                tokenProvider.createRefreshToken(user.getEmail()));
        repository.save(refreshToken);
        return refreshToken;
    }

}
