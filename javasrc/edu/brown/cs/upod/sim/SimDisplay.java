/********************************************************************************/
/*										*/
/*		SimDisplay.java 						*/
/*										*/
/*	Display panel								*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.upod.sim;

import edu.brown.cs.upod.usim.*;

import edu.brown.cs.ivy.swing.*;

import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;


class SimDisplay extends JFrame implements SimConstants, GLEventListener
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SimSimulator	the_simulator;

private GLCanvas	drawing_area;
private JLabel		message_area;
private DrawThread	gl_thread;
private boolean 	is_inited;
private Bounds		x_bounds;
private Bounds		y_bounds;
private Bounds		z_bounds;
private SimSpinner      input_manager;
private SimSurface      base_surface;

private static final int	FRAMES_PER_SECOND = 15;

private static final long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SimDisplay(SimSimulator ss)
{
   super("Simulator Display");

   the_simulator = ss;
   gl_thread = null;
   message_area = null;
   is_inited = false;
   x_bounds = null;
   y_bounds = null;
   z_bounds = null;

   setupWindow();

   input_manager = new SimSpinner(this,drawing_area);

   setDefaultCloseOperation(EXIT_ON_CLOSE);
   
   base_surface = new SimSurface();
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

public void begin()
{
   setVisible(true);
   gl_thread = new DrawThread();
   if (is_inited) gl_thread.start();
}


void setControlledObject(SimObject obj)
{
   input_manager.setObject(obj);
}


/********************************************************************************/
/*										*/
/*	Window setup methods							*/
/*										*/
/********************************************************************************/

private void setupWindow()
{
   Dimension d = new Dimension(600,600);
   SwingGridPanel pnl = new SwingGridPanel();

   GLProfile glp = GLProfile.getDefault();
   GLCapabilities caps = new GLCapabilities(glp);
   drawing_area = new GLCanvas(caps);
   drawing_area.setSize(d);
   drawing_area.addGLEventListener(this);

   message_area = new JLabel("Hello",JLabel.CENTER);
   Font ft = message_area.getFont();
   ft = ft.deriveFont(20f);
   message_area.setFont(ft);

   pnl.addGBComponent(drawing_area,0,0,1,1,10,10);
   pnl.addGBComponent(message_area,0,1,1,1,10,0);

   setContentPane(pnl);
   pack();
}




/********************************************************************************/
/*										*/
/*	GL methods							       */
/*										*/
/********************************************************************************/

@Override public void display(GLAutoDrawable d)
{
   Collection<SimObject> objset = the_simulator.getObjects();

   if (x_bounds == null) computeBounds();

   GL2 gl = (GL2) d.getGL();
   gl.glMatrixMode(GL2.GL_MODELVIEW);
   gl.glLoadIdentity();

   SimCamera cam = input_manager.getCamera();
   cam.updateSettings();
   cam.draw(gl);
   gl.glMatrixMode(GL2.GL_MODELVIEW);

   gl.glClearColor(0f,0f,0f,1f);
   gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);
   gl.glEnable(GL.GL_DEPTH_TEST);
   gl.glEnable(GL.GL_BLEND);
   gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);

   double maxc = Math.max(x_bounds.getSpan(),y_bounds.getSpan());
   maxc = Math.max(maxc,z_bounds.getSpan());

   gl.glScaled(1/maxc,-1/maxc,1/maxc);
   gl.glTranslated(-x_bounds.getCenter(),-y_bounds.getCenter(),-z_bounds.getCenter());

   drawObject(gl,base_surface);
   
   for (SimObject so : objset) {
      drawObject(gl,so);
    }

   gl.glFlush();

   long when = the_simulator.getTime().getTime();
   Date date = new Date(when);
   message_area.setText(date.toString());
}



private void drawObject(GL2 gl,SimObject obj) 
{
   if (obj == null) return;
   
   gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_TRANSFORM_BIT);
   gl.glPushMatrix();
   obj.draw(gl);
   gl.glPopMatrix();
   gl.glPopAttrib();
}



public void displayChanged(GLAutoDrawable d,boolean mode,boolean dev) { }


