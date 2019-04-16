package com.yf.btp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Printer implements Serializable, Parcelable {

    private String name;

    private String mac;

    public Printer() {
    }

    public Printer(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    protected Printer(Parcel in) {
        name = in.readString();
        mac = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mac);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Printer> CREATOR = new Creator<Printer>() {
        @Override
        public Printer createFromParcel(Parcel in) {
            return new Printer(in);
        }

        @Override
        public Printer[] newArray(int size) {
            return new Printer[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "Printer{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
