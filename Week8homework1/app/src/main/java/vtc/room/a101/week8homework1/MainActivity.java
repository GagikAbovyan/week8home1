package vtc.room.a101.week8homework1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private boolean isInternal = true;
    private EditText editFileName;
    private EditText editText;
    private TextView readText;
    private volatile int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        whatButtonIsChecked();
        saveButtonClick();
        readButtonClick();
    }

    private void findViews() {
        editFileName = (EditText) findViewById(R.id.edit_file_name);
        editText = (EditText) findViewById(R.id.edit_text);
        readText = (TextView) findViewById(R.id.read_text);
    }

    private void readButtonClick() {
        final Button readButton = (Button) findViewById(R.id.read_button);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadThread readThread = new ReadThread();
                readThread.start();
            }
        });
    }

    private void sendNotification(final String not) {
        Toast.makeText(this, not, Toast.LENGTH_SHORT).show();
    }

    private void saveButtonClick() {
        final Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editFileName.getText().toString().isEmpty()) {
                    sendNotification(getString(R.string.illegal_argument));
                    return;
                }

                SaveThread saveThread = new SaveThread();
                saveThread.start();
            }
        });
    }

    private void whatButtonIsChecked() {
        final RadioButton internalButton = (RadioButton) findViewById(R.id.internal_button);
        final RadioButton externalButton = (RadioButton) findViewById(R.id.external_button);
        internalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internalButton.setChecked(true);
                externalButton.setChecked(false);
                isInternal = true;
            }
        });

        externalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                externalButton.setChecked(true);
                internalButton.setChecked(false);
                isInternal = false;
            }
        });
    }


    private class SaveThread extends Thread {
        @Override
        public void run() {
            if (isInternal) {
                saveInInternalStorage();
            } else {
                saveInExternalStorage();
            }
        }

        private void saveInExternalStorage() {
            File extStore = Environment.getExternalStorageDirectory();
            String path = extStore.getAbsolutePath() + File.separator + editFileName.getText().toString();
            String data = editText.getText().toString();
            File file = new File(path);
            try (FileOutputStream out = new FileOutputStream(file);){
                if (!file.exists()) file.createNewFile();
                OutputStreamWriter myOutWriter = new OutputStreamWriter(out);
                myOutWriter.append(data);
                myOutWriter.close();
            } catch (Exception e) {
                Log.i("fff", getString(R.string.fail));
            }
        }

        private void saveInInternalStorage() {
            File file = new File(editFileName.getText().toString());
            if (file.exists()) file.mkdir();
            try (FileOutputStream fOut = openFileOutput(file.getPath(), MODE_PRIVATE)) {
                fOut.write(editText.getText().toString().getBytes());
            } catch (Exception e) {
                Log.i("fff", getString(R.string.fail));
            }
        }
    }


    private class ReadThread extends Thread {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isInternal) {
                        readText.setText(readInInternalStorage(String.format(editFileName.getText().toString(), i)));
                    } else {
                        readText.setText(readInExternalStorage(String.format(editFileName.getText().toString(), i)));
                    }
                }
            });
        }

        private String readInInternalStorage(String editFileName) {
            try (FileInputStream fis = openFileInput(editFileName);
                 InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                 BufferedReader bufferedReader = new BufferedReader(isr)){
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (FileNotFoundException e) {
                sendNotification(getString(R.string.not_file));
                return getString(R.string.not_file);
            } catch (UnsupportedEncodingException e) {
                sendNotification(getString(R.string.unsupported));
                return getString(R.string.unsupported);
            } catch (IOException e) {
                sendNotification(getString(R.string.error));
                return getString(R.string.error);
            }
        }

        private String readInExternalStorage(final String fileName) {
            File extStore = Environment.getExternalStorageDirectory();
            String path = extStore.getAbsolutePath() + File.separator + fileName;
            String s;
            StringBuilder stringBuilder = new StringBuilder();
            File file = new File(path);
            try (FileInputStream fIn = new FileInputStream(file);
                 BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn))){
                while ((s = myReader.readLine()) != null) {
                    stringBuilder.append(s);
                }
            } catch (IOException e) {
                Log.i("fff", getString(R.string.fail));
            }
            return stringBuilder.toString();
        }
    }

    private void permission() {
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                69);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 69:
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permission();
                }
        }
    }

}