@Override public void dispose(GLAutoDrawable d) { }

@Override public void init(GLAutoDrawable d)
{
   GL2 gl = (GL2) d.getGL();
   try {
      PrintStream ps = new PrintStream("/research/people/spr/upod/sim/src/gltrace.out");
      gl = (GL2) d.setGL(new TraceGL2(gl,ps));
    }
   catch (IOException e) { }
   gl.glClearColor(0f,0f,0f,1f);
   gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);
   gl.glEnable(GL.GL_DEPTH_TEST);
   gl.glEnable(GL.GL_CULL_FACE);

   computeBounds();

   is_inited = true;

   if (gl_thread != null) gl_thread.start();
}

@Override public void reshape(GLAutoDrawable d,int x,int y,int wd,int ht)
{
   input_manager.setup(wd,ht);
   d.getGL().glViewport(0,0,wd,ht);
}


UsimLocation viewScale(UsimLocation loc)
{
   if (x_bounds == null) return loc;
   
   double maxc = Math.max(x_bounds.getSpan(),y_bounds.getSpan());
   maxc = Math.max(maxc,z_bounds.getSpan());
   
   SimLocation nloc = new SimLocation(
         (loc.getX() - x_bounds.getCenter()) / maxc,
         (loc.getY() - y_bounds.getCenter()) / maxc * -1,
         (loc.getZ() - z_bounds.getCenter()) / maxc);
   
   return nloc;
}



private void computeBounds()
{
   int nobj = 0;
   double xmin = 0;
   double xmax = 0;
   double ymin = 0;
   double ymax = 0;
   double zmin = 0;
   double zmax = 0;
   for (SimObject so : the_simulator.getObjects()) {
      if (so.isMovable()) continue;
      Rectangle2D r2 = so.getXYBounds();
      Bounds zb = so.getZBounds();
      if (nobj == 0) {
	 xmin = r2.getMinX();
	 xmax = r2.getMaxX();
	 ymin = r2.getMinY();
	 ymax = r2.getMaxY();
	 zmin = zb.getMinValue();
	 zmax = zb.getMaxValue();
       }
      else {
	 xmin = Math.min(xmin,r2.getMinX());
	 xmax = Math.max(xmax,r2.getMaxX());
	 ymin = Math.min(ymin,r2.getMinY());
	 ymax = Math.max(ymax,r2.getMaxY());
	 zmin = Math.min(zmin,zb.getMinValue());
	 zmax = Math.max(zmax,zb.getMaxValue());
       }
      ++nobj;
    }

   x_bounds = new Bounds(xmin,xmax);
   y_bounds = new Bounds(ymin,ymax);
   z_bounds = new Bounds(zmin,zmax);
}




/********************************************************************************/
/*										*/
/*	Drawing Thread								*/
/*										*/
/********************************************************************************/

private class DrawThread extends Thread {

   private long last_draw;

   DrawThread() {
      super("Sim GL Drawing Thread");
      last_draw = 0;
      setDaemon(true);
    }

   @Override public void run() {
      long frametime = 1000 / FRAMES_PER_SECOND;
      long delay = 0;
   
      for ( ; ; ) {
         long t = System.currentTimeMillis();
         if (last_draw != 0) {
            long dt = t - last_draw;
            delay += frametime - dt;
            if (delay > 50) {
               try {
        	  Thread.sleep(delay-11,500000);
        	}
               catch (InterruptedException e) { }
               long t1 = t;
               t = System.currentTimeMillis();
               delay -= t-t1;
               if (delay < 0) delay = 0;
             }
          }
         last_draw = t;
         drawing_area.display();
       }
    }

}	// end of inner class DrawThread



/********************************************************************************/
/*										*/
/*	Camera implementation							*/
/*										*/
/********************************************************************************/

private static class SimCamera {

   protected UsimLocation	look_at;
   protected UsimLocation	camera_at;
   protected UsimLocation	camera_up;
   protected UsimLocation	camera_right;
   private double	aspect_ratio;
   protected double	cur_angle;
   private int		window_width;
   private int		window_height;
   protected boolean	do_perspective;
   private GLU          glu_object;
   
