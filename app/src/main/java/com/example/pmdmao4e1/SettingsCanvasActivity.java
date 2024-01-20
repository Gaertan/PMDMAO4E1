package com.example.pmdmao4e1;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsCanvasActivity extends AppCompatActivity {
    private int currentStrokeSize;
    private SeekBar seekBar;
    private TextView textViewCanvasSize;
    private EditText editTextNumberCanvasSize;
    private Button buttonOkSettingsCanvas;

    private static final int MIN_BRUSH_SIZE = 1;
    private static final int MAX_BRUSH_SIZE =110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_canvas);

        seekBar = findViewById(R.id.seekBarStrokeSize);
        textViewCanvasSize = findViewById(R.id.textViewCanvasSize);
        editTextNumberCanvasSize = findViewById(R.id.editTextNumberStrokeSize);
        buttonOkSettingsCanvas = findViewById(R.id.buttonOkSettingsCanvas);

        seekBar.setMax(MAX_BRUSH_SIZE);
        seekBar.setMin(MIN_BRUSH_SIZE);

        //tamaño actual del lienzo desde la actividad principal
        currentStrokeSize = getIntent().getIntExtra("strokeSize", MIN_BRUSH_SIZE);
        editTextNumberCanvasSize.setText(String.valueOf(currentStrokeSize));
        seekBar.setProgress(currentStrokeSize);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextNumberCanvasSize.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        buttonOkSettingsCanvas.setOnClickListener(view -> {
            int newStrokeSize = Integer.parseInt(editTextNumberCanvasSize.getText().toString());
            getIntent().putExtra("newStrokeSize", newStrokeSize);
            setResult(RESULT_OK, getIntent());
            finish();
        });
    }
}