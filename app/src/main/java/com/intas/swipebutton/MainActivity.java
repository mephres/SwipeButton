package com.intas.swipebutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

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
        for (int i = 0; i <= 20; i++) {
            data.add("Item " + i);
        }

        recyclerView = findViewById(R.id.recyclerView);
        itemAdapter = new RecyclerViewAdapter(this, data);
        itemAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(itemAdapter);

        SwipeHelper swipeHelper = new SwipeHelper(this) {
            @SuppressLint("ResourceType")
            @Override
            public void createRightButton(RecyclerView.ViewHolder viewHolder, List<BelowButton> belowButtons) {

                BelowButton deleteButton = new BelowButton(getApplicationContext())
                        .setText("Delete")
                        .setButtonTextSize(14)
                        .setImage(R.drawable.ic_baseline_delete_48)
                        .setButtonBackgroundColor(getResources().getColor(R.color.grey_300))
                        .setTextColor(getResources().getColor(R.color.red_600))
                        .setImageColor(getResources().getColor(R.color.red_600));
                deleteButton.setClickListener(new BelowButtonClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        final String item = itemAdapter.getData().get(pos);
                        itemAdapter.removeItem(pos);

                        Snackbar snackbar = Snackbar.make(recyclerView, "Запись удалена", Snackbar.LENGTH_LONG);
                        snackbar.setAction("Восстановить", new View.OnClickListener() {
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
                BelowButton likeButton = new BelowButton(getApplicationContext())
                        .setText("Like it")
                        .setButtonTextSize(18)
                        //.setImage(R.drawable.ic_baseline_thumb_up_alt_48)
                        .setButtonBackgroundColor(getResources().getColor(R.color.red_500))
                        .setTextColor(getResources().getColor(R.color.white))
                        .setImageColor(getResources().getColor(R.color.white));
                likeButton.setClickListener(new BelowButtonClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        final String item = itemAdapter.getData().get(pos);
                        Toast.makeText(getApplicationContext(), "Like it. Позиция - " + pos, Toast.LENGTH_SHORT).show();
                    }
                });
                belowButtons.add(deleteButton);
                belowButtons.add(likeButton);
            }

            @Override
            public void createLeftButton(RecyclerView.ViewHolder viewHolder, List<BelowButton> belowButtons) {
                BelowButton moveButton = new BelowButton(getApplicationContext())
                        .setText("Share it")
                        .setButtonTextSize(10)
                        .setImage(R.drawable.ic_baseline_share_24)
                        .setButtonCornerRadius(90)
                        .setButtonBackgroundColor(getResources().getColor(R.color.blue_700))
                        .setTextColor(getResources().getColor(R.color.white))
                        .setImageColor(getResources().getColor(R.color.white));
                moveButton.setClickListener(new BelowButtonClickListener() {
                    @Override
                    public void onClick(View view, int pos) {
                        final String item = itemAdapter.getData().get(pos);
                        Toast.makeText(getApplicationContext(), "Share it. Позиция - " + pos, Toast.LENGTH_SHORT).show();
                    }
                });
                belowButtons.add(moveButton);
            }
        };
        swipeHelper.setButtonMargin(10);
        swipeHelper.setButtonWidth(300);
        swipeHelper.attachToRecyclerView(recyclerView);
    }
}