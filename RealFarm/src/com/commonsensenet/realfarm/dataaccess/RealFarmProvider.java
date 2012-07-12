package com.commonsensenet.realfarm.dataaccess;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.commonsensenet.realfarm.Global;
import com.commonsensenet.realfarm.model.Action;
import com.commonsensenet.realfarm.model.ActionName;
import com.commonsensenet.realfarm.model.Fertilizing;
import com.commonsensenet.realfarm.model.Harvesting;
import com.commonsensenet.realfarm.model.Irrigation;
import com.commonsensenet.realfarm.model.MarketPrice;
import com.commonsensenet.realfarm.model.Plot;
import com.commonsensenet.realfarm.model.Problem;
import com.commonsensenet.realfarm.model.SeedType;
import com.commonsensenet.realfarm.model.Selling;
import com.commonsensenet.realfarm.model.Sowing;
import com.commonsensenet.realfarm.model.Spraying;
import com.commonsensenet.realfarm.model.User;
import com.commonsensenet.realfarm.model.WeatherForecast;

public class RealFarmProvider {
	public abstract interface OnDataChangeListener {
		public abstract void onDataChanged(String data, int temperature,
				String type, int adminflag);
	}

	/** Date format used throughout the application. */
	public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/** Name of the folder where the log is located. */
	public static final String LOG_FOLDER = "/csn_app_logs";
	private static Map<Context, RealFarmProvider> sMapProviders = new HashMap<Context, RealFarmProvider>();
	/** Listener used to detect changes in the weather forecast. */
	private static OnDataChangeListener sWeatherForecastDataListener;

	public static RealFarmProvider getInstance(Context ctx) {
		if (!sMapProviders.containsKey(ctx))
			sMapProviders.put(ctx, new RealFarmProvider(new RealFarmDatabase(
					ctx)));
		return sMapProviders.get(ctx);
	}

	/** Cached ActionNames to improve performance. */
	private List<ActionName> mAllActionNames;
	/** Cached seeds to improve performance. */
	private List<SeedType> mAllSeeds;
	/** Calendar instance used to log activities. */
	private Calendar mCalendar = Calendar.getInstance();
	/** Real farm database access. */
	private RealFarmDatabase mDatabase;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
	/** Path of the logging directory inside the SD card. */
	private String mExternalDirectoryLog;
	private File mFile;
	private File mFile1;
	private FileWriter mFileWriter;
	private FileWriter mFileWriter1;

	protected RealFarmProvider(RealFarmDatabase database) {

		// database that will be used to handle data.
		mDatabase = database;

		// used to force creation.
		mDatabase.open();
		mDatabase.close();
	}

	public long addWeatherForecast(String date, int value, String type,
			int adminFlag) {

		Log.d("WF values: ", "in setdata");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_DATE, date);
		args.put(RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_TEMPERATURE,
				value);
		args.put(RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_TYPE, type);
		mDatabase.open();

		long result = mDatabase.insertEntries(
				RealFarmDatabase.TABLE_NAME_WEATHERFORECAST, args);

		mDatabase.close();

