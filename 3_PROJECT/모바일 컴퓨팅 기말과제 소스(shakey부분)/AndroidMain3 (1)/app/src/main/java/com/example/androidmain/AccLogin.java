package com.example.androidmain;


import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AccLogin extends AppCompatActivity {
    EditText et_name;
    Button but_sign;
    TextView tvResult;

    String name;
    String email;

    GoogleAccountCredential credential;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    GoogleSignInAccount account;

    Task task;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    AlertDialog.Builder oDialog;
    private static final int RC_SIGN_IN = 9001;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_menu);

        et_name = findViewById(R.id.et_name);
        but_sign = findViewById(R.id.btn_signin);
        tvResult = findViewById(R.id.tvResult);

        oDialog = new AlertDialog.Builder(this);

    }

    /////////////////////////////////////// ???????????? ?????? ????????? ???????????? ?????????
    public void signInUser(View v){
        //////////// ?????? ????????? ????????? ?????? //////////
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        name = et_name.getText().toString();
        chooseAccount();
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    //////////////////////////PHP ????????? ????????? ????????? ?????? ////////////////////////////////
    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog; // ?????? ?????????????????? ?????? ????????? ????????????

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AccLogin.this,
                    "Please Wait", null, true, true);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            tvResult.setText(result);

            if (result.equals("success")){
                Log.d("line2", result);

                Log.d("userdata:", UserData.getUserEmail()+ UserData.getUserName());
                Intent it = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(it);
                finish();
            }
            else{
                credential=null;
                UserData.setCredential(null);
                tvResult.setText("?????? ????????? ???????????????. \n" +
                        "????????? ?????? ???????????? ??? ??? ????????? ??? ????????????.\n?????????????????? ?????? ?????? ?????? ???????????? ??????????????????.");

            }
            Log.d("OnPostExecute state", "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String email_ = params[1];
            String name_ = params[2];
            String signdate_ = params[3];

            String serverURL = params[0];
            // ????????? String ????????? postParameters??? ??????
            String postParameters = "email=" + email_ + "&" + "name=" + name_ + "&" + "signdate=" + signdate_;
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                ////////////// ?????? //////////////
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8")); //php??? ??????
                outputStream.flush();
                outputStream.close();

                ////////////// php?????? echo???????????? ?????? ????????? ?????? //////////////
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("code", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = null;
                StringBuilder stringBuilder = new StringBuilder();

                line = bufferedReader.readLine();

                if (line.equals("success")){
                    Log.d("line2", line);

                    UserData.setUserEmail(email_);
                    UserData.setUserName(name_);
                    UserData.setCredential(credential);
                    Log.d("userdata:", UserData.getUserEmail()+ UserData.getUserName());
                }
                else{
                    credential=null;

                }
                /*
                String line = null;
                String flag = null;

                line = bufferedReader.readLine();
                flag = line;
                Log.d("line2", line);

                if (line.equals("success")){
                    Log.d("line2", line);

                    UserData.setUserEmail(email_);
                    UserData.setUserName(name_);
                    UserData.setCredential(credential);
                    Log.d("userdata:", UserData.getUserEmail()+ UserData.getUserName());
                }
                else{
                    credential=null;

                }
                */

                bufferedReader.close();

                return line; //OnPostExecute??? ???????????? ??? ?????????

            } catch (Exception e) {
                Log.d("error", "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }
        }
    }
    //////////////////////////PHP ????????? ????????? ????????? ?????? ////////////////////////////////

    //////////////////////////// ??? ???????????? ????????? ?????? ???????????? ??? ???????????????////////////

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                } else {
                    chooseAccount();
                }
                break;
        }
        email = credential.getSelectedAccountName();
        InsertData task = new InsertData();
        task.execute("http://113.198.234.124/insert.php", email, name, null);
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                googleAPI.isGooglePlayServicesAvailable(this);
        if(connectionStatusCode != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(connectionStatusCode)) {
                googleAPI.getErrorDialog(this, connectionStatusCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }

        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(() -> {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.getErrorDialog(this,
                    connectionStatusCode,
                    REQUEST_GOOGLE_PLAY_SERVICES).show();

        });
    }
}