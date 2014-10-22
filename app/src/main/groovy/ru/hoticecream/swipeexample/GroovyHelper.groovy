package ru.hoticecream.swipeexample

import android.app.Activity
import android.os.Bundle
import android.util.Log

class GroovyHelper extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vertical_swipe)
        def a = 10
        def b = 20
        def (c, d) = [10, 20]
    }
}