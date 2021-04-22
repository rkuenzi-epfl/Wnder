package com.github.wnder.user;

/**
 * This class defines the global user of the application
 */
public class GlobalUser{

    //By default, it's a guest
    private static User user = new GuestUser();

    /**
     * Prevents instantiation
     */
    private GlobalUser(){}

    /**
     * Get the global user
     * @return global user
     */
    public static User getUser(){
        return user;
    }

    /**
     * Reset global user, put it back to a guest user
     */
    public static void resetUser(){
        user = new GuestUser();
    }

    /**
     * Set global user
     * @param newUser new global user
     */
    public static void setUser(User newUser){
        user = newUser;
    }
}
