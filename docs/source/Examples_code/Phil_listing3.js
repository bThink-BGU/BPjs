if (!PHILOSOPHER_COUNT) PHILOSOPHER_COUNT = 5;

for (var i=1; i<=PHILOSOPHER_COUNT; i++) {
	addStick(i);
	addPhil(i);
}
