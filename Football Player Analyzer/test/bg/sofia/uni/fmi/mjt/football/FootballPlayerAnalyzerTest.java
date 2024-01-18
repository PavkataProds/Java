package bg.sofia.uni.fmi.mjt.football;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballPlayerAnalyzerTest {

    private FootballPlayerAnalyzer playerAnalyzer;

    @BeforeEach
    void setUp() {
        String csvData = "Name;FullName;BirthDate;Age;HeightCm;WeightKg;Positions;Nationality;OverallRating;Potential;ValueEuro;WageEuro;PreferredFoot\n" +
                "Messi;Lionel Messi;6/24/1987;34;170.18;72.12;ST,CF,RW;Argentina;93;93;100000000;500000;LEFT\n" +
                "Ronaldo;Cristiano Ronaldo;2/5/1985;36;187.96;83.01;ST,LW;Portugal;92;92;90000000;450000;RIGHT\n" +
                "Neymar;Neymar Jr.;2/5/1992;29;175.26;68.04;LW,CAM;Brazil;91;91;85000000;400000;RIGHT\n";
        Reader reader = new StringReader(csvData);
        playerAnalyzer = new FootballPlayerAnalyzer(reader);
    }

    @Test
    void testGetAllPlayers() {
        List<Player> allPlayers = playerAnalyzer.getAllPlayers();
        assertEquals(3, allPlayers.size());
    }

    @Test
    void testGetAllNationalities() {
        Set<String> allNationalities = playerAnalyzer.getAllNationalities();
        assertEquals(3, allNationalities.size());
        assertTrue(allNationalities.contains("Argentina"));
        assertTrue(allNationalities.contains("Portugal"));
        assertTrue(allNationalities.contains("Brazil"));
    }

    @Test
    void testGetHighestPaidPlayerByNationality() {
        Player highestPaidArgentinian = playerAnalyzer.getHighestPaidPlayerByNationality("Argentina");
        assertEquals("Messi", highestPaidArgentinian.name());

        Player highestPaidPortuguese = playerAnalyzer.getHighestPaidPlayerByNationality("Portugal");
        assertEquals("Ronaldo", highestPaidPortuguese.name());

        Player highestPaidBrazilian = playerAnalyzer.getHighestPaidPlayerByNationality("Brazil");
        assertEquals("Neymar", highestPaidBrazilian.name());
    }

    @Test
    void testGroupByPosition() {
        Map<Position, Set<Player>> groupedPlayers = playerAnalyzer.groupByPosition();
        assertEquals(3, groupedPlayers.size());
        assertTrue(groupedPlayers.containsKey(Position.ST));
        assertTrue(groupedPlayers.containsKey(Position.CF));
        assertTrue(groupedPlayers.containsKey(Position.RW));
    }

    @Test
    void testGetTopProspectPlayerForPositionInBudget() {
        Optional<Player> topProspectStriker = playerAnalyzer.getTopProspectPlayerForPositionInBudget(Position.ST, 100000000);
        assertTrue(topProspectStriker.isPresent());
        assertEquals("Ronaldo", topProspectStriker.get().name());

        Optional<Player> topProspectMidfielder = playerAnalyzer.getTopProspectPlayerForPositionInBudget(Position.CAM, 500000);
        assertTrue(topProspectMidfielder.isPresent());
        assertEquals("Neymar", topProspectMidfielder.get().name());

        Optional<Player> topProspectGoalkeeper = playerAnalyzer.getTopProspectPlayerForPositionInBudget(Position.GK, 100000);
        assertFalse(topProspectGoalkeeper.isPresent());
    }

    @Test
    void testGetSimilarPlayers() {
        Player messi = new Player("Messi", "Lionel Messi", LocalDate.of(1987, 6, 24),
                34, 170.18, 72.12, List.of(Position.ST, Position.CF, Position.RW), "Argentina",
                93, 93, 100000000, 500000, Foot.LEFT);

        Set<Player> similarPlayers = playerAnalyzer.getSimilarPlayers(messi);
        assertEquals(1, similarPlayers.size());
        assertTrue(similarPlayers.contains(messi));

        Player ronaldo = new Player("Ronaldo", "Cristiano Ronaldo", LocalDate.of(1985, 2, 5),
                36, 187.96, 83.01, List.of(Position.ST, Position.LW), "Portugal",
                92, 92, 90000000, 450000, Foot.RIGHT);

        similarPlayers = playerAnalyzer.getSimilarPlayers(ronaldo);
        assertEquals(0, similarPlayers.size());
    }

    @Test
    void testGetPlayersByFullNameKeyword() {
        Set<Player> playersByFullName = playerAnalyzer.getPlayersByFullNameKeyword("Messi");
        assertEquals(1, playersByFullName.size());
        assertEquals("Messi", playersByFullName.iterator().next().name());

        playersByFullName = playerAnalyzer.getPlayersByFullNameKeyword("Cristiano");
        assertEquals(1, playersByFullName.size());
        assertEquals("Ronaldo", playersByFullName.iterator().next().name());

        playersByFullName = playerAnalyzer.getPlayersByFullNameKeyword("Neymar");
        assertEquals(1, playersByFullName.size());
        assertEquals("Neymar", playersByFullName.iterator().next().name());
    }
}