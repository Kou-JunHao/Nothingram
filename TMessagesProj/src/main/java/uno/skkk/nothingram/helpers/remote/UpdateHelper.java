package uno.skkk.nothingram.helpers.remote;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class UpdateHelper extends BaseRemoteHelper {
    private static final String GITHUB_RELEASES_LATEST = "https://api.github.com/repos/Kou-JunHao/Nothingram/releases/latest";
    private static final Pattern LAST_NUMBER_PATTERN = Pattern.compile("(\\d+)(?!.*\\d)");
    private static OkHttpClient httpClient;

    /**
     * @param date {long} - date in milliseconds
     */
    public static String formatDateUpdate(long date) {
        long epoch;
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            epoch = pInfo.lastUpdateTime;
        } catch (Exception e) {
            epoch = 0;
        }
        if (date <= epoch) {
            return LocaleController.formatString(R.string.LastUpdateNever);
        }
        try {
            Calendar rightNow = Calendar.getInstance();
            int day = rightNow.get(Calendar.DAY_OF_YEAR);
            int year = rightNow.get(Calendar.YEAR);
            rightNow.setTimeInMillis(date);
            int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
            int dateYear = rightNow.get(Calendar.YEAR);

            if (dateDay == day && year == dateYear) {
                if (Math.abs(System.currentTimeMillis() - date) < 60000L) {
                    return LocaleController.formatString(R.string.LastUpdateRecently);
                }
                return LocaleController.formatString(R.string.LastUpdateFormatted, LocaleController.formatString(R.string.TodayAtFormatted,
                        LocaleController.getInstance().getFormatterDay().format(new Date(date))));
            } else if (dateDay + 1 == day && year == dateYear) {
                return LocaleController.formatString(R.string.LastUpdateFormatted, LocaleController.formatString(R.string.YesterdayAtFormatted,
                        LocaleController.getInstance().getFormatterDay().format(new Date(date))));
            } else if (Math.abs(System.currentTimeMillis() - date) < 31536000000L) {
                String format = LocaleController.formatString(R.string.formatDateAtTime,
                        LocaleController.getInstance().getFormatterDayMonth().format(new Date(date)),
                        LocaleController.getInstance().getFormatterDay().format(new Date(date)));
                return LocaleController.formatString(R.string.LastUpdateDateFormatted, format);
            } else {
                String format = LocaleController.formatString(R.string.formatDateAtTime,
                        LocaleController.getInstance().getFormatterYear().format(new Date(date)),
                        LocaleController.getInstance().getFormatterDay().format(new Date(date)));
                return LocaleController.formatString(R.string.LastUpdateDateFormatted, format);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return "LOC_ERR";
    }

    private static final class InstanceHolder {
        private static final UpdateHelper instance = new UpdateHelper();
    }

    public static UpdateHelper getInstance() {
        return InstanceHolder.instance;
    }

    private static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }
        return httpClient;
    }

    private static int extractVersionCode(String value) {
        if (TextUtils.isEmpty(value)) {
            return -1;
        }
        Matcher matcher = LAST_NUMBER_PATTERN.matcher(value);
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Throwable t) {
            FileLog.e(t);
            return -1;
        }
    }

    private static TLRPC.TL_help_appUpdate mapReleaseToUpdate(GitHubRelease release) {
        if (release == null || release.draft || release.prerelease) {
            return null;
        }
        int latestVersionCode = extractVersionCode(release.tagName);
        if (latestVersionCode <= BuildConfig.VERSION_CODE) {
            return null;
        }
        TLRPC.TL_help_appUpdate update = new TLRPC.TL_help_appUpdate();
        update.id = Math.max(1, latestVersionCode);
        update.version = !TextUtils.isEmpty(release.name) ? release.name : release.tagName;
        update.text = !TextUtils.isEmpty(release.body) ? release.body : LocaleController.getString(R.string.AppUpdate);
        if (!TextUtils.isEmpty(release.htmlUrl)) {
            update.url = release.htmlUrl;
            update.flags |= 4;
        }
        return update;
    }

    @Override
    protected void onError(String text, Delegate delegate) {
        delegate.onTLResponse(null, text);
    }

    @Override
    protected String getRequestMethod() {
        return "check_for_updates";
    }

    @Override
    protected String getRequestParams() {
        return "";
    }

    public void checkNewVersionAvailable(Delegate delegate) {
        Utilities.globalQueue.postRunnable(() -> {
            TLRPC.TL_help_appUpdate update = null;
            try {
                Request request = new Request.Builder()
                        .url(GITHUB_RELEASES_LATEST)
                        .header("Accept", "application/vnd.github+json")
                        .build();
                try (var response = getHttpClient().newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        update = mapReleaseToUpdate(GSON.fromJson(body, GitHubRelease.class));
                    }
                }
            } catch (Throwable t) {
                FileLog.e(t);
            }
            TLRPC.TL_help_appUpdate finalUpdate = update;
            AndroidUtilities.runOnUIThread(() -> delegate.onTLResponse(finalUpdate, null));
        });
    }

    public static class GitHubRelease {
        @SerializedName("tag_name")
        @Expose
        public String tagName;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("body")
        @Expose
        public String body;
        @SerializedName("html_url")
        @Expose
        public String htmlUrl;
        @SerializedName("published_at")
        @Expose
        public String publishedAt;
        @SerializedName("prerelease")
        @Expose
        public boolean prerelease;
        @SerializedName("draft")
        @Expose
        public boolean draft;
    }
}
