package ga.ai.mobile.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import ga.ai.mobile.BuildConfig;
import ga.ai.mobile.R;

public class VersionChecker extends AsyncTask<String, String, String> {

    private String ver;
    private String annotation = "";
    private Context mContext;
    private String mPhone;
    private String aID;

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (ver != null && ver.compareTo(BuildConfig.VERSION_NAME) > 0) {
            annotation = String.format(annotation, BuildConfig.VERSION_NAME);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.new_version)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage(annotation)
                    .setPositiveButton(R.string.get_new_version, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String appPackageName = mContext.getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                                mContext.startActivity(intent);
                            } catch (android.content.ActivityNotFoundException anfe) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            builder.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            String mp = "";
            if (mPhone != null) {
                mp = Base64.encodeToString(mPhone.getBytes(), Base64.NO_WRAP);
            }
            URL url = new URL("http://altai-info.ga/mobile-app/misc/version.php?p=" + mp + "&aID=" + aID);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            ver = bufferedReader.readLine();
            StringBuilder stringBuilder = new StringBuilder();

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
                stringBuilder.append("\n");
            }
            bufferedReader.close();
            annotation = stringBuilder.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ver;
    }

    public void check() throws Exception {
        long last = PrefUtils.getLong(PrefUtils.LAST_UPDATE_CHECK_TIME, 0);
        long current = System.currentTimeMillis();
        if (current - last > 86400000) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            PrefUtils.putLong(PrefUtils.LAST_UPDATE_CHECK_TIME, current);
        }
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public void setaID(String aID) {
        this.aID = aID;
    }
}