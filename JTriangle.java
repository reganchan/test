/**
 * @(#)JTriangle.java
 *
 * Sample Applet application
 *
 * @author 
 * @version 1.00 04/12/12
 */
 
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.applet.*;
import java.lang.Math;
import java.util.Vector;
import java.lang.Thread;
import java.lang.Integer;

class Globals
{
	private Globals() {}
	
	public static final int BOARD_WIDTH = 180;
	public static final int BOARD_HEIGHT = 180;
	
	public static final int DEFAULT_BOARD_DIM = 5;
	public static final int MAX_BOARD_DIM = 6;
	public static final int GREEN = 0;
	public static final int RED = 1;
	public static final int BLUE = 2;
	public static final int EMPTY = GREEN;
	public static final int LEAVEBLANK = -1;
	
	public static final int PLAYER_TURN = 0;
	public static final int COMPUTER_TURN = 1;
	public static final int WAIT_FOR_RED_MARK = 2;
	public static final int WAIT_FOR_BLUE_MARK = 3;
	public static final int GAME_OVER = -1;
	
	public static final int MAX_MOVE = 3;
	public static final int MAX_TRIES = 5;
	
	public static final int EASY = 0;
	public static final int HARD = 1;
	
	public static final int MAX_PIECES = 21;
	public static final int RESULTS_SIZE = 2097152;
	
	public static final int ANIMATE_FPS = 15;
	public static final int ANIMATE_RPS = 2;
	
	public static final String NEW_GAME = "NewGame";
	public static final String PASS_MOVE = "PassMove";
	public static final String UNDO_MOVE = "UndoMove";
}

class Pos
{
	public int row, col;

	Pos(int new_row, int new_col)
	{	row = new_row;
		col = new_col;
	}
}

class Move
{
	public Pos beg, end;
	
	Move()
	{
		beg = new Pos(-1, -1);
		end = new Pos(-1, -1);
	}
	Move(final Pos new_beg, final Pos new_end)
	{	beg = new Pos(new_beg.row, new_beg.col);
		end = new Pos(new_end.row, new_end.col);;
	}
	public boolean is_one()
	{	return beg.row == end.row && beg.col == end.col;
	}
}

