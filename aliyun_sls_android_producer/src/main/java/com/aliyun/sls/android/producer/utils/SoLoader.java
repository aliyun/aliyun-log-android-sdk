package com.aliyun.sls.android.producer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

/**
 * @author gordon
 * @date 2021/04/27
 */
public class SoLoader {
    private static final int MAX_RETRY_COUNT = 5;

    private SoLoader() {
        //no instance
    }

    public static SoLoader instance() {
        return new SoLoader();
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    public void loadLibrary(Context context, String library) {
        try {
            System.loadLibrary(library);
            return;
        } catch (UnsatisfiedLinkError e) {
            // ignore
        }

        if (null == context) {
            return;
        }

        File libraryFile = getLibraryFile(context, library);
        if (!libraryFile.exists()) {
            cleanup(context, library);
        }

        installLibrary(context, library);

        System.load(libraryFile.getAbsolutePath());
    }

    private void cleanup(Context context, String library) {
        final File libraryDir = getLibraryDir(context);
        if (!libraryDir.exists()) {
            return;
        }
        final File libraryFile = getLibraryFile(context, library);

        final String mapLibraryName = mapLibraryName(library);

        final File[] files = libraryDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(mapLibraryName);
            }
        });

        if (null == files || files.length == 0) {
            return;
        }

        for (File file : files) {
            if (!file.getAbsolutePath().equals(libraryFile.getAbsolutePath())) {
                file.delete();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    private void installLibrary(Context context, String library) {
        final String[] abis = getSupportABIs();
        final String mapLibraryName = mapLibraryName(library);
        final File libraryFile = getLibraryFile(context, library);

        final ZipFileInEntry zipFileInEntry = getZipFileInEntry(context, abis, mapLibraryName);
        if (null == zipFileInEntry) {
            return;
        }

        int tries = 0;
        while (tries++ < MAX_RETRY_COUNT) {
            try {
                if (!libraryFile.exists() && !libraryFile.createNewFile()) {
                    continue;
                }
            } catch (IOException e) {
                continue;
            }

            InputStream ins = null;
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(libraryFile);
                ins = zipFileInEntry.zipFile.getInputStream(zipFileInEntry.zipEntry);
                final long written = copy(ins, fos);
                fos.getFD().sync();
                if (written != libraryFile.length()) {
                    continue;
                }
            } catch (IOException e) {
                continue;
            } finally {
                try {
                    if (ins != null) {
                        ins.close();
                    }
                } catch (Throwable e) {
                    // ignore
                }

                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }

            libraryFile.setReadable(true, false);
            libraryFile.setExecutable(true, false);
            libraryFile.setWritable(true, false);
            return;
        }

        try {
            zipFileInEntry.zipFile.close();
        } catch (Throwable e) {
            // ignore
        }
    }

    private long copy(InputStream in, OutputStream out) throws IOException {
        long copied = 0;
        byte[] buf = new byte[4096];
        while (true) {
            int read = in.read(buf);
            if (read == -1) {
                break;
            }
            out.write(buf, 0, read);
            copied += read;
        }
        out.flush();
        return copied;
    }

    private ZipFileInEntry getZipFileInEntry(Context context, String[] abis, String mapLibraryName) {
        for (String apkFile : getAPKFiles(context)) {
            ZipFile zipFile = null;
            int tries = 0;
            while (tries++ < MAX_RETRY_COUNT) {
                try {
                    zipFile = new ZipFile(new File(apkFile), ZipFile.OPEN_READ);
                    break;
                } catch (IOException e) {

                }
            }

            if (null == zipFile) {
                return null;
            }

            tries = 0;
            while (tries++ < MAX_RETRY_COUNT) {
                String soNameInApk;
                ZipEntry libraryEntry;

                for (final String abi : abis) {
                    soNameInApk = "lib" + File.separatorChar + abi + File.separatorChar + mapLibraryName;
                    libraryEntry = zipFile.getEntry(soNameInApk);

                    if (libraryEntry != null) {
                        return new ZipFileInEntry(zipFile, libraryEntry);
                    }
                }
            }

            try {
                zipFile.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return null;
    }

    private File getLibraryFile(Context context, String library) {
        return new File(getLibraryDir(context), mapLibraryName(library));
    }

    private File getLibraryDir(Context context) {
        return context.getDir("libs", Context.MODE_PRIVATE);
    }

    private String mapLibraryName(String library) {
        if (library.startsWith("lib") && library.endsWith(".so")) {
            // Already mapped
            return library;
        }

        return System.mapLibraryName(library);
    }

    private String[] getSupportABIs() {
        if (VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS.length > 0) {
            return Build.SUPPORTED_ABIS;
        } else if (!TextUtils.isEmpty(Build.CPU_ABI2)) {
            return new String[] {Build.CPU_ABI, Build.CPU_ABI2};
        } else {
            return new String[] {Build.CPU_ABI};
        }
    }

    private String[] getAPKFiles(final Context context) {
        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
            && applicationInfo.splitSourceDirs != null
            && applicationInfo.splitSourceDirs.length != 0) {
            String[] apks = new String[applicationInfo.splitSourceDirs.length + 1];
            apks[0] = applicationInfo.sourceDir;
            System.arraycopy(applicationInfo.splitSourceDirs, 0, apks, 1, applicationInfo.splitSourceDirs.length);
            return apks;
        }
        return new String[] {applicationInfo.sourceDir};
    }

    private static class ZipFileInEntry {
        public ZipFile zipFile;
        public ZipEntry zipEntry;

        public ZipFileInEntry(ZipFile zipFile, ZipEntry zipEntry) {
            this.zipFile = zipFile;
            this.zipEntry = zipEntry;
        }
    }

}
