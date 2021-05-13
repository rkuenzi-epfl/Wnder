package com.github.wnder.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserDatabase {

    /**
     * Retrieve on of guessed or uploaded picture list
     * @param user the use for which we want the list
     * @param picturesListName which list we want
     * @return a Future of list of picture ids
     */
    CompletableFuture<List<String>> getPictureList(User user, String picturesListName);

    /**
     * Retrieve a picture id of a picture the user did not guess
     * @param user the use for which we want a picture id
     * @return a Future with a picture id
     */
    CompletableFuture<String> getNewPictureForUser(User user, int radius);

}
