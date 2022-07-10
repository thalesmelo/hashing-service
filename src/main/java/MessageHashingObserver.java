import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageHashingObserver implements Observer {

	private static final int HASHING_ITERACTIONS = 5000;
	private static final byte[] PIPES = "||".getBytes(StandardCharsets.UTF_8);
	private Sink sink;
	private AtomicSalt saltProvider = new AtomicSalt();

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

	private synchronized byte[] hash(byte[] message, byte[] salt) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
		byte[] hashedMessage = sha256(md, message, salt);
		for (int i = 0; i < HASHING_ITERACTIONS - 1; i++) {
			hashedMessage = sha256(md, hashedMessage, salt);
		}
		return hashedMessage;
	}

	private synchronized byte[] sha256(MessageDigest md, byte[] message, byte[] salt) {
		md.reset();
		md.update(bytesConcat(message, salt));
		return md.digest();
	}

	private byte[] bytesConcat(byte[] message, byte[] salt) {
		return ByteBuffer.allocate(message.length + salt.length).put(message).put(salt).array();
	}

}