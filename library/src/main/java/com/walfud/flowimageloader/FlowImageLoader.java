package com.walfud.flowimageloader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.walfud.flowimageloader.cache.CacheManager;
import com.walfud.flowimageloader.dna.Dna;
import com.walfud.flowimageloader.dna.action.CacheAction;
import com.walfud.flowimageloader.dna.action.IntoAction;
import com.walfud.flowimageloader.dna.action.InvalidateAction;
import com.walfud.flowimageloader.dna.gene.CenterInsideGene;
import com.walfud.flowimageloader.dna.gene.CircleGene;
import com.walfud.flowimageloader.dna.gene.CropGene;
import com.walfud.flowimageloader.dna.gene.LoadGene;
import com.walfud.flowimageloader.dna.gene.ResizeGene;
import com.walfud.flowimageloader.dna.gene.RoundGene;
import com.walfud.walle.WallE;

import okhttp3.OkHttpClient;

/**
 * <pre>{@code
 * FlowImageLoader.with(getApplicationContext())
 *       .resize(1000, 500)
 *       .load(Uri.parse("https://raw.githubusercontent.com/walfud/lib-flowimageloader/master/doc/workflow.png")).resize(200, 100)
 *       .cache()
 *       .into(mIv)
 *       .pls();
 * }</pre>
 * Created by walfud on 2015/11/11.
 */
public class FlowImageLoader {

    public static final String TAG = "FlowImageLoader";

    private static OkHttpClient sOkHttpClientInstance;
    private Dna mDna;
    /**
     * Whether `load` has been called.
     */
    private boolean mHasUri = false;

    private FlowImageLoader(Context context) {
        mDna = new Dna();
        mHasUri = false;
    }

    // Function
    public static FlowImageLoader with(Context context) {
        Context appContext = context.getApplicationContext();
        if (sOkHttpClientInstance == null) {
            // Initialization
            WallE.initialize(appContext);
            CacheManager.getInstance().init(appContext);
        }

        return new FlowImageLoader(appContext);
    }

    public FlowImageLoader load(String url) {
        return load(Uri.parse(url));
    }
    public FlowImageLoader load(Uri uri) {
        mDna.digest(new LoadGene(uri));
        mHasUri = true;

        return this;
    }

    /**
     * If there is uri in flow, `resize` means resize the local bitmap;
     * Otherwise, means send a resize request to server. But We can'first promise the server will
     * do correct resize action.
     *
     * @param width
     * @param height
     * @return
     * @see #mHasUri
     */
    public FlowImageLoader resize(int width, int height) {
        if (!mHasUri) {
            // 'resize' request to server
            // TODO:
            throw new RuntimeException("not impl");
        } else {
            // 'resize' action in local bitmap
            mDna.digest(new ResizeGene(width, height));
        }

        return this;
    }

    public FlowImageLoader circle() {
        mDna.digest(new CircleGene());

        return this;
    }

    public FlowImageLoader round(double radiusX, double radiusY) {
        mDna.digest(new RoundGene(radiusX, radiusY));

        return this;
    }

    public FlowImageLoader crop(int width, int height) {
        mDna.digest(new CropGene(width, height));

        return this;
    }

    public FlowImageLoader centerInside(int width, int height) {
        mDna.digest(new CenterInsideGene(width, height));

        return this;
    }

    public FlowImageLoader cache() {
        return cache(true, true);
    }

    public FlowImageLoader cache(boolean memory, boolean disk) {
        mDna.absorb(new CacheAction());

        return this;
    }

    public FlowImageLoader invalidate() {
        return invalidate(true, true);
    }

    public FlowImageLoader invalidate(boolean memory, boolean disk) {
        mDna.absorb(new InvalidateAction());

        return this;
    }

    public FlowImageLoader into(ImageView imageView) {
        return into(imageView, IntoAction.INVALID_LOADING_ID, IntoAction.INVALID_FAIL_ID);
    }

    public FlowImageLoader into(ImageView imageView, @DrawableRes int loadingId, @DrawableRes int failId) {
        mDna.absorb(new IntoAction(imageView));
        mDna.setListener(new Dna.Listener() {
            @Override
            public void onStart() {
                if (loadingId != IntoAction.INVALID_LOADING_ID) {
                    imageView.setImageResource(loadingId);
                }
            }

            @Override
            public void onFinish(boolean suc) {
                if (!suc && failId != IntoAction.INVALID_FAIL_ID) {
                    imageView.setImageResource(failId);
                }
            }
        });

        return this;
    }

    /**
     * Run the flow
     */
    public void pls() {
        mDna.evolve();
    }

    /**
     * Cancel the flow
     */
    public void cancel() {
        mDna.eliminate();
    }

    // Other
    public static OkHttpClient getOkHttpClientInstance() {
        if (sOkHttpClientInstance == null) {
            sOkHttpClientInstance = new OkHttpClient.Builder()
                    .build();
        }

        return sOkHttpClientInstance;
    }
}
