package com.example.secureEye.Utils;


import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;

import com.example.secureEye.R;

public class TypefaceSpan extends MetricAffectingSpan {

    public static final String fontName = "roboto_bold.otf";
    /**
     * An <code>LruCache</code> for previously loaded typefaces.
     */
    private static LruCache<String, Typeface> sTypefaceCache =
            new LruCache<String, Typeface>(12);

    private Typeface mTypeface;
    private static Context context;

    /**
     * Load the {@link Typeface} and apply to a {@link Spannable}.
     */
    public TypefaceSpan(Context context, String typefaceName) {
        mTypeface = sTypefaceCache.get(typefaceName);
        this.context=context;

        if (mTypeface == null) {
            mTypeface = Typeface.create(ResourcesCompat.getFont(context, R.font.roboto_bold), Typeface.BOLD);

            // Cache the loaded Typeface
            sTypefaceCache.put(typefaceName, mTypeface);
        }
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    //Convert and resize our image to 200dp for faster uploading our images to DB


}
