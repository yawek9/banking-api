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

package xyz.yawek.banking.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JWTTokenProvider {

    @Value("${app.token.secret}")
    private String secret;

    @Value("${app.token.expiration}")
    private long expiration;

    public String createToken(String username) {
        return JWT.create()
                .withIssuer(username)
                .withIssuedAt(Instant.ofEpochMilli(System.currentTimeMillis()))
                .withExpiresAt(Instant.ofEpochMilli(System.currentTimeMillis() + expiration * 1000))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * Validates provided token
     * @param token encrypted token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException ignored) { }
        return false;
    }

    public String getUsername(String token) {
        return JWT.decode(token).getIssuer();
    }

}