   private static double DEFAULT_ANGLE = 16.0;
   private static double MIN_ANGLE = 0.01;

   SimCamera() {
      aspect_ratio = 0;
      window_width = 0;
      window_height = 0;
      glu_object = new GLU();
      resetSettings();
    }

   void setup(int w,int h) {
      window_width = w;
      window_height = h;
      aspect_ratio = ((double)w)/((double)h);
      resetSettings();
    }


   void resetSettings() {
      double y0 = -20;
      double z0 = 10;
      double d = Math.sqrt(y0*y0 + z0*z0);
      double y1 = z0/d;
      double z1 = -y0/d;
   
      look_at = new SimLocation(0,0,0);
      camera_at = new SimLocation(0,y0,z0);
   
      camera_up = new SimLocation(0,y1,z1);
      camera_right = new SimLocation(1,0,0);
      cur_angle = DEFAULT_ANGLE/d*6;
      do_perspective = true;
      // dumpCamera("RESET");
    }

   void updateSettings()                        { }
   int getWindowWidth() 			{ return window_width; }
   int getWindowHeight()			{ return window_height; }

   void toggleCameraType() {
      do_perspective = !do_perspective;
    }

   void panLeftRight(double d) {
      d *= cur_angle / DEFAULT_ANGLE;
      look_at = look_at.linearOp(camera_right,-d);
      camera_at = camera_at.linearOp(camera_right,d);
      // dumpCamera("PAN LR " + d);
    }

   void panUpDown(double d) {
      d *= cur_angle / DEFAULT_ANGLE;
      look_at = look_at.linearOp(camera_up,-d);
      camera_at = camera_at.linearOp(camera_up,d);
      // dumpCamera("PAN UD " + d);
    }

   void zoomInOut(double d) {
      if (d == 0) cur_angle = DEFAULT_ANGLE;
      else cur_angle += d;
      if (cur_angle < MIN_ANGLE) cur_angle = MIN_ANGLE;
      // dumpCamera("ZOOM d");
    }

   void rotateUpDown(double deg) {
      doRotate(camera_right,deg);
      // dumpCamera("ROT UD " + deg);
    }

   void rotateLeftRight(double deg) {
      doRotate(camera_up,deg);
      // dumpCamera("ROT LR " + deg);
    }

   void draw(GL2 gl) {
      // System.err.println("CAM " + camera_at + " " + look_at + " " + camera_up + " " + cur_angle + " " + aspect_ratio);
      gl.glViewport(0,0,window_width,window_height);
      glu_object.gluLookAt(camera_at.getX(),camera_at.getY(),camera_at.getZ(),
            look_at.getX(),look_at.getY(),look_at.getZ(),
            camera_up.getX(),camera_up.getY(),camera_up.getZ());
      gl.glMatrixMode(GL2.GL_PROJECTION);
      gl.glLoadIdentity();
      if (do_perspective) {
         glu_object.gluPerspective(cur_angle,aspect_ratio,0.05,100.0);
       }
      else {
         float v = (float)(cur_angle/DEFAULT_ANGLE/2);
         v = 10;
         gl.glOrthof(-v,v,-v,v,-1,1);
       }
      gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

   private void doRotate(UsimLocation v,double deg) {
      SimMatrix m1 = new SimMatrix();
      SimMatrix m2 = new SimMatrix();
      m1.translate(look_at,-1);
      m2.rotateDeg(v,deg);
      m1.leftMult(m2);
      m2.translate(look_at,1);
      m1.leftMult(m2);
   
      SimLocation v1 = m1.leftMultBy(camera_at);
      camera_up = adjust(camera_up,m1,v1);
      camera_right = adjust(camera_right,m1,v1);
      camera_at = v1;
    }

   private UsimLocation adjust(UsimLocation v,SimMatrix m,SimLocation nat) {
      UsimLocation v1 = camera_at.linearOp(v,1);
      v1 = m.leftMultBy(v1);
      v1 = v1.linearOp(nat,-1);  // check this
      v1 = v1.normalize();
      return v;
    }

   // protected void dumpCamera(String what) {
      // if (what == null) what = "";
      // System.err.println("CAMERA: [" + what + "] " +
	    // camera_at + " " + camera_up + " " + camera_right + " " + cur_angle);
    // }

}	// end of inner class SimCamera


private static class SimObjectCamera extends SimCamera {
   
