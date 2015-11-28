package kavovar.petlack.eu.kavovar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ListDevicesActivity extends AppCompatActivity {

    private final String TAG = "ListDevicesActivity";
    private static int REQUEST_ENABLE_BT = 0;

    private ListView list;
    private ProgressBar progress;

    private DevicesAdapter adapter;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothChatService chatService;

    private byte writeData = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_devices);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);

        list = (ListView) findViewById(R.id.list);
        progress = (ProgressBar) findViewById(R.id.loading);

        adapter = new DevicesAdapter(this, new ArrayList<BluetoothDevice>());
        list.setAdapter(adapter);

        chatService = new BluetoothChatService(this, new Handler(Looper.myLooper()));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();
            Intent data = new Intent();
            BluetoothDevice d = adapter.getItem(position);
            if (d.getBondState() == BluetoothDevice.BOND_NONE) {
                Log.d(TAG, "not bound");
                if (d.createBond()) {
                    Log.d(TAG, "will pair");
                    list.setVisibility(View.GONE);
                    progress.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "not");
                }
            }
            else if (d.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "bound");
            }
            data.putExtra("name", d.getName());
            data.putExtra("mac", d.getAddress());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
        unregisterReceiver(mPairReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Nefunguje");
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                adapter.clear();
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                makeDiscovery();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Bluetooth zapnute");
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Bluetooth zapinanie odmietnute");
                finish();
            }
            else {
                Log.d(TAG, "Bluetooth neviem co");
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            adapter.clear();
            bluetoothAdapter.cancelDiscovery();
            makeDiscovery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeDiscovery() {
        Log.d(TAG, "Discovery");
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Add the name and address to an array adapter to show in a ListView
            Log.d(TAG, device.getName() + "\n" + device.getAddress() + " " + device.getBondState());
            adapter.add(device);
        }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "AAA" + device.toString());
            if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                Log.d(TAG, "Paired");
                finish();
            } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                Log.d(TAG, "Unpaired");
            }

        }
        }
    };

}
