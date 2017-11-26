package com.alexeilebedev.gles1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Home extends AppCompatActivity {
    Glview _glview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _glview = new Glview(this);
        setContentView(_glview);
    }
    @Override
    protected void onResume() {
        super.onResume();
        _glview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _glview.onPause();
    }

}