   private SimDisplay for_display;
   private SimObject for_object;
   
   private static double VIEW_ANGLE = 60;
   
   SimObjectCamera(SimDisplay disp,SimObject obj) {
      super();
      for_display = disp;
      for_object = obj;
      resetSettings();
    }
   
   @Override void resetSettings() {
      if (for_object == null) return;
      
      look_at = for_display.viewScale(for_object.getCameraLookAt());
      camera_at = for_display.viewScale(for_object.getCameraPosition());
      camera_up = for_object.getCameraUp();
      camera_right = for_object.getCameraRight();
      
      
      cur_angle = VIEW_ANGLE;
      cur_angle = 60;
      do_perspective = true;
      // dumpCamera("RESET");
    }
   
   @Override void updateSettings() {
      if (for_object == null) return;
      
      look_at = for_display.viewScale(for_object.getCameraLookAt());
      camera_at = for_display.viewScale(for_object.getCameraPosition());
      camera_right = for_object.getCameraRight();
      
      // BasisLogger.logD("AT " + camera_at);
      
      // look_at = new SimLocation(0,0,0);
      // camera_at = new SimLocation(0,-1.2,0);
      // camera_up = new SimLocation(0,1,2);
      // camera_up = camera_up.normalize();
    }
   
}       // end of inner class SimObjectCamera



/********************************************************************************/
/*										*/
/*	User interface class							*/
/*										*/
/********************************************************************************/

private static class SimSpinner implements MouseListener, MouseMotionListener, KeyListener  {

   private enum SpinType { NONE, ROTATE, MOVE, ZOOM };

   private SimDisplay   for_display;
   private SimCamera	the_camera;
   private SimCamera    object_camera;
   private SpinType	spin_type;
   private int		spin_width;
   private int		spin_height;
   private double	spin_yz;
   private double	spin_zx;
   private SimObject    cur_object;
   private boolean      use_object;
   private boolean      use_objectcam;

   private final static double PAN_SCALE = 2.0;
   private final static double ZOOM_SCALE = 10.0;

   SimSpinner(SimDisplay disp,Component c) {
      for_display = disp;
      the_camera = new SimCamera();
      object_camera = null;
      spin_type = SpinType.NONE;
      c.addMouseListener(this);
      c.addMouseMotionListener(this);
      c.addKeyListener(this);
      cur_object = null;
      use_object = false;
      use_objectcam = false;
    }
   
   SimCamera getCamera() {    
      if (use_objectcam) return object_camera;
      return the_camera; 
    }
   
   void setup(int wd,int ht) {
      the_camera.setup(wd,ht);
      if (object_camera != null)
         object_camera.setup(wd,ht);
    }
   
   void setObject(SimObject obj) {
      cur_object = obj;
      object_camera = new SimObjectCamera(for_display,obj);
      if (the_camera.getWindowWidth() != 0) {
         object_camera.setup(the_camera.getWindowWidth(),
               the_camera.getWindowHeight());
       }
    }
  
   @Override public void mouseClicked(MouseEvent e)		{ }
   @Override public void mouseEntered(MouseEvent e)		{ }
   @Override public void mouseExited(MouseEvent e)		{ }

   @Override public void mousePressed(MouseEvent e) {
      if (spin_type != SpinType.NONE) handleSpinEvent(e);
      else if (e.getButton() == MouseEvent.BUTTON3) startSpinEvent(e);
    }

   @Override public void mouseReleased(MouseEvent e) {
      if (spin_type != SpinType.NONE && e.getButton() == MouseEvent.BUTTON3) {
	 spin_type = SpinType.NONE;
       }
    }

