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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yawek.banking.exception.NotEnoughBalanceException;
import xyz.yawek.banking.model.Payment;
import xyz.yawek.banking.model.User;
import xyz.yawek.banking.repository.PaymentRepository;

import java.math.BigDecimal;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository repository;

    @Autowired
    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public void makePayment(User sender, User receiver, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0)
            throw new NotEnoughBalanceException("Not enough balance");
        sender.takeBalance(amount);
        receiver.addBalance(amount);
        repository.save(new Payment(sender, receiver, amount));
    }

    public Page<Payment> getPageableByUser(Pageable pageable, User user) {
        return repository.findBySenderOrReceiver(user, user, pageable);
    }

}
