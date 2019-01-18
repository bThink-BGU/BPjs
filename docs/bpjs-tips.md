# BPjs tips
Mainly for commands that are useful, but not in frequent use.

## Build jar with tests
Useful for running long verifications outside of NetBeans

```
mvn jar:test-jar
```

NOTE: The build must use jdk8 for now. Execution can be done on any jdk (at least, worked for us with jdk11).


## Running verifications that live in the tests directory from the terminal
to run the actual test, also build the uber-jar:

```
mvn package -P uber-jar
```

Now both live in the `target` directory. You can now run the test using Java, as usual, with both jars in the `-cp` parameter:

```
java -cp target/BPjs-0.9.2-SNAPSHOT.uber.jar:target/BPjs-0.9.2-SNAPSHOT-tests.jar il.ac.bgu.cs.bp.bpjs.TicTacToe.TicTacToeVerMain
```

## Event comparison
Better to use non-strict JavaScript object comparison for now. So prefer

```javascript
  var evt = bp.sync({waitFor:ANY_ADDITION, block:chooseBlock(bias)});
  if ( evt.name == ADD_WETS.name ) {
    ...
```

over

```javascript
  var evt = bp.sync({waitFor:ANY_ADDITION, block:chooseBlock(bias)});
  if ( evt === ADD_WETS ) {
    ...
```

as the latter may return a false negative, especially during verification.

## Implementing Custom Events and Using Custom Objects as Event Data

ALWAYS make sure that you have state-based, meaningful `equals()` and `hashCode()`. Or verification fails.

## State minimization 
(yes, this is informed by the "data minimization" directive of privacy by design)
It's better to store least amount of data.  E.g. in the fruitRatio.js file, this verison of the b-threads yields three states:

```JavaScript
bp.registerBThread( "RaspberryAdder", function(){
    var fruitIndex=0;
    while (true) {
        var evt = null;
        if ( fruitIndex > 0 ) {
            evt = bp.hot(true).sync({request:ADD_RASPB,
                                     waitFor:ADD_FRUIT});
        } else {
            evt = bp.sync({waitFor:ADD_FRUIT});
        }
        fruitIndex = fruitIndex + 
                    evt.data.blueberries-evt.data.raspberries;
    }
});
```

Where this version yields 4:
```JavaScript
bp.registerBThread( "RaspberryAdder", function(){
    var fruitIndex=0;
    var evt = null;
    while (true) {
        if ( fruitIndex > 0 ) {
            evt = bp.hot(true).sync({request:ADD_RASPB,
                                     waitFor:ADD_FRUIT});
        } else {
            evt = bp.sync({waitFor:ADD_FRUIT});
        }
        fruitIndex = fruitIndex + 
                    evt.data.blueberries-evt.data.raspberries;
    }
});
```
That's because the former does not store the event from the previous iteration.