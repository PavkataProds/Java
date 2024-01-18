package bg.sofia.uni.fmi.mjt.football;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record Player(String name, String fullName, LocalDate birthDate, int age, double heightCm,
                     double weightKg, List<Position> positions, String nationality, int overallRating,
                     int potential, long valueEuro, long wageEuro, Foot preferredFoot) {
    public static Player of(String line) {
        Collection<String> stats = List.of(line.split(";"));
        ArrayList<String> statsArray = new ArrayList<>(stats);

        int index = 0;
        String name = statsArray.get(index++);
        String fullName = statsArray.get(index++);
        LocalDate birthDate = LocalDate.parse(statsArray.get(index++), DateTimeFormatter.ofPattern("M/d/yyyy"));
        int age = Integer.parseInt(statsArray.get(index++));
        double heightCm = Double.parseDouble(statsArray.get(index++));
        double weightKg = Double.parseDouble(statsArray.get(index++));

        Collection<String> positionsStr = List.of(statsArray.get(index++).split(","));
        List<Position> positions = new ArrayList<>();
        for (String str : positionsStr) {
            positions.add(Position.valueOf(str.toUpperCase()));
        }

        String nationality = statsArray.get(index++);
        int overallRating = Integer.parseInt(statsArray.get(index++));
        int potential = Integer.parseInt(statsArray.get(index++));
        long valueEuro = Long.parseLong(statsArray.get(index++));
        long wageEuro = Long.parseLong(statsArray.get(index++));
        Foot preferredFoot = Foot.valueOf(statsArray.get(index).toUpperCase());

        return new Player(name, fullName, birthDate, age, heightCm, weightKg, positions,
                nationality, overallRating, potential, valueEuro, wageEuro, preferredFoot);
    }
}