package bg.sofia.uni.fmi.mjt.gym.workout;

import java.util.SequencedCollection;

public record Workout(SequencedCollection<Exercise> exercises) {
    public Exercise getLastElement() {
        return exercises.getLast();
    }

    public void addElement(Exercise exercise) {
        exercises.addLast(exercise);
    }
}