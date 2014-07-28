/*ImageAdapter created by Nitin Rao
 * This class creates a custom adapter for loading images by extending the baseadapter
 */
package com.vimd.p1t;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidtablayout.R;

public class ImageAdapter extends BaseAdapter {
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayList<Double> distances = new ArrayList<Double>();
    private Activity activity;
    private String[] data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public ImageAdapter(Activity a, String[] d,ArrayList<String>name,ArrayList<Double> dist ) {
        activity = a;
        data=d;
        names = (ArrayList<String>) name.clone();
        distances = (ArrayList<Double>) dist.clone();
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.listview_item_row, null);

        TextView text=(TextView)vi.findViewById(R.id.list_itm_service_name);
        ImageView image=(ImageView)vi.findViewById(R.id.list_item_img);
        TextView dist=(TextView)vi.findViewById(R.id.list_item_distance);
        text.setText(names.get(position));
        dist.setText(distances.get(position) + " miles");
        imageLoader.DisplayImage(data[position], image);
        return vi;
    }
}