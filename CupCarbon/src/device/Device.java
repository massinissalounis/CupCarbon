/*----------------------------------------------------------------------------------------------------------------
 * CupCarbon: OSM based Wireless Sensor Network design and simulation tool
 * www.cupcarbon.com
 * ----------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2013 Ahcene Bounceur
 * ----------------------------------------------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *----------------------------------------------------------------------------------------------------------------*/

package device;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import map.Layer;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import script.Script;
import utilities.MapCalc;
import utilities.UColor;
import utilities._Constantes;
import wisen_simulation2.DeviceSimulator;
import wisen_simulation2.Simulation;
import battery.Battery;
import cupcarbon.DeviceParametersWindow;
import cupcarbon.Parameters;

/**
 * @author Ahcene Bounceur
 * @author Kamal Mehdi
 * @author Lounis Massinissa
 * @version 1.0
 */
public abstract class Device implements Runnable, MouseListener,
		MouseMotionListener, KeyListener, _Constantes, Cloneable {

	public static final int SENSOR = 1;
	public static final int GAS = 2;
	public static final int FLYING_OBJECT = 3;
	public static final int BASE_STATION = 4;
	public static final int BRIDGE = 5;
	public static final int MOBILE = 6;
	public static final int MOBILE_WR = 7;
	public static final int MARKER = 8;
	public static final int VERTEX = 9;

	public static final boolean DEAD = false;
	public static final boolean ALIVE = true;

	public static int moveSpeed = 100;

	protected boolean altDown = false;
	protected boolean shiftDown = false;
	protected boolean cmdDown = false;
	protected boolean ctrlDown = false;
	protected int lastKeyCode = 0;

	public static int number = 0;

	protected int id = 0;
	protected String userId = "";

	protected double x, y;
	protected double xori;
	protected double yori;
	protected double dx, dy;
	protected double radius = 0;
	protected double radiusOri = 0;
	protected boolean selected = false;
	protected char key = 0;
	protected boolean move = false;
	protected boolean inside = false;
	protected static boolean displayDetails = false;
	protected boolean underSimulation = false;
	protected int hide = 0;
	protected boolean increaseNode = false;
	protected boolean reduceNode = false;
	protected boolean withRadio = false;
	protected boolean withSensor = false;
	protected boolean mobile = false;
	protected boolean detection = false;
	protected boolean displayRadius = false;
	protected boolean displayDistance = false;
	protected boolean selectedByAlgo = false;
	protected boolean visible = true;
	protected String[][] infos;
	protected static boolean displayInfos = false;

	protected DeviceSimulator deviceSimulator = null;
	protected Simulation simulation = null;
	protected String scriptFileName = "";
	protected String gpsFileName = "";
	protected Script script = null;
	protected int channel = 0;
	
	public static int dataRate = 0;
	

	protected boolean state = ALIVE;

	protected Thread thread;

	/**
	 * Empty constructor
	 */
	public Device() {
	}

	/**
	 * Constructor with the 3 main parameters of a device
	 * 
	 * @param x
	 *            Longitude
	 * @param y
	 *            Latitude
	 * @param radius
	 *            Radius
	 */
	public Device(double x, double y, double radius) {
		id = number++;
		userId = "S" + id;
		this.x = x;
		this.y = y;
		this.radius = radius;
		radiusOri = radius;
		Layer.getMapViewer().addMouseListener(this);
		Layer.getMapViewer().addMouseMotionListener(this);
		Layer.getMapViewer().addKeyListener(this);
	}

	/**
	 * ID First Letter
	 * 
	 * @return the first letter of the identifier
	 */
	public abstract String getIdFL();

	/**
	 * @return the name of the identifier
	 */
	public abstract String getNodeIdName();

	/**
	 * @return the type of the device
	 */
	public abstract int getType();

	/**
	 * Draw the device
	 * 
	 * @param g
	 *            Graphics
	 */
	public abstract void draw(Graphics g);

	/**
	 * Set the radio radius of the device
	 * 
	 * @param radiuRadius
	 */
	public abstract void setRadioRadius(double radiuRadius);

	/**
	 * Set the capture unit radius of the device
	 * 
	 * @param captureRadius
	 */
	public abstract void setCaptureRadius(double captureRadius);

	/**
	 * Set the GPS file path of the device
	 * 
	 * @param gpsFileName
	 */
	public void setGPSFileName(String gpsFileName) {
		if(gpsFileName.endsWith(".gps"))
			this.gpsFileName = gpsFileName;
		else
			this.gpsFileName = "";
	}

	/**
	 * @return the GPS file path
	 */
	public abstract String getGPSFileName();

	/**
	 * This method draw the sensor with another look in order to view if an
	 * algorithm has selected them or not
	 * 
	 * @param g
	 *            Graphics
	 */
	public void drawSelectedByAlgo(Graphics g) {
	}

	/**
	 * Consume v units
	 * 
	 * @param v
	 */
	public void consume(double v) {
	}

	/**
	 * @return the id of the device
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id of the device
	 * 
	 * @param id
	 *            Identifier
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the user ID
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Set the user ID
	 * 
	 * @param userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Set a path and name for the script file
	 * 
	 * @param scriptFileName
	 */
	public void setScriptFileName(String scriptFileName) {
		this.scriptFileName = scriptFileName;
		deviceSimulator.setScriptFile(scriptFileName);
	}

	/**
	 * @return the path of the communication script file
	 */
	public String getScriptFileName() {
		return scriptFileName;
	}

	/**
	 * Initialize the simulation
	 * 
	 * @param simulation
	 */
	public void initSimulator(Simulation simulation) {
		this.simulation = simulation;
		deviceSimulator.init(simulation);
	}

	/**
	 * Set the informations of a device
	 * 
	 * @param infos
	 */
	public void setInfos(String[][] infos) {
		this.infos = infos;
	}

	/**
	 * @return the informations about the device
	 */
	public String[][] getInfos() {
		return infos;
	}

	/**
	 * An algorithm can select or not a device
	 * 
	 * @param b
	 *            Yes/No (to select the device by algorithms)
	 */
	public void setAlgoSelect(boolean b) {
		this.selectedByAlgo = b;
	}

	/**
	 * @return the capture unit radius
	 */
	public double getCaptureUnitRadius() {
		return 0.0;
	}

	/**
	 * @return the radio radius
	 */
	public double getRadioRadius() {
		return 0.0;
	}

	/**
	 * Set the detection
	 * 
	 * @param detection
	 *            Yes/No (detect or not the an event)
	 */
	public void setDetection(boolean detection) {
		this.detection = detection;
	}

	/**
	 * @return if the device is with or without radio
	 */
	public boolean withRadio() {
		return withRadio;
	}

	/**
	 * Set the selection (select or not the device)
	 * 
	 * @param selection
	 */
	public void setSelection(boolean selection) {
		this.selected = selection;
	}

	/**
	 * Invert selection: Select devices that are not selected and de-select
	 * devices that are selected
	 */
	public void invSelection() {
		selected = !selected;
	}

	/**
	 * @return the radius of the device
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Set the radius of the device
	 * 
	 * @param radius
	 *            Radius of the device
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * @return the longitude
	 */
	public double getX() {
		return x;
	}

	/**
	 * Set the longitude
	 * 
	 * @param x
	 *            Longitude
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the latitude
	 */
	public double getY() {
		return y;
	}

	/**
	 * Set the latitude
	 * 
	 * @param y
	 *            Latitude
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Set if it is possible to move or not the device
	 * 
	 * @param b
	 *            Move or not
	 */
	public void setMove(boolean b) {
		move = b;
	}

	/**
	 * @return the value of the radio in meter from a value given in pixels
	 */
	public double convertRadius() {
		return 40075017.0 * Math.cos(y)
				/ Math.pow(2, (Layer.getMapViewer().getZoom() + 8));
	}

	/**
	 * Set the attribute inside to true or false depending the the point (xs,ys)
	 * if it is inside the device or not
	 * 
	 * @param xs
	 *            Longitude
	 * @param ys
	 *            Latitude
	 */
	public void inside(int xs, int ys) {
		Point p = new Point(xs, ys);
		GeoPosition gp = Layer.getMapViewer().convertPointToGeoPosition(p);
		Point2D p1 = Layer.getMapViewer().getTileFactory()
				.geoToPixel(gp, Layer.getMapViewer().getZoom());
		GeoPosition gp2 = new GeoPosition(x, y);
		Point2D p2 = Layer.getMapViewer().getTileFactory()
				.geoToPixel(gp2, Layer.getMapViewer().getZoom());
		double d1 = p1.getX() - p2.getX();
		double d2 = p1.getY() - p2.getY();
		inside = false;
		double v = Math.sqrt(d1 * d1 + d2 * d2);
		if (v < MapCalc.radiusInPixels(getMaxRadius())) {
			inside = true;
		}
	}

	/**
	 * @return radius
	 */
	public double getMaxRadius() {
		return radius;
	}

	/**
	 * @param device
	 *            A device
	 * @return the distance in pixels between the current device and the one
	 *         given as a parameter
	 */
	public double distanceInPixel(Device device) {
		double x2 = device.getX();
		double y2 = device.getY();
		return MapCalc.distanceEnPixels(x, y, x2, y2);
	}

	/**
	 * @param device
	 * @return the distance in meters between the current device and the one
	 *         given as a parameter
	 */
	public double distance(Device device) {
		double x2 = device.getX();
		double y2 = device.getY();
		return MapCalc.distance(x, y, x2, y2);
	}

	/**
	 * @param device
	 * @return the horizontal distance in meters between the current design and
	 *         the one given as a parameter
	 */
	public double distanceX(Device device) {
		double x2 = device.getX();
		return MapCalc.distance(x, y, x2, y);
	}

	/**
	 * @param device
	 * @return the vertical distance in meters between the current design and
	 *         the one given as a parameter
	 */
	public double distanceY(Device device) {
		double y2 = device.getY();
		return MapCalc.distance(x, y, x, y2);
	}

	/**
	 * @return if the device is selected or not
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Update the window of the node parameters
	 */
	public void sensorParametersUpdate() {
		if (selected) {
			DeviceParametersWindow.textField_5.setText("" + x);
			DeviceParametersWindow.textField_6.setText("" + y);
			DeviceParametersWindow.textField_7.setText("" + getRadius());
			DeviceParametersWindow.textField_8.setText("" + getRadioRadius());
			DeviceParametersWindow.textField_9.setText(""
					+ getCaptureUnitRadius());

			String[] gpsFile = getGPSFileName()
					.split(Parameters.SEPARATOR + "");
			String gpsFileName = gpsFile[gpsFile.length - 1];
			//if (!gpsFileName.equals(""))
			DeviceParametersWindow.gpsPathNameComboBox.setSelectedItem(gpsFileName);

			String[] scriptFile = getScriptFileName().split(Parameters.SEPARATOR + "");
			String scriptFileName = scriptFile[scriptFile.length - 1];
			//if (!scriptFileName.equals(""))
			DeviceParametersWindow.scriptComboBox.setSelectedItem(scriptFileName);
		}
	}

	/**
	 * Calculate the shift between the location of the device and the location
	 * of the mouse cursor
	 * 
	 * @param evx
	 *            Longitude of the mouse cursor
	 * @param evy
	 *            Latitude of the mouse cursor
	 */
	public void calculateDxDy(int evx, int evy) {
		Point p = new Point(evx, evy);
		GeoPosition gp = Layer.getMapViewer().convertPointToGeoPosition(p);
		double ex = gp.getLatitude();
		double ey = gp.getLongitude();
		dx = ex - x;
		dy = ey - y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (!inside && !ctrlDown) {
			selected = false;
			Layer.getMapViewer().repaint();
		}

		if (inside) {
			selected = !selected;
			Layer.getMapViewer().repaint();
		}

		increaseNode = false;
		reduceNode = false;
		sensorParametersUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		calculateDxDy(e.getX(), e.getY());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent key) {
		lastKeyCode = key.getKeyCode();
		if (lastKeyCode == 27) {
			initSelection();
			Layer.getMapViewer().repaint();
		}
		if (key.isShiftDown())
			shiftDown = true;
		if (key.isAltDown())
			altDown = true;
		if (key.isControlDown())
			ctrlDown = true;
		if (key.isMetaDown())
			cmdDown = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent arg0) {
		altDown = false;
		shiftDown = false;
		ctrlDown = false;
		cmdDown = false;
	}

	/**
	 * Set the ID number
	 */
	public void setId() {
		id = ++number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(KeyEvent e) {

		key = e.getKeyChar();

		if (selected) {
			sensorParametersUpdate();
			if (key == 'c') {
				try {
					// Layer.addNode(this.clone());
					DeviceList.add(this.clone());
					move = false;
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
			}

			if (key == 'k') {
				visible = !visible;
			}

			if (key == 'h') {
				if (hide++ == 3)
					hide = 0;
			}
			if (key == 'j') {
				hide = 0;
			}
			if (key == ';') {
				move = false;
				radius += 5;
				reduceNode = false;
				increaseNode = !increaseNode;
			}

			if (key == ',') {
				move = false;
				increaseNode = false;
				reduceNode = !reduceNode;
				if (radius > 0)
					radius -= 5;
			}
		}

		if (key == 'k' && cmdDown) {
			visible = true;
		}

		if (key == 'f') {
			Device.displayDetails = false;
			Device.displayInfos = false;
		}

		if (key == 'g') {
			Device.displayDetails = true;
			Device.displayInfos = true;
		}

		if (key == 'S') {// && (!ctrlDown && !cmdDown)) {
			simulate();
		}

		if (key == 'q') {
			stopSimulation();
		}

		if (key == 'm') {
			move = true;
			increaseNode = false;
			reduceNode = false;
		}

		if (key == 'l') {
			move = false;
		}

		if (key == 'a' && cmdDown) {
			selected = true;
			move = false;
		}

		if (key == 'z') {
			selected = false;
		}

		if (key == 'i') {
			invSelection();
		}

		if (key == 'e') {
			displayRadius = true;
		}

		if (key == 'r') {
			displayRadius = false;
		}

		if (key == 'w') {
			selected = false;
			if (Layer.selectType == getType())
				selected = true;
		}

		if (key == '1' || key == '2' || key == '3' || key == '4' || key == '5'
				|| key == '6' || key == '7' || key == '8') {
			move = false;
			selected = false;
		}
		Layer.getMapViewer().repaint();
	}

	public void preprocessing() {
	};

	/**
	 * Stop the simulation
	 */
	@SuppressWarnings("deprecation")
	public void stopSimulation() {
		// synchronized(this) {
		// this.wait(4000);
		// if (selected)
		preprocessing();
		if (thread != null) {
			thread.stop();
			x = xori;
			y = yori;
		}		
		thread = null;
		underSimulation = false;
	}

	/**
	 * Start the simulation
	 */
	public void simulate() {
		move = false;
		increaseNode = false;
		reduceNode = false;
		if (selected) {
			selected = false;
			start();
		}
		/*
		 * if (isAlive()) //synchronized(this) { //this.notify(); //} resume();
		 * else if (isInterrupted()) System.out.println("Simulation"); else
		 * if(zzz++==0) start();
		 */
	}

	/**
	 * Initialize the selection
	 */
	public void initSelection() {
		selected = false;
		move = false;
		increaseNode = false;
		reduceNode = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = new Point(e.getX(), e.getY());
		GeoPosition gp = Layer.getMapViewer().convertPointToGeoPosition(p);
		double ex = gp.getLatitude();
		double ey = gp.getLongitude();

		if (!move) {
			calculateDxDy(e.getX(), e.getY());
		}

		boolean tmp_inside = inside;

		inside(e.getX(), e.getY());

		if (inside != tmp_inside) {
			displayInfos = !displayInfos;
			Layer.getMapViewer().repaint();
		}

		if ((move && selected) && hide == 0) {
			x = ex - dx;
			y = ey - dy;
			Layer.getMapViewer().repaint();
		}

		if (increaseNode) {
			radius += 30;
			Layer.getMapViewer().repaint();
		}
		if (reduceNode) {
			if (radius > 0)
				radius -= 30;
			Layer.getMapViewer().repaint();
		}
		sensorParametersUpdate();
	}

	/**
	 * Draw two red arrows to note that the device will be moved if the mouse
	 * cursor is moved
	 * 
	 * @param x
	 *            Longitude
	 * @param y
	 *            Latitude
	 * @param g
	 *            Graphics
	 */
	public void drawMoveArrows(int x, int y, Graphics g) {
		if (move && (inside || selected)) {
			// 2 fleches rouges
			g.setColor(UColor.ROUGE);
			g.drawLine(x, y, x + 20, y);
			g.drawLine(x + 20, y, x + 14, y + 3);
			g.drawLine(x + 20, y, x + 14, y - 3);
			g.drawLine(x, y - 20, x, y);
			g.drawLine(x, y - 20, x - 3, y - 16);
			g.drawLine(x, y - 20, x + 3, y - 16);
		}
	}

	/**
	 * Draw +- symbol to note that the size of the device will be reduced or
	 * increased if the mouse cursor is moved or if the +- keys are used
	 * 
	 * @param x
	 *            Longitude
	 * @param y
	 *            Latitude
	 * @param g
	 *            Graphics
	 */
	public void drawIncRedDimNode(int x, int y, Graphics g) {
		if (reduceNode || increaseNode) {
			g.setColor(UColor.ROUGE);
			g.drawLine(x - 10, y, x + 10, y);
		}
		if (increaseNode) {
			g.drawLine(x, y - 10, x, y + 10);
		}
	}

	/**
	 * Initialization
	 * 
	 * @param g
	 *            Graphics
	 */
	public void initDraw(Graphics g) {
		g.setFont(new Font("arial", 1, 12));
	}

	/**
	 * Draw the ID of the device
	 * 
	 * @param x
	 *            Longitude
	 * @param y
	 *            Latitude
	 * @param g
	 *            Graphics
	 */
	public void drawId(int x, int y, Graphics g) {
		if (displayDetails) {
			g.setColor(Color.BLACK);
			g.drawString(getNodeIdName(), (int) (x + 10), (int) (y + 10));
		}
	}

	/**
	 * @param device
	 * @return if a device is in the radio area of the current device
	 */
	public boolean radioDetect(Device device) {
		if (withRadio && device.withRadio()) {
			// double dMax = Math.max(MapCalc.rayonEnPixel(getRadioRadius()),
			// MapCalc.rayonEnPixel(node.getRadioRadius()));
			double dMax = Math.max(getRadioRadius(), device.getRadioRadius());
			return (dMax > (distance(device)));
		}
		return false;
	}

	// public double maxDistance(Device node) {
	// double dMax = Math.max(MapCalc.rayonEnPixel(getRadioRadius()),
	// MapCalc.rayonEnPixel(node.getRadioRadius()));
	// return (dMax > distance(node)) ;
	// }
	// return false;
	// }

	/**
	 * @param device
	 * @return if a device is in the capture unit area of the current device
	 */
	public boolean detection(Device device) {
		if (withSensor && withRadio && (!device.withRadio || device.mobile)) {
			// double dMax =
			// MapCalc.rayonEnPixel(getCaptureRadius())+MapCalc.rayonEnPixel(node.getCaptureRadius());
			double dMax = getCaptureUnitRadius()
					+ device.getCaptureUnitRadius();
			if (dMax > distance(device)) {
				// setDetection(true);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/*
	 * public boolean detected(Device node) { if((!withRadio || mobile) &&
	 * node.withSensor && node.withRadio) { //double dMax =
	 * MapCalc.rayonEnPixel(
	 * getCaptureRadius())+MapCalc.rayonEnPixel(node.getCaptureRadius()); double
	 * dMax = getCaptureRadius()+node.getCaptureRadius(); if(dMax >
	 * distance(node)) { node.setDetection(true); return true; } else { return
	 * false; } } return false; }
	 */

	/**
	 * Draw the (line) radio link
	 * 
	 * @param device
	 *            Device
	 * @param g
	 *            Graphics
	 */
	public void drawRadioLink(Device device, Graphics g) {

		int[] coord = MapCalc.geoToIntPixelMapXY(x, y);
		int lx1 = coord[0];
		int ly1 = coord[1];
		coord = MapCalc.geoToIntPixelMapXY(device.getX(), device.getY());
		int lx2 = coord[0];
		int ly2 = coord[1];

		// int lx1 = MapCalc.geoToIntPixelMapX(x, y);
		// int ly1 = MapCalc.geoToIntPixelMapY(x, y);
		// int lx2 = MapCalc.geoToIntPixelMapX(device.getX(), device.getY());
		// int ly2 = MapCalc.geoToIntPixelMapY(device.getX(), device.getY());
		g.setColor(Color.BLACK);
		g.drawLine(lx1, ly1, lx2, ly2);
	}

	/**
	 * Draw the (line) detection link
	 * 
	 * @param device
	 *            Device
	 * @param g
	 *            Graphics
	 */
	public void drawDetectionLink(Device device, Graphics g) {
		int[] coord = MapCalc.geoToIntPixelMapXY(x, y);
		int lx1 = coord[0];
		int ly1 = coord[1];
		coord = MapCalc.geoToIntPixelMapXY(device.getX(), device.getY());
		int lx2 = coord[0];
		int ly2 = coord[1];

		// int lx1 = MapCalc.geoToIntPixelMapX(x, y);
		// int ly1 = MapCalc.geoToIntPixelMapY(x, y);
		// int lx2 = MapCalc.geoToIntPixelMapX(device.getX(), device.getY());
		// int ly2 = MapCalc.geoToIntPixelMapY(device.getX(), device.getY());
		g.setColor(Color.RED);
		g.drawLine(lx1, ly1, lx2, ly2);
	}

	/**
	 * Draw the radius
	 * 
	 * @param x
	 *            Longitude
	 * @param y
	 *            Latitude
	 * @param r
	 *            Radius
	 * @param g
	 *            Graphics
	 */
	public void drawRadius(int x, int y, int r, Graphics g) {
		if (r > 0 && displayRadius) {
			g.setColor(UColor.WHITED_TRANSPARENT);
			int lr1 = (int) (r * Math.cos(Math.PI / 4.));
			g.drawLine(x, y, (int) (x + lr1), (int) (y - lr1));
			g.drawString("" + radius, x + (lr1 / 2), (int) (y - (lr1 / 4.)));
		}
	}

	/**
	 * Draw the informations of the device
	 * 
	 * @param device
	 *            Device
	 * @param g
	 *            Graphics
	 */
	public void drawInfos(Device device, Graphics g) {
		int[] coord;
		if (displayInfos && selected && infos != null) {
			g.setFont(new Font("arial", 1, 10));
			coord = MapCalc.geoToIntPixelMapXY(x, y);
			int lx1 = coord[0];
			int ly1 = coord[1];
			// int lx1 = MapCalc.geoToIntPixelMapX(x, y);
			// int ly1 = MapCalc.geoToIntPixelMapY(x, y);
			g.setColor(UColor.WHITED_TRANSPARENT);
			g.fillRect(lx1 + 20, ly1 - 25, 150, 80);
			g.setColor(UColor.NOIR_TRANSPARENT);
			g.drawRect(lx1 + 20, ly1 - 25, 150, 80);
			g.setColor(Color.black);
			g.drawString(device.getInfos()[0][0] + device.getInfos()[0][1],
					lx1 + 30, ly1 - 10);
			g.drawString(device.getInfos()[1][0] + device.getInfos()[1][1],
					lx1 + 30, ly1);
			g.drawString(device.getInfos()[2][0] + device.getInfos()[2][1],
					lx1 + 30, ly1 + 10);
			g.drawString(device.getInfos()[3][0] + device.getInfos()[3][1],
					lx1 + 30, ly1 + 20);
			g.drawString(device.getInfos()[4][0] + device.getInfos()[4][1],
					lx1 + 30, ly1 + 30);
			g.drawString(device.getInfos()[5][0] + device.getInfos()[5][1],
					lx1 + 30, ly1 + 40);
			g.drawString(device.getInfos()[6][0] + device.getInfos()[6][1],
					lx1 + 30, ly1 + 50);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Device clone() throws CloneNotSupportedException {
		Device newNode = (Device) super.clone();
		newNode.setId();
		newNode.move = true;
		Layer.getMapViewer().addMouseListener(newNode);
		Layer.getMapViewer().addMouseMotionListener(newNode);
		Layer.getMapViewer().addKeyListener(newNode);
		return newNode;
	}

	/**
	 * Start simulation thread
	 */
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		} else {
			System.out.println("Simulation is running for node "
					+ getNodeIdName());
		}
	}

	/**
	 * @return the battery level
	 */
	public double getBatteryLevel() {
		return this.getBattery().getCapacity();
	}

	/**
	 * @return the battery object
	 */
	public Battery getBattery() {
		return null;
	}

	/**
	 * @return the state of the device
	 */
	public boolean getState() {
		return state;
	}

	/**
	 * Set the state value
	 * 
	 * @param state
	 */
	public void setState(boolean state) {
		this.state = state;
	}

	/**
	 * @return if the device is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @return the simulation instance
	 */
	public DeviceSimulator getSimulator() {
		return deviceSimulator;
	}

	public Thread getThread() {
		return thread;
	}

	public static void initNumber() {
		number = 0;
	}

	public static void incNumber() {
		number++;
	}
	
	public void fixori() {
		xori = x;
		yori = y;
	}

	public void toori() {
		x = xori;
		y = yori;
	}
	
	public int getHide() {
		return this.hide;
	}

	public abstract int getNextTime();
	public abstract void loadRouteFromFile();
	public abstract void exeNext(boolean visual, int visualDelay);
	public abstract boolean canMove();

	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public int getChannel() {
		return channel;
	}
	
}