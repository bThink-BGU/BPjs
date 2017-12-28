package il.ac.bgu.cs.bp.bpjs.TicTacToe.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings({ "serial" })
public class StaticEvents {
	static public BEvent draw = new BEvent("Draw") {
	};

	static public BEvent XWin = new BEvent("XWin") {
	};

	static public BEvent OWin = new BEvent("OWin") {
	};

	static public BEvent gameOver = new BEvent("GameOver") {
	};
}
