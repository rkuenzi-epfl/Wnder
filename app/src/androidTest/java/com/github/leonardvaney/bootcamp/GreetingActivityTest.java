package com.github.leonardvaney.bootcamp;

        import android.content.Intent;

        import androidx.test.core.app.ActivityScenario;
        import androidx.test.core.app.ApplicationProvider;
        import androidx.test.espresso.Espresso;
        import androidx.test.espresso.ViewAction;
        import androidx.test.espresso.matcher.ViewMatchers;
        import androidx.test.ext.junit.rules.ActivityScenarioRule;
        import androidx.test.ext.junit.runners.AndroidJUnit4;

        import org.junit.Rule;
        import org.junit.Test;
        import org.junit.runner.RunWith;

        import static androidx.test.espresso.assertion.ViewAssertions.matches;
        import static androidx.test.espresso.matcher.ViewMatchers.withText;
        import static com.github.leonardvaney.bootcamp.MainActivity.EXTRA_MESSAGE;

@RunWith(AndroidJUnit4.class)
public class GreetingActivityTest {

    @Test
    public void testGreeting(){

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GreetingActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "test"); //au lieu de faire ça on pourrait peut-être faire qqch de plus intelligent?

        ActivityScenario scenario = ActivityScenario.launch(intent);

        Espresso.onView(ViewMatchers.withId(R.id.textView3)).check(matches(withText("test")));

        scenario.close();
    }
}
