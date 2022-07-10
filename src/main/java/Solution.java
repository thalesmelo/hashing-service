public abstract class Solution {
	private Source source;
	private Sink sink;

	public Solution(Source source, Sink sink) {
		this.source = source;
		this.sink = sink;
	}

	public void start() {
		Observer observer = createObserver(sink);
		source.subscribe(observer);
	}

	protected abstract Observer createObserver(Sink sink); // implement this
}