package com.immrayral.task3.port;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Dock extends Thread {
    public int dockID;
    private boolean free = true;
   private BlockingQueue<Ship> shipsQueue;
    private Ship currentShip;
    private int counter;
    private Storage storage;
    private final ReentrantLock lock = new ReentrantLock();

    public Dock(BlockingQueue<Ship> shipsQueue, int dockNum, int counter, Storage storage) {
        super();
        this.dockID = dockNum;
        this.shipsQueue = shipsQueue;
        this.counter = counter;
        this.storage = storage;
    }

    public boolean isFree() {return free;}


    public void setCounter(int counter) {
        this.counter = counter;
    }

    public Ship getCurrentShip() {
        return currentShip;
    }



    public void setCurrentShip(Ship currentShip) {
        this.currentShip = currentShip;
    }

    private boolean canWork() {
        return counter>0;
    }
    public ReentrantLock getLock() {
        return this.lock;
    }

    @Override
    public void run() {
    boolean worked = false;
        while(canWork()) {

            if (currentShip!=null) {
                lock.lock();
                try {

                free=false;
                worked = true;

                if(currentShip.getCargo()==0)
                {
                    currentShip.interrupt();
                    currentShip = null;
                    if (this.lock.tryLock())
                        try {

                            continue;
                        } finally {
                            this.lock.unlock();
                        }
                }
                    System.out.println("NOT NULL DOCK-" + dockID + " SHIP-" + currentShip.getShipID());
                    if (storage.tryTransfer(currentShip)) {
                        counter--;
                        System.out.println("DOCK - " + dockID + "  Ship " + currentShip.getShipID() + " transfer cargo to storage");
                        System.out.println("Current capacity - " + storage.capacity);//!!!!!!!!!!!!! storage public
                        free = true;
                    } else {

                        Iterator<Ship> ships = storage.getIterator();
                        Boolean flag = false;
                        while (ships.hasNext()) {
                            Ship someShip = ships.next();
                            if (someShip.tryAddCargo(currentShip.getCargo())) {
                                System.out.println("DOCK - " + dockID + "  Ship " + currentShip.getShipID() + "transfer cargo to ship" + someShip.getShipID());
                                flag = true;
                                counter--;
                                free = true;
                                currentShip = storage.getShip();
                                break;
                            }
                        }
                        if (!flag) {
                            System.out.println("DOCK - " + dockID + "  Ship " + currentShip.getShipID() + " can't transfer to anywhere cargo");
                            currentShip=null;
                        }

                    }
                } finally {
                    if (this.lock.tryLock())
                        this.lock.unlock();
                }
            } else
            {

                     free = true;

                     currentShip = storage.getShip();

                     if (currentShip == null && worked) {
                         try {
                           this.interrupt();
                           return;
                         }
                         catch (Exception e) {
                             e.printStackTrace();
                         }
                     }


            }
//
//            else {
//                try {
//                    lock.lock();
//                        free = true;
//                        wait();
//                        lock.unlock();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

        }
    }
}
