package kavovar.petlack.eu.kavovar;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DevicesAdapter extends ArrayAdapter<Device> {

    public DevicesAdapter(Context context, ArrayList<Device> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Device device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_device, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.name);
        TextView tvMac = (TextView) convertView.findViewById(R.id.mac);
        // Populate the data into the template view using the data object
        // Return the completed view to render on screen

        tvName.setText(device.name);
        tvMac.setText(device.mac);

        return convertView;
    }



}