package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;

public class ThreePhaseThread extends Thread {

    private int timeout;
    private ThreePhaseCommit commit;
    private boolean finished;

    private final static Logger LOGGER = Logger.getLogger(ThreePhaseThread.class);
    
    public ThreePhaseThread(int timeout, ThreePhaseCommit commit)
    {
        this.timeout = timeout;
        this.commit = commit;
    }
    
    public void run()
    {
        boolean flag = false;
        int time = 0;
        while(!flag)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ioe) {
                    continue;
            }
            time += 100;
            
            if (isFinished())
            {
                break;
            }
            if (time > timeout)
            {
                timeout();
            }
        }
    }

    private void timeout() {
        LOGGER.info("Se disparo el timeout. Aborto commit");
        try {
            commit.onTimeout();
            setFinished(true);
        } catch (RemoteException e) {
            LOGGER.error(e);
        }
    }

    public synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }

    public synchronized boolean isFinished() {
        return finished;
    }
    
    
}
