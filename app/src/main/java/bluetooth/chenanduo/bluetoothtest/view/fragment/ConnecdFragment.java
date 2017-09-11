package bluetooth.chenanduo.bluetoothtest.view.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bluetooth.chenanduo.bluetoothtest.BaseFragment;
import bluetooth.chenanduo.bluetoothtest.MainActivity;
import bluetooth.chenanduo.bluetoothtest.R;
import bluetooth.chenanduo.bluetoothtest.adapter.ConnecdAdapter;
import bluetooth.chenanduo.bluetoothtest.util.Logger;

/**
 * Created by chen on 2017
 */

public class ConnecdFragment extends BaseFragment implements View.OnClickListener {

    private TextView mT;
    private TextView mtv_macName;
    private TextView tv_result;
    private EditText et_send;
    private Button btn_send;
    private RecyclerView mRecyclerView;
    private ConnecdAdapter mConnecdAdapter;
    private MainActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connecd, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mtv_macName = (TextView) view.findViewById(R.id.tv_connecd_mac);
        tv_result = (TextView) view.findViewById(R.id.tv_result);
        et_send = (EditText) view.findViewById(R.id.et_send);
        btn_send = (Button) view.findViewById(R.id.btn_send);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);

        btn_send.setOnClickListener(this);

        mActivity = (MainActivity) getActivity();

    }

    public void setMac(String mac) {
        Logger.d("已连接:");
        mtv_macName.setText("已连接:" + mac);
    }

    private List<String> mDatas = new ArrayList<>();
    private String service = null;
    private String notifi = null;
    private String write = null;

    public void setAdapter(List<String> list) {
        mDatas.clear();
        mDatas.addAll(list);
        Logger.d("list的长度:" + list.size());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mConnecdAdapter = new ConnecdAdapter(mDatas);
        mRecyclerView.setAdapter(mConnecdAdapter);

        mConnecdAdapter.onItemClick(new ConnecdAdapter.itemClick() {
            @Override
            public void OnItemClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                //                builder.setIcon(R.drawable.iv8);
                builder.setTitle("使用该UUID作为Write_UUID");
                final String items[] = {"Write"};
                /**
                 * 第二个参数：指定被选中的项
                 */
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String uuid = mDatas.get(position);
                        //消失
                        dialog.dismiss();
                        Logger.d("uuid:" + uuid);
                        for (int i = 0; i < mDatas.size(); i++) {
                            //找到该type下的服务读写等通知
                            String type = mDatas.get(i).substring(0, 1);
                            String tempUUID = mDatas.get(i);
                            if (type.equals(uuid.substring(0, 1))) {
                                Logger.d("该UUID下:" + tempUUID.substring(tempUUID.indexOf(":") + 1, tempUUID.length()));
                                String text = tempUUID.substring(1, tempUUID.length());
                                // 1 dxt ：
                                String properties = tempUUID.substring(1, 4);
                                if (properties.startsWith("服务")) {
                                    service = text.substring(3, text.length());
                                } else if (properties.contains("通")) {
                                    notifi = text.substring(text.indexOf(":")+1, text.length());
                                }
                                write = uuid.substring(uuid.indexOf(":")+1, uuid.length());
                            }
                        }
                        Logger.d("service:" + service + "\r\n" + "notifi:" + notifi + "\r\n" + "write:" + write);
                        if (service != null && notifi != null && write != null) {
                            //如果三个都不为空就设置通知
                            mActivity.setNotifi(service, notifi, write);
                        }
                    }
                });
                //记得要show
                builder.show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //获取用户输入
                getUserInput();
                break;
        }
    }

    public void getUserInput() {
        String date = et_send.getText().toString().trim();
        if (TextUtils.isEmpty(date)) {
            showHint("为空");
            return;
        }
        if (write != null) {
            mActivity.write(date, write);
        }
    }

    public void setResult(String text) {
        tv_result.setText("返回：" + text);
    }

    public void clean() {
        tv_result.setText("返回：");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*service = null;
        notifi = null;
        write = null;*/
    }
}
