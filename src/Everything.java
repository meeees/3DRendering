import java.awt.Canvas;
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
	private int[] pixels;
	private BufferedImage screen;
	private double fieldOfView;
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Everything");
		frame.setSize(WIDTH + 6, HEIGHT + 28);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		Everything e = new Everything();
		frame.add(e);
		frame.setLayout(null);
		e.setBounds(0, 0, WIDTH, HEIGHT);
		frame.setVisible(true);
		frame.repaint();
		frame.setIgnoreRepaint(true);
		e.doThings();
	}
	
	public void doThings() {
		screen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		pixels = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
		fieldOfView = Math.toRadians(60);
		center = new Vector3(WIDTH / 2, HEIGHT / 2);
		addKeyListener(this);
		requestFocus();
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
		}
	}
	
	public void update() {
		fieldOfView = Math.toRadians(60);
	}
	
	private void clearScreen() {
		Arrays.fill(pixels, 0xff000000);
	}
	
	private void renderPoint(Vector3 point) {
		double dx = Math.cos(fieldOfView / 2.0) / Math.sin(fieldOfView / 2.0);
		//System.out.printf("%.5f : ", dx);
		double xCord = (dx * (point.getX() - center.getX())) / (point.getZ() - center.getZ()) + center.getX();
		double yCord = (dx * (point.getY() - center.getY())) / (point.getZ() - center.getZ()) + center.getY();
		//System.out.printf("%.2f : %.2f\n", xCord, yCord);
		if(xCord >= 0 && xCord < WIDTH && yCord >= 0 && yCord < HEIGHT) {
			
			pixels[(int) yCord * WIDTH + (int) xCord] = 0xffff0000;
		}
	}
	
	private void renderThings() {
		renderPoint(new Vector3(200, 200, 1));
		renderPoint(new Vector3(200, 200 + 50, 1));
		renderPoint(new Vector3(200, 200, 1.5));
		renderPoint(new Vector3(200, 200 + 50, 1.5));
		renderPoint(new Vector3(200, 200, 2));
		renderPoint(new Vector3(200, 200 + 50, 2));
		renderPoint(new Vector3(200, 200, 2.5));
		renderPoint(new Vector3(200, 200 + 50, 2.5));

	}
	
	public void render(Graphics2D g) {
		clearScreen();
		renderThings();
		g.drawImage(screen, 0, 0, null);
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
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
