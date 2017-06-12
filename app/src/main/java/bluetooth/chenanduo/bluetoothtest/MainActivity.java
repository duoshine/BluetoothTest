package bluetooth.chenanduo.bluetoothtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bluetooth.chenanduo.bluetoothtest.adapter.HomePagerAdapter;
import bluetooth.chenanduo.bluetoothtest.bluetooth.BluetoothLeClass;
import bluetooth.chenanduo.bluetoothtest.util.Logger;
import bluetooth.chenanduo.bluetoothtest.util.SpUtil;
import bluetooth.chenanduo.bluetoothtest.util.ThreadUtils;
import bluetooth.chenanduo.bluetoothtest.util.ToastUtils;
import bluetooth.chenanduo.bluetoothtest.util.Util;
import bluetooth.chenanduo.bluetoothtest.view.fragment.BondedFragment;
import bluetooth.chenanduo.bluetoothtest.view.fragment.ConnecdFragment;
import bluetooth.chenanduo.bluetoothtest.view.fragment.ShowNotBleFragment;

/*bluetooth测试工具*/
public class MainActivity extends AppCompatActivity implements BluetoothLeClass.OnConnectListener, BluetoothLeClass.OnDisconnectListener, BluetoothLeClass.OnDataAvailableListener, BluetoothLeClass.OnServiceDiscoverListener, BluetoothAdapter.LeScanCallback, ViewPager.OnPageChangeListener {

    private static final int REQUEST_PERMISSION_ACCESS_LOCATION = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private List<String> titles = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private ConnecdFragment mConnecdFragment;
    private BluetoothLeClass mBLE;
    private Handler mHandler = new Handler();
    private List<BluetoothDevice> devices = new ArrayList<>();
    private ShowNotBleFragment mShowNotBleFragment;
    private static String address;
    private static String name;
    private boolean isConnecd;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void init() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("BluetoothTest");
        mConnecdFragment = new ConnecdFragment();

        mShowNotBleFragment = new ShowNotBleFragment();
        fragments.add(mShowNotBleFragment);
        fragments.add(new BondedFragment());
        fragments.add(mConnecdFragment);
        titles.add("Scanner");
        titles.add("Bonded");
        titles.add("Connecd");

        HomePagerAdapter adapter = new HomePagerAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(adapter);

        //6.0动态申请权限
        requestPermission();

