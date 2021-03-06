/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.widget;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Bridge from Presenter to RecyclerView.Adapter. Public to allow use by third
 * party presenters.
 */
public class ItemBridgeAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ItemBridgeAdapter";
    private static final boolean DEBUG = false;

    /**
     * Interface for listening to view holder operations.
     */
    public static class AdapterListener {
        public void onAddPresenter(Presenter presenter, int type) {
        }
        public void onCreate(ViewHolder viewHolder) {
        }
        public void onBind(ViewHolder viewHolder) {
        }
        public void onUnbind(ViewHolder viewHolder) {
        }
        public void onAttachedToWindow(ViewHolder viewHolder) {
        }
        public void onDetachedFromWindow(ViewHolder viewHolder) {
        }
    }

    /**
     * Interface for wrapping a view created by presenter into another view.
     * The wrapper must be immediate parent of the wrapped view.
     */
    public static abstract class Wrapper {
        public abstract View createWrapper(View root);
        public abstract void wrap(View wrapper, View wrapped);
    }

    private ObjectAdapter mAdapter;
    private Wrapper mWrapper;
    private PresenterSelector mPresenterSelector;
    private FocusHighlightHandler mFocusHighlight;
    private AdapterListener mAdapterListener;
    private ArrayList<Presenter> mPresenters = new ArrayList<Presenter>();

    final class OnFocusChangeListener implements View.OnFocusChangeListener {
        View.OnFocusChangeListener mChainedListener;

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (DEBUG) Log.v(TAG, "onFocusChange " + hasFocus + " " + view
                    + " mFocusHighlight" + mFocusHighlight);
            if (mWrapper != null) {
                view = (View) view.getParent();
            }
            if (mFocusHighlight != null) {
                mFocusHighlight.onItemFocused(view, hasFocus);
            }
            if (mChainedListener != null) {
                mChainedListener.onFocusChange(view, hasFocus);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final Presenter mPresenter;
        final Presenter.ViewHolder mHolder;
        final OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener();
        Object mItem;
        Object mExtraObject;

        /**
         * Get {@link Presenter}.
         */
        public final Presenter getPresenter() {
            return mPresenter;
        }

        /**
         * Get {@link Presenter.ViewHolder}.
         */
        public final Presenter.ViewHolder getViewHolder() {
            return mHolder;
        }

        /**
         * Get currently bound object.
         */
        public final Object getItem() {
            return mItem;
        }

        /**
         * Get extra object associated with the view.  Developer can attach
         * any customized UI object in addition to {@link Presenter.ViewHolder}.
         * A typical use case is attaching an animator object.
         */
        public final Object getExtraObject() {
            return mExtraObject;
        }

        /**
         * Set extra object associated with the view.  Developer can attach
         * any customized UI object in addition to {@link Presenter.ViewHolder}.
         * A typical use case is attaching an animator object.
         */
        public void setExtraObject(Object object) {
            mExtraObject = object;
        }

        ViewHolder(Presenter presenter, View view, Presenter.ViewHolder holder) {
            super(view);
            mPresenter = presenter;
            mHolder = holder;
        }
    }

    private ObjectAdapter.DataObserver mDataObserver = new ObjectAdapter.DataObserver() {
        @Override
        public void onChanged() {
            ItemBridgeAdapter.this.notifyDataSetChanged();
        }
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            ItemBridgeAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
        }
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            ItemBridgeAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
        }
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            ItemBridgeAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
        }
    };

    public ItemBridgeAdapter(ObjectAdapter adapter, PresenterSelector presenterSelector) {
        setAdapter(adapter);
        mPresenterSelector = presenterSelector;
    }

    public ItemBridgeAdapter(ObjectAdapter adapter) {
        this(adapter, null);
    }

    public ItemBridgeAdapter() {
    }

    public void setAdapter(ObjectAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterObserver(mDataObserver);
        }
        mAdapter = adapter;
        if (mAdapter == null) {
            return;
        }

        mAdapter.registerObserver(mDataObserver);
        if (hasStableIds() != mAdapter.hasStableIds()) {
            setHasStableIds(mAdapter.hasStableIds());
        }
    }

    public void setWrapper(Wrapper wrapper) {
        mWrapper = wrapper;
    }

    public Wrapper getWrapper() {
        return mWrapper;
    }

    void setFocusHighlight(FocusHighlightHandler listener) {
        mFocusHighlight = listener;
        if (DEBUG) Log.v(TAG, "setFocusHighlight " + mFocusHighlight);
    }

    public void clear() {
        setAdapter(null);
    }

    public void setPresenterMapper(ArrayList<Presenter> presenters) {
        mPresenters = presenters;
    }

    public ArrayList<Presenter> getPresenterMapper() {
        return mPresenters;
    }

    @Override
    public int getItemCount() {
        return mAdapter.size();
    }

    @Override
    public int getItemViewType(int position) {
        PresenterSelector presenterSelector = mPresenterSelector != null ?
                mPresenterSelector : mAdapter.getPresenterSelector();
        Object item = mAdapter.get(position);
        Presenter presenter = presenterSelector.getPresenter(item);
        int type = mPresenters.indexOf(presenter);
        if (type < 0) {
            mPresenters.add(presenter);
            type = mPresenters.indexOf(presenter);
            if (DEBUG) Log.v(TAG, "getItemViewType added presenter " + presenter + " type " + type);
            if (mAdapterListener != null) {
                mAdapterListener.onAddPresenter(presenter, type);
            }
        }
        return type;
    }

    /**
     * {@link View.OnFocusChangeListener} that assigned in
     * {@link Presenter#onCreateViewHolder(ViewGroup)} may be chained, user should never change
     * {@link View.OnFocusChangeListener} after that.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (DEBUG) Log.v(TAG, "onCreateViewHolder viewType " + viewType);
        Presenter presenter = mPresenters.get(viewType);
        Presenter.ViewHolder presenterVh;
        View view;
        if (mWrapper != null) {
            view = mWrapper.createWrapper(parent);
            presenterVh = presenter.onCreateViewHolder(parent);
            mWrapper.wrap(view, presenterVh.view);
        } else {
            presenterVh = presenter.onCreateViewHolder(parent);
            view = presenterVh.view;
        }
        ViewHolder viewHolder = new ViewHolder(presenter, view, presenterVh);
        if (mAdapterListener != null) {
            mAdapterListener.onCreate(viewHolder);
        }
        View presenterView = viewHolder.mHolder.view;
        if (presenterView != null) {
            viewHolder.mFocusChangeListener.mChainedListener = presenterView.getOnFocusChangeListener();
            presenterView.setOnFocusChangeListener(viewHolder.mFocusChangeListener);
        }
        if (mFocusHighlight != null) {
            mFocusHighlight.onInitializeView(view);
        }
        return viewHolder;
    }

    public void setAdapterListener(AdapterListener listener) {
        mAdapterListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (DEBUG) Log.v(TAG, "onBindViewHolder position " + position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mItem = mAdapter.get(position);

        viewHolder.mPresenter.onBindViewHolder(viewHolder.mHolder, viewHolder.mItem);

        if (mAdapterListener != null) {
            mAdapterListener.onBind(viewHolder);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mPresenter.onUnbindViewHolder(viewHolder.mHolder);

        viewHolder.mItem = null;

        if (mAdapterListener != null) {
            mAdapterListener.onUnbind(viewHolder);
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        ViewHolder viewHolder = (ViewHolder) holder;
        if (mAdapterListener != null) {
            mAdapterListener.onAttachedToWindow(viewHolder);
        }
        viewHolder.mPresenter.onViewAttachedToWindow(viewHolder.mHolder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mPresenter.onViewDetachedFromWindow(viewHolder.mHolder);
        if (mAdapterListener != null) {
            mAdapterListener.onDetachedFromWindow(viewHolder);
        }
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getId(position);
    }

}
