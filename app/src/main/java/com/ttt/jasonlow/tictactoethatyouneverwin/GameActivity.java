package com.ttt.jasonlow.tictactoethatyouneverwin;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private TBoard board;
    private LinearLayout ly_board;
    private Button[][] boxes;
    private Switch sw_comp;
    private boolean foundWinner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int DIMEN = getIntent().getExtras().getInt("dimen");
        board = new TBoard(DIMEN);

        setTitle(String.valueOf(DIMEN) + "x" + String.valueOf(DIMEN) + " Board");

        ly_board = (LinearLayout) findViewById(R.id.game_board);
        sw_comp = (Switch) findViewById(R.id.switch_comp);

        generateBoardUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void generateBoardUI() {
        boxes = new Button[board.DIMEN][board.DIMEN];
        int btn_pad = 5, frame_pad = 50;
        //get screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        //calculate button width
        int btn_width = (screenWidth - (frame_pad * 2) - (btn_pad * 2 * board.DIMEN)) / board.DIMEN;
        ViewGroup.LayoutParams btn_params = new LinearLayout.LayoutParams(btn_width, btn_width);

        //setting up layout
        LinearLayout new_row = null;
        for (int y = 0; y < board.DIMEN; y++) {
            new_row = new LinearLayout(this);
            new_row.setOrientation(LinearLayout.HORIZONTAL);

            for (int x = 0; x < board.DIMEN; x++) {
                final Button btn_block = new Button(this);
                btn_block.setLayoutParams(btn_params);
                btn_block.setTextSize(btn_width / 7.5f);
                btn_block.setTextColor(getResources().getColor(R.color.clrBlack));
                btn_block.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                //btn_block.setBackgroundColor(getResources().getColor(R.color.colorNormal));

                //set OnClickListener
                final int finalX = x, finalY = y;
                btn_block.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        board.insert(finalX, finalY, -1);
                        btn_block.setText("O");
                        btn_block.setEnabled(false);
                        board.totalMoves++;

                        ArrayList<ArrayList<int[]>> winLines = board.getWinLine(-1);
                        if (winLines != null) {
                            //Display all win lines of the winner
                            showLine(winLines);
                            boxesEnable(false);
                            Toast.makeText(v.getContext(), "Conguration! You won the game!", Toast.LENGTH_LONG).show();
                            foundWinner = true;
                        } else
                            systemMove();

                        if(board.totalMoves == board.DIMEN * board.DIMEN)
                            Toast.makeText(v.getContext(), "Draw", Toast.LENGTH_SHORT).show();
                    }
                });

                new_row.addView(btn_block);
                boxes[x][y] = btn_block;
            }
            ly_board.addView(new_row);
        }

        sw_comp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && board.totalMoves == 0)
                    systemMove();
            }
        });
    }

    private void boxesEnable(boolean flag) {
        for (Button[] btn : boxes)
            for (Button box : btn)
                box.setEnabled(flag);
    }

    private void showLine(ArrayList<ArrayList<int[]>> lines) {
        for (ArrayList<int[]> line : lines)
            for (int[] bx : line)
                boxes[bx[0]][bx[1]].setBackgroundColor(getResources().getColor(R.color.colorWin));

    }

    public void btn_restart(View view) {
        Intent i = getIntent();
        finish();
        startActivity(i);
    }

    private void systemMove() {
        int[] move = board.getSystemMove();
        if (move != null) {
            boxes[move[0]][move[1]].setText("X");
            boxes[move[0]][move[1]].setEnabled(false);
            board.insert(move[0], move[1], 1);
            board.totalMoves++;
            ArrayList<ArrayList<int[]>> winLines = board.getWinLine(1);
            if (winLines != null) {
                showLine(winLines);
                boxesEnable(false);
                Toast.makeText(this, "Computer win!", Toast.LENGTH_SHORT).show();
                foundWinner = true;
            }
        }
    }
}
