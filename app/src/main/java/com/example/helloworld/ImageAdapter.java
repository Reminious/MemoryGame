package com.example.helloworld;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class ImageAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] imageUrls;
    private boolean[] isSelected;
    private boolean isDownloading;

    public ImageAdapter(Context context, String[] imageUrls) {
        super(context, R.layout.row, imageUrls);
        this.context = context;
        this.imageUrls = imageUrls;
        this.isSelected = new boolean[imageUrls.length];
    }

    public void setDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }//get the status of downloading

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(R.layout.row, parent, false);
        }//inflate the layout when it is null

        int[] imageViews = new int[]{R.id.imageView0, R.id.imageView1, R.id.imageView2, R.id.imageView3};//get the id of imageViews
        int[] selectedIcons = new int[]{R.id.selectedIcon0, R.id.selectedIcon1, R.id.selectedIcon2, R.id.selectedIcon3};//get the id of selectedIcons

        for (int i = 0; i < 4; i++) {
            int imageIndex = position * 4 + i;//get the index of image, where position is the index of row and i is the index of column
            ImageView imageView = row.findViewById(imageViews[i]);
            ImageView selectedIcon = row.findViewById(selectedIcons[i]);
            if (imageIndex < imageUrls.length && imageUrls[imageIndex] != null && !imageUrls[imageIndex].isEmpty()) {//if the imageUrl is not null and not empty
                GlideUrl glideUrl = new GlideUrl(imageUrls[imageIndex], new LazyHeaders.Builder()//LazyHeaders is used to add headers to Glide requests
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .build());//add the header to the request
                Glide.with(context)
                        .load(glideUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)//do not cache the image
                        .skipMemoryCache(true)//do not load the image from cache
                        .error(R.drawable.haha1)//if the image is not loaded, load the default image haha1
                        .listener(new RequestListener<Drawable>() {//set the listener to the request, use Tag:Glide to check the status of loading in Logcat
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


                imageView.setTag(imageIndex);//set the tag of imageView to the index of image, which is used to get the index of image when the imageView is clicked
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isDownloading) {//if the downloading is not processing, the imageView can be clicked
                            int index = (int) v.getTag();//get the index of image
                            if (isSelected[index] || getSelectedCount() < 6) {//if the image is selected or the number of selected images is less than 6
                                isSelected[index] = !isSelected[index];//change the status of the image when clicked, if it is selected, set it to unselected, if it is unselected, set it to selected
                                if (isSelected[index]) {
                                    selectedIcon.setVisibility(View.VISIBLE);//if the image is selected, set the selectedIcon to visible
                                    selectedIcon.setAlpha(0f);
                                }
                                selectedIcon.animate()//add animation to the selectedIcon
                                        .alpha(isSelected[index] ? 1.0f : 0f)//if the image is selected, set the alpha to 1.0f, if it is unselected, set the alpha to 0f
                                        .setDuration(200)//set the duration of animation to 200ms
                                        .withEndAction(new Runnable() {
                                            //set the end action of animation if the image is unselected, which is used to set the selectedIcon to gone when the animation is finished
                                            @Override
                                            public void run() {
                                                if (!isSelected[index]) {
                                                    selectedIcon.setVisibility(View.GONE);
                                                }
                                            }
                                        })
                                        .start();
                                v.animate()
                                        .scaleX(0.95f)//set the scale of imageView to 0.95f when clicked
                                        .scaleY(0.95f)
                                        .alpha(isSelected[index] ? 0.2f : 1.0f)//if the image is selected, set the alpha to 0.2f
                                        .setDuration(200)
                                        .withEndAction(new Runnable() {
                                            //set the end action of animation if the image is unselected, which is used to set the scale of imageView to 1.0f when the animation is finished
                                            @Override
                                            public void run() {
                                                v.animate()
                                                        .scaleX(1.0f)
                                                        .scaleY(1.0f)
                                                        .setDuration(200)
                                                        .start();
                                            }
                                        })
                                        .start();
                                int selectedCount = getSelectedCount();
                                Toast.makeText(context, selectedCount + "/6 images selected", Toast.LENGTH_SHORT).show();
                                ((MainActivity) context).updateGameButton(selectedCount == 6);//enable the download button if 6 images are selected
                            }
                        }
                    }
                });



                selectedIcon.setVisibility(View.GONE);//set the visibility of selectedIcon to gone
                imageView.setVisibility(View.VISIBLE);//set the visibility of imageView to visible
                imageView.setAlpha(1.0f);
            } else {

                imageView.setVisibility(View.INVISIBLE);//set the visibility of imageView to invisible if the imageUrl is null or empty
                selectedIcon.setVisibility(View.GONE);//set the visibility of selectedIcon to gone too
            }
        }
        return row;
    }

    @Override
    public int getCount() {//get the number of rows in the GridView
        return (int) Math.ceil(imageUrls.length / 4.0);
    }

    public String[] getSelectedImages() {
        List<String> selectedImages = new ArrayList<>();
        for (int i = 0; i < isSelected.length; i++) {
            if (isSelected[i]) {
                selectedImages.add(imageUrls[i]);
            }
        }
        return selectedImages.toArray(new String[0]);
    }

    public void clearSelections() {//clear all the selections after downloading
        Arrays.fill(isSelected, false);
        notifyDataSetChanged();
    }

    private int getSelectedCount() {
        int count = 0;
        for (boolean selected : isSelected) {
            if (selected) {
                count++;
            }
        }
        return count;
    }


    public String[] getAllImages() {
        return imageUrls;
    }


}
