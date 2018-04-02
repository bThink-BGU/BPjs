# BPjs tips
Mainly for commands that are useful, but not in frequent use.

## Build jar with tests
Useful for running long verifications outside of NetBeans

```
mvn jar:test-jar
```

NOTE: Must use jdk8 for now.


## Running verifications that live in the tests directory from the terminal
to run the actual test, also build the uber-jar:

```
mvn package -P uber-jar
```

Now both live in the `target` directory. You can now run the test using Java, as usual, with both jars in the `-cp` parameter:

```
java -cp target/BPjs-0.9.2-SNAPSHOT.uber.jar:target/BPjs-0.9.2-SNAPSHOT-tests.jar il.ac.bgu.cs.bp.bpjs.TicTacToe.TicTacToeVerMain
```

