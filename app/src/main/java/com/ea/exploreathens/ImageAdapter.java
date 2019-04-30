package com.ea.exploreathens;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ea.exploreathens.code.CodeUtility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {

    // Adapter, um die 3 Bilder in SiteActivity darzustellen
    private Context mContext;
    private ArrayList<String> mImages;

    public ImageAdapter(Context context, ArrayList<String> mImages) {
        mContext = context;
        this.mImages = mImages;
        Log.d("info", "Created ImageAdapter for " + mImages);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d("image", "trying to display " + mImages.get(position));
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Picasso.get().load(CodeUtility.baseURL + "/image/" + mImages.get(position)).into(imageView);

        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }

}