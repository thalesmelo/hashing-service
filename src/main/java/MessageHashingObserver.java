import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;

public class MessageHashingObserver implements Observer {

	private static MessageDigest md;
	static {
		try {
			// This is expensive so better reuse for processing of many messages
			md = MessageDigest.getInstance("SHA-256"); // TODO: maybe switch for a more cryptographic strong algorithm.
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static final int HASHING_ITERACTIONS = 5000;
	private Sink sink;
	private AtomicSalt saltProvider = new AtomicSalt();

	private ReentrantLock lock = new ReentrantLock();

	public MessageHashingObserver(Sink sink) {
		this.sink = sink;
	}

	@Override
	public void onSalt(byte[] salt) {
		this.saltProvider.set(salt);
	}

	@Override
	public void onMessage(long id, byte[] message) {
		// No chance to have the salt changed during the calculation.
		// nor we have to lock salt the whole method execution, because that would not
		// be efficient.
		// So we get a copy and free the salt provider to be updated, without compromise
		// our calculation.
		// Plus we get thread safety due to the nature of local scope
		byte[] salt = saltProvider.get();
		sink.publishHash(id, message, salt, hash(message, salt));
	}

	private byte[] hash(byte[] message, byte[] salt) {
		lock.lock();
		try {
			byte[] hashedMessage = message;
			for (int i = 0; i < HASHING_ITERACTIONS; i++) {
				hashedMessage = sha256(md, hashedMessage, salt);
			}
			return hashedMessage;
		} finally {
			lock.unlock();
		}
	}

	private byte[] sha256(MessageDigest md, byte[] message, byte[] salt) {
		md.reset();
		md.update(bytesConcat(message, salt));
		return md.digest();
	}

	// TODO: Look for the fastest way to copy this array
	private byte[] bytesConcat(byte[] message, byte[] salt) {
		byte[] result = new byte[message.length + salt.length];
		System.arraycopy(message, 0, result, 0, message.length);
		System.arraycopy(salt, 0, result, message.length, salt.length);
		return result;
	}

}
