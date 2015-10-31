package br.unb.integration_project.driftkartapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private PrepareDeviceCommunication prepareDeviceCommunication;
    private AlertDialog searchDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
