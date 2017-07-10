package com.jafir.jafirutil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jafir.ziputil.ZipProgressUtil;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog d;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        findViewById(R.id.compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ZipProgressUtil.ZipFile(Environment.getExternalStorageDirectory() + "/zip/Qingning-master/", Environment.getExternalStorageDirectory() + "/zip/", new ZipProgressUtil.ZipListener() {
                    @Override
                    public void zipStart() {
                        d = new ProgressDialog(v.getContext());
                        d.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        d.show();
                    }

                    @Override
                    public void zipSuccess() {
                        d.dismiss();
                        Toast.makeText(v.getContext(), "success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void zipProgress(int progress) {
                        d.setProgress(progress);
                    }

                    @Override
                    public void zipFail(Exception e) {
                        e.printStackTrace();
                        d.dismiss();
                        Toast.makeText(v.getContext(), "failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.decompress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                ZipProgressUtil.UnZipFile(Environment.getExternalStorageDirectory() + "/Qingning-master.zip",
                        Environment.getExternalStorageDirectory() + "/zip/", new ZipProgressUtil.ZipListener() {
                            @Override
                            public void zipStart() {
                                d = new ProgressDialog(v.getContext());
                                d.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                d.show();
                            }

                            @Override
                            public void zipSuccess() {
                                d.dismiss();
                                Toast.makeText(v.getContext(), "success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void zipProgress(int progress) {
                                d.setProgress(progress);
                            }

                            @Override
                            public void zipFail(Exception e) {
                                e.printStackTrace();
                                d.dismiss();
                                Toast.makeText(v.getContext(), "failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


    }
}
