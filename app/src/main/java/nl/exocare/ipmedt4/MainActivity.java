package nl.exocare.ipmedt4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ViewFlipper;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Resources;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import static nl.exocare.ipmedt4.R.layout.activity_revalidatie;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private long timeCountInMilliSeconds = 1 * 60000;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    private ProgressBar progressBarCircle, progressBarCircle2, tijdlijn;
    private EditText editTextMinute;
    private TextView textViewTime, beginDatum, eindDatum, controleDatum, revalidatieDatum, textViewTime2, datumGipsBehandeling, datumControleBehandeling;
    private ImageView imageViewReset, imageViewStartStop, image;
    private CountDownTimer countDownTimer;
    TimelineHandler timeline = null;

    private ViewFlipper includeChange;

    //database???
    private String jouwBreuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // method call to initialize the views
        initViews();
        // method call to initialize the listeners
        initListeners();

        //datums ophalen
        try {
            timeline = new TimelineHandler();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //de bottom navigatie listener
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //hoogte en breedte van het scherm bepalen en tijdlijn vullen
        ViewTreeObserver observer = includeChange.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                fillTimeline();
                includeChange.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);
            }
        });

        //Behandelingspagina datums goed zetten
        fillBehandelingPagina();
    }

    /**
     * het veranderen van de views als er op de bottom navigatie wordt geklikt
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //[username]
                    includeChange.setDisplayedChild(0);
                    return true;
                case R.id.navigation_dashboard:
                    //Behandeling
                    includeChange.setDisplayedChild(1);
                    return true;
                case R.id.navigation_notifications:
                    //Revalidatie
                    includeChange.setDisplayedChild(2);
                    return true;
            }
            return false;
        }

    };

    /**
     * method to initialize the views
     */
    private void initViews() {
        datumGipsBehandeling = (TextView) findViewById(R.id.datum_gips);
        datumControleBehandeling = (TextView) findViewById(R.id.datum_controle);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        progressBarCircle2 = (ProgressBar) findViewById(R.id.progressBarCircle_gips);
        editTextMinute = (EditText) findViewById(R.id.editTextMinute);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewTime2 = (TextView) findViewById(R.id.textViewTime_gips);
        imageViewReset = (ImageView) findViewById(R.id.imageViewReset);
        imageViewStartStop = (ImageView) findViewById(R.id.imageViewStartStop);
        includeChange = (ViewFlipper)findViewById(R.id.vf);
        beginDatum = (TextView) findViewById(R.id.beginDatum);
        eindDatum = (TextView) findViewById(R.id.eindDatum);
        controleDatum = (TextView) findViewById(R.id.controleDatum);
        revalidatieDatum = (TextView) findViewById(R.id.revalidatieDatum);
        tijdlijn = (ProgressBar) findViewById(R.id.vertical_progressbar);
    }

    /**
     * method to initialize the click listeners
     */
    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewReset:
                reset();
                break;
            case R.id.imageViewStartStop:
                startStop();
                break;
            case R.id.buttonFaq:
                Intent intent1 = new Intent(MainActivity.this, FaqActivity.class);
                startActivity(intent1);
                break;
            case R.id.buttonStrek:
                Intent intent2 = new Intent(MainActivity.this, RekStrekActivity.class);
                startActivity(intent2);
                break;
            case R.id.buttonKracht:
                Intent intent3 = new Intent(MainActivity.this, KrachtActivity.class);
                startActivity(intent3);
                break;
        }
    }

    /**
     * method to reset count down timer
     */
    private void reset() {
        stopCountDownTimer();
        startCountDownTimer();
    }


    /**
     * method to start and stop count down timer
     */
    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {

            // call to initialize the timer values
            setTimerValues();
            // call to initialize the progress bar values
            setProgressBarValues();
            // showing the reset icon
            imageViewReset.setVisibility(View.VISIBLE);
            // changing play icon to stop icon
            imageViewStartStop.setImageResource(R.drawable.icon_stop);
            // making edit text not editable
            editTextMinute.setEnabled(false);
            // changing the timer status to started
            timerStatus = TimerStatus.STARTED;
            // call to start the count down timer
            startCountDownTimer();

        } else {

            // hiding the reset icon
            imageViewReset.setVisibility(View.GONE);
            // changing stop icon to start icon
            imageViewStartStop.setImageResource(R.drawable.icon_start);
            // making edit text editable
            editTextMinute.setEnabled(true);
            // changing the timer status to stopped
            timerStatus = TimerStatus.STOPPED;
            stopCountDownTimer();

        }

    }

    /**
     * method to initialize the values for count down timer
     */
    private void setTimerValues() {
        int time = 0;
        if (!editTextMinute.getText().toString().isEmpty()) {
            // fetching value from edit text and type cast to integer
            time = Integer.parseInt(editTextMinute.getText().toString().trim());
            //uit de timelinehandler halen
//            time = 0;
//            try {
//                time = (int) timeline.getTrajectduur(timeline.getCurrentTime(), timeline.getControleDatum());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
        } else {
            // toast message to fill edit text

        }
        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = time * 60 * 1000;
    }

    /**
     * method to start count down timer
     */
    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                textViewTime2.setText(hmsTimeFormatter(millisUntilFinished));

                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
                progressBarCircle2.setProgress((int) (millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {

                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                textViewTime2.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                // call to initialize the progress bar values
                setProgressBarValues();
                // hiding the reset icon
                imageViewReset.setVisibility(View.GONE);
                // changing stop icon to start icon
                imageViewStartStop.setImageResource(R.drawable.icon_start);
                // making edit text editable
                editTextMinute.setEnabled(true);
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
            }

        }.start();
        countDownTimer.start();
    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
        progressBarCircle2.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle2.setProgress((int) timeCountInMilliSeconds / 1000);
    }


    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02du\n%02dm",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)));
        return hms;


    }

    /**
     * Deze methode vult de tijdlijn en zorgt voor de format
     */
    private void fillTimeline() {
        //set de datums van begin en eind goed
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        beginDatum.setText(df.format(timeline.getBeginDatum()));
        eindDatum.setText(df.format(timeline.getEindDatum()));
        controleDatum.setText(df.format(timeline.getControleDatum()));
        revalidatieDatum.setText(df.format(timeline.getRevalidatieDatum()));

        int trajectDuur = 0;
        try {
            //haal de duur van het herstellen op
            trajectDuur = (int) timeline.getTrajectduur(timeline.getBeginDatum(), timeline.getEindDatum());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //de tijd die de patient al bezig is
        int verstrekenTijd = (int) timeline.getVerstrekentijd();

        //zorg dat tijdlijn progress juist is
        tijdlijn.setMax(trajectDuur);
        tijdlijn.setProgress(verstrekenTijd);

        //zet de controle en begin revalidatie afspraken op de juiste hoogte in de tijdlijn
        ConstraintLayout controleLayout = (ConstraintLayout) findViewById(R.id.controleLayout);
        ConstraintLayout revalidatieLayout = (ConstraintLayout) findViewById(R.id.revalidatieLayout);
        ConstraintLayout hoofdLayout = (ConstraintLayout) findViewById(R.id.hoofdLayout);
        //voor het toevoegen van de controleafspraak
        int procentPerPixel = includeChange.getHeight() / trajectDuur;
        int afstandControle = 0;
        int afstandRevalidatie = 0;
        int afstandHoofd = 0;
        try {
            afstandControle = procentPerPixel * (int) (timeline.getTrajectduur(timeline.getBeginDatum(), timeline.getControleDatum()));
            afstandRevalidatie = procentPerPixel * (int) (timeline.getTrajectduur(timeline.getBeginDatum(), timeline.getRevalidatieDatum()));
            afstandHoofd = procentPerPixel * (int) (timeline.getTrajectduur(timeline.getBeginDatum(), timeline.getCurrentTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ConstraintLayout.LayoutParams controleParams = (ConstraintLayout.LayoutParams) controleLayout.getLayoutParams();
        controleParams.topMargin = afstandControle;
        ConstraintLayout.LayoutParams revalidatieParams = (ConstraintLayout.LayoutParams) revalidatieLayout.getLayoutParams();
        revalidatieParams.topMargin = afstandRevalidatie;

        //hoofdafstand
        ConstraintLayout.LayoutParams hoofdParams = (ConstraintLayout.LayoutParams) hoofdLayout.getLayoutParams();
        hoofdParams.topMargin = afstandHoofd;
    }

    /**
     * het goedzetten van de datums op de behandelingspagina
     */
    public void fillBehandelingPagina() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        datumGipsBehandeling.setText(df.format(timeline.getBeginDatum()));
        datumControleBehandeling.setText(df.format(timeline.getControleDatum()));
    }

    /**
     * methode als er op het poppetje gedrukt wordt
     *
     * @param v
     */
    public void imageClick(View v) {
        image = (ImageView) findViewById(v.getId());
        image.setDrawingCacheEnabled(true);
        image.setOnTouchListener(changeColorListener);
    }

    /**
     * on touch listener voor het vinden van de kleuren en het aangeklikte bot
     *
     */
    private final View.OnTouchListener changeColorListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(final View v, MotionEvent ev) {

            final int action = ev.getAction();
            // get coords van touch
            final int evX = (int) ev.getX();
            final int evY = (int) ev.getY();

            ImageView imageView = (ImageView) findViewById(R.id.botzien);

            switch (action) {
                case MotionEvent.ACTION_UP :
                    int touchColor = getHotspotColor(R.id.botzienAreas, evX, evY);
                    ColorTool ct = new ColorTool();
                    int tolerance = 25;
                    if (ct.closeMatch (Color.RED, touchColor, tolerance)) {
                        jouwBreuk = "bovenbeen";
                        imageView.setImageResource(R.drawable.botzienbovenbeen);
                    } else if (ct.closeMatch (Color.BLUE, touchColor, tolerance)) {
                        jouwBreuk = "knie";
                        imageView.setImageResource(R.drawable.botzienknie);
                    } else if (ct.closeMatch (Color.GREEN, touchColor, tolerance)) {
                        jouwBreuk = "onderbeen";
                        imageView.setImageResource(R.drawable.botzienonderbeen);
                    } else if (ct.closeMatch (Color.YELLOW, touchColor, tolerance)) {
                        jouwBreuk = "voet";
                        imageView.setImageResource(R.drawable.botzienvoet);
                    }
                    break;
            } // end switch
            return true;
        }
    };

    /**
     * het opzoeken van welke kleur aangeraakt wordt
     *
     * @param hotspotId
     * @param x
     * @param y
     * @return
     */
    public int getHotspotColor (int hotspotId, int x, int y) {
        ImageView img = (ImageView) findViewById (hotspotId);
        img.setDrawingCacheEnabled(true);
        Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
        img.setDrawingCacheEnabled(false);
        return hotspots.getPixel(x, y);
    }
}
