package com.eslamdev.mawjaz.view;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.eslamdev.mawjaz.util.LocalHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalHelper.onAttach(newBase));
    }
}