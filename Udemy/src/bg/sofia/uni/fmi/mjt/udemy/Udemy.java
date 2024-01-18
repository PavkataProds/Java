package bg.sofia.uni.fmi.mjt.udemy;

import bg.sofia.uni.fmi.mjt.udemy.account.Account;
import bg.sofia.uni.fmi.mjt.udemy.course.Category;
import bg.sofia.uni.fmi.mjt.udemy.course.Course;
import bg.sofia.uni.fmi.mjt.udemy.exception.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.udemy.exception.CourseNotFoundException;

public class Udemy implements LearningPlatform {

    private final Account[] accounts;
    private final Course[] courses;

    public Udemy(Account[] accounts, Course[] courses) {
        this.accounts = accounts;
        this.courses = courses;
    }

    @Override
    public Course findByName(String name) throws CourseNotFoundException {
        for (Course course : courses) {
            if (course.getName().equals(name)) return course;
        }
        throw new CourseNotFoundException();
    }

    @Override
    public Course[] findByKeyword(String keyword) {
        int size = 0;
        for (Course course : courses) {
            if (course.getName().contains(keyword) ||
                    course.getDescription().contains(keyword)) size++;
        }

        Course[] foundCourses = new Course[size];
        for (Course course : courses) {
            if (course.getName().contains(keyword) ||
                    course.getDescription().contains(keyword)) {
                foundCourses[size-- - 1] = course;
            }
        }

        return foundCourses;
    }

    @Override
    public Course[] getAllCoursesByCategory(Category category) {
        int size = 0;
        for (Course course : courses) {
            if (course.getCategory().equals(category)) size++;
        }

        Course[] foundCourses = new Course[size];
        for (Course course : courses) {
            if (course.getCategory().equals(category)) {
                foundCourses[size-- - 1] = course;
            }
        }

        return foundCourses;
    }

    @Override
    public Account getAccount(String name) throws AccountNotFoundException {
        for (Account account : accounts) {
            if (account.getUsername().equals(name)) return account;
        }
        throw new AccountNotFoundException();
    }

    @Override
    public Course getLongestCourse() {
        Course longestCourse = courses[0];
        for (int i = 1; i < courses.length; i++) {
            if (longestCourse.getTotalTime().less(courses[i].getTotalTime())) {
                longestCourse = courses[i];
            }
        }

        return longestCourse;
    }

    @Override
    public Course getCheapestByCategory(Category category) {
        Course cheapestCourse = courses[0];
        for (int i = 1; i < courses.length; i++) {
            if (courses[i].getCategory() == category && courses[i].getPrice() < cheapestCourse.getPrice()) {
                cheapestCourse = courses[i];
            }
        }

        return cheapestCourse;
    }
}