   @Override public void mouseDragged(MouseEvent e) {
      if (spin_type != SpinType.NONE) handleSpinEvent(e);
    }
   @Override public void mouseMoved(MouseEvent e) {
      if (spin_type != SpinType.NONE) handleSpinEvent(e);
    }

   @Override public void keyPressed(KeyEvent e) {
      if (spin_type == SpinType.NONE) handleKey(e);
    }

   @Override public void keyTyped(KeyEvent e) {
      if (spin_type == SpinType.NONE) handleKey(e);
    }

   @Override public void keyReleased(KeyEvent e)		{ }

   private void startSpinEvent(MouseEvent me) {
      spin_width = the_camera.getWindowWidth();
      spin_height = the_camera.getWindowHeight();
      spin_yz = me.getX();
      spin_zx = me.getY();
      if (me.isShiftDown()) spin_type = SpinType.MOVE;
      else if (me.isControlDown()) spin_type = SpinType.ZOOM;
      else {
	 spin_type = SpinType.ROTATE;
	 spinAngles(me.getX(),me.getY());
       }
    }

   private void handleSpinEvent(MouseEvent me) {
      double oldyz = spin_yz;
      double oldzx = spin_zx;
      spin_yz = me.getX();
      spin_zx = me.getY();

      switch (spin_type) {
	 case NONE :
	    break;
	 case ROTATE :
	    spinAngles(me.getX(),me.getY());
	    doSpin(spin_yz - oldyz,spin_zx - oldzx);
	    break;
	 case MOVE :
	    if (spin_yz != oldyz)
	       the_camera.panLeftRight(-PAN_SCALE*(oldyz-spin_yz)/spin_width);
	    if (spin_zx != oldzx)
	       the_camera.panUpDown(PAN_SCALE*(oldzx-spin_zx)/spin_height);
	    break;
	 case ZOOM :
	    if (spin_yz != oldyz)
	       the_camera.zoomInOut(ZOOM_SCALE*(oldyz-spin_yz)/spin_width);
	    if (spin_zx != oldzx)
	       the_camera.zoomInOut(-ZOOM_SCALE*(oldzx-spin_zx)/spin_height);
	    break;
       }
    }

   private void spinAngles(int ix,int iy) {
      double x = (ix - spin_width/2.0)/spin_width;
      double y = (iy - spin_height/2.0)/spin_height;
      double z = 1 - x*x - y*y;
      if (z <= 0) return;
      z = Math.sqrt(z);
      spin_yz = Math.atan2(y,z);
      spin_zx = Math.atan2(z,x);
    }

   private void doSpin(double ud,double lr) {
      if (ud != 0) the_camera.rotateUpDown(-ud * 180 / Math.PI);
      if (lr != 0) the_camera.rotateLeftRight(lr * 180 / Math.PI);
    }

   private void handleKey(KeyEvent e) {
      switch (e.getKeyCode()) {
         case KeyEvent.VK_HOME :
            if (use_objectcam) object_camera.resetSettings();
            else the_camera.resetSettings();
            break;
         case KeyEvent.VK_F12 :
            the_camera.toggleCameraType();
            break;
         case KeyEvent.VK_PLUS :
         case KeyEvent.VK_ADD :
            if (cur_object != null && !use_object) use_object = true;
            else use_object = false;
            break;
         case KeyEvent.VK_MINUS :
         case KeyEvent.VK_SUBTRACT :
            if (cur_object != null && !use_objectcam) use_objectcam = true;
            else use_objectcam = false;
            break;
         case KeyEvent.VK_UP :
         case KeyEvent.VK_KP_UP :
            if (use_object) {
               cur_object.moveDelta(true);
             }
            else if (use_objectcam) {
               // tilt head up
             }
            else {
               the_camera.rotateUpDown(30.0);
             }
            break;
         case KeyEvent.VK_DOWN :
         case KeyEvent.VK_KP_DOWN :
            if (use_object) {
               cur_object.moveDelta(false);
             }
            else if (use_objectcam) {
               // tilt head down
             }
            else {
               the_camera.rotateUpDown(-30.0);
             }
            break;
         case KeyEvent.VK_LEFT :
         case KeyEvent.VK_KP_LEFT :
            if (use_object) {
               cur_object.rotateDelta(true);
             }
            else if (use_objectcam) {
               // tilt head left
             }
            else {
               the_camera.rotateLeftRight(30.0);
             }
            break;
         case KeyEvent.VK_RIGHT :
         case KeyEvent.VK_KP_RIGHT :
            if (use_object) {
               cur_object.rotateDelta(false);
             }
            else if (use_objectcam) {
               // tilt head right
             }
            else {
               the_camera.rotateLeftRight(-30.0);
             }
            break;
         case KeyEvent.VK_PAGE_UP :
            the_camera.zoomInOut(-2);
            break;
         case KeyEvent.VK_PAGE_DOWN :
            the_camera.zoomInOut(2);
            break;
         default :
            break;
       }
    }
}	// end of inner class SimSpinner


/********************************************************************************/
/*										*/
/*	Class for matrix computations						*/
/*										*/
/********************************************************************************/

private static class SimMatrix {

