package bg.sofia.uni.fmi.mjt.udemy.course;

import bg.sofia.uni.fmi.mjt.udemy.course.duration.ResourceDuration;

public class Resource implements Completable {

    private boolean isCompleted;
    private String name;
    private ResourceDuration duration;

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getCompletionPercentage() {
        if (isCompleted) {
            return 100;
        } else {
            return 0;
        }
    }

    public Resource(String name, ResourceDuration duration) {
        this.isCompleted = false;
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public ResourceDuration getDuration() {
        return duration;
    }

    public void complete() {
        isCompleted = true;
        // TODO: add implementation here
    }

    public boolean equals(Resource resource) {
        return this.name.equals(resource.getName()) && this.duration.equals(resource.getDuration());
    }
}