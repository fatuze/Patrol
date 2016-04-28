package com.example.fatuze.fanran;

import com.baidu.mapapi.map.Marker;

/**
 * Created by fatuze on 2016/4/1.
 */
public class Site {
    private double mLatitude;  //纬度
    private double mLongitude; //经度
    private int mID;
    private int mDescription;
    private Marker mMarker;

    public Site()
    {
        mLatitude = 0.0;
        mLongitude = 0.0;
        mID = 0;
        mDescription = R.string.text_state;
    }

    public Site(double latitude, double longitude, int id, int description)
    {
        mLatitude = latitude;
        mLongitude = longitude;
        mID = id;
        mDescription = description;
    }

    public void SetLatitude(double latitude)
    {
        mLatitude = latitude;
    }
    public void SetLongitude(double longitude)
    {
        mLongitude = longitude;
    }
    public void SetID(int id)
    {
        mID = id;
    }
    public void SetDescription(int description)
    {
        mDescription = description;
    }
    public void SetMarker(Marker marker)
    {
        mMarker = marker;
    }

    public double GetLatitude()
    {
        return mLatitude;
    }
    public double GetLongitude()
    {
        return mLongitude;
    }
    public int GetID()
    {
        return mID;
    }
    public int GetDescription()
    {
        return mDescription;
    }
    public Marker GetMarker()
    {
        return mMarker;
    }
}
