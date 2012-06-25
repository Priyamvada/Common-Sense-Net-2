package com.commonsensenet.realfarm.aggregates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.commonsensenet.realfarm.Global;
import com.commonsensenet.realfarm.homescreen.HelpEnabledActivity;
import com.commonsensenet.realfarm.R;
import com.commonsensenet.realfarm.control.NumberPicker;
import com.commonsensenet.realfarm.dataaccess.RealFarmProvider;
import com.commonsensenet.realfarm.homescreen.Homescreen;
import com.commonsensenet.realfarm.model.User;
import com.commonsensenet.realfarm.model.aggregate.AggregateRecommendation;

public class fertilize_aggregate extends HelpEnabledActivity implements
		OnLongClickListener {
	/** Database provider used to persist the data. */
	private RealFarmProvider mDataProvider;
	private AggregateRecommendation aggrRec;
	/** Reference to the current instance. */
	private final fertilize_aggregate mParentReference = this;
	private String units_fert = "0", fert_var_sel = "0", day_fert_sel = "0",
			day_fert_sel_1;
	private int fert_no, day_fert_int;
	private String fert_no_sel, months_fert = "0";
	boolean liked;
	public void onBackPressed() {

		// stops all active audio.
		stopAudio();

		if (Global.writeToSD == true) {
			String logtime = getCurrentTime();
			mDataProvider.File_Log_Create("UIlog.txt", logtime + " -> ");
			mDataProvider.File_Log_Create("UIlog.txt", " Fertilizing "
					+ " Softkey " + " click " + " Back_button " + " null "
					+ " \r\n");
		}
		Intent adminintent = new Intent(fertilize_aggregate.this,
				Homescreen.class);

		startActivity(adminintent);
		fertilize_aggregate.this.finish();

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("Fertilizer Aggregate entered");
		mDataProvider = RealFarmProvider.getInstance(this);
	
	//	super.onCreate(savedInstanceState);
	//	setContentView(R.layout.fertilizing_dialog);
		
		super.onCreate(savedInstanceState, R.layout.fertilize_aggregate);    
		System.out.println("Fertilizer Aggregate entered");  
		setHelpIcon(findViewById(R.id.helpIndicator));   
		ImageButton btnLike = (ImageButton) findViewById(R.id.aggr_item_fert_like1);
		System.out.println("Fertilizer Aggregate entered");
		btnLike.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		
		if (v.getId() == R.id.aggr_item_fert_like1) {
			
			// for the like button
			if (!liked) {
				v.setBackgroundResource(R.drawable.circular_btn_green);
			} else {
				v.setBackgroundResource(R.drawable.circular_btn_normal);
			}
			liked = !liked;
		}
		
			}
		});
		
		ImageButton btnLike2 = (ImageButton) findViewById(R.id.aggr_item_fert_like2);
		System.out.println("Fertilizer Aggregate entered");
		btnLike2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		
		if (v.getId() == R.id.aggr_item_fert_like2) {
			
			// for the like button
			if (!liked) {
				v.setBackgroundResource(R.drawable.circular_btn_green);
			} else {
				v.setBackgroundResource(R.drawable.circular_btn_normal);
			}
			liked = !liked;
		}
		
			}
		});
		

		final ImageButton img_action = (ImageButton) findViewById(R.id.aggr_img_action);
		

		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		
		List<String> list = new ArrayList<String>();
		list.add("list 1");
		list.add("list 2");
		list.add("list 3");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(dataAdapter);  

		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
		
		
		final Button userslist = (Button) findViewById(R.id.txt_btn_1);
	    /*  Spinner spinner = (Spinner) findViewById(R.id.spinner1);

	        Integer[] image = { R.drawable.ic_72px_fertilizing2, R.drawable.ic_72px_fertilizing2, R.drawable.ic_72px_fertilizing2 };
	        spinner.getLayoutParams().width = 3;

	        // Customise ArrayAdapter
	        spinner.setAdapter(new SpinnerImgAdapter(this, R.layout.spinner_op, image));*/
		
		
		userslist.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			
					Dialog dlg = new Dialog(v.getContext());
					dlg.setContentView(R.layout.vi_aggr_recommendation_details);
					dlg.setCancelable(true);
					// parts
					TextView lblDetals = (TextView) dlg
							.findViewById(R.id.dlg_lbl_details);
					ImageView imgAction = (ImageView) dlg
							.findViewById(R.id.dlg_img_action);
					ImageView imgSeed = (ImageView) dlg.findViewById(R.id.dlg_img_seed);
					LinearLayout peopleList = (LinearLayout) dlg
							.findViewById(R.id.dlg_linlay_userDataDetails);
					ImageButton btnSound = (ImageButton) dlg
							.findViewById(R.id.dlg_btn_audio_play);
					//
					dlg.setTitle("");
					lblDetals.setText(mDataProvider.getActionNameById(
							aggrRec.getAction()).getNameKannada()
							+ " "
							+ mDataProvider.getSeedById(aggrRec.getSeed())
									.getNameKannada());
					imgAction.setImageResource(mDataProvider.getActionNameById(
							aggrRec.getAction()).getRes());
					imgSeed.setImageResource(mDataProvider
							.getSeedById(aggrRec.getSeed()).getResBg());
					// fill user list
					Iterator<Integer> iterPeople = aggrRec.getUserIds().iterator();

					while (iterPeople.hasNext()) {
						Integer usrId = iterPeople.next();
						View lin = dlg.getLayoutInflater().inflate(
								R.layout.element_farmer, peopleList, false);
						TextView lblName = (TextView) lin
								.findViewById(R.id.lbl_farmername);
						ImageView imgFarmer = (ImageView) lin
								.findViewById(R.id.img_farmer);
						User usr = mDataProvider.getUserById(usrId);

						int resID = dlg
								.getContext()
								.getResources()
								.getIdentifier(usr.getUserImgName(), "drawable",
										"com.commonsensenet.realfarm");
						imgFarmer.setImageResource(resID);
						lblName.setText(usr.getFirstName() + " " + usr.getLastName());
						peopleList.addView(lin);
					}

					btnSound.setOnClickListener(this);

			
					dlg.show();
				
				
			}
		});

	
	}

	protected void initmissingval() {
		playAudio(R.raw.missinginfo);
		//ShowHelpIcon(v);  
	}

	public boolean onLongClick(View v) {

		if (v.getId() == R.id.home_btn_var_fert) {

			playAudio(R.raw.selecttypeoffertilizer);
			ShowHelpIcon(v);  

			if (Global.writeToSD == true) {

				String logtime = getCurrentTime();
				mDataProvider.File_Log_Create("UIlog.txt", logtime + " -> ");
				mDataProvider.File_Log_Create("UIlog.txt", " Fertilizing "
						+ " audio " + " longtap " + " type_fertilizer "
						+ "Audio_played" + " \r\n");
			}

		}

		if (v.getId() == R.id.home_btn_units_fert) {

			playAudio(R.raw.selecttheunits);
			ShowHelpIcon(v);  
			if (Global.writeToSD == true) {

				String logtime = getCurrentTime();
				mDataProvider.File_Log_Create("UIlog.txt", logtime + " -> ");
				mDataProvider.File_Log_Create("UIlog.txt", " Fertilizing "
						+ " audio " + " longtap " + " units_fertilizer "
						+ "Audio_played" + " \r\n");

			}
		}

		if (v.getId() == R.id.home_btn_units_no_fert) {

			playAudio(R.raw.selecttheunits);
			ShowHelpIcon(v);  

			if (Global.writeToSD == true) {

				String logtime = getCurrentTime();
				mDataProvider.File_Log_Create("UIlog.txt", logtime + " -> ");
				mDataProvider.File_Log_Create("UIlog.txt", " Fertilizing "
						+ " audio " + " longtap " + " units_fertilizer "
						+ "Audio_played" + " \r\n");

			}
		}

		if (v.getId() == R.id.home_btn_day_fert) {

			playAudio(R.raw.selectthedate);
			ShowHelpIcon(v);  
			
			if (Global.writeToSD == true) {

				String logtime = getCurrentTime();
				mDataProvider.File_Log_Create("UIlog.txt", logtime + " -> ");
				mDataProvider.File_Log_Create("UIlog.txt", " Fertilizing "
						+ " audio " + " longtap " + " day_fertilizer "
						+ "Audio_played" + " \r\n");

			}
		}

		if (v.getId() == R.id.fert_ok) {

			playAudio(R.raw.ok);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.fert_cancel) {

			playAudio(R.raw.cancel);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.aggr_img_help) {

			playAudio(R.raw.help);
			ShowHelpIcon(v);  
			if (Global.writeToSD == true) {

				String logtime = getCurrentTime();
				mDataProvider.File_Log_Create("UIlog.txt", logtime + " -> ");
				mDataProvider.File_Log_Create("UIlog.txt", " Fertilizing "
						+ " audio " + " longtap " + " help_fertilizer "
						+ "Audio_played" + " \r\n");
			}
		}

		if (v.getId() == R.id.home_var_fert_1) { // audio integration
			playAudio(R.raw.fertilizer1);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_var_fert_2) {
			playAudio(R.raw.fertilizer2);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_var_fert_3) {
			playAudio(R.raw.fertilizer3);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_btn_units_1) {
			playAudio(R.raw.bagof10kg);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_btn_units_2) {
			playAudio(R.raw.bagof20kg);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_btn_units_3) {
			playAudio(R.raw.bagof50kg);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_day_1) {
			playAudio(R.raw.twoweeksbefore);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_day_2) {
			playAudio(R.raw.oneweekbefore);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_day_3) {
			playAudio(R.raw.yesterday);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_day_4) {
			playAudio(R.raw.todayonly);
			ShowHelpIcon(v);  
		}

		if (v.getId() == R.id.home_day_5) {
			playAudio(R.raw.tomorrows);
			ShowHelpIcon(v);  
		}
		
		if (v.getId() == R.id.amount_sow_txt_btn) {                        //20-06-2012
			playAudio(R.raw.amount);
			ShowHelpIcon(v);                                     
		}
		
		if (v.getId() == R.id.date_sow_txt_btn) {                        //20-06-2012
			playAudio(R.raw.date);
			ShowHelpIcon(v);                                      
		}
		
		if (v.getId() == R.id.variety_sow_txt_btn) {                        //20-06-2012
			playAudio(R.raw.fertilizername);
			ShowHelpIcon(v);                                      
		}

		
		if (v.getId() == R.id.home_btn_month_fert) {                        //20-06-2012
			playAudio(R.raw.fertilizername);
			ShowHelpIcon(v);                                      
		}
		
		if (v.getId() == R.id.home_month_1) { // added

			playAudio(R.raw.jan);
			ShowHelpIcon(v);                                      //added for help icon
		}
		if (v.getId() == R.id.home_month_2) { // added

			playAudio(R.raw.feb);
			ShowHelpIcon(v);                                      //added for help icon

		}

		if (v.getId() == R.id.home_month_3) { // added

			playAudio(R.raw.mar);
			ShowHelpIcon(v);                                      //added for help icon

		}

		if (v.getId() == R.id.home_month_4) { // added

			playAudio(R.raw.apr);
			ShowHelpIcon(v);                                      //added for help icon

		}

		if (v.getId() == R.id.home_month_5) { // added

			playAudio(R.raw.may);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_6) { // added

			playAudio(R.raw.jun);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_7) { // added

			playAudio(R.raw.jul);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_8) { // added

			playAudio(R.raw.aug);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_9) { // added

			playAudio(R.raw.sep);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_10) { // added

			playAudio(R.raw.oct);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_11) { // added

			playAudio(R.raw.nov);
			ShowHelpIcon(v);                                      //added for help icon
		}

		if (v.getId() == R.id.home_month_12) { // added

			playAudio(R.raw.dec);
			ShowHelpIcon(v);                                      //added for help icon
		}
		
		if (v.getId() == R.id.number_ok) { // added

			playAudio(R.raw.ok);
			ShowHelpIcon(v);                                      //added for help icon
		}
		
		if (v.getId() == R.id.number_cancel) { // added

			playAudio(R.raw.cancel);
			ShowHelpIcon(v);                                      //added for help icon
		}

		return true;
	}

	protected void cancelAudio() {
		playAudio(R.raw.cancel);
		Intent adminintent = new Intent(fertilize_aggregate.this,
				Homescreen.class);

		startActivity(adminintent);
		fertilize_aggregate.this.finish();
	}

	protected void okAudio() {
		playAudio(R.raw.ok);
	}
}