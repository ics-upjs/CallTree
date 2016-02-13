#Call Tree

The **Call Tree** is a Java library for visualizing call trees. It can help beginners to understand control flow of their Java programs containing recursive methods and to simplify debugging of recursive programs.

Comparing to other tools for visualization of call trees, this tool can be used with arbitrary IDE. It does not inspect, modify, or even interpret the code. Construction of call tree is realized utilizing simple API calls that inspect current call stack.

##How to use the library

* Add the library (`CallTree-1.0.0.jar`) to class path (build path) or use Maven to add dependency:
  
  ```
  <dependency>
    <groupId>sk.upjs</groupId>
    <artifactId>calltree</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
  
* Place API calls in your recursive methods. The most important API call is `CallTree.markCall` that must be placed as the first command of your recursive method(s).
* Call `CallTree.markReturn` to store a value returned by recursive method.
* Call `CallTree.log` to attach a log message to current execution of recursive method.

##Example

Original method: 
``` java
public static int fib(int n) { 
  if (n <= 1) { 
    return 1; 
  }

  return fib(n - 1) + fib(n - 2);
} 
```

Method with API calls: 
``` java 
public static int fib(int n) { 
  CallTree.markCall(n);

  if (n <= 1) {
    return CallTree.markReturn(1);
  }

  return CallTree.markReturn(fib(n - 1) + fib(n - 2));
} 
```

##Notes

* CallTree is thread-safe.
* If `CallTree.markCall` or `CallTree.markReturn` are called from Event Dispatch Thread, execution of the computation thread is not suspended.
* If `CallTree.markCall` or `CallTree.markReturn` are not called from Event Dispatch Thread, execution of the computation thread is stopped until the Continue button is pressed (or given time expires).
