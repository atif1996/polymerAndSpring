package com.atif.app;

/**
 * Simple POD type with the user record fields
 */
public class UserRecord
{
    public int rowId;
    public String firstName;
    public String lastName;
    public int age;

    public UserRecord(int uid, String first, String last, int userAge ) {
        rowId = uid;
        firstName = first;
        lastName = last;
        age = userAge;
    }
}
