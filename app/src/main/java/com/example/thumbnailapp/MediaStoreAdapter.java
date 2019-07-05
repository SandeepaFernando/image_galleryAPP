package com.example.thumbnailapp;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MediaStoreAdapter extends RecyclerView.Adapter<MediaStoreAdapter.ViewHolder> {

    private Cursor mMediaStoreCursor;
    private final Activity mActivity;
    private OnClickListener onClickListener = null;
    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;


    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public interface OnClickListener{
        void onItemClick(View view, int pos);
        void OnItemLongClick(View view, Uri uri, int position);

    }

    public MediaStoreAdapter(Activity activity) {
        this.mActivity = activity;
        //this.mOnClickThumbListener = (OnClickThumbListener) activity;
        selected_items = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(mActivity)
                .load(getUriFromMediaStore(position))
                .centerCrop()
                .override(96, 96)
                .into(holder.getImageView());


        holder.itemRelative.setActivated(selected_items.get(position, false));

        holder.itemRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener == null) return;
                onClickListener.onItemClick(v, position);
            }
        });

        holder.itemRelative.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onClickListener == null) return false;
                onClickListener.OnItemLongClick(v,getUriFromMediaStore(position),position);
                return true;
            }
        });


        toggleCheckedIcon(holder, position);

    }

    private void toggleCheckedIcon(ViewHolder holder, int position) {
        if (selected_items.get(position, false)) {
            holder.imageView_done.setVisibility(View.VISIBLE);

            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            holder.mImageView.setVisibility(View.VISIBLE);
            holder.imageView_done.setVisibility(View.INVISIBLE);
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }

    @Override
    public int getItemCount() {
        return (mMediaStoreCursor == null) ? 0 : mMediaStoreCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mImageView;
        ImageView imageView_done;
        public View itemRelative;

        public ViewHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.media_view);
            imageView_done = itemView.findViewById(R.id.image_view_done);
            //mImageView.setOnClickListener(this);

            itemRelative = itemView.findViewById(R.id.item_view);
        }

        public ImageView getImageView() {
            return mImageView;
        }

        @Override
        public void onClick(View v) {
        }

    }

    private Cursor swapCursor(Cursor cursor) {
        if (mMediaStoreCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mMediaStoreCursor;
        this.mMediaStoreCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private Uri getUriFromMediaStore(int position) {
        int dataIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

        mMediaStoreCursor.moveToPosition(position);

        String dataString = mMediaStoreCursor.getString(dataIndex);
        Uri mediaUri = Uri.parse("file://" + dataString);
        return mediaUri;
    }

    public void toggleSelection(int pos) {
        current_selected_idx = pos;
        if (selected_items.get(pos, false)) {
            selected_items.delete(pos);
        } else {
            selected_items.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selected_items.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            items.add(selected_items.keyAt(i));
        }
        return items;
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }


}
