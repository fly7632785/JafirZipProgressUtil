package com.jafir.ziputil;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by jafir on 2017/7/10.
 */

public class ZipProgressUtil {

    private static int BUFF_SIZE = 2048;

    /***
     * 解压通用方法
     *
     * @param zipFileString
     *            zip文件路径
     * @param outPathString
     *            解压路径
     * @param listener
     *            监听
     */
    public static void UnZipFile(final String zipFileString, final String outPathString, final ZipListener listener) {
        Thread zipThread = new UnzipMainThread(zipFileString, outPathString, listener);
        zipThread.start();
    }

    /***
     * 压缩通用方法
     * 默认zip名为folder名字
     *
     * @param folderString
     *            待压缩的文件夹路径
     * @param outPathString
     *            压缩后的路径
     * @param listener
     *            监听
     */
    public static void ZipFile(final String folderString, final String outPathString, final ZipListener listener) {
        Thread zipThread = new ZipMainThread(folderString, outPathString, listener);
        zipThread.start();
    }

    public interface ZipListener {
        /**
         * 开始解压
         */
        void zipStart();

        /**
         * 解压成功
         */
        void zipSuccess();

        /**
         * 解压进度
         */
        void zipProgress(int progress);

        /**
         * 解压失败
         */
        void zipFail(Exception e);
    }


    private static class UnzipMainThread extends Thread {

        String zipFileString;
        String outPathString;
        ZipProgressUtil.ZipListener listener;

        public UnzipMainThread(String zipFileString, String outPathString, ZipProgressUtil.ZipListener listener) {
            this.zipFileString = zipFileString;
            this.outPathString = outPathString;
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.zipStart();
                    }
                });
                long sumLength = 0;
                // 获取解压之后文件的大小,用来计算解压的进度
                long ziplength = getZipTrueSize(zipFileString);
                System.out.println("====文件的大小==" + ziplength);
                FileInputStream inputStream = new FileInputStream(zipFileString);
                ZipInputStream inZip = new ZipInputStream(inputStream);

                ZipEntry zipEntry;
                String szName = "";
                while ((zipEntry = inZip.getNextEntry()) != null) {
                    szName = zipEntry.getName();
                    if (zipEntry.isDirectory()) {
                        szName = szName.substring(0, szName.length() - 1);
                        File folder = new File(outPathString + File.separator + szName);
                        folder.mkdirs();
                    } else {
                        File file = new File(outPathString + File.separator + szName);
                        file.createNewFile();
                        FileOutputStream out = new FileOutputStream(file);
                        int len;
                        byte[] buffer = new byte[BUFF_SIZE];
                        while ((len = inZip.read(buffer)) != -1) {
                            sumLength += len;
                            int progress = (int) ((sumLength * 100) / ziplength);
                            updateProgress(progress, listener);
                            out.write(buffer, 0, len);
                            out.flush();
                        }
                        out.close();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.zipSuccess();
                    }
                });

                inZip.close();
            } catch (final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.zipFail(e);
                    }
                });
            }
        }

        int lastProgress = 0;

        private void updateProgress(int progress, ZipProgressUtil.ZipListener listener2) {
            /** 因为会频繁的刷新,这里我只是进度>1%的时候才去显示 */
            if (progress > lastProgress) {
                lastProgress = progress;
                listener2.zipProgress(progress);
            }
        }
    }

    private static class ZipMainThread extends Thread {

        String zipFileString;
        String outPathString;
        ZipProgressUtil.ZipListener listener;
        private long currentSize;
        private long maxSize;

        public ZipMainThread(String zipFileString, String outPathString, ZipProgressUtil.ZipListener listener) {
            this.zipFileString = zipFileString;
            this.outPathString = outPathString;
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.zipStart();
                    }
                });
                // 获取解压之后文件的大小,用来计算解压的进度
                maxSize = getTotalSize(new File(zipFileString));
                System.out.println("====文件的大小==" + maxSize);
                File file = new File(zipFileString);  // 定义要压缩的文件夹
                File zipFile = new File(outPathString, file.getName() + ".zip");    // 定义压缩文件名称
                ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
                //压缩