class Board
{
	private int _dim;
	public int[][] _board = new int[Globals.MAX_BOARD_DIM][Globals.MAX_BOARD_DIM];
;
	Board(int dim)
	{
		_dim = dim;
		for(int i=0; i < _dim; i++)
		{
			for(int j=0; j < _dim; j++)
			{
				_board[i][j] = Globals.EMPTY;
			}
		}
	}
	Board()
	{
		_dim = Globals.DEFAULT_BOARD_DIM;
		for(int i=0; i < _dim; i++)
		{
			for(int j=0; j < _dim; j++)
			{
				_board[i][j] = Globals.EMPTY;
			}
		}
	}
	public void make_move(final Move move_to_do, int code)
	{
		Pos beg = move_to_do.beg;
		Pos end = move_to_do.end;
		int y_pos = beg.row;
		int x_pos = beg.col;
		int x_displacement = (beg.col == end.col) ? 0 : (beg.col > end.col) ? -1 : 1;
		int y_displacement = (beg.row == end.row) ? 0 : (beg.row > end.row) ? -1 : 1;
		_board[y_pos][x_pos] = code;
	
		while(x_pos != end.col || y_pos != end.row)
		{
			y_pos += y_displacement;
			x_pos += x_displacement;
			_board[y_pos][x_pos] = code;
		}
	}
	public final int read(final Pos pos_to_read)
	{
		return _board[pos_to_read.row][pos_to_read.col];
	}
	public final Pos next_empty_pos(final Pos current)
	{
		int row = current.row;
		int use_col = current.col - 1;
		for (; row >= 0; row--)
		{
			for (int col=use_col; col >= 0; col--)
			{
				if (_board[row][col] == Globals.EMPTY)
					return new Pos(row, col);
			}
			use_col = row - 1;
		}
		return new Pos(-1, -1);
	}
	public final Pos first_empty_pos()
	{
		if (_board[_dim - 1][_dim - 1] == Globals.EMPTY)
			return new Pos(_dim - 1, _dim - 1);
		else return next_empty_pos(new Pos(_dim - 1, _dim - 1));
	}
	public final boolean is_valid_move(final Move move_to_test)
	{
		final Pos beg = move_to_test.beg;
		final Pos end = move_to_test.end;
		int dx = end.col - beg.col;
		int dy = end.row - beg.row;
		int abs_dx = dx < 0 ? -dx : dx;
		int abs_dy = dy < 0 ? -dy : dy;
		if (!(dx == 0 || dy == 0 || dx == dy) || abs_dx >= Globals.MAX_MOVE || abs_dy >= Globals.MAX_MOVE)
			return false;
	
		int y_pos = beg.row;
		int x_pos = beg.col;
		dx = (beg.col == end.col) ? 0 : (beg.col > end.col) ? -1 : 1;
		dy = (beg.row == end.row) ? 0 : (beg.row > end.row) ? -1 : 1;
		if (_board[y_pos][x_pos] != Globals.EMPTY) return false;
		while (x_pos != end.col || y_pos != end.row)
		{
			x_pos += dx;
			y_pos += dy;
			if (_board[y_pos][x_pos] != Globals.EMPTY) return false;
		}
		return true;
	}
	public final Move first_valid_move()
	{
		Pos beg = first_empty_pos();
		return new Move(beg, beg);
	}
	public final Move next_valid_move(final Move current_move)
	{
		Pos beg = new Pos(current_move.beg.row, current_move.beg.col);
		Pos end = new Pos(current_move.end.row, current_move.end.col);
		int dx = beg.col - end.col;
		int dy = beg.row - end.row;
		boolean tryNextMoveType = false;
	
		if(dy == 0)						// Next lefward move
		{
			int new_end_col = end.col - 1;
			int new_end_row = end.row;
			int new_dx = dx + 1;
			if(new_end_col >= 0 && new_dx < Globals.MAX_MOVE && _board[new_end_row][new_end_col] == Globals.EMPTY)
				return new Move(beg, new Pos(new_end_row, new_end_col));
	
			tryNextMoveType = true;		// try next move type
			end = beg;
			dx = 0;
		}
		if(dx == 0 || tryNextMoveType)	// Next upward move
		{
			int new_end_col = end.col;
			int new_end_row = end.row - 1;
			int new_dy = dy + 1;
			if(new_end_row >= new_end_col && new_end_row >= 0 && new_dy < Globals.MAX_MOVE && _board[new_end_row][new_end_col] == Globals.EMPTY)
				return new Move(beg, new Pos(new_end_row, new_end_col));
	
			end = beg;					// try next move type
			dy = 0;
		}
	
		// Next upward-left move
		int new_end_col = end.col - 1;
		int new_end_row = end.row - 1;
		int new_dy = dy + 1;
		if(new_end_row >= 0 && new_end_col >= 0 && new_dy < Globals.MAX_MOVE && _board[new_end_row][new_end_col] == Globals.EMPTY)
			return new Move(beg, new Pos(new_end_row, new_end_col));
	
		// Next beginning piece
		beg = next_empty_pos(beg);
		return new Move(beg, beg);
	}
	public final int get_dim()
	{	return _dim;
	}
	public final int state()
	{
		int code = 0;
		for (int i=0; i < _dim; i++)
		{
			for (int j=0; j <= i; j++)
			{
				code <<= 1;
				if (_board[i][j] != Globals.EMPTY) code |= 1;
			}
		}
		return code;
	}
	public final int left_rotated_state()
	{
		int code = 0;
		for (int j=_dim-1; j >= 0; j--)
		{
			for (int i=j; i < _dim; i++)
			{
				code <<= 1;
				if (_board[i][j] != Globals.EMPTY) code |= 1;
			}
		}
		return code;
	}
	public final int right_rotated_state()
	{
		int code = 0;
		for (int i=0; i < _dim; i++)
		{
			for (int j=0; j <= i; j++)
			{
				code <<= 1;
				if (_board[_dim-j-1][i-j] != Globals.EMPTY) code |= 1;
			}
		}
		return code;
	}
	public final int flipped_state()
	{
		int code = 0;
		for (int i=0; i < _dim; i++)
		{
			for (int j=i; j >= 0; j--)
			{
				code <<= 1;
				if (_board[i][j] != Globals.EMPTY) code |= 1;
			}
		}
		return code;
	}
	public final int left_rotated_flipped_state()
	{
		int code = 0;
		for (int i=0; i < _dim; i++)
		{
			for (int j=0; j <= i; j++)
			{
				code <<= 1;
				if (_board[_dim-i+j-1][j] != Globals.EMPTY) code |= 1;
			}
		}
		return code;
	}
	public final int right_rotated_flipped_state()
	{
		int code = 0;
		for (int j=_dim-1; j >= 0; j--)
		{
			for (int i=_dim-1; i >= j; i--)
			{
				code <<= 1;
				if (_board[i][j] != Globals.EMPTY) code |= 1;
			}
		}
		return code;
	}
	public final Board copy()
	{
		Board new_board = new Board(_dim);
		for(int i=0; i < _dim; i++)
		{
			for(int j=0; j < _dim; j++)
			{
				new_board._board[i][j] = _board[i][j];
			}
		}
		return new_board;
	}
}

