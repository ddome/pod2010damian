package ar.edu.itba.pod.legajo47189.communication.Impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ar.edu.itba.pod.simul.communication.ThreePhaseCommit;
import ar.edu.itba.pod.simul.communication.Transactionable;

public class TransactionThread extends Thread {

    private long timeout;
    private Transactionable transacion;
    private boolean finished;

    private final static Logger LOGGER = Logger.getLogger(ThreePhaseThread.class);
    
    public TransactionThread(long timeout, Transactionable transacion)
    {
        this.timeout = timeout;
        this.transacion = transacion;
    }
    
    public void run()
    {
        boolean flag = false;
        int time = 0;
        LOGGER.debug(timeout);
        timeout = 100000;
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
                LOGGER.info("Timeout en transaccion. Se hace rollback a los " + time);
                timeout();
            }
        }
    }

    private void timeout() {
        
        try {
            transacion.rollback();
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
