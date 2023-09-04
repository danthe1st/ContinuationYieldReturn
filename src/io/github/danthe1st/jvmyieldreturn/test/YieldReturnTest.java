package io.github.danthe1st.jvmyieldreturn.test;

import io.github.danthe1st.jvmyieldreturn.Yielder;

class YieldReturnTest {
	// --enable-preview --add-exports java.base/jdk.internal.vm=YieldReturn
	// @start region="example"
	public static void main(String[] args) {
		System.out.println("main thread: " + Thread.currentThread());
		
		for (String s : Yielder.iterable(YieldReturnTest::someMethod)) {
			System.out.println("Text: " + s);
		}

		System.out.println();
		System.out.println("Now using streams:");
		
		Yielder.stream(YieldReturnTest::someMethod).limit(2).forEach(System.out::println);
	}
	
	private static void someMethod(Yielder<String> y) {
		y.yield("Hello - " + Thread.currentThread());
		System.out.println("between yields");
		y.yield("World - " + Thread.currentThread());
		
		for (String s : Yielder.iterable(YieldReturnTest::otherMethod)) {
			y.yield("nested: " + s);
		}
		
		y.yield("bye - " + Thread.currentThread());
	}
	
	private static void otherMethod(Yielder<String> y) {
		y.yield("it can");
		y.yield("also be");
		y.yield("nested");
	}
	// @end
}
