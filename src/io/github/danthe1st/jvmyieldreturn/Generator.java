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
 * <p>
 * This function can send values to the {@link Iterable} by calling {@link Generator#yield(Object) yield}.
 * When the first object is requested by an {@link Iterator}, the function is executed until a {@link Generator#yield(Object) yield} call.
 * The function is then suspended until the next element is requested.
 * <p>
 * The provided function is called once per call to {@link Iterable#iterator()}.
 * <p>
 * <b>The method {@link Generator#yield(Object)} might return in a different thread or might not return at all. {@code try}-{@code finally} or closing of try-with-resources-blocks might not be executed if a {@link Generator#yield(Object)} call happens inside these blocks.</b>
 * {@snippet
 * 	class="io.github.danthe1st.jvmyieldreturn.test.YieldReturnTest"
 * 	region="example"
 * 	lang="java"
 * }
 * <p>
 * In general, the {@link Generator} object passed to the function should not be passed to other methods that might call {@link Generator#yield(Object)} inside a {@code try}-{@code finally} or closing of try-with-resources-block.
 * {@snippet
 * 	class="io.github.danthe1st.jvmyieldreturn.test.YieldReturnTest"
 * 	region="badCodeTryFinally"
 * 	lang="java"
 * }
 * <p>
 * In case a {@code try}-{@code finally} or closing of try-with-resources-block is needed, it can surround the complete iteration code ensuring that the resource is closed when the iteration completes.
 * {@snippet
 * 	class="io.github.danthe1st.jvmyieldreturn.test.YieldReturnTest"
 * 	region="goodCodeTryFinally"
 * 	lang="java"
 * }
 * @param <T> The type of elements to iterate over.
 */
public class Generator<T> {
	private final AtomicReference<T> ref;
	private final ContinuationScope scope;
	
	private Generator(ContinuationScope scope, AtomicReference<T> ref) {
		this.ref = ref;
		this.scope = scope;
	}
	
	/**
	 * Passes an object to the corresponding {@link Iterator} and suspends until the next object is requested.
	 * <p>
	 * <b>This method might return in a different thread or might not return at all. {@code try}-{@code finally} or closing of try-with-resources-blocks might not be executed if a {@link Generator#yield(Object)} call happens inside these blocks.</b>
	 * @param value The object that should be passed to the {@link Iterator}.
	 */
	public void yield(T value) {
		ref.set(value);
		Continuation.yield(scope);
	}
	
	/**
	 * Creates an {@link Iterable} obtaining its values by lazily evaluating a function.
	 * <p>
	 * <b>When calling {@link Generator#yield(Object)} in the passed function, that call  might return in a different thread or might not return at all. {@code try}-{@code finally} or closing of try-with-resources-blocks might not be executed if a {@link Generator#yield(Object)} call happens inside these blocks.</b>
	 * @param <T> The type of elements to iterate over.
	 * @param fun The function used for obtaining values to iterate over. Values are passed to the {@link Iterator} using a parameter passed to the function.
	 * @return An {@link Iterable} that allows iterating over the values provided by the given function.
	 * @see Generator#yield(Object)
	 */
	public static <T> Iterable<T> iterable(Consumer<Generator<T>> fun) {
		return () -> {
			ContinuationScope scope = new ContinuationScope("yieldReturn");
			AtomicReference<T> ref = new AtomicReference<>();
			
			Generator<T> yielder = new Generator<>(scope, ref);
			
			Continuation con = new Continuation(scope, () -> fun.accept(yielder));
			return new YieldedIterator<>(ref, con);
		};
	}
	
	/**
	 * Creates a {@link Stream} obtaining its values by lazily evaluating a function.
	 * <p>
	 * <b>When calling {@link Generator#yield(Object)} in the passed function, that call  might return in a different thread or might not return at all. {@code try}-{@code finally} or closing of try-with-resources-blocks might not be executed if a {@link Generator#yield(Object)} call happens inside these blocks.</b>
	 * @param <T> The type of the elements to iterate over.
	 * @param fun The function used for obtaining values to iterate over. Values are passed to the {@link Stream} using a parameter passed to the function.
	 * @return A {@link Stream} that allows lazily accessing the values provided by the given function.
	 * @see Generator#yield(Object)
	 */
	public static <T> Stream<T> stream(Consumer<Generator<T>> fun){
		return StreamSupport.stream(iterable(fun).spliterator(), false);
	}
}