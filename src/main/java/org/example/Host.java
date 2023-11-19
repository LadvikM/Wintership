package org.example;

import lombok.Data;

@Data
public class Host {
    private long balance;

    public void updateBalance(long amount) {
        balance += amount;
    }
}
