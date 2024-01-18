package bg.sofia.uni.fmi.mjt.football;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class FootballPlayerAnalyzer {

    private final List<Player> players;

    public FootballPlayerAnalyzer(Reader reader) {
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            players = new ArrayList<>();

            boolean firstRow = true;
            while ((line = br.readLine()) != null) {
                if (firstRow) {
                    firstRow = false;
                } else {
                    players.add(Player.of(line));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Player> getAllPlayers() {
        if (players.isEmpty()) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(players);
    }

    public Set<String> getAllNationalities() {
        Set<String> nationalities = new HashSet<>();
        for (Player p : players) {
            nationalities.add(p.nationality());
        }
        return Collections.unmodifiableSet(nationalities);
    }

    public Player getHighestPaidPlayerByNationality(String nationality) {
        if (nationality == null) {
            throw new IllegalArgumentException();
        }
        Player highestPaid = null;

        for (Player p : players) {
            if (nationality.equals(p.nationality()) && (highestPaid == null || highestPaid.wageEuro() < p.wageEuro())) {
                highestPaid = p;
            }
        }

        if (highestPaid == null) {
            throw new NoSuchElementException();
        }
        return highestPaid;
    }

    public Map<Position, Set<Player>> groupByPosition() {
        Map<Position, Set<Player>> group = new HashMap<>();

        for (Position position : Position.values()) {
            Set<Player> playerSet = new HashSet<>();
            for (Player player : players) {
                for (Position playerPosition : player.positions()) {
                    if (position.equals(playerPosition)) {
                        playerSet.add(player);
                    }
                }
            }
            if (!playerSet.isEmpty()) {
                group.put(position, playerSet);
            }
        }

        return group;
    }

    public Optional<Player> getTopProspectPlayerForPositionInBudget(Position position, long budget) {
        if (position == null || budget < 0) {
            throw new IllegalArgumentException();
        }
        Optional<Player> highestProspectPlayer = Optional.empty();
        double highestProspect = Double.MIN_VALUE;

        for (Player player : players) {
            double currentProspect = (double) (player.overallRating() + player.potential()) / player.age();
            if (highestProspect < currentProspect &&
                    budget > player.valueEuro() && player.positions().contains(position)) {
                highestProspect = currentProspect;
                highestProspectPlayer = Optional.of(player);
            }
        }
        return highestProspectPlayer;
    }

    public Set<Player> getSimilarPlayers(Player player) {
        if (player == null) {
            throw new IllegalArgumentException();
        }
        Set<Player> similarPlayers = new HashSet<>();
        final int maxOverall = 3;
        for (Player player2 : players) {
            boolean similarPos = false;
            for (Position position : Position.values()) {
                if (player.positions().contains(position) && player2.positions().contains(position)) {
                    similarPos = true;
                    break;
                }
            }
            if (similarPos && player.preferredFoot().equals(player2.preferredFoot()) &&
                    Math.abs(player.overallRating() - player2.overallRating()) <= maxOverall) {
                similarPlayers.add(player2);
            }
        }
        return Collections.unmodifiableSet(similarPlayers);
    }

    public Set<Player> getPlayersByFullNameKeyword(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException();
        }
        Set<Player> playersByFullName = new HashSet<>();
        for (Player p : players) {
            if (p.fullName().contains(keyword)) {
                playersByFullName.add(p);
            }
        }
        return Collections.unmodifiableSet(playersByFullName);
    }

}