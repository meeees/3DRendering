import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;


public class InputHandler implements KeyListener, MouseMotionListener {
	
	private Engine engine;
	
	private boolean fast = false;
	private boolean slow = false;
	private double mouseSense = 1;
	private int mouseX, mouseY;
	
	public InputHandler(Engine e)
	{
		engine = e;
	}
	
	//this needs to be redone later
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_R) {
			engine.notifyCamChange(new Vector3(Engine.WIDTH / 2, Engine.HEIGHT / 2, 0), new Vector3(0, Math.PI / 2, 0), Math.toRadians(90));
			return;
		}
		if(e.getKeyCode() == KeyEvent.VK_SHIFT)
		{
			fast = true;
			return;
		}
		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
		{
			slow = true;
			return;
		}
		Vector3 camPos = engine.getCamPos();
		Vector3 camRot = engine.getCamRot();
		double fieldOfView = engine.getFOV();
		//resets the camera location and rotation
		double speed = 3f;
		if(fast)
			speed *= 2;
		if(slow)
			speed /= 2;
		double speedZ = speed / 100;
		//handle input movement left-right, up-down, and in-out
		Vector3 dx = new Vector3(speed * Math.cos(camRot.getZ()) * Math.sin(camRot.getY()), 0, speedZ * Math.sin(camRot.getZ()) * Math.sin(camRot.getY()));
		Vector3 dz = new Vector3(-speed * Math.sin(camRot.getZ() * Math.sin(camRot.getY())), speed * Math.cos(camRot.getY()), speedZ * Math.cos(camRot.getZ()) * Math.sin(camRot.getY()));
		System.out.println(dz.getY());
		System.out.println(camRot.getY() + " : " + Math.cos(camRot.getY()));
		if(e.getKeyCode() == KeyEvent.VK_A) {
			camPos.move(dx.scale(-1));
		}
		else if(e.getKeyCode() == KeyEvent.VK_D) {
			camPos.move(dx);
		}
		else if(e.getKeyCode() == KeyEvent.VK_W) {
			camPos.move(dz);
		}
		else if(e.getKeyCode() == KeyEvent.VK_S) {
			camPos.move(dz.scale(-1));
		}
		else if(e.getKeyCode() == KeyEvent.VK_E)
		{
			camPos.move(new Vector3(0, speed, 0));
		}
		else if(e.getKeyCode() == KeyEvent.VK_Q)
		{
			camPos.move(new Vector3(0, -speed, 0));
		}
		
		//change the field of view
		else if(e.getKeyCode() == KeyEvent.VK_Z) {
			fieldOfView += 0.02;
			if(fieldOfView > Math.PI) {
				fieldOfView = Math.PI;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_X) {
			fieldOfView -= 0.02;
			if(fieldOfView < 0) {
				fieldOfView = 0;
			}
		}
		
		//handle input for rotation, will be moved to mouse input at some point
		else if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
			camRot.move(new Vector3(0, 0, -Math.PI / 180));
			if(camRot.getZ() < 0) {
				camRot.move(new Vector3(0, 0, Math.PI * 2));
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
			camRot.move(new Vector3(0, 0, Math.PI / 180));
			if(camRot.getZ() > Math.PI * 2) {
				camRot.move(new Vector3(0, 0, -Math.PI * 2));
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_MINUS) {
			camRot.move(new Vector3(0, Math.PI / 180, 0));
			if(camRot.getY() > Math.PI){
				camRot.setY(Math.PI);
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_EQUALS) {
			camRot.move(new Vector3(0, -Math.PI / 180, 0));
			if(camRot.getY() < 0){
				camRot.setY(0);
			}
		}
		engine.notifyCamChange(camPos, camRot, fieldOfView);
	}
	
	public void mouseDragged(MouseEvent e) {
		double deltaX = mouseX - e.getX();
		double deltaY = mouseY - e.getY();
		mouseX = e.getX();
		mouseY = e.getY();
		deltaX /= Engine.WIDTH;
		deltaY /= Engine.HEIGHT;
		deltaX *= mouseSense;
		deltaY *= mouseSense;
		engine.notifyCamChange(engine.getCamPos(), engine.getCamRot().add(new Vector3(0, deltaY, deltaX)), engine.getFOV());
	}

	public void keyReleased(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_SHIFT)
			fast = false;
		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
			slow = false;

	}

	public void keyTyped(KeyEvent e) {

	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}


}
