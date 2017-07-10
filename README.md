# JafirZipProgressUtil
a tool to zip and unZip with progressDialog


Uses:
```
 findViewById(R.id.compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ZipProgressUtil.ZipFile(Environment.getExternalStorageDirectory() + "/zip/Qingning-master/",
                Environment.getExternalStorageDirectory() + "/zip/",
                new ZipProgressUtil.ZipListener() {
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
        
 ```

#### gradle

```
 repositories {
        maven { url 'https://jitpack.io' }
 }
 ```
 ```

 compile 'com.github.fly7632785:JafirZipProgressUtil:1.0.1'
```

