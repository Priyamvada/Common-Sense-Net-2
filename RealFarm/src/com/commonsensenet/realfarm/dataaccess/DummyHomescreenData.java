package com.commonsensenet.realfarm.dataaccess;

import java.util.Random;
import java.util.Vector;

import android.R.anim;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.commonsensenet.realfarm.R;
import com.commonsensenet.realfarm.model.Recommendation;

public class DummyHomescreenData extends BaseAdapter {
	protected Context mCtx;
	protected Activity mActivity;
	protected LayoutInflater mInflater;
	protected RealFarmDatabase mDb;
	protected RealFarmProvider mDataProvider;
	protected Vector<Recommendation> mInfoPile;
	
	public DummyHomescreenData(Context ctx, Activity activity, int numOfItems) {
		mCtx = ctx;
		mActivity = activity;
		mInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDb = new RealFarmDatabase(mCtx);
		mDataProvider = new RealFarmProvider(mDb);
		generateDummyItems(numOfItems);
	}
	
	private void generateDummyItems(int numOfItems) {
		Random rn = new Random();
		// get maximum numbers for each type
		int maxAct = mDataProvider.getActionNamesList().size()-3;
		int maxSeed = mDataProvider.getSeedsList().size()-3;
		// generation
		mInfoPile = new Vector<Recommendation>(numOfItems);
		Recommendation tmpObj;
		for( int runner = 0; runner < numOfItems; ++runner){
			int actionId = 3+rn.nextInt(maxAct);
			int seedId = 3+rn.nextInt(maxSeed);
			tmpObj = new Recommendation(runner, seedId, actionId, "date");
			mInfoPile.add(tmpObj);
		}
	}
	
	public void generateDummyItems(int numOfItems, Vector<Recommendation> infoCont){
		Random rn = new Random();
		// get maximum numbers for each type
		int maxAct = mDataProvider.getActionNamesList().size()-3;
		int maxSeed = mDataProvider.getSeedsList().size()-3;
		// generation
		infoCont.clear();
		Recommendation tmpObj;
		for( int runner = 0; runner < numOfItems; ++runner){
			int actionId = 3+rn.nextInt(maxAct);
			int seedId = 3+rn.nextInt(maxSeed);
			tmpObj = new Recommendation(runner, seedId, actionId, "date");
			infoCont.add(tmpObj);
		}
	}

//	@Override
	public int getCount() {
		return mInfoPile.size();
	}

//	@Override
	public Object getItem(int arg0) {
		return mInfoPile.get(arg0);
	}

//	@Override
	public long getItemId(int position) {
		return mInfoPile.get(position).getId();
	}
	
//	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View element;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	element = mInflater.inflate(R.layout.aggregate_item, parent, false );
        } else {
        	element = convertView;
        }

		// change the title
        TextView lblDesc = (TextView) element.findViewById(R.id.lbl_desc);
        TextView lblDetail = (TextView) element.findViewById(R.id.lbl_detail);
        ImageView imgDesc = (ImageView) element.findViewById(R.id.img_desc);
        ImageButton btnLike = (ImageButton) element.findViewById(R.id.aggr_item_btn_like);

        Recommendation tmpRec = mInfoPile.get(position);
        lblDesc.setText( mDataProvider.getActionNameById(tmpRec.getAction()).getName());
        lblDetail.setText( mDataProvider.getSeedById(tmpRec.getSeed()).getName());
        imgDesc.setImageResource(mDataProvider.getActionNameById(tmpRec.getAction()).getRes());
        
        btnLike.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				v.setBackgroundResource(R.drawable.circular_btn_green);
			}
		});
        
        return element;
	}
	
	public RealFarmProvider getDataProvider(){
		return mDataProvider;
	}
	
	public RealFarmDatabase getDatabase(){
		return mDb;
	}
}
