//License
/***
 * Java TelnetD library (embeddable telnet daemon)
 * Copyright (c) 2000-2005 Dieter Wimberger 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***/

package telnetd.io;

import telnetd.io.terminal.Terminal;
import telnetd.io.terminal.TerminalManager;
import telnetd.net.Connection;
import telnetd.net.ConnectionData;
import telnetd.net.ConnectionEvent;


import java.io.IOException;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Class for Terminal specific I/O.
 * It represents the layer between the application layer and the generic telnet I/O.
 * Terminal specific I/O is achieved via pluggable terminal classes
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 * @see telnetd.io.TelnetIO
 * @see telnetd.io.terminal.Terminal
 */
public class TerminalIO
    implements BasicTerminalIO {


  private TelnetIO m_TelnetIO;					//low level I/O

  private Connection m_Connection;			//the connection this instance is working for
  private ConnectionData m_ConnectionData;	//holds data of the connection
  private Terminal m_Terminal;					//active terminal object

  //Members
  private boolean m_AcousticSignalling;		//flag for accoustic signalling
  private boolean m_Autoflush;					//flag for autoflushing mode
  private boolean m_ForceBold;				//flag for forcing bold output
  private boolean m_LineWrapping;

  /**
   * Constructor of the TerminalIO class.
   *
   * @param con Connection the instance will be working for
   */
  public TerminalIO(Connection con) {
    m_Connection = con;
    m_AcousticSignalling = true;
    m_Autoflush = true;

    //store the associated  ConnectionData instance
    m_ConnectionData = m_Connection.getConnectionData();
    try {
      //create a new telnet io
      m_TelnetIO = new TelnetIO();
      m_TelnetIO.setConnection(con);
      m_TelnetIO.initIO();
    } catch (Exception ex) {
      //handle, at least log
    }

    //set default terminal
    try {
      setDefaultTerminal();
    } catch (Exception ex) {
		Logger.log(Logger.WARNING, LoggingCategory.TELNET, "TerminalIO()" + ex);
      
      throw new RuntimeException();
    }
  }//constructor



  /************************************************************************
   * Visible character I/O methods   				                        *
   ************************************************************************/

  /**
   * Read a single character and take care for terminal function calls.
   *
   * @return <ul>
   *         <li>character read
   *         <li>IOERROR in case of an error
   *         <li>DELETE,BACKSPACE,TABULATOR,ESCAPE,COLORINIT,LOGOUTREQUEST
   *         <li>UP,DOWN,LEFT,RIGHT
   *         </ul>
   */
  public  int read() throws IOException {
    int i = m_TelnetIO.read();
   // System.out.print("Opravdu přečtena hodnota:"+i);
    //translate possible control sequences
    i = m_Terminal.translateControlCharacter(i);

    //System.out.println(" a přeložená hodnota:"+i);
    //catch & fire a logoutrequest event
    if (i == LOGOUTREQUEST) {
      m_Connection.processConnectionEvent(new ConnectionEvent(m_Connection, ConnectionEvent.CONNECTION_LOGOUTREQUEST));
      i = HANDLED;
    } else if (i > 256 && i == ESCAPE) {
      //translate an incoming escape sequence
      i = handleEscapeSequence(i);
    }

    //return i holding a char or a defined special key
    return i;
  }//read

  public  void write(byte b) throws IOException {
    m_TelnetIO.write(b);
    if (m_Autoflush) {
      flush();
    }
  }//write

    public void write(int i) throws IOException {
    m_TelnetIO.write(i);
    if (m_Autoflush) {
      flush();
    }
  }//write


  public  void write(char ch) throws IOException {
    m_TelnetIO.write(ch);
    if (m_Autoflush) {
      flush();
    }
  }//write(char)

  public  void write(String str) throws IOException {
    if (m_ForceBold) {
      m_TelnetIO.write(m_Terminal.formatBold(str));
    } else {
      m_TelnetIO.write(m_Terminal.format(str));
    }
    if (m_Autoflush) {
      flush();
    }
  }//write(String)


  /*** End of Visible character I/O methods  ******************************/



  /**
   * *********************************************************************
   * Erase methods							                            *
   * **********************************************************************
   */

  public  void eraseToEndOfLine() throws IOException {
    doErase(EEOL);
  }//eraseToEndOfLine

  public  void eraseToBeginOfLine() throws IOException {
    doErase(EBOL);
  }//eraseToBeginOfLine

  public  void eraseLine() throws IOException {
    doErase(EEL);
  }//eraseLine

  public  void eraseToEndOfScreen() throws IOException {
    doErase(EEOS);
  }//eraseToEndOfScreen

  public  void eraseToBeginOfScreen() throws IOException {
    doErase(EBOS);
  }//eraseToBeginOfScreen

  public  void eraseScreen() throws IOException {
    doErase(EES);
  }//eraseScreen

  private  void doErase(int funcConst) throws IOException {

    m_TelnetIO.write(m_Terminal.getEraseSequence(funcConst));
    if (m_Autoflush) {
      flush();
    }
  }//erase

  /*** End of Erase methods  **********************************************/

  /**
   * *********************************************************************
   * Cursor related methods							                    *
   * **********************************************************************
   */

  public  void moveCursor(int direction, int times) throws IOException {

    m_TelnetIO.write(m_Terminal.getCursorMoveSequence(direction, times));
    if (m_Autoflush) {
      flush();
    }
  }//moveCursor

  public void moveLeft(int times) throws IOException {
    moveCursor(LEFT, times);
  }//moveLeft

  public  void moveRight(int times) throws IOException {
    moveCursor(RIGHT, times);
  }//moveRight

  public  void moveUp(int times) throws IOException {
    moveCursor(UP, times);
  }//moveUp

  public  void moveDown(int times) throws IOException {
    moveCursor(DOWN, times);
  }//moveDown

  public  void setCursor(int row, int col) throws IOException {
    int[] pos = new int[2];
    pos[0] = row;
    pos[1] = col;
    m_TelnetIO.write(m_Terminal.getCursorPositioningSequence(pos));
    if (m_Autoflush) {
      flush();
    }
  }//setCursor

  public void homeCursor() throws IOException {
    m_TelnetIO.write(m_Terminal.getCursorPositioningSequence(HOME));
    if (m_Autoflush) {
      flush();
    }
  }//homeCursor

  public void storeCursor() throws IOException {
    m_TelnetIO.write(m_Terminal.getSpecialSequence(STORECURSOR));
  }//store Cursor

  public  void restoreCursor() throws IOException {
    m_TelnetIO.write(m_Terminal.getSpecialSequence(RESTORECURSOR));
  }//restore Cursor

  /*** End of cursor related methods **************************************/


  /**
   * *********************************************************************
   * Special terminal function methods							        *
   * **********************************************************************
   */


  public  void setSignalling(boolean bool) {
    m_AcousticSignalling = bool;
  }//setAcousticSignalling


  public  boolean isSignalling() {
    return m_AcousticSignalling;
  }//isAcousticSignalling

  /**
   * Method to write the NVT defined BEL onto the stream.
   * If signalling is off, the method simply returns, without
   * any action.
   */
  public  void bell() throws IOException {
    if (m_AcousticSignalling) {
      m_TelnetIO.write(BEL);
    }
    if (m_Autoflush) {
      flush();
    }
  }//bell

  /**
   * EXPERIMENTAL, not defined in the interface.
   */
  public  boolean defineScrollRegion(int topmargin, int bottommargin) throws IOException {
    if (m_Terminal.supportsScrolling()) {
      m_TelnetIO.write(m_Terminal.getScrollMarginsSequence(topmargin, bottommargin));
      flush();
      return true;
    } else {
      return false;
    }
  }//defineScrollRegion

  public  void setForegroundColor(int color) throws IOException {
    if (m_Terminal.supportsSGR()) {
      m_TelnetIO.write(m_Terminal.getGRSequence(FCOLOR, color));
      if (m_Autoflush) {
        flush();
      }
    }
  }//setForegroundColor

  public  void setBackgroundColor(int color) throws IOException {
    if (m_Terminal.supportsSGR()) {
      //this method adds the offset to the fg color by itself
      m_TelnetIO.write(m_Terminal.getGRSequence(BCOLOR, color + 10));
      if (m_Autoflush) {
        flush();
      }
    }
  }//setBackgroundColor

  public  void setBold(boolean b) throws IOException {
    if (m_Terminal.supportsSGR()) {
      if (b) {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, BOLD));
      } else {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, BOLD_OFF));
      }
      if (m_Autoflush) {
        flush();
      }
    }
  }//setBold

  public  void forceBold(boolean b) {
    m_ForceBold = b;
  }//forceBold

  public  void setUnderlined(boolean b) throws IOException {
    if (m_Terminal.supportsSGR()) {
      if (b) {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, UNDERLINED));
      } else {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, UNDERLINED_OFF));
      }
      if (m_Autoflush) {
        flush();
      }

    }
  }//setUnderlined

  public  void setItalic(boolean b) throws IOException {
    if (m_Terminal.supportsSGR()) {
      if (b) {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, ITALIC));
      } else {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, ITALIC_OFF));
      }
      if (m_Autoflush) {
        flush();
      }
    }
  }//setItalic


  public  void setBlink(boolean b) throws IOException {
    if (m_Terminal.supportsSGR()) {
      if (b) {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, BLINK));
      } else {
        m_TelnetIO.write(m_Terminal.getGRSequence(STYLE, BLINK_OFF));
      }
      if (m_Autoflush) {
        flush();
      }
    }
  }//setItalic

  public  void resetAttributes() throws IOException {
    if (m_Terminal.supportsSGR()) {
      m_TelnetIO.write(m_Terminal.getGRSequence(RESET, 0));
    }
  }//resetGR

  /*** End of special terminal function methods ***************************/

  /************************************************************************
   * Auxiliary I/O methods						                        *
   ************************************************************************/

  /**
   * Method that parses forward for escape sequences
   */
  private int handleEscapeSequence(int i) throws IOException {
    if (i == ESCAPE) {
      int[] bytebuf = new int[m_Terminal.getAtomicSequenceLength()];
      //fill atomic length
      //FIXME: ensure CAN, broken Escapes etc.
      for (int m = 0; m < bytebuf.length; m++) {
        bytebuf[m] = m_TelnetIO.read();
		
		if(bytebuf[m] == Terminal.ESC)
			return ESCAPE;  // double escape read
		
      }
      return m_Terminal.translateEscapeSequence(bytebuf, m_TelnetIO);
    }
    if (i == BYTEMISSING) {
      //FIXME:longer escapes etc...
    }

    return HANDLED;
  }//handleEscapeSequence

  /**
   * Accessor method for the autoflushing mechanism.
   */
  public boolean isAutoflushing() {
    return m_Autoflush;
  }//isAutoflushing

  public  void resetTerminal() throws IOException {
    m_TelnetIO.write(m_Terminal.getSpecialSequence(DEVICERESET));
  }

  public  void setLinewrapping(boolean b) throws IOException {
    if (b && !m_LineWrapping) {
      m_TelnetIO.write(m_Terminal.getSpecialSequence(LINEWRAP));
      m_LineWrapping = true;
      return;
    }
    if (!b && m_LineWrapping) {
      m_TelnetIO.write(m_Terminal.getSpecialSequence(NOLINEWRAP));
      m_LineWrapping = false;
      return;
    }
  }//setLineWrapping

  public boolean isLineWrapping() {
    return m_LineWrapping;
  }//

  /**
   * Mutator method for the autoflushing mechanism.
   */
  public  void setAutoflushing(boolean b) {
    m_Autoflush = b;
  }//setAutoflushing


  /**
   * Method to flush the Low-Level Buffer
   */
  public  void flush() throws IOException {
    m_TelnetIO.flush();
  }//flush (implements the famous iToilet)


  public  void close() {
    m_TelnetIO.closeOutput();
    m_TelnetIO.closeInput();
  }//close

  /*** End of Auxiliary I/O methods  **************************************/



  /************************************************************************
   * Terminal management specific methods			                        *
   ************************************************************************/

  /**
   * Accessor method to get the active terminal object
   *
   * @return Object that implements Terminal
   */
  public Terminal getTerminal() {
    return m_Terminal;
  }//getTerminal

  /**
   * Sets the default terminal ,which will either be
   * the negotiated one for the connection, or the systems
   * default.
   */
  public void setDefaultTerminal() throws IOException {
    //set the terminal passing the negotiated string
    setTerminal(m_ConnectionData.getNegotiatedTerminalType());
  }//setDefaultTerminal

  /**
   * Mutator method to set the active terminal object
   * If the String does not name a terminal we support
   * then the vt100 is the terminal of selection automatically.
   *
   * @param terminalName String that represents common terminal name
   */
  public void setTerminal(String terminalName) throws IOException {

    m_Terminal = TerminalManager.getReference().getTerminal(terminalName);
    //Terminal is set we init it....
    initTerminal();
    //debug message
	Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Set terminal to " + m_Terminal.toString());
    
  }//setTerminal


  /**
   * Terminal initialization
   */
  private  void initTerminal() throws IOException {
    m_TelnetIO.write(m_Terminal.getInitSequence());
    flush();
  }//initTerminal

  /**
   *
   */
  public int getRows() {
    return m_ConnectionData.getTerminalRows();
  }//getRows

  /**
   *
   */
  public int getColumns() {
    return m_ConnectionData.getTerminalColumns();
  }//getColumns


  /**
   * Accessor Method for the terminal geometry changed flag
   */
  public boolean isTerminalGeometryChanged() {
    return m_ConnectionData.isTerminalGeometryChanged();
  }//isTerminalGeometryChanged

  /*** End of terminal management specific methods  ***********************/

  /** Constants Declaration  **********************************************/

  /**
   * Terminal independent representation constants for terminal
   * functions.
   */
  public static final int[] HOME = {0, 0};

  public static final int
      IOERROR = -1;		//IO error
  public static final int// Positioning 10xx
      UP = 1001; 		//one up
  public static final int DOWN = 1002; 		//one down
  public static final int RIGHT = 1003; 	//one left
  public static final int LEFT = 1004; 		//one right
  //HOME=1005,		//Home cursor pos(0,0)


  public static final int// Functions 105x
      STORECURSOR = 1051;	//store cursor position + attributes
  public static final int RESTORECURSOR = 1052;	//restore cursor + attributes

  public static final int// Erasing 11xx
      EEOL = 1100;		//erase to end of line
  public static final int EBOL = 1101;		//erase to beginning of line
  public static final int EEL = 1103;		//erase entire line
  public static final int EEOS = 1104;		//erase to end of screen
  public static final int EBOS = 1105;		//erase to beginning of screen
  public static final int EES = 1106;		//erase entire screen

  public static final int// Escape Sequence-ing 12xx
      ESCAPE = 1200;		//Escape
  public static final int BYTEMISSING = 1201;	//another byte needed
  public static final int UNRECOGNIZED = 1202;	//escape match missed

  public static final int// Control Characters 13xx
      ENTER = 1300;		//LF is ENTER at the moment
  public static final int TABULATOR = 1301;	 	//Tabulator
  public static final int DELETE = 1302;		//Delete
  public static final int BACKSPACE = 1303;		//BACKSPACE
  public static final int COLORINIT = 1304;		//Color inited
  public static final int HANDLED = 1305;
  public static final int LOGOUTREQUEST = 1306;		//CTRL-D beim login

  /**
   * Internal UpdateType Constants
   */
  public static final int
      LineUpdate = 475,
  CharacterUpdate = 476,
  ScreenpartUpdate = 477;

  /**
   * Internal BufferType Constants
   */
  public static final int
      EditBuffer = 575,
  LineEditBuffer = 576;

  /**
   * Network Virtual Terminal Specific Keys
   * Thats what we have to offer at least.
   */
  public static final int BEL = 7;
  public static final int BS = 8;
  public static final int DEL = 127;
  public static final int CR = 13;
  public static final int LF = 10;

  public static final int FCOLOR = 10001;
  public static final int BCOLOR = 10002;
  public static final int STYLE = 10003;
  public static final int RESET = 10004;
  public static final int BOLD = 1;
  public static final int BOLD_OFF = 22;
  public static final int ITALIC = 3;
  public static final int ITALIC_OFF = 23;
  public static final int BLINK = 5;
  public static final int BLINK_OFF = 25;
  public static final int UNDERLINED = 4;
  public static final int UNDERLINED_OFF = 24;
  public static final int DEVICERESET = 10005;
  public static final int LINEWRAP = 10006;
  public static final int NOLINEWRAP = 10007;

  /** end Constants Declaration  ******************************************/

  /**NEW CONSTANTS DECLARATION **/
  // SOURCE : EXPERIMENTAL + http://nemesis.lonestar.org/reference/telecom/codes/ascii.html
  public static final int CTRL_C = 3;
  public static final int CTRL_Z = 26;
  public static final int CTRL_W = 23;
  public static final int CTRL_L = 12;
  public static final int CTRL_D = 1305;
  public static final int CTRL_R = 18;
  public static final int CTRL_S = 19;
  public static final int CTRL_X = 24;
  public static final int HOME_KEY = 1072;
  public static final int END_KEY = 1070;
  public static final int PAGE_DOWN = 1054;
  public static final int PAGE_UP = 1053;
  public static final int CTRL_SHIFT_6 = 30;
  
  

	@Override
	public boolean avaiable() {
		return m_TelnetIO.avaiable();
	}
  
  
}//class TerminalIO
