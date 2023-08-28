package io.github.danthe1st.jvmyieldreturn.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.github.danthe1st.jvmyieldreturn.Yielder;

class YieldingIteratorTests {

	@Test
	void testEmptyFunctionHasNextBeforeNext() {
		Iterable<String> yielder = Yielder.create(y -> {
		});
		Iterator<String> it = yielder.iterator();
		assertFalse(it.hasNext());
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void testEmptyFunctionMultipleHasNextCalls() {
		Iterable<String> yielder = Yielder.create(y -> {
		});
		Iterator<String> it = yielder.iterator();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}

	@Test
	void testEmptyFunctionNoHasNext() {
		Iterable<String> yielder = Yielder.create(y -> {
		});
		assertThrows(NoSuchElementException.class, yielder.iterator()::next);
	}

	@Test
	void testFunctionWithSingleYieldHasNextBeforeNext() {
		Iterable<String> yielder = Yielder.create(y -> {
			y.yield("someValue");
		});

		Iterator<String> it = yielder.iterator();
		assertTrue(it.hasNext());
		assertEquals("someValue", it.next());
		assertFalse(it.hasNext());
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void testFunctionWithSingleYieldMultipleHasNextCalls() {
		Iterable<String> yielder = Yielder.create(y -> {
			y.yield("someValue");
		});

		Iterator<String> it = yielder.iterator();
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		assertEquals("someValue", it.next());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void testFunctionWithSingleYieldNoHasNext() {
		Iterable<String> yielder = Yielder.create(y -> {
			y.yield("someValue");
		});

		Iterator<String> it = yielder.iterator();
		assertEquals("someValue", it.next());
		assertThrows(NoSuchElementException.class, it::next);
		assertFalse(it.hasNext());
	}

	@Test
	void testFunctionYieldingNullHasNextBeforeNext() {
		Iterable<Object> yielder = Yielder.create(y -> {
			y.yield(null);
		});

		Iterator<Object> it = yielder.iterator();
		assertTrue(it.hasNext());
		assertNull(it.next());
		assertFalse(it.hasNext());
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void testFunctionYieldingNullNoHasNext() {
		Iterable<Object> yielder = Yielder.create(y -> {
			y.yield(null);
		});

		Iterator<Object> it = yielder.iterator();
		assertNull(it.next());
		assertThrows(NoSuchElementException.class, it::next);
		assertFalse(it.hasNext());
	}

	@Test
	void testYieldFromOutsideFunction() {
		var holder = new Object() {
			Yielder<String> y;
		};
		Iterable<String> yielder = Yielder.create(y -> {
			holder.y = y;
			y.yield("a");
		});
		Iterator<String> it = yielder.iterator();
		assertEquals("a", it.next());
		assertThrows(IllegalStateException.class, () -> holder.y.yield("b"));
		assertFalse(it.hasNext());
	}

//	//requires module jdk.incubator.concurrent (in Java 20)
//	@Test
//	void testYieldFromThread() {
//		Iterable<Class<?>> yielder = Yielder.create(y -> {
//			try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
//				Future<Object> f = scope.fork(() -> {
//					y.yield(getClass());// should fail with IllegalStateException
//					return fail("should have thrown exception");
//				});
//				f.get();
//			} catch (InterruptedException e) {
//				Thread.currentThread().interrupt();
//				fail("interrupted");
//			} catch (ExecutionException e) {
//				y.yield(e.getCause().getClass());
//			}
//		});
//		assertEquals(IllegalStateException.class, yielder.iterator().next());
//	}

	@Test
	void testWithException() {
		Iterable<Object> it = Yielder.create(y -> {
			throw new IntendedException();
		});
		assertThrows(IntendedException.class, it.iterator()::next);
	}

	class IntendedException extends RuntimeException {
	}
}
