package com.github.cypher.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Updater extends Thread {
	private Map<Updateable, Integer> watching = new ConcurrentHashMap<>(10);

	private final int interval;

	public Updater(){
		interval = 500;
	}

	public Updater(int interval){
		this.interval = interval;
	}

	@Override
	public void run() {

		for (int i = 1;;i++){
			for (Map.Entry<Updateable, Integer> entry: watching.entrySet()) {
				if (i % entry.getValue() == 0 ){
					entry.getKey().update();
				}
			}

			try{
				Thread.sleep(interval);
			}catch (InterruptedException err){

			}
		}
	}

	public void add(Updateable u, Integer i){
		watching.put(u, i);
	}

	public void remove(Updateable u){
		watching.remove(u);
	}
}
