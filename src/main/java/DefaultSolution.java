
public class DefaultSolution extends Solution {

	public DefaultSolution(Source source, Sink sink) {
		super(source, sink);
	}

	@Override
	protected Observer createObserver(Sink sink) {
		return new MessageHashingObserver(sink);
	}

}
