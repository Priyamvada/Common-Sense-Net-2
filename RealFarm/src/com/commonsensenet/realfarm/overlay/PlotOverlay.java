package com.commonsensenet.realfarm.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import com.commonsensenet.realfarm.map.OfflineMapView;
import com.commonsensenet.realfarm.map.Overlay;
import com.commonsensenet.realfarm.model.Plot;

public class PlotOverlay extends Overlay {

	/** Polygon that represents the overlay. */
	private Plot mPlot;

	/**
	 * Creates a new Plot2Overlay instance.
	 * 
	 * @param polygon
	 *            a Polygon object to be drawn.
	 */
	public PlotOverlay(Plot plot) {
		mPlot = plot;
	}

	@Override
	public void draw(Canvas canvas, OfflineMapView offlineMapView) {
		super.draw(canvas, offlineMapView);

		// defines the style of the overlay
		Paint paint = new Paint();
		paint.setStrokeWidth(7);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStrokeWidth(3);

		// loads the points that need to be drawn.
		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);

		// selects the color based on the owner of the plot
		paint.setARGB(100, 55, 175, 35);
		if (mPlot.getOwnerId() == 1)
			paint.setARGB(100, 228, 29, 29);

		// gets the points
		Point[] points = mPlot.getPoints(offlineMapView);

		if (points.length > 0) {
			// move for the first point
			path.moveTo(points[0].x, points[0].y);

			// adds the rest of the lines.
			for (int i = 1; i < points.length; i++) {
				path.lineTo(points[i].x, points[i].y);
			}
		}

		// closes the path.
		path.close();

		// Change paint style depending on user
		PathShape pShape = new PathShape(path, (float) 100, (float) 100);
		ShapeDrawable mShape = new ShapeDrawable(pShape);
		mShape.getPaint().set(paint);
		mShape.setBounds(0, 0, 100, 100);

		mShape.draw(canvas);

	}
}
