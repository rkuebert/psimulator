package telnetd.io.toolkit.myToolkit;

import java.io.IOException;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;
import telnetd.io.toolkit.Editarea;
import telnetd.io.toolkit.Editline;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class MyEditArea extends Editarea implements Quitable{

	private int exitCode = 0;
	private boolean quit = false;
	/**
	 * reference to component redrawer
	 */
	private ComponentReDrawer componentReDrawer;
	

	public MyEditArea(BasicTerminalIO io, String name, int rowheight, int maxrows) {
		super(io, name, rowheight, maxrows);
	}

	/**
	 *
	 * @param componentReDrawer
	 */
	public void setComponentReDrawer(ComponentReDrawer componentReDrawer) {
		this.componentReDrawer = componentReDrawer;
	}

	public int getExitCode() {
		return exitCode;
	}

	@Override
	public void run() throws IOException {

		int oldcursor = 0;
		this.quit = false;
		m_IO.setAutoflushing(false);
		//check flag
		if (m_Firstrun) {
			//reset flag
			m_Firstrun = false;
			//make a new editline
			if (lines.isEmpty()) {
				line = createLine();
				appendLine(line);
			}
		}

		do {
			//switch return of a line
			
			int editLineRet = line.run();
			switch (editLineRet) {
				case BasicTerminalIO.UP:
					if (m_RowCursor > 0) {
						if (m_FirstVisibleRow == m_RowCursor) {
							scrollUp();
						} else {
							cursorUp();
						}
					} else {
						m_IO.bell();
					}
					break;
				case BasicTerminalIO.DOWN:

					if (m_RowCursor < (lines.size() - 1)) {
						if (m_RowCursor == m_FirstVisibleRow + (m_Dim.getHeight() - 1)) {
							scrollDown();
						} else {
							cursorDown();
						}
					} else {
						m_IO.bell();
					}
					break;
				case BasicTerminalIO.ENTER:

					if (m_RowCursor == (m_Rows - 1)) {
						quit = true;
					} else {
						if (!hasLineSpace()) {
							m_IO.bell();
						} else {
							String wrap = line.getHardwrap();
							line.setHardwrapped(true);

							if (m_RowCursor == (lines.size() - 1)) {
								appendNewLine();
							} else {
								insertNewLine();
							}
							//cursor
							m_RowCursor++;
							//activate new row
							activateLine(m_RowCursor);
							//set value of new row
							try {
								line.setValue(wrap);
								line.setCursorPosition(0);
								m_IO.moveLeft(line.size());
							} catch (Exception ex) {
							}
						}
					}
					break;

				case BasicTerminalIO.LEFT:
					if (m_RowCursor > 0) {
						if (m_FirstVisibleRow == m_RowCursor) {
							scrollUp();
							line.setCursorPosition(line.size());
							m_IO.moveRight(line.size());
						} else {
							//Cursor
							m_RowCursor--;
							//buffer
							activateLine(m_RowCursor);
							line.setCursorPosition(line.size());

							//screen
							m_IO.moveUp(1);
							m_IO.moveRight(line.size());
						}
					} else {
						m_IO.bell();
					}
					break;
				case BasicTerminalIO.RIGHT:
					if (m_RowCursor < (lines.size() - 1)) {
						if (m_RowCursor == m_FirstVisibleRow + (m_Dim.getHeight() - 1)) {
							line.setCursorPosition(0);
							m_IO.moveLeft(line.size());
							scrollDown();
						} else {
							//Cursor
							m_RowCursor++;
							//screen horizontal
							m_IO.moveLeft(line.size());
							//buffer
							activateLine(m_RowCursor);
							line.setCursorPosition(0);
							//screen
							m_IO.moveDown(1);
						}
					} else {
						m_IO.bell();
					}
					break;
				case BasicTerminalIO.BACKSPACE:
					if (m_RowCursor == 0 || line.size() != 0 || m_RowCursor == m_FirstVisibleRow) {
						m_IO.bell();
					} else {
						//take line from buffer
						//and draw update all below
						removeLine();
					}
					break;
				case TerminalIO.CTRL_S:
				case TerminalIO.CTRL_X:
					this.exitCode = editLineRet;
					return;

				case TerminalIO.ESCAPE:  // IGNORE
					//done = true;
					break;


				default:
					if (!hasLineSpace()) {
						m_IO.bell();
					} else {
						String wrap = line.getSoftwrap();
						//System.out.println("softwrap:"+wrap);
						line.setHardwrapped(false);

						if (m_RowCursor == (lines.size() - 1)) {
							appendNewLine();
						} else {
							insertNewLine();
						}
						//cursor
						m_RowCursor++;
						//activate new row
						activateLine(m_RowCursor);
						//set value of new row
						try {
							line.setValue(wrap);
							//getLine(rowCursor-1).getLastRelPos();
							//line.setCursorPosition(0);
							//myIO.moveLeft(line.size());
						} catch (Exception ex) {
						}
					}

			}
			m_IO.flush();
		} while (!quit);


	}

	@Override
	protected Editline createLine() {
		MyEditLine editLine = new MyEditLine(m_IO);
		editLine.setComponentReDrawer(this.componentReDrawer);
		return editLine;
	}

	@Override
	public int quit() {
		this.quit = true;
		
		MyEditLine editLine = (MyEditLine) this.line;
		editLine.quit();
		
		return 0;
	}
}
