package io.github.danthe1st.jvmyieldreturn;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jdk.incubator.concurrent.ScopedValue;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

public abstract class YieldingSupplier<T> implements Supplier<T>, Iterable<T> {

	private final ScopedValue<ContinuationScope> SCOPE_HOLDER = ScopedValue.newInstance();
	private final ScopedValue<AtomicReference<T>> REF_HOLDER = ScopedValue.newInstance();

	protected final void yieldReturn(T toYield) {
		REF_HOLDER.get().set(toYield);
		Continuation.yield(SCOPE_HOLDER.get());
	}

	@Override
	public final Iterator<T> iterator() {
		ContinuationScope scope = new ContinuationScope("yieldReturn");
		AtomicReference<T> ref = new AtomicReference<>();
		Continuation con = new Continuation(scope, () -> ScopedValue.where(REF_HOLDER, ref,
				() -> ScopedValue.where(SCOPE_HOLDER, scope, () -> ref.set(get()))));
		return new YieldedIterator<>(ref, con);
	}

}