//                zip(file, zipOut,outPathString, sumLength, ziplength);
                ZipFiles(file.getParent() + File.separator, file.getName(), zipOut);
                zipOut.finish();
                zipOut.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.zipSuccess();
                    }
                });
            } catch (final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.zipFail(e);
                    }
                });
            }
        }

        private void zip(File file, ZipOutputStream zipOut, String outPathString, long sumLength, long ziplength) throws Exception {
            outPathString = outPathString + (outPathString.trim().length() == 0 ? "" : File.separator)
                    + file.getName();
            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                for (File f : fileList) {
                    zip(f, zipOut, outPathString, sumLength, ziplength);
                }
            } else {
                byte buffer[] = new byte[BUFF_SIZE];
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file),
                        BUFF_SIZE);
                zipOut.putNextEntry(new ZipEntry(outPathString));
                int realLength;
                while ((realLength = in.read(buffer)) != -1) {
                    sumLength += realLength;
                    int progress = (int) ((sumLength * 100) / ziplength);
                    updateProgress(progress, listener);
                    zipOut.write(buffer, 0, realLength);
                }
                in.close();
                zipOut.flush();
                zipOut.closeEntry();
            }
        }

        int lastProgress = 0;

        private void updateProgress(int progress, ZipProgressUtil.ZipListener listener2) {
            /** 因为会频繁的刷新,这里我只是进度>1%的时候才去显示 */
            if (progress > lastProgress) {
                lastProgress = progress;
                listener2.zipProgress(progress);
            }
        }


        /**
         * 压缩文件
         *
         * @param folderString   压缩的文件夹
         * @param fileString     文件名
         * @param zipOutputSteam 压缩的流
         * @throws Exception
         */
        private void ZipFiles(String folderString, String fileString, java.util.zip.ZipOutputStream zipOutputSteam) throws Exception {
            if (zipOutputSteam == null)
                return;

            File file = new File(folderString + fileString);

            //判断是不是文件
            if (file.isFile()) {

                ZipEntry zipEntry = new ZipEntry(fileString);
                FileInputStream inputStream = new FileInputStream(file);
                zipOutputSteam.putNextEntry(zipEntry);

                int len;
                byte[] buffer = new byte[BUFF_SIZE];

                while ((len = inputStream.read(buffer)) != -1) {
                    currentSize += len;
                    int progress = (int) ((currentSize * 100) / maxSize);
                    updateProgress(progress, listener);
                    zipOutputSteam.write(buffer, 0, len);
                }

                zipOutputSteam.closeEntry();
            } else {

                //文件夹的方式,获取文件夹下的子文件
                String fileList[] = file.list();

                //如果没有子文件, 则添加进去即可
                if (fileList.length <= 0) {
                    ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                    zipOutputSteam.putNextEntry(zipEntry);
                    zipOutputSteam.closeEntry();
                }

                //如果有子文件, 遍历子文件
                for (int i = 0; i < fileList.length; i++) {
                    ZipFiles(folderString, fileString + File.separator + fileList[i], zipOutputSteam);
                }

            }

        }

    }

    /**
     * 获取文件夹及其文件的大小
     * 递归算法
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getTotalSize(File file) throws Exception {
        long size = 0;
        try {
            //获取路径下所有的文件或者目录
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                //如果是一个路径或者目录 则继续递归
                if (fileList[i].isDirectory()) {
                    size = size + getTotalSize(fileList[i]);
                    //否则是一个文件  获取文件大小
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 获取zip的大小
     *
     * @param filePath
     * @return
     */
    private static long getZipTrueSize(String filePath) {
        long size = 0;
        ZipFile f;
        try {
            f = new ZipFile(filePath);
            Enumeration<? extends ZipEntry> en = f.entries();
            while (en.hasMoreElements()) {
                size += en.nextElement().getSize();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    private static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }
}