		// notifies any listener that the data changed.
		if (sWeatherForecastDataListener != null) {
			sWeatherForecastDataListener.onDataChanged(date, value, type,
					adminFlag);
		}
		Log.d("done: ", "wf setdata");
		return result;
	}

	public void File_Log_Create(String FileNameWrite, String Data) {

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/csn_app_logs");
		if (!folder.exists()) {
			folder.mkdir();
		}

		// File file = new File(mExternalDirectoryLog, "Test5.txt");
		// //LoggedData is the file to which values will be written
		if (FileNameWrite == "value.txt") {
			mExternalDirectoryLog = Environment.getExternalStorageDirectory()
					.toString() + LOG_FOLDER;
			// mExternalDirectoryLog =
			// Environment.getExternalStorageDirectory().toString();
			mFile = new File(mExternalDirectoryLog, FileNameWrite);
			// fWriter =new FileWriter(file);

			try {
				mFileWriter = new FileWriter(mFile, true);
				mFileWriter.append(Data);
				// fWriter.newLine();
				mFileWriter.close();
				// Toast.makeText(this,
				// "Your text was written to SD Card successfully...",
				// Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e("WRITE TO SD", e.getMessage());
			}

		}
		if (FileNameWrite == "UIlog.txt") {
			mExternalDirectoryLog = Environment.getExternalStorageDirectory()
					.toString() + LOG_FOLDER;
			// mExternalDirectoryLog =
			// Environment.getExternalStorageDirectory().toString();
			mFile1 = new File(mExternalDirectoryLog, FileNameWrite);
			// fWriter =new FileWriter(file);

			try {
				mFileWriter1 = new FileWriter(mFile1, true);
				mFileWriter1.append(Data);
				// fWriter.newLine();
				mFileWriter1.close();
				// Toast.makeText(this,
				// "Your text was written to SD Card successfully...",
				// Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e("WRITE TO SD", e.getMessage());
			}
		}

	}

	public ActionName getActionNameById(int actionNameId) {

		mDatabase.open();

		ActionName tmpAction = null;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTIONNAME,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTIONNAME_NAME,
								RealFarmDatabase.COLUMN_NAME_ACTIONNAME_NAMEKANNADA,
								RealFarmDatabase.COLUMN_NAME_ACTIONNAME_RESOURCE,
								RealFarmDatabase.COLUMN_NAME_ACTIONNAME_AUDIO },
						RealFarmDatabase.COLUMN_NAME_ACTIONNAME_ID + "="
								+ actionNameId, null, null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();

			tmpAction = new ActionName(actionNameId, c.getString(0),
					c.getString(1), c.getInt(2), c.getInt(3));
		}
		c.close();
		mDatabase.close();

		return tmpAction;
	}

	public List<ActionName> getActionNames() {

		if (mAllActionNames == null) {
			// opens the database.
			mDatabase.open();

			// query all actions
			Cursor c = mDatabase
					.getEntries(
							RealFarmDatabase.TABLE_NAME_ACTIONNAME,
							new String[] {
									RealFarmDatabase.COLUMN_NAME_ACTIONNAME_ID,
									RealFarmDatabase.COLUMN_NAME_ACTIONNAME_NAME,
									RealFarmDatabase.COLUMN_NAME_ACTIONNAME_NAMEKANNADA,
									RealFarmDatabase.COLUMN_NAME_ACTIONNAME_RESOURCE,
									RealFarmDatabase.COLUMN_NAME_ACTIONNAME_AUDIO },
							null, null, null, null, null);
			c.moveToFirst();

			mAllActionNames = new LinkedList<ActionName>();

			if (c.getCount() > 0) {
				do {
					mAllActionNames.add(new ActionName(c.getInt(0), c
							.getString(1), c.getString(2), c.getInt(3), c
							.getInt(4)));

					String log = "ACTIONNAME_ID: " + c.getInt(0) + " ,NAME "
							+ c.getString(1) + " ,NAME_KANNADA: "
							+ c.getString(2) + " ,RESOURCE" + c.getInt(3)
							+ " AUDIO " + c.getInt(4) + " ADMINFLAG "
							+ c.getInt(5) + "\r\n";
					Log.d("action name values: ", log);

					if (Global.writeToSD == true) {
						File_Log_Create("value.txt", "Action names table \r\n");
						File_Log_Create("value.txt", log);
					}

				} while (c.moveToNext());
			}

			c.close();
			mDatabase.close();

		}

		return mAllActionNames;
	}

	public List<Action> getActions() {

		List<Action> tmpActions = new LinkedList<Action>();

		mDatabase.open();

		Cursor c = mDatabase
				.getAllEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID,
								RealFarmDatabase.COLUMN_NAME_ACTION_GROWINGID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SEEDVARIETY,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_TYPEOFFERTILIZER,
								RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_HARVESTFEEDBACK,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLINGPRICE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUALITYOFSEED,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
								RealFarmDatabase.COLUMN_NAME_ACTION_TREATMENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_PESTICIDETYPE });

		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				Action aa = new Action(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getString(7), c.getString(8),
						c.getInt(9), c.getInt(10), c.getString(11),
						c.getString(12), c.getString(13), c.getInt(14),
						c.getString(15), c.getString(16), c.getInt(17),
						c.getInt(18), c.getString(19), c.getString(20),
						c.getString(21));
				tmpActions.add(aa);

				String log = "ACTION_ID: " + c.getInt(0) + " ,ACTIONNAMEID: "
						+ c.getInt(1) + " ,GROWINGID " + c.getInt(2)
						+ ",ACTIONTYPE: " + c.getString(3) + " ,SEEDVARIETY: "
						+ c.getString(4) + " Quantity1: " + c.getInt(5)
						+ " Quantity2: " + c.getInt(6) + " Units: "
						+ c.getString(7) + " , DAY: " + c.getString(8)
						+ " , user id: " + c.getInt(9) + " , plot id: "
						+ c.getInt(10) + " ,TYPEOFFERTILIZER "
						+ c.getString(11) + " , PROBLEMStype: "
						+ c.getString(12) + " , , FEEDBACK: " + c.getString(13)
						+ " , SELLINGPRICE: " + c.getInt(14)
						+ " ,QUALITYOFSEED: " + c.getString(15)
						+ " , sell TYPE: " + c.getString(16) + " , SENT: "
						+ c.getInt(17) + " , ISADMIN: " + c.getInt(18)
						+ " , ACTIONPERFORMEDDATE: " + c.getString(19)
						+ " , TREATMENT: " + c.getString(20)
						+ " , PESTICIDETYPE: " + c.getString(21) + "\r\n";
				Log.d("values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "New Action table \r\n");
					File_Log_Create("value.txt", log);
				}
			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpActions;
	}

	public List<Action> getActionsByUserId(int userId) {

		List<Action> tmpActions = new LinkedList<Action>();

		mDatabase.open();

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID,
								RealFarmDatabase.COLUMN_NAME_ACTION_GROWINGID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SEEDVARIETY,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_TYPEOFFERTILIZER,
								RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_HARVESTFEEDBACK,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLINGPRICE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUALITYOFSEED,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
								RealFarmDatabase.COLUMN_NAME_ACTION_TREATMENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_PESTICIDETYPE },
						RealFarmDatabase.COLUMN_NAME_ACTION_USERID + "= "
								+ userId, null, null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				Action aa = new Action(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getString(7), c.getString(8),
						c.getInt(9), c.getInt(10), c.getString(11),
						c.getString(12), c.getString(13), c.getInt(14),
						c.getString(15), c.getString(16), c.getInt(17),
						c.getInt(18), c.getString(19), c.getString(20),
						c.getString(21));
				tmpActions.add(aa);

				String log = "ACTION_ID: " + c.getInt(0) + " ,ACTIONNAMEID: "
						+ c.getInt(1) + " ,GROWINGID " + c.getInt(2)
						+ ",ACTIONTYPE: " + c.getString(3) + " ,SEEDVARIETY: "
						+ c.getString(4) + " Quantity1: " + c.getInt(5)
						+ " Quantity2: " + c.getInt(6) + " Units: "
						+ c.getString(7) + " , DAY: " + c.getString(8)
						+ " , user id: " + c.getInt(9) + " , plot id: "
						+ c.getInt(10) + " ,TYPEOFFERTILIZER "
						+ c.getString(11) + " , PROBLEMStype: "
						+ c.getString(12) + " , , FEEDBACK: " + c.getString(13)
						+ " , SELLINGPRICE: " + c.getInt(14)
						+ " ,QUALITYOFSEED: " + c.getString(15)
						+ " , sell TYPE: " + c.getString(16) + " , SENT: "
						+ c.getInt(17) + " , ISADMIN: " + c.getInt(18)
						+ " , ACTIONPERFORMEDDATE: " + c.getString(19)
						+ " , TREATMENT: " + c.getString(20)
						+ " , PESTICIDETYPE: " + c.getString(21);
				Log.d("values: ", log);
			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpActions;
	}

	public List<Action> getActionsByUserIdAndPlotId(int userId, int plotId) { // modified

		List<Action> tmpActions = new LinkedList<Action>();

		mDatabase.open();

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID,
								RealFarmDatabase.COLUMN_NAME_ACTION_GROWINGID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SEEDVARIETY,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_TYPEOFFERTILIZER,
								RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_HARVESTFEEDBACK,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLINGPRICE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUALITYOFSEED,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
								RealFarmDatabase.COLUMN_NAME_ACTION_TREATMENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_PESTICIDETYPE },
						RealFarmDatabase.COLUMN_NAME_ACTION_USERID + "= "
								+ userId + " AND "
								+ RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID
								+ "= " + plotId + "", null, null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				Action aa = new Action(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getString(7), c.getString(8),
						c.getInt(9), c.getInt(10), c.getString(11),
						c.getString(12), c.getString(13), c.getInt(14),
						c.getString(15), c.getString(16), c.getInt(17),
						c.getInt(18), c.getString(19), c.getString(20),
						c.getString(21));
				tmpActions.add(aa);

				String log = "ACTION_ID: " + c.getInt(0) + " ,ACTIONNAMEID: "
						+ c.getInt(1) + " ,GROWINGID " + c.getInt(2)
						+ ",ACTIONTYPE: " + c.getString(3) + " ,SEEDVARIETY: "
						+ c.getString(4) + " Quantity1: " + c.getInt(5)
						+ " Quantity2: " + c.getInt(6) + " Units: "
						+ c.getString(7) + " , DAY: " + c.getString(8)
						+ " , user id: " + c.getInt(9) + " , plot id: "
						+ c.getInt(10) + " ,TYPEOFFERTILIZER "
						+ c.getString(11) + " , PROBLEMStype: "
						+ c.getString(12) + " , , FEEDBACK: " + c.getString(13)
						+ " , SELLINGPRICE: " + c.getInt(14)
						+ " ,QUALITYOFSEED: " + c.getString(15)
						+ " , sell TYPE: " + c.getString(16) + " , SENT: "
						+ c.getInt(17) + " , ISADMIN: " + c.getInt(18)
						+ " , ACTIONPERFORMEDDATE: " + c.getString(19)
						+ " , TREATMENT: " + c.getString(20)
						+ " , PESTICIDETYPE: " + c.getString(21);
				Log.d("values: ", log);
			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpActions;
	}

	public RealFarmDatabase getDatabase() {
		return mDatabase;
	}

	public Cursor getFertilizer() {

		mDatabase.open();

		Cursor c = mDatabase.getAllEntries(
				RealFarmDatabase.TABLE_NAME_FERTILIZER, new String[] {
						RealFarmDatabase.COLUMN_NAME_FERTILIZER_ID,
						RealFarmDatabase.COLUMN_NAME_FERTILIZER_NAME,
						RealFarmDatabase.COLUMN_NAME_FERTILIZER_AUDIO,
						RealFarmDatabase.COLUMN_NAME_FERTILIZER_UNITID });

		// user exists in database
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				// adds the users into the list.

				String log = "ID: " + c.getInt(0) + " ,NAME: " + c.getString(1)
						+ " AUDIO: " + c.getInt(2) + " UNITID: " + c.getInt(3)
						+ "\r\n";
				Log.d("fertilizer: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Fertilizer table \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());
		}

		// closes the DB and the cursor.
		// c.close();
		mDatabase.close();

		return c;
	}

	public List<Fertilizing> getfertizing() {

		mDatabase.open();
		int fert = 4;

		List<Fertilizing> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_TYPEOFFERTILIZER,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ fert, null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<Fertilizing>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Fertilizing(c.getInt(0), c.getString(1), c
						.getInt(2), c.getString(3), c.getString(4), c
						.getString(5), c.getInt(6), c.getInt(7), c.getInt(8), c
						.getInt(9), c.getString(10)));

				String log = "action id: " + c.getInt(0) + " ,Action type: "
						+ c.getString(1) + " ,quantity1: " + c.getInt(2)
						+ " ,Type of fertilize: " + c.getString(3) + " ,units"
						+ c.getString(4) + "day " + c.getString(5)
						+ " user id " + c.getInt(6) + " plot id " + c.getInt(7)
						+ "sent  " + c.getInt(8) + " Is admin " + c.getInt(9)
						+ " action performed date " + c.getString(10) + "\r\n";
				Log.d("Fertilizing values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "fertilizing action \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<Harvesting> getharvesting() {

		mDatabase.open();
		int harv = 8;

		List<Harvesting> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_HARVESTFEEDBACK,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ harv, null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<Harvesting>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Harvesting(c.getInt(0), c.getString(1), c
						.getInt(2), c.getInt(3), c.getString(4),
						c.getString(5), c.getInt(6), c.getInt(7), c
								.getString(8), c.getInt(9), c.getInt(10), c
								.getString(11)));

				String log = "action id: " + c.getInt(0) + " ,action type: "
						+ c.getString(1) + " ,quantity1: " + c.getInt(2)
						+ " ,quantity2" + c.getInt(3) + "units "
						+ c.getString(4) + " day " + c.getString(5)
						+ " user  id " + c.getInt(6) + " plot id "
						+ c.getInt(7) + " harvest feedback " + c.getString(8)
						+ "sent  " + c.getInt(9) + " Is admin " + c.getInt(10)
						+ " action performed date " + c.getString(11) + "\r\n";
				;
				Log.d("harvesting values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "harvesting action \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<Irrigation> getirrigate() {

		mDatabase.open();
		int irrigate = 7; // id 7 from actionname table

		List<Irrigation> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
								RealFarmDatabase.COLUMN_NAME_ACTION_IRRIGATE_METHOD },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ irrigate, null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<Irrigation>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Irrigation(c.getInt(0), c.getString(1), c
						.getInt(2), c.getString(3), c.getString(4),
						c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), c
								.getString(9), c.getString(10)));

				String log = "action id: " + c.getInt(0) + " ,Action type: "
						+ c.getString(1) + " ,Action name id: " + irrigate
						+ " ,QUANTITY1: " + c.getInt(2)

						+ " ,units" + c.getString(3) + "day " + c.getString(4)
						+ " user id " + c.getInt(5) + " plot id " + c.getInt(6)
						+ "sent  " + c.getInt(7) + " Is admin " + c.getInt(8)
						+ " action performed date " + c.getString(9)
						+ " method " + c.getString(10) + "\r\n";
				Log.d("Irrigation values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Irrigation action \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<MarketPrice> getMarketPrices() {
		List<MarketPrice> tmpList;

		// opens the database.
		mDatabase.open();
		Log.d("done: ", "in market price");
		// query all actions
		Cursor c = mDatabase.getEntries(
				RealFarmDatabase.TABLE_NAME_MARKETPRICE, new String[] {
						RealFarmDatabase.COLUMN_NAME_MARKETPRICE_ID,
						RealFarmDatabase.COLUMN_NAME_MARKETPRICE_DATE,
						RealFarmDatabase.COLUMN_NAME_MARKETPRICE_TYPE,
						RealFarmDatabase.COLUMN_NAME_MARKETPRICE_VALUE,
						RealFarmDatabase.COLUMN_NAME_MARKETPRICE_ADMINFLAG },
				null, null, null, null, null);
		c.moveToFirst();

		tmpList = new LinkedList<MarketPrice>();

		if (c.getCount() > 0) {
			do {
				tmpList.add(new MarketPrice(c.getInt(0), c.getString(1), c
						.getString(2), c.getInt(3), c.getInt(4)));

				String log = "MP_ID: " + c.getInt(0) + " ,MP_date "
						+ c.getString(1) + " ,MP_TYPE: " + c.getString(2)
						+ " ,MP_value" + c.getInt(3) + "MP_admin flag "
						+ c.getInt(4) + "\r\n";
				Log.d("MP values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "market price table \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());
		}
		Log.d("done: ", "finished MP getdata");
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public Cursor getPesticides() {

		mDatabase.open();

		Cursor c = mDatabase.getAllEntries(
				RealFarmDatabase.TABLE_NAME_PESTICIDE, new String[] {
						RealFarmDatabase.COLUMN_NAME_PESTICIDE_ID,
						RealFarmDatabase.COLUMN_NAME_PESTICIDE_NAME,
						RealFarmDatabase.COLUMN_NAME_PESTICIDE_AUDIO });

		// user exists in database
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				// adds the users into the list.

				String log = "ID: " + c.getInt(0) + " ,NAME: " + c.getString(1)
						+ " AUDIO: " + c.getInt(2) + "\r\n";
				Log.d("pesticides: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Pesticides table \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());
		}

		// closes the DB and the cursor.
		// c.close();
		mDatabase.close();

		return c;
	}

	public List<Plot> getPlotDelete(int delete) {

		mDatabase.open();

		List<Plot> tmpList;

		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				new String[] { RealFarmDatabase.COLUMN_NAME_PLOT_ID,
						RealFarmDatabase.COLUMN_NAME_PLOT_USERID,
						RealFarmDatabase.COLUMN_NAME_PLOT_SEEDTYPEID,
						RealFarmDatabase.COLUMN_NAME_PLOT_IMAGEPATH,
						RealFarmDatabase.COLUMN_NAME_PLOT_SOILTYPE,
						RealFarmDatabase.COLUMN_NAME_PLOT_ADMINFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_TIMESTAMP },
				RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG + "=" + delete,
				null, null, null, null);

		c.moveToFirst();
		tmpList = new LinkedList<Plot>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {

				tmpList.add(new Plot(c.getInt(0), c.getInt(1), c.getInt(2), c
						.getString(3), c.getString(4), delete, c.getInt(5), c
						.getInt(6)));

				String log = "PlotId: " + c.getInt(0) + " ,PlotUserId: "
						+ c.getInt(1) + " ,PlotSeedTypeId: " + c.getInt(2)
						+ "PlotImage: " + c.getString(3) + " ,SoilType: "
						+ c.getString(4)

						+ " ,deleteFlag: " + delete + " ,AdminFlag: "
						+ c.getInt(7) + "\r\n";
				Log.d("plot values: ", log);

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<Plot> getPlots() {

		// opens the database.
		List<Plot> plotList = new LinkedList<Plot>();

		mDatabase.open();

		// query all actions
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				new String[] { RealFarmDatabase.COLUMN_NAME_PLOT_ID,
						RealFarmDatabase.COLUMN_NAME_PLOT_USERID,
						RealFarmDatabase.COLUMN_NAME_PLOT_SEEDTYPEID,
						RealFarmDatabase.COLUMN_NAME_PLOT_IMAGEPATH,
						RealFarmDatabase.COLUMN_NAME_PLOT_SOILTYPE,
						RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_ADMINFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_TIMESTAMP }, null,
				null, null, null, null);
		c.moveToFirst();

		Plot p = null;
		if (c.getCount() > 0) {
			do {
				p = new Plot(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getInt(7));
				plotList.add(p);

				Log.d("plot values: ", p.toString());

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "New plot  table \r\n");
					File_Log_Create("value.txt", p.toString());
				}
			} while (c.moveToNext());
		}

		c.close();
		mDatabase.close();
		return plotList;
	}

	public List<Plot> getPlotsByUserId(int userId) {

		// opens the database.
		List<Plot> plotList = new LinkedList<Plot>();

		mDatabase.open();

		// query all actions
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				new String[] { RealFarmDatabase.COLUMN_NAME_PLOT_ID,
						RealFarmDatabase.COLUMN_NAME_PLOT_USERID,
						RealFarmDatabase.COLUMN_NAME_PLOT_SEEDTYPEID,
						RealFarmDatabase.COLUMN_NAME_PLOT_IMAGEPATH,
						RealFarmDatabase.COLUMN_NAME_PLOT_SOILTYPE,
						RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_ADMINFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_TIMESTAMP },
				RealFarmDatabase.COLUMN_NAME_PLOT_USERID + "=" + userId, null,
				null, null, null);

		c.moveToFirst();

		Plot p = null;
		if (c.getCount() > 0) {
			do {
				p = new Plot(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getInt(7));
				plotList.add(p);

				Log.d("plot values: ", p.toString());
			} while (c.moveToNext());
		}

		c.close();
		mDatabase.close();
		return plotList;
	}

	// modified(You can take seedtypyId corresponding to the userId and plotId)
	public List<Plot> getPlotsByUserIdAndDeleteFlag(int userId, int delete) {

		// opens the database.
		List<Plot> plotList = new LinkedList<Plot>();

		mDatabase.open();

		// query all actions
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				new String[] { RealFarmDatabase.COLUMN_NAME_PLOT_ID,
						RealFarmDatabase.COLUMN_NAME_PLOT_USERID,
						RealFarmDatabase.COLUMN_NAME_PLOT_SEEDTYPEID,
						RealFarmDatabase.COLUMN_NAME_PLOT_IMAGEPATH,
						RealFarmDatabase.COLUMN_NAME_PLOT_SOILTYPE,
						RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_ADMINFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_TIMESTAMP },
				RealFarmDatabase.COLUMN_NAME_PLOT_USERID + "=" + userId
						+ " AND "
						+ RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG + "="
						+ delete + "", null, null, null, null);

		c.moveToFirst();

		Plot p = null;
		if (c.getCount() > 0) {
			do {
				p = new Plot(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getInt(7));
				plotList.add(p);
			} while (c.moveToNext());
		}

		c.close();
		mDatabase.close();
		return plotList;
	}

	// Get WF data

	// modified(You can take seedtypyId corresponding to the userId and plotId)
	public List<Plot> getPlotsByUserIdAndPlotId(int userId, int plotId) {
		// opens the database.
		List<Plot> plotList = new LinkedList<Plot>();

		mDatabase.open();

		// query all actions
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				new String[] { RealFarmDatabase.COLUMN_NAME_PLOT_ID,
						RealFarmDatabase.COLUMN_NAME_PLOT_USERID,
						RealFarmDatabase.COLUMN_NAME_PLOT_SEEDTYPEID,
						RealFarmDatabase.COLUMN_NAME_PLOT_IMAGEPATH,
						RealFarmDatabase.COLUMN_NAME_PLOT_SOILTYPE,
						RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_ADMINFLAG,
						RealFarmDatabase.COLUMN_NAME_PLOT_TIMESTAMP },
				RealFarmDatabase.COLUMN_NAME_PLOT_USERID + "=" + userId
						+ " AND " + RealFarmDatabase.COLUMN_NAME_PLOT_ID + "="
						+ plotId + "", null, null, null, null);

		c.moveToFirst();

		Plot p = null;
		if (c.getCount() > 0) {
			do {
				p = new Plot(c.getInt(0), c.getInt(1), c.getInt(2),
						c.getString(3), c.getString(4), c.getInt(5),
						c.getInt(6), c.getInt(7));
				plotList.add(p);

				Log.d("plot values: ", p.toString());
			} while (c.moveToNext());
		}

		c.close();
		mDatabase.close();
		return plotList;
	}

	public List<Problem> getProblem() {

		mDatabase.open();
		int problem = 6; // id 7 from actionname table for reporting of problems

		List<Problem> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ problem, null, null, null, null);

		c.moveToFirst();
		tmpList = new LinkedList<Problem>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Problem(c.getInt(0), c.getString(1), c
						.getString(2), c.getInt(3), c.getInt(4),
						c.getString(5), c.getInt(6), c.getInt(7), c
								.getString(8)));

				String log = "action id: " + c.getInt(0) + " ,Action type: "
						+ c.getString(1) + " ,Actionnameid: " + problem
						+ "day " + c.getString(2) + " user id " + c.getInt(3)
						+ " plot id " + c.getInt(4) + " Problem type "
						+ c.getString(5) + "sent  " + c.getInt(6)
						+ " Is admin " + c.getInt(7)
						+ " action performed date " + c.getString(8) + "\r\n";
				Log.d("Problem values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Problem action \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	// TODO: should not return a cursor!
	public Cursor getProblems() {

		mDatabase.open();

		Cursor c = mDatabase.getAllEntries(RealFarmDatabase.TABLE_NAME_PROBLEM,
				new String[] { RealFarmDatabase.COLUMN_NAME_PROBLEM_ID,
						RealFarmDatabase.COLUMN_NAME_PROBLEM_NAME,
						RealFarmDatabase.COLUMN_NAME_PROBLEM_AUDIO,
						RealFarmDatabase.COLUMN_NAME_PROBLEM_RESOURCE,
						RealFarmDatabase.COLUMN_NAME_PROBLEM_PROBLEMTYPEID,
						RealFarmDatabase.COLUMN_NAME_PROBLEM_ADMINFLAG });

		// user exists in database
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				// adds the users into the list.

				String log = "ID: " + c.getInt(0) + " ,NAME: " + c.getString(1)
						+ " AUDIO: " + c.getInt(2) + "RESOURCE: " + c.getInt(3)
						+ " PROBLEMTYPEID: " + c.getInt(4) + " ADMINFLAG: "
						+ c.getInt(5) + "\r\n";
				Log.d("problems: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Problems table \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());
		}

		// closes the DB and the cursor.
		// c.close();
		mDatabase.close();

		return c;
	}

	// TODO: should not return a cursor!
	public Cursor getProblemType() {

		mDatabase.open();

		Cursor c = mDatabase.getAllEntries(
				RealFarmDatabase.TABLE_NAME_PROBLEMTYPE, new String[] {
						RealFarmDatabase.COLUMN_NAME_PROBLEMTYPE_ID,
						RealFarmDatabase.COLUMN_NAME_PROBLEMTYPE_NAME,
						RealFarmDatabase.COLUMN_NAME_PROBLEMTYPE_AUDIO,
						RealFarmDatabase.COLUMN_NAME_PROBLEMTYPE_RESOURCE,
						RealFarmDatabase.COLUMN_NAME_PROBLEMTYPE_ADMINFLAG

				});

		// user exists in database
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				// adds the users into the list.

				String log = "ID: " + c.getInt(0) + " ,NAME: " + c.getString(1)
						+ " AUDIO: " + c.getInt(2) + "RESOURCE: " + c.getInt(3)
						+ " ADMINFLAG: " + c.getInt(4) + "\r\n";
				Log.d("problems: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "ProblemType table \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());
		}

		// closes the DB and the cursor.
		// c.close();
		mDatabase.close();

		return c;
	}

	public SeedType getSeedById(int seedId) {

		SeedType res = null;
		mDatabase.open();
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_SEEDTYPE,
				new String[] { RealFarmDatabase.COLUMN_NAME_SEEDTYPE_NAME,
						RealFarmDatabase.COLUMN_NAME_SEEDTYPE_NAMEKANNADA,
						RealFarmDatabase.COLUMN_NAME_SEEDTYPE_RESOURCE,
						RealFarmDatabase.COLUMN_NAME_SEEDTYPE_AUDIO,
						RealFarmDatabase.COLUMN_NAME_SEEDTYPE_VARIETY,
						RealFarmDatabase.COLUMN_NAME_SEEDTYPE_VARIETYKANNADA,
						RealFarmDatabase.COLUMN_NAME_SEEDTYPE_RESOURCE_BG },
				RealFarmDatabase.COLUMN_NAME_SEEDTYPE_ID + "=" + seedId, null,
				null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			res = new SeedType(seedId, c.getString(0), c.getString(1),
					c.getInt(2), c.getInt(3), c.getString(4), c.getString(5),
					c.getInt(6));
		}
		c.close();
		mDatabase.close();
		return res;

	}

	public List<SeedType> getSeeds() {

		// seeds are not in cache
		if (mAllSeeds == null) {

			mAllSeeds = new ArrayList<SeedType>();
			mDatabase.open();

			Cursor c = mDatabase
					.getAllEntries(
							RealFarmDatabase.TABLE_NAME_SEEDTYPE,
							new String[] {
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_ID,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_NAME,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_NAMEKANNADA,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_RESOURCE,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_AUDIO,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_VARIETY,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_VARIETYKANNADA,
									RealFarmDatabase.COLUMN_NAME_SEEDTYPE_RESOURCE_BG });

			if (c.getCount() > 0) {
				c.moveToFirst();
				do {
					SeedType s = new SeedType(c.getInt(0), c.getString(1),
							c.getString(2), c.getInt(3), c.getInt(4),
							c.getString(5), c.getString(6), c.getInt(7));
					mAllSeeds.add(s);

					Log.d("seed type: ", s.toString());

					if (Global.writeToSD == true) {
						File_Log_Create("value.txt", "seed type table \r\n");
						File_Log_Create("value.txt", s.toString());
					}
				} while (c.moveToNext());

			}
			c.close();
			mDatabase.close();
		}

		return mAllSeeds;
	}

	public List<Selling> getselling() {

		mDatabase.open();
		int sell = 12;

		List<Selling> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLINGPRICE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUALITYOFSEED,
								RealFarmDatabase.COLUMN_NAME_ACTION_SELLTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ sell, null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<Selling>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Selling(c.getInt(0), c.getString(1), c
						.getInt(2), c.getInt(3), c.getString(4),
						c.getString(5), c.getInt(6), c.getInt(7), c.getInt(8),
						c.getString(9), c.getString(10), c.getInt(11), c
								.getInt(12), c.getString(13)));

				String log = "action id: " + c.getInt(0) + " ,action type: "
						+ c.getString(1) + " ,quantity1: " + c.getInt(2)
						+ " ,quantity2: " + c.getInt(3) + " ,units"
						+ c.getString(4) + "day " + c.getString(5)
						+ " user id " + c.getInt(6) + " plot id " + c.getInt(7)
						+ " selling price " + c.getInt(8) + " quality of seed "
						+ c.getString(9) + " selling type " + c.getString(10)
						+ "sent  " + c.getInt(11) + " Is admin " + c.getInt(12)
						+ " action performed date " + c.getString(13) + "\r\n";
				Log.d("selling values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "selling a \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<Sowing> getsowing() {

		mDatabase.open();
		int sow = 3;

		List<Sowing> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_SEEDVARIETY,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
								RealFarmDatabase.COLUMN_NAME_ACTION_TREATMENT },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ sow, null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<Sowing>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Sowing(c.getInt(0), c.getString(1),
						c.getInt(2), c.getString(3), c.getString(4), c
								.getString(5), c.getInt(6), c.getInt(7), c
								.getInt(8), c.getInt(9), c.getString(10), c
								.getString(11)));

				String log = "action id: " + c.getInt(0) + " ,Action type: "
						+ c.getString(1) + " ,QUANTITY1: " + c.getInt(2)
						+ " ,seed variety: " + c.getString(3) + " ,units"
						+ c.getString(4) + "day " + c.getString(5)
						+ " user id " + c.getInt(6) + " plot id " + c.getInt(7)
						+ "sent  " + c.getInt(8) + " Is admin " + c.getInt(9)
						+ " action performed date " + c.getString(10)
						+ " treatment " + c.getString(11) + "\r\n";
				Log.d("sowing values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Sowing action \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<Spraying> getspraying() {

		mDatabase.open();
		int spray = 5;

		List<Spraying> tmpList;

		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_ACTION,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_ACTION_ID,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1,
								RealFarmDatabase.COLUMN_NAME_ACTION_UNITS,
								RealFarmDatabase.COLUMN_NAME_ACTION_DAY,
								RealFarmDatabase.COLUMN_NAME_ACTION_USERID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID,
								RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE,
								RealFarmDatabase.COLUMN_NAME_ACTION_SENT,
								RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN,
								RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
								RealFarmDatabase.COLUMN_NAME_ACTION_PESTICIDETYPE },
						RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID + "="
								+ spray, null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<Spraying>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new Spraying(c.getInt(0), c.getString(1), c
						.getInt(2), c.getString(3), c.getString(4),
						c.getInt(5), c.getInt(6), c.getString(7), c.getInt(8),
						c.getInt(9), c.getString(10), c.getString(11)));

				String log = "action id: " + c.getInt(0) + " ,action type"
						+ c.getString(1) + " ,quantity1" + c.getString(2)

						+ " ,units" + c.getString(3) + "day " + c.getString(4)
						+ " user id " + c.getInt(5) + " plot id " + c.getInt(6)
						+ " ,Problem type" + c.getString(7) + "sent  "
						+ c.getInt(8) + " Is admin " + c.getInt(9)
						+ " action performed date " + c.getString(10)
						+ " Pesticide type " + c.getString(11) + "\r\n";
				Log.d("spraying values: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "spraying action \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	// TODO: incorrect name!
	public void getUnit() {

		mDatabase.open();

		Cursor c = mDatabase.getAllEntries(RealFarmDatabase.TABLE_NAME_UNIT,
				new String[] { RealFarmDatabase.COLUMN_NAME_UNIT_ID,
						RealFarmDatabase.COLUMN_NAME_UNIT_NAME,
						RealFarmDatabase.COLUMN_NAME_UNIT_AUDIO });

		// user exists in database
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				// adds the users into the list.

				String log = "UNIT_ID: " + c.getInt(0) + " ,UNIT_NAME: "
						+ c.getString(1) + " ,UNIT_AUDIO: " + c.getInt(2)
						+ "\r\n";

				Log.d("Unit: ", log);

				if (Global.writeToSD == true) {
					File_Log_Create("value.txt", "Unit table \r\n");
					File_Log_Create("value.txt", log);
				}

			} while (c.moveToNext());
		}

		// closes the DB and the cursor.
		c.close();
		mDatabase.close();

	}

	public User getUserById(int userId) {
		mDatabase.open();
		User tmpUser = null;

		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_USER,
				new String[] { RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_LASTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_MOBILE,
						RealFarmDatabase.COLUMN_NAME_USER_IMAGE,
						RealFarmDatabase.COLUMN_NAME_USER_DELETEFLAG,
						RealFarmDatabase.COLUMN_NAME_USER_ADMINFLAG },
				RealFarmDatabase.COLUMN_NAME_USER_ID + " = " + userId, null,
				null, null, null);

		// user exists in database
		if (c.getCount() > 0) {
			c.moveToFirst();

			// tmpUser = new User(userId, c.getString(0), c.getString(1),
			// c.getString(2), c.getString(3));

			tmpUser = new User(userId, c.getString(0), c.getString(1),
					c.getString(2), c.getString(3), c.getInt(4), c.getInt(5));
		}

		c.close();
		mDatabase.close();

		return tmpUser;

	}

	public User getUserByMobile(String deviceId) {

		mDatabase.open();

		User tmpUser = null;
		String mobile;

		if (deviceId == null)
			mobile = RealFarmDatabase.DEFAULT_NUMBER;
		else
			mobile = deviceId;

		Cursor c = mDatabase
				.getEntries(RealFarmDatabase.TABLE_NAME_USER, new String[] {
						RealFarmDatabase.COLUMN_NAME_USER_ID,
						RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_LASTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_IMAGE,
						RealFarmDatabase.COLUMN_NAME_USER_ADMINFLAG },

						RealFarmDatabase.COLUMN_NAME_USER_MOBILE + "= '"
								+ mobile + "'", null, null, null, null);

		if (c.getCount() > 0) { // user exists in database
			c.moveToFirst();

			// tmpUser = new User(c.getInt(0), c.getString(1), c.getString(2),
			// mobile, c.getString(3));

			tmpUser = new User(c.getInt(0), c.getString(1), c.getString(2),
					mobile, c.getString(3), c.getInt(4), c.getInt(5));
		}
		c.close();
		mDatabase.close();

		return tmpUser;
	}

	/**
	 * 
	 * @return integer number of users in the DB
	 */
	public int getUserCount() {
		mDatabase.open();
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_USER,
				new String[] { RealFarmDatabase.COLUMN_NAME_USER_ID }, null,
				null, null, null, null);
		int userCount = c.getCount();
		c.close();
		mDatabase.close();
		return userCount;
	}

	public List<User> getUserDelete(int delete) {

		mDatabase.open();
		// int delete=0;

		System.out.println("In getuserDelete");

		List<User> tmpList;

		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_USER,
				new String[] { RealFarmDatabase.COLUMN_NAME_USER_ID,
						RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_LASTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_MOBILE,
						RealFarmDatabase.COLUMN_NAME_USER_IMAGE,
						RealFarmDatabase.COLUMN_NAME_USER_ADMINFLAG },
				RealFarmDatabase.COLUMN_NAME_USER_DELETEFLAG + "=" + delete,
				null, null, null, null);

		//
		c.moveToFirst();
		tmpList = new LinkedList<User>();
		System.out.println(c.getCount());

		if (c.getCount() > 0) {
			do {
				tmpList.add(new User(c.getInt(0), c.getString(1), c
						.getString(2), c.getString(3), c.getString(4), delete,
						c.getInt(5)));

				String log = "user id: " + c.getInt(0) + " ,First name: "
						+ c.getString(1) + " ,Last name: " + c.getString(2)
						+ " ,Mobile: " + c.getString(3) + " ,Image"
						+ c.getString(4) + " delete flag " + delete
						+ "Admin flag " + c.getInt(5) + "\r\n";
				Log.d("sowing values: ", log);

			} while (c.moveToNext());

		}
		c.close();
		mDatabase.close();

		return tmpList;
	}

	public List<User> getUsers() {

		// opens the database.
		List<User> userList = new LinkedList<User>();

		mDatabase.open();

		// query all actions
		Cursor c = mDatabase.getEntries(RealFarmDatabase.TABLE_NAME_USER,
				new String[] { RealFarmDatabase.COLUMN_NAME_USER_ID,
						RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_LASTNAME,
						RealFarmDatabase.COLUMN_NAME_USER_MOBILE,
						RealFarmDatabase.COLUMN_NAME_USER_IMAGE,
						RealFarmDatabase.COLUMN_NAME_USER_DELETEFLAG,
						RealFarmDatabase.COLUMN_NAME_USER_ADMINFLAG }, null,
				null, null, null, null);
		c.moveToFirst();

		if (c.getCount() > 0) {
			do {
				userList.add(new User(c.getInt(0), c.getString(1), c
						.getString(2), c.getString(3), c.getString(4), c
						.getInt(5), c.getInt(6)));

				String log = "Id: " + c.getString(0) + " ,FirstName: "
						+ c.getString(1) + " ,LastName: " + c.getString(2)
						+ "Mobile: " + c.getString(3) + " ,Img: "
						+ c.getString(4) + " ,DeleteFlag: " + c.getInt(5)
						+ " ,AdminFlag: " + c.getInt(6) + "\r\n";
				Log.d("user: ", log);

				if (Global.writeToSD == true) {

					File_Log_Create("value.txt", "User table \r\n");
					File_Log_Create("value.txt", log);
				}
			} while (c.moveToNext());
		}

		c.close();
		mDatabase.close();
		return userList;
	}

	public List<WeatherForecast> getWeatherForecasts() {
		List<WeatherForecast> tmpList;

		// opens the database.
		mDatabase.open();
		Log.d("done: ", "in Wf getdata");
		// query all actions
		Cursor c = mDatabase
				.getEntries(
						RealFarmDatabase.TABLE_NAME_WEATHERFORECAST,
						new String[] {
								RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_ID,
								RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_DATE,
								RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_TEMPERATURE,
								RealFarmDatabase.COLUMN_NAME_WEATHERFORECAST_TYPE },
						null, null, null, null, null);
		c.moveToFirst();

		tmpList = new LinkedList<WeatherForecast>();

		if (c.getCount() > 0) {
			do {
				tmpList.add(new WeatherForecast(c.getInt(0), c.getString(1), c
						.getInt(2), c.getString(3)));

				String log = "WF_ID: " + c.getInt(0) + " , WF_date "
						+ c.getString(1) + " , WF_temperature: " + c.getInt(2)
						+ " , WF_type" + c.getString(3);
				Log.d("WF values: ", log);

			} while (c.moveToNext());
		}
		Log.d("done: ", "finished Wf getdata");
		c.close();
		mDatabase.close();

		return tmpList;
	}

	// main crop info corresponds to seed type id
	public long insertPlot(int userId, int seedTypeId, String imagePath,
			String soilType, int delete, int admin) {

		// increases the current plot id
		System.out.println("SETPLOTNEW");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_SEEDTYPEID, seedTypeId);
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_IMAGEPATH, imagePath);
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_SOILTYPE, soilType);
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG, delete);
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_ADMINFLAG, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_TIMESTAMP, admin);

		// public static final String COLUMN_NAME_PLOT_CENTERX = "centerX";
		// public static final String COLUMN_NAME_PLOT_CENTERY = "centerY";
		// public static final String COLUMN_NAME_PLOT_DATE = "date";
		// public static final String COLUMN_NAME_PLOT_SEEDTYPEID =
		// "seedtypeId";
		mDatabase.open();

		long result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				args);

		mDatabase.close();

		return result;
	}

	public void Log_Database_backupdate() {
		File_Log_Create("value.txt",
				"/******************************************************/");
		File_Log_Create("value.txt",
				"Database backup date for the following tables: \r\n");

		String dbEntryDate = mDateFormat.format(mCalendar.getTime());
		File_Log_Create("value.txt", dbEntryDate);
		File_Log_Create("value.txt",
				"/******************************************************/");
	}

	public long removeAction(int id) {
		mDatabase.open();

		long result = mDatabase.deleteEntriesdb(
				RealFarmDatabase.TABLE_NAME_ACTION,
				RealFarmDatabase.COLUMN_NAME_ACTION_ID + "=" + id, null);

		mDatabase.close();
		return result;
	}

	public long setAction(int actionid, int actionnameid, int growingid,
			String actiontype, String Seedvariety, int quantity1,
			int quantity2, String Units, String day, int userid, int plotid,
			String TypeFert, String Prob, String feedback, int sp,
			String QuaSeed, String type, int i, int j, String treat,
			String pesttype) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();
		// calendar.add(Calendar.DATE, -15);
		System.out.println("SETACTIONNEW");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, actionnameid);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_GROWINGID, growingid);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, actiontype);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SEEDVARIETY, Seedvariety);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, quantity1);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2, quantity2);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, Units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userid);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotid);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_TYPEOFFERTILIZER, TypeFert);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE, Prob);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_HARVESTFEEDBACK, feedback);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SELLINGPRICE, sp);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUALITYOFSEED, QuaSeed);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SELLTYPE, type);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, i);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, j);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_TREATMENT, treat);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PESTICIDETYPE, pesttype);

		long result;

		// User user = getUserByMobile(deviceId);

		mDatabase.open();

		// if (user != null) { // user exists in database => update
		// result = mDb.update(RealFarmDatabase.TABLE_NAME_USER, args,
		// RealFarmDatabase.COLUMN_NAME_USER_MOBILE + " = '"
		// + deviceId + "'", null);
		// } else { // user must be created
		// result = mDb
		// .update(RealFarmDatabase.TABLE_NAME_PLOT, args,null,null);

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);
		// }

		mDatabase.close();

		return result;
	}

	public long setDeleteFlagForPlot(int plotId) {

		System.out.println("in setDeleteFlagForPlot ");
		ContentValues args = new ContentValues();
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_DELETEFLAG, 1);

		long result;

		mDatabase.open();

		result = mDatabase.update(RealFarmDatabase.TABLE_NAME_PLOT, args,
				RealFarmDatabase.COLUMN_NAME_PLOT_ID + " = '" + plotId + "'",
				null);
		// result = mDb.update(RealFarmDatabase.TABLE_NAME_USER, args,
		// RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME + " = '"
		// + firstname + " AND " + RealFarmDatabase.COLUMN_NAME_USER_LASTNAME +
		// " = '"
		// + lastname , null);

		mDatabase.close();
		System.out.println(result);
		return result;
	}

	public long setDeleteFlagForUser(int userid) {

		System.out.println("in setDeleteFlagForUser ");
		ContentValues args = new ContentValues();
		args.put(RealFarmDatabase.COLUMN_NAME_USER_DELETEFLAG, 1);

		long result;

		mDatabase.open();

		result = mDatabase.update(RealFarmDatabase.TABLE_NAME_USER, args,
				RealFarmDatabase.COLUMN_NAME_USER_ID + " = '" + userid + "'",
				null);
		// result = mDb.update(RealFarmDatabase.TABLE_NAME_USER, args,
		// RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME + " = '"
		// + firstname + " AND " + RealFarmDatabase.COLUMN_NAME_USER_LASTNAME +
		// " = '"
		// + lastname , null);

		mDatabase.close();
		System.out.println(result);
		return result;
	}

	public long setFertilizing(int userId, long plotId, int qua1,
			String TypeofFert, String Units, String day, int sent, int admin) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET fertilizing");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 4);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Fertilizing");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, qua1);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_TYPEOFFERTILIZER,
				TypeofFert);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, Units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	// day takes the date
	public long setHarvest(int userId, long plotId, int qua1, int qua2,
			String Units, String day, String harvfeedback, int sent, int admin) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET harvest");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 8);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Harvesting");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, qua1);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2, qua2);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, Units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_HARVESTFEEDBACK,
				harvfeedback);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	// qua1 mapped to no of hours
	public long setIrrigation(int userId, long plotId, int qua1, String Units,
			String day, int sent, int admin, String method) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET IRRIGATION");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 7);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Irrigate");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, qua1);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, Units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_IRRIGATE_METHOD, method);

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	public long setMarketPrice(int id, String date, String type, int value,
			int adminflag) {

		ContentValues args = new ContentValues();
		args.put(RealFarmDatabase.COLUMN_NAME_MARKETPRICE_ID, id);
		args.put(RealFarmDatabase.COLUMN_NAME_MARKETPRICE_DATE, date);
		args.put(RealFarmDatabase.COLUMN_NAME_MARKETPRICE_TYPE, type);
		args.put(RealFarmDatabase.COLUMN_NAME_MARKETPRICE_VALUE, value);
		args.put(RealFarmDatabase.COLUMN_NAME_MARKETPRICE_ADMINFLAG, adminflag);

		mDatabase.open();

		long result = mDatabase.insertEntries(
				RealFarmDatabase.TABLE_NAME_MARKETPRICE, args);

		mDatabase.close();

		return result;
	}

	public long setPlot(int userID) {

		ContentValues args = new ContentValues();
		args.put(RealFarmDatabase.COLUMN_NAME_PLOT_USERID, userID);

		mDatabase.open();

		// add to plot list
		long result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_PLOT,
				args);

		mDatabase.close();
		return result;

	}

	public long setProblem(int userId, long plotId, String day,
			String probType, int sent, int admin) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET PROBLEM");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 6);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Problem");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE, probType);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	// day takes the date
	public long setselling(int userId, int plotId, int qua1, int qua2,
			String Units, String day, int sellingprice, String QuaOfSeed,
			String selltype, int sent, int admin) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET selling");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 12);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Selling");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, qua1);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY2, qua2);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, Units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SELLINGPRICE, sellingprice);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUALITYOFSEED, QuaOfSeed);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SELLTYPE, selltype);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	public long setSowing(int userId, long plotId, int quantity,
			String seedVariety, String units, String day, String treat,
			int sent, int admin) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET SOWING");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 3);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Sowing");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, quantity);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SEEDVARIETY, seedVariety);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_TREATMENT, treat);

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	public long setSpraying(int userId, long plotId, int quantity1,
			String Units, String day, String probtype, int sent, int admin,
			String pesttype) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		Calendar calendar = Calendar.getInstance();

		System.out.println("SET spraying");
		ContentValues args = new ContentValues();

		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONTYPE, "Spraying");
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONNAMEID, 5);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_QUANTITY1, quantity1);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_UNITS, Units);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_DAY, day);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_USERID, userId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PLOTID, plotId);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PROBLEMTYPE, probtype);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_SENT, sent);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ISADMIN, admin);
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_ACTIONPERFORMEDDATE,
				dateFormat.format(calendar.getTime()));
		args.put(RealFarmDatabase.COLUMN_NAME_ACTION_PESTICIDETYPE, pesttype);

		long result;

		mDatabase.open();

		result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_ACTION,
				args);

		mDatabase.close();

		return result;
	}

	public long setUserInfo(String deviceId, String firstname, String lastname) {

		getUserCount();
		ContentValues args = new ContentValues();
		args.put(RealFarmDatabase.COLUMN_NAME_USER_ID, (getUserCount()) + 1);
		args.put(RealFarmDatabase.COLUMN_NAME_USER_MOBILE, deviceId);
		args.put(RealFarmDatabase.COLUMN_NAME_USER_FIRSTNAME, firstname);
		args.put(RealFarmDatabase.COLUMN_NAME_USER_LASTNAME, lastname);
		args.put(RealFarmDatabase.COLUMN_NAME_USER_DELETEFLAG, 0);
		args.put(RealFarmDatabase.COLUMN_NAME_USER_ADMINFLAG, 0);

		long result;

		User user = getUserByMobile(deviceId);

		mDatabase.open();

		if (user != null) { // user exists in database => update
			result = mDatabase.update(RealFarmDatabase.TABLE_NAME_USER, args,
					RealFarmDatabase.COLUMN_NAME_USER_MOBILE + " = '"
							+ deviceId + "'", null);
		} else { // user must be created
			result = mDatabase.insertEntries(RealFarmDatabase.TABLE_NAME_USER,
					args);
		}

		// if main id is undefined and result is good
		if ((result > 0) && (RealFarmDatabase.MAIN_USER_ID == -1))
			RealFarmDatabase.MAIN_USER_ID = (int) result;

		mDatabase.close();

		return result;
	}

	public void setWeatherForecastDataChangeListener(
			OnDataChangeListener listener) {
		sWeatherForecastDataListener = listener;
	}
}
