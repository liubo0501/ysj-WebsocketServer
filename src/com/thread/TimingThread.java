package com.thread;

public class TimingThread implements Runnable {
	@Override
	public void run() {
		while(true) {
				try {
					Thread.sleep(1000*30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
}
