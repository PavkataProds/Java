package bg.sofia.uni.fmi.mjt.udemy.account;

import bg.sofia.uni.fmi.mjt.udemy.account.type.AccountType;

public class EducationalAccount extends AccountBase {
    private int index;

    public EducationalAccount(String name, double balance) {
        super(name, balance, AccountType.EDUCATION);
        index = 0;
    }
}