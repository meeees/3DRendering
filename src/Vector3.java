
public class Vector3 {
	
	private double x, y, z;
	
	public Vector3() {
		this(0f, 0f, 0f);
	}
	
	public Vector3(double x, double y) {
		this(x, y, 0f);
	}
	
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setPos(Vector3 v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setZ(double z) {
		this.z = z;
	}
	
	public Vector3 add(Vector3 v) {
		return new Vector3(x + v.x, y + v.y, z + v.z);
	}
	
	public void move(Vector3 v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	public double getX() {
		return x;	
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public static Vector3 getZero() {
		return new Vector3();
	}
	
	public double dot(Vector3 other) {
		return x * other.x + y * other.y + z * other.z;
	}
	
	public double mag() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public Vector3 normalize() {
		double mag = mag();
		return new Vector3(x / mag, y / mag, z / mag);
	}
	
	public double distance(Vector3 other) {
		return Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z));
	}
	
	public Vector3 cross(Vector3 other) {
		double x = this.y * other.z - this.z * other.y;
		double y = this.z * other.x - this.x * other.z;
		double z = this.x * other.y - this.y * other.x;
		return new Vector3(x, y, z);
	}

}
