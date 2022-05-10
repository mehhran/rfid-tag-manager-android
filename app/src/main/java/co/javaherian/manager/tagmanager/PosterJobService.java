package co.javaherian.manager.tagmanager;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import java.io.IOException;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PosterJobService extends JobService {

    private static final String TAG = "PosterJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params) {

        new Thread(() -> {

            /*
            *  Something like this will be sent to the server:
            *
            *  {
            *   "tags" : [
            *   {"strEPC": strEPC, "antenna": antenna, "strRSSI": strRSSI, "nReadCount": nReadCount},
            *   {"strEPC": strEPC, "antenna": antenna, "strRSSI": strRSSI, "nReadCount": nReadCount},
            *   {"strEPC": strEPC, "antenna": antenna, "strRSSI": strRSSI, "nReadCount": nReadCount},
            *   ... ],
            *   "scan_time": currentTime,
            *  }
            *
            */

            RequestBody formBody = new FormBody.Builder()
                    .add("tags", params.getExtras().getString("tags"))
                    .add("scan_time", params.getExtras().getString("scan_time"))
                    .build();

            String myApiKey = BuildConfig.API_KEY;
            String myApiUrl = BuildConfig.TAGS_API_URL;

            Request request = new Request.Builder()
                    .url(myApiUrl)
                    .post(formBody)
                    .addHeader("Authorization", myApiKey)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);

            try {
                Response response = call.execute();
                Objects.requireNonNull(response.body()).close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Job finished");
            jobFinished(params, false);
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
