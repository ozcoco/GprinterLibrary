package com.yf.btp.ui;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;


import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.yf.btp.PickedPrinterCallback;
import com.yf.btp.PrinterConnectStatCallback;
import com.yf.btp.R;
import com.yf.btp.entity.Printer;
import com.yf.btp.widgets.DecorativeAdapter;

import java.util.ArrayList;
import java.util.List;


public class BTDialog extends AppCompatDialog implements DialogInterface.OnDismissListener, DialogInterface.OnShowListener, PrinterConnectStatCallback {

    public final static int BLUETOOTH_DEVICE_CLASS_GPRINTER = 1664;

    public final static int BLUETOOTH_DEVICE_CLASS_MAJOR_GPRINTER = 1536;

    public interface OnControlListener {

        boolean isPicked(BluetoothDevice dev);

        boolean pick(BluetoothDevice dev);

        void connect(BluetoothDevice dev);

        void disconnect(BluetoothDevice dev);
    }

    public enum PRINTER_CONNECT_STAT {

        CONNECTED, CONNECTING, DISCONNECT
    }


    private RecyclerView rcy_pair;

    private RecyclerView rcy_discovery;

    private Button btn_scan;

    private ContentLoadingProgressBar progressBar;

    private ContentLoadingProgressBar pgr_connect;

    private DecorativeAdapter<BtHolder, BluetoothDevice> pairAdapter;

    private DecorativeAdapter<BtHolder, BluetoothDevice> discoveryAdapter;

    private OnControlListener mOnControlListener;

    private BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler = new Handler();

    private ArrayMap<String, BluetoothDevice> mPairingMap = new ArrayMap<>();

    private ArrayMap<String, BluetoothDevice> mDiscoveryMap = new ArrayMap<>();

    private ArrayMap<String, PRINTER_CONNECT_STAT> mPrinterConnStatMap = new ArrayMap<>();

    private PickedPrinterCallback mPickedPrinterCallback;

    public void setPickedPrinterCallback(PickedPrinterCallback pickedPrinterCallback) {
        this.mPickedPrinterCallback = pickedPrinterCallback;
    }


    public OnControlListener getOnControlListener() {
        return mOnControlListener;
    }

    public void setOnControlListener(OnControlListener onControlListener) {
        this.mOnControlListener = onControlListener;
    }

    public BTDialog(Context context, int theme) {
        super(context, theme);

        BleManager.getInstance().init((Application) context.getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("BTDialog", "^_*>>>>>>>>>>onCreate......");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dailog_bt);

    }


    /**
     * connect printer stat
     */
    @Override
    public void onConnecting(String mac) {

        Log.e("bluetooth printer", " onConnecting  >>>" + mac);

        mPrinterConnStatMap.put(mac, PRINTER_CONNECT_STAT.CONNECTING);

        loadData();

        pgr_connect.setVisibility(View.VISIBLE);

    }

