# Continuation-based `yield return`

This project contains a generator feature for Java applications using Project Loom's internal `Continuation` class.

A Javadoc is available [on GitHub Pages](https://danthe1st.github.io/ContinuationYieldReturn/)

## Demo

For a demo, see [`io.github.danthe1st.jvmyieldreturn.example.GeneratorExamples`](./src/io/github/danthe1st/jvmyieldreturn/example/GeneratorExamples.java):
```java
private void example() {
	System.out.println("main thread: " + Thread.currentThread());

	for(String s : Generator.iterable(this::someMethod)){
		System.out.println("Text: " + s);
	}

	System.out.println();
	System.out.println("Now using streams:");

	Generator.stream(this::someMethod).limit(2).forEach(System.out::println);
}

private void someMethod(Generator<String> y) {
	y.yield("Hello - " + Thread.currentThread());
	System.out.println("between yields");
	y.yield("World - " + Thread.currentThread());

	for(String s : Generator.iterable(this::otherMethod)){
		y.yield("nested: " + s);
	}

	y.yield("bye - " + Thread.currentThread());
}

private void otherMethod(Generator<String> y) {
	y.yield("it can");
	y.yield("also be");
	y.yield("nested");
}
```

This demo should print
```
main thread: Thread[#1,main,5,main]
Text: Hello - Thread[#1,main,5,main]
between yields
Text: World - Thread[#1,main,5,main]
Text: nested: it can
Text: nested: also be
Text: nested: nested
Text: bye - Thread[#1,main,5,main]

Now using streams:
Hello - Thread[#1,main,5,main]
between yields
World - Thread[#1,main,5,main]
```

It can be seen that the main thread switches between the enhanced for loop and the method with the `yield return` code.

It is necessary to supply the JVM arguments `--enable-preview --add-exports java.base/jdk.internal.vm=YieldReturn` both for compiling and running the application.

## How it works

### `Continuation`s

`Continuation` is an internal JDK class which is used for virtual threads.
It allows running some code using `Continuation#run` until that code calls `Continuation.yield`.
At this point, execution of that code stops ("Freeze") and the `Continuation#run` returns.
If `Continuation#run` is then called again, the said code continues ("Thaw") at the `Continuation.yield` method call.

### How this project uses `Continuation`s
This project allows to create an `Iterable` which runs some (user-provided) `Function<Yielder<T>,T>` when `hasNext()` or `next()` is called and no value that hasn't been consumed by `next()` was computed already.
When the code inside that `Function` calls the `Yielder#yield` method, the method is suspended and `Iterator#next` returns the value passed to `Yielder#yield`.
The method is resumed when `Iterator#hasNext`/`Iterator#next` is called again after the previous value was consumed by `Iterator#next`.
The `yield`ing parts of the method will always run in the same thread that also calls `Iterator#hasNext`/`Iterator#next`.

## Setup and Requirements

### Requirements
This project requires a Hotspot JDK version 20 (other Java versions may not work).

### JVM configuration

It is necessary to supply the arguments `--enable-preview --add-exports java.base/jdk.internal.vm=YieldReturn` both for compiling and running the application.

Since `Continuation` is an internal JDK class, it is normally not available.
It can be made available to this project using the command-line argument `--add-exports java.base/jdk.internal.vm=YieldReturn` which allows this project (module `YieldReturn`) to access classes of the package `jdk.internal.vm` of the `java.base` module.

The argument `--enable-preview` is necessary since `Continuation`s are only available as a preview feature (as of Java 20).

#### Eclipse

In Eclipse, the export can be added from the `Libraries` tab of the Build path (Right click on the project > `Build Path` > `Configure Build Path` > `Libraries`).
After expanding the `JRE System Library` (this should point to a Java 20 JDK), there should be a `Is modular` option.
This option needs to be edited via double-clicking or the `Edit`-button on the right.
![image](https://github.com/danthe1st/ContinuationYieldReturn/assets/34687786/042bdd53-2a5c-42a1-8f1a-41659c3ac9c8)

In the `Details` tab, a new export needs to be added via the `Add` button on the right.
![image](https://github.com/danthe1st/ContinuationYieldReturn/assets/34687786/bb7f997c-3dcd-4f2e-8742-7155530bbe4f)

Enter `java.base` for the `Source Module` and `jdk.internal.vm` for the `Package` entry.
![image](https://github.com/danthe1st/ContinuationYieldReturn/assets/34687786/6893b91d-d9db-40f7-bfed-495c74dcfaa4)

`--enable-preview` can be added to the VM arguments of the run configuration.
![image](https://github.com/danthe1st/ContinuationYieldReturn/assets/34687786/1ac7bb62-6fd4-487d-9c4d-b55a09580d13)

### CLI

The content of the `src` directory can be compiled and run using the `javac`/`java` command as follows:

First, create a directory called `build` in the project root in order to store `.class` files and ensure that you are running Java 20 with Hotspot.

Then, the code can be compiled using
```bash
javac --source 20 --enable-preview --add-exports java.base/jdk.internal.vm=YieldReturn -d build src/module-info.java src/io/github/danthe1st/jvmyieldreturn/*.java src/io/github/danthe1st/jvmyieldreturn/test/*.java
```

After the code is compiled to the `build` directory, it can be run with the following command:
```bash
java --enable-preview --add-exports java.base/jdk.internal.vm=YieldReturn --module-path build -m YieldReturn/io.github.danthe1st.jvmyieldreturn.test.YieldReturnTest
```

### Tests

Some tests are present in the `test` directory.

Running the tests requires JUnit 5.

## Disclaimers

See [This JVM Language Summit talk](https://www.youtube.com/watch?v=6nRS6UiN7X0) for details on how `Continuation`s work in Java.
This talk heavily inspired the creation of this project.

`Continuation` is not public API.
This project uses classes which may not be present in different JVMs or may be removed/changed even within minor Java updates.
This project is just for educational purposes!
If you are using `Continuation` in your code, do so at your own risk.