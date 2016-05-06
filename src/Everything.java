import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import javax.swing.JFrame;

//this class name will probably be changed later
public class Everything extends Canvas implements KeyListener {
	
	//some stuff java likes to have
	private static final long serialVersionUID = 1L;
	//width and height of the render area, the actual frame will be twice this size
	static final int WIDTH = 600, HEIGHT = 480;
	//camera position in x,y,z
	private Vector3 camPos;
	//camera postition in x,y,z (roll,pitch,yaw)
	//currently only support for yaw rotation
	private Vector3 camRot;
	//field of view of the camera
	private double fieldOfView;
	//the data for the screen image
	private int[] pixels;
	//the actual BufferedImage object being used as a screen
	private BufferedImage screen;
	//frame count from last second
	private int frames;
	//current time in milliseconds
	private long curTime;
	//time since the last second has elapsed
	private long culmTime;
	
	public static void main(String args[]) {
		//JFrame setup
		JFrame frame = new JFrame("Everything");
		
		//uses + 6 and + 28 because those are the offsets of the frame decorations on my machine, could probably be made more elegant
		//uses * 2 because the screen image gets streched to make seeing dots easier
		frame.setSize(WIDTH * 2 + 6, HEIGHT * 2 + 28);
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		Everything e = new Everything();
		frame.add(e);
		frame.setLayout(null);
		e.setBounds(0, 0, WIDTH * 2, HEIGHT * 2);
		frame.setVisible(true);
		frame.repaint();
		frame.setIgnoreRepaint(true);
		e.mainLoop();
	}
	
	public void mainLoop() {
		int fps = 0;
		screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		//this array of ints is tied to the screen data, so drawing the image will draw the data represented by this array
		pixels = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
		
		//initialize the camera at the center and with no rotation
		fieldOfView = Math.toRadians(90);
		camPos = new Vector3(WIDTH / 2, HEIGHT / 2);
		camRot = new Vector3();
		
		//use implemented KeyListener methods from this class
		addKeyListener(this);
		//requests for the Canvas to be taking input from devices
		requestFocus();
		
		curTime = System.currentTimeMillis();
		
		//actual main logic loop
		while(true) {
			BufferStrategy bs = getBufferStrategy();
			if(bs == null) {
				createBufferStrategy(3);
				continue;
			}
			Graphics2D g = (Graphics2D) bs.getDrawGraphics();
			render(g);
			update();
			bs.show();
			fps += 1;
			long nowTime = System.currentTimeMillis();
			culmTime += nowTime - curTime;
			if(culmTime > 1000) {
				culmTime -= 1000;
				frames = fps;
				fps = 0;
			}
			curTime = nowTime;
		}
	}
	
	//any non-rendering and non-input logic should go here, currently nothing
	public void update() {

	}
	
	//fills the pixels array with black
	private void clearScreen() {
		Arrays.fill(pixels, 0xff000000);
	}
	
	//render a single point, using the pixels array as the screen
	private void renderPoint(Vector3 point) {
		double dx = Math.cos(fieldOfView / 2.0) / Math.sin(fieldOfView / 2.0);
		double xT = point.getX() - camPos.getX();
		xT /= WIDTH / 2;
		double yT = point.getY() - camPos.getY();
		yT /= HEIGHT / 2;
		double zT = point.getZ() - camPos.getZ();
		double theta = Math.atan2(xT, zT);
		theta += camRot.getZ();
		//System.out.println(Math.toDegrees(theta));
		double dist = Math.sqrt(xT * xT + zT * zT);
		zT = dist * Math.cos(theta);
		xT = dist * Math.sin(theta);
		xT *= WIDTH / 2;
		yT *= HEIGHT / 2;
		System.out.println(xT + " : " + zT);
		if(zT < 0) {
			return;
		}
		double xCord = (dx * (xT / (zT))) + camPos.getX();
		//xCord += xCord * Math.cos(rot.getZ()) - xCord;
		double yCord = (dx * (yT / zT)) + camPos.getY();
		//System.out.printf("%.2f : %.2f\n", xCord, yCord);
		if(xCord >= 0 && xCord < WIDTH && yCord >= 0 && yCord < HEIGHT) {
			
			pixels[(int) yCord * WIDTH + (int) xCord] = 0xffff0000;
		}
	}
	
	private void renderThings() {
		for(double i = 0.5; i < 5.0; i+=0.5) {
			renderPoint(new Vector3(200, 200, i));
			renderPoint(new Vector3(200, 200 + 50, i));
		}
		renderPoint(new Vector3(WIDTH / 2, HEIGHT / 2, 2));

	}
	
	public void render(Graphics2D g) {
		clearScreen();
		renderThings();
		g.drawImage(screen, 0, 0, WIDTH * 2, HEIGHT * 2, null);
		g.setColor(Color.YELLOW);
		//draw some debug information on the bottom of the screen
		g.drawString(String.format("X: %.2f Y: %.2f Z: %.2f FOV: %.2f", camPos.getX(), camPos.getY(), camPos.getZ(), Math.toDegrees(fieldOfView)), 5, HEIGHT * 2 - 18);
		g.drawString(String.format("ZRot: %.2f FPS: %d", Math.toDegrees(camRot.getZ()), frames), 5, HEIGHT * 2 - 5);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//resets the camera location and rotation
		if(e.getKeyCode() == KeyEvent.VK_R) {
			camPos = new Vector3(WIDTH / 2, HEIGHT / 2, 0);
			camRot = new Vector3(0, 0, 0);
		}
		
		//handle input movement left-right, up-down, and in-out
		if(e.getKeyCode() == KeyEvent.VK_A) {
			camPos.move(new Vector3(-3, 0, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			camPos.move(new Vector3(3, 0, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			camPos.move(new Vector3(0, -3, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_Q) {
			camPos.move(new Vector3(0, 3, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_W) {
			camPos.move(new Vector3(0, 0, 0.03));
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			camPos.move(new Vector3(0, 0, -0.03));
		}
		
		//change the field of view
		if(e.getKeyCode() == KeyEvent.VK_Z) {
			fieldOfView += 0.02;
			if(fieldOfView > Math.PI) {
				fieldOfView = Math.PI;
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_X) {
			fieldOfView -= 0.02;
			if(fieldOfView < 0) {
				fieldOfView = 0;
			}
		}
		
		//handle input for rotation, will be moved to mouse input at some point
		if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
			camRot.move(new Vector3(0, 0, -Math.PI / 180));
			if(camRot.getZ() < 0) {
				camRot.move(new Vector3(0, 0, Math.PI * 2));
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
			camRot.move(new Vector3(0, 0, Math.PI / 180));
			if(camRot.getZ() > Math.PI * 2) {
				camRot.move(new Vector3(0, 0, -Math.PI * 2));
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
