package com.example.pmdmao4e1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
    private int currentStrokeSize;
    private final int DEFAULT_SIZE_PINCEL = 5;
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

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);

        customView = new CustomView(this);
        rl = findViewById(R.id.Rel);
        rl.addView(customView);


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(getResources().getColor(android.R.color.black));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        try {
            int trazoGuardado = obtenerTrazoGuardado();
            Toast.makeText(this, "Tamaño de pincelada cargado: " + trazoGuardado, Toast.LENGTH_SHORT).show();
            currentStrokeSize=trazoGuardado;
            paint.setStrokeWidth(currentStrokeSize);
        }catch (Exception e){
            currentStrokeSize = DEFAULT_SIZE_PINCEL;

            paint.setStrokeWidth(DEFAULT_SIZE_PINCEL);}


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
//INICIO CUSTOM VIEW
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
          //  super.onDraw(canvas);
          //  canvas.drawBitmap(drawBitmap, 0, 0, drawBitmapPaint);
           // canvas.drawPath(path, paint);

            for (Path p : paths) {
                canvas.drawPath(p, paint);
            }
            canvas.drawPath(path, paint);



        }

  // 3 METODOS DEPRECATED, ANTES DE METER UNDO Y REDO --------------------------
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
//------------------------------
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float touchX = event.getX();
            float touchY = event.getY();

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touch_start(touchX, touchY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(touchX, touchY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
                default:
                    return false;
            }
            return true;
        }


        //TOUCHES
        private void touch_start(float x, float y) {
            undonePaths.clear();
            path.reset();
            path.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                path.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            path.lineTo(mX, mY);
            canvas.drawPath(path, paint);
            paths.add(path);
            path = new Path();
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(drawBitmap);
        }
    }
//FIN DE CUSTOM VIEW
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
            cambiarSizeSettings();
            return true;
        }
        if (itemId == R.id.undo) {
            undo();
            return true;
        }
        if (itemId == R.id.redo) {
            redo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void borrarCanvas(){
        //MANTIENE DESHACER(UNO A UNO):
        for (Path p : paths) {
            undonePaths.add(p);
        }
        paths.removeAll(paths);
        customView.invalidate();
        //NO MANTIENE DESHACER:
      /*
      for (Path p : paths) {
            p.reset();}
       for (Path p : undonePaths) {
            p.reset();}
        drawBitmap = Bitmap.createBitmap(customView.getWidth(), customView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(drawBitmap);
        customView.invalidate();*/
    }

    private void guardar(){
        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String time = formatter.format(new Date());
        String fileName = "firma_" + time.toString() + ".jpg";
        File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            drawBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Archivo con la firma guardado: " + file.toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar el archivo de firma ", Toast.LENGTH_SHORT).show();
            System.err.println(e.getMessage());
        }
    }


    //ACTIVITY 2 INTENT FOR RESULT
    private static final int REQUEST_SETTINGS_STROKE = 1;
   private void cambiarSizeSettings(){
       Intent intent = new Intent(this, SettingsCanvasActivity.class);
       intent.putExtra("strokeSize", currentStrokeSize);
       startActivityForResult(intent, REQUEST_SETTINGS_STROKE);
   }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS_STROKE) {
            if (resultCode == RESULT_OK) {

                int strokeSize = data.getIntExtra("newStrokeSize",DEFAULT_SIZE_PINCEL);
                guardarNuevoTrazo(strokeSize);
                paint.setStrokeWidth(strokeSize);
                Toast.makeText(this, "Tamaño de pincelada: " + strokeSize, Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onBackPressed() {
        if (!paths.isEmpty()) {
            undo();
        } else {
            super.onBackPressed();
        }
    }

    public void undo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
           customView.invalidate();
        }
    }

    public void redo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            customView.invalidate();
        }
    }


    private void guardarNuevoTrazo(int nuevoTrazo) {

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("trazo", nuevoTrazo);
            editor.apply();
        }
        catch (Exception e){
            Toast.makeText(this, "Se ha producido una excepcion guardando el valor de settings", Toast.LENGTH_SHORT).show();

        }

    }
    private int obtenerTrazoGuardado() {

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            return sharedPreferences.getInt("trazo", DEFAULT_SIZE_PINCEL);
        }
        catch (Exception e){

            Toast.makeText(this, "Se ha producido una excepcion obteniendo el valor de settings", Toast.LENGTH_SHORT).show();

            return DEFAULT_SIZE_PINCEL;}


    }
}