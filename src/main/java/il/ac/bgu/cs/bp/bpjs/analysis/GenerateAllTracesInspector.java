package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import java.util.*;
import java.util.stream.Collectors;

public class GenerateAllTracesInspector implements ExecutionTraceInspection {
  /**
   * Maps <sourceNodeId, <targetNodeId, eventFromSourceToTarget>>
   */
  private final Map<Integer, Map<Integer, BEvent>> graph = new TreeMap<>();
  private int startNode;

  @Override
  public String title() {
    return "GenerateAllTracesInspector";
  }

  @Override
  public Optional<Violation> inspectTrace(ExecutionTrace aTrace) {
    int stateCount = aTrace.getStateCount();
    var lastNode = aTrace.getNodes().get(stateCount - 1);
    if (stateCount == 1) {
      startNode = aTrace.getNodes().get(0).getState().hashCode();
    } else {
      var src = aTrace.getNodes().get(stateCount - 2);
      Map<Integer, BEvent> srcNode = graph.computeIfAbsent(src.getState().hashCode(), k -> new TreeMap<>());
      srcNode.put(lastNode.getState().hashCode(), src.getEvent().get());
    }
    return Optional.empty();
  }

  public Collection<List<BEvent>> calculateAllTraces() {
    return dfsFrom(startNode, new ArrayDeque<>(), new ArrayDeque<>());
  }

  private Collection<List<BEvent>> dfsFrom(int id, ArrayDeque<Integer> nodeStack, ArrayDeque<BEvent> eventStack) {
    Map<Integer, BEvent> outbounds = graph.get(id);
    nodeStack.push(id);
    if (outbounds == null || outbounds.isEmpty()) {
      nodeStack.pop();
      return new ArrayList<>() {{
        add(new ArrayList<>(eventStack));
      }};
    } else {
      Collection<List<BEvent>> res = outbounds.entrySet().stream()
          .filter(e -> !nodeStack.contains(e.getKey()))
          .map(e -> {
            eventStack.push(e.getValue());
            Collection<List<BEvent>> innerDfs = dfsFrom(e.getKey(),nodeStack,eventStack);
            eventStack.pop();
            return innerDfs;
          })
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
      nodeStack.pop();
      return res;
    }
  }


}
