
public class AtomicSalt {

	private byte[] salt;

	public synchronized void set(byte[] salt) {
		this.salt = salt.clone();
		
	}

	public synchronized byte[] get() {
		return salt.clone();
	}

}
