package galaxy.app.destressify;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import galaxy.app.destressify.R;

public class loadingDialog {

    private Activity activity;
    private AlertDialog dialog;

    loadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.activity_customdialog, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    void dismissDialog() {
        dialog.dismiss();
    }
}
