package com.aliyun.sls.android.crashreporter.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

/**
 * @author gordon
 * @date 2022/5/9
 */
class LogParser {
    private static final String TAG = "LogParser";

    private static final String BLOCK_START = "*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***";
    private static final String BLOCK_MODULE_START = "--- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---";

    private static class Singleton {
        private static final LogParser INSTANCE = new LogParser();
    }

    private Context context;
    private String packageName;
    private Long time = null;
    private String id = null;
    private String catId = null;
    private ErrorSummary errorSummary = null;

    private LogParser() {
        //no instance
    }

    public static LogParser getInstance() {
        return Singleton.INSTANCE;
    }

    public LogParserResult parser(Context context, File file, String type) {
        this.context = context;
        this.packageName = context.getPackageName();
        LogParserResult result = new LogParserResult();

        internalParser(result, file, type);

        return result;
    }

    private void internalParser(LogParserResult result, File file, String type) {
        BufferedReader bufferedReader = null;
        try {
            final FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            State state = State.NOT_START;
            List<BlockBuilder> entities = new ArrayList<>();
            BlockBuilder blockBuilder = null;
            while (null != (line = bufferedReader.readLine())) {
                if (TextUtils.isEmpty(line)) {
                    continue;
                }

                // check if BLOCK_START line
                // if true, next line is basic info
                if (state == State.NOT_START && TextUtils.equals(BLOCK_START, line)) {
                    state = State.START;
                    continue;
                }

                // if BLOCK_MODULE_START
                if (TextUtils.equals(BLOCK_MODULE_START, line)) {
                    state = State.BLOCK_START;
                    if (null != blockBuilder) {
                        blockBuilder.pack();
                        entities.add(blockBuilder);
                        blockBuilder = null;
                    }
                    continue;
                }

                // when START state, do not exchange state until BLOCK_MODULE_START reached
                if (state == State.START) {
                    if (null == blockBuilder) {
                        blockBuilder = new BlockBuilder("basic_info");
                    }
                    parseBasicBlock(blockBuilder, line);
                    continue;
                }

                if (state == State.BLOCK_START) {
                    if (line.startsWith("Process Name:")) {
                        if (TextUtils.equals(type, "anr")) {
                            blockBuilder = new BlockBuilder("anr_summary");
                        } else {
                            blockBuilder = new BlockBuilder("stacktrace");
                        }
                        parseBlock(blockBuilder, line);
                    } else if (line.startsWith("opened file count:")) {
                        blockBuilder = new BlockBuilder("opened_files");
                        parseBlock(blockBuilder, line);
                    } else {
                        blockBuilder = new BlockBuilder(line);
                    }

                    state = State.BLOCK_ING;
                    continue;
                }

                if (state == State.BLOCK_ING) {
                    parseBlock(blockBuilder, line);
                }
            }

            for (BlockBuilder entity : entities) {
                result.putObject(entity.blockName, entity.content);
            }

            if (null != time) {
                result.put("time", String.valueOf(time));
                time = null;
            }

            if (null != id) {
                result.put("id", id);
                id = null;
            }

            if (null != errorSummary) {
                LogParserResult summary = new LogParserResult();
                summary.putObject("exception", errorSummary.exception);
                summary.putObject("reason", errorSummary.reason);
                if (!TextUtils.isEmpty(errorSummary.code)) {
                    summary.putObject("code", errorSummary.code);
                }
                result.put("summary", summary);

                final String exception = TextUtils.isEmpty(errorSummary.exception) ? "" : errorSummary.exception;
                final String code = TextUtils.isEmpty(errorSummary.code) ? "" : errorSummary.code;
                catId = EncryptionUtils.md5(exception + code);
                result.put("catId", catId);

                errorSummary = null;
                catId = null;
            }

        } catch (Throwable t) {
            // ignore
        } finally {
            IOUtils.closeSilently(bufferedReader);
        }

    }

    private void parseBasicBlock(BlockBuilder entity, String line) {
        if (null != entity) {
            if (line.startsWith("Basic Information") && line.contains("time:")) {
                this.time = toLongTime(obtainTime(line));
            }

            if (line.startsWith("Report Name:")) {
                this.id = toId(line);
            }

            entity.append(line);
            entity.append("\n");
        }
    }

    private void parseBlock(BlockBuilder entity, String line) {
        if (null != entity) {
            entity.append(line);
            entity.append("\n");

            if ("stacktrace".equals(entity.blockName)) {
                parseSummary(line);
            }
        }
    }

    private boolean parseJavaSummary = false;
    private boolean parseNativeSummary = false;
    private boolean parseAnrSummary = false;

