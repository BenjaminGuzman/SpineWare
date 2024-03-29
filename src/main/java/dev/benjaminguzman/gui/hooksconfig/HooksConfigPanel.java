/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.benjaminguzman.gui.hooksconfig;

import dev.benjaminguzman.SpineWare;
import dev.benjaminguzman.gui.Fonts;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class HooksConfigPanel extends JPanel
{
	private final FileNameExtensionFilter onlySoundFilesFilter;
	private final boolean chooseDirInsteadOfFile; // if this is true, the file chooser won't allow the user to
	private boolean initialized; // flag to avoid calling initComponents more than once
	private JCheckBox enabledCheckBox;
	private JLabel selectedAudioLabel;
	private JTextField cmdTextField;
	// choose a file, just a directory

	/**
	 * Constructs the object
	 * <p>
	 * This constructor will delegate to the {@link #HooksConfigPanel(boolean)}
	 * with a value of false, meaning that the user must choose a file instead of a directory to play sound
	 */
	public HooksConfigPanel()
	{
		this(false);
	}

	/**
	 * Constructs the object
	 *
	 * @param chooseDirInsteadOfFile if true, the use must choose a directory containing multiple audio files
	 *                               instead of choosing a single file to play
	 */
	public HooksConfigPanel(boolean chooseDirInsteadOfFile)
	{
		super();

		this.chooseDirInsteadOfFile = chooseDirInsteadOfFile;
		if (this.chooseDirInsteadOfFile) {
			this.onlySoundFilesFilter = null;
			return;
		}

		String[] supportedAudioFileExtensions = getSupportedAudioFileExtensions();

		this.onlySoundFilesFilter = new FileNameExtensionFilter(
			"Supported audio files: " + Arrays.toString(supportedAudioFileExtensions),
			supportedAudioFileExtensions
		);
	}

	public static String[] getSupportedAudioFileExtensions()
	{
		Set<String> supportedExtensions = Arrays.stream(AudioSystem.getAudioFileTypes())
		                                        .map(AudioFileFormat.Type::getExtension)
		                                        .collect(Collectors.toSet());

		// jlayer dependency allow us to reproduce mp3 files
		supportedExtensions.add("mp3");

		return supportedExtensions.toArray(new String[0]);
	}

	public String getSelectedAudio()
	{
		String selectedAudio = this.selectedAudioLabel.getText().trim();
		return selectedAudio.isEmpty() ? null : selectedAudio;
	}

	public void setSelectedAudio(String selection)
	{
		this.selectedAudioLabel.setText(selection == null ? " " : selection);
	}

	public String getCmd()
	{
		String cmd = this.cmdTextField.getText().trim();
		return cmd.isEmpty() ? null : cmd;
	}

	public void setCmd(String cmd)
	{
		this.cmdTextField.setText(cmd);
	}

	public boolean isFeatureEnabled()
	{
		return this.enabledCheckBox.isSelected();
	}

	public void setFeatureEnabled(boolean enabled)
	{
		this.enabledCheckBox.setSelected(enabled);
		this.enabledCheckBox.getActionListeners()[0].actionPerformed(null);
	}

	/**
	 * Creates the panel containing all GUI elements
	 *
	 * @param hookTitle   the title for the hook (a JLabel will be added to the panel)
	 * @param audioDesc   the description for the audio
	 * @param selectAudio the text to display in the "select audio" button
	 * @param noAudio     the text to display in the "no audio" button
	 * @param executeCmd  the execute command label to indicate the user he/she can input a command to execute
	 */
	public void initComponents(
		String hookTitle,
		String audioDesc,
		String selectAudio,
		String noAudio,
		String executeCmd
	)
	{
		if (this.initialized)
			throw new RuntimeException("You shouldn't be calling initComponents more than once");
		this.initialized = true;

		this.setLayout(new GridBagLayout());

		/*
		First row:
			Hook title label        enabled checkbox
		 */
		JLabel hookTitleLabel = new JLabel(hookTitle);
		hookTitleLabel.setFont(Fonts.SANS_SERIF_BOLD_15);
		this.enabledCheckBox = new JCheckBox(SpineWare.messagesBundle.getString("feature_enabled"));

		/*
		Second row:
			Play audio file         select audio file       no audio file
		 */
		JLabel audioDescLabel = new JLabel(audioDesc);
		JButton selectAudioBtn = new JButton(selectAudio);
		JButton noAudioBtn = new JButton(noAudio);

		/*
		Third row:
			label showing the current selected file/directory
		 */
		this.selectedAudioLabel = new JLabel(" "); // space tells the layout manager to display the component
		this.selectedAudioLabel.setFont(Fonts.MONOSPACED_BOLD_12);
		this.selectedAudioLabel.setHorizontalAlignment(SwingConstants.CENTER);

		/*
		Fourth row:
			Execute command
		 */
		JLabel executeCmdLabel = new JLabel(executeCmd);

		/*
		Fifth row:
			input for the desired command to execute
		 */
		this.cmdTextField = new JTextField();
		this.cmdTextField.setFont(Fonts.MONOSPACED_BOLD_12);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		/*
		Add first row:
			Hook title label        enabled checkbox
		 */
		gbc.weightx = 2;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(hookTitleLabel, gbc);

		gbc.weightx = 0;
		gbc.gridx = 2;
		++gbc.gridy;
		gbc.fill = GridBagConstraints.NONE;
		this.add(enabledCheckBox, gbc);

		/*
		Add second row:
			Play audio file         select audio file       no audio file
		 */
		gbc.weightx = 2;
		gbc.gridx = 0;
		++gbc.gridy;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(audioDescLabel, gbc);

		gbc.weightx = 0;
		++gbc.gridx;
		gbc.fill = GridBagConstraints.NONE;
		this.add(selectAudioBtn, gbc);

		++gbc.gridx;
		this.add(noAudioBtn, gbc);

		/*
		Third row:
			label showing the current selected file/directory
		 */
		gbc.weightx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		++gbc.gridy;
		this.add(selectedAudioLabel, gbc);

		/*
		Fourth row:
			Execute command
		 */
		++gbc.gridy;
		this.add(executeCmdLabel, gbc);

		/*
		Fifth row:
			input for the desired command to execute
		 */
		++gbc.gridy;
		gbc.ipadx = 10;
		gbc.ipady = 10;
		this.add(cmdTextField, gbc);


		// set listeners for buttons
		selectAudioBtn.addActionListener((ActionEvent evt) -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(
				this.chooseDirInsteadOfFile
					? JFileChooser.DIRECTORIES_ONLY
					: JFileChooser.FILES_ONLY
			);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(this.onlySoundFilesFilter);

			int choice = fileChooser.showSaveDialog(this);

			if (choice == JFileChooser.CANCEL_OPTION) {
				selectedAudioLabel.setText(" ");
				return;
			}

			String selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			selectedAudioLabel.setText(selectedFilePath);
		});
		noAudioBtn.addActionListener((ActionEvent evt) -> selectedAudioLabel.setText(" "));

		// add listener for checkbox
		this.enabledCheckBox.addActionListener((ActionEvent evt) -> {
			boolean enabled = this.enabledCheckBox.isSelected();

			selectAudioBtn.setEnabled(enabled);
			noAudioBtn.setEnabled(enabled);
			this.cmdTextField.setEnabled(enabled);
		});
	}
}
