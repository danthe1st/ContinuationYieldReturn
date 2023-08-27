package io.github.danthe1st.jvmyieldreturn.test;

import io.github.danthe1st.jvmyieldreturn.YieldingSupplier;

public class YieldReturnTest {
	// --enable-preview --add-exports java.base/jdk.internal.vm=JVMYieldReturn
	public static void main(String[] args) {
		Iterable<String> it = new YieldingSupplier<>() {
			@Override
			public String get() {
				yieldReturn("Hello - " + Thread.currentThread());
				System.out.println("between yields");
				yieldReturn("World - " + Thread.currentThread());

				Iterable<String> inner = new YieldingSupplier<>() {

					@Override
					public String get() {
						yieldReturn("it can");
						yieldReturn("also be");
						return "nested";
					}
				};

				for (String s : inner) {
					yieldReturn(s);
				}

				return "bye - " + Thread.currentThread();
			}
		};

		System.out.println("main thread: " + Thread.currentThread());

		for (String s : it) {
			System.out.println("Text: " + s);
		}
	}
}
