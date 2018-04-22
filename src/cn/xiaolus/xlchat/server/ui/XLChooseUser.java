package cn.xiaolus.xlchat.server.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cn.xiaolus.xlchat.server.protocol.ChooseListener;

import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class XLChooseUser extends JDialog {

	private static final long serialVersionUID = 4130470477319710150L;
	private ChooseListener chooseListener;
	private final JPanel contentPanel = new JPanel();
	private JComboBox<String> comboBoxSelectUser;
	public static String ALL = "所有用户";

	/**
	 * Create the dialog.
	 */
	public XLChooseUser() {
		setTitle("XLChat服务器 - 选择用户");
		setResizable(false);
		setBounds(100, 100, 300, 150);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel centerPanel = new JPanel();
			contentPanel.add(centerPanel, BorderLayout.CENTER);
			{
				JLabel label = new JLabel("请选择用户：");
				centerPanel.add(label);
			}
			{
				comboBoxSelectUser = new JComboBox<>();
				centerPanel.add(comboBoxSelectUser);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						finishChooseUser();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void addChooseListener(ChooseListener c) {
		this.chooseListener = c;
		DefaultComboBoxModel<String> comboBoxModel = c.provideComboBoxModel();
		comboBoxModel.addElement(ALL);
		comboBoxSelectUser.setModel(comboBoxModel);
	}
	
	public void finishChooseUser() {
		String selected = (String)comboBoxSelectUser.getSelectedItem();
		int reply = JOptionPane.showConfirmDialog(this, selected+"将会立即被注销，您确定吗？", "操作确认" , JOptionPane.YES_NO_OPTION);
		if(reply == JOptionPane.OK_OPTION) {
			chooseListener.didFinishChooseUser(selected);
		}
		dispose();
	}

}
