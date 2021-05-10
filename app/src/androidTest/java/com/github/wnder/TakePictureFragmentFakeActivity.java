package com.github.wnder;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Workaround to put a fragment in an activity that we control for the following reasons:
 * It is a AndroidEntryPoint to be able to use/mock hilt modules
 * It allows to create custom ActivityResultRegistry to simulate the result from the take picture activity
 * ! Warning: it creates a lot of empty image files in the "Pictures" storage of the phone
 */
@AndroidEntryPoint
public class TakePictureFragmentFakeActivity extends AppCompatActivity {

    public static final String EXPECTED_RESULT = "expectedResult";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);

        Boolean expectedResult = getIntent().getExtras().getBoolean(EXPECTED_RESULT, true);
        ActivityResultRegistry testRegistry = new ActivityResultRegistry() {
            @Override
            public <I, O> void onLaunch(int requestCode, @NonNull ActivityResultContract<I, O> contract, I input, @Nullable ActivityOptionsCompat options) {
                dispatchResult(requestCode, expectedResult);
            }
        };
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, new TakePictureFragment(testRegistry), null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

}