class BoardCanvas extends Canvas
{
	JTriangle app;
	Board displayBoard, thinkerBoard;
	int dim, bubbleRadius, status, difficulty, animate_color, animate_step;
	int fps, rps, animateInterval, animateDiff;
	int[] results;
	boolean boardInUse, spinning;
	Pos begin, animate_pos;
	Thread lastAnimatorThread;
	Vector moves_history;
	
	class SpinnerThread extends Thread
	{
		public void run()
		{
			animate_color = Globals.EMPTY;
			animate_pos = begin;
			while(spinning)
			{
				for(animate_step = bubbleRadius; animate_step > 0 && spinning; animate_step-=animateDiff)
				{
					try	{sleep(animateInterval);}
					catch (java.lang.InterruptedException e) {}
					repaint();
				}
				for(animate_step = 0; animate_step < bubbleRadius && spinning; animate_step+=animateDiff)
				{
					try	{sleep(animateInterval);}
					catch (java.lang.InterruptedException e) {}
					repaint();
				}

			}
		}
	}
	class AnimateThread extends Thread
	{
		Move animate_move;
		int useColor;

		AnimateThread(Move move_to_do, int code)
		{
			animate_move = move_to_do;
			useColor = code;
		}
		void animate_bubble()
		{
			for(animate_step = 0; animate_step < bubbleRadius; animate_step+=animateDiff)
			{
				try	{sleep(animateInterval);}
				catch (java.lang.InterruptedException e) {}
				repaint();
			}
			displayBoard._board[animate_pos.row][animate_pos.col] = useColor;
			repaint();
		}
		public void run()
		{
			Thread threadToFollow = lastAnimatorThread;
			lastAnimatorThread = this;
			if(threadToFollow != null)
			{
				try{threadToFollow.join();}
				catch(InterruptedException e) {}
			}
				
			boardInUse = true;
			Pos beg = animate_move.beg;
			Pos end = animate_move.end;
			int y_pos = beg.row;
			int x_pos = beg.col;
			int x_displacement = (beg.col == end.col) ? 0 : (beg.col > end.col) ? -1 : 1;
			int y_displacement = (beg.row == end.row) ? 0 : (beg.row > end.row) ? -1 : 1;
		
			animate_color = useColor;
			animate_pos = new Pos(beg.row, beg.col);
			animate_bubble();
			while (x_pos != end.col || y_pos != end.row)
			{
				y_pos += y_displacement;
				x_pos += x_displacement;
				animate_pos.row = y_pos;
				animate_pos.col = x_pos;
				animate_bubble();
			}
			animate_pos = new Pos(-1, -1);
			if(lastAnimatorThread == this)
			{
				boardInUse = false;
				lastAnimatorThread = null;
			}
		}
	}
	class ThinkerThread extends Thread
	{
		int evaluate_situation()
		{
			int current_state = thinkerBoard.state();
		
			if(results[current_state] != 0)
				return results[current_state];
		
			Move opponent_move = thinkerBoard.first_valid_move();
			if(opponent_move.beg.row == -1)
			{	results[current_state] = -1;
				return -1;
			}
		
			int best_score = 1;
			while(opponent_move.beg.row != -1)				// Don't enter loop if computer lost
			{	thinkerBoard.make_move(opponent_move, Globals.RED);		// Make human test move
				int new_score = -evaluate_situation();
				thinkerBoard.make_move(opponent_move, Globals.EMPTY);	// Undo human test move
		
				if((best_score > 0 && new_score < 0) ||
					((best_score > 0 || new_score < 0) && new_score > best_score))
				{
					best_score = new_score;
				}
				opponent_move = thinkerBoard.next_valid_move(opponent_move);
			}
			int return_value = best_score > 0 ? best_score + 1 : best_score - 1;
			results[current_state] = return_value;
			results[thinkerBoard.flipped_state()] = return_value;
			results[thinkerBoard.left_rotated_state()] = return_value;
			results[thinkerBoard.right_rotated_state()] = return_value;
			results[thinkerBoard.left_rotated_flipped_state()] = return_value;
			results[thinkerBoard.right_rotated_flipped_state()] = return_value;
		
			return return_value;
		}
		public void run()
		{
			Move current_move = thinkerBoard.first_valid_move();
			if(current_move.beg.row == -1)
			{
				app.showMsg("Press New to try again.");
				status = Globals.GAME_OVER;
				return;
			}
			app.showMsg("Thinking...");
		
			int best_score = 1;
		
			Vector good_moves = new Vector();
			while(current_move.beg.row != -1)
			{
				thinkerBoard.make_move(current_move, Globals.BLUE);
				int new_score = -evaluate_situation();
				thinkerBoard.make_move(current_move, Globals.EMPTY);
		
				if((best_score > 0 && new_score < 0) ||
					((best_score > 0 || new_score < 0) && new_score >= best_score))
				{	if (new_score != best_score)
					{	good_moves.removeAllElements();
						best_score = new_score;
					}
					if (difficulty == Globals.EASY || new_score < 0 || current_move.is_one())
					{
						good_moves.addElement(current_move);
					}
				}
				current_move = thinkerBoard.next_valid_move(current_move);
			}
	
			int moveNumber = (int)(Math.random() * Integer.MAX_VALUE);
			moveNumber %= good_moves.size();
			Move move_chosen = (Move)good_moves.elementAt(moveNumber);
			status = Globals.PLAYER_TURN;
			moves_history.addElement(move_chosen);
			make_move(move_chosen, Globals.BLUE);
			
			app.showMsg("Okay, your turn");
			repaint();
		
			if(best_score == 1)
			{
				status = Globals.GAME_OVER;
				app.showMsg("You won this game!");
			}
		}
	}
		
