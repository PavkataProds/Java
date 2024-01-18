package bg.sofia.uni.fmi.mjt.udemy.course;

import bg.sofia.uni.fmi.mjt.udemy.course.duration.CourseDuration;
import bg.sofia.uni.fmi.mjt.udemy.exception.ResourceNotFoundException;

public class Course implements Purchasable, Completable {

    private boolean isPurchased;
    private String name;
    private String description;
    private double price;
    private Resource[] content;
    private Category category;
    private CourseDuration totalTime;

    public Course(String name, String description, double price, Resource[] content, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.content = content;
        this.category = category;
        //this.totalTime = CourseDuration.(content);
        isPurchased = false;

    }

    public void purchase() {
        isPurchased = true;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public boolean isCompleted() {
        int length = content.length;
        for (int i = 0; i < length; i++) {
            if (!content[i].isCompleted()) {
                return false;
            }
        }
        return true;
    }

    public int getCompletionPercentage() {
        int length = content.length;
        double result = 0;
        for (int i = 0; i < length; i++) {
            if (content[i].isCompleted()) {
                result++;
            }
        }
        if (result == 0) {
            return 0;
        }
        return (int) Math.round((result * 100) / length);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public Resource[] getContent() {
        return content;
    }

    public CourseDuration getTotalTime() {
        return totalTime;
    }

    public void completeResource(Resource resourceToComplete) throws ResourceNotFoundException {
        int length = content.length;
        for (int i = 0; i < length; i++) {
            if (content[i].equals(resourceToComplete)) {
                content[i].complete();
                resourceToComplete.complete();
                return;
            }
        }
        throw new ResourceNotFoundException();
    }
}