package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {
	private Board board;
	private int turn;
	private Color currentPlayer;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;
	private ChessPiece promoted;

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

	public boolean isCheckMate() {
		return checkMate;
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

		ChessPiece movedPiece = (ChessPiece) board.piece(target);

		// #especial move promotion
		promoted = null;
		if (movedPiece instanceof Pawn) {
			if ((movedPiece.getColor() == Color.WHITE && targetPosition.getRow() == 0)
					|| (movedPiece.getColor() == Color.BLACK && targetPosition.getRow() == 7)) {
				promoted = movedPiece;
				promoted = replacePromotedPiece("Q");
			}
		}

		check = testCheck(opponent(currentPlayer)) ? true : false;

		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		} else {
			this.nextTurn();
		}

		// #especial move en passant
		if (movedPiece instanceof Pawn
				&& (target.getRow() == origin.getRow() + 2 || target.getRow() == origin.getRow() - 2)) {
			enPassantVulnerable = movedPiece;
		} else {
			enPassantVulnerable = null;
		}

		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position originPosition, Position targetPosition) {
		ChessPiece p = (ChessPiece) board.removePiece(originPosition);
		Piece captured = board.removePiece(targetPosition);
		p.increaseMoveCount();
		board.placePiece(p, targetPosition);

		if (captured != null) {
			piecesOnTheBoard.remove(captured);
			capturedPieces.add(captured);
		}

		// #especial move castling king side rook
		if (p instanceof King && targetPosition.getColumn() == originPosition.getColumn() + 2) {
			Position originR = new Position(originPosition.getRow(), originPosition.getColumn() + 3);
			Position targetR = new Position(originPosition.getRow(), originPosition.getColumn() + 1);

			ChessPiece rook = (ChessPiece) board.removePiece(originR);
			board.placePiece(rook, targetR);

			rook.increaseMoveCount();
		}

		// #especial move castling queen side rook
		if (p instanceof King && targetPosition.getColumn() == originPosition.getColumn() - 2) {
			Position originR = new Position(originPosition.getRow(), originPosition.getColumn() - 4);
			Position targetR = new Position(originPosition.getRow(), originPosition.getColumn() - 1);

			ChessPiece rook = (ChessPiece) board.removePiece(originR);
			board.placePiece(rook, targetR);

			rook.increaseMoveCount();
		}

		// #especial move en passant
		if (p instanceof Pawn) {
			if (targetPosition.getColumn() != originPosition.getColumn() && captured == null) {
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(targetPosition.getRow() + 1, targetPosition.getColumn());
				} else {
					pawnPosition = new Position(targetPosition.getRow() - 1, targetPosition.getColumn());
				}

				captured = board.removePiece(pawnPosition);
				capturedPieces.add(captured);
				piecesOnTheBoard.remove(captured);
			}
		}

		return captured;
	}

	private void undoMove(Position origin, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece) board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, origin);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}

		// #especial move castling king side rook
		if (p instanceof King && target.getColumn() == origin.getColumn() + 2) {
			Position originR = new Position(origin.getRow(), origin.getColumn() + 3);
			Position targetR = new Position(origin.getRow(), origin.getColumn() + 1);

			ChessPiece rook = (ChessPiece) board.removePiece(targetR);
			board.placePiece(rook, originR);

			rook.decreaseMoveCount();
		}

		// #especial move castling queen side rook
		if (p instanceof King && target.getColumn() == origin.getColumn() - 2) {
			Position originR = new Position(origin.getRow(), origin.getColumn() - 4);
			Position targetR = new Position(origin.getRow(), origin.getColumn() - 1);

			ChessPiece rook = (ChessPiece) board.removePiece(targetR);
			board.placePiece(rook, originR);

			rook.decreaseMoveCount();
		}

		// #especial move en passant
		if (p instanceof Pawn) {
			if (target.getColumn() != origin.getColumn() && capturedPiece == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece) board.removePiece(target);
				Position pawnPosition;

				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				} else {
					pawnPosition = new Position(4, target.getColumn());
				}

				board.placePiece(pawn, pawnPosition);
			}
		}
	}

	public ChessPiece replacePromotedPiece(String type) {
		if (promoted == null) {
			throw new IllegalStateException("There's no piece to be promoted");
		}
		if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
			return promoted;
		}

		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);
		ChessPiece newPiece = this.newPiece(type, promoted.getColor());
		board.placePiece(newPiece, pos);
		piecesOnTheBoard.add(newPiece);
		
		return newPiece;

	}

	private ChessPiece newPiece(String type, Color color) {
		if (type.equals("B")) return new Bishop(board, color);
		if (type.equals("N")) return new Knight(board, color);
		if (type.equals("Q")) return new Queen(board, color);

		return new Rook(board, color);
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

	private boolean testCheckMate(Color color) {
		if (!testCheck(color))
			return false;

		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) {
			boolean[][] mat = p.possibleMoves();

			for (int i = 0; i < board.getRows(); i++) {
				for (int j = 0; j < board.getColumns(); j++) {
					if (mat[i][j]) {
						Position origin = ((ChessPiece) p).getChessPosition().toPosition();
						Position target = new Position(i, j);

						Piece captured = makeMove(origin, target);

						if (!testCheck(color)) {
							undoMove(origin, target, captured);
							return false;
						} else {
							undoMove(origin, target, captured);
						}

					}
				}
			}
		}

		return true;
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
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
		placeNewPiece('b', 1, new Knight(board, Color.WHITE));
		placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('d', 1, new Queen(board, Color.WHITE));
		placeNewPiece('e', 1, new King(board, Color.WHITE, this));
		placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('g', 1, new Knight(board, Color.WHITE));
		placeNewPiece('h', 1, new Rook(board, Color.WHITE));
		placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

		placeNewPiece('a', 8, new Rook(board, Color.BLACK));
		placeNewPiece('b', 8, new Knight(board, Color.BLACK));
		placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('d', 8, new Queen(board, Color.BLACK));
		placeNewPiece('e', 8, new King(board, Color.BLACK, this));
		placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('g', 8, new Knight(board, Color.BLACK));
		placeNewPiece('h', 8, new Rook(board, Color.BLACK));
		placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
	}

	private void nextTurn() {
		this.turn++;
		this.currentPlayer = this.currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
	}

	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}

	public ChessPiece getPromoted() {
		return promoted;
	}
}
