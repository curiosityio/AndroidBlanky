package com.levibostian.androidblanky.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.levibostian.androidblanky.R;
import com.levibostian.androidblanky.fragment.MainFragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        startupFragment();
    }

    private void startupFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, MainFragment.newInstance())
                .commit();
    }

}
