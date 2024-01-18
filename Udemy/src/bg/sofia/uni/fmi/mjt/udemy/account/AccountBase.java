package bg.sofia.uni.fmi.mjt.udemy.account;

import bg.sofia.uni.fmi.mjt.udemy.account.type.AccountType;
import bg.sofia.uni.fmi.mjt.udemy.course.Course;
import bg.sofia.uni.fmi.mjt.udemy.course.Resource;
import bg.sofia.uni.fmi.mjt.udemy.exception.*;

public abstract class AccountBase implements Account {
    protected String username;
    protected AccountType accountType;
    protected double balance;
    protected Course[] courses;
    protected double[] grades;
    protected int courseCount;
    protected final int MAX_COUNT = 100;

    public AccountBase(String username, double balance) {
        this.username = username;
        this.balance = balance;

        this.accountType = AccountType.STANDARD;
        this.courseCount = 0;
        this.courses = new Course[MAX_COUNT];
        this.grades = new double[MAX_COUNT];
    }

    public AccountBase(String username, double balance, AccountType accountType) {
        this.username = username;
        this.balance = balance;

        this.accountType = accountType;
        this.courseCount = 0;
        this.courses = new Course[MAX_COUNT];
        this.grades = new double[MAX_COUNT];
    }

    public String getUsername() {
        return username;
    }

    public void addToBalance(double amount) {
        if (amount < 0) {
            throw new RuntimeException();
        }
        this.balance += amount;
    }

    public double getBalance() {
        return balance;
    }

    public void buyCourse(Course course) throws InsufficientBalanceException, CourseAlreadyPurchasedException, MaxCourseCapacityReachedException {
        //check
        if (courseCount == MAX_COUNT) {
            throw new MaxCourseCapacityReachedException();
        }
        if (course.getPrice() > balance) {
            throw new InsufficientBalanceException();
        }
        for (int i = 0; i < courseCount; i++) {
            if (courses[i].equals(course)) throw new CourseAlreadyPurchasedException();
        }
        balance -= course.getPrice();
        courses[courseCount] = course;
        ++courseCount;
    }

    protected void buyCourse(Course course, double price) throws InsufficientBalanceException, CourseAlreadyPurchasedException, MaxCourseCapacityReachedException {
        if (courseCount == MAX_COUNT) {
            throw new MaxCourseCapacityReachedException();
        }
        if (course.getPrice() > balance) {
            throw new InsufficientBalanceException();
        }
        for (int i = 0; i < courseCount; i++) {
            if (courses[i].equals(course)) throw new CourseAlreadyPurchasedException();
        }
        balance -= price;
        courses[courseCount] = course;
        ++courseCount;
    }

    public void completeResourcesFromCourse(Course course, Resource[] resourcesToComplete) throws CourseNotPurchasedException, ResourceNotFoundException {
        for (int i = 0; i < courseCount; i++) {
            if (courses[i].equals(course)) {
                for (Resource resource : resourcesToComplete) {
                    courses[i].completeResource(resource);
                }
                return;
            }
        }
        throw new CourseNotPurchasedException();
    }

    public void completeCourse(Course course, double grade) throws CourseNotPurchasedException, CourseNotCompletedException {
        for (int i = 0; i < courseCount; i++) {
            if (courses[i].equals(course)) {
                if (!course.isCompleted()) {
                    throw new CourseNotCompletedException();
                }
                grades[i] = grade;
                return;
            }
        }
        throw new CourseNotPurchasedException();
    }

    public Course getLeastCompletedCourse() {
        Course result = courses[0];
        for (int i = 0; i < courseCount - 1; i++) {
            if (result.getCompletionPercentage() > courses[i + 1].getCompletionPercentage()) {
                result = courses[i + 1];
            }
        }

        return result;
    }
}