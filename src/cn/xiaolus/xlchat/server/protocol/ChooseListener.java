package cn.xiaolus.xlchat.server.protocol;

import javax.swing.DefaultComboBoxModel;

public interface ChooseListener {
	public DefaultComboBoxModel<String> provideComboBoxModel();
	public void didFinishChooseUser(String choice);
}
