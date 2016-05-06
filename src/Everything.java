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


public class Everything extends Canvas implements KeyListener {
	
	private static final long serialVersionUID = 1L;
	static final int WIDTH = 600, HEIGHT = 480;
	private Vector3 center;
	private Vector3 rot;
	private int[] pixels;
	private BufferedImage screen;
	private double fieldOfView;
	private int frames;
	private long curTime;
	private long culmTime;
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Everything");
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
		e.doThings();
	}
	
	public void doThings() {
		int fps = 0;
		screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		pixels = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
		fieldOfView = Math.toRadians(90);
		center = new Vector3(WIDTH / 2, HEIGHT / 2);
		rot = new Vector3(0f, 0f);
		addKeyListener(this);
		requestFocus();
		curTime = System.currentTimeMillis();
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
	
	public void update() {

	}
	
	private void clearScreen() {
		Arrays.fill(pixels, 0xff000000);
	}
	
	private void renderPoint(Vector3 point) {
		double dx = Math.cos(fieldOfView / 2.0) / Math.sin(fieldOfView / 2.0);
		double xT = point.getX() - center.getX();
		xT /= WIDTH / 2;
		double yT = point.getY() - center.getY();
		yT /= HEIGHT / 2;
		double zT = point.getZ() - center.getZ();
		double theta = Math.atan2(xT, zT);
		theta += rot.getZ();
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
		double xCord = (dx * (xT / (zT))) + center.getX();
		//xCord += xCord * Math.cos(rot.getZ()) - xCord;
		double yCord = (dx * (yT / zT)) + center.getY();
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
		g.drawString(String.format("X: %.2f Y: %.2f Z: %.2f FOV: %.2f", center.getX(), center.getY(), center.getZ(), Math.toDegrees(fieldOfView)), 5, HEIGHT * 2 - 18);
		g.drawString(String.format("ZRot: %.2f FPS: %d", Math.toDegrees(rot.getZ()), frames), 5, HEIGHT * 2 - 5);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_A) {
			center.move(new Vector3(-3, 0, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			center.move(new Vector3(3, 0, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_E) {
			center.move(new Vector3(0, -3, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_Q) {
			center.move(new Vector3(0, 3, 0));
		}
		if(e.getKeyCode() == KeyEvent.VK_W) {
			center.move(new Vector3(0, 0, 0.03));
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			center.move(new Vector3(0, 0, -0.03));
		}
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
		if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
			rot.move(new Vector3(0, 0, -Math.PI / 180));
			if(rot.getZ() < 0) {
				rot.move(new Vector3(0, 0, Math.PI * 2));
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
			rot.move(new Vector3(0, 0, Math.PI / 180));
			if(rot.getZ() > Math.PI * 2) {
				rot.move(new Vector3(0, 0, -Math.PI * 2));
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
