package com.sheen.jgkit.demo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.sheen.jgkit.ntv.Common;

public class MainActivity extends AppCompatActivity {

    static final int IRC_SELECT_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnSelectImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSelectImageButtonClick(view);
            }
        });
    }

    public void OnSelectImageButtonClick(View view) {
        String message = "Message from JIN:" +  Common.getVersion();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        return;
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IRC_SELECT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IRC_SELECT_IMAGE: {
                if (resultCode == RESULT_OK) {
                    new AlertDialog.Builder(this)
                            .setTitle("Note")
                            .setMessage(String.format("You have selected: %s.", data.toUri(0)))
                            .show();

                    Uri uri = data.getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Drawable d = Drawable.createFromStream(inputStream, uri.toString() );
                        //findViewById(R.id.backgroundView).setBackground(d);
                    } catch (FileNotFoundException e) {
                        Log.e("VividImageViwe", "Failed to read the image data.");
                    }
                }
            }
            break;
            default:
                break;
        }
    }
}
