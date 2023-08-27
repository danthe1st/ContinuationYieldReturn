package io.github.danthe1st.jvmyieldreturn.test;

import io.github.danthe1st.jvmyieldreturn.Yielder;

public class YieldReturnTest {
	// --enable-preview --add-exports java.base/jdk.internal.vm=JVMYieldReturn
	public static void main(String[] args) {
		Iterable<String> it = Yielder.create(YieldReturnTest::someMethod);

		System.out.println("main thread: " + Thread.currentThread());

		for (String s : it) {
			System.out.println("Text: " + s);
		}
	}

	private static String someMethod(Yielder<String> y) {
		y.yield("Hello - " + Thread.currentThread());
		System.out.println("between yields");
		y.yield("World - " + Thread.currentThread());

		for (String s : Yielder.create(YieldReturnTest::otherMethod)) {
			y.yield("nested: " + s);
		}

		return "bye - " + Thread.currentThread();
	}

	private static String otherMethod(Yielder<String> i) {
		i.yield("it can");
		i.yield("also be");
		return "nested";
	}
}