   private double [] [] m_v;

   SimMatrix() {
      m_v = new double[4][4];
      zero();
    }

   void zero() {
      for (int i = 0; i < 4; ++i) {
	 for (int j = 0; j < 4; ++j) m_v[i][j] = 0;
       }
    }

   void identity() {
      zero();
      for (int i = 0; i < 4; ++i) m_v[i][i] = 1.0;
    }

   void translate(UsimLocation loc,double scale) {
      identity();
      m_v[3][0] = loc.getX()*scale;
      m_v[3][1] = loc.getY()*scale;
      m_v[3][2] = loc.getZ()*scale;
    }

   void rotateDeg(UsimLocation v0,double deg) {
      rotateRad(v0,Math.toRadians(deg));
    }

   void rotateRad(UsimLocation v0,double rad) {
      UsimLocation v0n = v0.normalize();
      double u = v0n.getX();
      double v = v0n.getY();
      double w = v0n.getZ();
      double u2 = u*u;
      double v2 = v*v;
      double w2 = w*w;
      double uv = u*v;
      double uw = u*w;
      double vw = v*w;
      double cosr = Math.cos(rad);
      double sinr = Math.sin(rad);
      identity();
      m_v[0][0] = u2 + (1-u2)* cosr;
      m_v[1][0] = uv * (1-cosr) - w*sinr;
      m_v[2][0] = uw * (1-cosr) + v*sinr;
      m_v[0][1] = uv * (1-cosr) + w*sinr;
      m_v[1][1] = v2 + (1-v2)*cosr;
      m_v[2][1] = vw*(1-cosr) - u*sinr;
      m_v[0][2] = uw*(1-cosr) - v*sinr;
      m_v[1][2] = vw*(1-cosr) + u*sinr;
      m_v[2][2] = w2+(1-w2)*cosr;
    }

   void leftMult(SimMatrix m)		{ multiply(m,this); }

   private void multiply(SimMatrix m1,SimMatrix m2) {
      double [][] r = new double[4][4];
      for (int i = 0; i < 4; ++i) {
	 for (int j = 0; j < 4; ++j) {
	    double v = 0;
	    for (int k = 0; k < 4; ++k)
	       v += m1.m_v[i][k] * m2.m_v[k][j];
	    r[i][j] = v;
	  }
       }
      for (int i = 0; i < 4; ++i) {
	 for (int j = 0; j < 4; ++j)
	    m_v[i][j] = r[i][j];
       }
    }

    SimLocation leftMultBy(UsimLocation v) {
       double rx = v.getX() * m_v[0][0] + v.getY() * m_v[1][0] + v.getZ() * m_v[2][0] + m_v[3][0];
       double ry = v.getX() * m_v[0][1] + v.getY() * m_v[1][1] + v.getZ() * m_v[2][1] + m_v[3][1];
       double rz = v.getX() * m_v[0][2] + v.getY() * m_v[1][2] + v.getZ() * m_v[2][2] + m_v[3][2];
       return new SimLocation(rx,ry,rz);
     }

}	// end of inner class SimMatrix



}	// end of class SimDisplay




/* end of SimDisplay.java */

