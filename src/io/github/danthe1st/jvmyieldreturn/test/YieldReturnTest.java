package io.github.danthe1st.jvmyieldreturn.test;

import io.github.danthe1st.jvmyieldreturn.Yielder;

class YieldReturnTest {
	// --enable-preview --add-exports java.base/jdk.internal.vm=YieldReturn
	public static void main(String[] args) {
		YieldReturnTest test = new YieldReturnTest();
		System.out.println("EXAMPLE CODE");
		test.example();
		System.out.println();
		
		System.out.println("==========================");
		System.out.println("BAD CODE WITH try-finally");
		test.badCodeTryFinally();
		System.out.println();
		
		System.out.println("==========================");
		System.out.println("GOOD CODE WITH try-finally");
		test.goodCodeTryFinally();
	}
	// @start region="example"
	private void example() {
		System.out.println("main thread: " + Thread.currentThread());
		
		for(String s : Yielder.iterable(this::someMethod)){
			System.out.println("Text: " + s);
		}
		
		System.out.println();
		System.out.println("Now using streams:");
		
		Yielder.stream(this::someMethod).limit(2).forEach(System.out::println);
	}
	
	private void someMethod(Yielder<String> y) {
		y.yield("Hello - " + Thread.currentThread());
		System.out.println("between yields");
		y.yield("World - " + Thread.currentThread());
		
		for(String s : Yielder.iterable(this::otherMethod)){
			y.yield("nested: " + s);
		}
		
		y.yield("bye - " + Thread.currentThread());
	}
	
	private void otherMethod(Yielder<String> y) {
		y.yield("it can");
		y.yield("also be");
		y.yield("nested");
	}
	// @end
	
	// @start region = "badCodeTryFinally"
	private void badCodeTryFinally() {
		Iterable<String> iterable = Yielder.iterable(this::methodWithTryFinally);
		String firstElement = iterable.iterator().next();
		System.out.println(firstElement);
	}
	
	private void methodWithTryFinally(Yielder<String> y) {
		try{
			y.yield("Hello World");// DON'T DO THIS
		}finally{// may not get executed
			System.out.println("Some very important cleanup code");
		}
	}
	// @end
	
	// @start region = "goodCodeTryFinally"
	private void goodCodeTryFinally() {
		try{
			Iterable<String> iterable = Yielder.iterable(this::methodWithTryFinally);
			String firstElement = iterable.iterator().next();
			System.out.println(firstElement);
		}finally{// will get executed
			System.out.println("Some very important cleanup code");
		}
		
	}
	
	private void yieldSomething(Yielder<String> y) {
		y.yield("Hello World");// OK, this is not surrounded by any try-finally
	}
	// @end
}
