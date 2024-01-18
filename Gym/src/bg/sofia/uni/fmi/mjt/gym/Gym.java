package bg.sofia.uni.fmi.mjt.gym;

import bg.sofia.uni.fmi.mjt.gym.member.Address;
import bg.sofia.uni.fmi.mjt.gym.member.GymMember;
import bg.sofia.uni.fmi.mjt.gym.member.Member;
import bg.sofia.uni.fmi.mjt.gym.workout.Exercise;

import java.time.DayOfWeek;

import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Gym implements GymAPI {
    private final SortedSet<GymMember> members;
    private final int capacity;
    private final Address address;

    public Gym(int capacity, Address address) {
        this.capacity = capacity;
        this.address = address;
        members = new TreeSet<>();
    }

    @Override
    public SortedSet<GymMember> getMembers() {
        return members;
    }

    @Override
    public SortedSet<GymMember> getMembersSortedByName() {
        Comparator c = (o1, o2) -> {
            String m1 = ((Member) o1).getName();
            String m2 = ((Member) o2).getName();
            return m1.compareTo(m2);
        };
        return new TreeSet<GymMember>(c);
    }

    @Override
    public SortedSet<GymMember> getMembersSortedByProximityToGym() {
        Comparator c = (o1, o2) -> {
            double m1 = ((Member) o1).getAddress().getDistanceTo(address);
            double m2 = ((Member) o2).getAddress().getDistanceTo(address);
            return Double.compare(m1, m2);
        };
        return new TreeSet<GymMember>(c);
    }

    @Override
    public void addMember(GymMember member) throws GymCapacityExceededException {
        if (member == null) {
            throw new IllegalArgumentException();
        }
        if (members.size() >= capacity) {
            throw new GymCapacityExceededException();
        }
        members.add(member);
    }

    @Override
    public void addMembers(Collection<GymMember> members) throws GymCapacityExceededException {
        for (GymMember gm : members) {
            addMember(gm);
        }
    }

    @Override
    public boolean isMember(GymMember member) {
        if (member == null) {
            throw new IllegalArgumentException();
        }
        for (GymMember gm : members) {
            if (member.equals(gm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isExerciseTrainedOnDay(String exerciseName, DayOfWeek day) {
        if (exerciseName == null || day == null || exerciseName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (GymMember gm : members) {
            Collection<Exercise> exercises = gm.getTrainingProgram().get(day).exercises();
            for (Exercise e : exercises) {
                if (e.name().equals(exerciseName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Map<DayOfWeek, List<String>> getDailyListOfMembersForExercise(String exerciseName) {
        if (exerciseName == null || exerciseName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Map<DayOfWeek, List<String>> result = new HashMap<>();

        EnumSet<DayOfWeek> dow = EnumSet.allOf(DayOfWeek.class);
        for (DayOfWeek d : dow) {
            List<String> names = new ArrayList<>();
            for (GymMember gm : members) {
                Collection<Exercise> exercises = gm.getTrainingProgram().get(d).exercises();
                for (Exercise e : exercises) {
                    if (e.name().equals(exerciseName)) {
                        names.add(gm.getName());
                    }
                }
            }
            result.put(d, names);
        }
        return result;
    }
}