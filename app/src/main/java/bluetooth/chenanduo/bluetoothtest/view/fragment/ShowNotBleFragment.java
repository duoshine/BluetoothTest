package bluetooth.chenanduo.bluetoothtest.view.fragment;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import bluetooth.chenanduo.bluetoothtest.BaseFragment;
import bluetooth.chenanduo.bluetoothtest.MainActivity;
import bluetooth.chenanduo.bluetoothtest.R;
import bluetooth.chenanduo.bluetoothtest.adapter.ShowNotBleAdapter;
import bluetooth.chenanduo.bluetoothtest.adapter.ShowNotBleInterface;
import bluetooth.chenanduo.bluetoothtest.util.Logger;

/**
 * Created by chen on 2017  显示所有未连接的蓝牙设备
 */

public class ShowNotBleFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shownotble, null);
        initView(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.SwipeRefreshLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        //一秒换一种颜色  一秒一种颜色是安卓强制  时间无法自定义
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);
        //下拉刷新的监听事件
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                isRefresh = true;
                //刷新就扫描蓝牙
                ((MainActivity) getActivity()).startScan();
                //一秒后隐藏刷新
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
/*
    public void showDialog() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) {
                    showHint("连接超时,请重试");
                    mProgressDialog.dismiss();
                }
            }
        }, 5000);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
        }
        mProgressDialog.setMessage("正在连接...");
        mProgressDialog.show();
    }

    public void hindDialog() {
        mProgressDialog.dismiss();
    }*/

    public void setDate(final List<BluetoothDevice> devices) {
        this.devices = devices;
        ShowNotBleAdapter mShowNotBleAdapter = new ShowNotBleAdapter(devices);
        mRecyclerView.setAdapter(mShowNotBleAdapter);
        /*条目点击事件*/
        mShowNotBleAdapter.onItemClick(new ShowNotBleInterface() {
            @Override
            public void itemClick(int position) {
                if (devices != null && devices.size() != 0) {
                    ((MainActivity) getActivity()).setPagerIndex(devices.get(position).getAddress(),devices.get(position).getName());
                    Logger.d("连接的mac地址：" + devices.get(position).getAddress());
                }
            }
        });
    }
}