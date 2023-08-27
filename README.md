# Continuation-based `yield return`

This project contains a `yield return` feature for Java applications using Project Loom's internal `Continuation` class.

## Demo

For a demo, see [`io.github.danthe1st.jvmyieldreturn.test.YieldReturnTest`](src/io/github/danthe1st/jvmyieldreturn/test/YieldReturnTest.java):
```java
Iterable<String> it = new YieldingSupplier<>() {
	@Override
	public String get() {
		doYield("Hello - " + Thread.currentThread());
		doYield("World - " + Thread.currentThread());
		return "bye - " + Thread.currentThread();
	}
};

System.out.println("main thread: " + Thread.currentThread());

for (String s : it) {
	System.out.println("Text: " + s);
}
```

This demo should print
```
main thread: Thread[#1,main,5,main]
Text: Hello - Thread[#1,main,5,main]
Text: World - Thread[#1,main,5,main]
Text: bye - Thread[#1,main,5,main]
```

It can be seen that the main thread switches between the enhanced for loop and the method with the `yield return` code.

It is necessary to supply the JVM arguments `--enable-preview --add-exports java.base/jdk.internal.vm=JVMYieldReturn` both for compiling and running the application.

## How it works

### `Continuation`s

`Continuation` is an internal JDK class which is used for virtual threads.
It allows running some code using `Continuation#run` until that code calls `Continuation.yield`.
At this point, execution of that code stops ("Freeze") and the `Continuation#run` returns.
If `Continuation#run` is then called again, the said code continues ("Thaw") at the `Continuation.yield` method call.

### How this project uses `Continuation`s
This project allows to create an `Iterable` which runs some (user-provided) code when `next()` is called.
When that code calls the `doYield` method, the method is suspended and `Iterator#next` returns the value passed to `doYield`.
The method is resumed when `Iterator#next` is called again.
The `yield`ing parts of the method will always run in the same thread that also calls `Iterator#next`.

## Disclaimers

See [This JVM Language Summit talk](https://www.youtube.com/watch?v=6nRS6UiN7X0) for details on how `Continuation`s work in Java.
This talk heavily inspired the creation of this project.

`Continuation` is not public API.
This project uses classes which may not be present in different JVMs or may be removed/changed even within minor Java updates.
This project is just for educational purposes!
If you are using `Continuation` in your code, do so at your own risk.