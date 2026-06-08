package cn.tealc.taygedo;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 设备身份标识
 * 模拟iOS设备的三个唯一标识符，用于API请求的设备指纹
 */
public class DeviceIdentity {
    /** 设备ID，32位十六进制随机字符串 */
    private String deviceId;
    /** OpenUDID，大写UUID格式 */
    private String openudid;
    /** Vendor ID，大写UUID格式 */
    private String vendorid;

    public DeviceIdentity() {
    }

    public DeviceIdentity(String deviceId, String openudid, String vendorid) {
        this.deviceId = deviceId;
        this.openudid = openudid;
        this.vendorid = vendorid;
    }

    /**
     * 生成随机设备身份
     * deviceId为16字节随机数的十六进制表示，
     * openudid和vendorid为随机UUID的大写形式
     */
    public static DeviceIdentity generate() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder hex = new StringBuilder(32);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return new DeviceIdentity(
                hex.toString(),
                UUID.randomUUID().toString().toUpperCase(),
                UUID.randomUUID().toString().toUpperCase()
        );
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOpenudid() {
        return openudid;
    }

    public void setOpenudid(String openudid) {
        this.openudid = openudid;
    }

    public String getVendorid() {
        return vendorid;
    }

    public void setVendorid(String vendorid) {
        this.vendorid = vendorid;
    }
}
