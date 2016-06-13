package com.ttt.jasonlow.tictactoethatyouneverwin;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Jason Low on 6/10/2016.
 */
public class TBoard {
    public final int DIMEN; //dimension of the board - DIMEN*DIMEN
    private int[][] mBoard; //the game board
    private ArrayList<ArrayList<int[]>> win_lines = new ArrayList<>(); //All possible win lines
    public int totalMoves = 0; //total moves

    TBoard(int DIMEN) {
        this.DIMEN = DIMEN;
        mBoard = new int[DIMEN][DIMEN];

        generateWinLines();
    }

    private void generateWinLines() {
        ArrayList<int[]> line1, line2;
        int[] box;

        //Get horizontal and vertical win lines
        for (int i = 0; i < DIMEN; i++) {
            line1 = new ArrayList<>();
            line2 = new ArrayList<>();
            for (int j = 0; j < DIMEN; j++) {
                box = new int[2];
                box[0] = i;
                box[1] = j;
                line1.add(box);

                box = new int[2];
                box[0] = j;
                box[1] = i;
                line2.add(box);
            }
            win_lines.add(line1);
            win_lines.add(line2);
        }

        //Get cross win lines - the X lines
        line1 = new ArrayList<>();
        line2 = new ArrayList<>();
        for (int i = 0; i < DIMEN; i++) {
            box = new int[]{i, i};
            line1.add(box);
            box = new int[]{DIMEN - i - 1, i};
            line2.add(box);
        }
        win_lines.add(line1);
        win_lines.add(line2);

        //For print checking all win lines
        String str;
        /*for (ArrayList<int[]> line : win_lines) {
            str = "";
            for (int[] bx : line)
                str += ("[" + String.valueOf(bx[0]) + ", " + String.valueOf(bx[1]) + "] ");
            Log.d("Line", str);
        }*/
    }

    //Get win line(s) of player
    public ArrayList<ArrayList<int[]>> getWinLine(int player) {
        boolean[] flag;
        ArrayList<ArrayList<int[]>> foundLines = new ArrayList<>();

        int counter;
        for (ArrayList<int[]> line : win_lines) {
            counter = 0;
            flag = new boolean[DIMEN];
            for (int[] box : line) {
                if (mBoard[box[0]][box[1]] == player)
                    flag[counter] = true;
                counter++;
            }

            for (int i = 0; i < DIMEN; i++) {
                if (!flag[i])
                    break;
                if (i == DIMEN - 1 && flag[i]) {
                    foundLines.add(line);
                }
            }
        }
        if (foundLines.size() == 0)
            return null;
        return foundLines;
    }

    public void insert(int x, int y, int player) {
        mBoard[x][y] = player;
    }

    public int[] getSystemMove() {
        if (DIMEN == 3) {
            if (totalMoves == 1) {
                if (mBoard[1][1] == 0) //if center empty
                    return new int[]{1, 1};
                else {
                    int[] position = new int[]{0, 2};
                    return new int[]{position[(int) (Math.random() * 2)], position[(int) (Math.random() * 2)]}; //random edge position
                }
            }
        }

        if (totalMoves == 0) //Random first move
            return new int[]{(int) (Math.random() * DIMEN), (int) (Math.random() * DIMEN)};

        if (totalMoves == DIMEN * DIMEN) //if achieved maximum moves
            return null;

        int randInt; //for generate random int

        //check comp one step win
        ArrayList<int[]> temp = oneStepWin(1);
        if (temp != null) {
            randInt = (int) (Math.random() * temp.size());
            return new int[]{temp.get(randInt)[0], temp.get(randInt)[1]};
        }

        //check enemy one step win
        temp = oneStepWin(-1);
        if (temp != null) {
            randInt = (int) (Math.random() * temp.size());
            return new int[]{temp.get(randInt)[0], temp.get(randInt)[1]};
        }

        //get enemy win rate of all boxes
        int[][] rateMatrix = winRates(-1), twoWay = null;
        HighestRatePosition hrp = filterHighest(rateMatrix);
        ArrayList<int[]> highestRatePosition = hrp.highestRatePosition, lowestPosition = null;
        int highest = hrp.highestRate, lowest = 0;

        //Check two way
        if (highestRatePosition.size() > 1) {
            for (int[] position : highestRatePosition) {
                insert(position[0], position[1], -1);
                ArrayList<int[]> oneStepWin = oneStepWin(-1);
                if (oneStepWin != null && oneStepWin.size() > 1) {
                    if (twoWay == null) {
                        twoWay = new int[DIMEN][DIMEN];
                        lowestPosition = new ArrayList<>();
                    }

                    for (int[] oneStep : oneStepWin) {
                        twoWay[oneStep[0]][oneStep[1]]--;
                        if (twoWay[oneStep[0]][oneStep[1]] == lowest)
                            lowestPosition.add(new int[]{oneStep[0], oneStep[1]});

                        if (twoWay[oneStep[0]][oneStep[1]] < lowest) {
                            lowestPosition.clear();
                            lowest = twoWay[oneStep[0]][oneStep[1]];
                            lowestPosition.add(new int[]{oneStep[0], oneStep[1]});
                        }
                    }
                }
                insert(position[0], position[1], 0);
            }
        }

        if (twoWay != null) {
            //return random two way position
            randInt = (int) (Math.random() * lowestPosition.size());
            return new int[]{lowestPosition.get(randInt)[0], lowestPosition.get(randInt)[1]};
        } else {
            //find best move
            for (int[] position : highestRatePosition) {
                insert(position[0], position[1], 1);
                ArrayList<int[]> oneStepWin = oneStepWin(1);
                if (oneStepWin != null) {
                    rateMatrix[position[0]][position[1]] += oneStepWin.size();
                }
                if (rateMatrix[position[0]][position[1]] > highest)
                    highest = rateMatrix[position[0]][position[1]];
                insert(position[0], position[1], 0);
            }
            printDArray(rateMatrix);

            //filter highest rate positions list to best positions list
            highestRatePosition.clear();
            for (int y = 0; y < DIMEN; y++) {
                for (int x = 0; x < DIMEN; x++) {
                    if (rateMatrix[x][y] == highest)
                        highestRatePosition.add(new int[]{x, y});
                }
            }

            //return random best position
            randInt = (int) (Math.random() * highestRatePosition.size());
            return new int[]{highestRatePosition.get(randInt)[0], highestRatePosition.get(randInt)[1]};
        }
    }

