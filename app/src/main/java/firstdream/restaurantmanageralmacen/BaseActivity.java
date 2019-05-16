package firstdream.restaurantmanageralmacen;


import android.app.Activity;
import android.app.ProgressDialog;

/**
 * Created by Jorge on 17/11/18.
 */

public class BaseActivity extends Activity{

        public ProgressDialog mProgressDialog;

        public void showProgressDialog() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Cargando ...");
                mProgressDialog.setIndeterminate(true);
            }

            mProgressDialog.show();
        }


        public void hideProgressDialog() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            hideProgressDialog();
        }

    }

