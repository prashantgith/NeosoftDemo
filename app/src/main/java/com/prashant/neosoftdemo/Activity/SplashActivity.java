package com.prashant.neosoftdemo.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prashant.neosoftdemo.Application.MyApplication;
import com.prashant.neosoftdemo.R;
import com.prashant.neosoftdemo.Utils.PreferenceSettings;

public class SplashActivity extends AppCompatActivity
{

    public final static int PERM_REQUEST_CODE_DRAW_OVERLAYS = 1234;
    long SPLASH_TIME = 3000; // 3000
    private PreferenceSettings mPreferenceSettings;
    private static final int REQUEST_PERMISSION_SETTING = 1001;
    private static final int PERMISSION_CALLBACK_CONSTANT = 1000;
    private static final String TAG = "SplashActivity";
    private boolean sentToSettings = false;
    private String[] permissionsRequired = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        mPreferenceSettings = MyApplication.getInstance().getPreferenceSettings();

        permissionToDrawOverlays();
    }

    public void permissionToDrawOverlays() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
            }
            else
            {
                Log.e("splash", " true");
                goAhead();
            }
        }
        else
        {
            Log.e("splash", " true");
            goAhead();
        }
    }

    public void goAhead()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            takePermissions();
        } else {
            initialize();

        }



    }

    private void initialize()
    {

        if (mPreferenceSettings.getNewInstall())
        {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();

        }
        else
        {
            new Handler().postDelayed(new Runnable() {

                public void run()
                {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }, SPLASH_TIME);
        }

        mPreferenceSettings.setNewInstall(true);

    }

    public void takePermissions() {


        if (ActivityCompat.checkSelfPermission(SplashActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED )
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissionsRequired[0]))
            {
                //Show Information about why you need the permission
                final Dialog dialog = new Dialog(SplashActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.dialog_permission);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.getWindow().setAttributes(lp);

                TextView tvTitle, tvMessage;
                tvTitle = (TextView) dialog.findViewById(R.id.txt_permission_title);
                tvMessage = (TextView) dialog.findViewById(R.id.txt_permission_message);

                tvTitle.setText("" + getResources().getString(R.string.location_permission));
                tvMessage.setText("" + getResources().getString(R.string.location_permission_message));

                LinearLayout btnSend = (LinearLayout) dialog.findViewById(R.id.btn_allow);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(SplashActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                        dialog.dismiss();
                    }
                });

              /*  LinearLayout btnCancel = (LinearLayout) dialog.findViewById(R.id.btn_cancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/

                dialog.show();

            } else if (mPreferenceSettings.getPermission(permissionsRequired[0])) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                final Dialog dialog = new Dialog(SplashActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.dialog_permission);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.getWindow().setAttributes(lp);

                TextView tvTitle, tvMessage;
                tvTitle = (TextView) dialog.findViewById(R.id.txt_permission_title);
                tvMessage = (TextView) dialog.findViewById(R.id.txt_permission_message);

                tvTitle.setText("" + getResources().getString(R.string.location_permission));
                tvMessage.setText("" + getResources().getString(R.string.location_permission_message));

                LinearLayout btnSend = (LinearLayout) dialog.findViewById(R.id.btn_allow);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", SplashActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(SplashActivity.this, "Go to Permissions to Grant Location", Toast.LENGTH_LONG).show();
                    }
                });

              /*  LinearLayout btnCancel = (LinearLayout) dialog.findViewById(R.id.btn_cancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/

                dialog.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(SplashActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

//            txtPermissions.setText("Permissions Required");
            mPreferenceSettings.setPermission(permissionsRequired[0], true);

        } else {
            //You already have the permission, just go ahead.
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                initialize();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissionsRequired[0]))
            {
                final Dialog dialog = new Dialog(SplashActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.dialog_permission);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.getWindow().setAttributes(lp);

                TextView tvTitle, tvMessage;
                tvTitle = (TextView) dialog.findViewById(R.id.txt_permission_title);
                tvMessage = (TextView) dialog.findViewById(R.id.txt_permission_message);

                tvTitle.setText("" + getResources().getString(R.string.location_permission));
                tvMessage.setText("" + getResources().getString(R.string.location_permission_message));

                LinearLayout btnSend = (LinearLayout) dialog.findViewById(R.id.btn_allow);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(SplashActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                        dialog.dismiss();
                    }
                });

              /*  LinearLayout btnCancel = (LinearLayout) dialog.findViewById(R.id.btn_cancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/
                dialog.show();
            } else {
                //Toast.makeText(SplashActivity.this, "Unable to get Permission", Toast.LENGTH_LONG).show();
                final Dialog dialog = new Dialog(SplashActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.dialog_permission);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER;

                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.getWindow().setAttributes(lp);

                TextView tvTitle, tvMessage;
                tvTitle = (TextView) dialog.findViewById(R.id.txt_permission_title);
                tvMessage = (TextView) dialog.findViewById(R.id.txt_permission_message);

                tvTitle.setText("" + getResources().getString(R.string.location_permission));
                tvMessage.setText("" + getResources().getString(R.string.location_permission_message));

                LinearLayout btnSend = (LinearLayout) dialog.findViewById(R.id.btn_allow);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ActivityCompat.requestPermissions(SplashActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                        dialog.dismiss();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", SplashActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(SplashActivity.this, "Go to Permissions to Grant Location", Toast.LENGTH_LONG).show();
                    }
                });

              /*  LinearLayout btnCancel = (LinearLayout) dialog.findViewById(R.id.btn_cancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });*/
                dialog.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PERM_REQUEST_CODE_DRAW_OVERLAYS) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
                    if (!Settings.canDrawOverlays(this)) {
                        // ADD UI FOR USER TO KNOW THAT UI for SYSTEM_ALERT_WINDOW permission was not granted earlier...
                        Log.e("splash", " false");
                        goAhead();

                    }
                    else
                    {
                        Log.e("splash", " true");
                        goAhead();

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
