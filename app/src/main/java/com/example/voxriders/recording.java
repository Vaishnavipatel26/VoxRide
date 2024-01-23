package com.example.voxriders;

import static android.Manifest.permission.RECORD_AUDIO;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class recording extends AppCompatActivity {
    MediaRecorder mediaRecorder ;
    private LottieAnimationView recordButton;
    private LottieAnimationView waves;
    private Chronometer timer;
    boolean isRecording=false;
    int serverResponseCode = 0;
    static final int RequestPermissionCode = 200;
    AlertDialog.Builder builder;
    static final String endpoint="https://1912parth.pythonanywhere.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        recordButton = findViewById(R.id.playbutton);
        recordButton.pauseAnimation();
        waves = findViewById(R.id.waves);
        waves.pauseAnimation();
        timer=findViewById(R.id.time);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO)
                            == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(recording.this
                                , new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,}, RequestPermissionCode);
                        return ;
                    }
                }
                else{
                    builder= new AlertDialog.Builder(recording.this);
                    builder.setMessage("Error")
                            .setTitle("No Microphone Found");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return ;
                }
                isRecording=!isRecording;
                if(isRecording)
                    try{
//                        recordButton.setText("Stop Recording");
                        mediaRecorder=new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        mediaRecorder.setOutputFile(getRecordingFilePath());
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        recordButton.playAnimation();
                        waves.playAnimation();
                        timer.start();
//                        Toast.makeText(recording.this, "Recording Started",
//                                Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        recordButton.pauseAnimation();
                        waves.pauseAnimation();
                        timer.stop();
                    }
                else
                    try {
//                        recordButton.setText("Start Recording");
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
//                                        Toast.makeText(recording.this, "Recording Stopped",
//                                                Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(getApplicationContext(), loading.class);
                                        startActivity(i);
//                                        finish();
                                    }
                                });
                                uploadFile(getRecordingFilePath());

                            }
                        }).start();
                    }catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        waves.pauseAnimation();
                        recordButton.pauseAnimation();
                        timer.stop();
                    }
            }
        });
    }
    @Override
    public void onBackPressed() {
        if(isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
//            recordButton.setText("Start Recording");
            return ;
        }
        super.onBackPressed();
    }

    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        SharedPreferences preferences=getSharedPreferences("data", Context.MODE_PRIVATE);
        if (!sourceFile.isFile())
            return 0;
        else
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(endpoint);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"lang\""+lineEnd+lineEnd+preferences.getString("lang","hi")+lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                    InputStreamReader reader=new InputStreamReader(conn.getInputStream());
                    JsonReader jsonReader=new JsonReader(reader);
                    jsonReader.beginObject();
                    SharedPreferences.Editor editor=preferences.edit();
                    while(jsonReader.hasNext())
                    {
                        String name=jsonReader.nextName();
                        editor.putString(name,jsonReader.nextString());
                    }
                    editor.putBoolean("isIdempotent",true);
                    editor.commit();
                    Intent intent=getPackageManager().getLaunchIntentForPackage("com.ubercab");
                    startActivity(intent);

                }
               else  runOnUiThread(new Runnable() {
                    public void run() {
//                                Toast.makeText(UploadToServer.this, "File Upload Complete.",
//                                             Toast.LENGTH_SHORT).show();
                            }
                });
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //  Toast.makeText(UploadToServer.this, "MalformedURLException",
                        //Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        //Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
                        //Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload", "Exception : "  + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i=new Intent(recording.this, language.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }
    private boolean isMicrophonePresent(){
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    @Override
    protected void onRestart() {
        waves.setProgress(0);
        recordButton.setProgress(0);
        super.onRestart();
    }

    private String getRecordingFilePath(){
        ContextWrapper contextwrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextwrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory,"temp"+".m4a");
        return file.getPath();
    }
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            builder = new AlertDialog.Builder(recording.this);
            builder.setMessage("Permissions Required")
                    .setTitle("Please allow permissions");
            builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else
            recordButton.callOnClick();
    }
}