	BoardCanvas(JTriangle useApplet, int useFps, int useRps)
	{
		app = useApplet;
		fps = useFps;
		rps = useRps;
		changeSize(5);

		setSize(Globals.BOARD_WIDTH, Globals.BOARD_HEIGHT);
		
		addMouseListener(new MouseAdapter()
			{public void mouseReleased(MouseEvent e) {mouseHandler(e);}});
	}
	public void make_move(Move move_to_do, int code)
	{
		thinkerBoard.make_move(move_to_do, code);
		(new AnimateThread(move_to_do, code)).start();
		app.checkButtonStatus();
	}
	public void onComputerTurn()
	{
		status = Globals.COMPUTER_TURN;
		(new ThinkerThread()).start();
	}
	void mouseHandler(MouseEvent e)
	{
		if (status != Globals.PLAYER_TURN || boardInUse) return;
		int x = e.getX();
		int y = e.getY();
		int row = (int)Math.floor(y / (bubbleRadius * 2));
		int col = (int)Math.floor((x - (dim - row - 1) * bubbleRadius) / (bubbleRadius * 2));
	
		if (row < 0 || row >= dim || col < 0 || col > row ||
			displayBoard.read(new Pos(row, col)) != Globals.EMPTY)
		{	begin = new Pos(-1, -1);
			animate_pos = new Pos(-1, -1);
			spinning = false;
			repaint();
			return;
		}
	
		if (begin.row == -1)
		{	begin = new Pos(row, col);
			spinning = true;
			(new SpinnerThread()).start();
			return;
		}
	
		Move current_move = new Move(begin, new Pos(row, col));
		switch(status)
		{
		case Globals.PLAYER_TURN:
			begin = new Pos(-1, -1);
			animate_pos = new Pos(-1, -1);
			spinning = false;
			repaint();
			if (displayBoard.is_valid_move(current_move))
			{	make_move(current_move, Globals.RED);
				moves_history.addElement(current_move);
			}
			else
			{
				app.showMsg("Your move must be in a line");
				return;
			}
	
			onComputerTurn();
			return;
		case Globals.GAME_OVER:
//			SetWindowText("Game Over");
			return;
		}
		begin = new Pos(-1, -1);
		animate_pos = new Pos(-1, -1);
		spinning = false;
		repaint();
		status = Globals.PLAYER_TURN;
	}
	public void reset()
	{
		displayBoard = new Board(dim);
		thinkerBoard = new Board(dim);
		
		status = Globals.PLAYER_TURN;
		begin = new Pos(-1, -1);
		animate_pos = new Pos(-1, -1);
		boardInUse = false;
		spinning = false;
		
		moves_history = new Vector();

		repaint();
		app.checkButtonStatus();
		app.showMsg("Everything is set");
	}
	public void changeSize(int newSize)
	{
		dim = newSize;
		
		results = new int[Globals.RESULTS_SIZE];
		bubbleRadius = Globals.BOARD_WIDTH / (dim * 2);
		animateInterval = 1000 / fps;
		animateDiff = 2 * bubbleRadius * rps / fps;

		reset();
	}
	public void onUndo()
	{
		int size = moves_history.size();
		if(size > 0)		
		{
			Move temp = (Move)moves_history.elementAt(size-1);
			make_move(temp, Globals.EMPTY);
			moves_history.removeElementAt(size-1);
		}
		if(size > 1)
		{
			Move temp = (Move)moves_history.elementAt(size-2);
			if (displayBoard.read(temp.beg) == Globals.RED)
			{
				make_move(temp, Globals.EMPTY);
				moves_history.removeElementAt(size-2);
			}
		}
	}
	
