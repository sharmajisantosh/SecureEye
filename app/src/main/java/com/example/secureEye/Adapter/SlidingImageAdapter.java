package com.example.secureEye.Adapter;

import android.content.Context;
import android.media.Image;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.secureEye.R;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.List;

public class SlidingImageAdapter extends PagerAdapter {

    private static final String TAG = "SlidingImageAdapter";
    private List<String> urls;
    private LayoutInflater inflater;
    private Context context;

    public SlidingImageAdapter(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return urls.size();
    }
    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.sliding_image_layout, view, false);

        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.messageImageView);

        Picasso.get().load(urls.get(position)).into(imageView);
        if (urls.size()==0){
            Picasso.get().load(R.drawable.no_image_found).into(imageView);
        }

        view.addView(imageLayout, 0);

        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: "+position);

                new StfalconImageViewer.Builder<>(context, urls, new ImageLoader<String>() {
                    @Override
                    public void loadImage(ImageView imageView, String imageUrl) {
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                }).show();
            }
        });
        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}