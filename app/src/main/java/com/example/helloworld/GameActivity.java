package com.example.helloworld;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    private int pairCount = 0;  // 配对成功计数
    private int elapsedTime = 0;  // 过去的时间（秒）
    private Timer timer;
    private TextView pairCountTextView;
    private TextView timerTextView;

    private GameImageAdapter adapter;
    private ListView listView;
    private String[] selectedImages;  // 从 MainActivity 传递过来的图片数组
    private String[] shuffledImages;  // 存储打乱后的图片 URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        pairCountTextView = findViewById(R.id.pairCountTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // 获取从 MainActivity 传递过来的图片数组
        selectedImages = getIntent().getStringArrayExtra("selectedImages");

        // 复制一份以生成 12 张图片
        shuffledImages = new String[selectedImages.length * 2];
        System.arraycopy(selectedImages, 0, shuffledImages, 0, selectedImages.length);
        System.arraycopy(selectedImages, 0, shuffledImages, selectedImages.length, selectedImages.length);

        // 打乱图片顺序
        shuffleArray(shuffledImages);

        startTimer();

        adapter = new GameImageAdapter(this, shuffledImages);
        listView = findViewById(R.id.listView);
        if (listView != null) {
            listView.setAdapter(adapter);
        }
    }

    // 辅助方法：打乱数组顺序
    private void shuffleArray(String[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // 交换元素
            String temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime++;
                updateTimerText();
            }
        }, 1000, 1000);
    }

    private void updateTimerText() {
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;
        final String timerText = String.format("Time: %02d:%02d", minutes, seconds);

        // 在主线程更新 UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerTextView.setText(timerText);
            }
        });
    }

    public ListView getListView() {
        return listView;
    }

    void updatePairCountText() {
        final String pairCountText = "Pairs: " + pairCount;

        // 在主线程更新 UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pairCountTextView.setText(pairCountText);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public class GameImageAdapter extends ArrayAdapter<String> {

        private Context context;
        private String[] imageUrls;
        private boolean[] isSelected;
        private boolean isPaired;
        private List<Integer> pairedIndexes = new ArrayList<>();
        //private int PairCount;
        private List<Integer> lastTwoSelections = new ArrayList<>();
        private Handler handler;
        private final Object pairCountLock = new Object();
        public GameImageAdapter(Context context, String[] imageUrls) {
            super(context, R.layout.row, imageUrls);
            this.context = context;
            this.imageUrls = imageUrls;
            this.isSelected = new boolean[imageUrls.length];
            this.handler = new Handler(Looper.getMainLooper());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = inflater.inflate(R.layout.row, parent, false);
            }

            int[] imageViews = new int[]{R.id.imageView0, R.id.imageView1, R.id.imageView2, R.id.imageView3};
            int[] selectedIcons = new int[]{R.id.selectedIcon0, R.id.selectedIcon1, R.id.selectedIcon2, R.id.selectedIcon3};

            for (int i = 0; i < 4; i++) {
                int imageIndex = position * 4 + i;
                ImageView imageView = row.findViewById(imageViews[i]);
                ImageView selectedIcon = row.findViewById(selectedIcons[i]);

                if (imageIndex < imageUrls.length && imageUrls[imageIndex] != null && !imageUrls[imageIndex].isEmpty()) {
                    GlideUrl glideUrl = new GlideUrl(imageUrls[imageIndex], new LazyHeaders.Builder()
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .build());
                    Glide.with(context)
                            .load(glideUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(R.drawable.haha1)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("Glide", "Load failed for " + model);
                                    if (e != null) {
                                        e.logRootCauses("Glide");
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    Log.d("Glide", "Load success for " + model);
                                    return false;
                                }
                            })
                            .into(imageView);

                    imageView.setTag(imageIndex);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isPaired ) {
                                int index = (int) v.getTag();
                                handleImageClick(index);
                            }
                        }
                    });

                    selectedIcon.setVisibility(isSelected[imageIndex] ? View.VISIBLE : View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setAlpha(isSelected[imageIndex] ? 1.0f : 0.1f);
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                    selectedIcon.setVisibility(View.GONE);
                }
            }

            return row;
        }

        public void handleImageClick(int index) {
            // 只有当没有选中图片或只选中了一张图片时，玩家才能选择图片
            if (!isSelected[index] && lastTwoSelections.size() < 2) {
                isSelected[index] = true;
                lastTwoSelections.add(index);
                animateSelection(index);

                // 当选中两张图片后，处理配对逻辑
                if (lastTwoSelections.size() == 2) {
                    handlePairedImages();
                }
            }
        }


        private void animateSelection(int index) {
            ImageView imageView = findImageViewByIndex(index);
            ImageView selectedIcon = findSelectedIconByIndex(index);

            selectedIcon.setVisibility(View.VISIBLE);
            selectedIcon.setAlpha(0f);

            selectedIcon.animate()
                    .alpha(1.0f)
                    .setDuration(200)
                    .start();

            imageView.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .alpha(1.0f)
                    .setDuration(200)
                    .start();
        }

        private void animateDeselection(int index) {
            ImageView imageView = findImageViewByIndex(index);
            ImageView selectedIcon = findSelectedIconByIndex(index);

            selectedIcon.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        selectedIcon.setVisibility(View.GONE);
                    })
                    .start();

            imageView.animate()
                    .alpha(0.1f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
        }

        private void handlePairedImages() {
            if (lastTwoSelections.size() == 2) {
                int firstIndex = lastTwoSelections.get(0);
                int secondIndex = lastTwoSelections.get(1);
                String firstImage = imageUrls[firstIndex];
                String secondImage = imageUrls[secondIndex];

                handler.postDelayed(() -> {
                    if (firstImage.equals(secondImage)) {
                        setPairingSuccess();
                        disablePairedImagesClick(); // Disable click for paired images
                    } else {
                        resetPairing();
                    }
                }, 500);
            }
        }


        private void disablePairedImagesClick() {
            for (int i = 0; i < isSelected.length; i++) {
                if (isSelected[i]) {
                    ImageView imageView = findImageViewByIndex(i);
                    imageView.setOnClickListener(null); // Disable click
                }
            }
        }


        private void setPairingSuccess() {
            isPaired = true;
            handler.post(() -> {
                pairCount++; // 仅在配对成功时增加
                ((GameActivity) context).updatePairCountText();
                // 检查是否所有配对都已完成
                if (pairCount == 6) {
                    stopTimer(); // 停止计时器
                    Toast.makeText(context, "Finish!", Toast.LENGTH_SHORT).show();
                    // 可以在这里添加其他完成游戏的逻辑
                }
            });

            pairedIndexes.addAll(lastTwoSelections);

            for (Integer index : lastTwoSelections) {
                isSelected[index] = true; // 保持配对成功的图片为选中状态
            }

            lastTwoSelections.clear();
            isPaired = false;
        }

        private void stopTimer() {
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }


        private void resetPairing() {
            handler.postDelayed(() -> {
                for (int index : lastTwoSelections) {
                    isSelected[index] = false;
                    animateDeselection(index);
                }
                lastTwoSelections.clear();
            }, 200);
        }


        private ImageView findImageViewByIndex(int index) {
            int position = index % 4;
            View row = getViewByPosition(index / 4);
            int[] imageViews = new int[]{R.id.imageView0, R.id.imageView1, R.id.imageView2, R.id.imageView3};
            return row.findViewById(imageViews[position]);
        }

        private ImageView findSelectedIconByIndex(int index) {
            int position = index % 4;
            View row = getViewByPosition(index / 4);
            int[] selectedIcons = new int[]{R.id.selectedIcon0, R.id.selectedIcon1, R.id.selectedIcon2, R.id.selectedIcon3};
            return row.findViewById(selectedIcons[position]);
        }

        private View getViewByPosition(int position) {
            return ((GameActivity) context).getListView().getChildAt(position);
        }

    }
}