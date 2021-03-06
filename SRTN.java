import java.util.*;
import java.io.*;

public class SRTN{

	private Udri udri = new Udri();
	private ArrayList<Process> unstartedProcesses = new ArrayList<Process>();
	private ArrayList<Process> incomingProcesses = new ArrayList<Process>();
	// private ArrayList<Process> readyQueue = new ArrayList<Process>();
	private PriorityQueue<Process> readyQueue = new PriorityQueue<Process>(10000,Process.RemainingCPUTimeNeededComparator);
	private ArrayList<Process> blockedProcesses = new ArrayList<Process>();
	private ArrayList<Process> finishedProcesses = new ArrayList<Process>();
	private int numberOfProcesses = 0;
	private Process currProcesses = null;
	private Boolean verbose = false;
	private int totalIOTime = 0;


	public SRTN(ArrayList<Process> processes,Boolean verbose){
		numberOfProcesses = processes.size();
		this.verbose = verbose;
		System.out.print("The original input was: "+numberOfProcesses+" ");
		Iterator<Process> itBeforeSorting = processes.iterator();
		while(itBeforeSorting.hasNext()){
			Process temp = itBeforeSorting.next();
			Process tempCopy = new Process(temp);
			unstartedProcesses.add(tempCopy);
			tempCopy.printProcessSimple();
		}
		System.out.println("");
		System.out.print("The (sorted) input is:  "+numberOfProcesses+" ");
		Collections.sort(unstartedProcesses,Process.ArrivalComparator);
		Iterator<Process> itAfterSorting = unstartedProcesses.iterator();
		int processNum = 0;
		while(itAfterSorting.hasNext()){
			Process tempCopy = itAfterSorting.next();
			tempCopy.setProcessID(processNum++);
			tempCopy.printProcessSimple();
		}
		System.out.println("");
	}

	private void fillArrivingProcesses(int time){
		ArrayList<Process> tempProcessesArriving = new ArrayList<Process>();
		while(unstartedProcesses.size() > 0){
			Process tempCopy = unstartedProcesses.remove(0);
			if(tempCopy.getArrivalTime() <= time){
				incomingProcesses.add(tempCopy);
			}
			else{
				tempProcessesArriving.add(tempCopy);
			}
		}
		while(tempProcessesArriving.size() > 0){
			unstartedProcesses.add(tempProcessesArriving.remove(0));
		}
		// Collections.sort(incomingProcesses,Process.RemainingCPUTimeNeededComparator);
		while(incomingProcesses.size() > 0){
			Process temp = incomingProcesses.remove(0);
			temp.setState("ready");
			readyQueue.add(temp);
		}
		// Collections.sort(readyQueue,Process.RemainingCPUTimeNeededComparator);
	}

	private void fillCompletedBlockedProcesses(int time){
		ArrayList<Process> tempProcessesUnblocked = new ArrayList<Process>();
		while(blockedProcesses.size() > 0){
			Process tempCopy = blockedProcesses.remove(0);
			if(tempCopy.getRemainingIOBurstTime() == 0){
				incomingProcesses.add(tempCopy);
			}
			else{
				tempProcessesUnblocked.add(tempCopy);
			}
		}
		while(tempProcessesUnblocked.size() > 0){
			blockedProcesses.add(tempProcessesUnblocked.remove(0));
		}
		Collections.sort(incomingProcesses,Process.RemainingCPUTimeNeededComparator);
		while(incomingProcesses.size() > 0){
			Process temp = incomingProcesses.remove(0);
			temp.setState("ready");
			readyQueue.add(temp);
		}
		// Collections.sort(readyQueue,Process.RemainingCPUTimeNeededComparator);
	}

	private void incrementWaitTimes(){
		Iterator<Process> it = readyQueue.iterator();
		while(it.hasNext()){
			Process temp = it.next();
			temp.incrementWaitTime();
		}
	}

	private void decrementIOTimes(){
		if(blockedProcesses.size() > 0){
			totalIOTime++;
		}
		Iterator<Process> it = blockedProcesses.iterator();
		while(it.hasNext()){
			Process temp = it.next();
			temp.decrementRemainingIOBurstTime();
			temp.incrementIOTime();
		}
	}

	private void printAllProcessses(int totalTime){
		System.out.println("");
		System.out.println("\nThe scheduling algorithm used was Preemptive Shortest Job First");
		System.out.println("");
		int totalTurnAroundTime = 0;
		int totalWaitingTime = 0;
		int totalCPUTime = 0;

		Iterator<Process> it = finishedProcesses.iterator();
		while(it.hasNext()){
			Process temp = it.next();
			totalTurnAroundTime += temp.getTurnAroundTime();
			totalWaitingTime += temp.getTotalWaitTime();
			totalCPUTime += temp.getCPUTimeNeeded();
			temp.printProcess();
		}

		System.out.println("Summing Data:");
		System.out.println("              Finishing time: "+totalTime);
		System.out.println("              CPU Utilization: "+(double)totalCPUTime/totalTime);
		System.out.println("              I/O Utilization: "+(double)totalIOTime/totalTime);
		System.out.println("              Throughput: "+(double)numberOfProcesses*100/totalTime+" processes per hundred cycles");
		System.out.println("              Average turnaround time: "+(double)totalTurnAroundTime/numberOfProcesses);
		System.out.println("              Average waiting time: "+(double)totalWaitingTime/numberOfProcesses);
	}

