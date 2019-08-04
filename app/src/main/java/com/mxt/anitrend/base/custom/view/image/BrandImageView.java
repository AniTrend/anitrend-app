package com.mxt.anitrend.base.custom.view.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

/**
 * Created by max on 2017/12/10.
 * always 4:3 aspect ratio images
 * borrowed functionality from plaid BadgedFourThreeImage
 */

public class BrandImageView extends AppCompatImageView implements CustomView {

    private Drawable badge;
    private boolean badgeBoundsSet = true;
    private int badgeGravity;
    private int badgePadding;

    private int spanSize;
    private Point deviceDimens = new Point();

    public BrandImageView(Context context) {
        super(context);
        onInit();
    }

    public BrandImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public BrandImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        badge = new GifBadge(getContext());
        badgeGravity = Gravity.END | Gravity.TOP;
        CompatUtil.INSTANCE.getScreenDimens(deviceDimens, getContext());
        spanSize = getResources().getInteger(R.integer.grid_giphy_x3);
        badgePadding = getContext().getResources().getDimensionPixelSize(R.dimen.lg_margin);
        badge.setColorFilter(CompatUtil.INSTANCE.getColorFromAttr(getContext(), R.attr.titleColor), PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int Width;
        if((Width = MeasureSpec.getSize(widthMeasureSpec)) == 0)
            Width = (deviceDimens.x / spanSize) - badgePadding;

        int Height = (int) (Width * (3.3f/4f));
        super.onMeasure(MeasureSpec.makeMeasureSpec(Width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(Height, MeasureSpec.EXACTLY));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (!badgeBoundsSet) {
            layoutBadge();
        }
        badge.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutBadge();
    }

    private void layoutBadge() {
        Rect badgeBounds = badge.getBounds();
        Gravity.apply(badgeGravity, badge.getIntrinsicWidth(), badge.getIntrinsicHeight(),
                new Rect(0, 0, getWidth(), getHeight()), badgePadding, badgePadding, badgeBounds);
        badge.setBounds(badgeBounds);
        badgeBoundsSet = true;
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    /**
     * A drawable for indicating that an image is animated
     */
    private static class GifBadge extends Drawable {

        private static final String GIF = "GIPHY";
        private static final int TEXT_SIZE = 8;    // sp
        private static final int PADDING = 4;       // dp
        private static final int CORNER_RADIUS = 2; // dp
        private static final int BACKGROUND_COLOR = Color.WHITE;
        private static final String TYPEFACE = "sans-serif-black";
        private static final int TYPEFACE_STYLE = Typeface.NORMAL;
        private static Bitmap bitmap;
        private static int width;
        private static int height;
        private final Paint paint;

        GifBadge(Context context) {
            if (bitmap == null) {
                final DisplayMetrics dm = context.getResources().getDisplayMetrics();
                final float density = dm.density;
                final float scaledDensity = dm.scaledDensity;
                final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint
                        .SUBPIXEL_TEXT_FLAG);
                textPaint.setTypeface(Typeface.create(TYPEFACE, TYPEFACE_STYLE));
                textPaint.setTextSize(TEXT_SIZE * scaledDensity);

                final float padding = PADDING * density;
                final float cornerRadius = CORNER_RADIUS * density;
                final Rect textBounds = new Rect();
                textPaint.getTextBounds(GIF, 0, GIF.length(), textBounds);
                height = (int) (padding + textBounds.height() + padding);
                width = (int) (padding + textBounds.width() + padding);
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.setHasAlpha(true);
                final Canvas canvas = new Canvas(bitmap);
                final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                backgroundPaint.setColor(BACKGROUND_COLOR);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    canvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, backgroundPaint);
                else canvas.drawRect(0, 0, width, height, backgroundPaint);
                // punch out the word 'GIF', leaving transparency
                textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawText(GIF, padding, height - padding, textPaint);
            }
            paint = new Paint();
        }

        @Override
        public int getIntrinsicWidth() {
            return width;
        }

        @Override
        public int getIntrinsicHeight() {
            return height;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawBitmap(bitmap, getBounds().left, getBounds().top, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            // ignored
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

    }
}
