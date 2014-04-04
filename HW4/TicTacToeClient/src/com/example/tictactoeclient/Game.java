package com.example.tictactoeclient;

public class Game {
	int[][] board;
	
	//ctor
	Game(){
		this.board = new int[3][3];
		for (int i=0; i<3; i++){
			for (int j=0; j<3; j++){
				this.board[i][j]=0;
			}
		}
	}
	
	void put(int row, int col, int XorO){ // XorO = -1/1; X=-1, O=1
		board[row][col] = XorO;
	}
	
	boolean ifWin (int row, int col){	
		// check row
		if (board[row][0]==board[row][1] && board[row][1]==board[row][2]){
			return true;
		}
		
		//check column
		if (board[0][col]==board[1][col] && board[1][col]==board[2][col]){
			return true;
		}
		
		// check diagonal
		if (row==col){
			if (board[0][0]==board[1][1] && board[1][1]==board[2][2]){
				return true;
			}
		}
		if (row+col == 2){
			if (board[0][2]==board[1][1] && board[1][1]==board[2][0]){
				return true;
			}
		}

		return false;
	}

	boolean ifTie(){
		for (int i=0; i<3; i++){
			for (int j=0; j<3; j++){
				if (board[i][j]==0){
					return false;
				}
			}
		}
		return true;
	}
	
}
