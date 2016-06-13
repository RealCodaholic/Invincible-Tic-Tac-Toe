package com.ttt.jasonlow.tictactoethatyouneverwin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btn_3x3(View view) {
        startGame(3);
    }

    public void btn_4x4(View view) {
        startGame(4);
    }

    public void btn_5x5(View view) {
        startGame(5);
    }

    private void startGame(int dimen) {
        Bundle b = new Bundle();
        b.putInt("dimen", dimen);
        startActivity(new Intent(this, GameActivity.class).putExtras(b));
    }
}
