package org.oz.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.btp.PrinterService;
import com.yf.btp.entity.Printer;

import org.oz.test.util.QRCodeUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements ServiceConnection {


    private PrinterService mPrinterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivityPermissionsDispatcher.onPermissionWithPermissionCheck(this);

    }


    private Printer mPrinter;

    @SuppressLint("SetTextI18n")
    public void onPrint(View view) {

        if (mPrinter == null) {

            mPrinterService.setPickedPrinterCallback(dev -> {

                mPrinter = dev;

                TextView tx_content = findViewById(R.id.tx_content);

                tx_content.setText(dev.getName() + ":" + dev.getMac());

            });

            mPrinterService.pickPrinter(this);

        } else {

            Bitmap bitmap = QRCodeUtils.createQRCodeBitmap("sddfsgdgfsdfsgfdsvdhjvhjsdhfshjdhsdfs", 100, 100);

            mPrinterService.printBitmap(bitmap, new Point(80, 98), new Size(34, 30), 10);

        }

    }


    private void bindService() {

        if (mPrinterService != null) return;

        Intent intent = new Intent(this, PrinterService.class);

        startService(intent);

        bindService(intent, this, Context.BIND_AUTO_CREATE); // bindService

    }

    private void unbindService() {

        unbindService(this);

        stopService(new Intent(this, PrinterService.class));
    }


    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {

        PrinterService.LocalBinder binder = (PrinterService.LocalBinder) service;

        mPrinterService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        mPrinterService = null;
    }


    @Override
    protected void onStart() {
        super.onStart();

        bindService();

    }

    @Override
    protected void onStop() {
        super.onStop();

        mPrinter = null;

        unbindService();

    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onPermission() {


    }


    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onPermissionRationale(PermissionRequest request) {

        Toast.makeText(this, "onPermissionRationale", Toast.LENGTH_SHORT).show();

        request.proceed();

    }

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onPermissionDenied() {
        Toast.makeText(this, "onPermissionDenied", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onPermissionNeverAskAgain() {
        Toast.makeText(this, "onPermissionNeverAskAgain", Toast.LENGTH_SHORT).show();
    }

}
