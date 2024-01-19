package com.example.pmdmao4e1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Bitmap drawBitmap;
    private Canvas canvas;
    private Path path;

    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private Paint drawBitmapPaint;
    private RelativeLayout rl;
    private CustomView customView;
    private Paint paint;
    private Button buttonGuardar;
    private Button buttonBorrar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customView = new CustomView(this);
        rl = findViewById(R.id.Rel);
        rl.addView(customView);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(getResources().getColor(android.R.color.holo_green_dark));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(20);


        buttonGuardar = findViewById(R.id.buttonGuardar);
        buttonBorrar = findViewById(R.id.buttonBorrar);

        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });

        buttonBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarCanvas();
            }
        });

    }

    public class CustomView extends View {

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        public CustomView(Context context) {
            super(context);
            path = new Path();
            drawBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(drawBitmap, 0, 0, drawBitmapPaint);
            canvas.drawPath(path, paint);
        }

        private void touchStart(float x, float y) {
            path.reset();
            path.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touchMove(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touchUp() {
            path.lineTo(mX, mY);
            canvas.drawPath(path, paint);
            path.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    invalidate();
                    break;
            }
            return true;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(drawBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        paint.setXfermode(null);
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings_canvas) {
          //  cambiarSizeSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void borrarCanvas(){
        path.reset();
        drawBitmap = Bitmap.createBitmap(customView.getWidth(), customView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(drawBitmap);
        customView.invalidate();
    }

    private void guardar(){

        String pattern = "mm ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String time = formatter.format(new Date());
        String path = ("/d-codepages" + time + ".png");

        File file = new File(Environment.getExternalStorageDirectory() + path);

        try {
            drawBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            Toast.makeText(this, "File Saved ::" + path, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "ERROR" + e.toString(), Toast.LENGTH_SHORT).show();
        }


    }

   // private void cambiarSizeSettings(){        Intent intent = new Intent(this, SettingsCanvasActivity.class);
  //      startActivityForResult(intent, REQUEST_SETTINGS_CANVAS);}

    private static final int REQUEST_SETTINGS_CANVAS = 1;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS_CANVAS) {
            if (resultCode == RESULT_OK) {
                int canvasSize = data.getIntExtra("canvasSize", 0);
                // Aquí puedes utilizar el valor de canvasSize
                Toast.makeText(this, "Tamaño de canvas seleccionado: " + canvasSize, Toast.LENGTH_SHORT).show();
            }
        }
    }



}