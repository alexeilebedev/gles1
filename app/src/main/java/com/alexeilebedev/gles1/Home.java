package com.alexeilebedev.gles1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;


public class Home extends AppCompatActivity {
    Glview _glview;
    // clock task
    class Task extends TimerTask {
        @Override public void run() {
            _glview.requestRender();
        }
    }
    Task _task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _glview = new Glview(this);
        setContentView(_glview);
        Timer timer = new Timer(true);
        _task=new Task();
        timer.scheduleAtFixedRate(_task,0,50);
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
