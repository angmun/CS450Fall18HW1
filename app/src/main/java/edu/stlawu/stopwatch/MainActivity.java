// Angelica Munyao
// HW 1: A Stopwatch Application

package edu.stlawu.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Define a class that extends the TimerTask abstract class for use with a timer object to
    // schedule stopwatch time updates:
    // Deci-seconds counter
    class Counter extends TimerTask{
        // Instance variable:
        // Keeping track of the time count value
        private int count = 0;

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Update the count value on the respective views
                            MainActivity.this.tv_min.setText(String.format(Locale.getDefault(), "%02d" ,count/600));
                            MainActivity.this.tv_sec.setText(String.format(Locale.getDefault(), "%02d" ,(count/10)%60));
                            MainActivity.this.tv_dsec.setText(String.format(Locale.getDefault(), "%d" ,count%10));

                            //Increment count value
                            count = count + 1;
                        }
                    }
            );
        }
    }



    // Declare variables for the various views:
    // TextView objects for displaying time elapsed
    private TextView tv_min = null;
    private TextView tv_sec = null;
    private TextView tv_dsec = null;

    // Button objects for stopwatch controls
    private Button bt_start = null;
    private Button bt_stop = null;
    private Button bt_resume = null;
    private Button bt_reset = null;

    // Declare the view group object for displaying time elapsed
    private LinearLayout ll_display = null;

    // Declare the variable for a timer object required for updating the displayed time elapsed
    private Timer t = null;

    // Declare a counter object to update the displayed time values
    private Counter ctr = null;

    // Declare variables for including a sound to the reset button when clicked
    private AudioAttributes aattr = null;
    private SoundPool soundPool = null;
    private int bloopSound = 0;



    // We initialize views and set behaviors for user interaction with the application when the
    // activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views:
        // Text views:
        this.tv_min = findViewById(R.id.tv_min);
        this.tv_sec = findViewById(R.id.tv_sec);
        this.tv_dsec = findViewById(R.id.tv_dsec);

        // Buttons:
        this.bt_start = findViewById(R.id.bt_start);
        this.bt_stop = findViewById(R.id.bt_stop);
        this.bt_resume = findViewById(R.id.bt_resume);
        this.bt_reset = findViewById(R.id.bt_reset);

        // Initialize the view group for use in visual effects
        this.ll_display = findViewById(R.id.ll_display);

        // Initialize a sound to be linked to the reset button
        this.aattr = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
        this.soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aattr).build();
        this.bloopSound = this.soundPool.load(this, R.raw.bloop, 1);



        // Initial state of the buttons:
        // At first, stop and resume are disabled as they are not required
        bt_stop.setEnabled(false);
        bt_resume.setEnabled(false);


        //Set behaviors for the button views when clicked:
        // Start
        this.bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the start button
                bt_start.setEnabled(false);

                // Enable the stop button
                bt_stop.setEnabled(true);

                // Begin the counter
                t.scheduleAtFixedRate(ctr, 0, 100);
            }
        });


        // Stop
        this.bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the stop button
                bt_stop.setEnabled(false);

                // Enable the resume button
                bt_resume.setEnabled(true);

                // Stop the counters
                t.cancel();

                // Capture the time elapsed value
                int dsecs = ctr.count;

                // Re-initialize the timer as well as the counter
                t = new Timer();
                ctr = new Counter();


                // Reload the value captured before the timer was reset
                ctr.count = dsecs;

            }
        });


        // Resume
        this.bt_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the resume button
                bt_resume.setEnabled(false);

                // Enable the stop button
                bt_stop.setEnabled(true);

                // Begin the counter with a delay to keep the rate of time elapsed updating consistent;
                t.scheduleAtFixedRate(ctr, 100, 100);
            }
        });


        // Reset
        this.bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enable the start button
                bt_start.setEnabled(true);

                // Disable the resume and stop buttons
                bt_stop.setEnabled(false);
                bt_resume.setEnabled(false);

                // Reset the display
                tv_min.setText(R.string.min);
                tv_sec.setText(R.string.sec);
                tv_dsec.setText(R.string.dsec);

                // Stop the counter and reinitialize it as well as the counters
                t.cancel();
                t = new Timer();
                ctr = new Counter();


                // Play the bloop sound
                soundPool.play(bloopSound, 1,1,1,0,1);

                // Cause an animation of the time display
                Animator anim = AnimatorInflater.loadAnimator(MainActivity.this, R.animator.counter);
                anim.setTarget(ll_display);
                anim.start();
            }
        });

    }



    // Load the saved time elapsed value from a previous run when the application starts
    @Override
    protected void onStart() {
        super.onStart();

        // Reload the time elapsed from a previous run, and if it is the first time running, start at 0
        int count = getPreferences(MODE_PRIVATE).getInt("count", 0);

        // Set the text views to display the time elapsed accordingly
        this.tv_min.setText(String.format(Locale.getDefault(), "%02d" ,count/600));
        this.tv_sec.setText(String.format(Locale.getDefault(), "%02d" ,(count/10)%60));
        this.tv_dsec.setText(String.format(Locale.getDefault(), "%d" ,count%10));

        // Initialize the timer and counter timer task
        t = new Timer();
        ctr = new Counter();

        // Have the timer task's count begin at the right time elapsed
        this.ctr.count = count;

    }

    // Preserve the time elapsed when the application is halted
    @Override
    protected void onStop() {
        super.onStop();

        // Save the value of the time elapsed at the moment of process halt
        // NB: There is a delay which results in the last saved time-elapsed value being greater by some deci-seconds
        getPreferences(MODE_PRIVATE).edit().putInt("count", ctr.count).apply();
    }
}
