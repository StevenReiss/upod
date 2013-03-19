/********************************************************************************/
/*                                                                              */
/*              BasisThreadPool.java                                            */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.upod.basis;



import java.util.concurrent.*;


public class BasisThreadPool extends ThreadPoolExecutor implements BasisConstants, ThreadFactory
{



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static BasisThreadPool          the_pool = null;
private static int                      thread_counter = 0;



/********************************************************************************/
/*                                                                              */
/*      Static Entries                                                          */
/*                                                                              */
/********************************************************************************/

/**
 *	Execute a background task using our thread pool.
 **/

public static void start(Runnable r)
{
   if (r != null) getPool().execute(r);
}






private static synchronized BasisThreadPool getPool()
{
   if (the_pool == null) {
      the_pool = new BasisThreadPool();
    }
   return the_pool;
}



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private BasisThreadPool()
{
   super(BASIS_CORE_POOL_SIZE,BASIS_MAX_POOL_SIZE,
         BASIS_POOL_KEEP_ALIVE_TIME,TimeUnit.MILLISECONDS,
         new LinkedBlockingQueue<Runnable>());
   
   setThreadFactory(this);
}




/********************************************************************************/
/*										*/
/*	Thread creation methods 						*/
/*										*/
/********************************************************************************/

@Override public Thread newThread(Runnable r)
{
   return new Thread(r,"BasisWorkerThread_" + (++thread_counter));
}



/********************************************************************************/
/*										*/
/*	Logging methods 							*/
/*										*/
/********************************************************************************/

@Override protected void beforeExecute(Thread t,Runnable r)
{
   super.beforeExecute(t,r);
}



@Override protected void afterExecute(Runnable r,Throwable t)
{
   super.afterExecute(r,t);
   
   if (t != null) {
      System.err.println("BASIS: Problem with background task " + r.getClass().getName() + " " + r + ": " + t);
      t.printStackTrace();
    }
}





}       // end of class BasisThreadPool




/* end of BasisThreadPool.java */

