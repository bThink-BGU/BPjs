const obj = {hello: "World", idioms:["request","waitFor","block"]};
const stuff = new Set();
stuff.add("thing 1");
stuff.add("thing 2");
stuff.add("thing 42");

bp.log.info("Here is field hello: {0} of object {1}", obj.hello, obj);
bp.log.info("Here is are some {1}: {0}", stuff, "stuff");

bp.log.info("I have a {0,number} reasons to block this event.", 1000000);
bp.log.info("{0} {0,number,#.##} {0,number,#.####}", 3.14159);