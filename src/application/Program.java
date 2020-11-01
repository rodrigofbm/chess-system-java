package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {
		List<ChessPiece> captureds = new ArrayList<ChessPiece>();
		ChessMatch chessMatch = new ChessMatch();
		Scanner sc = new Scanner(System.in);

		while (true) {
			try {
				UI.clearScreen();
				UI.printMatch(chessMatch, captureds);
				System.out.println();
				System.out.print("Origin: ");

				ChessPosition origin = UI.readChessPostion(sc);
				boolean[][] possibleMoves = chessMatch.possibleMoves(origin);
				
				UI.clearScreen();
				UI.printBoard(chessMatch.getPieces(), possibleMoves);
				
				System.out.println();
				System.out.print("Target: ");

				ChessPosition target = UI.readChessPostion(sc);

				ChessPiece capturedPiece = chessMatch.performChessMove(origin, target);
				if(capturedPiece != null) captureds.add(capturedPiece);
				
			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
	}
}
