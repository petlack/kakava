package kavovar.petlack.eu.kavovar;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PairedDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

    public interface IPairedDeviceCallbacks {
        public void onConnect(BluetoothDevice device);
        public void onDisconnect(BluetoothDevice device);
        public void onCommandOn(BluetoothDevice device);
        public void onCommandOff(BluetoothDevice device);
        public void onUnpair(BluetoothDevice device);
        public void onAlarm(BluetoothDevice device);
    }

    private BluetoothDevice connectedDevice = null;
    private IPairedDeviceCallbacks callbacks;

    public PairedDevicesAdapter(Context context, ArrayList<BluetoothDevice> users, IPairedDeviceCallbacks callbacks) {
        super(context, 0, users);
        this.callbacks = callbacks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_paired_device, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.name);
        ImageView action = (ImageView) convertView.findViewById(R.id.action);
        ImageView on = (ImageView) convertView.findViewById(R.id.on);
        ImageView off = (ImageView) convertView.findViewById(R.id.off);
        ImageView alarm = (ImageView) convertView.findViewById(R.id.alarm);
        ImageView unpair = (ImageView) convertView.findViewById(R.id.unpair);
        // Populate the data into the template view using the data object
        // Return the completed view to render on screen

        if ("HC-06".equals(device.getName())) {
            tvName.setText("Kavovar");
        }
        else if ("HC-05".equals(device.getName())) {
            tvName.setText("Ziarovka");
        }
        else {
            tvName.setText(device.getName());
        }

        if (device.equals(connectedDevice)) {
            action.setImageResource(R.drawable.ic_flash_off_black_48dp);
            alarm.setVisibility(View.VISIBLE);
            on.setVisibility(View.VISIBLE);
            off.setVisibility(View.VISIBLE);
            unpair.setVisibility(View.GONE);
        }
        else {
            action.setImageResource(R.drawable.ic_flash_on_black_48dp);
            alarm.setVisibility(View.GONE);
            on.setVisibility(View.GONE);
            off.setVisibility(View.GONE);
            unpair.setVisibility(View.VISIBLE);
        }

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device.equals(connectedDevice)) {
                    callbacks.onDisconnect(device);
                }
                else {
                    callbacks.onConnect(device);
                }
            }
        });

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCommandOn(device);
            }
        });
        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onCommandOff(device);
            }
        });

        unpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onUnpair(device);
            }
        });
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onAlarm(device);
            }
        });

        return convertView;
    }

    public void setConnectedDevice(BluetoothDevice device) {
        connectedDevice = device;
        notifyDataSetInvalidated();
    }

}