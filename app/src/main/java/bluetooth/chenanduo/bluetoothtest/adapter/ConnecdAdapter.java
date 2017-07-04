package bluetooth.chenanduo.bluetoothtest.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import bluetooth.chenanduo.bluetoothtest.R;

/**
 * Created by chen on 2017
 */

public class ConnecdAdapter extends RecyclerView.Adapter<ConnecdAdapter.MyViewHolder> {
    private List<String> list;

    public ConnecdAdapter(List<String> list) {
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connecd_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String temp = list.get(position);
        String text = temp.substring(1, temp.length());
        String properties = temp.substring(1, 4);
        if (text.startsWith("服务")) {
            holder.tv_uuid.setText(text);
        } else if (properties.contains("读写通")) {
            holder.tv_uuid.setText("        " + "Read/Write/Notifi:" + text.substring(3, text.length()));
        } else if (properties.contains("读写")) {
            holder.tv_uuid.setText("        " + "Read/Write:" + text.substring(2, text.length()));
        } else if (properties.contains("读")) {
            holder.tv_uuid.setText("        " + "Read:" + text.substring(1, text.length()));
        } else if (properties.contains("写")) {
            holder.tv_uuid.setText("        " + "Write:" + text.substring(1, text.length()));
        } else if (properties.contains("通")) {
            holder.tv_uuid.setText("        " + "Notifi:" + text.substring(1, text.length()));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClick != null)
                    mItemClick.OnItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_uuid;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_uuid = (TextView) itemView.findViewById(R.id.tv_uuid);
        }
    }

    public interface itemClick {
        void OnItemClick(int position);
    }

    private itemClick mItemClick;

    public void onItemClick(itemClick itemClick) {
        this.mItemClick = itemClick;
    }
}