    @Override
    public void onConnected(String mac) {

        Log.e("bluetooth printer", " onConnected  >>>" + mac);

        mPrinterConnStatMap.put(mac, PRINTER_CONNECT_STAT.CONNECTED);

        loadData();

        pgr_connect.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFailure(String mac) {

        Log.e("bluetooth printer", " onFailure  >>>" + mac);

        mPrinterConnStatMap.put(mac, PRINTER_CONNECT_STAT.DISCONNECT);

        loadData();

        pgr_connect.setVisibility(View.INVISIBLE);
    }


    /**
     * 蓝牙接收广播
     */
    private BroadcastReceiver btReceiver = new BroadcastReceiver() {
        //接收
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();

            Object[] lstName = new Object[0];

            if (b != null)
                lstName = b.keySet().toArray();

            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.e("bluetooth", keyName + ">>>" + String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device;
            // 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBluetoothClass().getMajorDeviceClass() == BLUETOOTH_DEVICE_CLASS_MAJOR_GPRINTER
                        && device.getBluetoothClass().getDeviceClass() == BLUETOOTH_DEVICE_CLASS_GPRINTER) {

                    mHandler.post(() -> {

                        if (!mDiscoveryMap.containsKey(device.getAddress()) && device.getBondState() != BluetoothDevice.BOND_BONDED) {

                            mDiscoveryMap.put(device.getAddress(), device);

                            discoveryAdapter.setData(mDiscoveryMap.values());
                        }
                    });

                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                progressBar.setVisibility(View.INVISIBLE);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                progressBar.setVisibility(View.VISIBLE);

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                int stat = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (stat) {

                    case BluetoothAdapter.STATE_ON: {

                        loadData();
                    }
                    break;
                    case BluetoothAdapter.STATE_OFF: {


                    }
                    break;
                    case BluetoothAdapter.STATE_TURNING_OFF: {


                    }
                    break;
                    case BluetoothAdapter.STATE_TURNING_ON: {

                        loadData();
                    }
                    break;
                }

            }

            //配对状态改变时
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                switch (device.getBondState()) {

                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d("BlueToothTestActivity", "正在配对......");
//
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d("BlueToothTestActivity", "完成配对");
//
                        mDiscoveryMap.remove(device.getAddress());

                        loadData();

                        if (mOnControlListener != null)
                            mOnControlListener.connect(device);

                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d("BlueToothTestActivity", "取消配对");

                        Toast.makeText(getContext(), "不可用设备！", Toast.LENGTH_SHORT).show();

                        if (mOnControlListener != null)
                            mOnControlListener.disconnect(device);

                    default:
                        break;
                }
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        Log.d("BTDialog", "^_*>>>>>>>>>>onStart......");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initComponent();

        checkBleDevice();//判断是否支持蓝牙，并打开蓝牙

        initData();

        initView();

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("BTDialog", "^_*>>>>>>>>>>onStop......");


        getContext().unregisterReceiver(btReceiver);

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        mBluetoothAdapter = null;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {

        clearData();

    }

    @Override
    public void onShow(DialogInterface dialog) {

        loadData();
    }


    private void initComponent() {

        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//配对状态改变
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//Local Adapter状态发生了变化
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描完成
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//扫描开始
        getContext().registerReceiver(btReceiver, intent);

        setOnDismissListener(this);

        setOnShowListener(this);
    }

    private void initData() {


    }


    /**
     * 判断是否支持蓝牙，并打开蓝牙
     * 获取到BluetoothAdapter之后，还需要判断是否支持蓝牙，以及蓝牙是否打开。
     * 如果没打开，需要让用户打开蓝牙：
     */
    private void checkBleDevice() {

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        } else {
            Log.i("blueTooth", "该手机不支持蓝牙");
        }
    }

    private void btScan() {

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            } else {
                mBluetoothAdapter.startDiscovery();

                BleManager.getInstance().scan(bleScanCallback);

            }

        }
    }


    private BleScanCallback bleScanCallback = new BleScanCallback() {
        @Override
        public void onScanFinished(List<BleDevice> scanResultList) {

            mHandler.post(() -> progressBar.setVisibility(View.INVISIBLE));

        }

        @Override
        public void onScanStarted(boolean success) {
            mHandler.post(() -> progressBar.setVisibility(View.VISIBLE));
        }

        @Override
        public void onScanning(BleDevice bleDevice) {

            final BluetoothDevice device = bleDevice.getDevice();

            if (device.getBluetoothClass().getMajorDeviceClass() == BLUETOOTH_DEVICE_CLASS_MAJOR_GPRINTER
                    && device.getBluetoothClass().getDeviceClass() == BLUETOOTH_DEVICE_CLASS_GPRINTER) {

                mHandler.post(() -> {

                    if (!mDiscoveryMap.containsKey(device.getAddress()) && device.getBondState() != BluetoothDevice.BOND_BONDED) {

                        mDiscoveryMap.put(device.getAddress(), device);

                        discoveryAdapter.setData(mDiscoveryMap.values());
                    }
                });

            }

        }
    };


    private void initView() {

        progressBar = findViewById(R.id.progressBar);

        pgr_connect = findViewById(R.id.pgr_connect);

        btn_scan = findViewById(R.id.btn_scan);

        rcy_pair = findViewById(R.id.rcy_pair);

        rcy_discovery = findViewById(R.id.rcy_discovery);

        rcy_pair.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        rcy_discovery.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        pairAdapter = new DecorativeAdapter<>(getContext(), new DecorativeAdapter.IAdapterDecorator<BtHolder, BluetoothDevice>() {

            @Override
            public BtHolder onCreateViewHolder(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {

                return new BtHolder(inflater.inflate(R.layout.item_bt, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull Context context, @NonNull BtHolder holder, @NonNull BluetoothDevice dev, int position) {

                holder.tx_name.setText(String.format("%s：%s", dev.getName(), dev.getAddress()));

                holder.btn_connect.setText(R.string.action_connect);

                PRINTER_CONNECT_STAT stat = mPrinterConnStatMap.get(dev.getAddress());

                if (stat != null) {
                    switch (stat) {

                        case CONNECTED:

                            holder.btn_connect.setEnabled(false);

                            holder.btn_disconnect.setEnabled(true);

                            holder.btn_pick.setEnabled(true);

                            holder.btn_connect.setText(R.string.action_connect);

                            break;

                        case CONNECTING:

                            holder.btn_disconnect.setEnabled(false);

                            holder.btn_pick.setEnabled(false);

                            holder.btn_connect.setEnabled(false);

                            holder.btn_connect.setText(R.string.action_connecting);

                            break;

                        case DISCONNECT:

                            holder.btn_disconnect.setEnabled(false);

                            holder.btn_pick.setEnabled(false);

                            holder.btn_connect.setEnabled(true);

                            break;
                    }

                } else {

                    holder.btn_disconnect.setEnabled(false);

                    holder.btn_pick.setEnabled(false);

                    holder.btn_connect.setEnabled(true);
                }

                holder.btn_pick.setOnClickListener(v -> {

                    if (mOnControlListener != null)
                        if (mOnControlListener.pick(dev)) {

                            Toast.makeText(getContext(), "已选择" + dev.getName() + "设备", Toast.LENGTH_SHORT).show();

                            if (mPickedPrinterCallback != null)
                                mPickedPrinterCallback.onPicked(new Printer(dev.getName(), dev.getAddress()));

                            dismiss();

                        } else {

                            Toast.makeText(getContext(), "设备不可用！", Toast.LENGTH_SHORT).show();

                        }

                });


                holder.btn_connect.setOnClickListener(v -> {

                    if (mOnControlListener != null)
                        mOnControlListener.connect(dev);

                });

                holder.btn_disconnect.setOnClickListener(v -> {

                    mPrinterConnStatMap.remove(dev.getAddress());

                    if (mOnControlListener != null)
                        mOnControlListener.disconnect(dev);

                });

            }
        });


        discoveryAdapter = new DecorativeAdapter<>(getContext(), new DecorativeAdapter.IAdapterDecorator<BtHolder, BluetoothDevice>() {

            @Override
            public BtHolder onCreateViewHolder(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
                return new BtHolder(inflater.inflate(R.layout.item_bt, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull Context context, @NonNull BtHolder holder, @NonNull BluetoothDevice dev, int position) {

                holder.tx_name.setText(String.format("%s：%s", dev.getName(), dev.getAddress()));

                holder.btn_disconnect.setVisibility(View.GONE);

                holder.btn_pick.setVisibility(View.GONE);

                holder.btn_connect.setEnabled(true);

                holder.btn_connect.setOnClickListener(v -> {

                    v.setEnabled(false);

                    dev.createBond();

                    mPairingMap.put(dev.getAddress(), dev);

                });

            }

        });

        rcy_pair.setAdapter(pairAdapter);

        rcy_discovery.setAdapter(discoveryAdapter);

        btn_scan.setOnClickListener(v -> btScan());

        List<BluetoothDevice> btDevList = new ArrayList<>(mBluetoothAdapter.getBondedDevices());

        pairAdapter.setData(btDevList);

    }


    /**
     * load data
     */
    private void loadData() {

        if (pairAdapter != null && mHandler != null && mBluetoothAdapter != null) {

            final List<BluetoothDevice> devices = new ArrayList<>();

            for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices())
                if (device.getBluetoothClass().getMajorDeviceClass() == BLUETOOTH_DEVICE_CLASS_MAJOR_GPRINTER
                        && device.getBluetoothClass().getDeviceClass() == BLUETOOTH_DEVICE_CLASS_GPRINTER)
                    devices.add(device);

            mHandler.post(() -> pairAdapter.setData(devices));
        }


        if (discoveryAdapter != null && mHandler != null && mBluetoothAdapter != null)
            mHandler.post(() -> discoveryAdapter.setData(mDiscoveryMap.values()));
    }

    private void clearData() {

        if (pairAdapter != null)
            pairAdapter.clearData();

        if (discoveryAdapter != null)
            discoveryAdapter.clearData();
    }


    public class BtHolder extends RecyclerView.ViewHolder {

        TextView tx_name;

        Button btn_connect;

        Button btn_disconnect;

        Button btn_pick;

        BtHolder(@NonNull View itemView) {
            super(itemView);
            tx_name = itemView.findViewById(R.id.tx_name);
            btn_connect = itemView.findViewById(R.id.btn_connect);
            btn_disconnect = itemView.findViewById(R.id.btn_disconnect);
            btn_pick = itemView.findViewById(R.id.btn_pick);
        }

    }


}
