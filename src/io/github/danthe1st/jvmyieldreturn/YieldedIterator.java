package io.github.danthe1st.jvmyieldreturn;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import jdk.internal.vm.Continuation;

class YieldedIterator<T> implements Iterator<T> {

	private final Continuation continuation;
	private final AtomicReference<T> ref;

	YieldedIterator(AtomicReference<T> ref, Continuation continuation) {
		this.continuation = continuation;
		this.ref = ref;
	}

	@Override
	public boolean hasNext() {
		return !continuation.isDone();
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		continuation.run();
		return ref.get();
	}
}