	public boolean passEnabled()
	{
		return moves_history.size() == 0;
	}
	public boolean undoEnabled()
	{
		return moves_history.size() > 0;
	}
	public boolean busy()
	{
		return status != Globals.PLAYER_TURN || boardInUse;
	}

	public void update(Graphics g)
	{
		paint(g);
	}
	public void paint(Graphics g)
	{
		Image img = createImage(Globals.BOARD_WIDTH, Globals.BOARD_HEIGHT);
		Graphics b = img.getGraphics();
		for (int i=0; i < dim; i++)
		{	for (int j=0; j <= i; j++)
			{
				int startX = (dim - i) * bubbleRadius + j * bubbleRadius * 2;
				int startY = i * bubbleRadius * 2;
				b.setColor(Color.white);
				b.fillRect(startX - bubbleRadius, startY, bubbleRadius*2, bubbleRadius*2);
				if (i == animate_pos.row && j == animate_pos.col)
				{
					
					startX -= animate_step;
					switch (animate_color)
					{
					case Globals.RED:
						b.setColor(Color.red);
						break;
					case Globals.GREEN:
						b.setColor(Color.green);
						break;
					case Globals.BLUE:
						b.setColor(Color.blue);
						break;
					}
						
					b.fillOval(startX, startY, animate_step*2, bubbleRadius*2);
					continue;
				}
				startX -= bubbleRadius;
				if (i == begin.row && j == begin.col)
				{
					b.setColor(Color.white);
					b.drawOval(startX, startY, bubbleRadius*2, bubbleRadius*2);
				}
				else
				{
					switch (displayBoard.read(new Pos(i, j)))
					{
					case Globals.RED:
						b.setColor(Color.red);
						break;
					case Globals.GREEN:
						b.setColor(Color.green);
						break;
					case Globals.BLUE:
						b.setColor(Color.blue);
						break;
					}
					b.fillOval(startX, startY, bubbleRadius*2, bubbleRadius*2);
				}
			}
		}
		g.drawImage(img,0,0,this);
	}
}

