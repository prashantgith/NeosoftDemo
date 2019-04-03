package com.prashant.neosoftdemo.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by EbitM9 on 4/27/2017.
 */

public class PreferenceSettings
{
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Context mContext;
    private int PRIVATE_MODE = 0;

    private static final String PREFERENCE_NAME = "neosoft";
    private static final String KEY_NEW_INSTALL = "firstinstall";

    public PreferenceSettings(Context context)
    {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_NAME,PRIVATE_MODE);
        mEditor = mSharedPreferences.edit();
    }

    public void setPermission(String KEY_PERMISSION, boolean login)
    {
        mEditor.putBoolean(KEY_PERMISSION,login).commit();
    }

    public boolean getPermission(String KEY_PERMISSION)
    {
        return mSharedPreferences.getBoolean(KEY_PERMISSION,false);
    }

    public void setNewInstall(boolean first)
    {
        mEditor.putBoolean(KEY_NEW_INSTALL,first).commit();
    }

    public boolean getNewInstall()
    {
        return mSharedPreferences.getBoolean(KEY_NEW_INSTALL,false);
    }

}
