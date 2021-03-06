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

package wisen_simulation2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import project.Project;
import synchronization.Scheduler;
import synchronization.Semaphore;
import cupcarbon.MtSimulationWindow;
import device.Device;
import device.DeviceList;

public class Simulation implements Simulator_Interface, Runnable {

	// In the following, the values are in milli-seconds
	private static long defaulSimulationDelay = 360000000; // 100 hours
	private long simulationDelay = 0;
	private long defaulSimulationLogicDelay = 60000; // 60 seconds
	private long simulationLogicDelay = 0;
	private long step = 3600000; // a step of one hour
	private long iStep = 0;
	private Semaphore semaphore = null;
	private Scheduler scheduler = null;
	private Thread thread = null;
	private boolean more;
	private String logFileName = "log";
	private String simulationName = "Name";
	private PrintStream logps;
	private long startTime;
	private long endTime;

	public Simulation(String name, String log) {
		setSimulationName(name);
		setLogFileName(log);
		setSimulationMode(SimulationMode.PARALLELMODE);
		setSimulationDelay(Simulation.defaulSimulationDelay);
		setSimulationLogicDelay(defaulSimulationLogicDelay);
	}

	public long getStartTime() {
		return startTime ;
	}
	
	public Scheduler getScheduler() {
		return scheduler ;
	}
	
	public Semaphore getSemaphore() {
		return semaphore ;
	}
	
	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	@Override
	public void initSimulation() {
		scheduler = new Scheduler();
		semaphore = new Semaphore(1);
		more = true;
		for (Device device : DeviceList.getNodes()) {
			device.initSimulator(this);
			device.getSimulator().start();
		}

		try {
			logps = new PrintStream(new FileOutputStream(
					Project.getLogFileFromName(Project
							.getLogFileExtension(logFileName))));
			logps.println("CupCarbon v. 1.0");
			logps.println("Simulation name : " + simulationName);
			logps.println("Events list [");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void addEvent(Event event) {
		event.setSimulation(this);
		scheduler.addEvent(event);
	}

	public void removeEvent(Event event) {
		scheduler.removeEvent(event);
	}

	@Override
	public void run() {
		while (more) {
			semaphore.P();
			action();
		}
		endSimulation();
	}

	public void action() {
		Event nextEvent = scheduler.getNextEvent();

		if ((nextEvent == null)
				|| (nextEvent.getEventDate() > getSimulationDelay())
				|| ((System.nanoTime() - startTime) > (getSimulationLogicDelay() * 1000000))) {
			if (nextEvent == null)
				JOptionPane.showMessageDialog(null, "NextEvent Null");

			if (nextEvent.getEventDate() > getSimulationDelay())
				JOptionPane.showMessageDialog(null, "SimDelay -");

			if ((System.nanoTime() - startTime) > (getSimulationLogicDelay() * 1000000))
				JOptionPane.showMessageDialog(null, "SimLogicDelay -");

			more = false;
		} else {

			if ((nextEvent.getEventDate() / step) >= iStep) {
				iStep++;

				for (Device device : DeviceList.getNodes()) {
					device.getSimulator().saveResult(step * iStep);
				}
			}

			nextEvent.getDevice().getBattery().setCapacity((long)(nextEvent.getDevice().getBattery().getCapacity() - (0.00000000008 / 100.) * nextEvent.getPowerRatio()));
			//Battery btry1 = nextEvent.getDevice().getBattery();			
			//btry1.setCapacity(btry1.getCapacity() - (0.00000000008 / 100.) * nextEvent.getPowerRatio());
			
			logps.println((System.nanoTime() - startTime) + " "
					+ nextEvent.getDevice().getId() + " "
					+ nextEvent.getDevice().getUserId() + " send "
					+ nextEvent.getMessage() + ", date : "
					+ (nextEvent.getEventDate() * 1000000) + " + eps "
					+ nextEvent.getEpsilon());

			MtSimulationWindow.setState("Simulate (MT) ...");
			for (Device device : DeviceList.getNodes()) {
				if ((nextEvent.getDevice() != device) && (nextEvent.getDevice().radioDetect(device))) {
					// cosommation des capteurs recepteurs
					device.getBattery().setCapacity((long)(device.getBattery().getCapacity() - (0.00000000008 / 100.) * nextEvent.getPowerRatio()));
					
					//Battery btry2 = device.getBattery();
					//btry2.setCapacity(btry2.getCapacity() - (0.00000000008 / 100.) * nextEvent.getPowerRatio());
					
					logps.println((System.nanoTime() - startTime) + " "
							+ device.getId() + " " + device.getUserId() + " recive "
							+ nextEvent.getMessage() + ", date : "
							+ (nextEvent.getEventDate() * 1000000) + " + eps "
							+ nextEvent.getEpsilon());
				}
			}
			MtSimulationWindow.setState("End of Simulation (MT) ...");
			// nextEvent.getDevice().
			nextEvent.getDevicesimulator().lock.V();
		}
	}

	@Override
	public void startSimulation() {
		initSimulation();
		thread = new Thread(this);
		startTime = System.nanoTime();
		thread.start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void stopSimulation() {
		thread.stop();
	}

	@Override
	public void pauseSimulation() {
	}

	@Override
	public void endSimulation() {
		endTime = System.nanoTime();
		logps.println("]");
		logps.println();
		logps.println("Duration :");
		logps.println("Logic : " + (endTime - startTime) + " ns");
		logps.println("End simulation");
		logps.close();
		JOptionPane.showMessageDialog(null, ""
				+ (System.nanoTime() - startTime) + " - "
				+ (getSimulationDelay() * 1000000));
		JOptionPane.showMessageDialog(
				null,
				"Fin de simulation, Consultez le fichier log pour plus de d�atail\n Log :"
						+ Project.getLogFileFromName(Project
								.getLogFileExtension(logFileName)));
	}

	@Override
	public void consumptionEnergy(Event event) {

	}

	@Override
	public long getSimulationLogicDelay() {
		return simulationLogicDelay;
	}

	@Override
	public void setSimulationLogicDelay(long simulationLogicDelay) {
		this.simulationLogicDelay = simulationLogicDelay;
	}

	@Override
	public void setSimulationDelay(long delay) {
		simulationDelay = delay;
	}

	@Override
	public long getSimulationDelay() {
		return simulationDelay;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public String getSimulationName() {
		return simulationName;
	}

	public void setSimulationName(String simulationName) {
		this.simulationName = simulationName;
	}

	@Override
	public long getRemainingSimulationTime() {
		return 0;
	}

	@Override
	public int getNumberSentMessages() {
		return 0;
	}

	@Override
	public int getNumberSentMessages(long delai1, long delai2, TimeMode tmode) {
		return 0;
	}

	@Override
	public int getNumberSentMessages(Device device) {
		return 0;
	}

	@Override
	public int getNumberSentMessages(Device device, long delai1, long delai2,
			TimeMode tmode) {
		return 0;
	}

	@Override
	public Object getSimulationState() {
		return null;
	}

	@Override
	public void setSimulationMode(SimulationMode sMode) {
	}

	@Override
	public SimulationMode getSimulationMode() {
		return null;
	}

}
