package com.giskook.express_delivery;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class FrameLayoutWithHole extends FrameLayout {
    private Bitmap mEraserBitmap;
    private Canvas mEraserCanvas;
    private Paint mEraser;
    private Context mContext;

    private int mBackgroundColor;
    public Rect mRect;
    public int mDisplayWidth;
    public int mDisplayHeight;

    public FrameLayoutWithHole(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public FrameLayoutWithHole(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.FrameLayoutWithHole);
        mBackgroundColor = ta.getColor(R.styleable.FrameLayoutWithHole_background_color,-1);
        mRect = new Rect();
        mRect.left = ta.getInt(R.styleable.FrameLayoutWithHole_left,0);
        mRect.top  =  ta.getInt(R.styleable.FrameLayoutWithHole_top,0);
        mRect.right = ta.getInt(R.styleable.FrameLayoutWithHole_right,0);
        mRect.bottom = ta.getInt(R.styleable.FrameLayoutWithHole_bottom,0);
        init(null,0);
        ta.recycle();
    }

    public FrameLayoutWithHole(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FrameLayoutWithHole(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public FrameLayoutWithHole(Context context, int backgroundColor,int left, int top
            ,int right,int bottom){
        this(context);

        mBackgroundColor = backgroundColor;
        this.mRect.left = left;
        this.mRect.top = top;
        this.mRect.right = right;
        this.mRect.bottom = bottom;
        init(null,0);
    }
    private void init(AttributeSet  attrs, int defStyle) {
        setWillNotDraw(false);
//        mDensity = mContext.getResources().getDisplayMetrics().density;

        Point size = new Point();
        size.x = mContext.getResources().getDisplayMetrics().widthPixels;
        size.y = mContext.getResources().getDisplayMetrics().heightPixels;
        this.mDisplayHeight = size.y;
        this.mDisplayWidth = size.x;

        // left top right bottom scale&
        mRect.left = size.x/5;
        mRect.right = mRect.left + size.x*3/5;
        mRect.top = size.y/7;
        mRect.bottom = mRect.top + (mRect.right-mRect.left)/5;

        mBackgroundColor  = mBackgroundColor !=-1?mBackgroundColor: Color.parseColor("#55000000");

        mEraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        mEraserCanvas = new Canvas(mEraserBitmap);


        mEraser = new Paint();
        mEraser.setColor(0xFFFFFFFF);
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);

        Log.d("tourguide", "getHeight: " + size.y);
        Log.d("tourguide", "getWidth: " + size.x);

    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mEraserBitmap.eraseColor(Color.TRANSPARENT);
        mEraserCanvas.drawColor(mBackgroundColor);

        mEraserCanvas.drawRect(mRect, mEraser);

        canvas.drawBitmap(mEraserBitmap, 0, 0, null);

    }
}
