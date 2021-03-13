package com.github.wnder;

public class GlobalUser{

    private static User user = new GuestUser();

    //prevent instantiation
    private GlobalUser(){}

    public static User getUser(){
        return user;
    }

    public static void resetUser(){
        user = new GuestUser();
    }

    public static void setUser(User newUser){
        user = newUser;
    }
}
