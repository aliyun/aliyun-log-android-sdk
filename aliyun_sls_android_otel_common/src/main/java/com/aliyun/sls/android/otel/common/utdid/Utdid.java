package com.aliyun.sls.android.otel.common.utdid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.UUID;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

/**
 * @author gordon
 * @date 2021/05/24
 */
public final class Utdid {
    private Utdid() {
        //no instance
    }

    private static class Holder {
        final static Utdid INSTANCE = new Utdid();
    }

    public static Utdid getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void setUtdid(Context context, String utdid) {
        if (null == context || TextUtils.isEmpty(utdid)) {
            return;
        }

        try {
            Lock.lock(context);
            Storage.getInstance().setUtdid(context, utdid);
        } catch (Throwable t) {
            // ignore
        } finally {
            Lock.unlock();
        }
    }

    public synchronized String getUtdid(Context context) {
        String utdid = Storage.getInstance().getUtdid(context);
        if (!TextUtils.isEmpty(utdid)) {
            return utdid;
        }

        try {
            Lock.lock(context);
            utdid = UUID.randomUUID().toString();
            String[] parts = utdid.split("-");
            utdid = parts[0] + parts[1] + parts[2];
            //noinspection CharsetObjectCanBeUsed
            utdid = Base64.encodeToString(utdid.getBytes("UTF-8"), Base64.DEFAULT);

            Storage.getInstance().setUtdid(context, utdid);
        } catch (Throwable t) {
            utdid = "ffffffffffffffffffffffff";
        } finally {
            Lock.unlock();
        }

        return utdid;
    }

    private static class Storage {
        final String FILE_PATH = "/sls_android/files";

        private static class Holder {
            final static Storage
                INSTANCE = new Storage();
        }

        static Storage getInstance() {
            return Holder.INSTANCE;
        }

        void setUtdid(Context context, String utdid) {
            final File file = getFile(context);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                //noinspection CharsetObjectCanBeUsed
                OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
                writer.write(utdid);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String getUtdid(Context context) {
            final File file = getFile(context);
            if (!file.exists()) {
                return "";
            }

            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String line = reader.readLine();
                reader.close();

                line = validUtdid(line);
                return line;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "";
        }

        private String validUtdid(String utidid) {
            if (TextUtils.isEmpty(utidid)) {
                return "ffffffffffffffffffffffff";
            }

            if (utidid.endsWith("\n")) {
                return utidid.substring(0, utidid.length() - 1);
            }

            return utidid;
        }

        File getFile(Context context) {
            File file = context.getFilesDir();
            file = new File(file, FILE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            return new File(file, "unique");
        }
    }

    private static class Lock {
        private static File lockFile = null;
        private static FileChannel channel = null;
        private static FileLock lock = null;

        static synchronized void lock(Context context) {
            if (null == lockFile) {
                lockFile = Storage.getInstance().getFile(context);
            }

            if (!lockFile.exists()) {
                try {
                    lockFile.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }

            if (null == channel) {
                try {
                    channel = new RandomAccessFile(lockFile, "rw").getChannel();
                } catch (FileNotFoundException e) {
                    return;
                }
            }
            try {
                lock = channel.lock();
            } catch (IOException e) {
                // ignore
            }
        }

        static synchronized void unlock() {
            if (null != lock) {
                try {
                    lock.release();
                } catch (IOException e) {
                    // ignore
                } finally {
                    lock = null;
                }
            }

            if (null != channel) {
                try {
                    channel.close();
                } catch (IOException e) {
                    // ignore
                } finally {
                    channel = null;
                }
            }
        }
    }
}
