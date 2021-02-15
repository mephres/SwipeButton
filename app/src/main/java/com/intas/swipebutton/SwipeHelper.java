package com.intas.swipebutton;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public abstract class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    private RecyclerView recyclerView;
    private List<UnderlayButton> buttons;
    private List<UnderlayButton> leftbuttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private Map<Integer, List<UnderlayButton>> buttonsLeftBuffer;

    private Queue<Integer> recoverQueue;
    private Context context;
    private static int buttonMargin = 10;
    private static int buttonWidth = 200;

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (UnderlayButton button : buttons) {
                if (button.onClick(e.getX(), e.getY())) {
                    break;
                }
            }
            for (UnderlayButton button : leftbuttons) {
                if (button.onClick(e.getX(), e.getY())) {
                    break;
                }
            }
            return true;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (swipedPos < 0) return false;
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
            View swipedItem = swipedViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(e);
                else {
                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        }
    };

    public SwipeHelper(Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

        this.buttons = new ArrayList<>();
        this.leftbuttons = new ArrayList<>();

        this.gestureDetector = new GestureDetector(context, gestureListener);

        buttonsBuffer = new HashMap<>();
        buttonsLeftBuffer = new HashMap<>();

        this.context = context;
        recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };
    }

    public int getButtonMargin() {
        return buttonMargin;
    }

    public void setButtonMargin(int buttonMargin) {
        this.buttonMargin = buttonMargin;
    }

    public int getButtonWidth() {
        return buttonWidth;
    }

    public void setButtonWidth(int buttonWidth) {
        this.buttonWidth = buttonWidth;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        if (buttons.size() > 0) {
            swipeThreshold = 0.5f * buttons.size() * buttonWidth;
        }

        if (buttonsLeftBuffer.containsKey(swipedPos))
            leftbuttons = buttonsLeftBuffer.get(swipedPos);
        else
            leftbuttons.clear();

        buttonsLeftBuffer.clear();
        if (leftbuttons.size() > 0) {
            swipeThreshold = 0.5f * leftbuttons.size() * buttonWidth;
        }

        recoverSwipedItem();
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0) {
            swipedPos = pos;
            return;
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateRightUnderlayButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * buttonWidth / itemView.getWidth();
                translationX = buffer.size() > 0 ? translationX - buttonMargin : translationX;
                drawRightButtons(c, itemView, buffer, pos, translationX);
            } else if (dX > 0) {
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsLeftBuffer.containsKey(pos)) {
                    instantiateLeftUnderlayButton(viewHolder, buffer);
                    buttonsLeftBuffer.put(pos, buffer);
                } else {
                    buffer = buttonsLeftBuffer.get(pos);
                }

                translationX = dX * buffer.size() * buttonWidth / itemView.getWidth();
                translationX = buffer.size() > 0 ? translationX + buttonMargin : translationX;

                drawLeftButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            int pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    private void drawRightButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX) {
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (UnderlayButton button : buffer) {
            float left = right - dButtonWidth + buttonMargin;
            button.onDraw(c, new RectF(
                    left,
                    itemView.getTop(),
                    right,
                    itemView.getBottom()), pos);

            right = left - buttonMargin;
        }
    }

    private void drawLeftButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX) {

        float left = itemView.getLeft();
        float dButtonWidth = (1) * dX / buffer.size();

        for (UnderlayButton button : buffer) {
            float right = left + dButtonWidth - buttonMargin;
            button.onDraw(c, new RectF(
                    left,
                    itemView.getTop(),
                    right,
                    itemView.getBottom()), pos);

            left = right + buttonMargin;
        }
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.recyclerView.setOnTouchListener(onTouchListener);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(this.recyclerView);
    }

    public abstract void instantiateRightUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    public abstract void instantiateLeftUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    public interface UnderlayButtonClickListener {
        void onClick(View view, int pos);
    }

    public static class UnderlayButton extends View {

        private String text = "";
        private int imageResId = 0;
        private int buttonBackgroundColor = Color.WHITE;
        private int textColor = Color.BLACK;
        private int imageColor = Color.BLACK;
        private int buttonTextSize = 14;
        private int buttonCornerRadius = 8;

        private int pos;
        private RectF clickRegion;
        private Context context;
        private UnderlayButtonClickListener clickListener;

        public UnderlayButton(Context context) {
            super(context);
            this.clickListener = clickListener;
            this.context = context;
        }

        public String getText() {
            return text;
        }

        public UnderlayButton setText(String text) {
            this.text = text;
            return this;
        }

        public int getImage() {
            return imageResId;
        }

        public UnderlayButton setImage(int imageResId) {
            this.imageResId = imageResId;
            return this;
        }

        public int getButtonBackgroundColor() {
            return buttonBackgroundColor;
        }


        public UnderlayButton setButtonBackgroundColor(int buttonBackgroundColor) {
            this.buttonBackgroundColor = buttonBackgroundColor;
            return this;
        }

        public int getTextColor() {
            return textColor;
        }

        public UnderlayButton setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public int getImageColor() {
            return imageColor;
        }

        public UnderlayButton setImageColor(int imageColor) {
            this.imageColor = imageColor;
            return this;
        }

        public int getButtonTextSize() {
            return buttonTextSize;
        }

        public UnderlayButton setButtonTextSize(int buttonTextSize) {
            this.buttonTextSize = buttonTextSize;
            return this;
        }

        public int getButtonCornerRadius() {
            return buttonCornerRadius;
        }

        public UnderlayButton setButtonCornerRadius(int buttonCornerRadius) {
            this.buttonCornerRadius = buttonCornerRadius;
            return this;
        }

        public UnderlayButtonClickListener getClickListener() {
            return clickListener;
        }

        public void setClickListener(UnderlayButtonClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public boolean onClick(float x, float y) {
            if (clickRegion != null && clickRegion.contains(x, y)) {
                clickListener.onClick(this, pos);
                return true;
            }

            return false;
        }

        public void onDraw(Canvas c, RectF rect, int pos) {
            Paint p = new Paint();

            p.setColor(this.buttonBackgroundColor);
            c.drawRoundRect(rect, this.buttonCornerRadius, this.buttonCornerRadius, p);

            drawImage(imageResId, c, rect, p);
            drawText(text, c, rect, p);

            clickRegion = rect;
            this.pos = pos;
        }

        private void drawImage(int imageResId, Canvas c, RectF button, Paint p) {

            if (imageResId == 0) {
                return;
            }

            Bitmap bitmap = drawableToBitmap(imageResId);

            float top;
            if (!text.isEmpty()) {
                bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
                top = button.centerY() - (bitmap.getWidth() / 2) - 25;
            } else {
                bitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, false);
                top = button.centerY() - (bitmap.getWidth() / 2);
            }
            //float xx = cWidth / 2f - bitmap.getWidth() / 2 - r.left + 5;

            c.drawBitmap(bitmap, button.centerX() - (bitmap.getWidth() / 2), top, p);

        }

        private void drawText(String text, Canvas c, RectF button, Paint p) {

            float textSize = Resources.getSystem().getDisplayMetrics().density * this.buttonTextSize;
            p.setColor(textColor);
            p.setAntiAlias(true);
            p.setTextSize(textSize);
            p.setFakeBoldText(true);

            p.setTextScaleX(Math.abs(button.left - button.right) / buttonWidth);
            float textWidth = p.measureText(text);

            int topOffset = this.getImage() > 0 ? 30 : 0;

            c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (textSize / 2) + topOffset, p);
        }

        private Bitmap drawableToBitmap(int vectorDrawableId) {
            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                Drawable vectorDrawable = context.getApplicationContext().getDrawable(vectorDrawableId);
                vectorDrawable.setTint(imageColor);
                bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                        vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);
            } else {
                bitmap = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), vectorDrawableId);
            }
            return bitmap;
        }

        // эффект нажатия кнопки
        public void runAnimationButton(View view) {
            Animation animation = AnimationUtils.loadAnimation(context.getApplicationContext(),
                    R.anim.image_button_animation);

            view.startAnimation(animation);
        }
    }
}
