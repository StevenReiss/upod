/********************************************************************************/
/*										*/
/*		MessageChecker.java						*/
/*										*/
/*	Check for external motion and telephone connections			*/
/*										*/
/********************************************************************************/

package spr.automate;


import java.io.*;
import java.net.*;


public class MessageChecker {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   MessageChecker mc = new MessageChecker(args);
   mc.process();
   // runTest();
}




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int		check_port;
private long		report_time;
private String		last_report;
private Object		synch_object;
private BufferedReader	input_stream;
private FileWriter	 output_stream;

private static final String TTY_FILE = "/dev/ttyACM";
private static int    DEFAULT_PORT = 19892;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

MessageChecker(String [] args)
{
   check_port = DEFAULT_PORT;
   report_time = 0;
   last_report = null;
   synch_object = new Object();

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-p") && i+1 < args.length) {           // -port #
	    check_port = Integer.parseInt(args[++i]);
	  }
	 else badArgs();
       }
      else badArgs();
    }
}


private void process()
{
   FileInputStream fis = null;
   Exception lastex = null;
   String nm = null;

   for (int i = 0; i < 10; ++i) {
      nm = TTY_FILE + i;
      try {
	 fis = new FileInputStream(nm);
	 break;
       }
      catch (IOException e) {
	 lastex = e;
       }
    }
   if (fis == null) {
      reportError(lastex);
      System.exit(1);
    }

   try {
      input_stream = new BufferedReader(new InputStreamReader(fis));
      output_stream = new FileWriter(nm);

      UsbReader r = new UsbReader();
      r.start();

      ServerSocket ss = new ServerSocket(check_port);
      Acceptor ac = new Acceptor(ss);
      ac.start();
    }
   catch (IOException e) {
      reportError(e);
      System.exit(1);
    }
}




/********************************************************************************/
/*										*/
/*	Result computations							*/
/*										*/
/********************************************************************************/

private String getResult()
{
   long now = System.currentTimeMillis();
   String line = null;
   synchronized (synch_object) {
      try {
	 output_stream.write("GO\n");
	 output_stream.flush();
       }
      catch (IOException e) {
	 return null;
       }
      while (now > report_time) {
	 try {
	    synch_object.wait();
	  }
	 catch (InterruptedException e) { }
       }
      line = last_report;
    }
   return getReport(line);
}



private static String getReport(String ln)
{
   int [] vals = new int[128];

   try {
      int idx = ln.lastIndexOf("GO ");
      ln = ln.substring(idx+3);
      StreamTokenizer st = new StreamTokenizer(new StringReader(ln));
      st.parseNumbers();
      int ct = 0;

      while (st.nextToken() == StreamTokenizer.TT_NUMBER) {
	 vals[ct++] = (int) st.nval;
       }
    }
   catch (IOException e) { }

   int motion = (vals[9] > 2 && vals[18] - vals[9] > 8 ? 1 : 0);
   int phone = (vals[4] > 0 && vals[7] > 0 ? 1 : 0);

   return "RESULT " + motion + phone;
}




/********************************************************************************/
/*										*/
/*	Error routines								*/
/*										*/
/********************************************************************************/

private static void badArgs()
{
   System.err.println("MESSAGECHECKER: messagechecker [-p <port>]");
   System.exit(1);
}


private static void reportError(Exception e)
{
   System.err.println("MESSAGECHECKER: I/O error: " + e);
}




/********************************************************************************/
/*										*/
/*	UsbReader thread							*/
/*										*/
/********************************************************************************/

private class UsbReader extends Thread {

   @Override public void run() {
      try {
	 empty(input_stream);
	 for ( ; ; ) {
	    while (!input_stream.ready()) {
	       delay(1000);
	     }
	    delay(50);		// ensure a full message
	    String ln = input_stream.readLine();
	    if (ln == null) continue;
	    if (checkLine(ln)) {
	       synchronized (synch_object) {
		  report_time = System.currentTimeMillis();
		  last_report = ln;
		  synch_object.notifyAll();
		}
	     }
	  }
       }
      catch (IOException e) {
	 reportError(e);
       }
    }

}	// end of inner class UsbReader



