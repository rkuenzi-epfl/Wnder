package com.github.wnder;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;



import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class StorageTest {
    @Test
    public void testUploadAndDownloadToFireStore(){
        Storage storage = Mockito.mock(Storage.class);

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Jeremy", "Jus d'orange");
        testMap.put("Léonard", "Lasagne");
        testMap.put("Romain", "Pizza du jeudi soir");
        testMap.put("Nico", "Cookies");
        testMap.put("Alois", "merci MV");

        String collection = "test";
        storage.uploadToFirestore(testMap, collection);
        storage.downloadFromFirestore(collection);


        assertThat(testMap, is(storage.getMap()));
    }
}