    private void parseSummary(String line) {
        if (line.contains("Back traces starts.")) {
            parseJavaSummary = true;
            return;
        }

        if (line.startsWith("  fpsr ")) {
            parseNativeSummary = true;
            return;
        }

        if (line.startsWith("\"main\" ")) {
            parseAnrSummary = true;
            return;
        }

        if (parseJavaSummary) {
            if (null == errorSummary) {
                errorSummary = new ErrorSummary();
                errorSummary.exception = line.substring(0, line.indexOf(":")).trim();
                errorSummary.reason = line.substring(line.indexOf(":") + 1).trim();
                return;
            }

            if (line.contains("Caused by:")) {
                line = line.substring(line.indexOf(":"));
                errorSummary.exception = line.substring(0, line.indexOf(":")).trim();
                errorSummary.reason = line.substring(line.indexOf(":") + 1).trim();
                errorSummary.code = null;
                return;
            }

            if (TextUtils.isEmpty(errorSummary.code) && line.contains(packageName)) {
                errorSummary.code = line.replace("\t", "").replace("at ", "").trim();
                errorSummary.shouldParse = false;
            }

            if (!errorSummary.shouldParse) {
                parseJavaSummary = false;
            }
            return;
        }

        if (parseNativeSummary) {
            if (null == errorSummary) {
                errorSummary = new ErrorSummary();
                errorSummary.found = true;
                return;
            }

            if (errorSummary.found) {
                // #00 pc 000000000000c92c  /data/app/cn.vcinema.cinema-2/lib/arm64/libsls_producer.so
                // (set_get_time_unix_func+136)
                String[] tmp = line.trim().replaceAll("  ", " ").split(" ");
                errorSummary.exception = (tmp[2] + " " + tmp[3]).trim();
                errorSummary.reason = tmp[3].substring(tmp[3].lastIndexOf("/") + 1).trim();
                if (tmp.length == 5) {
                    errorSummary.code = tmp[4].substring(1, tmp[4].length() - 1).trim();
                }
                parseNativeSummary = false;
                return;
            }

            return;
        }

        if (parseAnrSummary) {
            if (null == errorSummary) {
                errorSummary = new ErrorSummary();
                return;
            }

            if (line.startsWith("  | held mutexes=")) {
                errorSummary.found = true;
                return;
            }

            if (errorSummary.shouldContinue) {
                if (line.contains(packageName)) {
                    errorSummary.exception = line.replace("  at ", "").trim();
                    errorSummary.shouldContinue = false;
                    parseAnrSummary = false;
                    return;
                }

                if (line.startsWith("\"Signal Catcher\"")) {
                    parseAnrSummary = false;
                    return;
                }
            }

            if (errorSummary.found) {
                if (line.startsWith("  at ")) {
                    errorSummary.exception = line.replace("  at ", "").trim();
                    if (line.contains(packageName)) {
                        parseAnrSummary = false;
                        return;
                    }

                    errorSummary.shouldContinue = true;
                }
            }
            return;
        }
    }

    private String obtainTime(String info) {
        try {
            String time = info.split("time:")[1].trim();
            return time.substring(0, time.length() - 1);
        } catch (Throwable t) {
            return null;
        }
    }

    private long toLongTime(String time) {
        try {
            return Long.parseLong(time);
        } catch (Throwable t) {
            return System.currentTimeMillis();
        }
    }

    private String parseTime(String time) {
        try {
            return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).parse(time).getTime() + "";
        } catch (Throwable e) {
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private String toId(String info) {
        String reportName;
        try {
            reportName = info.split(":")[1].trim();
        } catch (Exception e) {
            reportName = String.valueOf(SystemClock.uptimeMillis());
        }

        return EncryptionUtils.md5(reportName);
    }

    private enum State {
        NOT_START,
        START,
        BLOCK_START,
        BLOCK_ING,
        IDLE
    }

    private static class BlockBuilder {
        private static final ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
            @Override
            protected StringBuilder initialValue() {
                return new StringBuilder(512);
            }
        };

        String blockName;
        String content;
        StringBuilder contentBuilder;

        BlockBuilder(String blockName) {
            this.blockName = KeyConverter.convert(blockName);
            this.contentBuilder = stringBuilder.get();
            this.contentBuilder.setLength(0);
        }

        void append(String line) {
            this.contentBuilder.append(line);
        }

        void pack() {
            this.content = contentBuilder.toString();
        }
    }

    private static class ErrorSummary {
        String exception;
        String reason;
        String code;
        boolean found = false;
        boolean shouldContinue = false;
        boolean shouldParse = true;
    }

    private static class SummaryParser {

        void start() {

        }

        void parse(String line) {

        }

        void end() {

        }
    }
}
