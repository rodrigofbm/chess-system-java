package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {
	private Board board;
	private int turn;
	private Color currentPlayer;
	private boolean check;

	private List<Piece> piecesOnTheBoard = new ArrayList<Piece>();
	private List<Piece> capturedPieces = new ArrayList<Piece>();

	public ChessMatch() {
		this.board = new Board(8, 8);
		this.initialSetup();
		turn = 1;
		currentPlayer = Color.WHITE;
	}

	public int getTurn() {
		return turn;
	}

	public boolean isCheck() {
		return check;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getRows(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}

		return mat;
	}

	public boolean[][] possibleMoves(ChessPosition originPosition) {
		Position pos = originPosition.toPosition();
		validateOriginPosition(pos);

		return board.piece(pos).possibleMoves();
	}

	public ChessPiece performChessMove(ChessPosition originPosition, ChessPosition targetPosition) {
		Position origin = originPosition.toPosition();
		Position target = targetPosition.toPosition();
		validateOriginPosition(origin);
		validateTargetPosition(origin, target);
		Piece capturedPiece = makeMove(origin, target);

		if (testCheck(currentPlayer)) {
			undoMove(origin, target, capturedPiece);
			throw new ChessException("You can't put yourself in check!");
		}

		check = testCheck(opponent(currentPlayer)) ? true : false;

		this.nextTurn();
		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position originPosition, Position targetPosition) {
		Piece p = board.removePiece(originPosition);
		Piece captured = board.removePiece(targetPosition);

		board.placePiece(p, targetPosition);

		if (captured != null) {
			piecesOnTheBoard.remove(captured);
			capturedPieces.add(captured);
		}

		return captured;
	}

	private void undoMove(Position origin, Position target, Piece capturedPiece) {
		Piece p = board.removePiece(target);
		board.placePiece(p, origin);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
	}

	private Color opponent(Color color) {
		return color == Color.WHITE ? Color.BLACK : Color.WHITE;
	}

	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) {
			if (p instanceof King)
				return (ChessPiece) p;
		}

		throw new IllegalStateException("There's no " + color + " King on the board");
	}

	private boolean testCheck(Color color) {
		Position kingPos = king(color).getChessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnTheBoard.stream()
				.filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());

		for (Piece oppnent : opponentPieces) {
			boolean[][] mat = oppnent.possibleMoves();

			if (mat[kingPos.getRow()][kingPos.getColumn()]) {
				return true;
			}
		}

		return false;
	}

	private void validateOriginPosition(Position pos) {
		ChessPiece p = (ChessPiece) board.piece(pos);

		if (p.getColor() != this.currentPlayer) {
			throw new ChessException("The chosen piece is not yours");
		}
		if (!board.thereIsAPiece(pos)) {
			throw new ChessException("There's no piece on origin position");
		}

		if (!board.piece(pos).isThereAnyPossibleMove()) {
			throw new ChessException("There's no possible moves for this piece");
		}
	}

	private void validateTargetPosition(Position originPosition, Position targetPosition) {
		if (!board.piece(originPosition).possibleMove(targetPosition))
			throw new ChessException("The chosen piece can't move to the target position");
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}

	private void initialSetup() {
		placeNewPiece('c', 1, new Rook(board, Color.WHITE));
		placeNewPiece('c', 2, new Rook(board, Color.WHITE));
		placeNewPiece('d', 2, new Rook(board, Color.WHITE));
		placeNewPiece('e', 2, new Rook(board, Color.WHITE));
		placeNewPiece('e', 1, new Rook(board, Color.WHITE));
		placeNewPiece('d', 1, new King(board, Color.WHITE));

		placeNewPiece('c', 7, new Rook(board, Color.BLACK));
		placeNewPiece('c', 8, new Rook(board, Color.BLACK));
		placeNewPiece('d', 7, new Rook(board, Color.BLACK));
		placeNewPiece('e', 7, new Rook(board, Color.BLACK));
		placeNewPiece('e', 8, new Rook(board, Color.BLACK));
		placeNewPiece('d', 8, new King(board, Color.BLACK));
	}

	private void nextTurn() {
		this.turn++;
		this.currentPlayer = this.currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
	}
}
