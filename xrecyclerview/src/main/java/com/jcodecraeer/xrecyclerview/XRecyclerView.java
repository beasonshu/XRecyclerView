package com.jcodecraeer.xrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class XRecyclerView extends RecyclerView {

    private boolean isLoadingData = false;
    private boolean isnomore = false;
    private int mRefreshProgressStyle = ProgressStyle.SysProgress;
    private int mLoadingMoreProgressStyle = ProgressStyle.SysProgress;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private ArrayList<View> mFootViews = new ArrayList<>();
    private Adapter mAdapter;
    private WrapAdapter mWrapAdapter;
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;
    private LoadingListener mLoadingListener;
    private ArrowRefreshHeader mRefreshHeader;
    private boolean pullRefreshEnabled = true;
    private boolean loadingMoreEnabled = true;
    private static final int TYPE_REFRESH_HEADER = -5;
    private static final int TYPE_HEADER = -4;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = -3;

    public static final int LIST = 0;
    public static final int GRID = 1;
    public static final int STAGGERED_GRID = 2;
    private int mItemSelectorId;
    private int mDividerId;
    private int mDividerHeight;
    private int mStyle;
    private int mNumColumns;
    private int mOrientation;
    private int mVerticalSpacing;
    private int mHorizontalSpacing;

    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XRecyclerView);
            mItemSelectorId = a.getResourceId(R.styleable.XRecyclerView_android_listSelector, 0);
            mDividerId = a.getResourceId(R.styleable.XRecyclerView_android_divider, 0);
            mDividerHeight = a.getDimensionPixelSize(R.styleable.XRecyclerView_android_dividerHeight, 1);
            mStyle = a.getInt(R.styleable.XRecyclerView_XRecyclerStyle, LIST);
            mNumColumns = a.getInt(R.styleable.XRecyclerView_android_numColumns, 1);
            mOrientation = a.getInt(R.styleable.XRecyclerView_android_orientation, RecyclerView.VERTICAL);
            mVerticalSpacing = a.getDimensionPixelSize(R.styleable.XRecyclerView_android_verticalSpacing, 0);
            mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.XRecyclerView_android_horizontalSpacing, 0);

            a.recycle();

        }

        switch (mStyle) {
            case LIST:
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(mOrientation);
                setLayoutManager(linearLayoutManager);
                if (mOrientation == HORIZONTAL) {
                    addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL_LIST, mDividerId, mDividerHeight));
                } else {
                    addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST, mDividerId, mDividerHeight));
                }
                break;
            case GRID:
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, mNumColumns);
                gridLayoutManager.setOrientation(mOrientation);
                setLayoutManager(gridLayoutManager);
                break;
            case STAGGERED_GRID:
                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(mNumColumns, mOrientation);
                setLayoutManager(staggeredGridLayoutManager);
                break;
            default:
                break;
        }


        setItemAnimator(null);

        setOverScrollMode(OVER_SCROLL_NEVER);


        if (pullRefreshEnabled) {
            ArrowRefreshHeader refreshHeader = new ArrowRefreshHeader(context);
            mHeaderViews.add(0, refreshHeader);
            mRefreshHeader = refreshHeader;
            mRefreshHeader.setProgressStyle(mRefreshProgressStyle);
        }
        LoadingMoreFooter footView = new LoadingMoreFooter(context);
        footView.setProgressStyle(mLoadingMoreProgressStyle);
        addFootView(footView);
        mFootViews.get(0).setVisibility(GONE);
    }

    public void addHeaderView(View view) {
        if (pullRefreshEnabled && !(mHeaderViews.get(0) instanceof ArrowRefreshHeader)) {
            ArrowRefreshHeader refreshHeader = new ArrowRefreshHeader(view.getContext());
            mHeaderViews.add(0, refreshHeader);
            mRefreshHeader = refreshHeader;
            mRefreshHeader.setProgressStyle(mRefreshProgressStyle);
        }
        mHeaderViews.add(view);
    }

    public void addFootView(final View view) {
        mFootViews.clear();
        mFootViews.add(view);
    }

    public void loadMoreComplete(boolean isNoMore) {
        isLoadingData = false;
        View footView = mFootViews.get(0);
        if (!isNoMore) {
            if (footView instanceof LoadingMoreFooter) {
                ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_COMPLETE);
            } else {
                footView.setVisibility(View.GONE);
            }
        } else {
            if (footView instanceof LoadingMoreFooter) {
                ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_NOMORE);
            } else {
                footView.setVisibility(View.GONE);
            }
            isnomore = true;
        }
    }


    public void refreshComplete() {
        mRefreshHeader.refreshComplate();
    }

    public void setRefreshHeader(ArrowRefreshHeader refreshHeader) {
        mRefreshHeader = refreshHeader;
    }

    public void setPullRefreshEnabled(boolean enabled) {
        pullRefreshEnabled = enabled;
    }

    public void setLoadingMoreEnabled(boolean enabled) {
        loadingMoreEnabled = enabled;
        if (!enabled) {
            mFootViews.clear();
        }
    }

    public void setRefreshProgressStyle(int style) {
        mRefreshProgressStyle = style;
        if (mRefreshHeader != null) {
            mRefreshHeader.setProgressStyle(style);
        }
    }

    public void setLaodingMoreProgressStyle(int style) {
        mLoadingMoreProgressStyle = style;
        if (mFootViews.size() > 0 && mFootViews.get(0) instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootViews.get(0)).setProgressStyle(style);
        }
    }

    public void setArrowImageView(int resid) {
        if (mRefreshHeader != null) {
            mRefreshHeader.setArrowImageView(resid);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mWrapAdapter = new WrapAdapter(mHeaderViews, mFootViews, adapter);
        mWrapAdapter.setOnItemClickListener(mOnItemClickListener);
        mWrapAdapter.setOnItemLongClickListener(mOnItemLongClickListener);
        mWrapAdapter.setItemSelectorId(mItemSelectorId);
        mWrapAdapter.setSpace(mVerticalSpacing, mHorizontalSpacing);
        if (mStyle == GRID) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager)getLayoutManager();
            gridLayoutManager.setSpanSizeLookup(
                    mWrapAdapter.createSpanSizeLookup(mNumColumns));
        }
        mAdapter.registerAdapterDataObserver(mDataObserver);
        super.setAdapter(mWrapAdapter);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 1 && layoutManager.getItemCount() > layoutManager.getChildCount() && !isnomore && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {

                View footView = mFootViews.get(0);
                isLoadingData = true;
                if (footView instanceof LoadingMoreFooter) {
                    ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_LAODING);
                } else {
                    footView.setVisibility(View.VISIBLE);
                }
                mLoadingListener.onLoadMore();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled) {
                    mRefreshHeader.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeader.getVisiableHeight() > 5 && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                        return true;
                    }

                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener.onRefresh();
                            isnomore = false;
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private boolean isOnTop() {
        LayoutManager layoutManager = getLayoutManager();
        int firstVisibleItemPosition;
        if (layoutManager instanceof GridLayoutManager) {
            firstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(into);
            firstVisibleItemPosition = findMax(into);
        } else {
            firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if (firstVisibleItemPosition <= 1) {
            return true;
        }
        return false;

    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mWrapAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {

        this.mOnItemClickListener = mOnItemClickListener;
        if (mWrapAdapter != null) {
            mWrapAdapter.setOnItemClickListener(mOnItemClickListener);
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
        if (mWrapAdapter != null) {
            mWrapAdapter.setOnItemLongClickListener(mOnItemLongClickListener);
        }
    }

    public class WrapAdapter<VH extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

        private RecyclerView.Adapter adapter;

        private ArrayList<View> mHeaderViews;

        private ArrayList<View> mFootViews;

        private int headerPosition = 1;

        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;

        private int itemSelectorId;
        private int verticalSpacing;
        private int horizontalSpacing;

        public WrapAdapter(ArrayList<View> headerViews, ArrayList<View> footViews, RecyclerView.Adapter adapter) {
            this.adapter = adapter;
            this.mHeaderViews = headerViews;
            this.mFootViews = footViews;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position) || isFooter(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }

        public boolean isHeader(int position) {
            return position >= 0 && position < mHeaderViews.size();
        }

        public boolean isFooter(int position) {
            return position < getItemCount() && position >= getItemCount() - mFootViews.size();
        }

        public boolean isRefreshHeader(int position) {
            return position == 0;
        }

        public int getHeadersCount() {
            return mHeaderViews.size();
        }

        public int getFootersCount() {
            return mFootViews.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder(mHeaderViews.get(0));
            } else if (viewType == TYPE_HEADER) {
                return new SimpleViewHolder(mHeaderViews.get(headerPosition++));
            } else if (viewType == TYPE_FOOTER) {
                return new SimpleViewHolder(mFootViews.get(0));
            }
            RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(parent, viewType);
            if (verticalSpacing != 0 || horizontalSpacing != 0) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams();
                if (params == null) {
                    params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    holder.itemView.setLayoutParams(params);
                }
                params.topMargin = params.bottomMargin = verticalSpacing;
                params.rightMargin = params.leftMargin = horizontalSpacing;
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final int realPosition = getRealPosition(position);
            if (isHeader(position)) {
                return;
            }
            int adjPosition = position - getHeadersCount();
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition);
                    if (onItemClickListener != null) {

                        holder.itemView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!isFullSpan(position)) {
                                    onItemClickListener.onItemClick(XRecyclerView.this, view, realPosition, getItemId(realPosition));
                                }
                            }
                        });
                    }
                    if (onItemLongClickListener != null) {

                        holder.itemView.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                return !isFullSpan(position) && onItemLongClickListener.onItemLongClick(XRecyclerView.this, view, realPosition, getItemId(realPosition));
                            }
                        });
                    }

                    if (itemSelectorId != 0) {
                        if (!isFullSpan(position)) {
                            holder.itemView.setBackgroundResource(itemSelectorId);
                        }
                    }
                    return;
                }
            }

        }

        public boolean isFullSpan(int position) {
            return isHeaderPosition(position) || isFooterPosition(position);
        }

        private boolean isHeaderPosition(int position) {
            return position < getHeadersCount();
        }

        private boolean isFooterPosition(int position) {
            int footerCount = getFootersCount();

            if (footerCount == 0) {
                return false;
            } else {
                return position == (getHeadersCount() + adapter.getItemCount());
            }

        }

        private int getRealPosition(int position) {
            return position - getHeadersCount();
        }

        @Override
        public int getItemCount() {
            if (adapter != null) {
                return getHeadersCount() + getFootersCount() + adapter.getItemCount();
            } else {
                return getHeadersCount() + getFootersCount();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isHeader(position)) {
                return TYPE_HEADER;
            }
            if (isFooter(position)) {
                return TYPE_FOOTER;
            }
            int adjPosition = position - getHeadersCount();
            ;
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return adapter.getItemViewType(adjPosition);
                }
            }
            return TYPE_NORMAL;
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getHeadersCount()) {
                int adjPosition = position - getHeadersCount();
                int adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        public void setSpace(int verticalSpacing, int horizontalSpacing) {
            this.verticalSpacing = verticalSpacing;
            this.horizontalSpacing = horizontalSpacing;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
        }

        public void setItemSelectorId(int itemSelectorId) {
            this.itemSelectorId = itemSelectorId;
        }

        public void setVerticalSpacing(int verticalSpacing) {
            this.verticalSpacing = verticalSpacing;
        }

        public void setHorizontalSpacing(int horizontalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
        }


        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }

        public GridLayoutManager.SpanSizeLookup createSpanSizeLookup(int spanCount) {
            return new CustomSpanSizeLookup(null, spanCount);
        }

        private final class CustomSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
            private final GridLayoutManager.SpanSizeLookup source;
            private final int spanCount;

            private CustomSpanSizeLookup(GridLayoutManager.SpanSizeLookup source, int spanCount) {
                this.source = source;
                this.spanCount = spanCount;
            }

            @Override
            public int getSpanSize(int position) {

                if (isFullSpan(position)) {
                    return spanCount;
                } else {
                    return 1;
                }
            }
        }
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView parent, View view, int position, long id);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView parent, View view, int position, long id);
    }
}