private static void delay(long t)
{
   try {
      Thread.sleep(t);
    }
   catch (InterruptedException e) { }
}



private static void empty(Reader fr) throws IOException
{
   delay(500);

   char [] buf = new char[128];

   while (fr.ready()) {
      fr.read(buf);
      delay(200);
    }
}




private static boolean checkLine(String ln) throws IOException
{
   if (ln == null) return false;
   int idx = ln.lastIndexOf("GO ");
   if (idx < 0) return false;
   System.err.println("CHECK: " + ln);
   ln = ln.substring(idx+3);
   StreamTokenizer st = new StreamTokenizer(new StringReader(ln));
   st.parseNumbers();
   int [] vals = new int[128];
   int ct = 0;
   while (st.nextToken() == StreamTokenizer.TT_NUMBER) {
      vals[ct++] = (int) st.nval;
    }
   if (ct < 10) return false;

   return true;
}



/********************************************************************************/
/*										*/
/*	Accept connections							*/
/*										*/
/********************************************************************************/

private class Acceptor extends Thread {

   private ServerSocket server_socket;

   Acceptor(ServerSocket ss) {
      super("MessageCheckerAcceptor");
      server_socket = ss;
    }

   @Override public void run() {
      try {
	 for ( ; ; ) {
	    Socket s = server_socket.accept();
	    if (s != null) {
	       Client c = new Client(s);
	       c.start();
	     }
	  }
       }
      catch (IOException e) {
	 try {
	    server_socket.close();
	    server_socket = null;
	  }
	 catch (IOException ex) { }
       }
    }

}	// end of inner class Acceptor




/********************************************************************************/
/*										*/
/*	Client management							*/
/*										*/
/********************************************************************************/

private class Client extends Thread {

   private Socket client_socket;
   private InputStream client_input;
   private OutputStream client_output;

   Client(Socket s) {
      super("MessageCheckerClient_" + s);
      client_socket = s;
      try {
	 client_input = s.getInputStream();
	 client_output = s.getOutputStream();
       }
      catch (IOException e) { }
    }

   @Override public void run() {
      byte [] ibuf = new byte[20480];
      if (client_input == null) return;

      try {
	 client_input.read(ibuf);
	 String rslt = getResult();
	 if (rslt != null) {
	    rslt += "\n";
	    byte [] obuf = rslt.getBytes();
	    client_output.write(obuf);
	  }
       }
      catch (IOException e) {
	 System.err.println("MESSAGECHECKER: I/O error on user socket: " + e);
       }
      finally {
	 try {
	    client_input.close();
	    client_input = null;
	  }
	 catch (IOException e) { }
	 try {
	    client_output.close();
	    client_output = null;
	  }
	 catch (IOException e) { }
	 try {
	    client_socket.close();
	    client_socket = null;
	  }
	 catch (IOException e) { }
       }
    }

}	// end of inner class Client



/********************************************************************************/
/*										*/
/*	Test routine								*/
/*										*/
/********************************************************************************/

private static void runTest()
{
   for (int i = 0; i < 10; ++i) {
      delay(20000);
      try {
	 Socket s = new Socket("valerie.cs.brown.edu",DEFAULT_PORT);
	 OutputStream so = s.getOutputStream();
	 so.write("GO\n".getBytes());
	 so.flush();
	 InputStream si = s.getInputStream();
	 Reader r = new InputStreamReader(si);
	 char [] buf = new char[10240];
	 int ln = r.read(buf);
	 String rslt = new String(buf,0,ln);
	 System.err.println("RESULT: " + rslt);
       }
      catch (IOException e) {
	 System.err.println("TEST ERROR: " + e);
       }
    }
}





}	// end of class MessageChecker




/* end of MessageChecker.java */
