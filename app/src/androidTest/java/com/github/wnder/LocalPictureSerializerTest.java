package com.github.wnder;

import android.location.Location;

import com.github.wnder.picture.LocalPictureSerializer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@RunWith(JUnit4.class)
public class LocalPictureSerializerTest {

    private static Location realLoc;
    private static Location guessLoc;
    private static Map<String, Double> scoreboard;

    private static String serializedJSON;
    private static JSONObject deserializedJSON;

    @BeforeClass
    public static void setup(){
        realLoc = new Location("");
        realLoc.setLongitude(0.1);
        realLoc.setLatitude(1.1);

        guessLoc = new Location("");
        guessLoc.setLongitude(10.1);
        guessLoc.setLatitude(11.1);

        scoreboard = new HashMap<>();
        scoreboard.put("testUser", 200.);

        serializedJSON = LocalPictureSerializer.serializePicture(realLoc, guessLoc, scoreboard);
        deserializedJSON = LocalPictureSerializer.deserializePicture(serializedJSON);
    }

    @Test
    public void getRealLocationWorks() {
        //Real Location
        Location rL = LocalPictureSerializer.getRealLocation(deserializedJSON);
        assertThat(rL.getLongitude(), is(realLoc.getLongitude()));
        assertThat(rL.getLatitude(), is(realLoc.getLatitude()));
    }

    @Test
    public void getGuessLocationWorks(){
        //Guess Location
        Location gL = LocalPictureSerializer.getGuessLocation(deserializedJSON);
        assertThat(gL.getLongitude(), is(guessLoc.getLongitude()));
        assertThat(gL.getLatitude(), is(guessLoc.getLatitude()));
    }

    @Test
    public void getScoreboardWorks(){
        //Scoreboard
        Map<String, Double> newScoreboard = LocalPictureSerializer.getScoreboard(deserializedJSON);
        assertThat(newScoreboard.get("testUser"), is(scoreboard.get("testUser")));
    }

}
