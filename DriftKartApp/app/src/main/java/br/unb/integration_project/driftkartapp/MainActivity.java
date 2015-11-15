package br.unb.integration_project.driftkartapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private PrepareDeviceCommunication prepareDeviceCommunication;
    private AlertDialog searchDialog;
    private TextView timerTextView;
    private TextView speedTextView;
    private int lastBatteryCharge = -1;
    private ImageView batteryImageView;
    private TextView batteryValueTextView;
    private Calendar timerCalendar = Calendar.getInstance();
    private String incrementedFmtTime;
    private Timer timer;
    private ImageView timerImageView;
    private boolean isTimerStarted = false;
    private long lastExecutionTime = 0;
    private long incrementTime = 0;
    private View.OnClickListener timerImgListener = new View.OnClickListener() {
        @Override
        public void onClick(View timerImgView) {
            if(isTimerStarted) {
                lastExecutionTime = System.currentTimeMillis();
                timer.cancel();
                timer.purge();
                isTimerStarted = false;
                timerImageView.setImageResource(R.drawable.timer_press_state);
            }else {
                isTimerStarted = true;
                startTimer();
                timerImageView.setImageResource(R.drawable.timer_started_press_state);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerCalendar.clear();
        batteryImageView = (ImageView)findViewById(R.id.batteryImageView);
        batteryValueTextView = (TextView)findViewById(R.id.batteryValueTextView);
        timerImageView = (ImageView)findViewById(R.id.timerImageView);
        timerImageView.setOnClickListener(timerImgListener);
        timerTextView = (TextView)findViewById(R.id.timerTextView);
        speedTextView = (TextView)findViewById(R.id.speedometerTextView);

        prepareDeviceCommunication = new PrepareDeviceCommunication(this);
        prepareDeviceCommunication.establishBluetoothConnection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the user accepts to enable bluetooth.
        if(resultCode == RESULT_OK) {
            prepareDeviceCommunication.establishBluetoothConnection();
        } else {
            finish();
        }
    }

    public void hideNavBar() {
        final View decorView = getWindow().getDecorView();
        final int HIDE_NAV_STATUS_BAR = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    Runnable hideUiRunnable = new Runnable() {
                        @Override
                        public void run() {
                            decorView.setSystemUiVisibility(HIDE_NAV_STATUS_BAR);
                        }
                    };
                    new Handler(getMainLooper()).postDelayed(hideUiRunnable, 1000);
                }
            }
        });
        decorView.setSystemUiVisibility(HIDE_NAV_STATUS_BAR);
    }

    @Override
    protected void onDestroy() {
        //TODO: Destroy all BT allocated resources.
        super.onDestroy();
        prepareDeviceCommunication.closeBluetoothResources();
        if(timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public void setSpeed(String pSpeed) {
        speedTextView.setText(pSpeed);
    }

    public void startTimer() {
        TimerTask incrementTimerTask = new TimerTask() {
            Handler uiHandler = new Handler(Looper.getMainLooper());
            Runnable setTimerValueTask = new Runnable() {
                @Override
                public void run() {
                    incrementTime = System.currentTimeMillis();
                    timerTextView.setText(incrementedFmtTime);
                }
            };

            @Override
            public void run() {
                timerCalendar.add(Calendar.SECOND, 1);
                Date incrementDate = timerCalendar.getTime();
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                incrementedFmtTime = formatter.format(incrementDate);
                uiHandler.post(setTimerValueTask);
            }
        };

        timer = new Timer();
        if(lastExecutionTime != 0) {
            long delayTime = 1000 - (lastExecutionTime - incrementTime);
            timer.schedule(incrementTimerTask, delayTime, 1000);
            lastExecutionTime = 0;
        }else {
            timer.schedule(incrementTimerTask, 0, 1000);
        }
    }

    public void showSearchDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Estabelecendo Conexão...");
        searchDialog = dialogBuilder.create();
        searchDialog.show();
    }

    public void dismissSearchDialog() {
        searchDialog.dismiss();
        hideNavBar();
    }

    public void showConnTryAgainDialog() {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonValue) {
                switch (buttonValue) {
                    case DialogInterface.BUTTON_POSITIVE:
                        prepareDeviceCommunication.establishBluetoothConnection();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder btAlertBuilder = new AlertDialog.Builder(this);
        btAlertBuilder.setTitle("Sem conexão com o Kart.");
        btAlertBuilder.setPositiveButton("Tentar denovo", dialogListener);
        btAlertBuilder.setNegativeButton("Sair", dialogListener);

        AlertDialog alertDialog = btAlertBuilder.create();
        alertDialog.show();
    }

    public void showLongToastDialog(String pMessage) {
        Toast.makeText(this, pMessage, Toast.LENGTH_LONG).show();
    }

    public void setBattery(int pBatteryCharge) {
        //TODO: XML layout cannot have android:src image setted.
        //TODO: Set the real image reference on "batteryLayout".
        int batteryLayout, currentBatteryCharge = 0;
        if(90 < pBatteryCharge && pBatteryCharge <=100) {
            //batteryLayout = 100;
            currentBatteryCharge = 100;
        }else if(80 < pBatteryCharge && pBatteryCharge <=90) {
            //batteryLayout = 90;
            currentBatteryCharge = 90;
        }else if(60 < pBatteryCharge && pBatteryCharge <=80) {
            //batteryLayout = 80;
            currentBatteryCharge = 80;
        }else if(50 < pBatteryCharge && pBatteryCharge <=60) {
            //batteryLayout = 60;
            currentBatteryCharge = 60;
        }else if(30 < pBatteryCharge && pBatteryCharge <=50) {
            //batteryLayout = 50;
            currentBatteryCharge = 50;
        }else if(20 < pBatteryCharge && pBatteryCharge <=30) {
            //batteryLayout = 30;
            currentBatteryCharge = 30;
        }else if(10 < pBatteryCharge && pBatteryCharge <=20) {
            //batteryLayout = 20;
            currentBatteryCharge = 20;
        }else if(pBatteryCharge <=10) {
            //batteryLayout = 10;
            currentBatteryCharge = 10;
        }

        if(lastBatteryCharge != currentBatteryCharge) {
         //   batteryImageView.setImageResource(batteryLayout);
            lastBatteryCharge = currentBatteryCharge;
        }
        batteryValueTextView.setText(String.valueOf(pBatteryCharge));
    }

    public void showEnableBluetoothDialog(String pIntentAction) {
        Intent intent = new Intent(pIntentAction);
        startActivityForResult(intent, 1);
    }
}
