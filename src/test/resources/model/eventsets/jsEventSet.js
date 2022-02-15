/* global bp */

const esA = bp.EventSet("esA", function(e){ return e.name.startsWith("A");} );
const esA_T = bp.EventSet("esA-tag", function(e){ return e.name.startsWith("A");} );
const esB = bp.EventSet("esB", function(e){ return e.name.startsWith("B");} );
