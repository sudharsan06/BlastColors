package com.dreamcreators.blastcolors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnDragListener {
    private TextView scoreText, tv_message;
    private TextView highScoreText;
    public ImageButton colorButton;
    private int score = 0;
    private int highScore = 0;
    private Random random;
    private TextView timerText;
    private SharedPreferences sharedPreferences;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 30000; // 30 seconds
    private boolean timerStarted = false; // Flag to check if the timer has started
    private int currentColor; // To store the current color of the button
    private View dropArea;
    private SoundPool soundPool;
    private int tapPopSound, endGameSound, loseSound, earnCoinSound;
    private TextView floatingText;
    private ArrayList<String> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        floatingText = findViewById(R.id.floatingText);
        scoreText = findViewById(R.id.scoreText);
        colorButton = findViewById(R.id.colorButton);
        highScoreText = findViewById(R.id.highScoreText); // New TextView for high score
        timerText = findViewById(R.id.timerText);
        tv_message = findViewById(R.id.tv_message);
        dropArea = findViewById(R.id.dropArea);

        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        tapPopSound = soundPool.load(this, R.raw.tap, 1);
        loseSound = soundPool.load(this, R.raw.lose, 1);
        endGameSound = soundPool.load(this, R.raw.end_game, 1);
        earnCoinSound = soundPool.load(this, R.raw.earn_coin, 1);

        words = new ArrayList<>();
        words.add("Outstanding");
        words.add("Excellent");
        words.add("Fantastic");
        words.add("Remarkable");
        words.add("Impressive");
        words.add("Superb");
        words.add("Brilliant");
        words.add("Incredible");
        words.add("Magnificent");
        words.add("Wonderful");
        words.add("Astounding");
        words.add("Admirable");
        words.add("Commendable");
        words.add("Skillful");
        words.add("Praiseworthy");
        words.add("Inspiring");
        words.add("Extraordinary");
        words.add("Top-notch");
        words.add("Fabulous");

        random = new Random();
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        highScore = sharedPreferences.getInt("HighScore", 0); // Retrieve high score
        updateHighScore();

        startGame();
        changeColor();
        dropArea.setOnDragListener(this);
        colorButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // For API level 24 and above
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(null, shadowBuilder, v, 0);
                } else {
                    // For API level below 24
                    ClipData data = ClipData.newPlainText("", "");
                    //View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    v.startDrag(data, new View.DragShadowBuilder(v), v, 0);
                }
                return true;
            }
        });


        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timerStarted) {
                    timerStarted = true;
                    startTimer(); // Start the timer on the first tap
                    // Set the flag to true
                }

                if (currentColor == Color.RED) {
                    floatingText.setVisibility(View.INVISIBLE);
                    soundPool.play(loseSound, 1, 1, 0, 0, 1);
                    gameOver(); // Trigger game over if red
                } else {
                    floatingText.setVisibility(View.VISIBLE);
                    score++;
                    soundPool.play(tapPopSound, 1, 1, 0, 0, 1);
                    updateScore();
                    changeColor();
                    floatText(displayRandomWord(), "1");
                }




               /* if (timeLeftInMillis > 0) {

                }*/
            }
        });

    }

    private void startGame() {
        score = 0;
      /*  updateScore();
        changeColor();
        startTimer();*/
    }

    private void floatText(String textToDisplay, String points) {
        // Reset TextView position and size


        tv_message.setText(textToDisplay+"!");
        floatingText.setTranslationY(0);
        floatingText.setScaleX(1);
        floatingText.setScaleY(1);
        if(points.equals("1")) {
            floatingText.setText(" +1 Point ");
        }else if(points.equals("10")){
            floatingText.setText(" +10 Points ");
        }else{
            floatingText.setText("");
        }


        // Create ValueAnimator for floating up
        ValueAnimator animator = ValueAnimator.ofFloat(0, -1000); // Adjust -1000 as needed
        animator.setDuration(2000); // Duration of animation

        // Update TextView position and size during animation
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                floatingText.setTranslationY(animatedValue);
                float scale = 1 - (animatedValue / -1000); // Adjust scale logic if necessary
                floatingText.setScaleX(scale);
                floatingText.setScaleY(scale);
            }
        });

        // Optional: Add listener to reset or remove the TextView after animation ends
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // You can choose to hide or reset the text here if desired
            }
        });

        animator.start();
    }

    private void startTimer() {
        if (timerStarted) {
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateTimer();
                }

                @Override
                public void onFinish() {
                    colorButton.setEnabled(false);
                    // Handle end of game, for example: show final score, reset game, etc.
                    floatText("Game Over! Your score: " + score, "0");
                    Toast.makeText(MainActivity.this, "Game Over! Your score: " + score, Toast.LENGTH_LONG).show();
                    // Reset the game after a short delay
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetGame();
                        }
                    }, 2000); // 2-second delay before resetting
                }
            }.start();
        }
    }

    private void updateTimer() {

        int seconds = (int) (timeLeftInMillis / 1000);
        timerText.setText("" + seconds);

    }

    private void resetGame() {
        score = 0;
        timeLeftInMillis = 30000; // Reset to 30 seconds
        timerStarted = false; // Reset the timer flag
        // colorButton.setText("Tap Me!");
        updateScore();
        updateTimer();
        colorButton.setEnabled(true); // Re-enable the button
        changeColor(); // Change the color for the new match
        startTimer(); // Start the timer again
    }


    private void updateScore() {
        floatingText.setText(" +1 Point ");
        scoreText.setText("Score: " + score);
        // Check and update high score
        if (score > highScore) {
            highScore = score;
            updateHighScore();
            saveHighScore();
        }
    }

    private void updateScore(int increment) {
        score += increment;

        scoreText.setText("Score: " + score);
        // Check and update high score
        if (score > highScore) {
            highScore = score;
            updateHighScore();
            saveHighScore();
        }
    }


    private void gameOver() {

        colorButton.setEnabled(false);
        countDownTimer.cancel();
        timerStarted = false;
        dropArea.setVisibility(View.INVISIBLE);
        colorButton.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        //floatingText.setText("Game Over!");
        floatText("Game Over! Your score: " + score , "0");
        // colorButton.setBackgroundColor(Color.WHITE);
        Toast.makeText(MainActivity.this, "Game Over! You tapped red! Your score: " + score, Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resetGame();
                soundPool.play(endGameSound, 1, 1, 0, 0, 1);
            }
        }, 2000); // 2-second delay before resetting
    }

    private void updateHighScore() {
        highScoreText.setText("High Score: " + highScore);
    }

    private void changeColor() {
        int randomValue = random.nextInt(100); // Random value from 0 to 99
        if (timerStarted && randomValue < 10) { // 10% chance to be red
            currentColor = Color.RED;

            tv_message.setTextColor(currentColor);
            //floatText("Long press and drag the pizza to the plate", "0");
            colorButton.clearColorFilter();
            int transparentColor = Color.argb(0, 255, 255, 255); // Fully transparent
            colorButton.setColorFilter(transparentColor, PorterDuff.Mode.SRC_IN);
            colorButton.setBackgroundResource(R.drawable.pizza);
            tv_message.setText("Long press and drag the pizza to the plate");
            // colorButton.setText("Throw Me!");// Set to red
            dropArea.setVisibility(View.VISIBLE);
        } else {
            currentColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)); // Generate other colors
            dropArea.setVisibility(View.INVISIBLE);
            colorButton.clearColorFilter();
            tv_message.setTextColor(currentColor);
            colorButton.setBackground(getDrawable(R.drawable.raw));
            colorButton.setColorFilter(currentColor, PorterDuff.Mode.SRC_IN);
        }

    }

    private void saveHighScore() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("HighScore", highScore);
        editor.apply(); // Save the high score
    }


    @Override
    public boolean onDrag(View layoutview, DragEvent dragevent) {
        int action = dragevent.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                Log.d(Constants.TAG, "Drag event started");
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                //updateRegionRects();
                if (layoutview instanceof TextView) {
                    //highlightTargetView((TextView) layoutview, true);
                }
                // quadrantView.setVisibility(View.VISIBLE);
                Log.d(Constants.TAG, "Drag event entered into " + layoutview.toString());
                break;

            case DragEvent.ACTION_DRAG_LOCATION:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                if (layoutview instanceof View) {
                    //  highlightTargetView((TextView) layoutview, false);
                } else {
                    // quadrantView.setVisibility(View.INVISIBLE);
                }
                Log.d(Constants.TAG, "Drag event exited from " + layoutview.toString());
                break;
            case DragEvent.ACTION_DROP:
                if (layoutview instanceof View) {
                    //highlightTargetView((TextView) layoutview, false);
                    // colorButton.setText("Tap Me!");
                    soundPool.play(earnCoinSound, 1, 1, 0, 0, 1);
                    dropArea.setVisibility(View.INVISIBLE);
                    floatText(displayRandomWord(), "10");
                    updateScore(10); // Increase score by 10
                    changeColor();
                }
                // quadrantView.setVisibility(View.VISIBLE);
                Log.d(Constants.TAG, "Dropped");
                //Do increase score and continue...

                break;
            case DragEvent.ACTION_DRAG_ENDED:
                if (layoutview instanceof View) {
                    // highlightTargetView((TextView) layoutview, false);
                } else {
                    //quadrantView.setVisibility(View.INVISIBLE);
                }
                Log.d(Constants.TAG, "Drag ended");
                break;
            default:
                break;
        }
        return true;
    }

    private String displayRandomWord() {
        // Pick a random word from the ArrayList
        String randomWord = "";
        Random random = new Random();
        int randomIndex = random.nextInt(words.size());
        randomWord = words.get(randomIndex);

        // Display the word in the TextView
        return randomWord;
    }
}