package io.github.danthe1st.jvmyieldreturn;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import jdk.internal.vm.Continuation;

class YieldedIterator<T> implements Iterator<T> {

	private final Continuation continuation;
	private final AtomicReference<T> ref;

	private IterationElement<T> next;

	YieldedIterator(AtomicReference<T> ref, Continuation continuation) {
		this.continuation = continuation;
		this.ref = ref;
	}

	@Override
	public boolean hasNext() {
		if (next != null) {
			return true;
		}
		if (continuation.isDone()) {
			return false;
		}
		continuation.run();
		if (continuation.isDone()) {
			return false;
		}
		next = new IterationElement<>(ref.get());
		return true;
	}

	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		T elem = next.element();
		next = null;
		return elem;
	}
}

record IterationElement<T>(T element) {
}