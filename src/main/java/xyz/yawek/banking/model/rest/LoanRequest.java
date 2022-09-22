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

package xyz.yawek.banking.model.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.yawek.banking.validation.LoanAmount;

import javax.validation.constraints.NotNull;

@Data
public class LoanRequest {

    @NotNull
    @LoanAmount
    @Schema(description =
            "Must be divisible by 500, " +
            "greater or equal to 500, " +
            "lest or equal than 1000000")
    private long amount;

}
