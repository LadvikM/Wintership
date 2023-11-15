package org.example;

import org.example.entity.Host;
import org.example.entity.Match;
import org.example.entity.Player;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class BetProcessor {
    private static final String PLAYER_DATA = "src/main/resources/test_player_data.txt";
    private static final String MATCH_DATA = "src/main/resources/test_match_data.txt";
    private static final String RESULT = "result.txt";

    HashMap<String, Player> players = new HashMap<>();
    HashMap<String, Match> matches = new HashMap<>();
    Host host = new Host();

    public void processData() throws IOException {
        readMatchData();
        processPlayerData();
        writeResults();

    }
    // TODO find out why printing 2 empty lines after illegitimate operation
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
        for (Player player : players.values()) {
            if (player.getIllegalOperation() == null) {
                BigDecimal betsWon = new BigDecimal(player.getWonBets());
                BigDecimal totalBetsWon = new BigDecimal(player.getTotalBets());
                BigDecimal winrate = betsWon.divide(totalBetsWon, 2, RoundingMode.HALF_UP);
                //TODO Order by player ID
                writer.println(player.getId() + " " + player.getBalance() + " " + winrate);
            } else writer.println();
        }
    }

    private void writeIllegitimateOperation(PrintWriter writer) {
        for (Player player : players.values()) {
            if (player.getIllegalOperation() != null) {
                //TODO Order by player id
                writer.println(player.getIllegalOperation());
            } else writer.println();
        }
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
                        long hostBalanceChange = players.get(playerId).makeBet(match, transactionValue, betSide);
                        changeHostBalance(hostBalanceChange);
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
    }

    private void changeHostBalance(long hostBalanceChange) {
        host.updateBalance(hostBalanceChange);

    }
}
