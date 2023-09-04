package io.github.danthe1st.jvmyieldreturn;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

/**
 * Allows to create an {@link Iterable} or {@link Stream} obtaining its values by lazily evaluating a function.
 *
 * This function can send values to the {@link Iterable} by calling {@link Yielder#yield(Object) yield}.
 * When the first object is requested by an {@link Iterator}, the function is executed until a {@link Yielder#yield(Object) yield} call.
 * The function is then suspended until the next element is requested.
 *
 * The provided function is called once per call to {@link Iterable#iterator()}.
 *
 * {@snippet
 * 	class="io.github.danthe1st.jvmyieldreturn.test.YieldReturnTest"
 * 	region="example"
 * 	lang="java"
 * }
 * @param <T> The type of elements to iterate over.
 */
public class Yielder<T> {
	private final AtomicReference<T> ref;
	private final ContinuationScope scope;
	
	private Yielder(ContinuationScope scope, AtomicReference<T> ref) {
		this.ref = ref;
		this.scope = scope;
	}
	
	/**
	 * Passes an object to the corresponding {@link Iterator} and suspends until the next object is requested.
	 * @param value The object that should be passed to the {@link Iterator}.
	 */
	public void yield(T value) {
		ref.set(value);
		Continuation.yield(scope);
	}
	
	/**
	 * Creates an {@link Iterable} obtaining its values by lazily evaluating a function.
	 * @param <T> The type of elements to iterate over.
	 * @param fun The function used for obtaining values to iterate over. Values are passed to the {@link Iterator} using a parameter passed to the function.
	 * @return An {@link Iterable} that allows iterating over the values provided by the given function.
	 */
	public static <T> Iterable<T> iterable(Consumer<Yielder<T>> fun) {
		return () -> {
			ContinuationScope scope = new ContinuationScope("yieldReturn");
			AtomicReference<T> ref = new AtomicReference<>();
			
			Yielder<T> yielder = new Yielder<>(scope, ref);
			
			Continuation con = new Continuation(scope, () -> fun.accept(yielder));
			return new YieldedIterator<>(ref, con);
		};
	}
	
	/**
	 * Creates a {@link Stream} obtaining its values by lazily evaluating a function.
	 * @param <T> The type of the elements to iterate over.
	 * @param fun The function used for obtaining values to iterate over. Values are passed to the {@link Stream} using a parameter passed to the function.
	 * @return A {@link Stream} that allows lazily accessing the values provided by the given function.
	 */
	public static <T> Stream<T> stream(Consumer<Yielder<T>> fun){
		return StreamSupport.stream(iterable(fun).spliterator(), false);
	}
}