public class JTriangle extends Applet implements ActionListener, ItemListener
{
	BoardCanvas gameBoard;
	Button newButton, passButton, undoButton;
	Label output;
	Choice sizeList;
	public void init()
	{
		String fpsIn = getParameter("fps");
		String rpsIn = getParameter("rps");
		int fps, rps;
		try
		{
			fps = Integer.parseInt(fpsIn);
			rps = Integer.parseInt(rpsIn);
		}
		catch(NumberFormatException e)
		{
			fps = Globals.ANIMATE_FPS;
			rps = Globals.ANIMATE_RPS;
		}
		
		newButton = new Button("New");
		newButton.addActionListener(this);
		newButton.setActionCommand(Globals.NEW_GAME);

		passButton = new Button("Pass");
		passButton.addActionListener(this);
		passButton.setActionCommand(Globals.PASS_MOVE);
		
		undoButton = new Button("Undo");
		undoButton.addActionListener(this);
		undoButton.setActionCommand(Globals.UNDO_MOVE);
		
		sizeList = new Choice();
		sizeList.addItem("4");
		sizeList.addItem("5");
		sizeList.addItem("6");
		sizeList.addItemListener(this);
		sizeList.select(1);
		
		output = new Label();
		output.setAlignment(Label.CENTER);

		gameBoard = new BoardCanvas(this, fps, rps);
		
		this.setBackground(Color.white);
		
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		gridbag.setConstraints(gameBoard, c);
		add(gameBoard);

		c.gridheight = GridBagConstraints.BOTH;
		c.weighty = 1;
		gridbag.setConstraints(output, c);
		add(output);
		
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weighty = 0;
		gridbag.setConstraints(newButton, c);
		add(newButton);
		
		gridbag.setConstraints(passButton, c);
		add(passButton);
		
		gridbag.setConstraints(undoButton, c);
		add(undoButton);
		
		gridbag.setConstraints(sizeList, c);
		add(sizeList);
	}
	public void checkButtonStatus()
	{
		try
		{
			passButton.setEnabled(gameBoard.passEnabled());
			undoButton.setEnabled(gameBoard.undoEnabled());
		}
		catch(NullPointerException e) {}
	}
	public void showMsg(String msg)
	{
		output.setText(msg);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(gameBoard.busy())
			return;

		String command = e.getActionCommand();
		if(command == Globals.NEW_GAME)
		{
			gameBoard.reset();
		}
		else if(command == Globals.PASS_MOVE)
		{
			gameBoard.onComputerTurn();
		}
		else if(command == Globals.UNDO_MOVE)
		{
			gameBoard.onUndo();
		}
	}
	public void itemStateChanged(ItemEvent e)
	{
		if(gameBoard.busy())
			return;

		try {gameBoard.changeSize(Integer.parseInt(sizeList.getSelectedItem()));}
		catch(NumberFormatException nfe) {}
	}
}