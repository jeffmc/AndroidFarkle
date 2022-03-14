package com.example.androidfarkle;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

// Jeff McMillan
// 3/13/22
// MainActivity contains all the code for this Farkle android game. ( logic, UI handling )
// It is a dice game, rules can be found here https://en.wikipedia.org/wiki/Farkle

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int[] diceImages = new int[6]; // Contains pointers to image resources
    ImageButton[] buttons = new ImageButton[6]; // Dice button array

    int[] diceValues; // Values in ints

    final int HOT_DICE = 0, SELECTED_DICE = 1, LOCKED_DICE = 2; // Die states
    final int HOT_COLOR = Color.WHITE, SCORE_COLOR = Color.RED, LOCKED_COLOR = Color.BLUE; // Die state colors
    int[] diceState; // Die state array

    final int GAME_PREROLL = 0, GAME_SCORING = 1, GAME_FINISHED_ROUND = 2; // before roll, scoring, no moves left to make.
    int gameState; // Game state

    Button roll, score, stop; // Buttons
    final String selectedScorePrefix = "Selected Score: ", // Text prefixes
            roundScorePrefix = "Round Score: ",
            totalScorePrefix = "Total Score: ",
            roundNumberPrefix = "Current Round: ";
    int selectedScore = 0, roundScore = 0, totalScore = 0, roundNumber = 0; // Scores and round number
    TextView selectedScoreText, roundScoreText, totalScoreText, roundNumberText; // More text

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Entry point
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farkle);
        findUIElements();

        resetGame();
    }

    private void resetGame() { // RESTART ENTIRE GAME ALL VARIABLES!
        selectedScore = 0;
        roundScore = 0;
        roundNumber = 0;
        totalScore = 0;
        selectedScoreText.setText(selectedScorePrefix + selectedScore);
        roundScoreText.setText(roundScorePrefix + roundScore);
        roundNumberText.setText(roundNumberPrefix + roundNumber);
        totalScoreText.setText(totalScorePrefix + totalScore);
        resetDice();
    }

    private void resetDice() { // Reset just the dice and game state to pre-roll, perfect for newRound()
        diceValues = new int[6];
        for (int i=0;i<buttons.length;i++) {
            buttons[i].setBackgroundColor(Color.WHITE);
            buttons[i].setImageResource(diceImages[i]);
            diceValues[i] = i+1;
        }
        diceState = new int[6];
        Arrays.fill(diceState, HOT_DICE);
        setGameState(GAME_PREROLL);
    }

    private void findUIElements() { // Determine UI elements into their respective variables, and add listeners
        diceImages[0] = R.drawable.one;
        diceImages[1] = R.drawable.two;
        diceImages[2] = R.drawable.three;
        diceImages[3] = R.drawable.four;
        diceImages[4] = R.drawable.five;
        diceImages[5] = R.drawable.six;
        buttons[0] = (ImageButton) this.findViewById(R.id.imageButton1);
        buttons[1] = (ImageButton) this.findViewById(R.id.imageButton2);
        buttons[2] = (ImageButton) this.findViewById(R.id.imageButton3);
        buttons[3] = (ImageButton) this.findViewById(R.id.imageButton4);
        buttons[4] = (ImageButton) this.findViewById(R.id.imageButton5);
        buttons[5] = (ImageButton) this.findViewById(R.id.imageButton6);
        for (ImageButton btn : buttons)
            btn.setOnClickListener(this);
        roll = (Button) this.findViewById(R.id.rollBtn);
        score = (Button) this.findViewById(R.id.scoreBtn);
        stop = (Button) this.findViewById(R.id.stopBtn);
        roll.setOnClickListener(this);
        score.setOnClickListener(this);
        stop.setOnClickListener(this);
        selectedScoreText = (TextView) this.findViewById(R.id.selectedScoreText);
        roundScoreText = (TextView) this.findViewById(R.id.roundScoreText);
        roundNumberText = (TextView) this.findViewById(R.id.roundNumberText);
        totalScoreText = (TextView) this.findViewById(R.id.totalScoreText);
    }

    private void setGameState(int newState) { // Set gameState and change button states accordingly.
        if (newState == GAME_PREROLL) {
            gameState = GAME_PREROLL;
            roll.setEnabled(true);
            score.setEnabled(false);
            stop.setEnabled(roundScore > 0);
        } else if (newState == GAME_SCORING) {
            gameState = GAME_SCORING;
            roll.setEnabled(false);
            score.setEnabled(true);
            stop.setEnabled(false);
        } else if (newState == GAME_FINISHED_ROUND) {
            gameState = GAME_SCORING;
            roll.setEnabled(false);
            score.setEnabled(false);
            stop.setEnabled(true);
        } else {
            throw new IllegalArgumentException("ILLEGAL GAME STATE: " + newState);
        }
    }

    @Override
    public void onClick(View v) { // All click handling
        if (v.equals(roll)) {
            onRollClick();
            return;
        } else if (v.equals(score)) {
            onScoreClick();
            return;
        } else if (v.equals(stop)) {
            onStopClick();
            return;
        } else {
            for (int i=0;i<buttons.length;i++) {
                if (v.equals(buttons[i])) {
                    onDiceClick(i);
                    return;
                }
            }
        }
    }

    private void randomizeUnlockedDice() { // randomize the unlocked dice ( hot/selected )
        for (int i=0;i<buttons.length;i++) {
            if (diceState[i] == HOT_DICE || diceState[i] == SELECTED_DICE) {
                ImageButton btn = buttons[i];
                int random = (int) Math.floor(Math.random() * 6); // 0-5 (inclusive)
                btn.setImageResource(diceImages[random]);
                diceValues[i] = random + 1;
            }
        }
    }

    private void onRollClick() { // Handles roll button press
        randomizeUnlockedDice(); // Roll the dice
        if (getUnlockedScore() <= 0) {
            forfeitPrompt(); // No moves left to make this round, present user with options
            return;
        } else {
            setGameState(GAME_SCORING); // Go to scoring.
            return;
        }
    }

    private void forfeitPrompt() { // Used code from https://mkyong.com/android/android-alert-dialog-example/
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("Game over?");
        alertDialogBuilder
                .setMessage("Forfeit only this round or throw in the towel!")
                .setCancelable(false)
                .setPositiveButton("Forfeit round",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        roundScore = 0;
                        newRound();
                        alertUser("Forfeit round score and advanced!", false);
                    }
                })
                .setNegativeButton("Restart game",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        resetGame();
                        alertUser("Restarted!", false);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show(); // Finally show!
    }

    private void onScoreClick() {
        int scoredDie = 0; // Count dice
        int lockedDie = 0;

        updateSelectedScore(); // Get selected score
        if (selectedScore <= 0) {
            alertUser("Must select a dice of a score greater than zero!", false);
            return;
        }

        for (int i=0;i<buttons.length;i++) { // Iterate through all buttons
            ImageButton btn = buttons[i];
            if (diceState[i] == SELECTED_DICE) { // If selected
                scoredDie++; // Increment scored count
                diceState[i] = LOCKED_DICE; // Lock button
                lockedDie++; // Increment locked count
                btn.setBackgroundColor(LOCKED_COLOR); // Change background
            } else if (diceState[i] == LOCKED_DICE) { // If locked
                lockedDie++; // increment locked count
            }
        }

        roundScore += selectedScore; // Increment round score
        roundScoreText.setText(roundScorePrefix + roundScore);
        updateSelectedScore(); // Reset selected score

        if (lockedDie == 6) { // No more moves left to make this round
            setGameState(GAME_FINISHED_ROUND);
            alertUser("Round over! Press stop to advance to next round", false);
            return;
        } else {
            setGameState(GAME_PREROLL); // Allow to re roll or stop and advance to new round
            return;
        }
    }

    private void onStopClick() {
        newRound(); // When stop is clicked new round starts
    }

    private void newRound() {
        roundNumber++; // Increment round number
        roundNumberText.setText(roundNumberPrefix + roundNumber);
        totalScore += roundScore; // Increment overall score
        totalScoreText.setText(totalScorePrefix + totalScore);
        roundScore = 0; // Reset current round score
        roundScoreText.setText(roundScorePrefix + roundScore);
        resetDice(); // Reset the dice
    }

    private void onDiceClick(int i) { // i is the index of the dice pressed
        ImageButton btn = buttons[i];
        if (gameState == GAME_SCORING) {
            if (diceState[i] == HOT_DICE) { // Flip between HOT and SELECTED!
                diceState[i] = SELECTED_DICE;
                btn.setBackgroundColor(SCORE_COLOR); // change backgrounds accordingly
            } else if (diceState[i] == SELECTED_DICE) {
                diceState[i] = HOT_DICE;
                btn.setBackgroundColor(HOT_COLOR);
            } else if (diceState[i] == LOCKED_DICE) { // Prevent locked from flipping
                alertUser("Dice has already been scored!", false);
            }
            updateSelectedScore();
        } else {
            alertUser("Roll before selecting dice to score!", false);
        }
        return;
    }

    private void updateSelectedScore() { // Hypothetical score if button was pressed right now
        int[] dv = getDiceValuesOfState(SELECTED_DICE);
        selectedScore = getScore(dv);
        selectedScoreText.setText(selectedScorePrefix + selectedScore);
    }

    private int getUnlockedScore() { // Possible score from all unlocked dice
        int sdv[] = getDiceValuesOfState(SELECTED_DICE);
        int hdv[] = getDiceValuesOfState(HOT_DICE);
        int dv[] = Arrays.copyOf(sdv, sdv.length + hdv.length); // Concat arrays
        int o = sdv.length;
        for (int i=0;i<hdv.length;i++) {
            dv[o+i] = hdv[i];
        }
        return getScore(dv);
    }

    private int[] getDiceValuesOfState(int ds) { // Returns the dice values of all die with this state
        int count = 0;
        for (int state : diceState) {
            if (state == ds) count++;
        }
        int[] dv = new int[count];
        int dvi = 0;
        for (int i=0;i<buttons.length;i++) {
            if (diceState[i] == ds) {
                dv[dvi] = diceValues[i];
                dvi++;
            }
        }
        return dv;
    }

    private int getScore(int[] dv) { // Scoring function, takes in dice values, could be any number of dice >= 1;
        final int TRIPLE_ONE = 1000, TRIPLE_TWO = 200, TRIPLE_THREE = 300,
                TRIPLE_FOUR = 400, TRIPLE_FIVE = 500, TRIPLE_SIX = 600,
                SINGLE_ONE = 100, SINGLE_FIVE = 50;
        int[] counts = new int[6];
        for (int v : dv) {
            counts[v-1]++;
        }
        int score = 0;
        if (counts[0] < 3) { // Single One
            score += counts[0]*SINGLE_ONE;
        } else { // Triple Ones
            score += (counts[0]-2)*TRIPLE_ONE;
        }
        if (counts[1] >= 3) { // Two
            score += (counts[1]-2)*TRIPLE_TWO;
        }
        if (counts[2] >= 3) { // Three
            score += (counts[2]-2)*TRIPLE_THREE;
        }
        if (counts[3] >= 3) { // Four
            score += (counts[3]-2)*TRIPLE_FOUR;
        }
        if (counts[4] < 3) { // Single Five
            score += counts[4]*SINGLE_FIVE;
        } else { // Triple Fives
            score += (counts[4]-2)*TRIPLE_FIVE;
        }
        if (counts[5] >= 3) { // Six
            score += (counts[5]-2)*TRIPLE_SIX;
        }
        return score;
    }

    private void alertUser(String msg, boolean longToast) { // Send the user a toast! (shorthand)
        Toast.makeText(getApplicationContext(),msg, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
}