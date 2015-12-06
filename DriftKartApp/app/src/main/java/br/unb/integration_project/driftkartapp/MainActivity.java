package br.unb.integration_project.driftkartapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private int phoneLastBattCharge = -1;
    private int lastBatteryCharge = -1;
    private ImageView phoneBattImageView;
    private TextView phoneBattValueTextView;
    private ImageView batteryImageView;
    private TextView batteryValueTextView;
    private ProgressBar batteryProgressBar;
    private Calendar timerCalendar = Calendar.getInstance();
    private String incrementedFmtTime;
    private Handler uiHandler;
    private Timer timer;
    private TimerTask incrementTimerTask;
    private ImageView timerImageView;
    private Runnable hideUiRunnable;
    private boolean isTimerStarted = false;
    private long pausedTime = 0;
    private long txtViewSettedTime = 0;
    private View.OnClickListener checkBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox checkBoxView  = (CheckBox)view;
            //If Checkbox is not checked, isChecked() returns true.
            if(checkBoxView.isChecked()) {
                if(checkBoxView.getId() == R.id.economicCheckBox) {
                    prepareDeviceCommunication.getDataFlowHandling().setEngineMode((byte)0);
                    ((CheckBox)findViewById(R.id.performanceCheckBox)).setChecked(false);
                }else {
                    prepareDeviceCommunication.getDataFlowHandling().setEngineMode((byte)1);
                    ((CheckBox)findViewById(R.id.economicCheckBox)).setChecked(false);
                }
                checkBoxView.setChecked(true);
            }else {
                checkBoxView.setChecked(true);
            }
        }
    };
    private View.OnClickListener timerImgListener = new View.OnClickListener() {
        @Override
        public void onClick(View timerImgView) {
            if(isTimerStarted) {
                pausedTime = System.currentTimeMillis();
                incrementTimerTask.cancel();
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
    private View.OnLongClickListener timerTxtLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if(!isTimerStarted) {
                Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(80);
                timerCalendar.clear();
                timerTextView.setText("00:00:00");
                return true;
            }else {
                return false;
            }
        }
    };
    private BroadcastReceiver phoneBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if(intentAction.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int currentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                setBattery(currentBatteryLevel, false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new Handler(getMainLooper());
        timerCalendar.clear();
        phoneBattImageView = (ImageView)findViewById(R.id.phoneBattImageView);
        phoneBattValueTextView = (TextView)findViewById(R.id.phoneBattValueTextView);
        batteryImageView = (ImageView)findViewById(R.id.batteryImageView);
        batteryValueTextView = (TextView)findViewById(R.id.batteryValueTextView);
        batteryProgressBar = (ProgressBar)findViewById(R.id.batteryProgressBar);
        timerImageView = (ImageView)findViewById(R.id.timerImageView);
        timerImageView.setOnClickListener(timerImgListener);
        timerTextView = (TextView)findViewById(R.id.timerTextView);
        timerTextView.setOnLongClickListener(timerTxtLongListener);
        speedTextView = (TextView)findViewById(R.id.speedometerTextView);
        CheckBox ecoCheckbox = (CheckBox)findViewById(R.id.economicCheckBox);
        CheckBox perfCheckbox = (CheckBox)findViewById(R.id.performanceCheckBox);
        ecoCheckbox.setOnClickListener(checkBoxListener);
        perfCheckbox.setOnClickListener(checkBoxListener);

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(phoneBatteryReceiver, iFilter);

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

    private void hideNavStatusBar() {
        final View decorView = getWindow().getDecorView();
        final int HIDE_NAV_STATUS_BAR = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    if(hideUiRunnable == null) {
                        hideUiRunnable = new Runnable() {
                            @Override
                            public void run() {
                                decorView.setSystemUiVisibility(HIDE_NAV_STATUS_BAR);
                            }
                        };
                    }
                    uiHandler.postDelayed(hideUiRunnable, 5000);
                }
            }
        });
        decorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                uiHandler.removeCallbacks(hideUiRunnable);
                uiHandler.postDelayed(hideUiRunnable, 5000);
                return false;
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
        unregisterReceiver(phoneBatteryReceiver);
    }

    public void setSpeed(int pSpeed) {
        speedTextView.setText(String.valueOf(pSpeed));
    }

    private TimerTask getNewIncrementTimerTask() {
        return new TimerTask() {
            Runnable setTimerValueTask = new Runnable() {
                @Override
                public void run() {
                    txtViewSettedTime = System.currentTimeMillis();
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
    }

    private void startTimer() {
        incrementTimerTask = getNewIncrementTimerTask();
        timer = new Timer();
        if(pausedTime != 0) {
            long delayTime = 1000 - (pausedTime - txtViewSettedTime);
            try {
                timer.schedule(incrementTimerTask, delayTime, 1000);
            }catch (IllegalArgumentException iae) {
                timer.schedule(incrementTimerTask, 0, 1000);
            }
            pausedTime = 0;
        }else {
            timer.schedule(incrementTimerTask, 0, 1000);
        }
    }

    public void showSearchDialog() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View progressBarView = inflater.inflate(R.layout.search_progress_bar, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(progressBarView);
        searchDialog = dialogBuilder.create();
        searchDialog.show();
    }

    public void dismissSearchDialog() {
        searchDialog.dismiss();
        hideNavStatusBar();
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

    public void setBattery(int pBatteryCharge, boolean pIsKartBattery) {
        int batteryLayout, phoneBattLayout, currentBatteryCharge;
        int textColor = R.color.battery_high;

        if(90 < pBatteryCharge && pBatteryCharge <=100) {
            batteryLayout = R.drawable.battery_100;
            phoneBattLayout = R.drawable.phone_battery_100;
            currentBatteryCharge = 100;
        }else if(80 < pBatteryCharge && pBatteryCharge <=90) {
            batteryLayout = R.drawable.battery_90;
            phoneBattLayout = R.drawable.phone_battery_90;
            currentBatteryCharge = 90;
        }else if(60 < pBatteryCharge && pBatteryCharge <=80) {
            batteryLayout = R.drawable.battery_80;
            phoneBattLayout = R.drawable.phone_battery_80;
            currentBatteryCharge = 80;
        }else if(50 < pBatteryCharge && pBatteryCharge <=60) {
            batteryLayout = R.drawable.battery_60;
            phoneBattLayout = R.drawable.phone_battery_60;
            currentBatteryCharge = 60;
        }else if(30 < pBatteryCharge && pBatteryCharge <=50) {
            batteryLayout = R.drawable.battery_50;
            phoneBattLayout = R.drawable.phone_battery_50;
            currentBatteryCharge = 50;
        }else if(20 < pBatteryCharge && pBatteryCharge <=30) {
            batteryLayout = R.drawable.battery_30;
            phoneBattLayout = R.drawable.phone_battery_30;
            currentBatteryCharge = 30;
        }else if(10 < pBatteryCharge && pBatteryCharge <=20) {
            batteryLayout = R.drawable.battery_20;
            phoneBattLayout = R.drawable.phone_battery_20;
            currentBatteryCharge = 20;
            textColor = R.color.battery_low;
        }else {
            batteryLayout = R.drawable.battery_10;
            phoneBattLayout = R.drawable.phone_battery_10;
            currentBatteryCharge = 10;
            textColor = R.color.battery_low;
        }

        if(pIsKartBattery) {
            if (batteryProgressBar.getVisibility() == View.VISIBLE) {
                batteryProgressBar.setVisibility(View.GONE);
            }
            setBatteryLayout(pBatteryCharge, lastBatteryCharge, currentBatteryCharge, batteryLayout,
                    batteryImageView, batteryValueTextView, textColor);
        }else {
            setBatteryLayout(pBatteryCharge, phoneLastBattCharge, currentBatteryCharge, phoneBattLayout,
                    phoneBattImageView, phoneBattValueTextView, textColor);
        }
    }

    private void setBatteryLayout(int pBatteryValue, int pBattLastCharge, int pBattCurrentCharge,
                                 int pBattImage, ImageView pBattImgView, TextView pBattTxtView,
                                 int pBattValueColor) {
        if (pBattLastCharge != pBattCurrentCharge) {
            pBattImgView.setImageResource(pBattImage);
            pBattLastCharge = pBattCurrentCharge;
            pBattTxtView.setTextColor(getResources().getColor(pBattValueColor));
        }
        String batteryPercentage = String.valueOf(pBatteryValue) + "%";
        pBattTxtView.setText(batteryPercentage);
    }

    public void showEnableBluetoothDialog(String pIntentAction) {
        Intent intent = new Intent(pIntentAction);
        startActivityForResult(intent, 1);
    }
}
