package jp.soars.otp;

/**
 * エージェントの状態（位置と移動方法）
 */
public class TOtpState {

    /** 移動方法。徒歩、車、バスなど。 */
    private String fMode;

    /** 緯度 */
    private double fLatitude;

    /** 経度 */
    private double fLongitude;

    /**
     * コンストラクタ
     * @param mode 移動方法
     * @param latitude 緯度
     * @param longitude 経度
     */
    public TOtpState(String mode, double latitude, double longitude) {
        fMode = mode;
        fLatitude = latitude;
        fLongitude = longitude;
    }
    
    @Override
    public String toString() {
        return fMode + "," + fLatitude + "," + fLongitude;
    }

    /**
     * 移動方法を返す。
     * @return 移動方法
     */
    public String getMode() {
        return fMode;
    }

    /**
     * 緯度を返す。
     * @return 緯度
     */
    public double getLatitude() {
        return fLatitude;
    }

    /**
     * 経度を返す。
     * @return 経度
     */
    public double getLongitude() {
        return fLongitude;
    }
}
