package application;

import java.util.Scanner;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {
		ChessMatch chessMatch = new ChessMatch();
		Scanner sc = new Scanner(System.in);

		while (true) {
			UI.printBoard(chessMatch.getPieces());
			System.out.println();
			System.out.print("Origin: ");
			
			ChessPosition origin = UI.readChessPostion(sc);
			
			System.out.println();
			System.out.print("Target: ");
			
			ChessPosition target = UI.readChessPostion(sc);
			
			ChessPiece capturedPiece = chessMatch.performChessMove(origin, target);
		}
	}
}
