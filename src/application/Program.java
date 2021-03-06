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

		while (!chessMatch.isCheckMate()) {
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
				
				if(chessMatch.getPromoted() != null) {
					System.out.print("Enter piece for Promotion (B/N/R/Q)");
					String type = sc.nextLine().toUpperCase();
					while (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
						System.out.print("Invalid value! Enter piece for Promotion (B/N/R/Q)");
						type = sc.nextLine().toUpperCase();
					}
					
					chessMatch.replacePromotedPiece(type);
				}
				
			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
		
		UI.clearScreen();
		UI.printMatch(chessMatch, captureds);
	}
}
