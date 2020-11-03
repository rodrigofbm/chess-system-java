package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class King extends ChessPiece {
	ChessMatch chessMatch;

	public King(Board board, Color color, ChessMatch chessMatch) {
		super(board, color);
		this.chessMatch = chessMatch;
	}

	@Override
	public String toString() {
		return "K";
	}

	private boolean testRookCastling(Position position) {
		ChessPiece p = (ChessPiece) getBoard().piece(position);

		return p != null && p instanceof Rook && p.getColor() == this.getColor() && p.getMoveCount() == 0;
	}

	private boolean canMove(Position pos) {
		return !getBoard().thereIsAPiece(pos) || isThereOpponentPiece(pos);
	}

	@Override
	public boolean[][] possibleMoves() {
		boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getRows()];

		Position p = new Position(0, 0);

		// upward
		p.setValues(this.position.getRow() - 1, this.position.getColumn());
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// downward
		p.setValues(this.position.getRow() + 1, this.position.getColumn());
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// left
		p.setValues(this.position.getRow(), this.position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// right
		p.setValues(this.position.getRow(), this.position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// nw
		p.setValues(this.position.getRow() - 1, this.position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// ne
		p.setValues(this.position.getRow() - 1, this.position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// sw
		p.setValues(this.position.getRow() + 1, this.position.getColumn() - 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// se
		p.setValues(this.position.getRow() + 1, this.position.getColumn() + 1);
		if (getBoard().positionExists(p) && canMove(p)) {
			mat[p.getRow()][p.getColumn()] = true;
		}

		// #specialMove castling
		if (getMoveCount() == 0 && !chessMatch.isCheck()) {
			// #special move castling king side rook
			Position posR1 = new Position(position.getRow(), position.getColumn() + 3);
			if (this.testRookCastling(posR1)) {
				Position pos1 = new Position(position.getRow(), position.getColumn() + 1);
				Position pos2 = new Position(position.getRow(), position.getColumn() + 2);

				if (!getBoard().thereIsAPiece(pos1) && !getBoard().thereIsAPiece(pos2)) {
					mat[position.getRow()][pos2.getColumn()] = true;
				}
			}

			// #special move castling queen side rook
			Position posR2 = new Position(position.getRow(), position.getColumn() - 4);
			if (this.testRookCastling(posR2)) {
				Position pos1 = new Position(position.getRow(), position.getColumn() - 1);
				Position pos2 = new Position(position.getRow(), position.getColumn() - 2);
				Position pos3 = new Position(position.getRow(), position.getColumn() - 3);

				if (!getBoard().thereIsAPiece(pos1) && !getBoard().thereIsAPiece(pos2)
						&& !getBoard().thereIsAPiece(pos3)) {
					mat[position.getRow()][pos2.getColumn()] = true;
				}
			}
		}

		return mat;
	}
}
