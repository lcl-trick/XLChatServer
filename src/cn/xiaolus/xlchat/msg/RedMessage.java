package cn.xiaolus.xlchat.msg;

/**
 * 红包消息
 * @author 小路
 *
 */
public class RedMessage extends AbstractMessage {

	private static final long serialVersionUID = -6500346701676157773L;
	
//	红包总金额
	protected double totalAmount;
//	红包总个数
	protected int number;
//	消息状态码：发红包，接收红包，返回红包金额，红包已领完，红包超时未领取
	public static int SEND = 100, RECEIVE = 90, REPLY = 80, RUNOUT = -90;
//	消息状态码
	protected int status;
//	红包有效期为一天
	protected int ttl = 1440;
	
	public double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
}
