# BluetoothGprinterLibrary

Gprinter蓝牙打印库

## 使用
### 注册动态权限
```Java
int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            int REQUEST_READ_PHONE_STATE = 0x11;

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }
```

### 开启PrinterService和bind PrinterService

```Java

package org.oz.code;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yf.btp.PickedPrinterCallback;
import com.yf.btp.PrinterService;
import com.yf.btp.entity.Printer;

import org.oz.code.databinding.FragmentPrinterBinding;

import java.util.Objects;

/**
 * A placeholder fragment containing a simple view.
 */
public class PrinterActivityFragment extends Fragment {

    public PrinterActivityFragment() {
    }

    private FragmentPrinterBinding mBinding;

    private final Handles mHandles = new Handles();

    private PrinterService mPrinterService;

    private Intent intentPrinterService;

    public class Handles {

        public void onPrinter(View v) {

            new Thread(() -> {

                @SuppressLint("InflateParams") final View root = LayoutInflater.from(getContext()).inflate(R.layout.test_print, null);

                mPrinterService.printView(root);
//                mPrinterService.printBitmap(testBitmap());

            }).start();

        }


        public void onDiscovery(View v) {

            mPrinterService.setPickedPrinterCallback(dev -> {

                mBinding.setContent(dev.toString());

                mBinding.setDiscovery("valid:" + mPrinterService.isValid() + "DevName：" + mPrinterService.getSelectedDevName());


            });


            mPrinterService.pickPrinter(getContext());

        }

    }
    

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_printer, container, false);

        mBinding.setLifecycleOwner(this);

        mBinding.setHandles(mHandles);

        return mBinding.getRoot();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        bindService();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();

        initView();

    }


    private void initData() {


    }

    private void initView() {


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService();

        if (mBinding != null)
            mBinding.unbind();

        mBinding = null;
    }

    private void bindService() {

        intentPrinterService = new Intent(getContext(), PrinterService.class);

        Objects.requireNonNull(getActivity()).startService(intentPrinterService);

        Intent intent = new Intent(getContext(), PrinterService.class);

        Objects.requireNonNull(getActivity()).bindService(intent, mConnection, Context.BIND_AUTO_CREATE); // bindService

    }

    private void unbindService() {

        Objects.requireNonNull(getActivity()).unbindService(mConnection);

        Objects.requireNonNull(getActivity()).stopService(intentPrinterService);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            PrinterService.LocalBinder binder = (PrinterService.LocalBinder) service;

            mPrinterService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mPrinterService = null;
        }
    };

}
```

# 打印

### 蓝牙搜索Gprinter和连接Gprinter

添加设备选择监听器
```Java
PrinterService.setPickedPrinterCallback(PickedPrinterCallback pickedPrinterCallback);
```

选择蓝牙设备
```Java
PrinterService.pickPrinter(Context); //Context使用Activity或Fragment.getContext() 
```

## 打印Bitmap

```Java
PrinterService.printBitmap(Bitmap);
```

## 打印View

```Java
PrinterService.printView(View);
```

