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
    private SensorDataHanddling sensorHanddling;
    private AlertDialog searchDialog;
    private TextView timerTextView;
    private TextView speedTextView;
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

    @Override
    protected void onDestroy() {
        //TODO: Destroy all BT allocated resources.
        super.onDestroy();
        prepareDeviceCommunication.closeAllBlutoothResources();
        if(timer != null) {
            timer.cancel();
            timer.purge();
        }
        if(sensorHanddling != null) {
            sensorHanddling.stopLoopedReadData();
        }
    }

    public void setSensorHanddling(SensorDataHanddling pSensorHanddling) {
        sensorHanddling = pSensorHanddling;
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
        dialogBuilder.setMessage("Fazendo busca...");
        searchDialog = dialogBuilder.create();
        searchDialog.show();
    }

    public void dismissSearchDialog() {
        searchDialog.dismiss();
    }

    public void showConnTryAgainDialog(DialogInterface.OnClickListener pDialogListner) {
        AlertDialog.Builder btAlertBuilder = new AlertDialog.Builder(this);
        btAlertBuilder.setTitle("Sem conexão com o Kart.");
        btAlertBuilder.setPositiveButton("Tentar denovo", pDialogListner);
        btAlertBuilder.setNegativeButton("Sair", pDialogListner);

        AlertDialog alertDialog = btAlertBuilder.create();
        alertDialog.show();
    }

    public void showLongToastDialog(String pMessage) {
        Toast.makeText(this, pMessage, Toast.LENGTH_LONG).show();
    }

    public void showEnableBluetoothDialog(String pIntentAction) {
        Intent intent = new Intent(pIntentAction);
        startActivityForResult(intent, 1);
    }
}
