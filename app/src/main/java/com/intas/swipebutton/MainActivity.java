package com.intas.swipebutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerViewAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> data = new ArrayList();
        for (int i = 1; i <= 20; i++) {
            data.add("Item " + i);
        }

        recyclerView = findViewById(R.id.recyclerView);
        itemAdapter = new RecyclerViewAdapter(this, data);
        itemAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(itemAdapter);

        SwipeHelper swipeHelper = new SwipeHelper(this) {
            @SuppressLint("ResourceType")
            @Override
            public void instantiateRightUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {

                SwipeHelper.UnderlayButton deleteButton = new SwipeHelper.UnderlayButton(getApplicationContext())
                        .setText("Delete")
                        .setButtonTextSize(14)
                        .setImage(R.drawable.ic_baseline_delete_48)
                        .setButtonBackgroundColor(getResources().getColor(R.color.grey_300))
                        .setTextColor(getResources().getColor(R.color.red_600))
                        .setImageColor(getResources().getColor(R.color.red_600));
                deleteButton.setClickListener(new UnderlayButtonClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        final String item = itemAdapter.getData().get(pos);
                        itemAdapter.removeItem(pos);

                        Snackbar snackbar = Snackbar.make(recyclerView, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                itemAdapter.restoreItem(item, pos);
                                recyclerView.scrollToPosition(pos);
                            }
                        });

                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();
                    }
                });
                SwipeHelper.UnderlayButton likeButton = new SwipeHelper.UnderlayButton(getApplicationContext())
                        .setText("Like it")
                        .setButtonTextSize(18)
                        //.setImage(R.drawable.ic_baseline_thumb_up_alt_48)
                        .setButtonBackgroundColor(getResources().getColor(R.color.red_500))
                        .setTextColor(getResources().getColor(R.color.white))
                        .setImageColor(getResources().getColor(R.color.white));
                likeButton.setClickListener(new UnderlayButtonClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        final String item = itemAdapter.getData().get(pos);
                    }
                });
                underlayButtons.add(deleteButton);
                underlayButtons.add(likeButton);
            }

            @Override
            public void instantiateLeftUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                SwipeHelper.UnderlayButton moveButton = new SwipeHelper.UnderlayButton(getApplicationContext())
                        .setText("Share it")
                        .setButtonTextSize(10)
                        .setImage(R.drawable.ic_baseline_share_24)
                        .setButtonCornerRadius(90)
                        .setButtonBackgroundColor(getResources().getColor(R.color.blue_700))
                        .setTextColor(getResources().getColor(R.color.white))
                        .setImageColor(getResources().getColor(R.color.white));
                moveButton.setClickListener(new UnderlayButtonClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        final String item = itemAdapter.getData().get(pos);
                    }
                });
                underlayButtons.add(moveButton);
            }
        };
        swipeHelper.setButtonMargin(10);
        swipeHelper.setButtonWidth(300);
        swipeHelper.attachToRecyclerView(recyclerView);
    }

    // эффект нажатия кнопки
    public void runAnimationButton(@NotNull View view) {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.image_button_animation);

        view.startAnimation(animation);
    }
}