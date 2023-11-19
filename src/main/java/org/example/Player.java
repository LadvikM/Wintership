package org.example;

import lombok.Data;
import org.example.Host;
import org.example.Match;

import java.util.Objects;

@Data
public class Player {
    private final String id;
    Host host = new Host();
    private long balance;
    private int totalBets;
    private int wonBets;
    private String illegalOperation;
    private long amountWon;

    public void deposit(int depositAmount) {
        balance += depositAmount;
    }

    public void withdraw(int withdrawAmount) {
        if (withdrawAmount > balance) {
            illegalOperation = getId() + " WITHDRAW null " + withdrawAmount + " null";
            return;
        }
        balance -= withdrawAmount;
    }

    public void makeBet(Match match, int transactionValue, String betSide) {
        if (transactionValue > balance) {
            illegalOperation = getId() + " BET " + getId() + " " + transactionValue + " " + betSide;
        }
        totalBets++;
        String matchResult = match.getResult();
        double rateA = match.getRateA();
        double rateB = match.getRateB();
        // Won
        if (Objects.equals(matchResult, betSide)) {
            wonBets++;
            // Won
            if (betSide.equals("A")) {
                double amountChange = transactionValue * rateA;
                //Make double to long, truncate decimal part
                amountWon += (long) amountChange;
                balance += amountWon;
            } else if (betSide.equals("B")) {
                double amountChange = transactionValue * rateB;
                amountWon += (long) amountChange;
                balance += amountWon;
            }
            // Lost
        } else if (!Objects.equals(matchResult, betSide) && !Objects.equals(matchResult, "DRAW")) {
            balance -= transactionValue;
            amountWon -= transactionValue;
        }
    }
}
