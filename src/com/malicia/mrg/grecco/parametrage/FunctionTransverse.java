package com.malicia.mrg.grecco.parametrage;

import javax.swing.JLabel;
import javax.swing.JTextArea;

public class FunctionTransverse {
	protected static JTextArea LtextArea;
	protected static JLabel labelProgression;
	public FunctionTransverse() {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number //$NON-NLS-1$
												// with optional
												// '-' and decimal.
	}

	protected static void msg(String string) {
		LtextArea.setText(string + "\r\n" + LtextArea.getText()); //$NON-NLS-1$
		System.out.println(string);
	}

	protected static void msgProg(String string) {
		labelProgression.setText(string);
	}

}
