package io.github.danthe1st.jvmyieldreturn;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

public class Yielder<T> {
	private final AtomicReference<T> ref;
	private final ContinuationScope scope;

	private Yielder(ContinuationScope scope, AtomicReference<T> ref) {
		this.ref = ref;
		this.scope = scope;
	}

	public void yield(T value) {
		ref.set(value);
		Continuation.yield(scope);
	}

	public static <T> Iterable<T> create(Consumer<Yielder<T>> fun) {
		return () -> {
			ContinuationScope scope = new ContinuationScope("yieldReturn");
			AtomicReference<T> ref = new AtomicReference<>();

			Yielder<T> yielder = new Yielder<>(scope, ref);

			Continuation con = new Continuation(scope, () -> fun.accept(yielder));
			return new YieldedIterator<>(ref, con);
		};
	}
}