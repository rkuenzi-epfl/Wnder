package com.github.wnder.picture;

import android.graphics.Bitmap;

import com.github.wnder.Storage;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Class defining the reported pictures
 */
public final class ReportedPictures {

    /**
     * Non-instanciable class
     */
    private ReportedPictures(){
        //Non-instanciable class
    }

    /**
     * add a picture to the list of reported pictures on the db
     * @param uniqueId the unique ID of the reported picture
     * @return a completable future that completes when the picture is successfully added to reported photo
     */
    public static CompletableFuture<Void> addToReportedPictures(String uniqueId){
        CompletableFuture pictureAdded = new CompletableFuture();
        Storage.uploadToFirestore(new HashMap<String, Object>() {
        }, "reportedPictures", uniqueId)
                .addOnSuccessListener((nothing)->{
                    pictureAdded.complete(null);
                });
        return pictureAdded;
    }

    /**
     *
     * @param reportedPicturesAvailable what to do when the reported set of pictures is available
     * @return a completable future that completes when the set of reported picture is available
     */
    public static void onAllReportedPicturesAvailable(Consumer<Set<String>> reportedPicturesAvailable){
        Storage.downloadCollectionFromFirestore("reportedPictures")
                .addOnSuccessListener((queryDocumentSnapshots)->{
                    Set<String> reportedPics = new HashSet<>();
                    for(DocumentSnapshot ds: queryDocumentSnapshots.getDocuments()){
                        reportedPics.add(ds.getId());
                    }

                    reportedPicturesAvailable.accept(reportedPics);
                });
    }
}
