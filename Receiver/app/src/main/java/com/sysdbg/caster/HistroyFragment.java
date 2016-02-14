package com.sysdbg.caster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.sysdbg.caster.history.HistoryItem;
import com.sysdbg.caster.history.HistoryManager;
import com.sysdbg.caster.utils.ScreenUtils;
import com.sysdbg.caster.utils.StringUtils;

public class HistroyFragment extends BrowseFragment {
    private Callback callback;

    public HistroyFragment() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupListener();
        setupUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRows();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void setupUI() {
        setTitle("Caster");
        setBrandColor(getResources().getColor(R.color.brand_color));
        setHeadersState(HEADERS_ENABLED);
    }

    private void setupRows() {
        HistoryManager manager = HistoryManager.getInstance(getActivity());

        ArrayObjectAdapter rowAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter(getActivity());

        for(String domain : manager.getDomains()) {
            ArrayObjectAdapter itemAdapter = new ArrayObjectAdapter(cardPresenter);
            HeaderItem header = new HeaderItem(domain);

            for(HistoryItem item : manager.getItems(domain)) {
                itemAdapter.add(item);
            }

            rowAdapter.add(new ListRow(header, itemAdapter));
        }

        setAdapter(rowAdapter);
    }

    private void setupListener() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                CardViewHolder cardViewHolder = (CardViewHolder)itemViewHolder;
                HistoryItem historyItem = cardViewHolder.getHistoryItem();

                fireOnPlay(historyItem);
            }
        });
    }

    public interface Callback {
        void onPlay(HistoryItem item);
    }

    private void fireOnPlay(HistoryItem item) {
        if (callback != null) {
            callback.onPlay(item);
        }
    }

    private class CardViewHolder extends Presenter.ViewHolder {
        private HistoryItem historyItem;

        public CardViewHolder(View view) {
            super(view);
        }

        public HistoryItem getHistoryItem() {
            return historyItem;
        }

        public void setHistoryItem(HistoryItem historyItem) {
            this.historyItem = historyItem;
        }
    }

    private class CardPresenter extends Presenter {
        private static final int CARD_WIDTH = 500;
        private static final int CARD_HEIGHT = 300;

        private Context context;
        private Drawable loadingImg;

        public CardPresenter(Context context) {
            this.context = context;
            loadingImg = context.getResources().getDrawable(R.drawable.loading);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            ImageCardView view = new ImageCardView(parent.getContext());
            view.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);

            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            final HistoryItem historyItem = (HistoryItem)item;
            final CardViewHolder cardViewHolder = (CardViewHolder)viewHolder;


            ImageCardView imgCardView = (ImageCardView)viewHolder.view;
            final PicassoImageCardViewTarget target = new PicassoImageCardViewTarget(imgCardView);

            cardViewHolder.setHistoryItem(historyItem);
            String title = historyItem.getTitle();
            if (StringUtils.isEmpty(title)) {
                title = historyItem.getDescription();
            }
            if (StringUtils.isEmpty(title)) {
                title = "";
            }

            imgCardView.setTitleText(title);
            imgCardView.setMainImage(loadingImg);

            if (!StringUtils.isEmpty(historyItem.getImgUrl())) {
                Picasso.with(context)
                        .load(historyItem.getImgUrl())
                        .resize(ScreenUtils.convertDpToPixel(context, CARD_WIDTH),
                                ScreenUtils.convertDpToPixel(context, CARD_HEIGHT))
                        .error(loadingImg)
                        .into(target);
            }
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }
    }

    public static class PicassoImageCardViewTarget implements Target {
        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView imageCardView) {
            mImageCardView = imageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(mImageCardView.getContext().getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mImageCardView.setMainImage(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }
}
