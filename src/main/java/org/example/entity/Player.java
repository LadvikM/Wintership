package org.example.entity;

import lombok.Data;

import java.util.Objects;

@Data
public class Player {
    private final String id;
    private long balance;
    private int totalBets;
    private int wonBets;
    private String illegalOperation;
    Host host = new Host();


    public void deposit(int depositAmount) {
        System.out.println("Account deposited by " + depositAmount);
        // Increase balance
        balance += depositAmount;
    }

    public void withdraw(int withdrawAmount) {
        //Check that balance is not smaller than withdraw amount.
        // If true - make illegal operation.
        // If false - decrease balance
        if (withdrawAmount > balance) {
            illegalOperation = getId() + " WITHDRAW null " + withdrawAmount + " null";
            System.out.println("Illegal withdrawal!!!" + illegalOperation);
            return;
        }
        balance -= withdrawAmount;
        System.out.println("Account withdraws by " + withdrawAmount);
    }

    public long makeBet(Match match, int transactionValue, String betSide) {

        long hostBalanceChange = 0;

        if (transactionValue > balance) {
            illegalOperation = getId() + " BET " + getId() + " " + transactionValue + " " + betSide;
            System.out.println("Illegal BET!!!!" + illegalOperation);
            return hostBalanceChange;
        }
        totalBets++;
        String matchResult = match.getResult();
        double rateA = match.getRateA();
        double rateB = match.getRateB();

        //TODO Change host balance

        // Won
        if (Objects.equals(matchResult, betSide)) {
            wonBets++;
            if (betSide.equals("A")) {
                System.out.println("Balance before: " + balance);
                double amountChange = transactionValue*rateA;
                //Make double to long, truncate decimal part
                long amountWon = (long) amountChange;
                hostBalanceChange = -amountWon;
                balance += amountWon;
                System.out.println("Amount Won A: " + amountWon);
                System.out.println("Balance increaseA:" + id + " "+ balance);


            } else if (betSide.equals("B")) {
                System.out.println("Balance before: " + balance);
                double amountChange = transactionValue*rateB;
                long amountWon = (long) amountChange;
                balance += amountWon;
                System.out.println("Amount Won B: " + amountWon);
                System.out.println("Balance increase B: " + id + " "+ balance);
                hostBalanceChange = -amountWon;
            }

            // Lost
        } else if (!Objects.equals(matchResult, betSide) && !Objects.equals(matchResult, "DRAW")) {
            System.out.println("Amount lost: " + transactionValue );
            balance -= transactionValue;
            System.out.println("Balance decrease: " + id + " "+ balance);
            hostBalanceChange = transactionValue;
            // Draw
        }
        return hostBalanceChange;

    }
}
