package org.example.aaa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class BettingProcessor {
    private static final String PLAYER_DATA_FILE = "src/main/resources/player_data.txt";
    private static final String MATCH_DATA_FILE = "src/main/resources/match_data.txt";
    private static final String RESULT_FILE = "result.txt";

    private Map<String, Player> players = new HashMap<>();
    private static Map<String, Match> matches = new HashMap<>();
    private Host host = new Host();

    public static void main(String[] args) {
        BettingProcessor processor = new BettingProcessor();
        processor.processData();
        processor.writeResults();
    }

    private void processData() {
        try {
            readPlayerData();
            readMatchData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readPlayerData() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PLAYER_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String playerId = data[0];
                String operation = data[1];
                switch (operation) {
                    case "DEPOSIT":
                        int depositAmount = Integer.parseInt(data[3]);
                        players.computeIfAbsent(playerId, Player::new).deposit(depositAmount);
                        break;
                    case "BET":
                        String matchId = data[2];
                        int betAmount = Integer.parseInt(data[3]);
                        String side = data[4];
                        players.get(playerId).placeBet(matchId, betAmount, side);
                        break;
                    case "WITHDRAW":
                        int withdrawAmount = Integer.parseInt(data[3]);
                        players.get(playerId).withdraw(withdrawAmount);
                        break;
                }
            }
        }
    }

    private void readMatchData() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(MATCH_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String matchId = data[0];
                double rateA = Double.parseDouble(data[1]);
                double rateB = Double.parseDouble(data[2]);
                String result = data[3];
                matches.put(matchId, new Match(rateA, rateB, result));
            }
        }
    }

    private void writeResults() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULT_FILE))) {
            writeLegitimatePlayers(writer);
            writer.println();
            writeIllegitimatePlayers(writer);
            writer.println();
            writeHostBalance(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLegitimatePlayers(PrintWriter writer) {
        for (Player player : players.values()) {
            if (player.isLegitimate()) {
                writer.println(player.getId() + " " + player.getBalance() + " " + player.getWinRate());
            }
        }
    }

    private void writeIllegitimatePlayers(PrintWriter writer) {
        for (Player player : players.values()) {
            if (!player.isLegitimate()) {
                writer.println(player.getIllegitimateOperation());
            }
        }
    }

    private void writeHostBalance(PrintWriter writer) {
        writer.println("Host " + host.getBalance());
    }

    private static class Player {
        private final String id;
        private long balance;
        private int totalBets;
        private int wonBets;

        private String illegitimateOperation;

        public Player(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public long getBalance() {
            return balance;
        }

        public BigDecimal getWinRate() {
            if (totalBets == 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(wonBets)
                    .divide(BigDecimal.valueOf(totalBets), RoundingMode.CEILING);
        }

        public boolean isLegitimate() {
            return illegitimateOperation == null;
        }

        public String getIllegitimateOperation() {
            return illegitimateOperation;
        }

        public void deposit(int amount) {
            balance += amount;
        }

        public void placeBet(String matchId, int betAmount, String side) {
            if (balance < betAmount) {
                illegitimateOperation = getId() + " BET " + matchId + " " + betAmount + " " + side;
                return;
            }

            Match match = matches.get(matchId);
            if (match == null) {
                illegitimateOperation = getId() + " BET " + matchId + " " + betAmount + " " + side;
                return;
            }

            totalBets++;
            if ((side.equals("A") && match.getResult().equals("A")) ||
                    (side.equals("B") && match.getResult().equals("B"))) {
                balance += betAmount * match.getRateA();
                wonBets++;
            } else if (side.equals("A") && match.getResult().equals("B") ||
                    side.equals("B") && match.getResult().equals("A")) {
                balance -= betAmount;
            }
        }

        public void withdraw(int amount) {
            if (balance < amount) {
                illegitimateOperation = getId() + " WITHDRAW null " + amount + " null";
                return;
            }
            balance -= amount;
        }
    }

    private static class Match {
        private final double rateA;
        private final double rateB;
        private final String result;

        public Match(double rateA, double rateB, String result) {
            this.rateA = rateA;
            this.rateB = rateB;
            this.result = result;
        }

        public double getRateA() {
            return rateA;
        }

        public String getResult() {
            return result;
        }
    }

    private static class Host {
        private long balance;

        public long getBalance() {
            return balance;
        }

        public void updateBalance(long amount) {
            balance += amount;
        }
    }
}

