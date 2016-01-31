package com.malicia.mrg.grecco.parametrage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ExtParam {

	private static JPanel panel;
	private static JPanel StaticPanel;
	private static JLabel Staticlabel;
	private static JScrollPane Dynlabel;
	private static JTextArea textArea;
	private static final JFrame frame = new JFrame();

	private static List<String> listOfFiles = new ArrayList<>();

	public static void main(String[] args) {
		// Setup stuff
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(400, 360));
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		// Add panel stuff
		panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createLineBorder(frame.getForeground(), 1));

		Staticlabel = new JLabel("...");
		Staticlabel.setBorder(BorderFactory.createLineBorder(frame.getForeground(), 1));

		textArea = new JTextArea(19, 30);
		Dynlabel = new JScrollPane(textArea);
		Dynlabel.setBorder(BorderFactory.createLineBorder(frame.getForeground(), 1));

		panel.add(Staticlabel, BorderLayout.NORTH);
		panel.add(Dynlabel, BorderLayout.SOUTH);

		frame.getContentPane().add(panel);

		// Show frame
		frame.pack();
		frame.setVisible(true);

		loadListFile(true);
		for (String nmTable : listOfFiles) {
//			nmTable = "TC1RMEA";
			frame.setTitle(nmTable);
			TableDb2 tb = new TableDb2(nmTable, Staticlabel, textArea);
			tb.mapChampsdeExcel();
		}
	}

	private static void loadListFile(boolean reset) {

		URL url = ExtParam.class.getResource(Messages.getString("TableDb2.resourceRepertoireFichierDefinitionTable")); //$NON-NLS-1$
		File parentDirectory;
		try {
			parentDirectory = new File(new URI(url.toString()));
			loadListFile(parentDirectory, reset);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void loadListFile(File fileInRepertoire, boolean reset) {
		if (reset && listOfFiles != null) {
			listOfFiles.clear();
		}
		if (fileInRepertoire.isDirectory()) {
			File[] sousrepertoire = fileInRepertoire.listFiles();
			for (int i = 0; i < sousrepertoire.length; i++) {
				loadListFile(sousrepertoire[i], false);
			}
		} else {
			listOfFiles.add(fileInRepertoire.getName());
		}
	}
}
