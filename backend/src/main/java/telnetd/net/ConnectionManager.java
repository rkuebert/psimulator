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

package telnetd.net;

import telnetd.BootException;


import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.*;
import logging.Logger;
import logging.LoggingCategory;

/**
 * Class that takes care for active and queued connection.
 * Housekeeping is done also for connections that were just broken
 * off, or exceeded their timeout. Note that instances of this class can
 * only be created by using the factory method createConnectionManager(Properties settings).
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public class ConnectionManager
    implements Runnable {

  

  private Thread m_Thread;
  private ThreadGroup m_ThreadGroup;	//ThreadGroup all connections run in
  private List m_OpenConnections;
  private Stack m_ClosedConnections;
  private ConnectionFilter m_Filter; //reference to the connection filter
  private int m_MaxConnections;			//maximum allowed connections stored from the properties
  private int m_WarningTimeout;		//time to idle warning
  private int m_DisconnectTimeout;		//time to idle diconnection
  private int m_HousekeepingInterval;	//interval for managing cleanups
  private String m_LoginShell;
  private boolean m_LineMode = false;
  private boolean m_Stopping = false;


  private ConnectionManager(int con, int timew, int timedis,
                            int hoke, ConnectionFilter filter,
                            String lsh, boolean lm) {
    m_ThreadGroup = new ThreadGroup(new StringBuffer().append(this.toString()).append("Connections").toString());
    m_OpenConnections = Collections.synchronizedList(new ArrayList(100));
    //m_BrokenConnections = new Stack();
    m_ClosedConnections = new Stack();
    m_Filter = filter;
    m_LoginShell = lsh;
    m_LineMode = lm;
    m_MaxConnections = con;
    m_WarningTimeout = timew;
    m_DisconnectTimeout = timedis;
    m_HousekeepingInterval = hoke;
  }//constructor

  /**
   * Set a connection filter for this
   * ConnectionManager instance. The filter is used to handle
   * IP level allow/deny of incoming connections.
   *
   * @param filter ConnectionFilter instance.
   */
  public void setConnectionFilter(ConnectionFilter filter) {
    m_Filter = filter;
  }//setConnectionFilter

  /**
   * Gets the active ConnectionFilter instance or
   * returns null if no filter is set.
   *
   * @return the managers ConnectionFilter.
   */
  public ConnectionFilter getConnectionFilter() {
    return m_Filter;
  }//getConnectionFilter

  /**
   * Returns the number of open connections.
   * @return the number of open connections as <tt>int</tt>.
   */
  public int openConnectionCount() {
    return m_OpenConnections.size();
  }//openConnectionCount

  /**
   * Returns the {@link Connection} at the given index.
   * @param idx
   * @return
   */
  public Connection getConnection(int idx) {
    synchronized(m_OpenConnections) {
      return (Connection) m_OpenConnections.get(idx);
    }
  }//getConnection

  /**
   * Get all {@link Connection} instances with the given
   * <tt>InetAddress</tt>.
   *
   * @return all {@link Connection} instances with the given
   *         <tt>InetAddress</tt>.
   */
  public Connection[] getConnectionsByAdddress(InetAddress addr) {
    ArrayList l = new ArrayList();
    synchronized(m_OpenConnections) {
      for (Iterator iterator = m_OpenConnections.iterator(); iterator.hasNext();) {
        Connection connection = (Connection) iterator.next();
        if(connection.getConnectionData().getInetAddress().equals(addr)) {
          l.add(connection);
        }
      }
    }
    Connection[] conns = new Connection[l.size()];
    return (Connection[])l.toArray(conns);
  }//getConnectionsByAddress

  /**
   * Starts this <tt>ConnectionManager</tt>.
   */
  public void start() {
    m_Thread = new Thread(this);
    m_Thread.start();
  }//start

  /**
   * Stops this <tt>ConnectionManager</tt>.
   */
  public void stop() {
	  Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "stop()::" +  this.toString() );
    
    m_Stopping = true;
    //wait for thread to die
    try {
      m_Thread.join();
    } catch (InterruptedException iex) {
		Logger.log(Logger.ERROR, LoggingCategory.TELNET, "stop()" + iex );
      
    }
    synchronized(m_OpenConnections) {
      for (Iterator iter = m_OpenConnections.iterator(); iter.hasNext();) {
        try {
          Connection tc = (Connection) iter.next();
          //maybe write a disgrace to the socket?
          tc.close();
        } catch (Exception exc) {
			
			Logger.log(Logger.ERROR, LoggingCategory.TELNET, "stop()" + exc);
          
        }
      }
      m_OpenConnections.clear();
    }
	Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "stop():: Stopped " + this.toString() );
    
  }//stop

  /**
   * Method that that tries to connect an incoming request.
   * Properly  queueing.
   *
   * @param insock Socket thats representing the incoming connection.
   */
  public void makeConnection(Socket insock) {
	  Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "makeConnection()::" + insock.toString());
    
    if (m_Filter == null ||
        (m_Filter != null && m_Filter.isAllowed(insock.getInetAddress()))) {
      //we create the connection data object at this point to
      //store certain information there.
      ConnectionData newCD = new ConnectionData(insock, this);
      newCD.setLoginShell(m_LoginShell);
      newCD.setLineMode(m_LineMode);
      if (m_OpenConnections.size() < m_MaxConnections) {
        //create a new Connection instance
        Connection con = new Connection(m_ThreadGroup, newCD);
        //log the newly created connection
        Object[] args = {new Integer(m_OpenConnections.size() + 1)};
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, MessageFormat.format("connection #{0,number,integer} made.", args));
        
        //register it for being managed
        synchronized(m_OpenConnections) {
          m_OpenConnections.add(con);
        }
        //start it
        con.start();
      }
    } else {
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "makeConnection():: Active Filter blocked incoming connection.");
      
      try {
        insock.close();
      } catch (IOException ex) {
        //do nothing or log.
      }
    }
  }//makeConnection


  /**
   * Periodically does following work:
   * <ul>
   * <li> cleaning up died connections.
   * <li> checking managed connections if they are working properly.
   * <li> checking the open connections.
   * </ul>
   */
  public void run() {
    //housekeep connections
    try {
      do {
        //clean up and close all broken connections
        //cleanupBroken();
        //clean up closed connections
        cleanupClosed();
        //check all active connections
        checkOpenConnections();
        //sleep interval
        Thread.sleep(m_HousekeepingInterval);
      } while (!m_Stopping);

    } catch (Exception e) {
		Logger.log(Logger.ERROR, LoggingCategory.TELNET, "run()" + e);
      
    }
	Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "run():: Ran out " + this.toString() );
    
  }//run

  /*
  private void cleanupBroken() {
    //cleanup loop
    while (!m_BrokenConnections.isEmpty()) {
      Connection nextOne = (Connection) m_BrokenConnections.pop();
      log.info("cleanupBroken():: Closing broken connection " + nextOne.toString());
      //fire logoff event for shell site cleanup , beware could hog the daemon thread
      nextOne.processConnectionEvent(new ConnectionEvent(nextOne, ConnectionEvent.CONNECTION_BROKEN));
      //close the connection, will be automatically registered as closed
      nextOne.close();
    }
  }//cleanupBroken
*/
  private void cleanupClosed() {
    if(m_Stopping) {
      return;
    }
    //cleanup loop
    while (!m_ClosedConnections.isEmpty()) {
      Connection nextOne = (Connection) m_ClosedConnections.pop();
	  Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "cleanupClosed():: Removing closed connection " + nextOne.toString());
      
      synchronized(m_OpenConnections) {
        m_OpenConnections.remove(nextOne);
      }
    }
  }//cleanupBroken

  private void checkOpenConnections() {
    if(m_Stopping) {
      return;
    }
    //do routine checks on active connections
    synchronized(m_OpenConnections) {
      for (Iterator iter = m_OpenConnections.iterator(); iter.hasNext();) {
        Connection conn = (Connection) iter.next();
        ConnectionData cd = conn.getConnectionData();
        //check if it is dead and remove it.
        if (!conn.isActive()) {
          registerClosedConnection(conn);
          continue;
        }
        /* Timeouts check */
        //first we caculate the inactivity time
        long inactivity = System.currentTimeMillis() - cd.getLastActivity();
        //now we check for warning and disconnection
        if (inactivity > m_WarningTimeout) {
          //..and for disconnect
          if (inactivity > (m_DisconnectTimeout + m_WarningTimeout)) {
            //this connection needs to be disconnected :)
			  Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "checkOpenConnections():" + conn.toString() + " exceeded total timeout.");
            
            //fire logoff event for shell site cleanup , beware could hog the daemon thread
            conn.processConnectionEvent(new ConnectionEvent(conn, ConnectionEvent.CONNECTION_TIMEDOUT));
            //conn.close();
          } else {
            //this connection needs to be warned :)
            if (!cd.isWarned()) {
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET,"checkOpenConnections():" + conn.toString() + " exceeded warning timeout." );
              
              cd.setWarned(true);
              //warning event is fired but beware this could hog the daemon thread!!
              conn.processConnectionEvent(new ConnectionEvent(conn, ConnectionEvent.CONNECTION_IDLE));
            }
          }
        }
      }
      /* end Timeouts check */
    }
  }//checkConnections

  /**
   * Called by connections that got broken (i.e. I/O errors).
   * The housekeeper will properly close the connection,
   * and take care for misc necessary cleanup.
   *
   * @param con the connection that is broken.
   *
  public void registerBrokenConnection(Connection con) {
    if (!m_BrokenConnections.contains(con) && !m_ClosedConnections.contains(con)) {
      log.debug("registerBrokenConnection()::" + con.toString());
      m_BrokenConnections.push(con);
    }
  }//registerBrokenConnection
*/
  public void registerClosedConnection(Connection con) {
    if(m_Stopping) {
      return;
    }
    if (!m_ClosedConnections.contains(con)) {
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "registerClosedConnection()::" + con.toString());
      m_ClosedConnections.push(con);
    }
  }//unregister

  /**
   * Factory method for the ConnectionManager.<br>
   * A class operation that will return a new ConnectionManager instance.
   *
   * @param settings Properties containing the settings for this instance.
   */
  public static ConnectionManager createConnectionManager(String name, Properties settings)
      throws BootException {

    try {
      int maxc = Integer.parseInt(settings.getProperty(name + ".maxcon"));
      int timow = Integer.parseInt(settings.getProperty(name + ".time_to_warning"));
      int timodis = Integer.parseInt(settings.getProperty(name + ".time_to_timedout"));
      int hoke = Integer.parseInt(settings.getProperty(name + ".housekeepinginterval"));
      String filterclass = settings.getProperty(name + ".connectionfilter");
      ConnectionFilter filter = null;
      String loginshell = "";
      boolean linemode = false;
      if (filterclass != null && filterclass.length() != 0 && !filterclass.toLowerCase().equals("none")) {
        //load filter
        filter = (ConnectionFilter) Class.forName(filterclass).newInstance();
        filter.initialize(settings);
      }
      loginshell = settings.getProperty(name + ".loginshell");
      if (loginshell == null || loginshell.length() == 0) {
		  Logger.log(Logger.ERROR, LoggingCategory.TELNET, "Login shell not specified.");
        
        throw new BootException("Login shell must be specified.");
      }
      String inputmode = settings.getProperty(name + ".inputmode");
      if (inputmode == null || inputmode.length() == 0) {
		  Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Input mode not specified using character input as default.");
        
        linemode = false;
      } else if (inputmode.toLowerCase().equals("line")) {
        linemode = true;
      }
      //return fabricated manager
      ConnectionManager cm = new ConnectionManager(maxc, timow, timodis, hoke, filter, loginshell, linemode);
      //set higher priority!
      //cm.setPriority(Thread.NORM_PRIORITY + 2);
      return cm;
    } catch (Exception ex) {
		Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "createConnectionManager():" + ex);
      
      throw new BootException("Failure while creating ConnectionManger instance:\n" + ex.getMessage());
    }
  }//createManager


}//class ConnectionManager