package io.github.danthe1st.jvmyieldreturn.test;

import io.github.danthe1st.jvmyieldreturn.YieldingSupplier;

public class YieldReturnTest {
	// --enable-preview --add-exports java.base/jdk.internal.vm=JVMYieldReturn
	public static void main(String[] args) {
		Iterable<String> it = new YieldingSupplier<>() {
			@Override
			public String get() {
				doYield("Hello - " + Thread.currentThread());
				System.out.println("between yields");
				doYield("World - " + Thread.currentThread());
				return "bye - " + Thread.currentThread();
			}
		};

		System.out.println("main thread: " + Thread.currentThread());

		for (String s : it) {
			System.out.println("Text: " + s);
		}
	}
}
