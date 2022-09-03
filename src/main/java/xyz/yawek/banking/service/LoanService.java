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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yawek.banking.exception.LoanLimitException;
import xyz.yawek.banking.model.Loan;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.model.rest.LoanRequest;
import xyz.yawek.banking.repository.LoanRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {

    private final LoanRepository repository;

    @Value("${app.loan-repayment-amount-multiplier}")
    private BigDecimal repaymentAmountMultiplier;

    @Value("${app.loan-repayment-amount-limit}")
    private BigDecimal repaymentAmountLimit;

    public void createLoan(Loan loan) {
        if (loan.getUser().getLoans().stream()
                .map(Loan::getRepaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(loan.getAmount())
                .compareTo(repaymentAmountLimit) > 0)
            throw new LoanLimitException();
        loan.getUser().addBalance(loan.getAmount());
        repository.save(loan);
    }

    public Page<Loan> getLoansByUser(User user, Pageable pageable) {
        return repository.findByUser(user, pageable);
    }

    public Loan buildFromUserRequest(User user, LoanRequest loanRequest) {
        Loan loan = new Loan();
        loan.setAmount(new BigDecimal(loanRequest.getAmount()));
        loan.setRepaymentAmount(
                loan.getAmount().multiply(repaymentAmountMultiplier));
        loan.setRepaymentDate(
                LocalDateTime.ofInstant(ZonedDateTime.now().plusYears(
                        (long) (loan.getAmount().doubleValue() / 500)).toInstant(),
                ZoneId.systemDefault()));
        loan.setUser(user);
        return loan;
    }

}
