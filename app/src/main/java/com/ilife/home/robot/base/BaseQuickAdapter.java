package com.ilife.home.robot.base;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public abstract class BaseQuickAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {
    protected List<T> data;
    private int layoutId;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemChildLongClickListener mOnItemChildLongClickListener;
    protected SparseIntArray selectPosition;
    protected boolean isMultipleSelect;
    private boolean isMultiType;
    /**
     * layouts indexed with their types
     */
    private SparseIntArray layouts;

    public BaseQuickAdapter(@LayoutRes int layoutId, @NonNull List<T> data) {
        this.layoutId = layoutId;
        this.data = data;
        isMultiType = false;
    }

    public BaseQuickAdapter(@LayoutRes int layoutId, @NonNull List<T> data, SparseIntArray selectPosition) {
        this.layoutId = layoutId;
        this.data = data;
        this.selectPosition = selectPosition;
        isMultiType = false;
    }

    public BaseQuickAdapter(@NonNull List<T> data) {
        this.data = data;
        isMultiType = true;
    }

    public void setNewData(@NonNull List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public K onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        K holder;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(isMultiType ? getLayoutId(viewType) : layoutId, parent, false);
        holder = (K) new BaseViewHolder(view);
        bindItemViewClickListener(holder);
        holder.setAdapter(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull K holder, int position) {
        convert(holder, position);
    }

    public static final int TYPE_NOT_FOUND = -404;

    @Override
    public int getItemCount() {
        return data.size();
    }

    protected abstract void convert(@NonNull K holder, int position);

    private int getLayoutId(int viewType) {
        return layouts.get(viewType, TYPE_NOT_FOUND);
    }

    protected void addItemType(int type, @LayoutRes int layoutResId) {
        if (layouts == null) {
            layouts = new SparseIntArray();
        }
        layouts.put(type, layoutResId);
    }

    /**
     * bind onclick and onLongClick event for item view
     *
     * @param baseViewHolder
     */
    private void bindItemViewClickListener(final BaseViewHolder baseViewHolder) {
        if (baseViewHolder == null) {
            return;
        }
        final View view = baseViewHolder.itemView;
        if (getOnItemClickListener() != null) {
            view.setOnClickListener(v -> {
                int position = baseViewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                setOnItemClick(v, position);
            });
        }
        if (getOnItemLongClickListener() != null) {
            view.setOnLongClickListener(v -> {
                int position = baseViewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return false;
                }
                return setOnItemLongClick(v, position);
            });
        }
    }

    /**
     * override this method if you want to override click event logic
     *
     * @param v
     * @param position
     */
    public void setOnItemClick(View v, int position) {
        getOnItemClickListener().onItemClick(BaseQuickAdapter.this, v, position);
        if (selectPosition != null) {
            if (isMultipleSelect) {
                selectPosition.clear();
                selectPosition.put(position, position);
            } else if (selectPosition.indexOfKey(position) >= 0) {
                selectPosition.delete(position);
            } else {
                selectPosition.put(position, position);
            }
        }
    }


    public void cleanSelectPosition(){
        if (selectPosition!=null){
            selectPosition.clear();
        }
    }
    /**
     * override this method if you want to override longClick event logic
     *
     * @param v
     * @param position
     * @return
     */
    public boolean setOnItemLongClick(View v, int position) {
        return getOnItemLongClickListener().onItemLongClick(BaseQuickAdapter.this, v, position);
    }


    /**
     * 点击，长按事件
     */

    /**
     * Interface definition for a callback to be invoked when an itemchild in this
     * view has been clicked
     */
    public interface OnItemChildClickListener {
        /**
         * callback method to be invoked when an itemchild in this view has been click
         *
         * @param adapter
         * @param view     The view whihin the ItemView that was clicked
         * @param position The position of the view int the adapter
         */
        void onItemChildClick(BaseQuickAdapter adapter, View view, int position);
    }


    /**
     * Interface definition for a callback to be invoked when an childView in this
     * view has been clicked and held.
     */
    public interface OnItemChildLongClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param adapter  this BaseQuickAdapter adapter
         * @param view     The childView whihin the itemView that was clicked and held.
         * @param position The position of the view int the adapter
         * @return true if the callback consumed the long click ,false otherwise
         */
        boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position);
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked and held.
     */
    public interface OnItemLongClickListener {
        /**
         * callback method to be invoked when an item in this view has been
         * click and held
         *
         * @param adapter  the adpater
         * @param view     The view whihin the RecyclerView that was clicked and held.
         * @param position The position of the view int the adapter
         * @return true if the callback consumed the long click ,false otherwise
         */
        boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position);
    }


    /**
     * Interface definition for a callback to be invoked when an item in this
     * RecyclerView itemView has been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this RecyclerView has
         * been clicked.
         *
         * @param adapter  the adpater
         * @param view     The itemView within the RecyclerView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         */
        void onItemClick(BaseQuickAdapter adapter, View view, int position);
    }

    /**
     * Register a callback to be invoked when an item in this RecyclerView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an itemchild in View has
     * been  clicked
     *
     * @param listener The callback that will run
     */
    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        mOnItemChildClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an item in this RecyclerView has
     * been long clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an itemchild  in this View has
     * been long clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener listener) {
        mOnItemChildLongClickListener = listener;
    }


    /**
     * @return The callback to be invoked with an item in this RecyclerView has
     * been long clicked and held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * @return The callback to be invoked with an item in this RecyclerView has
     * been clicked and held, or null id no callback as been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * @return The callback to be invoked with an itemchild in this RecyclerView has
     * been clicked, or null id no callback has been set.
     */
    @Nullable
    public final OnItemChildClickListener getOnItemChildClickListener() {
        return mOnItemChildClickListener;
    }

    /**
     * @return The callback to be invoked with an itemChild in this RecyclerView has
     * been long clicked, or null id no callback has been set.
     */
    @Nullable
    public final OnItemChildLongClickListener getOnItemChildLongClickListener() {
        return mOnItemChildLongClickListener;
    }


}
