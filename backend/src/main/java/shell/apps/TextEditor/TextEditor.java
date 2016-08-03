/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.TextEditor;

import device.Device;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import logging.Logger;
import logging.LoggingCategory;
import shell.apps.TerminalApplication;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;
import telnetd.io.terminal.ColorHelper;
import telnetd.io.toolkit.Component;
import telnetd.io.toolkit.Statusbar;
import telnetd.io.toolkit.Titlebar;
import telnetd.io.toolkit.myToolkit.ComponentReDrawer;
import telnetd.io.toolkit.myToolkit.MyEditArea;
import telnetd.io.toolkit.myToolkit.Quitable;

/**
 *
 * @author Martin Lukáš
 */
public class TextEditor extends TerminalApplication implements ComponentReDrawer {

	private boolean quit = false;
	List<Component> componentsToDraw;
	private static String originalStatusText = "CTRL+S => SAVE, CTRL+X => QUIT";
	private String filePath;
	/**
	 * queue of quitable components to be called when quiting
	 */
	private List<Quitable> quitQueue = new LinkedList<>();

	public TextEditor(BasicTerminalIO terminalIO, Device device, String filePath) {
		super(terminalIO, device);
		this.componentsToDraw = new LinkedList<>();
		this.filePath = filePath;
	}

	@Override
	public void addComponentDraw(Component component) {
		this.componentsToDraw.add(component);
	}

	@Override
	public void drawComponents() {

		try {
			for (Component activeComponent : componentsToDraw) {
				activeComponent.draw();
			}
			terminalIO.flush();
		} catch (IOException ex) {
			Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Failed to draw a activeComponent");
		}

	}

	@Override
	public int run() {
		try {

			final LinkedList<String> tempLines = new LinkedList<>();
			try {
				this.device.getFilesystem().runInputFileJob(filePath, new InputFileJob() {

					@Override
					public int workOnFile(InputStream input) throws Exception {

						Scanner sc = new Scanner(input);

						while (sc.hasNext()) {
							tempLines.add(sc.nextLine());
						}

						return 0;
					}
				});
			} catch (FileNotFoundException ex) {
				try {
					if (this.device.getFilesystem().createNewFile(filePath)) {
						tempLines.add("new file created: " + filePath);
					} else {
						terminalIO.write("New file cannot be created in destination: " + filePath + TerminalIO.CRLF);
						return -1;
					}
				} catch (FileNotFoundException ex1) {
						terminalIO.write("New file cannot be created in destination: " + filePath + TerminalIO.CRLF);
						return -1;
				}
			}


			terminalIO.eraseScreen();
			terminalIO.homeCursor();
			//myio.flush();
			Titlebar tb2 = new Titlebar(terminalIO, "title 1");
			tb2.setTitleText("TextEditor");
			tb2.setAlignment(Titlebar.ALIGN_LEFT);
			tb2.setBackgroundColor(ColorHelper.BLUE);
			tb2.setForegroundColor(ColorHelper.YELLOW);
			this.addComponentDraw(tb2);

			Statusbar sb2 = new Statusbar(terminalIO, "status 1");
			sb2.setStatusText(originalStatusText);
			sb2.setAlignment(Statusbar.ALIGN_LEFT);
			sb2.setBackgroundColor(ColorHelper.BLUE);
			sb2.setForegroundColor(ColorHelper.YELLOW);
			this.addComponentDraw(sb2);


			terminalIO.setCursor(2, 1);

			final MyEditArea ea = new MyEditArea(terminalIO, "edit area", terminalIO.getRows() - 2, Integer.MAX_VALUE);
			ea.setComponentReDrawer(this);

			this.quitQueue.add(ea);

			ArrayList<String> lines = new ArrayList<>(tempLines);

			ea.setValue(lines);

			ea.draw();
			terminalIO.flush();

			this.drawComponents();

			while (!quit) {

				ea.run();

				int exitCode = ea.getExitCode();

				switch (exitCode) {
					case TerminalIO.CTRL_X:
						this.quit();
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Quiting from textEditor");
						break;
					case TerminalIO.CTRL_S:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Saving into file:" + filePath);
						sb2.setStatusText("Saved");
						this.device.getFilesystem().runOutputFileJob(filePath, new OutputFileJob() {

							@Override
							public int workOnFile(OutputStream output) throws Exception {
								PrintWriter wr = new PrintWriter(output);

								wr.print(ea.getValue());
								wr.flush();
								return 0;
							}
						});

						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Saved into file:" + filePath);
						sb2.draw();
						terminalIO.flush();
						sb2.setStatusText(originalStatusText);
						this.addComponentDraw(sb2);  // redraw later
						break;
				}

			}

			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "TextEditor quit with this value:" + BasicTerminalIO.CRLF + ea.getValue());

		} catch (IOException ex) {

			if (quit) // if there is a quit request, then it is ok
			{
				return 0;
			} else {
				this.quit();
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Exception occured, when reading a line from telnet, closing program: " + "TextEditor");
				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, ex.toString());
				return -1;
			}
		}

		return 0;


	}

	@Override
	public int quit() {
		try {
			this.quit = true;

			for (Quitable component : this.quitQueue) {
				component.quit();
			}

			this.terminalIO.eraseScreen();
			this.terminalIO.homeCursor();

		} catch (IOException ex) {
		}

		return 0;
	}
}
