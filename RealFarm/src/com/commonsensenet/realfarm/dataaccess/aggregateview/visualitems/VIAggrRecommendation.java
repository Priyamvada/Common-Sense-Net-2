package com.commonsensenet.realfarm.dataaccess.aggregateview.visualitems;

import java.util.Iterator;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commonsensenet.realfarm.R;
import com.commonsensenet.realfarm.dataaccess.RealFarmProvider;
import com.commonsensenet.realfarm.model.Seed;
import com.commonsensenet.realfarm.model.User;
import com.commonsensenet.realfarm.model.aggregate.AggregateRecommendation;

public class VIAggrRecommendation extends VisualItemBase {
	private String logTag = "VIAggrRecommendation";
	private static final int layoutTag = 2;

	protected AggregateRecommendation aggrRec;
	protected boolean liked; // TODO should be moved to the data level

	public VIAggrRecommendation(AggregateRecommendation aggrRec, RealFarmProvider dataProvider){
		super(dataProvider);
		this.aggrRec = aggrRec;
		Log.d(logTag, "created");
	}

	@Override
	public View populateView(View view, ViewGroup parent, LayoutInflater inflater){
		Log.d(logTag, "populateView");
		View element;
		if( view != null && (Integer) view.getTag() == layoutTag )
			element = view;
		else
			element = inflater.inflate(R.layout.vi_aggr_recommendation, parent, false);
		
		element.setTag(new Integer(layoutTag));
		// populate elements
		TextView lblCountPeople = (TextView) element.findViewById(R.id.lbl_count_people);
        ImageView imgAction = (ImageView) element.findViewById(R.id.img_action);
        ImageView imgPlant = (ImageView) element.findViewById(R.id.img_plant);
        ImageButton btnLike = (ImageButton) element.findViewById(R.id.aggr_item_btn_like);
        Button btnMain = (Button) element.findViewById(R.id.btn_main_click);

        Seed seed = dataProvider.getSeedById(aggrRec.getSeed());

        lblCountPeople.setText(String.valueOf(aggrRec.getUserIds().size())+" people did this");
        imgAction.setImageResource(dataProvider.getActionNameById(aggrRec.getAction()).getRes());
        // switch plant type to tile file
        //imgPlant.setImageResource(R.id)

        btnLike.setOnClickListener(this);
        btnMain.setOnClickListener(this);

		return element;
	}

	@Override
	public Object getDataItem() {
		return aggrRec;
	}
	
	@Override
	public void onClick(View v) {
		Log.d(logTag, "Click detected");
		if(v.getId() == R.id.aggr_item_btn_like){
			 // for the like button
			if(!liked){
				v.setBackgroundResource(R.drawable.circular_btn_green);
			} else {
				v.setBackgroundResource(R.drawable.circular_btn_normal);
			}
			liked = !liked;
		}
		if( v.getId() == R.id.btn_main_click ){
            Dialog dlg = new Dialog(v.getContext());
        	dlg.setContentView(R.layout.vi_aggr_recommendation_details);
        	dlg.setCancelable(true);
        	// parts
        	TextView dlgDetals = (TextView) dlg.findViewById(R.id.dlg_lbl_details);
        	ImageView imgAction = (ImageView) dlg.findViewById(R.id.dlg_img_action);
        	ImageView imgSeed = (ImageView) dlg.findViewById(R.id.dlg_img_seed);
        	LinearLayout peopleList = (LinearLayout) dlg.findViewById(R.id.dlg_linlay_userDataDetails);
        	// 
        	dlg.setTitle( dataProvider.getActionNameById(aggrRec.getAction()).getName() );
        	dlgDetals.setText( dataProvider.getActionNameById(aggrRec.getAction()).getName() + " " + dataProvider.getSeedById(aggrRec.getSeed()).getName() );
        	imgAction.setImageResource( dataProvider.getActionNameById(aggrRec.getAction()).getRes() );
        	imgSeed.setImageResource( dataProvider.getSeedById(aggrRec.getSeed()).getRes());
        	// fill user list
        	Iterator<Integer> iterPeople = aggrRec.getUserIds().iterator();
        	
        	while( iterPeople.hasNext() ){
        		Integer usrId = iterPeople.next();
        		TextView lblName = new TextView(v.getContext());
        		User usr = dataProvider.getUserById(usrId);
//        		int resID = v.getContext().getResources().getIdentifier(usr.getUserImgName(), "drawable-hdpi", v.getContext().getPackageName());
//        		Drawable img = v.getContext().getResources().getDrawable(resID);
        		lblName.setText( usr.getFirstName() + " " + usr.getLastName() );
//        		lblName.setCompoundDrawables(img, null, null, null);
        		peopleList.addView(lblName);
        	}
        	
        	Log.d(logTag, "Seed res id: "+String.valueOf(dataProvider.getSeedById(aggrRec.getSeed()).getRes()));
        	dlg.show();
		}
	}
	
	@Override
	public int getLayoutTag(){
		return layoutTag;
	}

}
