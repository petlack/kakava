package kavovar.petlack.eu.kavovar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends AppCompatActivity implements PairedDevicesAdapter.IPairedDeviceCallbacks {

    private static final int REQUEST_CHOOSE_DEVICE = 0;
    private static final String TAG = "MainActivity";

    private PairedDevicesAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothChatService chatService;
    private byte writeData = 0;
    private BluetoothDevice pairedDevice = null;

    SmoothProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (SmoothProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        chatService = new BluetoothChatService(this, new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //Log.d(TAG, msg.toString());
                if (msg.what == BluetoothChat.MESSAGE_STATE_CHANGE) {
                    if (msg.arg1 == BluetoothChatService.STATE_CONNECTED) {
                        adapter.setConnectedDevice(pairedDevice);
                        progressBar.progressiveStop();
                        progressBar.setVisibility(View.GONE);
                    }
                    else if (msg.arg1 == BluetoothChatService.STATE_FAILED) {
                        Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_LONG).show();
                        progressBar.progressiveStop();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, ListDevicesActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_DEVICE);
            }
        });

        ListView list = (ListView) findViewById(R.id.list);
        adapter = new PairedDevicesAdapter(this, new ArrayList<BluetoothDevice>(), this);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        adapter.clear();
        adapter.addAll(pairedDevices);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Log.d(TAG, "bound");
        try {
            if (chatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                Log.d(TAG, "connected");
            }
            else if (chatService.getState() == BluetoothChatService.STATE_NONE) {
                Log.d(TAG, "not connected");
                pairedDevice = device;
                chatService.connect(device);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.progressiveStart();
            }
            else {
                Log.d(TAG, "dont know " + chatService.getState());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect(BluetoothDevice device) {
        adapter.setConnectedDevice(null);
        pairedDevice = null;
        chatService.stop();
    }

    @Override
    public void onCommandOn(BluetoothDevice device) {
        chatService.write(new byte[]{1, 0});
    }

    @Override
    public void onCommandOff(BluetoothDevice device) {
        chatService.write(new byte[]{0, 0});
    }

    @Override
    public void onUnpair(BluetoothDevice device) {
        Utilities.unpairDevice(device);
        reload();
    }

    @Override
    public void onAlarm(BluetoothDevice device) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.alarm_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        userInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String value = userInput.getText().toString();
                                try {
                                    byte byteVal = Byte.parseByte(value);
                                    chatService.write(new byte[]{1, byteVal});
                                    Log.d(TAG, userInput.getText().toString());
                                }
                                catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Hodnota od 0 do 255", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
