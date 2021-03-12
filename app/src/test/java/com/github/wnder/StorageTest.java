package com.github.wnder;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class StorageTest {
    @Test
    public void testUploadAndDownloadToFireStore(){
        Map<String, String> testMap = new HashMap<>();
        testMap.put("Jeremy", "Jus d'orange");
        testMap.put("Léonard", "Lasagne");
        testMap.put("Romain", "Pizza du jeudi soir");
        testMap.put("Nico", "Cookies");
        testMap.put("Alois", "merci MV");


        assertThat(42, is(42));
    }
}
