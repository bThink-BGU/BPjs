class PriorityEss implements EventSelectionStrategy {

    @Override
    public Set<BEvent> selectableEvents(Set<BSyncStatement> statements, List<BEvent> externalEvents) {

         EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .map(BSyncStatement::getBlock )
                .filter(r -> r != EventSets.none )
                .collect( Collectors.toSet() ) );

        Iterator<BSyncStatement> stmts = statements.iterator();
        if ( stmts.hasNext() ) {
            BSyncStatement firstStmt = stmts.next();
            Set<BEvent> selectable = getNotBlocked(firstStmt, blocked);
            int minValue = getValue( firstStmt );

            while ( stmts.hasNext() ) {
                BSyncStatement curStmt = stmts.next();
                int curValue = getValue(curStmt);
                if ( curValue < minValue ) {
                    minValue = curValue;
                    selectable = getNotBlocked(curStmt, blocked);
                }
            }
            return new HashSet<>(selectable);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Optional<EventSelectionResult> select(Set<BSyncStatement> statements, List<BEvent> externalEvents, Set<BEvent> selectableEvents) {
        if ( selectableEvents.isEmpty() ) {
            return Optional.empty();
        } else {
            return Optional.of( new EventSelectionResult(selectableEvents.iterator().next()));
        }
    }

    private int getValue( BSyncStatement stmt ) {
        return stmt.hasData() ? ((Number)stmt.getData()).intValue() : Integer.MAX_VALUE;
    }

    private Set<BEvent> getNotBlocked(BSyncStatement stmt, EventSet blocked) {
        try {
            Context.enter();
            return stmt.getRequest().stream()
                    .filter( req -> !blocked.contains(req) )
                    .collect( toSet() );
        } finally {
            Context.exit();
        }
    }
}
