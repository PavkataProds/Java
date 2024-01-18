package bg.sofia.uni.fmi.mjt.gym.member;

import bg.sofia.uni.fmi.mjt.gym.workout.Exercise;
import bg.sofia.uni.fmi.mjt.gym.workout.Workout;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Member implements GymMember {
    private final String name;
    private final int age;
    private final String personalIdNumber;
    private final Gender gender;
    private final Address address;
    private final Map<DayOfWeek, Workout> trainingProgram;

    public Member(Address address, String name, int age, String personalIdNumber, Gender gender) {
        this.name = name;
        this.age = age;
        this.personalIdNumber = personalIdNumber;
        this.gender = gender;
        this.address = address;

        trainingProgram = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public String getPersonalIdNumber() {
        return personalIdNumber;
    }

    @Override
    public Gender getGender() {
        return gender;
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public Map<DayOfWeek, Workout> getTrainingProgram() {
        return trainingProgram;
    }

    @Override
    public void setWorkout(DayOfWeek day, Workout workout) {
        if (day == null || workout == null) {
            throw new IllegalArgumentException();
        }
        trainingProgram.put(day, workout);
    }

    @Override
    public Collection<DayOfWeek> getDaysFinishingWith(String exerciseName) {
        if (exerciseName == null || exerciseName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Collection<DayOfWeek> result = new ArrayList<DayOfWeek>();
        EnumSet<DayOfWeek> dow = EnumSet.allOf(DayOfWeek.class);
        for (DayOfWeek d : dow) {
            if (trainingProgram.get(d).getLastElement().name().equals(exerciseName)) {
                result.add(d);
            }
        }
        return result;
    }

    @Override
    public void addExercise(DayOfWeek day, Exercise exercise) throws DayOffException {
        if (day == null || exercise == null) {
            throw new IllegalArgumentException();
        }
        if (trainingProgram.get(day) == null) throw new DayOffException();

        trainingProgram.get(day).addElement(exercise);
    }

    @Override
    public void addExercises(DayOfWeek day, List<Exercise> exercises) throws DayOffException {
        for (Exercise e : exercises) {
            addExercise(day, e);
        }
    }
}