        //判断是否支持BLE
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtils.showToast(MainActivity.this, "该设备不支持BLE");
            return;
        }
        mBLE = BluetoothLeClass.getInstane(this);
        if (!mBLE.initialize()) {
            ToastUtils.showToast(this, "蓝牙开启失败");
            return;
        }

        /**
         * 蓝牙连接失败或者成功等的回调方法
         */
        mBLE.setOnConnectListener(this);//ble连接回调
        mBLE.setOnDisconnectListener(this);//ble断开连接回调
        mBLE.setOnDataAvailableListener(this);//从蓝牙设备读取信息回调
        mBLE.setOnServiceDiscoverListener(this);//从蓝牙设备读取信息回调
        /*判断蓝牙是否在开启状态 如果已经开启就直接扫描 如果没有开启则开启，等待3秒后开始扫描*/
        if (mBLE.isEnabled(MainActivity.this)) {
            //开始扫描设备
            startScan();
        } else {
            /*提示开启蓝牙*/
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            //开始扫描设备
            startScan();
        }
    }

    public void startScan() {
        /*用户连续扫描 会有多个任务 扫描前清掉handler*/
        mHandler.removeCallbacksAndMessages(null);
         /*五秒扫描时间*/
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBLE.stopScanDevices(MainActivity.this);
                Logger.d("时间到停止扫描");
            }
        }, 5000);
        mBLE.disconnect();
        mBLE.stopScanDevices(this);
        devices.clear();
        mBLE.startScanDevices(true, this);
    }

    //6.0权限机制  蓝牙要精确定位位置
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkAccessFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);//位置
            int sdPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//sd
            if (checkAccessFinePermission != PackageManager.PERMISSION_GRANTED || sdPermission != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_PERMISSION_ACCESS_LOCATION);
                return;

            }
        }
    }


    //申请权限回调方法 处理用户是否授权
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /**
                     * 用户同意授权
                     */

                } else {
                    //用户拒绝授权 则给用户提示没有权限功能无法使用，
                    ToastUtils.showToast(MainActivity.this, "拒绝授权 蓝牙功能无法使用");
                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_behavior);
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        //设置和viewpager联动
        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(this);
    }

    public void setPagerIndex(String mac, String name) {
        this.address = mac;
        this.name = name;
        mBLE.stopScanDevices(this);
        mBLE.disconnect();
        showDialog();
        mBLE.connect(mac);
    }

    /*连接成功*/
    @Override
    public void onConnect(BluetoothGatt gatt) {
        isConnecd = true;
        //搜索连接设备所支持的service  需要连接上才可以 这个方法是异步操作 在回调函数onServicesDiscovered中得到status
        //通过判断status是否等于BluetoothGatt.GATT_SUCCESS来判断查找Service是否成功
        gatt.discoverServices();
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (address != null) {
                    /*信任上一次连接的设备*/
                    SpUtil.saveString(MainActivity.this, "bondedname", name);
                    SpUtil.saveString(MainActivity.this, "bondedaddress", address);
                    //viewpager跳转至连接页面
                    mViewPager.setCurrentItem(2);
                    mConnecdFragment.setMac(address);
                }
                hindDialog();
                Logger.d("连接成功");
            }
        });
    }

    /*断开连接*/
    @Override
    public void onDisconnect(BluetoothGatt gatt) {
        Logger.d("断开连接");
        isConnecd = false;
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    /*返回*/
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        final String result = Util.Bytes2HexString(value);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result != null) {
                    mConnecdFragment.setResult(result);
                }
            }
        });
    }

    /*发送成功*/
    @Override
    public void onCharacteristicWriteSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Logger.d("写入蓝牙设备成功" + Util.Bytes2HexString(characteristic.getValue()));
    }

    /*扫描结果*/
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        // 设备搜索完毕
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean exist = false;
                //根据mac地质判断扫描到的设备是否已经存在集合中了
                for (int i = 0; i < devices.size(); i++) {
                    if (devices.get(i).getAddress().equals(
                            device.getAddress())) {
                        exist = true;
                        break;
                    }
                }
                //如果不存在 就放进集合中
                if (!exist) {
                    devices.add(device);
                    mShowNotBleFragment.setDate(devices);
                }
            }
        });
    }

    /*查找服务*/
    @Override
    public void onServiceDiscover(BluetoothGatt gatt) {
        final List list = displayGattServices(gatt.getServices());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnecdFragment.setAdapter(list);
            }
        });
    }

    private List<String> service_uuid = new ArrayList<>();
    private List<BluetoothGattService> GattService;

    /*遍历所有uuid  由用户选择*/
    private List displayGattServices(List<BluetoothGattService> gattServices) {
        service_uuid.clear();
        this.GattService = gattServices;
        for (int i = 0; i < gattServices.size(); i++) {
            service_uuid.add(gattServices.get(i).getUuid().toString());
            List<BluetoothGattCharacteristic> characteristics = gattServices.get(i).getCharacteristics();
            for (int i1 = 0; i1 < characteristics.size(); i1++) {
                service_uuid.add(characteristics.get(i1).getUuid().toString());
            }
        }
        return service_uuid;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0 && devices.size() != 0) {
            mShowNotBleFragment.setDate(devices);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*设置uuid*/
    public void setNotifi(String service, String notifi, String write) {
        if (mBLE != null && GattService.size() != 0) {
            mBLE.displayGattServices(GattService, service, notifi);
        }
    }

    /*写*/
    public void write(String bytes, String write) {
        if (mBLE != null) {
            if (isConnecd) {
                mConnecdFragment.clean();
                boolean b = mBLE.writeCharacteristic(bytes, UUID.fromString(write));
                if (!b) {
                    ToastUtils.showToast(MainActivity.this, "发送失败");
                }
            } else {
                ToastUtils.showToast(MainActivity.this, "蓝牙连接已断开");
            }
        }
    }

    public void showDialog() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) {
                    ToastUtils.showToast(MainActivity.this, "连接超时,请重试");
                    mProgressDialog.dismiss();
                }
            }
        }, 5000);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MainActivity.this);
        }
        mProgressDialog.setMessage("正在连接...");
        mProgressDialog.show();
    }

    public void hindDialog() {
        mProgressDialog.dismiss();
    }
}

