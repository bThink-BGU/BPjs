package il.ac.bgu.cs.bp.bpjs.execution;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Rhino191FeaturesTest {

    @Test
    public void testArrowFunctionsCanDefineBThreadsAndEventSets() {
        // Basic Rhino 1.9.1 smoke test: arrow functions should work both for
        // b-thread bodies and for event-set predicates in resource scripts.
        BProgram bprog = new ResourceBProgram("ArrowFunctions.js");
        assertEquals(
            Arrays.asList(
                new BEvent("arrow-1"),
                new BEvent("arrow-2"),
                new BEvent("arrow-3"),
                new BEvent("done")
            ),
            runAndCollectEvents(bprog)
        );
    }

    @Test
    public void testTemplateLiteralsAndDestructuringWorkTogether() {
        // Covers modern syntax inside BPjs code paths that matter for behavior:
        // template literals for event names and object destructuring on events
        // returned from bp.sync().
        BProgram bprog = new StringBProgram("Rhino191-template-destructuring",
            String.join("\n",
                "const makeEvent = pair => bp.Event(`${pair.prefix}-${pair.index}`, { suffix: `${pair.index}` });",
                "const steps = [{prefix: \"combo\", index: 1}, {prefix: \"combo\", index: 2}];",
                "",
                "bp.registerBThread(\"requester\", () => {",
                "    for (let i = 0; i < steps.length; i++) {",
                "        bp.sync({request: makeEvent(steps[i])});",
                "    }",
                "    bp.sync({request: bp.Event(`combo-${steps.length + 1}-tail`, { suffix: `${steps.length + 1}` })});",
                "});",
                "",
                "bp.registerBThread(\"validator\", () => {",
                "    const first = bp.sync({waitFor: makeEvent(steps[0])});",
                "    const {name, data} = first;",
                "    if (String(name) != `combo-${data.suffix}`) {",
                "        throw new Error(`Unexpected first event: ${name}`);",
                "    }",
                "    bp.sync({waitFor: makeEvent(steps[1])});",
                "    bp.sync({waitFor: bp.Event(`combo-${steps.length + 1}-tail`, { suffix: `${steps.length + 1}` })});",
                "    bp.sync({request: bp.Event(\"template-done\")});",
                "});"
            )
        );

        assertEquals(
            Arrays.asList(
                "combo-1",
                "combo-2",
                "combo-3-tail",
                "template-done"
            ),
            runAndCollectEventNames(bprog)
        );
    }

    @Test
    public void testLetStateCanAdvanceAcrossSyncPoints() {
        // Verifies that let/const-backed state survives continuation resumes and
        // still drives subsequent event creation after a sync point.
        BProgram bprog = new StringBProgram("Rhino191-let-state",
            String.join("\n",
                "let nextIndex = 1;",
                "const nextEvent = () => bp.Event(`let-${nextIndex}`);",
                "",
                "bp.registerBThread(\"requester\", () => {",
                "    bp.sync({request: nextEvent()});",
                "    nextIndex = nextIndex + 1;",
                "    bp.sync({request: nextEvent()});",
                "});",
                "",
                "bp.registerBThread(\"validator\", () => {",
                "    bp.sync({waitFor: bp.Event(\"let-1\")});",
                "    bp.sync({waitFor: bp.Event(\"let-2\")});",
                "    bp.sync({request: bp.Event(\"let-done\")});",
                "});"
            )
        );

        assertEquals(
            Arrays.asList(
                new BEvent("let-1"),
                new BEvent("let-2"),
                new BEvent("let-done")
            ),
            runAndCollectEvents(bprog)
        );
    }

    @Test
    public void testSetAndArrayMethodsCanDriveEventSets() {
        // Checks a useful combination of newer built-ins: array mapping/filtering
        // prepares the accepted names, and a Set-backed arrow predicate performs
        // membership checks inside a BPjs EventSet.
        BProgram bprog = new StringBProgram("Rhino191-set-array",
            String.join("\n",
                "const names = [1, 2, 3].map(n => `set-${n}`);",
                "const allowed = new Set(names.filter(name => name.indexOf(\"set-\") === 0));",
                "const matchingEvents = bp.EventSet(\"matching-events\", evt => allowed.has(String(evt.name)));",
                "",
                "bp.registerBThread(\"requester\", () => {",
                "    for (let i = 0; i < names.length; i++) {",
                    "        bp.sync({request: bp.Event(names[i])});",
                "    }",
                "});",
                "",
                "bp.registerBThread(\"validator\", () => {",
                "    const first = bp.sync({waitFor: matchingEvents});",
                "    if (String(first.name) != names[0]) throw new Error(`Expected ${names[0]} but got ${first.name}`);",
                "    const second = bp.sync({waitFor: matchingEvents});",
                "    if (String(second.name) != names[1]) throw new Error(`Expected ${names[1]} but got ${second.name}`);",
                "    const third = bp.sync({waitFor: matchingEvents});",
                "    if (String(third.name) != names[2]) throw new Error(`Expected ${names[2]} but got ${third.name}`);",
                "    bp.sync({request: bp.Event(\"set-done\")});",
                "});"
            )
        );

        assertEquals(
            Arrays.asList(
                new BEvent("set-1"),
                new BEvent("set-2"),
                new BEvent("set-3"),
                new BEvent("set-done")
            ),
            runAndCollectEvents(bprog)
        );
    }

    private List<BEvent> runAndCollectEvents(BProgram bprog) {
        BProgramRunner runner = new BProgramRunner(bprog);
        InMemoryEventLoggingListener eventLogger = runner.addListener(new InMemoryEventLoggingListener());
        runner.run();
        return eventLogger.getEvents();
    }

    private List<String> runAndCollectEventNames(BProgram bprog) {
        return runAndCollectEvents(bprog).stream()
            .map(BEvent::getName)
            .collect(Collectors.toList());
    }
}
