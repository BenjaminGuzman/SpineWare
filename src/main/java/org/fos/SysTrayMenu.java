package org.fos;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.io.InputStream;

public class SysTrayMenu extends JDialog {
	private final ActionListener onClickExitButton;
	private final ActionListener onClickOpenButton;

	public SysTrayMenu(final JFrame owner, final ActionListener onClickExitButton, final ActionListener onClickOpenButton) {
		super(owner, "SpineWare");
		this.onClickExitButton = onClickExitButton;
		this.onClickOpenButton = onClickOpenButton;

		this.setUndecorated(true);
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.setModal(false);
		this.setType(Type.POPUP);
		this.setModalityType(ModalityType.MODELESS);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		JPanel mainPanel = this.initComponents();
		this.setContentPane(mainPanel);
		this.pack();

		this.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				SysTrayMenu.this.setVisible(false);
			}
		});
	}

	public JPanel initComponents() {
		JPanel mainPanel = new JPanel(new GridBagLayout());

		InputStream inputStreamSWLogo = SWMain.getImageAsStream("/resources/media/SW_white.min.png");
		ImageIcon swLogoImageIcon = null;
		try {
			Image img = ImageIO.read(inputStreamSWLogo);
			img = img.getScaledInstance(50, 44, Image.SCALE_AREA_AVERAGING);
			swLogoImageIcon = new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel swLogoImageLabel = null;
		if (swLogoImageIcon != null)
			swLogoImageLabel = new JLabel(swLogoImageIcon);
		else
			swLogoImageLabel = new JLabel("SW");

		JButton exitButton = new JButton(SWMain.messagesBundle.getString("systray_exit"));
		JButton openButton = new JButton(SWMain.messagesBundle.getString("systray_open"));

		exitButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
			this.onClickExitButton.actionPerformed(evt);
		});
		openButton.addActionListener((ActionEvent evt) -> {
			this.setVisible(false);
			this.onClickOpenButton.actionPerformed(evt);
		});

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		gridBagConstraints.ipady = 10;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;

		mainPanel.add(swLogoImageLabel, gridBagConstraints);

		gridBagConstraints.gridy = 1;
		mainPanel.add(exitButton, gridBagConstraints);

		gridBagConstraints.gridy = 2;
		mainPanel.add(openButton, gridBagConstraints);

		return mainPanel;
	}
}
