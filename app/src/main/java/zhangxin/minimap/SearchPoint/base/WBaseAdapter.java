package zhangxin.minimap.SearchPoint.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class WBaseAdapter<T> extends BaseAdapter {
	protected Context mContext;
	protected List<T> mData;
	protected LayoutInflater mInflater;
	protected int mLayoutId;

	public WBaseAdapter(Context context, List<T> data, int layoutId) {
		mContext = context;
		mData = data;
		mInflater = LayoutInflater.from(context);
		mLayoutId = layoutId;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public T getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = ViewHolder.get(mContext, convertView, parent,
				mLayoutId, position);
		convert(holder, getItem(position), position);
		return holder.getRootView();
	}

	public abstract void convert(ViewHolder holder, T t, int position);
}
