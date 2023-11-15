package org.example.entity;

import lombok.Data;

@Data
public class Host {
    private long balance;

    public void updateBalance(long amount) {
        balance += amount;
        System.out.println("Host Balance: " + balance);
    }
}
