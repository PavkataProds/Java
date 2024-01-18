package bg.sofia.uni.fmi.mjt.udemy.account;

import bg.sofia.uni.fmi.mjt.udemy.account.type.AccountType;
import bg.sofia.uni.fmi.mjt.udemy.course.Category;
import bg.sofia.uni.fmi.mjt.udemy.course.Course;
import bg.sofia.uni.fmi.mjt.udemy.exception.CourseAlreadyPurchasedException;
import bg.sofia.uni.fmi.mjt.udemy.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.udemy.exception.MaxCourseCapacityReachedException;

public class BusinessAccount extends AccountBase {
    private Category[] allowedCategories;

    public BusinessAccount(String username, double balance, Category[] allowedCategories) {
        super(username, balance, AccountType.BUSINESS);
        this.allowedCategories = allowedCategories;
    }

    private boolean containsCategory(Category search) {
        for (Category category : allowedCategories) {
            if (category.equals(search)) return true;
        }
        return false;
    }

    public void buyCourse(Course course) throws InsufficientBalanceException, CourseAlreadyPurchasedException, MaxCourseCapacityReachedException {
        if (!containsCategory(course.getCategory())) throw new IllegalArgumentException();

        double price = course.getPrice() - (course.getPrice() * accountType.getDiscount());
        super.buyCourse(course, price);

    }
}