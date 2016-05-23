package com.example.fatuze.fanran;

import android.app.Activity;
import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by fatuze on 2016/5/16.
 */
public class SavedResultAdapter extends BaseAdapter{
    private Context mContext;
    private List<Map<String, Object>> mData;
    private static final String LOG_TAG = "SavedResultAdapter";

    private final static String ITEM_KEY_NAME      = "name";
    private final static String ITEM_KEY_STATUS    = "status";
    private final static String ITEM_KEY_TIME      = "time";

    private ViewHolder holder = null;

    public SavedResultAdapter(Context context)
    {
        this.mContext = context;
    }

    public void setData(List<Map<String, Object>> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        //How many items are in the data set represented by this Adapter.
        //在此适配器中所代表的数据集中的条目数
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        // Get the data item associated with the specified position in the data set.
        //获取数据集中与指定索引对应的数据项
        return position;
    }

    @Override
    public long getItemId(int position) {
        //Get the row id associated with the specified position in the list.
        //获取在列表中与指定索引对应的行id
        return position;
    }


    public void removeItem(int position) {
        mData.remove(position) ;
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView tv_name;
        TextView tv_status;
        TextView tv_time;
        Button button_upload;
        Button button_delete;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // The convertView argument is essentially a "ScrapView" as described is Lucas post
        // http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        // It will have a non-null value when ListView is asking you recycle the row layout.
        // So, when convertView is not null, you should simply update its contents instead

        if(convertView==null){
            holder = new ViewHolder();
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.content_saved_items, null);

            holder.tv_name = (TextView) convertView.findViewById(R.id.text_site_name);
            holder.tv_status = (TextView) convertView.findViewById(R.id.text_status);
            holder.tv_time = (TextView) convertView.findViewById(R.id.text_datetime);
            holder.button_upload = (Button) convertView.findViewById(R.id.uploadButton);
            holder.button_delete = (Button) convertView.findViewById(R.id.deleteButton);

            // store the holder with the view.
            convertView.setTag(holder);

        }else{
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_name.setText((String)mData.get(position).get(ITEM_KEY_NAME));
        int status = (Integer)mData.get(position).get(ITEM_KEY_STATUS);
        if(status == 0) {
            holder.tv_status.setText("正常");
        }else{
            holder.tv_status.setText("异常");
        }
        holder.tv_time.setText((String)mData.get(position).get(ITEM_KEY_TIME));
        holder.button_delete.setOnClickListener(new deleteButtonListener(position)) ;
        holder.button_upload.setOnClickListener(new uploadButtonListener(position));

        return convertView;
    }

    class deleteButtonListener implements View.OnClickListener {
        private int position ;

        deleteButtonListener( int pos) {
            position = pos;
        }

        @Override
        public void onClick( View v) {
            int vid = v.getId();
            if (vid == holder.button_delete.getId()) {
                removeItem(position);
            }
        }
    }

    class uploadButtonListener implements View.OnClickListener {
        private int position ;

        uploadButtonListener( int pos) {
            position = pos;
        }

        @Override
        public void onClick( View v) {
            int vid = v.getId();
            if (vid == holder.button_upload.getId()) {
                uploadItem(position);
            }
        }
    }

    public void uploadItem(int position) {
        Log.d(LOG_TAG, "uploadItem " + position);
    }
}
