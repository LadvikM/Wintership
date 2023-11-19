package org.example;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BetProcessor {
    private static final String PLAYER_DATA = "src/main/resources/player_data.txt";
    private static final String MATCH_DATA = "src/main/resources/match_data.txt";
    private static final String RESULT = "result.txt";


    HashMap<String, Player> players = new HashMap<>();
    HashMap<String, Match> matches = new HashMap<>();
    Host host = new Host();


    public void processData() throws IOException {
        readMatchData();
        processPlayerData();
        writeResults();

    }

    private void readMatchData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(MATCH_DATA));
        String match;

        while ((match = reader.readLine()) != null) {
            String[] matchData = match.split(",");
            String matchId = matchData[0];
            double rateA = Double.parseDouble(matchData[1]);
            double rateB = Double.parseDouble(matchData[2]);
            String result = matchData[3];
            matches.put(matchId, new Match(matchId, rateA, rateB, result));
        }
        reader.close();
    }

    private void processPlayerData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PLAYER_DATA));
        String playerAction;
        while ((playerAction = reader.readLine()) != null) {
            String[] line = playerAction.split(",");
            String playerId = line[0];
            String operation = line[1];
            int transactionValue = Integer.parseInt(line[3]);
            String matchId = null;
            String betSide = null;
            switch (operation) {
                case "BET" -> {
                    matchId = line[2];
                    betSide = line[4];
                    Match match = matches.get(matchId);
                    if (players.get(playerId).getIllegalOperation() == null) {
                        players.get(playerId).makeBet(match, transactionValue, betSide);

                    }
                }

                case "WITHDRAW" -> {
                    players.computeIfAbsent(playerId, Player::new);
                    if (players.get(playerId).getIllegalOperation() == null) {
                        players.get(playerId).withdraw(transactionValue);
                    }
                }

                case "DEPOSIT" -> {
                    players.computeIfAbsent(playerId, Player::new);
                    if (players.get(playerId).getIllegalOperation() == null) {
                        players.get(playerId).deposit(transactionValue);
                    }
                }
            }

        }
        reader.close();
        for (Player player : players.values()) {
            if (player.getIllegalOperation() == null) {
                long amountWon = player.getAmountWon();
                updateHostBalance(-amountWon);
            }
        }

    }


    private void writeResults() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULT))) {
            writeLegitimatePlayer(writer);
            writer.println();
            writeIllegitimateOperation(writer);
            writer.println();
            writer.println(host.getBalance());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLegitimatePlayer(PrintWriter writer) {
        Map<String, Player> sortedPlayers = new TreeMap<>(players);
        boolean legitimatePlayersExist = false;
        for (Player player : sortedPlayers.values()) {
            if (player.getIllegalOperation() == null) {
                BigDecimal betsWon = new BigDecimal(player.getWonBets());
                BigDecimal totalBetsWon = new BigDecimal(player.getTotalBets());
                BigDecimal winrate = betsWon.divide(totalBetsWon, 2, RoundingMode.HALF_UP);
                writer.println(player.getId() + " " + player.getBalance() + " " + winrate);
                legitimatePlayersExist = true;
            }
        }
        if (!legitimatePlayersExist) {
            writer.println();
        }
    }

    private void writeIllegitimateOperation(PrintWriter writer) {
        Map<String, Player> sortedPlayers = new TreeMap<>(players);
        boolean illegitimatePlayersExist = false;
        for (Player player : sortedPlayers.values()) {
            if (player.getIllegalOperation() != null) {
                writer.println(player.getIllegalOperation());
                illegitimatePlayersExist = true;
            }
        }
        if (!illegitimatePlayersExist) {
            writer.println();
        }
    }


    private void updateHostBalance(long hostBalanceChange) {
        host.updateBalance(hostBalanceChange);

    }
}
