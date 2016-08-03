package telnetd.io.toolkit.myToolkit;

import java.io.IOException;
import java.net.SocketTimeoutException;
import logging.Logger;
import logging.LoggingCategory;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;
import telnetd.io.toolkit.BufferOverflowException;
import telnetd.io.toolkit.Editline;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class MyEditLine extends Editline implements Quitable {

	/**
	 * reference to component redrawer
	 */
	private ComponentReDrawer componentReDrawer;
	private boolean quit = false;

	public MyEditLine(BasicTerminalIO io) {
		super(io);
	}

	/**
	 *
	 * @param componentReDrawer
	 */
	public void setComponentReDrawer(ComponentReDrawer componentReDrawer) {
		this.componentReDrawer = componentReDrawer;
	}

	@Override
	public int run() throws IOException {
		int in;
		this.quit = false;
		//draw();
		//myIO.flush();
		do {
			//get next key
			try {
				in = m_IO.read();
			} catch (SocketTimeoutException ex) {
				continue;
			}
			//store cursorpos
			m_LastCursPos = m_Cursor;

			this.componentReDrawer.drawComponents();

			switch (in) {
				case BasicTerminalIO.LEFT:
					if (!moveLeft()) {
						return in;
					}
					break;
				case BasicTerminalIO.RIGHT:
					if (!moveRight()) {
						return in;
					}
					break;
				case BasicTerminalIO.BACKSPACE:
					try {
						if (m_Cursor == 0) {
							return in;
						} else {
							removeCharAt(m_Cursor - 1);
						}
					} catch (IndexOutOfBoundsException ioobex) {
						m_IO.bell();
					}
					break;
				case BasicTerminalIO.DELETE:
					try {
						removeCharAt(m_Cursor);
					} catch (IndexOutOfBoundsException ioobex) {
						m_IO.bell();
					}
					break;
				case BasicTerminalIO.TABULATOR: {
					try {
						this.append("   ");
//						this.append("\t");
					} catch (BufferOverflowException ex) {
						Logger.log(Logger.WARNING, LoggingCategory.TELNET, "BufferOverflowException occured when running EditLine component");
					}

				}
				break;
				case TerminalIO.HOME_KEY: {
					while (moveLeft()) {
					}
				}
				break;

				case TerminalIO.END_KEY: {
					while (moveRight()) {
					}
				}
				break;

// EXIT KEYS					
				case BasicTerminalIO.ENTER:
				case BasicTerminalIO.UP:
				case BasicTerminalIO.DOWN:
				case TerminalIO.ESCAPE:
				case TerminalIO.CTRL_S:		// save key
				case TerminalIO.CTRL_X:		// quit key
					return in;

				case TerminalIO.UNRECOGNIZED:  // do nothing
					break;

				default:
					try {
						handleCharInput(in);
					} catch (BufferOverflowException boex) {
						setLastRead((char) in);
						return in;
					}
			}
			m_IO.flush();
		} while (!quit);
		return 0;
	}

	@Override
	public int quit() {
		this.quit = true;
		return 0;
	}
}
