package com.commonsensenet.realfarm.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.commonsensenet.realfarm.R;
import com.commonsensenet.realfarm.dataaccess.RealFarmProvider;
import com.commonsensenet.realfarm.model.Action;

public class ActionItemAdapter extends ArrayAdapter<Action> {

	/** Database provided that used to obtain the required data. */
	private RealFarmProvider mDataProvider;

	/**
	 * Creates a new ActionItemAdapter instance.
	 */
	public ActionItemAdapter(Context context, List<Action> actions,
			RealFarmProvider dataProvider) {
		super(context, android.R.layout.simple_list_item_1, actions);

		// TODO: this shouldn't be done!!
		mDataProvider = dataProvider;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ActionItemWrapper wrapper = null;
		if (row == null) {

			LayoutInflater li = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			row = li.inflate(R.layout.tpl_diary_item, parent, false);
			wrapper = new ActionItemWrapper(row);
			row.setTag(wrapper);
		} else {
			wrapper = (ActionItemWrapper) row.getTag();
		}

		wrapper.populateFrom(this.getItem(position), mDataProvider,
				this.getContext());
		return (row);
	}
}
