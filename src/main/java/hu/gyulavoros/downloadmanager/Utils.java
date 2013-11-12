package hu.gyulavoros.downloadmanager;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Locale;

public final class Utils {

    private static final String KBYTE_POSTFIX = " KB";
    private static final String MBYTE_POSTFIX = " MB";

    private static final String SPEED_KBYTE_POSTFIX = " KB/s";
    private static final String SPEED_MBYTE_POSTFIX = " MB/s";

    private static final DecimalFormat SPEED_FORMAT;
    private static final DecimalFormat SIZE_FORMAT;

    static {
        SPEED_FORMAT = new DecimalFormat();
        SPEED_FORMAT.setMinimumFractionDigits(2);
        SPEED_FORMAT.setMaximumFractionDigits(2);

        SIZE_FORMAT = new DecimalFormat();
        SIZE_FORMAT.setMinimumFractionDigits(2);
        SIZE_FORMAT.setMaximumFractionDigits(2);
    }

    public static URL verifyURL(String fileURL) {
        if (!fileURL.toLowerCase(Locale.getDefault()).startsWith("http://"))
            return null;

        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(fileURL);
        } catch (Exception e) {
            return null;
        }

        if (verifiedUrl.getFile().length() < 2)
            return null;

        return verifiedUrl;
    }

    public static String formatDownloadSpeed(final double speed) {
        return speed > 1024 ? SPEED_FORMAT.format(speed / 1024.0) + SPEED_MBYTE_POSTFIX : SPEED_FORMAT.format(speed) + SPEED_KBYTE_POSTFIX;
    }

    public static String formatDownloadSize(final double size) {
        return size > 1024 ? SIZE_FORMAT.format(size / 1024.0) + MBYTE_POSTFIX : SIZE_FORMAT.format(size) + KBYTE_POSTFIX;
    }

}
