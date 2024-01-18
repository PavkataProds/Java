package bg.sofia.uni.fmi.mjt.udemy.course.duration;

import bg.sofia.uni.fmi.mjt.udemy.course.Resource;

public record CourseDuration(int hours, int minutes) {
    public CourseDuration {
        if (!(hours >= 0 && hours <= 24) || !(minutes >= 0 && minutes <= 60)) {
            throw new IllegalArgumentException("Hours must be between 0 and 24, minutes must be between 0 and 60");
        }
    }

    public boolean less(CourseDuration other) {
        int thisTotalMinutes = this.hours() * 60 + this.minutes();
        int otherTotalMinutes = other.hours() * 60 + other.minutes();
        return thisTotalMinutes < otherTotalMinutes;
    }
}