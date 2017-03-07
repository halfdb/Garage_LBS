package ecnu.cs14.garagelbs.locator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import ecnu.cs14.garagelbs.support.data.MapData;

import java.util.HashSet;

/**
 * An experimental MapView.
 * Created by K on 2017/1/21.
 */

public class MapView extends View {
    private final static String TAG = MapView.class.getName();

    private Paint mShapePaint;
    private Paint mBackgroundPaint;
    private Paint mPositionDotPaint;
    private View mEmptyView;

    public MapView(Context context) {
        super(context);
        initPaint();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public void setEmptyView(@NonNull View v) {
        mEmptyView = v;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != VISIBLE) {
            mEmptyView.setVisibility(VISIBLE);
        } else {
            mEmptyView.setVisibility(GONE);
        }
    }

    private void initPaint() {
        mShapePaint = new Paint();
        mShapePaint.setColor(Color.DKGRAY);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.LTGRAY);
        mPositionDotPaint = new Paint();
        mPositionDotPaint.setColor(Color.BLUE);
    }

    private Rect mBackgroundRect = new Rect();
    private HashSet<MapData.Shape.Rect> mRects = new HashSet<>();
    private HashSet<MapData.Shape.Circle> mCircles = new HashSet<>();
    public void setMap(MapData map) {
        mBackgroundRect = new Rect(
                0,
                0,
                map.width,
                map.height
        );
        for (MapData.Shape shape: map.shapes) {
            switch (shape.type) {
                case RECT:
                {
                    mRects.add((MapData.Shape.Rect) shape);
                    break;
                }
                case CIRCLE:
                {
                    mCircles.add((MapData.Shape.Circle) shape);
                    break;
                }
            }
        }
        invalidate();
    }

    private HashSet<Pair<Integer, Integer>> mPositions = new HashSet<>();
    private Pair<Integer, Integer> mPosition;
    private static final float DOT_RADIUS = 20.0f;
    public void setPositionDot(Pair<Integer, Integer> position) {
        mPosition = position;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        // Log.i(TAG, "onMeasure: wMode: " + wMode + " hMode: " + hMode + " wSize: " + wSize + " hSize: " + hSize);
        setMeasuredDimension(wSize, hSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sx = (float) getWidth() / (float) mBackgroundRect.right;
        float sy = (float) getHeight() / (float) mBackgroundRect.bottom;
        float s = sx < sy ? sx : sy;
        canvas.translate(
                (getWidth() / 2f - mBackgroundRect.centerX() * s),
                (getHeight() / 2f - mBackgroundRect.centerY() * s)
        );
        canvas.scale(s, s, 0, 0);
        canvas.clipRect(mBackgroundRect);
        canvas.drawRect(mBackgroundRect, mBackgroundPaint);
        for (MapData.Shape.Rect rect :
                mRects) {
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, mShapePaint);
        }
        for (MapData.Shape.Circle circle :
                mCircles) {
            canvas.drawCircle(circle.center_left, circle.center_top, circle.radius, mShapePaint);
        }
        if (mPosition != null) {
            canvas.drawCircle(mPosition.first, mPosition.second, DOT_RADIUS / s, mPositionDotPaint);
        }
    }
}
