package danisharsalan.studybuddy;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Created by Danish on 11/17/16.
 */

public class RoundedTransformation implements com.squareup.picasso.Transformation {
    private final int radius;
    private final int margin;  // dp

    // radius is corner radii in dp
    // margin is the board in dp
    public RoundedTransformation(final int radius, final int margin) {
        this.radius = radius;
        this.margin = margin;
    }

    @Override
    public Bitmap transform(final Bitmap source) {


        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

        if (source != output) {
            source.recycle();
        }

        Paint paint1 = new Paint();
        paint1.setColor(Color.TRANSPARENT);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        paint1.setStrokeWidth(2);
        canvas.drawCircle((source.getWidth() - margin)/2, (source.getHeight() - margin)/2, radius-2, paint1);


        return output;
    }

    @Override
    public String key() {
        return "rounded";
    }
}

//public class RoundedTransformation implements Transformation {
//    @Override
//    public Bitmap transform(Bitmap source) {
//        try {
//
//            int size = Math.min(source.getWidth(), source.getHeight());
//
//            int x = (source.getWidth() - size) / 2;
//            int y = (source.getHeight() - size) / 2;
//
//            Bitmap squaredBitmap = Bitmap
//                    .createBitmap(source, x, y, size, size);
//            if (squaredBitmap != source) {
//                source.recycle();
//            }
//
//            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
//
//            Canvas canvas = new Canvas(bitmap);
//            Paint paint = new Paint();
//            BitmapShader shader = new BitmapShader(squaredBitmap,
//                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
//            paint.setShader(shader);
//            paint.setAntiAlias(true);
//
//            float r = size / 2f;
//            canvas.drawCircle(r, r, r, paint);
//
//            squaredBitmap.recycle();
//            return bitmap;
//        } catch (Exception e) {
//            // TODO: handle exception
//            if (BuildConfig.DEBUG)
//                e.printStackTrace();
//            return null;
//        }
//
//    }
//
//    @Override
//    public String key() {
//        return "circle";
//    }
//}
