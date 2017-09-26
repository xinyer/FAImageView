package kr.pe.burt.android.lib.faimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

import kr.pe.burt.android.lib.androidchannel.Timer;

/**
 * Created by burt on 15. 10. 22..
 */
public class FAImageView extends ImageView {

    public interface OnStartAnimationListener {
        void onStartAnimation();
    }

    public interface OnFinishAnimationListener {
        void onFinishAnimation(boolean isLoopAnimation);
    }

    public interface OnFrameChangedListener {
        void onFrameChanged(int index);
    }

    private final static int DEFAULT_INTERVAL = 1000;       // 1s

    Timer timer;
    int interval = DEFAULT_INTERVAL;

    ArrayList<Bitmap> drawableList;
    int currentFrameIndex = -1;
    boolean loop = false;
    boolean didStoppedAnimation = true;
    int animationRepeatCount = 1;
    boolean restoreFirstFrameWhenFinishAnimation = true;

    private OnStartAnimationListener    startAnimationListener  = null;
    private OnFrameChangedListener      frameChangedListener    = null;
    private OnFinishAnimationListener   finishAnimationListener = null;

    public FAImageView(Context context) {
        this(context, null);
    }

    public FAImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FAImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        drawableList = new ArrayList<>();
    }

    /**
     * set inteval in milli seconds
     * @param interval interval of a frame.
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void addImageFrame(Bitmap bitmap) {
        this.drawableList.add(bitmap);
    }

    public void addImageFrame(int imageRes) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageRes);
        addImageFrame(bitmap);
    }

    public void startAnimation() {

        if(drawableList == null || drawableList.size() == 0) {
            throw new IllegalStateException("You shoud add frame at least one frame");
        }

        if(didStoppedAnimation == false) return;

        didStoppedAnimation = false;

        if(startAnimationListener != null) {
            startAnimationListener.onStartAnimation();
        }

        // for implementing resume
        if(currentFrameIndex == -1) {
            currentFrameIndex = 0;
        }
        setImageBitmap(drawableList.get(currentFrameIndex));

        if(timer == null) {
            timer = new Timer(interval, new Timer.OnTimer() {
                @Override
                public void onTime(Timer timer) {
                    if( didStoppedAnimation ) return;

                    currentFrameIndex++;
                    if(currentFrameIndex == drawableList.size()) {
                        if(loop) {
                            if(finishAnimationListener != null) {
                                finishAnimationListener.onFinishAnimation(loop);
                            }
                            currentFrameIndex = 0;
                        } else {
                            animationRepeatCount--;

                            if(animationRepeatCount <= 0) {
                                currentFrameIndex = drawableList.size() - 1;

                                stopAnimation();

                                if(finishAnimationListener != null) {
                                    finishAnimationListener.onFinishAnimation(loop);
                                }

                            } else {
                                currentFrameIndex = 0;
                            }
                        }
                    }

                    if ( didStoppedAnimation == false ) {
                        if (frameChangedListener != null) {
                            frameChangedListener.onFrameChanged(currentFrameIndex);
                        }
                        setImageBitmap(drawableList.get(currentFrameIndex));
                    } else {
                        if(restoreFirstFrameWhenFinishAnimation) {
                            setImageBitmap(drawableList.get(0));
                        }
                        currentFrameIndex = -1;
                    }
                }
            });
            timer.stop();
        }
        if(timer.isAlive() == false) {
            timer.start();
        }
    }

    public boolean isAnimating() {
        return didStoppedAnimation == false;
    }

    public void stopAnimation() {
        if(timer != null && timer.isAlive()) {
            timer.stop();
        }
        timer = null; didStoppedAnimation = true;

    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setRestoreFirstFrameWhenFinishAnimation(boolean restore) {
        this.restoreFirstFrameWhenFinishAnimation = restore;
    }

    public void setAnimationRepeatCount(int animationRepeatCount) {
        this.animationRepeatCount = animationRepeatCount;
    }

    public void reset() {
        stopAnimation();
        if(drawableList != null){
            for (Bitmap bitmap : drawableList) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            drawableList.clear();
            drawableList = null; 
        }
        currentFrameIndex = -1;
    }

    public void setOnStartAnimationListener(OnStartAnimationListener listener) {
        startAnimationListener = listener;
    }

    public void setOnFrameChangedListener(OnFrameChangedListener listener) {
        frameChangedListener = listener;
    }

    public void setOnFinishAnimationListener(OnFinishAnimationListener listener) {
        finishAnimationListener = listener;
    }
}
