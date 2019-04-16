package com.yf.btp;

public interface PrinterConnectStatCallback {

    void onConnecting(String mac);

    void onConnected(String mac);

    void onFailure(String mac);

}
