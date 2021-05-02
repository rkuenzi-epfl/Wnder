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
    private static LocalPictureSerializer LPS = new LocalPictureSerializer();

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

        serializedJSON = LPS.serializePicture(realLoc, guessLoc, scoreboard);
        deserializedJSON = LPS.deserializePicture(serializedJSON);
    }

    @Test
    public void getRealLocationWorks() throws JSONException {
        //Real Location
        assertThat(deserializedJSON.get("realLongitude"), is(realLoc.getLongitude()));
        assertThat(deserializedJSON.get("realLatitude"), is(realLoc.getLatitude()));
    }

    @Test
    public void getGuessLocationWorks() throws JSONException{
        //Guess Location
        assertThat(deserializedJSON.get("guessedLongitude"), is(guessLoc.getLongitude()));
        assertThat(deserializedJSON.get("guessedLatitude"), is(guessLoc.getLatitude()));
    }

    @Test
    public void getScoreboardWorks() throws JSONException{
        //Scoreboard
        String json = (String)deserializedJSON.get("scoreboard");
        Gson gson = new Gson();
        Map<String, Double> newScoreboard = gson.fromJson(json, HashMap.class);
        assertThat(newScoreboard.get("testUser"), is(scoreboard.get("testUser")));
    }

}