    //returns all one step win position
    @Nullable
    private ArrayList<int[]> oneStepWin(int player) {
        ArrayList<int[]> oneStepWin = new ArrayList<>();
        for (int y = 0; y < DIMEN; y++) {
            for (int x = 0; x < DIMEN; x++) {
                if (mBoard[x][y] == 0) {
                    insert(x, y, player);
                    if (getWinLine(player) != null)
                        oneStepWin.add(new int[]{x, y});
                    insert(x, y, 0);
                }
            }
        }
        if (oneStepWin.size() == 0)
            return null;
        return oneStepWin;
    }

    private int[][] winRates(int player) {
        int[][] matrix = new int[DIMEN][DIMEN];
        for (int y = 0; y < DIMEN; y++) {
            for (int x = 0; x < DIMEN; x++) {
                if (mBoard[x][y] == 0) {
                    insert(x, y, player);
                    for (int y2 = 0; y2 < DIMEN; y2++) {
                        for (int x2 = 0; x2 < DIMEN; x2++) {
                            if (mBoard[x2][y2] == 0) {
                                insert(x2, y2, player);
                                ArrayList<ArrayList<int[]>> win = getWinLine(player);
                                if (win != null) {
                                    matrix[x][y] += win.size();
                                    matrix[x2][y2] += win.size();
                                }
                                insert(x2, y2, 0);
                            }
                        }
                    }
                    insert(x, y, 0);
                } else
                    matrix[x][y] = -1;
            }
        }
        return matrix;
    }

    private HighestRatePosition filterHighest(int[][] rateMatrix) {
        ArrayList<int[]> highestRatePosition = new ArrayList<>();
        int highest = 0;
        for (int y = 0; y < DIMEN; y++) {
            for (int x = 0; x < DIMEN; x++) {
                if (rateMatrix[x][y] == highest)
                    highestRatePosition.add(new int[]{x, y});
                else if (rateMatrix[x][y] > highest) {
                    highestRatePosition.clear();
                    highestRatePosition.add(new int[]{x, y});
                    highest = rateMatrix[x][y];
                }
            }
        }
        return new HighestRatePosition(highestRatePosition, highest);
    }

    private void printDArray(int[][] m) {
        String line;
        for (int x = 0; x < DIMEN; x++) {
            line = "";
            for (int y = 0; y < DIMEN; y++) {
                line += "[" + String.valueOf(m[y][x]) + "]";
            }
            Log.d("Double Array Printer", line);
        }
    }

    public void printBoard() {
        printDArray(mBoard);
        Log.d("Total moves", String.valueOf(totalMoves));
    }
}

class HighestRatePosition {
    public ArrayList<int[]> highestRatePosition;
    public int highestRate;

    HighestRatePosition(ArrayList<int[]> hrp, int hr) {
        highestRatePosition = hrp;
        highestRate = hr;
    }
}
