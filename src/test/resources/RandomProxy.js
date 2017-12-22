/* global bp */

var boolCount=0;
var intCount=0;
var floatCount=0;
var TOTAL_COUNT=1000;

for ( var i=0; i<TOTAL_COUNT; i++ ) {
    if ( bp.random.nextBoolean() ) boolCount++;
}

for ( var i=0; i<TOTAL_COUNT; i++ ) {
    if ( bp.random.nextFloat() > 0.5 ) floatCount++;
}

for ( var i=0; i<TOTAL_COUNT; i++ ) {
    if ( bp.random.nextInt(1000) > 500 ) intCount++;
}

bp.log.setLevel("Off");
var logLevel1 = bp.log.getLevel();

bp.log.setLevel("Warn");
var logLevel2 = bp.log.getLevel();