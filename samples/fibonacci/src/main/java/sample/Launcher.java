package sample;

import sk.upjs.paz.calltree.CallTree;

public class Launcher {

	private static int fib(int n) {
		CallTree.markCall(n);

		if (n <= 1) {
			return CallTree.markReturn(1);
		}

		return CallTree.markReturn(fib(n - 1) + fib(n - 2));
	}

	public static void main(String[] args) {
		System.out.println(fib(6));
	}

}
