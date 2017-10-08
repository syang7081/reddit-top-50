package com.myapplication.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.karen.myapplication.R;
import com.myapplication.datasource.DataSource;
import com.myapplication.datasource.GenericNetworkErrorHandler;
import com.myapplication.model.LinkInfo;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by syang on 10/6/2017.
 */

public class LinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = LinksAdapter.class.getSimpleName();
    private static final long MIN_SPAN = 60 * 1000; //ms
    private static final long HOUR_SPAN = MIN_SPAN * 60;
    private static final long DAY_SPAN = HOUR_SPAN * 24;

    private List<LinkInfo> links;
    private boolean isDetached = false;

    public LinksAdapter(List<LinkInfo> links) {
        this.links = links;
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        isDetached = true;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View view = recyclerView.getChildAt(i);
            recycleBitmap((LinkViewHolder)recyclerView.getChildViewHolder(view));
        }
    }

    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        recycleBitmap((LinkViewHolder) holder);
    }

    public void onViewDetachedFromWindow(RecyclerView.ViewHolder viewHolder) {
        super.onViewDetachedFromWindow(viewHolder);
        Log.d(TAG, "onViewDetachedFromWindow");
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (links == null || (links.size() - 1) < position) return;
        LinkInfo linkInfo = links.get(position);
        LinkViewHolder linkViewHolder = (LinkViewHolder) holder;

        // Recycle bitmap
        recycleBitmap(linkViewHolder);

        if (!TextUtils.isEmpty(linkInfo.getThumbNailUrl())) {
            linkViewHolder.thumbNail.setTag(linkInfo);
            loadImage(linkViewHolder.thumbNail, linkInfo.getThumbNailUrl());
        }

        linkViewHolder.title.setText(linkInfo.getTitle());
        linkViewHolder.author.setText("By " + linkInfo.getAuthorName());
        linkViewHolder.commentNum.setText("Comments: " + linkInfo.getNumberOfComments());
        linkViewHolder.timeElapsed.setText(getTimeElapsed(linkInfo.getCreatedUtc()));
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinkViewHolder viewHolder = new LinkViewHolder(LayoutInflater.from(parent.getContext()), parent);
        return viewHolder;
    }

    public int getItemCount() {
        if (links == null) return 0;
        return links.size();
    }

    private void recycleBitmap(LinkViewHolder linkViewHolder) {
        if (linkViewHolder.thumbNail.getTag() instanceof LinkInfo) {
            LinkInfo link = (LinkInfo) linkViewHolder.thumbNail.getTag();
            if (link.getThumbBitmap() != null) {
                linkViewHolder.thumbNail.setImageResource(R.mipmap.ic_launcher);
                link.getThumbBitmap().recycle();
            }
            linkViewHolder.thumbNail.setTag("");
        }
    }

    private String getTimeElapsed(long createdUtc) {
        String elapsedTime = "Seconds ago";
        long diff = System.currentTimeMillis() - createdUtc * 1000;
        if (diff / DAY_SPAN > 0) {
            elapsedTime = diff / DAY_SPAN + " days ago";
        }
        else if (diff / HOUR_SPAN > 0) {
            elapsedTime = diff / HOUR_SPAN + " hours ago";
        }
        else if (diff / MIN_SPAN > 0) {
            elapsedTime = diff / MIN_SPAN + " minutes ago";
        }
        return elapsedTime;
    }

    private void loadImage(final ImageView imageView, final String urlStr) {
        DataSource.getInstance()
                .loadImage(urlStr)
                .subscribe(new MyObserver(imageView));
    }

    class MyObserver implements Observer<Bitmap> {
        private Disposable disposable;
        private WeakReference<ImageView> imageViewWeakReference;
        //private ImageView imageView;

        public MyObserver(ImageView imageView) {
            //this.imageView = imageView;
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }

        public void onSubscribe(Disposable disposable) {
            this.disposable = disposable;
        }

        public void onComplete() {
            dispose();
        }

        private void dispose() {
            if (disposable != null) {
                disposable.dispose();
                disposable = null;
            }
        }

        public void onNext(Bitmap bitmap) {
            if (!isDetached && imageViewWeakReference.get() != null) {
                ImageView imageView = imageViewWeakReference.get();
                imageView.setImageBitmap(bitmap);
                if (imageView.getTag() instanceof LinkInfo) {
                    LinkInfo link = (LinkInfo) imageView.getTag();
                    link.setThumbBitmap(bitmap);
                }
            }
            dispose();
        }

        public void onError(Throwable throwable) {
            new GenericNetworkErrorHandler<>().accept(throwable);
            dispose();
        }
    }

    public static class LinkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView thumbNail;
        public TextView title;
        public TextView author;
        public TextView commentNum;
        public TextView timeElapsed;
        public LinkViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.single_link_layout, parent, false));
            thumbNail = (ImageView) itemView.findViewById(R.id.thumbnail);
            thumbNail.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            author = (TextView) itemView.findViewById(R.id.author);
            commentNum = (TextView) itemView.findViewById(R.id.comment_num);
            timeElapsed = (TextView) itemView.findViewById(R.id.time_elapsed);
        }

        public void onClick(View view) {
            if (view.getId() == R.id.thumbnail && view.getTag() instanceof LinkInfo) {
                // Open the web page
                String url = ((LinkInfo) view.getTag()).getUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    view.getContext().startActivity(i);
                }
            }
        }
    }
}