	private void printVerbose(Boolean verbose,int time){
		if(verbose){
			String algo = "SRTN";
			String timeString = Integer.toString(time);
			String tempString = ("     "+timeString+":").substring(timeString.length());
			System.out.print("\nBefore cycle"+tempString);
			for(int processNum = 0; processNum < numberOfProcesses; processNum++){

				for(Process temp : unstartedProcesses){
			        if(temp.getProcessID() == processNum) {
			        	String state = temp.getState();
			        	String tempString1 = ("             "+state).substring(state.length());
			        	String burstTime = Integer.toString(temp.getBurstTime(algo));
			        	String tempString2 = ("   "+burstTime).substring(burstTime.length());
			            System.out.print(tempString1+tempString2);
			            break;
			        }
			    }

			    for(Process temp : readyQueue){
			        if(temp.getProcessID() == processNum) {
			        	String state = temp.getState();
			        	String tempString1 = ("             "+state).substring(state.length());
			        	String burstTime = Integer.toString(temp.getBurstTime(algo));
			        	String tempString2 = ("   "+burstTime).substring(burstTime.length());
			            System.out.print(tempString1+tempString2);
			            break;
			        }
			    }

			    for(Process temp : blockedProcesses){
			        if(temp.getProcessID() == processNum) {
			        	String state = temp.getState();
			        	String tempString1 = ("             "+state).substring(state.length());
			        	String burstTime = Integer.toString(temp.getBurstTime(algo));
			        	String tempString2 = ("   "+burstTime).substring(burstTime.length());
			            System.out.print(tempString1+tempString2);
			            break;
			        }
			    }

			    for(Process temp : finishedProcesses){
			        if(temp.getProcessID() == processNum) {
			        	String state = temp.getState();
			        	String tempString1 = ("             "+state).substring(state.length());
			        	String burstTime = Integer.toString(temp.getBurstTime(algo));
			        	String tempString2 = ("   "+burstTime).substring(burstTime.length());
			            System.out.print(tempString1+tempString2);
			            break;
			        }
			    }
			    if(currProcesses != null){
			    	if(currProcesses.getProcessID() == processNum){
				    	String state = currProcesses.getState();
			        	String tempString1 = ("             "+state).substring(state.length());
			        	String burstTime = Integer.toString(currProcesses.getBurstTime(algo));
			        	String tempString2 = ("   "+burstTime).substring(burstTime.length());
			            System.out.print(tempString1+tempString2);	
				    }
			    }
			}
		}
	}

	private void printReadyQueue(){
		System.out.println(" ");
		Iterator<Process> it = readyQueue.iterator();
		while(it.hasNext()){
			Process temp = it.next();
			System.out.print(" ("+temp.getProcessID()+","+temp.getRemainingCPUTimeNeeded()+")");
		}
	}

	private void runSRTN(Boolean verbose){

		int time = -1;
		currProcesses = null;
		int prevProcessID = -1;

		while(finishedProcesses.size() < numberOfProcesses){
			time++;
			printVerbose(verbose,time);
			// printReadyQueue();

			if(currProcesses != null){
				if(currProcesses.getRemainingCPUBurstTime() == 0 || currProcesses.getRemainingCPUTimeNeeded() == 0){
					if(currProcesses.getRemainingCPUTimeNeeded() == 0){
						currProcesses.finish(time);
						currProcesses.setState("terminated");
						finishedProcesses.add(currProcesses);
					}
					else{
						currProcesses.randomIOBurst(udri);
						currProcesses.setState("blocked");
						blockedProcesses.add(currProcesses);
					}
					currProcesses = null;
				}
				else{
					currProcesses.setState("ready");
					readyQueue.add(currProcesses);
					currProcesses = null;
				}
			}

			// fillCompletedBlockedProcesses(time);
			// fillArrivingProcesses(time);
			// currProcesses = readyQueue.poll();
			// currProcesses.randomCPUBurst(udri);
			// currProcesses.setState("running");

			if(readyQueue.size() > 0){
				fillCompletedBlockedProcesses(time);
				fillArrivingProcesses(time);
				currProcesses = readyQueue.poll();
				if(currProcesses.getProcessID() != prevProcessID && currProcesses.getRemainingCPUBurstTime() == 0){
					prevProcessID = currProcesses.getProcessID();
					currProcesses.randomCPUBurst(udri);
				}
				currProcesses.setState("running");
			}
			else{
				currProcesses = null;
			}

			if(currProcesses == null){
				fillCompletedBlockedProcesses(time);
				fillArrivingProcesses(time);
				if(readyQueue.size() > 0){
					currProcesses = readyQueue.poll();
					if(currProcesses.getProcessID() != prevProcessID && currProcesses.getRemainingCPUBurstTime() == 0){
						prevProcessID = currProcesses.getProcessID();
						currProcesses.randomCPUBurst(udri);
					}
					currProcesses.setState("running");
				}
				else{
					currProcesses = null;
				}
			}

			if(currProcesses != null){
				currProcesses.run();
				prevProcessID = currProcesses.getProcessID();
			}
			else{
				prevProcessID = -1;
			}
			incrementWaitTimes();
			decrementIOTimes();
			
		}

		printAllProcessses(time);

	}

	public void run(){
		try{
			System.out.println("This detailed printout gives the state and remaining burst for each process");
			runSRTN(verbose);	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


}