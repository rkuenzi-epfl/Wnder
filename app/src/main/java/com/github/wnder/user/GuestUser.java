package com.github.wnder.user;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.wnder.R;
import com.github.wnder.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class GuestUser implements User{

    public String getName(){
        return "Guest";
    }

    public Uri getProfilePicture(){
        return Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag);
    }

    /**
     * Returns the id of a picture existing in the db
     * @return the id of the picture, an empty string if non is eligible
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void onNewPictureAvailable(Consumer<String> pictureIdAvailable){
        //Get the ids of all the uploaded pictures

        Set<String> allIds = new HashSet<>();
        Storage.downloadFromFirestore("pictures").addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                for(int i = 0; i < docs.size(); i++){
                    allIds.add(docs.get(i).getId());
                }
                //If no image fits, return empty string
                if(0 == allIds.size()){
                    pictureIdAvailable.accept("");
                }
                //else, return randomly chosen string
                else{
                    List<String> idList = new ArrayList<>();
                    idList.addAll(allIds);
                    Random random = new Random();
                    int index = random.nextInt(allIds.size());
                    pictureIdAvailable.accept(idList.get(index));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pictureIdAvailable.accept("");
            }
        });

    }
}
